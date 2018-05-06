package com.kidosc.kidomusic.util;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by jason.xu on 2018/5/3.
 */
public class HttpUtil {
    /**
     * 封装OkHttp的网络请求
     *
     * @param address  请求的URL
     * @param callback 回调函数,当请求request success or failed,在主线程做出不同的反馈
     */
    public static void sendOkHttpRequest(final String address, final Callback callback) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().retryOnConnectionFailure(true).build();
        Request request = new Request.Builder().url(address).build();
        okHttpClient.newCall(request).enqueue(callback);

    }
}
