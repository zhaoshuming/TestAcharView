package com.howbuy.taoliang.testacharview;

/**
 * Created by tao.liang on 2016/9/27.
 * 手指触摸事件 数据返回
 */

public interface ILineChartFingerTouchListener<T> {
    /**
     * 当前视图上是否有焦点(手指是否在上面)
     * true:当前有焦点
     */
    void onChartViewFocus(boolean focus);

    /**
     * 这个适用于单/多线条 的 单 手指触摸事件
     * 单手指触摸时对应的点的下标
     *
     * @param index
     */
    void onFitSingleTouchIndex(int index);

    /**
     * 这个适用于单/多线条 的 双 手指触摸事件
     * 返回手指触摸的点对应的数据下标
     *
     * @param preIndex 手指1:对应在屏幕上x方向上坐标点小的下标
     * @param sufIndex 手指2:对应在屏幕上x方向上坐标点大的下标
     */
    void onFitDoubleTouchIndexs(int preIndex, int sufIndex);
}
