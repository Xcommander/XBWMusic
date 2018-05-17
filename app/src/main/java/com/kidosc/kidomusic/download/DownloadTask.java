package com.kidosc.kidomusic.download;

import android.os.AsyncTask;
import android.util.Log;

import com.kidosc.kidomusic.util.Constant;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by jason.xu on 2018/5/3.
 */
public class DownloadTask extends AsyncTask<String, Integer, Integer> {
    /**
     * 最新的进度
     */
    private int mLastProcess;

    /**
     * 是否取消，这个是后面预留
     */
    private boolean mIsCanceled = false;
    /**
     * 是否停止,这个为后面预留
     */
    private boolean mIsPaused = false;
    /**
     * 监听接口，以便在各个状态下回调对应函数
     */
    private DownloadListener mDownloadListener;

    public DownloadTask(DownloadListener loadListener) {
        this.mDownloadListener = loadListener;

    }


    @Override
    protected Integer doInBackground(String... strings) {
        Log.e("xulinchao", "doInBackground: ");
        InputStream is = null;
        RandomAccessFile saveFile = null;
        File file = null;
        try {
            long downloadedLength = 0L;
            String downloadUrl = strings[0];
            String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
            file = new File(Constant.MUSIC_DIR + fileName);
            if (file.exists()) {
                downloadedLength = file.length();
            }
            long contentLength = getContentLength(downloadUrl);
            if (contentLength == 0) {
                return Constant.TYPE_FAILED;
            } else if (downloadedLength == contentLength) {
                //已下载的字节和文件的总字节相等，说明已经下载完成了
                return Constant.TYPE_SUCCESS;
            }
            OkHttpClient okHttpClient = new OkHttpClient().newBuilder().retryOnConnectionFailure(true).build();
            Request request = new Request.Builder()
                    // 断点下载，指定从哪个字节开始下载
                    .addHeader("RANGE", "bytes=" + downloadedLength + "-")
                    .url(downloadUrl)
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            if (response != null) {
                is = response.body().byteStream();
                saveFile = new RandomAccessFile(file, "rw");
                //跳过已下载的字节
                saveFile.seek(downloadedLength);
                byte[] b = new byte[1024];
                int total = 0;
                int len;
                while ((len = is.read(b)) != -1) {
                    if (mIsCanceled) {
                        return Constant.TYPE_CANCELED;
                    } else if (mIsPaused) {
                        return Constant.TYPE_PAUSED;
                    } else {
                        total += len;
                        saveFile.write(b, 0, len);
                        int progress = (int) ((total + downloadedLength) * 100 / contentLength);
                        publishProgress(progress);

                    }

                }
                response.body().close();
                return Constant.TYPE_SUCCESS;

            }

        } catch (IOException e) {
            Log.e("xulinchao", "doInBackground: error" );
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (saveFile != null) {

                    saveFile.close();

                }
                if (mIsCanceled && file != null) {
                    file.delete();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Constant.TYPE_FAILED;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        int status = integer;
        switch (status) {
            case Constant.TYPE_SUCCESS:
                mDownloadListener.onSuccess();
                break;
            case Constant.TYPE_FAILED:
                mDownloadListener.onFailed();
                break;
            case Constant.TYPE_PAUSED:
                mDownloadListener.onPaused();
                break;
            case Constant.TYPE_CANCELED:
                mDownloadListener.onCanceled();
            default:
                break;
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if (progress > mLastProcess) {
            mDownloadListener.onProgress(progress);
            mLastProcess = progress;
        }
    }

    /**
     * 暂停下载
     */
    public void pauseDownload() {
        mIsPaused = true;
    }

    /**
     * 取消下载
     */
    public void cancelDownload() {
        mIsCanceled = true;
    }

    /**
     * 根据路径去获取文件大小
     *
     * @param downloadUrl
     * @return
     * @throws IOException
     */

    private long getContentLength(String downloadUrl) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder().retryOnConnectionFailure(true).build();
        Request request = new Request.Builder().url(downloadUrl).build();
        Response response = okHttpClient.newCall(request).execute();
        if (response != null && response.isSuccessful()) {
            long contentLength = response.body().contentLength();
            response.close();
            return contentLength;
        }
        return 0;

    }
}
