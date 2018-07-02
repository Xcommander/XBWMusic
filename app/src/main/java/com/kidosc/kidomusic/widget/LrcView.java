package com.kidosc.kidomusic.widget;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import com.kidosc.kidomusic.R;

/**
 * Created by jason.xu on 2018/6/15.
 */
public class LrcView extends View {
    private static final int SCROLL_TIME = 500;
    private static final String DEFAULT_TEXT = "没有歌词显示!";

    /**
     * 存放歌词
     */
    private List<String> mLrcs = new ArrayList<>();
    /**
     * 存放时间
     */

    private List<Long> mTimes = new ArrayList<>();

    /**
     * 保存下一句开始的时间
     */
    private long mNextTime = 0L;

    /**
     * view的宽度
     */
    private int mViewWidth;
    /**
     * lrc界面的高度
     */
    private int mLrcHeight;
    /**
     * 多少行
     */
    private int mRows;
    /**
     * 当前行
     */
    private int mCurrentLine = 0;
    /**
     * y上的偏移
     */
    private int mOffsetY;
    /**
     * 最大滑动距离=一行歌词高度+歌词间距
     */
    private int mMaxScroll;

    /**
     * 字体大小
     */
    private float mTextSize;
    /**
     * 行间距
     */
    private float mDividerHeight;

    private Rect mTextBounds;
    /**
     * 常规的字体
     */
    private Paint mNormalPaint;
    /**
     * 当前歌词的大小
     */
    private Paint mCurrentPaint;

    private Bitmap mBackground;

    private Scroller mScroller;

    /**
     * 歌曲总时间
     */
    private long mTotalTime;

    /**
     * 控制歌词水平滚动的属性动画
     */
    private ValueAnimator mAnimator;

    /**
     * 歌词过长的时候x轴绘制坐标
     */
    private float mCurTextXForHighLightLrc;

    public static Pattern mPattern = Pattern.compile("\\[\\d.+\\].+");

    public LrcView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LrcView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new Scroller(context, new LinearInterpolator());
        inflateAttributes(attrs);
    }

    /**
     * 初始化操作
     *
     * @param attrs
     */
    private void inflateAttributes(AttributeSet attrs) {
        // <begin>
        // 解析自定义属性
        TypedArray ta = getContext().obtainStyledAttributes(attrs,
                R.styleable.Lrc);
        mTextSize = ta.getDimension(R.styleable.Lrc_textSize, 50.0f);
        mRows = ta.getInteger(R.styleable.Lrc_rows, 5);
        mDividerHeight = ta.getDimension(R.styleable.Lrc_dividerHeight, 0.0f);

        int normalTextColor = ta.getColor(R.styleable.Lrc_normalTextColor,
                0xffffffff);
        int currentTextColor = Color.parseColor("#f29112");
        ta.recycle();
        // </end>

        // 计算lrc面板的高度
        mLrcHeight = (int) (mTextSize + mDividerHeight) * mRows + 5;

        mNormalPaint = new Paint();
        mCurrentPaint = new Paint();

        // 初始化paint
        mNormalPaint.setTextSize(mTextSize);
        mNormalPaint.setColor(normalTextColor);
        mNormalPaint.setAntiAlias(true);
        mCurrentPaint.setTextSize(mTextSize);
        mCurrentPaint.setColor(currentTextColor);
        mCurrentPaint.setAntiAlias(true);

        mTextBounds = new Rect();
        mCurrentPaint.getTextBounds(DEFAULT_TEXT, 0, DEFAULT_TEXT.length(), mTextBounds);
        mMaxScroll = (int) (mTextBounds.height() + mDividerHeight);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 重新设置view的高度
        mLrcHeight = getMeasuredHeight();
        mViewWidth = getMeasuredWidth();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 获取view宽度
        mViewWidth = getMeasuredWidth();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (mBackground != null) {
            canvas.drawBitmap(Bitmap.createScaledBitmap(
                    mBackground, mViewWidth, mLrcHeight, true),
                    new Matrix(), null);
        }

        float centerY = (getMeasuredHeight() +
                mTextBounds.height() - mDividerHeight) / 2;
        if (mLrcs.isEmpty() || mTimes.isEmpty()) {
            mCurrentPaint.setColor(0xffffffff);
            mCurrentPaint.setTextSize(25f);
            canvas.drawText(DEFAULT_TEXT,
                    (mViewWidth - mCurrentPaint.measureText(DEFAULT_TEXT)) / 2,
                    centerY, mCurrentPaint);
            mCurrentPaint.setTextSize(mTextSize);
            mCurrentPaint.setColor(Color.parseColor("#f29112"));
            return;
        }
        if (mNextTime <= mTimes.get(1)) {
            mOffsetY = 0;
        }
        float offsetY = mTextBounds.height() + mDividerHeight;
        String currentLrc = mLrcs.get(mCurrentLine);
        float currentX = (mViewWidth - mCurrentPaint.measureText(currentLrc)) / 2;
        currentX = Math.max(currentX, 0);
        if (mCurrentPaint.measureText(currentLrc) > mViewWidth) {
            canvas.drawText(currentLrc, mCurTextXForHighLightLrc, centerY - mOffsetY, mCurrentPaint);
        } else {
            canvas.drawText(currentLrc, currentX, centerY - mOffsetY, mCurrentPaint);
        }
        int firstLine = mCurrentLine - mRows / 2;
        firstLine = firstLine <= 0 ? 0 : firstLine;
        int lastLine = mCurrentLine + mRows / 2 + 2;
        lastLine = lastLine >= mLrcs.size() - 1 ? mLrcs.size() - 1 : lastLine;
        // 画当前行上面的
        for (int i = mCurrentLine - 1, j = 1; i >= firstLine; i--, j++) {
            String lrc = mLrcs.get(i);
            float x = (mViewWidth - mNormalPaint.measureText(lrc)) / 2;
            x = Math.max(x, 0);
            canvas.drawText(lrc, x, centerY - j * offsetY - mOffsetY, mNormalPaint);
        }

        // 画当前行下面的
        for (int i = mCurrentLine + 1, j = 1; i <= lastLine; i++, j++) {

            String lrc = mLrcs.get(i);
            float x = (mViewWidth - mNormalPaint.measureText(lrc)) / 2;
            x = Math.max(x, 0);
            canvas.drawText(lrc, x, centerY + j * offsetY - mOffsetY, mNormalPaint);


        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mOffsetY = mScroller.getCurrY();
            if (mScroller.isFinished()) {
                int cur = mScroller.getCurrX();
                mCurrentLine = cur <= 1 ? 0 : cur - 1;
                mOffsetY = 0;
                /**
                 * 判断歌词长度是否过长，过长则滚动显示，还有一种是分行显示，但是有些问题，后续再考虑
                 */
                if (mViewWidth > 0 && mCurrentPaint.measureText(mLrcs.get(mCurrentLine)) > mViewWidth) {
                    if (mCurrentLine < mLrcs.size() - 1) {
                        startScrollLrc(mViewWidth - mCurrentPaint.measureText(mLrcs.get(mCurrentLine)), (long) ((mTimes.get(mCurrentLine + 1) - mTimes.get(mCurrentLine)) * 0.6));
                    } else if (mCurrentLine == mLrcs.size() - 1) {
                        startScrollLrc(mViewWidth - mCurrentPaint.measureText(mLrcs.get(mCurrentLine)), (long) ((mTotalTime - mTimes.get(mCurrentLine)) * 0.6));
                    }
                }
            }
            postInvalidate();
        }
    }

    /**
     * 解析时间
     *
     * @param time
     * @return
     */
    private Long parseTime(String time) {
        // 03:02.12
        String[] min = time.split(":");
        String[] sec = min[1].split("\\.");

        long minInt = Long.parseLong(min[0].replaceAll("\\D+", "")
                .replaceAll("\r", "").replaceAll("\n", "").trim());
        long secInt = Long.parseLong(sec[0].replaceAll("\\D+", "")
                .replaceAll("\r", "").replaceAll("\n", "").trim());
        long milInt = Long.parseLong(sec[1].replaceAll("\\D+", "")
                .replaceAll("\r", "").replaceAll("\n", "").trim());

        return minInt * 60 * 1000 + secInt * 1000 + milInt * 10;
    }

    /**
     * 解析每行
     *
     * @param line
     * @return
     */
    @Nullable
    private String[] parseLine(String line) {

        Matcher matcher = mPattern.matcher(line);
        // 如果形如：[xxx]后面啥也没有的，则return空
        if (!matcher.matches()) {
            System.out.println("throws " + line);
            return null;
        }

        line = line.replaceAll("\\[", "");
        String[] result = line.split("\\]");
        result[0] = String.valueOf(parseTime(result[0]));

        return result;
    }

    /**
     * 传入当前播放时间
     *
     * @param time
     */
    public synchronized void changeCurrent(long time) {
        // 如果当前时间小于下一句开始的时间
        // 直接return
        if (mNextTime > time) {
            return;
        }
        // 每次进来都遍历存放的时间
        for (int i = 0; i < mTimes.size(); i++) {
            // 发现这个时间大于传进来的时间
            // 那么现在就应该显示这个时间前面的对应的那一行
            // 每次都重新显示，是不是要判断：现在正在显示就不刷新了
            if (mTimes.get(i) > time) {
                mNextTime = mTimes.get(i);
                mScroller.abortAnimation();
                mScroller.startScroll(i, 0, 0, mMaxScroll, SCROLL_TIME);
                postInvalidate();
                return;
            }
        }
    }

    public void onDrag(int progress) {
        stopScrollLrc();
        for (int i = 0; i < mTimes.size(); i++) {
            if (Integer.parseInt(mTimes.get(i).toString()) > progress) {
                mNextTime = i == 0 ? 0 : mTimes.get(i - 1);
                return;
            }
        }
    }

    /**
     * 设置lrc的路径
     *
     * @param path
     */
    public void setLrcPath(String path) {
        reset();
        File file = new File(path);
        if (!file.exists()) {
            postInvalidate();
            return;
        }

        FileInputStream fis = null;
        BufferedInputStream bis = null;
        BufferedReader reader = null;
        try {
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            bis.mark(4);
            byte[] first3bytes = new byte[3];
            //找到文档的前三个字节并自动判断文档类型。
            bis.read(first3bytes);
            bis.reset();
            if (first3bytes[0] == (byte) 0xEF && first3bytes[1] == (byte) 0xBB
                    && first3bytes[2] == (byte) 0xBF) {

                reader = new BufferedReader(new InputStreamReader(bis, "utf-8"));

            } else if (first3bytes[0] == (byte) 0xFF
                    && first3bytes[1] == (byte) 0xFE) {

                reader = new BufferedReader(
                        new InputStreamReader(bis, "unicode"));
            } else if (first3bytes[0] == (byte) 0xFE
                    && first3bytes[1] == (byte) 0xFF) {

                reader = new BufferedReader(new InputStreamReader(bis,
                        "utf-16be"));
            } else if (first3bytes[0] == (byte) 0xFF
                    && first3bytes[1] == (byte) 0xFF) {

                reader = new BufferedReader(new InputStreamReader(bis,
                        "utf-16le"));
            } else {

                reader = new BufferedReader(new InputStreamReader(bis, "GBK"));
            }

            String line;
            String[] arr;
            while (null != (line = reader.readLine())) {
                arr = parseLine(line);
                if (arr == null) {
                    continue;
                }

                // 如果解析出来只有一个
                if (arr.length == 1) {
                    String last = mLrcs.remove(mLrcs.size() - 1);
                    mLrcs.add(last + arr[0]);
                    continue;
                }
                mTimes.add(Long.parseLong(arr[0]));
                mLrcs.add(arr[1]);
            }
            if (mTimes.size() > 0) {
                mTimes.add(2500000L);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void reset() {
        mLrcs.clear();
        mTimes.clear();
        mCurrentLine = 0;
        mNextTime = 0L;
    }

    /**
     * 是否设置歌词
     *
     * @return true 设置,false 没设置
     */
    public boolean hasLrc() {
        return mLrcs != null && !mLrcs.isEmpty();
    }

    /**
     * 设置背景图片
     *
     * @param bmp
     */
    public void setBackground(Bitmap bmp) {
        mBackground = bmp;
    }


    /************************************************歌词滚动***************************************************************/
    public void setmTotalTime(long mTotalTime) {
        this.mTotalTime = mTotalTime;
    }
    /**
     * 开始水平滚动歌词
     *
     * @param endX     歌词第一个字的最终的x坐标
     * @param duration 滚动的持续时间
     */
    private void startScrollLrc(float endX, long duration) {
        if (mAnimator == null) {
            mAnimator = ValueAnimator.ofFloat(0, endX);
            mAnimator.addUpdateListener(updateListener);
        } else {
            mCurTextXForHighLightLrc=0;
            mAnimator.cancel();
            mAnimator.setFloatValues(0, endX);
        }
        mAnimator.setDuration(duration);
        mAnimator.setStartDelay((long) (duration * 0.2));
        mAnimator.start();
    }

    /**
     * 停止歌词的滚动
     */
    private void stopScrollLrc() {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
        mCurTextXForHighLightLrc = 0;
    }


    /***
     * 监听属性动画的数值值的改变
     */
    ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            mCurTextXForHighLightLrc = (Float) animation.getAnimatedValue();
            postInvalidate();
        }
    };
}

