package com.kidosc.kidomusic.activity;

import android.app.Application;
import android.util.Log;

import com.kidosc.kidomusic.audio.AudioUtils;

/**
 * Created by jason.xu on 2018/5/4.
 */
public class MyApplication extends Application{
    protected static Application application;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("xulinchao", "onCreate: " );
        application=this;
    }

    public static AudioUtils getAudioUtils() {
        return AudioUtils.getInstance(application);
    }
}
