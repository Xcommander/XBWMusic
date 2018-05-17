package com.kidosc.kidomusic.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.kidosc.kidomusic.R;
import com.kidosc.kidomusic.audio.AudioPlayerConst;
import com.kidosc.kidomusic.audio.AudioUtils;
import com.kidosc.kidomusic.dialog.VolumeDialog;
import com.kidosc.kidomusic.model.MusicDesInfo;
import com.kidosc.kidomusic.util.Constant;
import com.kidosc.kidomusic.util.MusicUtil;
import com.kidosc.kidomusic.widget.MyPopWindow;

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
     * 音量调节Dialog
     */
    private VolumeDialog mVolumeDialog;
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
    private TextView mKBitsRate;

    private Button mPlayButton;

    private int mMusicPosition;
    private MusicDesInfo musicDesInfo;

    /**
     * 音频服务
     */
    private AudioManager mAudioManager;


    /**
     * 通过handler来达到延时效果，从而让控件消失
     */
    private Handler mHandler;


    /**
     * 监听当前音乐的状态
     */
    private AudioUtils.OnAudioListener audioListener = new AudioUtils.OnAudioListener() {
        @Override
        public void onStateChanged(AudioPlayerConst.PlayerState state, int position, final boolean isPlaying) {
            refreshPlayStateButton(isPlaying);
        }

        @Override
        public void onComplete() {
            playNextSong(true);
        }

        @Override
        public void onError(int extra) {

        }

        @Override
        public void onPosition(final int position) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPlaySeekBar.setProgress(position);
                }
            });

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
            MyApplication.getAudioUtils().getPlayer(AudioUtils.AudioType.MEDIA).seek(seekBar.getProgress());
        }
    };

    /**
     * 通过handler延时，达到dialog在一定时间没有动作的，自动消失
     */
    private Runnable volumeDialogRunable = new Runnable() {
        @Override
        public void run() {
            if (mVolumeDialog.isVisible()) {
                mVolumeDialog.dismiss();
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play);
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mSequenceButton = findViewById(R.id.btn_random);
        mSequenceButton.setBackground(getSequenceButtonDrawable());
        mPlaySeekBar = findViewById(R.id.play_seek);
        mProgress = findViewById(R.id.progress);
        mGenre = findViewById(R.id.genre);
        mSinger = findViewById(R.id.singer);
        mMusicName = findViewById(R.id.music_name);
        mDuration = findViewById(R.id.music_duration);
        mDurationPlayed = findViewById(R.id.music_duration_played);
        mPlayButton = findViewById(R.id.btn_play);
        mKBitsRate = findViewById(R.id.bit_rate);
        mSequencePopWindow = new MyPopWindow(this, Constant.POP_WINDOW_SEQUENCE);
        mSequencePopWindow.setmPopWinodListener(new MyPopWindow.PopWinodListener() {
            @Override
            public void onClick() {
                mSequenceButton.setBackground(getSequenceButtonDrawable());

            }
        });
        mVolumeDialog = new VolumeDialog(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                    setVolume(Constant.VOULME_DOWN);
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_POWER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    setVolume(Constant.VOULME_UP);
                    return true;
                }

                return false;
            }
        });
        mMusicPosition = getIntent().getIntExtra("position", 0);
        mHandler = new Handler();
        musicDesInfo = Constant.ALL_MUSIC_LIST.get(mMusicPosition);
        mPlaySeekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
        MyApplication.getAudioUtils().setOnAudioListener(audioListener);
        Log.e("xulinchao", "onCreate: music activity");
        playMusic(musicDesInfo.getUrl());

    }

    /**
     * 处理点击事件
     *
     * @param view
     */
    public void musicClick(View view) {
        switch (view.getId()) {
            case R.id.btn_random:
                mSequencePopWindow.showPopupWindow(mSequenceButton);
                break;
            case R.id.btn_ff:
                playNextSong(false);
                break;
            case R.id.btn_fb:
                playBackSong(false);
                break;
            case R.id.btn_play:
                MusicUtil.handleThread(new Runnable() {
                    @Override
                    public void run() {
                        MyApplication.getAudioUtils().getPlayer(AudioUtils.AudioType.MEDIA).playOrPause();
                    }
                });
            case R.id.vol_add:
                if (mVolumeDialog != null && mVolumeDialog.isVisible()) {
                    setVolume(Constant.VOULME_UP);
                }
                break;
            case R.id.vol_sub:
                setVolume(Constant.VOULME_DOWN);
                break;
            default:
                break;

        }
    }

    /**
     * 当音乐状态发生改变时，播放的button
     *
     * @param isPlaying
     */

    public void refreshPlayStateButton(final boolean isPlaying) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isPlaying) {
                    mPlayButton.setBackgroundResource(R.drawable.btn_stop);
                } else {
                    mPlayButton.setBackgroundResource(R.drawable.btn_play);
                }
            }
        });

    }

    /**
     * 初始化播放的控件
     */
    public void initPlayView() {
        mGenre.setText(musicDesInfo.getAlbum());
        mSinger.setText(musicDesInfo.getArtist());
        mMusicName.setText(musicDesInfo.getTitle());
        mDuration.setText(MusicUtil.formatTime(musicDesInfo.getDuration()));
        mProgress.setText(mMusicPosition + 1 + "/" + Constant.ALL_MUSIC_LIST.size());
        mPlaySeekBar.setProgress(0);
        mPlaySeekBar.setMax((int) musicDesInfo.getDuration());
        mKBitsRate.setText(MusicUtil.getMP3KBitsRate(musicDesInfo.getUrl()));
    }

    /**
     * 播放下一首
     */
    public void playNextSong(boolean isAuto) {
        selectMusic(Constant.MUSIC_NEXT, isAuto);
    }

    /**
     * 播放上一首
     */
    public void playBackSong(boolean isAuto) {
        selectMusic(Constant.MUSIC_BACK, isAuto);
    }

    /**
     * 根据类型选择播放的歌曲
     *
     * @param type   区分是否是前一曲还是后一曲
     * @param isAuto 标识是手动点击还是自动播放
     */
    public void selectMusic(String type, boolean isAuto) {
        Log.e("xulinchao", "selectMusic: " + Constant.TYPE_MUSIC_PLAY_FLAG);
        switch (Constant.TYPE_MUSIC_PLAY_FLAG) {
            case Constant.TYPE_ORDER:
                /**
                 * 顺序播放:(1)自动播放(2)单击
                 */

                if (isAuto) {
                    if (mMusicPosition < Constant.ALL_MUSIC_LIST.size() - 1) {
                        mMusicPosition = mMusicPosition + 1;
                        musicDesInfo = Constant.ALL_MUSIC_LIST.get(mMusicPosition);
                        playMusic(musicDesInfo.getUrl());
                    }

                } else {
                    if (Constant.MUSIC_NEXT.equals(type)) {
                        if (mMusicPosition < Constant.ALL_MUSIC_LIST.size() - 1) {
                            mMusicPosition = mMusicPosition + 1;
                        } else {
                            mMusicPosition = 0;
                        }

                    } else {
                        if (mMusicPosition > 0) {
                            mMusicPosition = mMusicPosition - 1;
                        } else {
                            mMusicPosition = Constant.ALL_MUSIC_LIST.size() - 1;
                        }

                    }
                    musicDesInfo = Constant.ALL_MUSIC_LIST.get(mMusicPosition);
                    playMusic(musicDesInfo.getUrl());

                }
                break;
            case Constant.TYPE_CYCLE:
                /**
                 * 单曲循环:(1)自动模式(2)单击
                 */
                if (isAuto) {
                    musicDesInfo = Constant.ALL_MUSIC_LIST.get(mMusicPosition);
                    playMusic(musicDesInfo.getUrl());
                } else {
                    if (Constant.MUSIC_NEXT.equals(type)) {
                        if (mMusicPosition < Constant.ALL_MUSIC_LIST.size() - 1) {
                            mMusicPosition = mMusicPosition + 1;
                        } else {
                            mMusicPosition = 0;
                        }

                    } else {
                        if (mMusicPosition > 0) {
                            mMusicPosition = mMusicPosition - 1;
                        } else {
                            mMusicPosition = Constant.ALL_MUSIC_LIST.size() - 1;
                        }

                    }
                    musicDesInfo = Constant.ALL_MUSIC_LIST.get(mMusicPosition);
                    playMusic(musicDesInfo.getUrl());

                }
                break;
            case Constant.TYPE_CYCLE_ALL:
                /**
                 * 全部循环
                 */
                if (Constant.MUSIC_NEXT.equals(type)) {
                    if (mMusicPosition < Constant.ALL_MUSIC_LIST.size() - 1) {
                        mMusicPosition = mMusicPosition + 1;
                    } else {
                        mMusicPosition = 0;
                    }

                } else {
                    if (mMusicPosition > 0) {
                        mMusicPosition = mMusicPosition - 1;
                    } else {
                        mMusicPosition = Constant.ALL_MUSIC_LIST.size() - 1;
                    }

                }
                musicDesInfo = Constant.ALL_MUSIC_LIST.get(mMusicPosition);
                playMusic(musicDesInfo.getUrl());

                break;
            case Constant.TYPE_RANDOM:
                /**
                 * 随机播放:
                 */
                mMusicPosition = (int) (Math.random() * Constant.ALL_MUSIC_LIST.size());
                musicDesInfo = Constant.ALL_MUSIC_LIST.get(mMusicPosition);
                playMusic(musicDesInfo.getUrl());
                break;
        }

    }

    /**
     * 播放音乐
     */
    public void playMusic(final String url) {
        MusicUtil.handleThread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("xulinchao", "run: music init view");
                        initPlayView();
                    }
                });
                Log.e("xulinchao", "run: 1 " + url);

                MyApplication.getAudioUtils().getPlayer(AudioUtils.AudioType.MEDIA).reset();
                Log.e("xulinchao", "run: alive");
                MyApplication.getAudioUtils().getPlayer(AudioUtils.AudioType.MEDIA).setDataSource(url);
                MyApplication.getAudioUtils().getPlayer(AudioUtils.AudioType.MEDIA).playOrPause();


            }
        });

    }


    /**
     * 拦截back,power调节音量
     **/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.e("jason", "KEYCODE_BACK: ");
            if (!mVolumeDialog.isVisible()) {
                mVolumeDialog.show(getFragmentManager(), "volume");
                setVolume(Constant.VOULME_DOWN);
            }
        } else if (keyCode == KeyEvent.KEYCODE_POWER) {
            if (!mVolumeDialog.isVisible()) {
                mVolumeDialog.show(getFragmentManager(), "volume");
                setVolume(Constant.VOULME_UP);
            }

        } else {
            return super.onKeyDown(keyCode, event);
        }
        return true;
    }


    /**
     * 根据按键类型,设置音量,以及UI刷新
     *
     * @param type
     */
    public void setVolume(String type) {

        mHandler.removeCallbacks(volumeDialogRunable);
        mHandler.postDelayed(volumeDialogRunable, 2000);
        int vol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (Constant.VOULME_UP.equals(type)) {
            vol = MusicUtil.getMusicVolume(vol, Constant.VOULME_UP);
        } else {
            vol = MusicUtil.getMusicVolume(vol, Constant.VOULME_DOWN);
        }
        //Dialog 中create view这个过程慢，导致变量还没有被赋值,空指针异常
        if (mVolumeDialog.mVolumePic == null) {
            return;
        }
        mVolumeDialog.mVolumePic.setImageDrawable(MusicUtil.getVolumePic(vol));
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, AudioManager.FLAG_VIBRATE);
        MyApplication.getAudioUtils().getPlayer(AudioUtils.AudioType.MEDIA).setVolume(vol);
    }

    /**
     * 获取sequence button的背景
     *
     * @return
     */

    public Drawable getSequenceButtonDrawable() {
        Drawable drawable;
        switch (Constant.TYPE_MUSIC_PLAY_FLAG) {
            case Constant.TYPE_ORDER:
                drawable = getDrawable(R.drawable.btn_order);
                break;
            case Constant.TYPE_CYCLE:
                drawable = getDrawable(R.drawable.btn_cycle1);
                break;
            case Constant.TYPE_CYCLE_ALL:
                drawable = getDrawable(R.drawable.btn_cycle);
                break;
            case Constant.TYPE_RANDOM:
                drawable = getDrawable(R.drawable.btn_random);
                break;
            default:
                drawable = getDrawable(R.drawable.btn_order);
                break;

        }
        return drawable;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
