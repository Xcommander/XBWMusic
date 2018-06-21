package com.kidosc.kidomusic.model;

/**
 * Created by jason.xu on 2018/5/4.
 */
public class MusicDesInfo {
    /**
     * 歌曲ID
     */
    private long id;
    /**
     * 歌曲名称
     */
    private String title;
    /**
     * 专辑
     */
    private String album;
    /**
     * 专辑ID
     */
    private int albumId;
    /**
     * 显示名称
     */
    private String displayName;
    /**
     * 歌手名称
     */
    private String artist;
    /**
     * 歌曲时长
     */
    private long duration;
    /**
     * 歌曲大小
     */
    private long size;
    /**
     * 歌曲路径
     */
    private String url;
    /**
     * 歌词名称
     */
    private String lrcTitle;
    /**
     * 歌词大小
     */
    private String lrcSize;

    /**
     * 发行时间
     */
    private String year;
    /**
     * 流派
     */
    private String genre;

    /**
     * 专辑图片
     */
    private String image;


    public MusicDesInfo() {
        super();

    }

    public MusicDesInfo(long id, String title, String album, int albumId, String displayName, String artist, long duration, long size, String url, String lrcTitle, String lrcSize) {
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getAlbumId() {
        return albumId;
    }

    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLrcTitle() {
        return lrcTitle;
    }

    public void setLrcTitle(String lrcTitle) {
        this.lrcTitle = lrcTitle;
    }

    public String getLrcSize() {
        return lrcSize;
    }

    public void setLrcSize(String lrcSize) {
        this.lrcSize = lrcSize;
    }
    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

}
