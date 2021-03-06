package com.kidosc.kidomusic.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import com.kidosc.kidomusic.R;
import com.kidosc.kidomusic.adapter.AdapterOnClickListener;
import com.kidosc.kidomusic.adapter.SequenceSelectAdapter;
import com.kidosc.kidomusic.util.Constant;

/**
 * Created by jason.xu on 2018/4/28.
 */
public class MyPopWindow extends PopupWindow {
    /**
     * mWindowType:PopWindow的类型
     */
    private String mWindowType;
    private PopWinodListener mPopWinodListener;
    private SequenceSelectAdapter mSequenceSelectAdapter;

    /**
     * 自定义PopWindow，根据传递的type，显示不同的style的PopWindow
     *
     * @param context
     * @param type
     */
    public MyPopWindow(Context context, String type) {
        super(context);
        this.mWindowType = type;
        View contentView = null;
        if (Constant.POP_WINDOW_SEQUENCE.equals(mWindowType)) {
            contentView = LayoutInflater.from(context).inflate(R.layout.select_sequence, null);
            setContentView(contentView);
            setHeight((int) ((context.getResources().getDisplayMetrics().heightPixels) * 0.68f));
            setWidth((int) (context.getResources().getDisplayMetrics().widthPixels * 0.55f));
            RecyclerView recyclerView = contentView.findViewById(R.id.sequence_list);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
            mSequenceSelectAdapter = new SequenceSelectAdapter(context, new AdapterOnClickListener() {
                @Override
                public void onItemOnClick() {
                    if(mPopWinodListener!=null){
                        mPopWinodListener.onClick();
                    }
                    Log.e("xulinchao", "onItemOnClick: dissmiss" );
                    dismiss();

                }
            });
            recyclerView.setAdapter(mSequenceSelectAdapter);
            recyclerView.setLayoutManager(layoutManager);
        } else if (Constant.POP_WINDOW_VOLUME.equals(mWindowType)) {
            contentView = LayoutInflater.from(context).inflate(R.layout.music_volume, null);
            setContentView(contentView);
            setHeight((int) ((context.getResources().getDisplayMetrics().heightPixels) * 0.41f));
            setWidth((int) (context.getResources().getDisplayMetrics().widthPixels * 0.7f));

        }
        setBackgroundDrawable(new ColorDrawable(0x00000000));
        setFocusable(true);
        setOutsideTouchable(true);
        update();
    }

    /**
     * 显示PopWindow
     *
     * @param parent 根据parent控件来显示这个PopWindow的位置
     */

    public void showPopupWindow(View parent) {
        if (!isShowing()) {
            if (Constant.POP_WINDOW_SEQUENCE.equals(mWindowType)) {
                mSequenceSelectAdapter.notifyDataSetChanged();
                showAtLocation(parent, Gravity.CENTER, 68, -2);
                showAsDropDown(parent);
            } else if (Constant.POP_WINDOW_VOLUME.equals(mWindowType)) {
                showAtLocation(parent, Gravity.CENTER, 5, 0);
                showAsDropDown(parent);
            }

        } else {
            dismiss();
        }
    }

    public void setmPopWinodListener(PopWinodListener mPopWinodListener) {
        this.mPopWinodListener = mPopWinodListener;
    }

    public interface PopWinodListener{
        /**
         * PopWindow和activity之间的通信
         */
        void onClick();
    }


}
