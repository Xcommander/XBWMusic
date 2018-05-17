package com.kidosc.kidomusic.model;

/**
 * Created by jason.xu on 2018/5/16.
 */
public class DownloadModel {
    private String downloadUrl;
    private String Oid;
    public int count = 0;

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getOid() {
        return Oid;
    }

    public void setOid(String oid) {
        Oid = oid;
    }

    public DownloadModel(String downloadUrl, String oid, int count) {
        this.downloadUrl = downloadUrl;
        Oid = oid;
        this.count = count;
    }

    public DownloadModel() {
    }
}
