package com.kidosc.kidomusic.audio;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;

/**
 * Created by jason.xu on 2018/5/4.
 */
public class MediaPlayerService extends Service implements IAudioPlayer {
    private MediaPlayer mediaPlayer;
    private ServiceBinder binder;
    private AudioManager audioManager;
    private AudioPlayerConst.PlayerState state;
    private boolean startAfterPrepare;
    private AudioUtils.OnAudioListener onAudioListener;
    private static String TAG = "MediaPlayerService";
    private String mDataSource;
    Handler mPlayHandler = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG,"----------------onCreate------");
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setOnPreparedListener(onPreparedListener);
        mediaPlayer.setOnCompletionListener(onCompletionListener);
        mediaPlayer.setOnErrorListener(onErrorListener);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        state = AudioPlayerConst.PlayerState.UNINITED;
        Log.i(TAG,"mediaPlayer==null:"+(mediaPlayer==null));
        startAfterPrepare = false;
        HandlerThread handlerThread = new HandlerThread("play_music",android.os.Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();
        mPlayHandler = new Handler(handlerThread.getLooper());
        musicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        Log.i(TAG,"musicVolume:"+musicVolume);
//        acquireWakeLock();
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (binder == null) {
            binder = new ServiceBinder();
        }
        return binder;
    }

    public class ServiceBinder extends Binder {
        /**
         * 获取当前Service的实例
         * @return
         */
        public MediaPlayerService getService(){
            return MediaPlayerService.this;
        }

        /**
         * 获取当前Service的实例
         * @return
         */
        public IAudioPlayer getMediaPlayerService(){
            return new MediaPlayerServiceBinder(MediaPlayerService.this);
        }
    }

    /**
     * 注册回调接口的方法，供外部调用
     * @param onAudioListener
     */
    public void setonAudioListener(AudioUtils.OnAudioListener onAudioListener) {
        Log.i(TAG,"setonAudioListener--onAudioListener==null:"+(onAudioListener==null));
        this.onAudioListener = onAudioListener;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"----------------onStartCommand------");
        return START_STICKY;
    }

    private void onStateChanged () {
        Log.i(TAG,"MediaPlayerService--onStateChanged-isPlaying():"+isPlaying()+
                ",getPosition():"+getPosition()+"onAudioListener!=null:"+(onAudioListener!=null));
        if (onAudioListener!=null){
            onAudioListener.onStateChanged(state,getPosition(),isPlaying());
        }else {
            //发送广播
            Intent intent = new Intent();
            intent.setAction("com.kidosc.music.register.listener");
            this.sendBroadcast(intent);
        }
    }

    @Override
    public synchronized void setDataSource(String dataSource) {
        Log.i(TAG,"setDataSource()---dataSource:"+dataSource);
        mDataSource = dataSource;
        startAfterPrepare = false;
        mPlayHandler.post(new Runnable() {
            @Override
            public void run() {
                preparePlayer();
            }
        });
    }

    public boolean preparePlayer () {
        if (mediaPlayer==null) {
            return false;
        }
        if (TextUtils.isEmpty(mDataSource)) {
            state = AudioPlayerConst.PlayerState.UNINITED;
            if (onAudioListener!=null) {
                onAudioListener.onError(AudioPlayerConst.PlayerConsts.ErrorExtra.DATA_SOURCE_ERROR);
            }
            releaseWakeLock();
            return false;
        }
        mediaPlayer.reset();

        try {
            mediaPlayer.setDataSource(mDataSource);
            mediaPlayer.prepareAsync();
            state = AudioPlayerConst.PlayerState.PREPARING;
            return true;
        } catch (IOException e) {
            releaseWakeLock();
            e.printStackTrace();
        }
        return false;
    }


    public void reset() {
        if (mediaPlayer==null) {
            return;
        }
        Log.i(TAG,"reset");
        mediaPlayer.reset();
    }


    @Override
    public boolean stop() {
        if (mediaPlayer==null) {
            return false;
        }

        if (state == AudioPlayerConst.PlayerState.PREPARING) {
            mediaPlayer.reset();
            return false;
        }
        if (getPosition() > 0) {
//            mediaPlayer.stop();
            mediaPlayer.pause();
        }
//        state = AudioPlayerConst.PlayerState.STOP;
        state = AudioPlayerConst.PlayerState.PAUSE;
        onStateChanged();
        releaseWakeLock();
        return true;
    }

    @Override
    public boolean playOrPause() {
        if (mediaPlayer==null) {
            return false;
        }
        mPlayHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG,"playOrPause--state:"+state);
                if (state == AudioPlayerConst.PlayerState.PLAYING) {
                    //暂停
                    mediaPlayer.pause();
                    state = AudioPlayerConst.PlayerState.PAUSE;
                    onStateChanged();
                    releaseWakeLock();
                } else if (state == AudioPlayerConst.PlayerState.PAUSE
                        || state == AudioPlayerConst.PlayerState.STOP) {
                    //播放
                    // audioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                    //startAfterPrepare = true;
                    //preparePlayer();
                    acquireWakeLock();
                    audioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                    mediaPlayer.start();
                    state = AudioPlayerConst.PlayerState.PLAYING;
                    onStateChanged();

                } else if (state == AudioPlayerConst.PlayerState.PREPARED) {
                    //开始
                    acquireWakeLock();
                    audioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                    mediaPlayer.start();
                    state = AudioPlayerConst.PlayerState.PLAYING;
                    onStateChanged();
                } else if (state == AudioPlayerConst.PlayerState.PREPARING){
                    //正在prepare
                    acquireWakeLock();
                    startAfterPrepare = true;
                } else if (state == AudioPlayerConst.PlayerState.UNINITED) {
                    //初始化
                    acquireWakeLock();
                    startAfterPrepare = true;
                    preparePlayer();
                }
            }
        });
        return true;
    }

    @Override
    public boolean seek(int pos) {
        if (mediaPlayer==null) {
            return false;
        }
        if (state == AudioPlayerConst.PlayerState.UNINITED) {
            return false;
        }
        mediaPlayer.seekTo(pos);
        mPlayHandler.post(new Runnable() {
            @Override
            public void run() {
                mediaPlayer.start();
                state = AudioPlayerConst.PlayerState.PLAYING;
                onStateChanged();
            }
        });
        return true;
    }

    @Override
    public int getPosition() {
        if (mediaPlayer==null) {
            return -1;
        }
        if (state == AudioPlayerConst.PlayerState.UNINITED) {
            return -1;
        }
        return mediaPlayer.getCurrentPosition();
    }

    @Override
    public boolean isPlaying() {
        return state == AudioPlayerConst.PlayerState.PLAYING;
    }

    @Override
    public void setVolume(int volume) {
        musicVolume = volume;
    }

    @Override
    public int getVolume() {
        return 0;
    }

    @Override
    public boolean isAlive() {
        return mediaPlayer != null;
    }

    private boolean mIsPlaying = false;
    private int musicVolume;
    private int systemVolume;
    /**
     * AUDIOFOCUS_GAIN：你已经获得音频焦点；
     AUDIOFOCUS_LOSS：你已经失去音频焦点很长时间了，必须终止所有的音频播放。因为长时间的失去焦点后，不应该在期望有焦点返回，这是一个尽可能清除不用资源的好位置。例如，应该在此时释放MediaPlayer对象；
     AUDIOFOCUS_LOSS_TRANSIENT：这说明你临时失去了音频焦点，但是在不久就会再返回来。此时，你必须终止所有的音频播放，但是保留你的播放资源，因为可能不久就会返回来。
     AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK：这说明你已经临时失去了音频焦点，但允许你安静的播放音频（低音量），而不是完全的终止音频播放。目前所有的情况下，oFocusChange的时候停止mediaPlayer
     */
    private AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            Log.i(TAG,"onAudioFocusChange--focusChange:"+focusChange);
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    Log.i(TAG,"AudioManager.AUDIOFOCUS_GAIN:"+focusChange);
                    if (mediaPlayer==null) {
                        return;
                    }

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // 还原音量

//                    private int[] mMusicLevelArray = {0,4,8,12,15};//媒体音量等级  1===musicVolume:11,systemVolume:5
//                    private int[] mSettingLevelArray = {0,1,3,5,7};// 0 1 2 3 4 系统音量等级

                    if (musicVolume<4) {
                        musicVolume = 0;
                        systemVolume = 0;
                    }else if (musicVolume < 8) {
                        musicVolume = 4;
                        systemVolume = 1;
                    }else if (musicVolume<12) {
                        musicVolume = 8;
                        systemVolume = 3;
                    }else if (musicVolume <15){
                        musicVolume = 12;
                        systemVolume = 5;
                    }else if (musicVolume <=15){
                        musicVolume = 15;
                        systemVolume = 7;
                    }
                    Log.i(TAG,"22222===musicVolume:"+musicVolume+",systemVolume:"+systemVolume);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, musicVolume, AudioManager.FLAG_VIBRATE);//FLAG_VIBRATE
                    audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM,systemVolume , AudioManager.FLAG_VIBRATE);
                    // 获得音频焦点
                    if (mIsPlaying) {
                        state = AudioPlayerConst.PlayerState.PLAYING;
                        mediaPlayer.start();
                        onStateChanged();
                    }
                    mIsPlaying = false;
                    break;

                case AudioManager.AUDIOFOCUS_LOSS:
                    Log.i(TAG,"AudioManager.AUDIOFOCUS_LOSS:"+focusChange);
                    // 长久的失去音频焦点，释放MediaPlayer
                    if (mediaPlayer==null) {
                        return;
                    }
                    if (mediaPlayer.isPlaying()){
                        mIsPlaying = true;
                    }
                    musicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    systemVolume = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);

                    state = AudioPlayerConst.PlayerState.STOP;
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    mediaPlayer.release();
                    releaseWakeLock();
                    onStateChanged();
                    break;

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    Log.i(TAG,"AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:"+focusChange);
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    Log.i(TAG,"AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:"+focusChange);
                    if (mediaPlayer==null) {
                        return;
                    }
                    // 失去音频焦点，无需停止播放，降低声音即可
//                    if (mediaPlayer.isPlaying()) {
//                        mediaPlayer.setVolume(0.1f, 0.1f);
//                    }                    if (mediaPlayer.isPlaying())
                    // 展示失去音频焦点，暂停播放等待重新获得音频焦点
                    if (mediaPlayer.isPlaying()){
                        mIsPlaying = true;
                    }
//                    musicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//                    systemVolume = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
                    Log.i(TAG,"1===musicVolume:"+musicVolume+",systemVolume:"+systemVolume);
                    state = AudioPlayerConst.PlayerState.PAUSE;
                    mediaPlayer.pause();
                    onStateChanged();
                    break;
            }
        }
    };

    private MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            state = AudioPlayerConst.PlayerState.PREPARED;
            mPlayHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG,"onPreparedListener--startAfterPrepare:"+startAfterPrepare);
                    if (startAfterPrepare) {
                        audioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                        mediaPlayer.start();
                        state = AudioPlayerConst.PlayerState.PLAYING;
                        startAfterPrepare = false;
                    }
                    onStateChanged();
                }
            });
        }
    };

    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            mPlayHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG,"onCompletionListener");
                    if (onAudioListener!=null) {
                        onAudioListener.onComplete();
                    }

                    state = AudioPlayerConst.PlayerState.PAUSE;
                    startAfterPrepare = true;
                    preparePlayer();
                }
            });
        }
    };

    private long lastErrorTime;
    private MediaPlayer.OnErrorListener onErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Log.i(TAG,"onErrorListener---onError:--what:"+what+",extra:"+extra);
            if (what ==MediaPlayer.MEDIA_ERROR_SERVER_DIED){
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
                mediaPlayer.setOnPreparedListener(onPreparedListener);
                mediaPlayer.setOnCompletionListener(onCompletionListener);
                mediaPlayer.setOnErrorListener(onErrorListener);
            }else {
                if (onAudioListener!=null) {
                    onAudioListener.onError(extra);
                }
            }
            return false;
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"----------------onDestroy------");
        releaseWakeLock();
        audioManager.abandonAudioFocus(onAudioFocusChangeListener);
        mediaPlayer.reset();
        mediaPlayer.release();
        mPlayHandler.removeCallbacks(null);
        mPlayHandler = null;
        mediaPlayer = null;
        binder = null;
    }

    //    PowerManager.WakeLock wakeLock = null;
    private PowerManager.WakeLock mWakeLock;
    //获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
    public void acquireWakeLock()
    {
        Log.i(TAG,"acquireWakeLock*****************************");
        if (null == mWakeLock)
        {
            // Initialize the wake lock
            final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            mWakeLock.setReferenceCounted(false);
            mWakeLock.acquire();
        }
    }
    //释放设备电源锁
    public void releaseWakeLock()
    {
        Log.i(TAG,"releaseWakeLock=============================");
        if (null != mWakeLock)
        {
            mWakeLock.release();
            mWakeLock = null;
        }
    }
}
