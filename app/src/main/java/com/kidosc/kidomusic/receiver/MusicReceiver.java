package com.kidosc.kidomusic.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.kidosc.kidomusic.download.DownloadUpdate;
import com.kidosc.kidomusic.gson.MusicInfo;
import com.kidosc.kidomusic.model.DownloadModel;
import com.kidosc.kidomusic.util.Constant;
import com.kidosc.kidomusic.util.MusicUtil;

import java.io.File;

import static com.kidosc.kidomusic.activity.MyApplication.getDownloadUtil;


/**
 * Created by jason.xu on 2018/5/9.
 */

public class MusicReceiver extends BroadcastReceiver {
    private Context mContext;

    private DownloadUpdate mDownloadUpdate = new DownloadUpdate() {
        @Override
        public void update() {
            //发送广播，通知发生了改变，主界面发生了变化
            Log.e("xulinchao", "update: MusicReceiver ");
            if (mContext != null) {
                Intent intent = new Intent();
                intent.setAction(Constant.ACTION_UPDATE_LIST);
                mContext.sendBroadcast(intent);
            }


        }
    };

    @Override
    public void onReceive(final Context context, final Intent intent) {
        mContext = context;
        if (Constant.ACTION_DOWNLOAD.equals(intent.getAction())) {
            File file = new File(Constant.MUSIC_DIR);
            if (!file.exists()) {
                file.mkdirs();
            }
            //下载广播
            Log.e("xulinchao", "onReceive: ok");
            int type = intent.getIntExtra("type", -1);
            final String Oid = intent.getStringExtra("Oid");
            final String content = intent.getStringExtra("content");
            if (type == 3) {
                MusicUtil.handleThread(new Runnable() {
                    @Override
                    public void run() {
                        MusicInfo musicInfo = MusicUtil.handleMusicResponse(content);

                        /**
                         *首次获取的保护
                         */
                        if(getDownloadUtil().getmDownloadBinder()==null) {
                            int i =0;
                            while(getDownloadUtil().getmDownloadBinder()==null){
                                if(i<15){
                                    SystemClock.sleep(100);
                                    i++;
                                }else{
                                    Log.d("MediaPlayerService", "get player error ");
                                    return;
                                }

                            }

                        }
                        if (getDownloadUtil().getmDownloadBinder().getmDownloadUpdate() == null) {
                            getDownloadUtil().getmDownloadBinder().setmDownloadUpdate(mDownloadUpdate);
                        }
                        DownloadModel model=new DownloadModel();
                        model.setDownloadUrl(Constant.MUSIC_ONLINE_URL
                                + musicInfo.path.split("/")[1]);
                        model.setOid(Oid);
                        model.setLrcUrl(Constant.MUSIC_ONLINE_URL
                                + (musicInfo.path.split("/")[1]).replace(".mp3",".lrc"));
                        getDownloadUtil().getmDownloadBinder().downloadTask(model);
                    }
                });

            }
        }

    }
}
