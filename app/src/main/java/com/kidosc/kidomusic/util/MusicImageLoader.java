package com.kidosc.kidomusic.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;

import org.jetbrains.annotations.Contract;

/**
 * Created by jason.xu on 2018/6/19.
 * des:加载图片
 */
public class MusicImageLoader {
    private static MusicImageLoader mInstance;
    private LruCache<String, Bitmap> mCache;

    @Contract(pure = true)
    public static MusicImageLoader getmInstance() {
        if (mInstance == null) {
            mInstance = new MusicImageLoader();
        }
        return mInstance;
    }

    public MusicImageLoader() {
        int maxSize = (int) (Runtime.getRuntime().maxMemory() / 8);
        mCache = new LruCache<String, Bitmap>(maxSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
    }

    /**
     * 根据路径获取图片
     * @param uri
     * @return
     */
    public Bitmap load(final String uri) {
        if (uri == null) {
            return null;
        }

        final String key = Encrypt.md5(uri);
        Bitmap bmp = getFromCache(key);

        if (bmp != null) {
            return bmp;
        }

        bmp = BitmapFactory.decodeFile(uri);
        addToCache(key, bmp);
        return bmp;
    }

    /**
     * 从内存中获取图片
     * @param key
     * @return
     */
    private Bitmap getFromCache(final String key) {
        return mCache.get(key);
    }

    /**
     * 将图片缓存到内存中
     * @param key
     * @param bmp
     */
    private void addToCache(final String key, final Bitmap bmp) {
        if (getFromCache(key) == null && key != null && bmp != null) {
            mCache.put(key, bmp);
        }
    }
}
