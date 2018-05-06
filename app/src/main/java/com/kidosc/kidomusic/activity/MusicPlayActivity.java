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
     * 界面的UI控件
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
    private MusicDesInfo musicDesInfo;

    private int mMusicTotalLen;
    private int mMusicPlayedLen;

    private boolean mIsActive = false;


    /**
     * 监听当前音乐的状态
     */
    private AudioUtils.OnAudioListener audioListener = new AudioUtils.OnAudioListener() {
        @Override
        public void onStateChanged(AudioPlayerConst.PlayerState state, int position, final boolean isPlaying) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshPlayStateButton(isPlaying);
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

        @Override
        public void onPosition(int position) {

        }
    };

    /**
     * 监听进度条拖动
     */
    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mDurationPlayed.setText(MusicUtil.formatTime(progress));

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            AudioUtils.getInstance(getApplication()).getPlayer(AudioUtils.AudioType.MEDIA).seek(seekBar.getProgress());
        }
    };

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
        mPlayButton = findViewById(R.id.btn_play);
        mSequencePopWindow = new MyPopWindow(this, Constant.POP_WINDOW_SEQUENCE);
        mVolumePopWindow = new MyPopWindow(this, Constant.POP_WINDOW_VOLUME);
        mMusicPosition = getIntent().getIntExtra("position", 0);
        musicDesInfo = Constant.ALL_MUSIC_LIST.get(mMusicPosition);
        mPlaySeekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
        AudioUtils.getInstance(getApplication()).setOnAudioListener(audioListener);
        playMusic(musicDesInfo.getUrl());
        mIsActive = true;
        //在service中不断的去更新主界面

    }

    /**
     * 处理点击事件
     *
     * @param view
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
     * 当音乐状态发生改变时，播放的button
     *
     * @param isPlaying
     */

    public void refreshPlayStateButton(boolean isPlaying) {
        if (isPlaying) {
            mPlayButton.setBackgroundResource(R.drawable.btn_stop);

        } else {
            mPlayButton.setBackgroundResource(R.drawable.btn_play);
        }

    }

    /**
     * 初始化播放的控件
     */
    public void initPlayView() {
        mGenre.setText(musicDesInfo.getAlbum());
        mSinger.setText(musicDesInfo.getArtist());
        mMusicName.setText(musicDesInfo.getTitle());
        mDurationPlayed.setText("00:00");
        mDuration.setText(MusicUtil.formatTime(musicDesInfo.getDuration()));
        mProgress.setText(mMusicPosition + 1 + "/" + Constant.ALL_MUSIC_LIST.size());
        mPlaySeekBar.setProgress(1000);
        mPlaySeekBar.setMax((int) musicDesInfo.getDuration());
    }

    /**
     * 播放下一首
     */
    public void playNextSong() {
        if (mMusicPosition < Constant.ALL_MUSIC_LIST.size() - 1) {
            mMusicPosition = mMusicPosition + 1;
        } else {
            mMusicPosition = 0;
        }
        if (AudioUtils.getInstance(getApplication()).getPlayer(AudioUtils.AudioType.MEDIA) == null) {
            return;
        }
        musicDesInfo = Constant.ALL_MUSIC_LIST.get(mMusicPosition);
        playMusic(musicDesInfo.getUrl());
    }

    /**
     * 播放上一首
     */
    public void playForward() {
        if (mMusicPosition > 0) {
            mMusicPosition = mMusicPosition - 1;
        } else {
            mMusicPosition = Constant.ALL_MUSIC_LIST.size() - 1;
        }
        if (AudioUtils.getInstance(getApplication()).getPlayer(AudioUtils.AudioType.MEDIA) == null) {
            return;
        }
        musicDesInfo = Constant.ALL_MUSIC_LIST.get(mMusicPosition);
        playMusic(musicDesInfo.getUrl());
    }

    /**
     * 播放音乐
     */
    public void playMusic(final String url) {
        initPlayView();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIsActive = false;
    }
}
