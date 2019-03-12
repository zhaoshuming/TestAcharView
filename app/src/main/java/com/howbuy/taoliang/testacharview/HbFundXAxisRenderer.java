package com.howbuy.taoliang.testacharview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.renderer.XAxisRenderer;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

/**
 * Created by tao.liang on 2016/11/17.
 * 在线线性图的时候, 需要自定义灰白相间背景样式,重写了renderGridLines
 * 在绘制x轴文本的时候 最后一个label超出了边界,所以重写了drawlabels
 */

public class HbFundXAxisRenderer extends XAxisRenderer {

    private boolean mCustomGridBgStyle;
    /**
     * 需要绘制灰白总条数(默认7个)
     */
    private int mCeilCount = 7;

    public HbFundXAxisRenderer(ViewPortHandler viewPortHandler, XAxis xAxis, Transformer trans,
                               boolean customGridBgStyle, int ceilCount) {
        super(viewPortHandler, xAxis, trans);
        this.mCustomGridBgStyle = customGridBgStyle;
        this.mCeilCount = ceilCount;
    }

    @Override
    protected void drawLabels(Canvas c, float pos, MPPointF anchor) {
//         super.drawLabels();
        final float labelRotationAngleDegrees = mXAxis.getLabelRotationAngle();
        boolean centeringEnabled = mXAxis.isCenterAxisLabelsEnabled();

        float[] positions = new float[mXAxis.mEntryCount * 2];

        for (int i = 0; i < positions.length; i += 2) {

            // only fill x values
            if (centeringEnabled) {
                positions[i] = mXAxis.mCenteredEntries[i / 2];
            } else {
                positions[i] = mXAxis.mEntries[i / 2];
            }
        }

        mTrans.pointValuesToPixel(positions);

        for (int i = 0; i < positions.length; i += 2) {

            float x = positions[i];

            if (mViewPortHandler.isInBoundsX(x)) {

                String label = mXAxis.getValueFormatter().getFormattedValue(mXAxis.mEntries[i / 2], mXAxis);

                    if (mXAxis.isAvoidFirstLastClippingEnabled()) {

                        if (i == 0) {
                            // avoid clipping of the first
                            float width = Utils.calcTextWidth(mAxisLabelPaint, label);
                            x += width / 2;
                        } else if (i == positions.length - 2) {
                            /**
                             * 设置x轴上的显示的lables,需要最大值*2-1 得到最后一个lable的位置
                             */
                            // avoid clipping of the last
                            float width = Utils.calcTextWidth(mAxisLabelPaint, label);
                            x -= width / 2;
                        }
                    }

                    drawLabel(c, label, x, pos, anchor, labelRotationAngleDegrees);


            }
        }
    }

    @Override
    public void renderGridLines(Canvas c) {
        if (mCustomGridBgStyle) {
            drawCustomGridRender(c);
        } else {
            super.renderGridLines(c);
        }

    }

    private static final int[] GRIDE_BG_COLOR = new int[]{0xffF7F7F7, 0xffF2F2F2};
    private RectF mGrideRectF = new RectF();
    private Paint mPaintBoard = new Paint();

    private void drawCustomGridRender(Canvas c) {

        int clipRestoreCount = c.save();
        c.clipRect(getGridClippingRect());
        if (mRenderGridLinesBuffer.length != mAxis.mEntryCount * 2) {
            mRenderGridLinesBuffer = new float[mXAxis.mEntryCount * 2];
        }
        float[] positions = mRenderGridLinesBuffer;
        for (int i = 0; i < positions.length; i += 2) {
            positions[i] = mXAxis.mEntries[i / 2];
            positions[i + 1] = mXAxis.mEntries[i / 2];
        }
        mTrans.pointValuesToPixel(positions);
        setupGridPaint();

        mGridPaint.setColor(mXAxis.getGridColor());
        mGridPaint.setStrokeWidth(mXAxis.getGridLineWidth());
        mGridPaint.setStyle(Paint.Style.FILL);

        float[] ceilLeft = new float[]{0f, 0f};
        float[] ceilRight = new float[]{0f, 0f};

        /**
         * 计算视图的宽度
         */
        float leftPos = mViewPortHandler.contentLeft();
        float rightPos = mViewPortHandler.contentRight();

        float perCeilWidth = (rightPos - leftPos) / mCeilCount;
        for (int i = 0; i < mCeilCount; i++) {
            //自定义画网格底色(灰白相间)
            //控制相邻两个不同的底色
            if (i % 2 == 0) {
                mGridPaint.setColor(GRIDE_BG_COLOR[0]);
            } else {
                mGridPaint.setColor(GRIDE_BG_COLOR[1]);
            }

            ceilLeft[0] = mViewPortHandler.contentLeft() + i * perCeilWidth;
            ceilRight[0] = mViewPortHandler.contentLeft() + (i + 1) * perCeilWidth;

            mGrideRectF.set(ceilLeft[0], mViewPortHandler.offsetTop(), ceilRight[0], mViewPortHandler.contentBottom());

            c.drawRect(mGrideRectF, mGridPaint);

        }
        c.restoreToCount(clipRestoreCount);

        //画左右两的边框线(因为设置了一个y轴不好控制显示,所以这里来办)
        c.drawLine(mViewPortHandler.contentLeft(), mViewPortHandler.offsetTop(), mViewPortHandler.contentLeft(),
                mViewPortHandler.contentBottom(), mPaintBoard);

        c.drawLine(mViewPortHandler.contentRight() - 2, mViewPortHandler.offsetTop(), mViewPortHandler.contentRight() - 2,
                mViewPortHandler.contentBottom(), mPaintBoard);

        //这里控制顶部边框线
//        c.drawLine(mViewPortHandler.offsetLeft(), mViewPortHandler.offsetTop(), mViewPortHandler.contentRight(),
//                mViewPortHandler.offsetTop(), mPaintBoard);
    }
}
