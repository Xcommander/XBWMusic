package com.kidosc.kidomusic.audio;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
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
    private Application mApp;
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
                while (mMediaPlayer==null){
                    //等待初始化OK
                }
                return mMediaPlayer;
            case MPD:
                return null;//TODO
            default:
                return null;
        }
    }

    public MediaPlayerService getmMediaPlayerCallback () {
        return mMediaPlayerCallback;
    }


    private AudioUtils(Application application) {
        mApp = application;
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
                mApp.sendBroadcast(intent);
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
    //获取当前的mediaservice的链接情况
    public boolean getServiceConnecion(){
        return mIsServiceConnection;
    }

    private void initPlayers () {
        if(mApp==null){
            Log.e("xulinchao", "initPlayers: null");
        }else{
            Log.e("xulinchao", "initPlayers: not null" );
        }
        Intent intent = new Intent(mApp, MediaPlayerService.class);
        mApp.startService(intent);
        mApp.bindService(intent,conn, Context.BIND_AUTO_CREATE);
    }

    public void release () {
        Intent intent = new Intent(mApp, MediaPlayerService.class);
        mApp.stopService(intent);
        mApp.unbindService(conn);
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
        void onStateChanged (AudioPlayerConst.PlayerState state, int position, boolean isPlaying);
        void onComplete ();
        void onError (int extra);
        void onPosition(int position);
    }
}
