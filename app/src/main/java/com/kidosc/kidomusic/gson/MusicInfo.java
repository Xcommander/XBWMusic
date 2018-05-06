package com.kidosc.kidomusic.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by jason.xu on 2018/5/3.
 */
public class MusicInfo {
    /**
     * 未知含义的变量
     */
    public String zm;
    /**
     * 歌手
     */
    public String singer;
    /**
     * 音乐名字
     */
    @SerializedName("muisc_name")
    public String muiscname;
    /**
     * 创建时间
     */
    @SerializedName("create_time")
    public String createtime;
    /**
     * 文件大小
     */
    @SerializedName("file_size")
    public String filesize;
    /**
     * 下载路径
     */
    public String path;

    /**
     * 歌曲的id，不知道是否是hash值
     */
    @SerializedName("_id")
    public MusicInfoId musicInfoId;

    public class MusicInfoId {
        @SerializedName("$oid")
        public String oid;
    }


}
