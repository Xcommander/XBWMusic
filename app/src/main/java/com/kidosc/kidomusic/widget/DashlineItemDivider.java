package com.kidosc.kidomusic.widget;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by jason.xu on 2018/4/26.
 */
public class DashlineItemDivider extends RecyclerView.ItemDecoration {
    /**
     * 自定义{@link RecyclerView.ItemDecoration},绘制下划线的分割线
     * @param c
     * @param parent
     * @param state
     */

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            //以下计算主要用来确定绘制的位置
            final int top = child.getBottom() + params.bottomMargin;
            //绘制虚线
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.parseColor("#959595"));
            Path path = new Path();
            path.moveTo(left, top);
            path.lineTo(right, top);
            PathEffect effects = new DashPathEffect(new float[]{11, 11, 11, 11}, 15);
            paint.setPathEffect(effects);
            c.drawPath(path, paint);
        }
        super.onDraw(c, parent, state);
    }
}