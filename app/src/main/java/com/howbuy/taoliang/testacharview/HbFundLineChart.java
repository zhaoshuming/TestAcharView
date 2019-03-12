package com.howbuy.taoliang.testacharview;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.renderer.LineChartRenderer;

/**
 * Created by tao.liang on 2016/9/28.
 * 基于mPAndroidChart 3.0.3-beta版本上:实现单手指/双手指触摸事件,支持单/多条线
 * 根据目前的需求:满足普通样式和好买自定义样式(类似累计收益线性视图样式)
 * 基本上就可以满足开发需求, 如果还有其它特殊样式,可以通过重写
 * HbFundXAxisRenderer实现
 */

public class HbFundLineChart extends HbFundLineChartBase<LineData> implements LineDataProvider {

    public HbFundLineChart(Context context) {
        super(context);
    }

    public HbFundLineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HbFundLineChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * 如果您的程序中使用了自定义视图或者绘图调用，程序可能会工作不正常。
     * 如果您的程序中只是用了标准的视图和Drawable，放心大胆的开启硬件加速吧！
     * 具体是哪些绘图操作不支持硬件加速呢：Canvas clipPath() clipRegion() drawPicture() drawPosText() drawTextOnPath() drawVertices()
     * Paint setLinearText() setMaskFilter() setRasterizer()
     * 另外还有一些绘图操作，开启和不开启硬件加速，效果不一样：
     */
    @Override
    protected void init() {
        super.init();
        //获取屏幕宽度,因为默认是向右延伸显示数字的(如图1),当最值在屏幕右端,屏幕不够显示时要向左延伸(如图2)
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);

        //setLayerType是Api 11 才开始有的,如果是在4.1版本以下,要禁用硬件加速,否则4.0.x版本是报错: HbLineChartRenderer
        setHardwareForbidden();
        mRenderer = new HbFundLineChartRenderer(this, mAnimator, mViewPortHandler, metrics.widthPixels, getContext());
    }

    /**
     * 在4.0.x上禁用硬件加速
     */
    private void setHardwareForbidden() {
        //setLayerType是Api 11 才开始有的,如果是在4.1版本以下,要禁用硬件加速,否则4.0.x版本是报错: HbLineChartRenderer
        //clipPath()方法从API18开始才能支持硬件加速
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            //setLayerType(LAYER_TYPE_SOFTWARE, null);
            setHardwareAccelerationEnabled(false);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (canvas.isHardwareAccelerated()) {
            setHardwareForbidden();
        }
        super.onDraw(canvas);
    }

    @Override
    public LineData getLineData() {
        return mData;
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mRenderer != null && mRenderer instanceof LineChartRenderer) {
            ((LineChartRenderer) mRenderer).releaseBitmap();
        }
        super.onDetachedFromWindow();
    }

    //把高亮线去掉
    public void cleanHighlight() {
        //在横屏时,viewpager 切换的时候,需要把原来选中的高亮线去掉
        if (mIndicesToHighlight != null) {
            mChartTouchListener.setLastHighlighted(null);
            mIndicesToHighlight = null;//这才是把高亮线去掉的主要代码
            invalidate();
        }

    }

}
