package com.kidosc.kidomusic.model;

/**
 * Created by jason.xu on 2018/5/4.
 */
public class MusicDesInfo {
    private long id; // 歌曲ID 3
    private String title; // 歌曲名称 0
    private String album; // 专辑 7
    private long albumId;//专辑ID 6
    private String displayName; //显示名称 4
    private String artist; // 歌手名称 2
    private long duration; // 歌曲时长 1
    private long size; // 歌曲大小 8
    private String url; // 歌曲路径 5
    private String lrcTitle; // 歌词名称
    private String lrcSize; // 歌词大小

    public MusicDesInfo() {
        super();

    }

    public MusicDesInfo(long id, String title, String album, long albumId,
                        String displayName, String artist, long duration, long size,
                        String url, String lrcTitle, String lrcSize) {
        super();
        this.id = id;
        this.title = title;
        this.album = album;
        this.albumId = albumId;
        this.displayName = displayName;
        this.artist = artist;
        this.duration = duration;
        this.size = size;
        this.url = url;
        this.lrcTitle = lrcTitle;
        this.lrcSize = lrcSize;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getAlbum() {
        return album;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getArtist() {
        return artist;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getDuration() {
        return duration;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getSize() {
        return size;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setLrcTitle(String lrcTitle) {
        this.lrcTitle = lrcTitle;
    }

    public String getLrcTitle() {
        return lrcTitle;
    }

    public void setLrcSize(String lrcSize) {
        this.lrcSize = lrcSize;
    }

    public String getLrcSize() {
        return lrcSize;
    }
}
