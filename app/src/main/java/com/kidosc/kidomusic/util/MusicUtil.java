package com.kidosc.kidomusic.util;


import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kidosc.kidomusic.R;
import com.kidosc.kidomusic.activity.MyApplication;
import com.kidosc.kidomusic.gson.MusicInfo;
import com.kidosc.kidomusic.model.DownloadModel;
import com.kidosc.kidomusic.model.MusicDesInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.MODE_PRIVATE;


/**
 * Created by jason.xu on 2018/5/3.
 */
public class MusicUtil {

    /**
     * 将json数据转化成object
     */
    public static MusicInfo handleMusicResponse(final String data) {
        try {
            JSONObject jsonObject = new JSONObject(data);
            String musicContent = jsonObject.toString();
            return new Gson().fromJson(musicContent, MusicInfo.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * 创建线程池来处理线程资源
     *
     * @param runnable
     */
    public static void handleThread(Runnable runnable) {
        ExecutorService singleThreadPool = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
        singleThreadPool.execute(runnable);
        singleThreadPool.shutdown();

    }

    /**
     * 将音乐中的时间转化成标准格式，例如 03:20
     *
     * @param time
     * @return
     */

    public static String formatTime(long time) {
        String min = time / (1000 * 60) + "";
        String sec = time % (1000 * 60) + "";
        if (min.length() < 2) {
            min = "0" + time / (1000 * 60) + "";
        } else {
            min = time / (1000 * 60) + "";
        }
        if (sec.length() == 4) {
            sec = "0" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 3) {
            sec = "00" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 2) {
            sec = "000" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 1) {
            sec = "0000" + (time % (1000 * 60)) + "";
        }
        return min + ":" + sec.trim().substring(0, 2);
    }

    /**
     * 根据按键类型，调节音量
     *
     * @param volume
     * @return
     */
    public static int getMusicVolume(int volume, String type) {
        if (Constant.VOULME_UP.equals(type)) {
            if (volume < 1) {
                volume = 1;
            } else if (volume < 3) {
                volume = 3;
            } else if (volume < 5) {
                volume = 5;
            } else if (volume < 7) {
                volume = 7;
            } else if (volume < 9) {
                volume = 9;
            } else if (volume < 10) {
                volume = 10;
            } else if (volume < 11) {
                volume = 11;
            } else if (volume < 13) {
                volume = 13;
            } else if (volume <= 15) {
                volume = 15;
            }

        } else {
            if (volume <= 1) {
                volume = 0;
            } else if (volume <= 3) {
                volume = 1;
            } else if (volume <= 5) {
                volume = 3;
            } else if (volume <= 7) {
                volume = 5;

            } else if (volume <= 9) {
                volume = 7;
            } else if (volume <= 10) {
                volume = 9;

            } else if (volume <= 11) {
                volume = 10;

            } else if (volume <= 13) {
                volume = 11;

            } else if (volume <= 15) {
                volume = 13;
            }


        }

        return volume;

    }

    /**
     * 根据音量返回图标
     *
     * @param volume
     * @return
     */

    public static Drawable getVolumePic(int volume) {
        Drawable drawable = MyApplication.getApplication().getDrawable(R.drawable.ic_vol_0);
        switch (volume) {
            case 0:
                drawable = MyApplication.getApplication().getDrawable(R.drawable.ic_vol_0);
                break;
            case 1:
                drawable = MyApplication.getApplication().getDrawable(R.drawable.ic_vol_1);
                break;
            case 3:
                drawable = MyApplication.getApplication().getDrawable(R.drawable.ic_vol_2);
                break;
            case 5:
                drawable = MyApplication.getApplication().getDrawable(R.drawable.ic_vol_3);
                break;
            case 7:
                drawable = MyApplication.getApplication().getDrawable(R.drawable.ic_vol_4);
                break;
            case 9:
                drawable = MyApplication.getApplication().getDrawable(R.drawable.ic_vol_5);
                break;
            case 10:
                drawable = MyApplication.getApplication().getDrawable(R.drawable.ic_vol_6);
                break;
            case 11:
                drawable = MyApplication.getApplication().getDrawable(R.drawable.ic_vol_7);
                break;
            case 13:
                drawable = MyApplication.getApplication().getDrawable(R.drawable.ic_vol_8);
                break;
            case 15:
                drawable = MyApplication.getApplication().getDrawable(R.drawable.ic_vol_9);
                break;
            default:
                //do nothing
                break;
        }
        return drawable;
    }

    /**
     * 根据路径,返回MP3的码率
     *
     * @param url
     * @return
     */
    @NonNull
    public static String getMP3KBitsRate(String url) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(url);
        String bit = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
        String Kbit = bit.substring(0, bit.length() - 3);
        return Kbit + "kbps";
    }

    /**
     * 获取存储的歌曲
     *
     * @return
     */
    public static List<MusicDesInfo> getMuiscInfos() {
        Cursor cursor = MyApplication.getApplication().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.MediaColumns.DATA + " like ?", new String[]{Constant.MUSIC_DIR + "%"},
                MediaStore.Audio.Media._ID);
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
            Log.e("xulinchao22", "getMuiscInfos: _id = "+id );
            if (isMusic != 0) {
                // 只把音乐添加到集合当中
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

    /**
     * 涉及到多线程，所以用锁
     */

    public synchronized static void updateList() {
        Constant.ALL_MUSIC_LIST.clear();
        Constant.ALL_MUSIC_LIST.addAll(getMuiscInfos());
    }

    /**
     * 将ArrayList数据转化成json数据保存到SharedPreferences
     *
     * @param spName    SharedPreferences名
     * @param name      键值名
     * @param arrayList 要转化的ArrayList
     */
    public static void saveToJson(String spName, String name, ArrayList<DownloadModel> arrayList) {
        SharedPreferences.Editor editor = MyApplication.getApplication().getSharedPreferences(spName, MODE_PRIVATE).edit();
        String json = (new Gson()).toJson(arrayList);
        Log.e("xulinchao", "saveToJson: " + json);
        editor.putString(name, json);
        editor.commit();
    }

    /**
     * 从SharedPreferences把保存的json数据拿出来，转化成ArrayList
     *
     * @param spName SharedPreferences文件名
     * @param name   键值名
     * @return
     */
    public static ArrayList<DownloadModel> jsonToList(String spName, String name) {
        SharedPreferences sharedPreferences = MyApplication.getApplication().getSharedPreferences(spName, MODE_PRIVATE);
        String json = sharedPreferences.getString(name, null);
        ArrayList<DownloadModel> models = new ArrayList<>();
        if (json != null) {
            Type type = new TypeToken<List<DownloadModel>>() {
            }.getType();
            models = (new Gson()).fromJson(json, type);
            if (models.size() != 0) {
                Log.e("xulinchao", "jsonToList: ");
            }

        }
        return models;

    }

    /**
     * 判断当前的网络状况
     * @return
     */
    public static boolean isNetWorkOK() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) MyApplication.getApplication().getSystemService(CONNECTIVITY_SERVICE);
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            Log.e("xulinchao22", "isNetWorkOK: " + networkCapabilities.toString());
            return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


}
