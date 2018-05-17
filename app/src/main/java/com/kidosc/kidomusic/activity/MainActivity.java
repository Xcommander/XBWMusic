package com.kidosc.kidomusic.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.kidosc.kidomusic.R;
import com.kidosc.kidomusic.adapter.MusicListAdapter;
import com.kidosc.kidomusic.audio.AudioUtils;
import com.kidosc.kidomusic.download.DownloadUpdate;
import com.kidosc.kidomusic.model.DownloadModel;
import com.kidosc.kidomusic.util.Constant;
import com.kidosc.kidomusic.util.MusicUtil;
import com.kidosc.kidomusic.widget.DashlineItemDivider;

import java.io.File;
import java.util.ArrayList;


public class MainActivity extends Activity {
    private RecyclerView mMusicListView;
    private RecyclerView.LayoutManager mManager;
    private MusicListAdapter musicListAdapter;


    /**
     * 广播:拦截当下载完成后，回调函数仍然是其他的监听函数，导致无法刷新界面
     */
    private BroadcastReceiver mUpdateListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MusicUtil.updateList();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicListAdapter != null) {
                        musicListAdapter.notifyDataSetChanged();
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
            Log.e("xulinchao", "update: MainActivity ");
            MusicUtil.updateList();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicListAdapter != null) {
                        musicListAdapter.notifyDataSetChanged();
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
        MusicUtil.handleThread(new Runnable() {
            @Override
            public void run() {
                if (MyApplication.getDownloadUtil().getmDownloadBinder().getmDownloadUpdate() != null) {
                    MyApplication.getDownloadUtil().getmDownloadBinder().setmDownloadUpdate(mDownloadUpdate);
                }
            }
        });

        mMusicListView = findViewById(R.id.music_list);
        mManager = new LinearLayoutManager(this);
        musicListAdapter = new MusicListAdapter(this, Constant.ALL_MUSIC_LIST);
        mMusicListView.setAdapter(musicListAdapter);
        mMusicListView.setLayoutManager(mManager);
        //添加分割线
        mMusicListView.addItemDecoration(new DashlineItemDivider());

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.ACTION_UPDATE_LIST);
        registerReceiver(mUpdateListReceiver, intentFilter);

        loadMusicList();
        loadDownloadTask();
    }

    /**
     * 加载音乐列表
     */
    public void loadMusicList() {
        MusicUtil.handleThread(new Runnable() {
            @Override
            public void run() {
                MusicUtil.updateList();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        musicListAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
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
                            MyApplication.getDownloadUtil().getmDownloadBinder().downloadTask(networkFailedList.get(i));
                        }
                        networkFailedList.clear();
                    }
                    if (resourceFailedList != null && resourceFailedList.size() > 0) {
                        for (int i = 0; i < resourceFailedList.size(); i++) {
                            MyApplication.getDownloadUtil().getmDownloadBinder().downloadTask(resourceFailedList.get(i));
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
        if (MyApplication.getAudioUtils().getPlayer(AudioUtils.AudioType.MEDIA) != null) {
            MyApplication.getAudioUtils().getPlayer(AudioUtils.AudioType.MEDIA).stop();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mUpdateListReceiver);
        if (MyApplication.getAudioUtils() != null) {
            MyApplication.getAudioUtils().release();
        }

    }

    /**
     * 1.设置监听广播，这样就可以知道是否有音乐的加入，从而刷新列表
     * 2.当下载Ok的时候，通知系统去扫描指定位置，将音乐加入media相关存储
     */


}
