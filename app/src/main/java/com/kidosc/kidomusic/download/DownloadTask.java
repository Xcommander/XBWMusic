package com.kidosc.kidomusic.download;

import android.os.AsyncTask;

import com.kidosc.kidomusic.util.Constant;

/**
 * Created by jason.xu on 2018/5/3.
 */
public class DownloadTask extends AsyncTask<String, Integer, Integer> {
    /**
     * 最新的进度
     */
    private int mLastProcess;
    /**
     * 监听接口，以便在各个状态下回调对应函数
     */
    private DownLoadListener mDownloadListener;

    public DownloadTask(DownLoadListener loadListener) {
        this.mDownloadListener = loadListener;

    }


    @Override
    protected Integer doInBackground(String... strings) {
        return null;
    }
}
