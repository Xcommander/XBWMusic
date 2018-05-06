package com.kidosc.kidomusic.util;


import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.google.gson.Gson;
import com.kidosc.kidomusic.gson.MusicResponse;
import com.kidosc.kidomusic.model.MusicDesInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Created by jason.xu on 2018/5/3.
 */
public class MusicUtil {

    /**
     * 将json数据转化成object
     */
    public static MusicResponse handleMusicResponse(final String data) {
        try {
            JSONObject jsonObject = new JSONObject(data);
            String musicContent = jsonObject.toString();
            return new Gson().fromJson(musicContent, MusicResponse.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * 创建线程池来处理线程资源
     *
     * @param runnable
     */
    public static void handleThread(Runnable runnable) {
        ExecutorService singleThreadPool = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
        singleThreadPool.execute(runnable);
        singleThreadPool.shutdown();

    }

    public static String formaTime(long time) {
        String min = time / (1000 * 60) + "";
        String sec = time % (1000 * 60) + "";
        if (min.length() < 2) {
            min = "0" + time / (1000 * 60) + "";
        } else {
            min = time / (1000 * 60) + "";
        }
        if (sec.length() == 4) {
            sec = "0" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 3) {
            sec = "00" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 2) {
            sec = "000" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 1) {
            sec = "0000" + (time % (1000 * 60)) + "";
        }
        return min + ":" + sec.trim().substring(0, 2);
    }

}
