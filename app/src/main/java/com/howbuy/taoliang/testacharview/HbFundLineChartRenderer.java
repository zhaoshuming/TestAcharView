package com.howbuy.taoliang.testacharview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineScatterCandleRadarDataSet;
import com.github.mikephil.charting.renderer.LineChartRenderer;
import com.github.mikephil.charting.utils.MPPointD;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tao.liang on 2016/9/28.
 * 画阴影和高亮线, 双手指效果
 */

public class HbFundLineChartRenderer extends LineChartRenderer {
    private final HbFundLineChartBase mHbBarLineChartBase;
    private Paint mShadePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    ArrayList<Float> mTouchPointList = new ArrayList<>();
    RectF rectDoubleShade = new RectF();


    private float hLength = Utils.convertDpToPixel(15f);//横线长15dp
    private float vLength = Utils.convertDpToPixel(10f);//竖线长10dp
    private float rect= Utils.convertDpToPixel(8f);//矩形高低差/2
    private float textX= Utils.convertDpToPixel(2f);//文本x坐标偏移量
    private float textY= Utils.convertDpToPixel(3f);//文本y偏移量
    private boolean isShowHLPoint = true;//是否显示最高点和最低点标识,默认显示
    private float textSixe = 10f;//文字大小

    private Context mContext;
    private int mWidth;//屏幕宽度,在构造方法中传进来赋值
    private float hViewLength = Utils.convertDpToPixel(30f);//vie宽30dp
    private float vViewLength = Utils.convertDpToPixel(20f);//view高20dp
    private float viewRect= Utils.convertDpToPixel(4f);//矩形高低差

    /**
     * path that is used for drawing highlight-lines (drawLines(...) cannot be used because of dashes)
     */
    private Path mHighlightLinePath = new Path();

    public HbFundLineChartRenderer(LineDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler, int width, Context context) {
        super(chart, animator, viewPortHandler);

        mContext = context;
        mWidth = width;
        mShadePaint.setColor(Color.parseColor("#2F4587F0"));
        mShadePaint.setStyle(Paint.Style.FILL);
        mShadePaint.setStrokeCap(Paint.Cap.ROUND);
        mShadePaint.setStrokeJoin(Paint.Join.ROUND);
        mDotPaint.setStyle(Paint.Style.FILL);
        mDotPaint.setStrokeCap(Paint.Cap.ROUND);
        mDotPaint.setStrokeJoin(Paint.Join.ROUND);
        mDotPaint.setColor(mHighlightPaint.getColor());
        mHbBarLineChartBase = (HbFundLineChartBase) mChart;
    }

    @Override
    public void drawHighlighted(Canvas c, Highlight[] indices) {
        LineData lineData = mChart.getLineData();
        mTouchPointList.clear();

        boolean canDrawShade = drawAreaNew(c, indices, lineData, mTouchPointList);
        if (canDrawShade) {
            drawShadeArea(c, indices, mTouchPointList);
        }
    }

    private void drawShadeArea(Canvas canvas, Highlight[] indices, List<Float> pointX) {
        if (indices != null) {
            if (indices.length == 1 && pointX.size() == 1) {
                rectDoubleShade.left = pointX.get(0);
                rectDoubleShade.right = rectDoubleShade.left;
            } else {
                rectDoubleShade.left = pointX.get(0);
                if (pointX.size() == 2) {
                    rectDoubleShade.right = pointX.get(1);
                } else {
                    rectDoubleShade.right = rectDoubleShade.left;
                }
            }
            rectDoubleShade.top = mViewPortHandler.contentTop();
            rectDoubleShade.bottom = mViewPortHandler.contentBottom();
            canvas.drawRect(rectDoubleShade, mShadePaint);
        }
    }

    @Override
    protected void drawHighlightLines(Canvas c, float x, float y, ILineScatterCandleRadarDataSet set) {
        mHighlightPaint.setColor(set.getHighLightColor());
        mHighlightPaint.setStrokeWidth(3.5f);

        PathEffect effects = new DashPathEffect(new float[]{5, 5, 5, 5}, 0);
        mHighlightPaint.setPathEffect(effects);

        // draw vertical highlight lines
        if (set.isVerticalHighlightIndicatorEnabled()) {

            // create vertical path
            mHighlightLinePath.reset();
            mHighlightLinePath.moveTo(x, mViewPortHandler.contentTop());
            mHighlightLinePath.lineTo(x, mViewPortHandler.contentBottom());

            c.drawPath(mHighlightLinePath, mHighlightPaint);
        }

        // draw horizontal highlight lines
        if (set.isHorizontalHighlightIndicatorEnabled()) {

            // create horizontal path
            mHighlightLinePath.reset();
            mHighlightLinePath.moveTo(mViewPortHandler.contentLeft(), y);
            mHighlightLinePath.lineTo(mViewPortHandler.contentRight(), y);

            c.drawPath(mHighlightLinePath, mHighlightPaint);
        }
    }

    /**
     * 这里API在3.0.3-beta版本上的代码,使用时,需要把注释的代码打开
     *
     * @param c
     * @param indices
     * @param lineData
     * @param pointX
     */
    private boolean drawAreaNew(Canvas c, Highlight[] indices, LineData lineData, ArrayList<Float> pointX) {
        if (indices == null) {
            return false;
        }
        boolean hasHight = false;
        for (Highlight high : indices) {


            ILineDataSet set = lineData.getDataSetByIndex(high.getDataSetIndex());

            if (set == null || !set.isHighlightEnabled())
                continue;

            Entry e = set.getEntryForXValue(high.getX(), high.getY());

            if (!isInBoundsX(e, set))
                continue;

            MPPointD pix = mChart.getTransformer(set.getAxisDependency()).getPixelForValues(e.getX(),
                    e.getY() * mAnimator.getPhaseY());

            high.setDraw((float) pix.x, (float) pix.y);

            // draw the lines (两端的线)
            drawHighlightLines(c, (float) pix.x, (float) pix.y, set);

            if (indices.length == 1 && (mHbBarLineChartBase != null && mHbBarLineChartBase.ismCustomGestureSingleTap())) {
                //如果图表中只有一个手指触摸,就显示小圆点,画笔使用和高亮线一样的颜色
                c.drawCircle((float) pix.x, (float) pix.y, set.getCircleRadius(), mDotPaint);
            }

            //把两端的点添加到集合中
            pointX.add((float) pix.x);
            hasHight = true;
        }
        return hasHight;
    }

//    private float[] mLineBuffer = new float[4];
//
//    /**
//     * Draws a normal line.
//     *
//     * @param c
//     * @param dataSet
//     */
//    protected void drawLinear(Canvas c, ILineDataSet dataSet) {
//
//        int entryCount = dataSet.getEntryCount();
//
//        final boolean isDrawSteppedEnabled = dataSet.isDrawSteppedEnabled();
//        final int pointsPerEntryPair = isDrawSteppedEnabled ? 4 : 2;
//
//        Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());
//
//        float phaseY = mAnimator.getPhaseY();
//
//        mRenderPaint.setStyle(Paint.Style.STROKE);
//
//        Canvas canvas = null;
//
//        // if the data-set is dashed, draw on bitmap-canvas
//        if (dataSet.isDashedLineEnabled()) {
//            canvas = mBitmapCanvas;
//        } else {
//            canvas = c;
//        }
//
//        mXBounds.set(mChart, dataSet);
//
//        // if drawing filled is enabled
//        if (dataSet.isDrawFilledEnabled() && entryCount > 0) {
//            drawLinearFill(c, dataSet, trans, mXBounds);
//        }
//
//        // more than 1 color
//        if (dataSet.getColors().size() > 1) {
//
//            if (mLineBuffer.length <= pointsPerEntryPair * 2)
//                mLineBuffer = new float[pointsPerEntryPair * 4];
//
//            for (int j = mXBounds.min; j <= mXBounds.range + mXBounds.min; j++) {
//
//                Entry e = dataSet.getEntryForIndex(j);
//                if (e == null) continue;
//
//                mLineBuffer[0] = e.getX();
//                mLineBuffer[1] = e.getY() * phaseY;
//
//                if (j < mXBounds.max) {
//
//                    e = dataSet.getEntryForIndex(j + 1);
//
//                    if (e == null) break;
//
//                    if (isDrawSteppedEnabled) {
//                        mLineBuffer[2] = e.getX();
//                        mLineBuffer[3] = mLineBuffer[1];
//                        mLineBuffer[4] = mLineBuffer[2];
//                        mLineBuffer[5] = mLineBuffer[3];
//                        mLineBuffer[6] = e.getX();
//                        mLineBuffer[7] = e.getY() * phaseY;
//                    } else {
//                        mLineBuffer[2] = e.getX();
//                        mLineBuffer[3] = e.getY() * phaseY;
//                    }
//
//                } else {
//                    mLineBuffer[2] = mLineBuffer[0];
//                    mLineBuffer[3] = mLineBuffer[1];
//                }
//
//                trans.pointValuesToPixel(mLineBuffer);
//
//                if (!mViewPortHandler.isInBoundsRight(mLineBuffer[0]))
//                    break;
//
//                // make sure the lines don't do shitty things outside
//                // bounds
//                if (!mViewPortHandler.isInBoundsLeft(mLineBuffer[2])
//                        || (!mViewPortHandler.isInBoundsTop(mLineBuffer[1]) && !mViewPortHandler
//                        .isInBoundsBottom(mLineBuffer[3])))
//                    continue;
//
//                // get the color that is set for this line-segment
//                mRenderPaint.setColor(dataSet.getColor(j));
//
//                canvas.drawLines(mLineBuffer, 0, pointsPerEntryPair * 2, mRenderPaint);
//            }
//
//        } else { // only one color per dataset
//
//            if (mLineBuffer.length < Math.max((entryCount) * pointsPerEntryPair, pointsPerEntryPair) * 2)
//                mLineBuffer = new float[Math.max((entryCount) * pointsPerEntryPair, pointsPerEntryPair) * 4];
//
//            Entry e1, e2;
//
//            e1 = dataSet.getEntryForIndex(mXBounds.min);
//
//            if (e1 != null) {
//
//                int j = 0;
//                for (int x = mXBounds.min; x <= mXBounds.range + mXBounds.min; x++) {
//
//                    e1 = dataSet.getEntryForIndex(x == 0 ? 0 : (x - 1));
//                    e2 = dataSet.getEntryForIndex(x);
//
//                    if (e1 == null || e2 == null) continue;
//
//                    mLineBuffer[j++] = e1.getX();
//                    mLineBuffer[j++] = e1.getY() * phaseY;
//
//                    if (isDrawSteppedEnabled) {
//                        mLineBuffer[j++] = e2.getX();
//                        mLineBuffer[j++] = e1.getY() * phaseY;
//                        mLineBuffer[j++] = e2.getX();
//                        mLineBuffer[j++] = e1.getY() * phaseY;
//                    }
//
//                    mLineBuffer[j++] = e2.getX();
//                    mLineBuffer[j++] = e2.getY() * phaseY;
//                }
//
//                if (j > 0) {
//                    trans.pointValuesToPixel(mLineBuffer);
//
//                    final int size = Math.max((mXBounds.range + 1) * pointsPerEntryPair, pointsPerEntryPair) * 2;
//
//                    mRenderPaint.setColor(dataSet.getColor());
//
//                    canvas.drawLines(mLineBuffer, 0, size, mRenderPaint);
//                }
//            }
//        }
//
//        mRenderPaint.setPathEffect(null);
//
//    }


//    protected void drawLinearFill(Canvas c, ILineDataSet dataSet, Transformer trans, XBounds bounds) {
//        final Path filled = mGenerateFilledPathBuffer;
//
//        final int startingIndex = bounds.min;
//        final int endingIndex = bounds.range + bounds.min;
//        final int indexInterval = 128;
//
//        int currentStartIndex = 0;
//        int currentEndIndex = indexInterval;
//        int iterations = 0;
//
//        // Doing this iteratively in order to avoid OutOfMemory errors that can happen on large bounds sets.
//        do {
//            currentStartIndex = startingIndex + (iterations * indexInterval);
//            currentEndIndex = currentStartIndex + indexInterval;
//            currentEndIndex = currentEndIndex > endingIndex ? endingIndex : currentEndIndex;
//
//            if (currentStartIndex <= currentEndIndex) {
//                generateFilledPath(dataSet, currentStartIndex, currentEndIndex, filled);
//
//                trans.pathValueToPixel(filled);
//
//                final Drawable drawable = dataSet.getFillDrawable();
//                if (drawable != null) {
//
//                    drawFilledPath(c, filled, drawable);
//                } else {
//
//                    drawFilledPath(c, filled, dataSet.getFillColor(), dataSet.getFillAlpha());
//                }
//            }
//
//            iterations++;
//
//        } while (currentStartIndex <= currentEndIndex);
//    }

//    /**
//     * Generates a path that is used for filled drawing.
//     *
//     * @param dataSet    The dataset from which to read the entries.
//     * @param startIndex The index from which to start reading the dataset
//     * @param endIndex   The index from which to stop reading the dataset
//     * @param outputPath The path object that will be assigned the chart data.
//     * @return
//     */
//    private void generateFilledPath(final ILineDataSet dataSet, final int startIndex, final int endIndex, final Path outputPath) {
//
//        final float fillMin = dataSet.getFillFormatter().getFillLinePosition(dataSet, mChart);
//        final float phaseY = mAnimator.getPhaseY();
//        final boolean isDrawSteppedEnabled = dataSet.getMode() == LineDataSet.Mode.STEPPED;
//
//        final Path filled = outputPath;
//        filled.reset();
//
//        final Entry entry = dataSet.getEntryForIndex(startIndex);
//
//        filled.moveTo(entry.getX(), fillMin);
//        filled.lineTo(entry.getX(), entry.getY() * phaseY);
//
//        // create a new path
//        Entry currentEntry = null;
//        Entry previousEntry = null;
//        for (int x = startIndex + 1; x <= endIndex; x++) {
//
//            currentEntry = dataSet.getEntryForIndex(x);
//
//            if (isDrawSteppedEnabled && previousEntry != null) {
//                filled.lineTo(currentEntry.getX(), previousEntry.getY() * phaseY);
//            }
//
//            filled.lineTo(currentEntry.getX(), currentEntry.getY() * phaseY);
//
//            previousEntry = currentEntry;
//        }
//
//        // close up
//        if (currentEntry != null) {
//            filled.lineTo(currentEntry.getX(), fillMin);
//        }
//
//        filled.close();
//    }

//    /**
//     * Draws the provided path in filled mode with the provided drawable.
//     *
//     * @param c
//     * @param filledPath
//     * @param drawable
//     */
//    protected void drawFilledPath(Canvas c, Path filledPath, Drawable drawable) {
//        super.drawFilledPath();
//        try {
//            c.save();
//            c.clipPath(filledPath);
//            drawable.setBounds((int) mViewPortHandler.contentLeft(),
//                    (int) mViewPortHandler.contentTop(),
//                    (int) mViewPortHandler.contentRight(),
//                    (int) mViewPortHandler.contentBottom());
//            drawable.draw(c);
//            c.restore();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    /**
//     * Draws the provided path in filled mode with the provided color and alpha.
//     * Special thanks to Angelo Suzuki (https://github.com/tinsukE) for this.
//     *
//     * @param c
//     * @param filledPath
//     * @param fillColor
//     * @param fillAlpha
//     */
//    protected void drawFilledPath(Canvas c, Path filledPath, int fillColor, int fillAlpha) {
//        int color = (fillAlpha << 24) | (fillColor & 0xffffff);
//
//        if (clipPathSupported()) {
//
//            int save = c.save();
//
//            c.clipPath(filledPath);
//
//            c.drawColor(color);
//            c.restoreToCount(save);
//        } else {
//
//            // save
//            Paint.Style previous = mRenderPaint.getStyle();
//            int previousColor = mRenderPaint.getColor();
//
//            // set
//            mRenderPaint.setStyle(Paint.Style.FILL);
//            mRenderPaint.setColor(color);
//
//            c.drawPath(filledPath, mRenderPaint);
//
//            // restore
//            mRenderPaint.setColor(previousColor);
//            mRenderPaint.setStyle(previous);
//        }
//
//    }

    private boolean clipPathSupported() {
        return Utils.getSDKInt() >= 18;
    }

    @Override
    public void drawData(Canvas c) {
        int width = (int) mViewPortHandler.getChartWidth();
        int height = (int) mViewPortHandler.getChartHeight();

        if (mDrawBitmap == null
                || (mDrawBitmap.get().getWidth() != width)
                || (mDrawBitmap.get().getHeight() != height)) {

            if (width > 0 && height > 0) {

                mDrawBitmap = new WeakReference<Bitmap>(Bitmap.createBitmap(width, height, mBitmapConfig));
                mBitmapCanvas = new Canvas(mDrawBitmap.get());
            } else
                return;
        }

        if (mDrawBitmap != null && mDrawBitmap.get() != null) {
            mDrawBitmap.get().eraseColor(Color.TRANSPARENT);
        }

        LineData lineData = mChart.getLineData();

        for (ILineDataSet set : lineData.getDataSets()) {

            if (set.isVisible())
                drawDataSet(c, set);
        }
        if (mDrawBitmap != null && mDrawBitmap.get() != null) {
            c.drawBitmap(mDrawBitmap.get(), 0, 0, mRenderPaint);
        }

    }


    /******************************* 点标记，类似多个markview默认显示 ************************************/
    @Override
    public void drawValues(Canvas c) {
        super.drawValues(c);
        if (isShowHLPoint) {
            LineDataSet dataSetByIndex = (LineDataSet) mChart.getLineData().getDataSetByIndex(0);
            Transformer trans = mChart.getTransformer(dataSetByIndex.getAxisDependency());
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);//抗锯齿画笔
            paint.setTextSize(Utils.convertDpToPixel(textSixe));//设置字体大小

            //画首中尾三个label
            float[] firstFloat = getFloat(dataSetByIndex.getValues(), 0);//根据数据集获取点
            drawPointLabel(trans, paint, c, firstFloat);
            float[] middleFloat = getFloat(dataSetByIndex.getValues(), (dataSetByIndex.getValues().size() - 1) / 2);//根据数据集获取点
            drawPointLabel(trans, paint, c, middleFloat);
            float[] endFloat = getFloat(dataSetByIndex.getValues(), dataSetByIndex.getValues().size() - 1);//根据数据集获取点
            drawPointLabel(trans, paint, c, endFloat);
            //画低点标记
//            drawLowPoint(dataSetByIndex, trans, paint, c);
        }
    }

    private void drawLowPoint(LineDataSet dataSetByIndex, Transformer trans, Paint paint, Canvas c) {
        float[] minFloat = getMinFloat(dataSetByIndex.getValues());//根据数据集获取最低点
        //通过trans得到最低点的屏幕位置
        MPPointD minPoint = trans.getPixelForValues(minFloat[0], minFloat[1]);
        float lowX = (float) minPoint.x;
        float lowY = (float) minPoint.y;
        paint.setColor(Color.parseColor("#1ab546"));
        float rectLength = Utils.convertDpToPixel((minFloat[1] + "").length() * Utils.convertDpToPixel(1.7f));//矩形框长
        //画横竖线
        c.drawLine(lowX, lowY, lowX, lowY + vLength, paint);
        if (lowX > mWidth - mWidth / 3) {//标识朝左
            c.drawLine(lowX, lowY + vLength, lowX - hLength, lowY + vLength, paint);
            //画矩形
            c.drawRect(new Rect((int) (lowX - hLength - rectLength), (int) (lowY + vLength - rect), (int) (lowX - hLength), (int) (lowY + vLength + rect)), paint);
            //写数字
            paint.setColor(Color.WHITE);
            c.drawText(minFloat[1] + "", lowX - rectLength - hLength + textX, lowY + vLength + textY, paint);
        } else {//标识朝右
            c.drawLine(lowX, lowY + vLength, lowX + hLength, lowY + vLength, paint);
            c.drawRect(new Rect((int) (lowX + hLength), (int) (lowY + vLength - rect), (int) (lowX + hLength + rectLength), (int) (lowY + vLength + rect)), paint);
            paint.setColor(Color.WHITE);
            c.drawText(minFloat[1] + "", lowX + hLength + textX, lowY + vLength + textY, paint);
        }
    }

    private float[] getMinFloat(List<Entry> lists) {
        float[] mixEntry = new float[2];
        for (int i = 0; i < lists.size() - 1; i++) {
            if (i == 0) {
                mixEntry[0] = lists.get(i).getX();
                mixEntry[1] = lists.get(i).getY();
            }
            if (mixEntry[1] > lists.get(i + 1).getY()) {
                mixEntry[0] = lists.get(i + 1).getX();
                mixEntry[1] = lists.get(i + 1).getY();
            }
        }
        return mixEntry;
    }

    private void drawPointLabel(Transformer trans, Paint paint, Canvas c, float[] floatPosition) {
        MPPointD maxPoint = trans.getPixelForValues(floatPosition[0], floatPosition[1]);
        float highX = (float) maxPoint.x;
        float highY = (float) maxPoint.y;
        TextView view = (TextView) LayoutInflater.from(mContext).inflate(R.layout.mark_view, null, false);
        if (highX > mWidth - mWidth / 4) {//标识朝左
            view.setBackgroundResource(R.mipmap.sm_lable_bg_buy_r);
            Bitmap bitmap = createBitmap(view, (int) hViewLength, (int) vViewLength);
            c.drawBitmap(bitmap, (int) (highX - hViewLength), (int) (highY - vViewLength - viewRect), paint);
        } else if (highX < mWidth / 4) {//标识朝右
            view.setBackgroundResource(R.mipmap.sm_lable_bg_buy_l);
            Bitmap bitmap = createBitmap(view, (int) hViewLength, (int) vViewLength);
            c.drawBitmap(bitmap, (int) (highX), (int) (highY - vViewLength - viewRect), paint);
        } else {//标识居中
            view.setBackgroundResource(R.mipmap.sm_lable_bg_buy_c);
            Bitmap bitmap = createBitmap(view, (int) hViewLength, (int) vViewLength);
            c.drawBitmap(bitmap, (int) (highX - hViewLength / 2), (int) (highY - vViewLength - viewRect), paint);
        }
    }

    private float[] getFloat(List<Entry> lists, int index) {
        float[] maxEntry = new float[2];
        maxEntry[0] = lists.get(index).getX();
        maxEntry[1] = lists.get(index).getY();
        return maxEntry;
    }

    private Bitmap createBitmap(View v, int width, int height) {
        //测量使得view指定大小
        int measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
        v.measure(measuredWidth, measuredHeight);
        //调用layout方法布局后，可以得到view的尺寸大小
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        Bitmap bmp = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        v.draw(c);
        return bmp;
    }
}
