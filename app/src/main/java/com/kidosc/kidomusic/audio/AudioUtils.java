package com.kidosc.kidomusic.audio;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * Created by jason.xu on 2018/5/4.
 */
public class AudioUtils {
    public enum AudioType {
        MEDIA, MPD
    }

    private MediaPlayerService mMediaPlayerCallback;
    private OnAudioListener onAudioListener;

    private IAudioPlayer mMediaPlayer;

    private static String TAG = "AudioUtils";

    private static AudioUtils mInstance;
    private WeakReference<Application> mApp;
    private boolean mIsServiceConnection=false;

    public static AudioUtils getInstance (Application application) {
        if (mInstance == null) {
            mInstance = new AudioUtils(application);
        }
        return mInstance;
    }

    public IAudioPlayer getPlayer (AudioType type) {
        switch (type) {
            case MEDIA:
                return mMediaPlayer;
            default:
                return null;
        }
    }

    public MediaPlayerService getmMediaPlayerCallback () {
        return mMediaPlayerCallback;
    }


    private AudioUtils(Application application) {
        mApp = new WeakReference<>(application);
        initPlayers();
    }

    ServiceConnection conn =new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMediaPlayer = ((MediaPlayerService.ServiceBinder) service).getMediaPlayerService();
            mMediaPlayerCallback = ((MediaPlayerService.ServiceBinder) service).getService();
            if (onAudioListener!=null) {
                mMediaPlayerCallback.setonAudioListener(onAudioListener);
            }else {
                //发送广播
                Intent intent = new Intent();
                intent.setAction("com.kidosc.music.register.listener");
                mApp.get().sendBroadcast(intent);
            }
            mIsServiceConnection=true;
            Log.i(TAG,"aaaaaamMediaPlayerCallback==null:"+(mMediaPlayerCallback==null));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMediaPlayer.stop();
            mMediaPlayer = null;
            mMediaPlayerCallback = null;
            mIsServiceConnection=false;
        }
    };

    /**
     * 获取当前的media service的链接情况
     *
     * @return 连接success为true，连接fail为false
     */
    public boolean getServiceConnecion(){
        return mIsServiceConnection;
    }

    private void initPlayers () {
        if(mApp.get()==null){
            Log.e("xulinchao", "initPlayers: null");
        }else{
            Log.e("xulinchao", "initPlayers: not null" );
        }
        Intent intent = new Intent(mApp.get(), MediaPlayerService.class);
        mApp.get().startService(intent);
        mApp.get().bindService(intent,conn, Context.BIND_AUTO_CREATE);
    }

    public void release () {
        Intent intent = new Intent(mApp.get(), MediaPlayerService.class);
        mApp.get().stopService(intent);
        mApp.get().unbindService(conn);
        mApp.clear();
        mApp = null;
        mInstance = null;
    }

    public void setOnAudioListener(OnAudioListener onAudioListener) {
        this.onAudioListener = onAudioListener;
        if (mMediaPlayerCallback!=null){
            mMediaPlayerCallback.setonAudioListener(onAudioListener);
        }
    }

    public interface OnAudioListener {
        /**
         *音乐状态发生变化，回调该接口
         * @param state
         * @param position
         * @param isPlaying
         */
        void onStateChanged (AudioPlayerConst.PlayerState state, int position, boolean isPlaying);

        /**
         * 当本首音乐播放完成之后，回调该接口
         */
        void onComplete ();

        /**
         * 当播放发生错误的时候，进行回调
         * @param extra
         */
        void onError (int extra);

        /**
         * 更新当前music播放的进度,周期是100ms
         * @param position
         */
        void onPosition(final int position);
    }
}
