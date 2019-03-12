package com.howbuy.taoliang.testacharview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * 三角形（默认黑色）
 */
public class PiggyHomeSwipeForChart extends View {

    private String TAG = "PiggyLoginProgress";
    private Paint mPaint;
    private Paint mPaint1;
    private int mColor;
    private Path mPath;

    private float mPos;
    private RectF rectF;

    public int getColor() {
        if (mColor == 0) {
            mColor = 0xffFFAF32;
        }
        return mColor;
    }


    public void setColorResource(int colorResID) {
        this.mColor = getResources().getColor(colorResID);
        mPaint.setColor(getColor());
        mPaint1.setColor(getColor());
        requestLayout();
    }

    public void setPosition(float pos) {
        setVisibility(VISIBLE);
        mPos = pos;
        requestLayout();
        invalidate();
    }

    public PiggyHomeSwipeForChart(Context context) {
        super(context);
        init();
    }

    public PiggyHomeSwipeForChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PiggyHomeSwipeForChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPath = new Path();
        mPaint = new Paint();
        mPaint1 = new Paint();
        mPaint.setStrokeWidth(3f);
        mPaint1.setStrokeWidth(3f);

        mPaint.setColor(getColor());
        mPaint1.setColor(getColor());
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint1.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPaint1.setAntiAlias(true);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.d(TAG, "onLayout() called with: " + "changed = [" + changed + "], left = [" + left + "], top = [" + top + "], right = [" + right + "], bottom = [" + bottom + "]");


        //尖角下的竖线
        rectF = new RectF(mPos - 1.5f, 20 + 0.5f, mPos + 1.5f, bottom);

        mPath.reset();
        mPath.moveTo(left - 15, 0);
        mPath.quadTo(left - 15, 0, mPos - 15, 0);
        mPath.quadTo(mPos - 15, 0, mPos, 20);
        mPath.quadTo(mPos, 20, mPos + 15, 0);
        mPath.quadTo(mPos + 15, 0, right, 0);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(mPath, mPaint);
        canvas.drawRect(rectF, mPaint1);
    }


}
