package com.kidosc.kidomusic.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.kidosc.kidomusic.R;
import com.kidosc.kidomusic.adapter.MusicListAdapter;
import com.kidosc.kidomusic.gson.MusicResponse;
import com.kidosc.kidomusic.model.MusicDesInfo;
import com.kidosc.kidomusic.util.Constant;
import com.kidosc.kidomusic.util.HttpUtil;
import com.kidosc.kidomusic.util.MusicUtil;
import com.kidosc.kidomusic.widget.DashlineItemDivider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends Activity {
    private RecyclerView mMusicListView;
    private ArrayList<MusicDesInfo> mMusicInfoList;
    private RecyclerView.LayoutManager mManager;
    private MusicListAdapter musicListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMusicListView = findViewById(R.id.music_list);
        mMusicInfoList = new ArrayList<>();
        mManager = new LinearLayoutManager(this);
        musicListAdapter = new MusicListAdapter(this, mMusicInfoList);
        mMusicListView.setAdapter(musicListAdapter);
        mMusicListView.setLayoutManager(mManager);
        //添加分割线
        mMusicListView.addItemDecoration(new DashlineItemDivider());

        testScan();
    }


    /**
     * 测试用例,从网站上获取音乐列表
     */
    public void testData() {
        HttpUtil.sendOkHttpRequest(Constant.ALL_MUSIC_URL, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("xulinchao", "onFailure: ");

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String data = response.body().string();
                MusicResponse musicResponse = MusicUtil.handleMusicResponse(data);
                if (musicResponse != null && Constant.REQUEST_SUCCESS.equals(musicResponse.status)) {
                    for (int i = 0; i < musicResponse.musicInfoList.size(); i++) {
//                        mMusicInfoList.add(musicResponse.musicInfoList.get(i));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                musicListAdapter.notifyDataSetChanged();

                            }
                        });
                    }

                } else {
                    Log.e("xulinchao", "onResponse: 2");
                }

            }
        });

    }

    /**
     * 测试手动目录扫描
     */
    public void testScan() {
        MusicUtil.handleThread(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    int checkSelfPermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
                    if (checkSelfPermission == PackageManager.PERMISSION_DENIED) {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        Constant.ALL_MUSIC_LIST = getMuiscInfos(getApplicationContext());
                        mMusicInfoList.addAll(Constant.ALL_MUSIC_LIST);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                musicListAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }

            }
        });
    }

    public List<MusicDesInfo> getMuiscInfos(Context context) {

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.MediaColumns.DATA + " like ?", new String[]{Constant.MUSIC_DIR + "%"},
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        List<MusicDesInfo> musicDesInfos = new ArrayList<MusicDesInfo>();
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToNext();
            // 音乐id
            long id = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Audio.Media._ID));
            // 音乐标题
            String title = cursor.getString((cursor
                    .getColumnIndex(MediaStore.Audio.Media.TITLE)));
            // 艺术家
            String artist = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.ARTIST));
            // 专辑
            String album = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.ALBUM));
            String displayName = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
            long albumId = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            // 时长
            long duration = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Audio.Media.DURATION));
            // 文件大小
            long size = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Audio.Media.SIZE));
            // 文件路径
            String url = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.DATA));
            // 是否为音乐
            int isMusic = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
            if (isMusic != 0) {
                // 只把音乐添加到集合当中
                Log.e("xulinchao", "getMuiscInfos: " + url);
                MusicDesInfo musicDesInfo = new MusicDesInfo();
                musicDesInfo.setTitle(title);
                musicDesInfo.setArtist(artist);
                musicDesInfo.setAlbum(album);
                musicDesInfo.setDisplayName(displayName);
                musicDesInfo.setAlbumId(albumId);
                musicDesInfo.setDuration(duration);
                musicDesInfo.setSize(size);
                musicDesInfo.setUrl(url);
                musicDesInfos.add(musicDesInfo);
            }
        }
        cursor.close();
        return musicDesInfos;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE) && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {
                //用户同意
                testScan();
            } else {
                //用户不同意

            }
        }
    }
    /**
     * 1.设置监听广播，这样就可以知道是否有音乐的加入，从而刷新列表
     * 2.当下载Ok的时候，通知系统去扫描指定位置，将音乐加入media相关存储
     */

}
