package com.kidosc.kidomusic.util;


import com.kidosc.kidomusic.model.MusicDesInfo;

import java.util.ArrayList;

/**
 * Created by jason.xu on 2018/5/2.
 */
public class Constant {
    /**
     * 播放序列
     */
    public static final String POP_WINDOW_SEQUENCE = "sequence";
    /**
     * 音量
     */
    public static final String POP_WINDOW_VOLUME = "volume";

    /**
     * 测试音乐用例的url
     */
    public static final String ALL_MUSIC_URL = "http://download.study-watch.net:27080/music/file/_find";

    /**
     * 请求的返回success的标志位
     */
    public static final String REQUEST_SUCCESS = "1";
    /**
     * 下载的URL
     */
    public static final String MUSIC_ONLINE_URL = "http://main.study-watch.net:8080/data/";

    /**
     * download task下载的状态
     * TYPE_SUCCESS:下载成功
     * TYPE_FAILED:下载失败
     * TYPE_PAUSED:下载暂停(此功能暂且没开发，留下标志位，以便后续需要)
     * TYPE_CANCELED:下载去掉(此功能暂且没开发，留下标志位，以便后续需要)
     */
    public static final int TYPE_SUCCESS = 0;
    public static final int TYPE_FAILED = 1;
    public static final int TYPE_PAUSED = 2;
    public static final int TYPE_CANCELED = 3;

    /**
     * 存放音乐的路径
     */
    public static final String MUSIC_DIR = "/storage/emulated/0/Music";


    /**
     * 音乐列表，暂且用静态变量存储
     */
    public static ArrayList<MusicDesInfo> ALL_MUSIC_LIST = new ArrayList<>();

    /**
     * 音量上键，下键
     */

    public static final String VOULME_UP = "volume_up";
    public static final String VOULME_DOWN = "volume_down";

    /**
     * 音乐播放序列
     */
    public static final int TYPE_ORDER = 0;
    public static final int TYPE_CYCLE = 1;
    public static final int TYPE_CYCLE_ALL = 2;
    public static final int TYPE_RANDOM = 3;

    /**
     * 播放序列的标志位,默认是顺序播放
     */
    public static int TYPE_MUSIC_PLAY_FLAG = 0;

    /**
     * 上一首，下一首
     */
    public static final String MUSIC_NEXT = "next";
    public static final String MUSIC_BACK = "back";

    /**
     * music下载广播，下载成功的广播
     */
    public static final String ACTION_DOWNLOAD = "com.kidosc.xbw_download";
    public static final String ACTION_DOWNLOAD_SUCCESS = "com.kidosc.xbw_done";

    /**
     * 更新主界面广播
     */
    public static final String ACTION_UPDATE_LIST = "com.kidosc.kidomusic.update_list";

    /**
     * sharedPreferences相关
     */
    public static final String SP_NAME_NETWORT_FAILED = "network";
    public static final String SP_NAME_RESOURCE_FAILED = "resource";
    public static final String KEY_NETWORT_FAILED = "network";
    public static final String KEY_RESOURCE_FAILED = "resource";

    /**
     * (1)PAGE_DES_INFO : 歌曲的详细信息标志位
     * (2)PAGE_ALBUM_IMAGE : 专辑的图片标志位
     * (3)PAGE_MUSIC_LRC : 歌词的标志位
     */
    public final static int PAGE_DES_INFO = 0;
    public final static int PAGE_ABLUM_INMAGE = 1;
    public final static int PAGE_MUISC_LRC = 2;


}
