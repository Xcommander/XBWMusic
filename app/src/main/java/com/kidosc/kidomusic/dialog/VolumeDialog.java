package com.kidosc.kidomusic.dialog;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.kidosc.kidomusic.R;
import com.kidosc.kidomusic.util.MusicUtil;

/**
 * Des:由于PopWindow不能拦截back键，所以改成dialog显示
 * Created by jason.xu on 2018/5/8.
 */
@SuppressLint("ValidFragment")
public class VolumeDialog extends DialogFragment {
    /**
     * 音量按钮
     */
    public ImageView mVolumePic;
    public ImageView mVolumeAdd;
    public ImageView mVolumeSub;

    /**
     * 监听函数
     */
    private DialogInterface.OnKeyListener mOnKeyListener;

    @SuppressLint("ValidFragment")
    public VolumeDialog(DialogInterface.OnKeyListener onKeyListener) {
        super();
        this.mOnKeyListener = onKeyListener;

    }

    /**
     * 设置样式
     */
    @Override
    public void onStart() {
        super.onStart();
        //设置大小以及禁止消失
        getDialog().getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.7f), (int) (getResources().getDisplayMetrics().heightPixels * 0.41f));
        getDialog().setCancelable(false);
        getDialog().setCanceledOnTouchOutside(false);


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //控件显示
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.gravity = Gravity.LEFT | Gravity.TOP;
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setAttributes(params);
        //设置透明背景
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        View view = inflater.inflate(R.layout.music_volume, container);
        AudioManager audioManager=(AudioManager)getContext().getSystemService(Context.AUDIO_SERVICE);
        mVolumePic = view.findViewById(R.id.vol_img);
        mVolumeAdd = view.findViewById(R.id.vol_add);
        mVolumeSub = view.findViewById(R.id.vol_sub);
        int vol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mVolumePic.setImageDrawable(MusicUtil.getVolumePic(vol));
        //设置坐标位置
        WindowManager.LayoutParams layoutParams = getDialog().getWindow().getAttributes();
        layoutParams.x = 53;
        layoutParams.y = 95;
        getDialog().getWindow().setAttributes(layoutParams);
        getDialog().setOnKeyListener(mOnKeyListener);
        return view;
    }

}
