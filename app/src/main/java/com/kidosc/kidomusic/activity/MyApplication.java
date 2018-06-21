package com.kidosc.kidomusic.activity;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.kidosc.kidomusic.audio.AudioUtils;
import com.kidosc.kidomusic.download.DownloadService;
import com.kidosc.kidomusic.download.DownloadUtil;
import com.kidosc.kidomusic.util.Constant;

import org.jetbrains.annotations.Contract;

/**
 * Created by jason.xu on 2018/5/4.
 */
public class MyApplication extends Application {
    private static Application application;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("xulinchao", "onCreate: ");
        application = this;

    }

    @Contract(pure = true)
    public static Application getApplication() {
        return application;
    }

    public static AudioUtils getAudioUtils() {
        return AudioUtils.getInstance(application);
    }

    public static DownloadUtil getDownloadUtil() {
        return DownloadUtil.getmInstance(application);
    }



}
