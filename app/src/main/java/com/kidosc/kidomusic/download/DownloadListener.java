package com.kidosc.kidomusic.download;

/**
 * Created by jason.xu on 2018/5/3.
 */
public interface DownloadListener {
    /**
     * 更新下载进度
     *
     * @param progress
     */
    void onProgress(int progress);

    /**
     * 下载成功
     */
    void onSuccess();

    /**
     * 下载失败
     */

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
