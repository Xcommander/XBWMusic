package com.kidosc.kidomusic.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.kidosc.kidomusic.R;
import com.kidosc.kidomusic.adapter.MusicViewAdapter;
import com.kidosc.kidomusic.audio.AudioPlayerConst;
import com.kidosc.kidomusic.audio.AudioUtils;
import com.kidosc.kidomusic.dialog.VolumeDialog;
import com.kidosc.kidomusic.model.MusicDesInfo;
import com.kidosc.kidomusic.util.Constant;
import com.kidosc.kidomusic.util.MusicImageLoader;
import com.kidosc.kidomusic.util.MusicUtil;
import com.kidosc.kidomusic.widget.AnimationImageView;
import com.kidosc.kidomusic.widget.CircularSeekBar;
import com.kidosc.kidomusic.widget.LrcView;
import com.kidosc.kidomusic.widget.MyPopWindow;

import java.util.ArrayList;
import java.util.List;


import static com.kidosc.kidomusic.activity.MyApplication.getAudioUtils;

public class MusicPlayActivity extends Activity implements ViewPager.OnPageChangeListener {
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
    private TextView mMusicName;
    private TextView mDurationPlayed;
    private TextView mDuration;

    private Button mPlayButton;

    private int mMusicPosition;
    private MusicDesInfo musicDesInfo;


    /**
     * view pager
     */
    private ViewPager mViewPager;
    private List<View> mViewList = new ArrayList<>();
    private LinearLayout mLinearLayout;
    private int mCurrentViewPagerPosition = 0;

    private View mMusicInfoView;
    private View mMusicAlbumView;
    private View mMusicLrcView;
    /**
     * 这个是旋转的图片
     */
    private AnimationImageView mAlbumImageView;

    /**
     * 歌词控件
     */
    private LrcView mLrcView;

    /**
     * 圆形进度条
     */
    private CircularSeekBar mCircularSeekBar;

    /**
     * 歌词相关控件
     */
    private TextView mSinger;
    private TextView mAlbum;
    private TextView mReleaseTime;
    private TextView mGenre;

    /**
     * 背景图片
     */
    private View mBackground;
    private Bitmap mBackgroundBitmap;


    /**
     * 音频服务
     */
    private AudioManager mAudioManager;

    /**
     * 音量状态按钮
     */
    private Button mVolumeState;


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
            refreshPlayStateButton(false);
            playNextSong(true);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAlbumImageView.playMusic(AnimationImageView.State.STATE_STOP);
                }
            });
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
                    mCircularSeekBar.setProgress(position);

                    mLrcView.changeCurrent(new Long(String.valueOf(position)));

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
            if (getAudioUtils().getServiceConnecion()) {
                getAudioUtils().getPlayer(AudioUtils.AudioType.MEDIA).seek(seekBar.getProgress());
                /**
                 * 拖动进度条时,歌词的时间戳的变化
                 */
                mLrcView.onDrag(seekBar.getProgress());
            }
        }
    };

    /**
     * 通过handler延时，达到dialog在一定时间没有动作的，自动消失
     */
    private Runnable volumeDialogRunable = new Runnable() {
        @Override
        public void run() {
            if (mVolumeDialog.isVisible()) {
                mVolumeDialog.dismissAllowingStateLoss();
            }

        }
    };

    /**
     * 拦截相关广播事件
     */
    BroadcastReceiver eventBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("xulinchao", "intent.getAction():" + intent.getAction());
            if (intent.getAction().equals("com.zeusis.myevents")) {
                boolean isInCall = false;
                ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                if (null != am.getRunningTasks(1) && !am.getRunningTasks(1).isEmpty()) {
                    String activityName = am.getRunningTasks(1).get(0).topActivity.getClassName();
                    isInCall = activityName.equals("com.zeusis.csp.activity.InCallActivity") || activityName.equals("com.zeusis.csp.activity.SecondInCallActivity");
                }
                Log.i("xulinchao", "isInCall:" + isInCall);
                if (isInCall) {
                    return;
                }
                if (getAudioUtils().getPlayer(AudioUtils.AudioType.MEDIA) == null) return;
                if (getAudioUtils().getPlayer(AudioUtils.AudioType.MEDIA).isPlaying()) {
                    getAudioUtils().getPlayer(AudioUtils.AudioType.MEDIA).playOrPause();
                    Log.i("xulinchao", "mediaplayer--stop");
                }
            } else if (intent.getAction().equals("com.zeusis.socket.ban_launcher")) {
                boolean flag = intent.getBooleanExtra("enable", false);
                if (flag) {
                    if (getAudioUtils().getPlayer(AudioUtils.AudioType.MEDIA) == null) return;
                    if (getAudioUtils().getPlayer(AudioUtils.AudioType.MEDIA).isPlaying()) {
                        getAudioUtils().getPlayer(AudioUtils.AudioType.MEDIA).playOrPause();
                        Log.i("xulinchao", "mediaplayer--stop");
                    }
                }
            } else if ("com.zeusis.csp.NEW_CHAT".equals(intent.getAction())) {
                if (getAudioUtils().getPlayer(AudioUtils.AudioType.MEDIA) != null && getAudioUtils().getPlayer(AudioUtils.AudioType.MEDIA).isPlaying()) {
                    getAudioUtils().getPlayer(AudioUtils.AudioType.MEDIA).playOrPause();
                }
            }

        }
    };
    /**
     * 动态广播，拦截亮屏和暗屏，暗屏不拦截
     */
    private boolean isScreenOn = true;
    BroadcastReceiver screenOnBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                isScreenOn = false;
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                isScreenOn = true;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play);
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mSequenceButton = findViewById(R.id.btn_sequence);
        mSequenceButton.setBackground(getSequenceButtonDrawable());
        mPlaySeekBar = findViewById(R.id.play_seek);
        mProgress = findViewById(R.id.progress);
        mVolumeState = findViewById(R.id.btn_vol);
        mMusicName = findViewById(R.id.music_title_name);
        mDuration = findViewById(R.id.music_duration);
        mDurationPlayed = findViewById(R.id.music_duration_played);
        mPlayButton = findViewById(R.id.btn_play);
        mSequencePopWindow = new MyPopWindow(this, Constant.POP_WINDOW_SEQUENCE);
        mSequencePopWindow.setmPopWinodListener(new MyPopWindow.PopWinodListener() {
            @Override
            public void onClick() {
                mSequenceButton.setBackground(getSequenceButtonDrawable());

            }
        });

        /**
         * 背景图片
         */
        mBackground = findViewById(R.id.music_background);

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
        getAudioUtils().setOnAudioListener(audioListener);
        Log.e("xulinchao", "onCreate: music activity");
        /**
         * View Pager三个控件
         */
        initViewPager();
        playMusic(musicDesInfo.getUrl());
        registerBR();


    }

    /**
     * 初始化view pager
     */
    private void initViewPager() {
        mViewPager = findViewById(R.id.music_info);
        mMusicInfoView = getLayoutInflater().inflate(R.layout.viewpager_des, null);
        mMusicAlbumView = getLayoutInflater().inflate(R.layout.viewpager_album_image, null);
        mMusicLrcView = getLayoutInflater().inflate(R.layout.viewpager_lrc, null);
        mViewList.add(mMusicInfoView);
        mViewList.add(mMusicAlbumView);
        mViewList.add(mMusicLrcView);
        MusicViewAdapter musicViewAdapter = new MusicViewAdapter(mViewList);
        mViewPager.setAdapter(musicViewAdapter);
        mViewPager.addOnPageChangeListener(this);


        /**
         * 小圆圈导航栏
         */
        mLinearLayout = findViewById(R.id.circle_indicator);
        for (int i = 0; i < 3; i++) {
            View circle = new View(this);
            circle.setBackgroundResource(R.drawable.circle_indicator);
            circle.setEnabled(false);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(10, 10);
            if (i != 0) {
                layoutParams.leftMargin = 15;
            }
            mLinearLayout.addView(circle, layoutParams);
        }

        mLinearLayout.getChildAt(0).setEnabled(true);

        /**
         * 歌词详细信息
         */
        mSinger = mMusicInfoView.findViewById(R.id.music_singer);
        mAlbum = mMusicInfoView.findViewById(R.id.music_album);
        mReleaseTime = mMusicInfoView.findViewById(R.id.music_release_time);
        mGenre = mMusicInfoView.findViewById(R.id.music_genre);

        /**
         * 转动的image view
         */
        mAlbumImageView = mMusicAlbumView.findViewById(R.id.music_album_image);

        /**
         * 圆形进度条
         */
        mCircularSeekBar = mMusicAlbumView.findViewById(R.id.circle_seekbar);


        /**
         * 歌词控件
         */
        mLrcView = mMusicLrcView.findViewById(R.id.music_lrc);

    }

    /**
     * 动态注册广播
     */
    private void registerBR() {
        IntentFilter intentFilter = new IntentFilter();
        // 屏幕灭屏广播
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        // 屏幕亮屏广播
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(screenOnBR, intentFilter);
        IntentFilter eventFilter = new IntentFilter();
        eventFilter.addAction("com.zeusis.myevents");
        eventFilter.addAction("com.zeusis.socket.ban_launcher");
        eventFilter.addAction("com.zeusis.csp.NEW_CHAT");
        registerReceiver(eventBR, eventFilter);
    }

    /**
     * 解绑
     */
    private void unRegisterBR() {
        unregisterReceiver(screenOnBR);
        unregisterReceiver(eventBR);
    }

    /**
     * 处理点击事件
     *
     * @param view
     */
    public void musicClick(View view) {
        switch (view.getId()) {
            case R.id.btn_sequence:
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
                        if (getAudioUtils().getServiceConnecion()) {
                            getAudioUtils().getPlayer(AudioUtils.AudioType.MEDIA).playOrPause();
                        }
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
                    mAlbumImageView.playMusic(AnimationImageView.State.STATE_PLAYING);
                    mVolumeState.setBackgroundResource(R.drawable.btn_music_volume);
                } else {
                    mPlayButton.setBackgroundResource(R.drawable.btn_play);
                    mAlbumImageView.playMusic(AnimationImageView.State.STATE_PAUSE);
                    mVolumeState.setBackgroundResource(R.drawable.btn_music_volume1);
                }
            }
        });

    }

    /**
     * 初始化播放的控件
     */
    public void initPlayView() {
        mMusicName.setText(musicDesInfo.getTitle());
        mDuration.setText(MusicUtil.formatTime(musicDesInfo.getDuration()));
        mProgress.setText(mMusicPosition + 1 + "/" + Constant.ALL_MUSIC_LIST.size());
        mPlaySeekBar.setProgress(0);
        mPlaySeekBar.setMax((int) musicDesInfo.getDuration());
        mCircularSeekBar.setMaxProgress((int) musicDesInfo.getDuration());

        mSinger.setText("演唱:" + " " + musicDesInfo.getArtist());
        mAlbum.setText("专辑:" + " " + musicDesInfo.getAlbum());
        mReleaseTime.setText("发行时间:" + " " + musicDesInfo.getYear());
        mGenre.setText("歌曲流派:" + " " + musicDesInfo.getGenre());

        /**
         * 背景图片更换
         */
        mBackgroundBitmap = MusicImageLoader.getmInstance().load(musicDesInfo.getImage());

        if (mBackgroundBitmap == null) {
            mBackground.setBackgroundResource(R.drawable.music_play_bg);
            mAlbumImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_music_04, null));
        } else {

            if (mCurrentViewPagerPosition == Constant.PAGE_ABLUM_INMAGE) {
                mBackground.setBackgroundResource(R.drawable.music_play_bg);
            } else {
                mBackground.setBackground(new BitmapDrawable(getResources(), mBackgroundBitmap));
            }
            Bitmap ovalBitmap = MusicUtil.getOvalBitmap(mBackgroundBitmap);
            mAlbumImageView.setImageBitmap(ovalBitmap);
        }


        /**
         * 设置歌词路径
         */

        mLrcView.setLrcPath(Constant.MUSIC_DIR + "/" + musicDesInfo.getTitle() + ".lrc");
        mLrcView.setmTotalTime(musicDesInfo.getDuration());

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
                 * <p>
                 * 顺序播放:(1)自动播放(2)单击
                 * <p/>
                 */
                Log.e("xulinchao11", "selectMusic: isAuto = " + isAuto + " mMusicPosition = " + mMusicPosition);
                if (isAuto) {
                    if (mMusicPosition < Constant.ALL_MUSIC_LIST.size() - 1) {
                        Log.e("xulinchao11", "selectMusic: ALL_MUSIC_LIST = " + Constant.ALL_MUSIC_LIST.size());
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
                        initPlayView();
                    }
                });
                /**
                 * 增加首次bind service ，connection还没完成，外部就进行了get binder的操作，导致获取为null
                 * 的保护
                 */
                if (getAudioUtils().getPlayer(AudioUtils.AudioType.MEDIA) == null) {
                    int i = 0;
                    while (getAudioUtils().getPlayer(AudioUtils.AudioType.MEDIA) == null) {
                        if (i < 10) {
                            SystemClock.sleep(100);
                            i++;
                        } else {
                            Log.d("xulinchao", "get player error ");
                            return;
                        }

                    }

                }
                getAudioUtils().getPlayer(AudioUtils.AudioType.MEDIA).setDataSource(url);
                getAudioUtils().getPlayer(AudioUtils.AudioType.MEDIA).playOrPause();
            }
        });

    }


    /**
     * 拦截back,power调节音量
     **/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //长按处理
        if (event.getRepeatCount() != 0) {
            return super.onKeyDown(keyCode, event);
        }
        if (!isScreenOn) {
            //暗屏不做任何处理
            return true;
        }
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
    public void setVolume(final String type) {

        mHandler.removeCallbacks(volumeDialogRunable);
        mHandler.postDelayed(volumeDialogRunable, 2000);
        int vol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (Constant.VOULME_UP.equals(type)) {
            vol = MusicUtil.getMusicVolume(vol, Constant.VOULME_UP);
        } else {
            vol = MusicUtil.getMusicVolume(vol, Constant.VOULME_DOWN);
        }
        //Dialog 中create view这个过程慢，导致变量还没有被赋值,空指针异常,所以50ms,进行新的尝试
        if (mVolumeDialog.mVolumePic == null) {
            Log.d("vol", "setVolume: ");
            MusicUtil.handleThread(new Runnable() {
                @Override
                public void run() {
                    SystemClock.sleep(50);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setVolume(type);
                        }
                    });
                }
            });
            return;
        }
        mVolumeDialog.mVolumePic.setImageDrawable(MusicUtil.getVolumePic(vol));
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, AudioManager.FLAG_VIBRATE);
        if (getAudioUtils().getServiceConnecion()) {
            getAudioUtils().getPlayer(AudioUtils.AudioType.MEDIA).setVolume(vol);
        }
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
                drawable = getDrawable(R.drawable.btn_music_order);
                break;
            case Constant.TYPE_CYCLE:
                drawable = getDrawable(R.drawable.btn_music_cycle1);
                break;
            case Constant.TYPE_CYCLE_ALL:
                drawable = getDrawable(R.drawable.btn_music_cycle);
                break;
            case Constant.TYPE_RANDOM:
                drawable = getDrawable(R.drawable.btn_music_random);
                break;
            default:
                drawable = getDrawable(R.drawable.btn_music_order);
                break;

        }
        return drawable;
    }

    /**
     * 重写ViewPager.OnPageChangeListener的三个方法
     */
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    private boolean mAblumImageSelected = false;

    @Override
    public void onPageSelected(int position) {
        Log.d("xulinchao", "onPageSelected: position = " + position);
        mLinearLayout.getChildAt(mCurrentViewPagerPosition).setEnabled(false);
        mLinearLayout.getChildAt(position).setEnabled(true);
        mCurrentViewPagerPosition = position;
        /**
         * (1)当滑动到歌曲动态图片的时候，导航栏往下移动，进度条消失
         * (2)当滑动到其他界面，进度条显示，并且导航栏上移
         */
        if (position == Constant.PAGE_ABLUM_INMAGE) {
            mAblumImageSelected = true;
            mPlaySeekBar.setVisibility(View.INVISIBLE);
            mLinearLayout.setTranslationY(25);
            mBackground.setBackgroundResource(R.drawable.music_play_bg);
        } else {
            if (mAblumImageSelected) {
                mPlaySeekBar.setVisibility(View.VISIBLE);
                mLinearLayout.setTranslationY(0);
                if (mBackgroundBitmap == null) {
                    mBackground.setBackgroundResource(R.drawable.music_play_bg);
                } else {
                    mBackground.setBackground(new BitmapDrawable(getResources(), mBackgroundBitmap));
                }
            }
        }

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    protected void onDestroy() {
        unRegisterBR();
        super.onDestroy();
    }


}
