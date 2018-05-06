package com.kidosc.kidomusic.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by jason.xu on 2018/5/3.
 */
public class MusicResponse {
    /**
     * request success,返回的标志位
     */
    @SerializedName("ok")
    public String status;

    /**
     * 返回的歌曲列表
     */
    @SerializedName("results")
    public List<MusicInfo> musicInfoList;
    /**
     * 返回的id
     */
    public String id;
}
