package com.kidosc.kidomusic.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.kidosc.kidomusic.R;
import com.kidosc.kidomusic.adapter.MusicListAdapter;
import com.kidosc.kidomusic.audio.AudioUtils;
import com.kidosc.kidomusic.download.DownloadUpdate;
import com.kidosc.kidomusic.model.DownloadModel;
import com.kidosc.kidomusic.util.Constant;
import com.kidosc.kidomusic.util.MusicUtil;
import com.kidosc.kidomusic.widget.DashlineItemDivider;
import com.kidosc.kidomusic.widget.MyRecyclerView;

import java.io.File;
import java.util.ArrayList;

import static com.kidosc.kidomusic.activity.MyApplication.getAudioUtils;
import static com.kidosc.kidomusic.activity.MyApplication.getDownloadUtil;


public class MainActivity extends Activity {
    private MyRecyclerView mMusicListView;
    private RecyclerView.LayoutManager mManager;
    private MusicListAdapter musicListAdapter;
    private ProgressDialog mProgressDialog;
    private TextView mEmptyView;
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
     * 广播:拦截当下载完成后，回调函数仍然是其他的监听函数，导致无法刷新界面
     */
    private BroadcastReceiver mUpdateListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("xulinchao22", "update: onReceive ");
            MusicUtil.updateList();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicListAdapter != null) {
                        musicListAdapter.updateData(Constant.ALL_MUSIC_LIST);
                    }
                }
            });
        }
    };

    /**
     * 下载完成后，更新列表
     */
    private DownloadUpdate mDownloadUpdate = new DownloadUpdate() {
        @Override
        public void update() {
            Log.e("xulinchao22", "update: MainActivity ");
            MusicUtil.updateList();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicListAdapter != null) {
                        musicListAdapter.updateData(Constant.ALL_MUSIC_LIST);
                    }
                }
            });


        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File file = new File(Constant.MUSIC_DIR);
        if (!file.exists()) {
            file.mkdirs();
        }
        mEmptyView = findViewById(R.id.empty_tv);
        mMusicListView = findViewById(R.id.music_list);
        mManager = new LinearLayoutManager(this);
        musicListAdapter = new MusicListAdapter(this, Constant.ALL_MUSIC_LIST);
        mMusicListView.setAdapter(musicListAdapter);
        mMusicListView.setLayoutManager(mManager);
        //添加分割线
        mMusicListView.addItemDecoration(new DashlineItemDivider());
        mMusicListView.setItemAnimator(null);

        mMusicListView.setEmptyView(mEmptyView);

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.ACTION_UPDATE_LIST);
        registerReceiver(mUpdateListReceiver, intentFilter);

        loadMusicList();

        MusicUtil.handleThread(new Runnable() {
            @Override
            public void run() {
                /**
                 *首次获取的保护
                 */
                if (getDownloadUtil().getmDownloadBinder() == null) {
                    int i = 0;
                    while (getDownloadUtil().getmDownloadBinder() == null) {
                        if (i < 15) {
                            SystemClock.sleep(100);
                            i++;
                        } else {
                            Log.d("xulinchao", "launch download service fail");
                            return;
                        }

                    }

                }
                if (getDownloadUtil().getmDownloadBinder().getmDownloadUpdate() != null) {
                    getDownloadUtil().getmDownloadBinder().setmDownloadUpdate(mDownloadUpdate);
                    loadDownloadTask();
                }
            }
        });
        registerBR();

    }

    /**
     * 加载音乐列表
     */
    public void loadMusicList() {
        showProgressDialog("歌曲", "努力加载歌曲哦..");
        MusicUtil.handleThread(new Runnable() {
            @Override
            public void run() {
                MusicUtil.updateList();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        musicListAdapter.updateData(Constant.ALL_MUSIC_LIST);
                        hideProgressDialog();
                    }
                });
            }
        });
    }

    /**
     * 动态注册广播
     */
    private void registerBR() {
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
        unregisterReceiver(eventBR);
    }

    /**
     * 这个是加载弹框，临时方案，后续再优化样式问题,过时方法以后再去解决
     *
     * @param title
     * @param message
     */
    public void showProgressDialog(String title, String message) {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(MainActivity.this, title, message, true, false);
        } else {
            mProgressDialog.setTitle(title);
            mProgressDialog.setMessage(message);
        }
        mProgressDialog.show();

    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    /**
     * 加载下载任务
     */
    public void loadDownloadTask() {
        MusicUtil.handleThread(new Runnable() {
            @Override
            public void run() {
                if (MusicUtil.isNetWorkOK()) {
                    ArrayList<DownloadModel> networkFailedList = MusicUtil.jsonToList(Constant.SP_NAME_NETWORT_FAILED, Constant.KEY_NETWORT_FAILED);
                    ArrayList<DownloadModel> resourceFailedList = MusicUtil.jsonToList(Constant.SP_NAME_RESOURCE_FAILED, Constant.KEY_RESOURCE_FAILED);
                    if (networkFailedList != null && networkFailedList.size() > 0) {
                        for (int i = 0; i < networkFailedList.size(); i++) {
                            getDownloadUtil().getmDownloadBinder().downloadTask(networkFailedList.get(i));
                        }
                        networkFailedList.clear();
                    }
                    if (resourceFailedList != null && resourceFailedList.size() > 0) {
                        for (int i = 0; i < resourceFailedList.size(); i++) {
                            getDownloadUtil().getmDownloadBinder().downloadTask(resourceFailedList.get(i));
                        }
                        resourceFailedList.clear();

                    }
                    MusicUtil.saveToJson(Constant.SP_NAME_NETWORT_FAILED, Constant.KEY_NETWORT_FAILED, networkFailedList);
                    MusicUtil.saveToJson(Constant.SP_NAME_RESOURCE_FAILED, Constant.KEY_RESOURCE_FAILED, resourceFailedList);

                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.e("xulinchao", "onBackPressed: ");
        MusicUtil.handleThread(new Runnable() {
            @Override
            public void run() {
                if (getAudioUtils().getServiceConnecion()) {
                    getAudioUtils().getPlayer(AudioUtils.AudioType.MEDIA).stop();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterBR();
        unregisterReceiver(mUpdateListReceiver);
        if (getAudioUtils() != null) {
            getAudioUtils().release();
        }

    }



}
