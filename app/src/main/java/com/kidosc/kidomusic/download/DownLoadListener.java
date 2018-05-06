package com.kidosc.kidomusic.download;

/**
 * Created by jason.xu on 2018/5/3.
 */
public interface DownLoadListener {
    void onProgress(int progress);

    void onSuccess();

    void onFailed();

    /**
     * 预留的接口函数,暂停下载
     */
    void onPaused();
    /**
     * 预留的接口函数,取消下载
     */
    void onCanceled();
}
