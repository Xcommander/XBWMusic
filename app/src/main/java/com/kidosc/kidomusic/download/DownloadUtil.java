package com.kidosc.kidomusic.download;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by jason.xu on 2018/5/10.
 */
public class DownloadUtil {

    /**
     * 各种实例
     */
    private static DownloadUtil mInstance;
    private Application mApp;
    private DownloadService.DownLoadBinder mDownloadBinder;
    /**
     * ServiceConnection实例
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mDownloadBinder = (DownloadService.DownLoadBinder) service;
            Log.e("xulinchao", "onServiceConnected: ");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mDownloadBinder = null;
        }
    };

    /**
     * 单例模式-懒汉,不存在竞争关系，所以不需要双锁模式
     *
     * @param application
     * @return
     */
    public static DownloadUtil getmInstance(Application application) {
        if (mInstance == null) {
            mInstance = new DownloadUtil(application);
        }
        return mInstance;
    }

    private DownloadUtil(Application application) {
        mApp = application;
        Init();
    }

    /**
     * 初始化,启动下载服务
     */

    private void Init() {
        if (mApp != null) {
            Intent intent = new Intent(mApp, DownloadService.class);
            mApp.startService(intent);
            mApp.bindService(intent, mConnection, BIND_AUTO_CREATE);
        }
    }

    /**
     * 当系统退出时，释放
     */
    public void release() {
        Intent intent = new Intent(mApp, DownloadService.class);
        mApp.stopService(intent);
        mApp.unbindService(mConnection);
        mApp = null;
        mDownloadBinder = null;
    }

    /**
     * 获取DownloadBinder实例
     *
     * @return
     */
    public DownloadService.DownLoadBinder getmDownloadBinder() {
        while (mDownloadBinder == null) {
            //等待初始化，此log不能去掉，可以起到延时，防止一切都初始化失败
            Log.e("xulinchao", "getmDownloadBinder: wait" );
        }
        return mDownloadBinder;
    }
}
