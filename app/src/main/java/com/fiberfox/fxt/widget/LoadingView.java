package com.fiberfox.fxt.widget;


import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class LoadingView extends View {
    private Paint paint;             //画笔
    private float radius;            //半径
    private float radiusOffset;      //半径的偏移量
    //不是固定不变的，当width为30dp时，它为2dp，当宽度变大，这个也会相应的变大
    private float stokeWidth = 2f;
    private ArgbEvaluator argbEvaluator = new ArgbEvaluator();             //创建渐变色彩
    private int startColor = Color.parseColor("#f1f1f1");       //起始颜色
    private int endColor = Color.parseColor("#111111");         //结束颜色
    int lineCount = 12;                                                    // 共12条线
    float avgAngle = 360f / lineCount;                                     //每条线的角度
    int time = 0;                                                          // 重复次数
    float centerX, centerY;                                                // 中心x，y

    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);  //设置抗锯齿的画笔
        stokeWidth =dp2px(context, stokeWidth);    //将dp值转换为像素值
        paint.setStrokeWidth(stokeWidth);          //设置画笔的粗细
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        radius = getMeasuredWidth() / 2;
        radiusOffset = radius / 2.5f;

        centerX = getMeasuredWidth() / 2;
        centerY = getMeasuredHeight() / 2;

        stokeWidth *= getMeasuredWidth()*1f / dp2px(getContext(), 30);
        paint.setStrokeWidth(stokeWidth);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        // back 2 3 4 5
        // 2 3 4 5 back
        // 3 4 5 back 2
        // ...
        for (int i = lineCount-1; i >=0 ; i--) {
            int temp = Math.abs(i + time) % lineCount;
            float fraction = (temp+1) * 1f / lineCount;
            int color = (int) argbEvaluator.evaluate(fraction, startColor, endColor);
            paint.setColor(color);

            float startX = centerX + radiusOffset;
            float endX = startX + radius / 3f;
            canvas.drawLine(startX, centerY, endX, centerY, paint);
            // 线的两端画个点，看着圆滑
            canvas.drawCircle(startX, centerY,stokeWidth/2, paint);
            canvas.drawCircle(endX, centerY,stokeWidth/2, paint);
            canvas.rotate(avgAngle, centerX, centerY);
        }
        postDelayed(increaseTask, 80);
    }

    private Runnable increaseTask = new Runnable() {
        @Override
        public void run() {
            time ++;
            invalidate();
        }
    };

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(increaseTask);
    }

public int dp2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}