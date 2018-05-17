package com.kidosc.kidomusic.download;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.kidosc.kidomusic.activity.MyApplication;
import com.kidosc.kidomusic.model.DownloadModel;
import com.kidosc.kidomusic.util.Constant;
import com.kidosc.kidomusic.util.MusicUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jason.xu on 2018/5/9.
 */
public class DownloadService extends Service {
    /**
     * 下载列表,
     * 暂且通过task是否为空的唯一性来控制下载的唯一性
     * 改进的方法是通过handler来进行下载控制，下载完成，发送success继续下载下一个，下载failed，也继续下载下一个
     */
    private ArrayList<DownloadModel> mDownloadList = new ArrayList<>();
    /**
     * 下载的task
     */
    private DownloadTask mDownloadTask;

    /**
     * 下载对象
     */
    private DownloadModel mDownloadModel;

    /**
     * 网络失败的列表
     */
    private ArrayList<DownloadModel> mNetWorkDownloadFailedList = new ArrayList<>();
    /**
     * 资源导致的失败列表
     */
    private ArrayList<DownloadModel> mResourceDownloadFailedList = new ArrayList<>();
    /**
     * 下载的监听函数
     */
    private DownloadListener mDownloadListener = new DownloadListener() {
        @Override
        public void onProgress(int progress) {
            //下载进度,如果服务端需要的话，则上传

        }

        @Override
        public void onSuccess() {
            //下载完成后,(1)通知媒体库来更新，并且更新当前的列表;(2)发送广播
            MediaScannerConnection.scanFile(DownloadService.this, new String[]{Constant.MUSIC_DIR +
                            mDownloadModel.getDownloadUrl().substring(mDownloadModel.getDownloadUrl().lastIndexOf("/"))}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            Log.e("xulinchao", "onScanCompleted: path = " + path + " uri = " + uri);
                            //扫描完成后，则更新列表
                            mDownloadBinder.getmDownloadUpdate().update();
                            //发送广播,下载完成广播
                            Intent intent = new Intent();
                            intent.setAction(Constant.ACTION_DOWNLOAD_SUCCESS);
                            intent.putExtra("Oid", mDownloadModel.getOid());
                            sendBroadcast(intent);
                        }
                    });
            Log.e("xulinchao", "onSuccess: " + mDownloadList.size());
            //如果下载成功，则继续下载下一个
            mDownloadTask = null;
            mDownloadList.remove(mDownloadModel);
            if (mDownloadList.size() != 0) {
                mDownloadBinder.startDownload(mDownloadList.get(0));
            } else {
                /**
                 * 当正常列表下载完成后，我们尝试下载资源异常和网络异常列表中的,这时候相当于新的任务
                 */
                if (mResourceDownloadFailedList.size() != 0) {
                    mDownloadBinder.downloadTask(mResourceDownloadFailedList.get(0));
                    mResourceDownloadFailedList.remove(mResourceDownloadFailedList.get(0));
                } else if (mNetWorkDownloadFailedList.size() != 0) {
                    mDownloadBinder.downloadTask(mNetWorkDownloadFailedList.get(0));
                    mNetWorkDownloadFailedList.remove(mNetWorkDownloadFailedList.get(0));
                }
            }


        }

        @Override
        public void onFailed() {
            /**
             * (1)资源问题，则直接下载后面一首
             * (2)网络原因,则等待网络OK在恢复下载
             */
            mDownloadTask = null;
            mDownloadList.remove(mDownloadModel);
            if (MusicUtil.isNetWorkOK()) {
                /**
                 * 资源有问题的话
                 * （1）如果失败了5次，那么移除资源
                 * （2）否则，加入资源失败的列表，准备后续下载
                 */
                if (mDownloadModel.count != 5) {
                    mResourceDownloadFailedList.add(mDownloadModel);
                } else {
                    //这里是否提示用户
                }
                /**
                 * 继续下载正常列表中的
                 */
                if (mDownloadList.size() != 0) {
                    mDownloadBinder.startDownload(mDownloadList.get(0));
                } else {
                    /**
                     * 当正常列表下载完成后，我们尝试下载资源异常和网络异常列表中的,这时候相当于新的任务
                     */
                    if (mResourceDownloadFailedList.size() != 0) {
                        mDownloadBinder.downloadTask(mResourceDownloadFailedList.get(0));
                        mResourceDownloadFailedList.remove(mResourceDownloadFailedList.get(0));
                    } else if (mNetWorkDownloadFailedList.size() != 0) {
                        mDownloadBinder.downloadTask(mNetWorkDownloadFailedList.get(0));
                        mNetWorkDownloadFailedList.remove(mNetWorkDownloadFailedList.get(0));
                    }
                }

            } else {
                /**
                 * 说明网络断开，则将不能下载的都加入到列表中,等待网络OK
                 */
                mNetWorkDownloadFailedList.add(mDownloadModel);

            }


        }

        @Override
        public void onPaused() {
            //下载暂停，如果服务端需要,则上传


        }

        @Override
        public void onCanceled() {
            //取消下载,如果服务端需要，则上传

        }
    };

    private DownLoadBinder mDownloadBinder = new DownLoadBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mDownloadBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //设置网络回调
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        connectivityManager.requestNetwork(new NetworkRequest.Builder().build(), new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                Log.e("xulinchao", "onAvailable: 1111");
                //网络恢复，我们尝试去恢复下载
                ArrayList<DownloadModel> networkList = new ArrayList<>();
                ArrayList<DownloadModel> resourceList = new ArrayList<>();
                networkList.addAll(mNetWorkDownloadFailedList);
                resourceList.addAll(mResourceDownloadFailedList);
                if (networkList.size() > 0) {
                    for (int i = 0; i < networkList.size(); i++) {
                        mDownloadBinder.downloadTask(networkList.get(i));
                    }
                }
                if (resourceList.size() > 0) {
                    for (int i = 0; i < resourceList.size(); i++) {
                        mDownloadBinder.downloadTask(resourceList.get(i));
                    }
                }

            }
        });
    }

    public class DownLoadBinder extends Binder {
        /**
         * 在DownloadBinder设置监听函数，这样方便service更新（也可以直接在service中设置）
         */
        private DownloadUpdate mDownloadUpdate;

        public void setmDownloadUpdate(DownloadUpdate mDownloadUpdate) {
            Log.e("xulinchao", "setmDownloadUpdate: " + mDownloadUpdate.toString());
            this.mDownloadUpdate = mDownloadUpdate;
        }

        public DownloadUpdate getmDownloadUpdate() {
            return mDownloadUpdate;
        }

        /**
         * 获取当前的service实例
         *
         * @return
         */
        public DownloadService getService() {
            return DownloadService.this;
        }

        /**
         * 提供给外部，下载任务接口
         *
         * @param model
         */

        public void downloadTask(DownloadModel model) {
            Log.e("xulinchao", "downloadTask: " + model.getDownloadUrl());
            mDownloadList.add(model);
            startDownload(model);
        }

        /**
         * 下载接口
         *
         * @param model
         */
        private void startDownload(DownloadModel model) {
            if (mDownloadTask == null && model != null) {
                mDownloadTask = new DownloadTask(mDownloadListener);
                mDownloadModel = model;
                mDownloadTask.execute(mDownloadModel.getDownloadUrl());

            }
        }

        /**
         * 提供给外部的,暂停下载
         */

        public void pauseDownload() {
            if (mDownloadTask != null) {
                mDownloadTask.pauseDownload();
            }
        }

        /**
         * 提供给外部的,取消下载
         */

        public void cancelDownload() {
            if (mDownloadTask != null) {
                mDownloadTask.cancelDownload();
            }
        }

    }


    @Override
    public void onDestroy() {
        //当service被杀死的时候,将下载失败列表保存到SharedPreferences
        if (mNetWorkDownloadFailedList.size() != 0) {
            MusicUtil.saveToJson(Constant.SP_NAME_NETWORT_FAILED, Constant.KEY_NETWORT_FAILED, mNetWorkDownloadFailedList);
        }
        if (mResourceDownloadFailedList.size() != 0) {
            MusicUtil.saveToJson(Constant.SP_NAME_RESOURCE_FAILED, Constant.KEY_RESOURCE_FAILED, mResourceDownloadFailedList);
        }
        super.onDestroy();
    }
}
