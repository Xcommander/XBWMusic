package com.kidosc.kidomusic.util;

import android.os.Environment;

import com.kidosc.kidomusic.model.MusicDesInfo;

import java.util.ArrayList;
import java.util.List;

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
     *测试音乐用例的url
     */
    public static final String ALL_MUSIC_URL="http://download.study-watch.net:27080/music/file/_find";

    /**
     * 请求的返回success的标志位
     */
    public static final String REQUEST_SUCCESS="1";
    /**
     * 在线播放的URL
     */
    public static final String MUSIC_ONLINE_URL="http://main.study-watch.net:8080/data";

    /**
     * download task下载的状态
     * TYPE_SUCCESS:下载成功
     * TYPE_FAILED:下载失败
     * TYPE_PAUSED:下载暂停(此功能暂且没开发，留下标志位，以便后续需要)
     * TYPE_CANCELED:下载去掉(此功能暂且没开发，留下标志位，以便后续需要)
     */
    public static final int TYPE_SUCCESS=0;
    public static final int TYPE_FAILED = 1;
    public static final int TYPE_PAUSED = 2;
    public static final int TYPE_CANCELED = 3;

    /**
     * 存放音乐的路径
     */
    public static final String MUSIC_DIR= "/storage/emulated/0/zhx_watch";

    /**
     * 音乐列表
     */
    public static List<MusicDesInfo> ALL_MUSIC_LIST=new ArrayList<>();



}
