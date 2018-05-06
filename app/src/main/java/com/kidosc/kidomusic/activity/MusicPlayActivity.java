package com.kidosc.kidomusic.activity;

import android.app.Activity;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import com.kidosc.kidomusic.R;
import com.kidosc.kidomusic.adapter.SequenceSelectAdapter;
import com.kidosc.kidomusic.audio.AudioPlayerConst;
import com.kidosc.kidomusic.audio.AudioUtils;
import com.kidosc.kidomusic.audio.MediaPlayerServiceBinder;
import com.kidosc.kidomusic.model.MusicDesInfo;
import com.kidosc.kidomusic.util.Constant;
import com.kidosc.kidomusic.util.HttpUtil;
import com.kidosc.kidomusic.util.MusicUtil;
import com.kidosc.kidomusic.widget.MyPopWindow;
import com.kidosc.kidomusic.activity.MyApplication;

public class MusicPlayActivity extends Activity {
    /**
     * 播放顺序按钮
     */
    private Button mSequenceButton;

    /**
     * 播放序列PopWindow
     */
    private MyPopWindow mSequencePopWindow;

    /**
     * 音量调节PopWindow
     */
    private MyPopWindow mVolumePopWindow;
    /**
     * 播放进度条
     */
    private SeekBar mPlaySeekBar;

    private TextView mProgress;
    private TextView mGenre;
    private TextView mSinger;
    private TextView mMusicName;
    private TextView mDurationPlayed;
    private TextView mDuration;

    private Button mPlayButton;

    private int mMusicPosition;
    private  MusicDesInfo musicDesInfo;
    private AudioUtils.OnAudioListener audioListener = new AudioUtils.OnAudioListener() {
        @Override
        public void onStateChanged(AudioPlayerConst.PlayerState state, int position, final boolean isPlaying) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshView(isPlaying);
                }
            });

        }

        @Override
        public void onComplete() {
            playNextSong();

        }

        @Override
        public void onError(int extra) {

        }
    };
    public void refreshView(boolean isPlaying){
        if(isPlaying){
            mPlayButton.setBackgroundResource(R.drawable.btn_stop);

        }else{
            mPlayButton.setBackgroundResource(R.drawable.btn_play);
        }
        mGenre.setText(musicDesInfo.getAlbum());
        mSinger.setText(musicDesInfo.getArtist());
        mMusicName.setText(musicDesInfo.getTitle());
        mDuration.setText(MusicUtil.formaTime(musicDesInfo.getDuration()));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play);
        mSequenceButton = findViewById(R.id.btn_random);
        mPlaySeekBar = findViewById(R.id.play_seek);
        mProgress = findViewById(R.id.progress);
        mGenre = findViewById(R.id.genre);
        mSinger = findViewById(R.id.singer);
        mMusicName = findViewById(R.id.music_name);
        mDuration = findViewById(R.id.music_duration);
        mDurationPlayed = findViewById(R.id.music_duration_played);
        mPlayButton=findViewById(R.id.btn_play);
        mSequencePopWindow = new MyPopWindow(this, Constant.POP_WINDOW_SEQUENCE);
        mVolumePopWindow = new MyPopWindow(this, Constant.POP_WINDOW_VOLUME);
        int len = getIntent().getIntExtra("size", 0);
        int pos = getIntent().getIntExtra("position", 0);
        mMusicPosition = pos-1;
        musicDesInfo = Constant.ALL_MUSIC_LIST.get(pos - 1);
        mProgress.setText(pos + "/" + len);
        mGenre.setText(musicDesInfo.getAlbum());
        mSinger.setText(musicDesInfo.getArtist());
        mMusicName.setText(musicDesInfo.getTitle());
        mDuration.setText(MusicUtil.formaTime(musicDesInfo.getDuration()));
        AudioUtils.getInstance(getApplication()).setOnAudioListener(audioListener);
        MusicUtil.handleThread(new Runnable() {
            @Override
            public void run() {
                playMusic(musicDesInfo.getUrl());
            }
        });

    }

    /**
     * 处理点击事件
     */
    public void MusicClick(View view) {
        switch (view.getId()) {
            case R.id.btn_random:
                mSequencePopWindow.showPopupWindow(mSequenceButton);
                break;
            case R.id.btn_ff:
                playNextSong();
                break;
            case R.id.btn_fb:
                playForward();
                break;
            case R.id.btn_play:
                MusicUtil.handleThread(new Runnable() {
                    @Override
                    public void run() {
                        AudioUtils.getInstance(getApplication()).getPlayer(AudioUtils.AudioType.MEDIA).playOrPause();
                    }
                });


            default:
                break;


        }


    }

    /**
     * 播放下一首
     */
    public void playNextSong() {
        if (mMusicPosition <Constant.ALL_MUSIC_LIST.size()-1) {
            mMusicPosition = mMusicPosition + 1;
        } else {
            mMusicPosition = 0;
        }
        if (AudioUtils.getInstance(getApplication()).getPlayer(AudioUtils.AudioType.MEDIA) == null) {
            return;
        }
        musicDesInfo=Constant.ALL_MUSIC_LIST.get(mMusicPosition);
        playMusic(musicDesInfo.getUrl());
    }

    /**
     * 播放上一首
     */
    public void playForward() {
        if (mMusicPosition > 0) {
            mMusicPosition = mMusicPosition - 1;
        } else {
            mMusicPosition = Constant.ALL_MUSIC_LIST.size()-1;
        }
        if (AudioUtils.getInstance(getApplication()).getPlayer(AudioUtils.AudioType.MEDIA) == null) {
            return;
        }
        musicDesInfo=Constant.ALL_MUSIC_LIST.get(mMusicPosition);
        playMusic(musicDesInfo.getUrl());
    }

    /**
     * 播放音乐
     */
    public void playMusic(final String url) {
        MusicUtil.handleThread(new Runnable() {
            @Override
            public void run() {
                AudioUtils.getInstance(getApplication()).getPlayer(AudioUtils.AudioType.MEDIA).setDataSource(url);
                AudioUtils.getInstance(getApplication()).getPlayer(AudioUtils.AudioType.MEDIA).playOrPause();
            }
        });

    }


    /**
     * 拦截back,power调节音量
     **/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //TODO:
            mVolumePopWindow.showPopupWindow(mPlaySeekBar);
            Log.e("xulinchao", "onKeyDown: back");
        } else if (keyCode == KeyEvent.KEYCODE_POWER) {
            //TODO
            mVolumePopWindow.showPopupWindow(mPlaySeekBar);
            Log.e("xulinchao", "onKeyDown: power");
        } else {
            return super.onKeyDown(keyCode, event);
        }
        return true;
    }

}
