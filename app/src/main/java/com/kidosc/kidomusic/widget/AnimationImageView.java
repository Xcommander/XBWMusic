package com.kidosc.kidomusic.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;

/**
 * Created by jason.xu on 2018/6/13.
 * des:自定义播放的旋转动画,也可通过handler + invalidate来实现
 */
public class AnimationImageView extends AppCompatImageView {
    /**
     * 动画变量对象
     */
    private ObjectAnimator mObjectAnimator;

    /**
     * 枚举类:当前的状态
     */
    public enum State {
        STATE_PLAYING,
        STATE_PAUSE,
        STATE_STOP

    }

    public AnimationImageView(Context context) {
        super(context);
        init();
    }

    public AnimationImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnimationImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化相关动画
     */
    private void init() {
        mObjectAnimator = ObjectAnimator.ofFloat(this, "rotation", 0f, 360f);
        mObjectAnimator.setDuration(4500);
        mObjectAnimator.setInterpolator(new LinearInterpolator());
        mObjectAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        mObjectAnimator.setRepeatMode(ObjectAnimator.RESTART);
    }

    /**
     * 根据当前状态，来决定动画的播放
     *
     * @param mState 状态值
     */
    public void playMusic(State mState) {
        if (mState == State.STATE_STOP) {
            mObjectAnimator.end();
        } else if (mState == State.STATE_PAUSE) {
            mObjectAnimator.pause();
        } else if (mState == State.STATE_PLAYING) {
            if (mObjectAnimator.isPaused()) {
                mObjectAnimator.resume();
            } else {
                mObjectAnimator.start();
            }
        }
    }

}
