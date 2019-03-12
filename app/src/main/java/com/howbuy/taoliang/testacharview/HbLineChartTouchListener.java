package com.howbuy.taoliang.testacharview;

import android.annotation.SuppressLint;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet;
import com.github.mikephil.charting.listener.BarLineChartTouchListener;

import java.util.ArrayList;

/**
 * Created by tao.liang on 2016/9/27.
 * 手指触摸事件 监听接口, 这里处理所有触摸事件
 */
public class HbLineChartTouchListener extends BarLineChartTouchListener {

    private static final String TAG = "HbBarLineChart";
    private HbFundLineChartBase mHbBarLineChartBase;

    private int mMaxTouch = 2;
    private int mTouchSlopTime = 100;
    private int mTouchDownTime = 150;
    private boolean mWaitForUser = true;
    private boolean mUserFocused = false;
    private boolean mUserMoved = false;
    private ArrayList<PointF> mDownEventList = new ArrayList<PointF>(mMaxTouch);


    /**
     * Constructor with initialization parameters.
     *
     * @param chart               instance of the chart
     * @param touchMatrix         the touch-matrix of the chart
     * @param dragTriggerDistance the minimum movement distance that will be interpreted as a "drag" gesture in dp (3dp equals
     */
    public HbLineChartTouchListener(BarLineChartBase<? extends BarLineScatterCandleBubbleData<? extends IBarLineScatterCandleBubbleDataSet<? extends Entry>>> chart, Matrix touchMatrix, float dragTriggerDistance) {
        super(chart, touchMatrix, dragTriggerDistance);
        mHbBarLineChartBase = (HbFundLineChartBase) mChart;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!mChart.isEnabled()) {
            resetTouch(false);
            return false;
        }
        resetTouch(true);

        int origionalAction = event.getAction();
        int pointIndex = (origionalAction & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT; // 等效于
        float x = event.getX(pointIndex), y = event.getY(pointIndex);
        if (pointIndex >= mMaxTouch) {
            return false;
        }

        /**
         *ismCustomFingerTouchEnable() 自定义添加一个手指触摸变量来控制事件的处理流程
         * 因为其它三个是源码中用到的
         */
        if (!mHbBarLineChartBase.ismCustomFingerTouchEnable()
                && (!mHbBarLineChartBase.isScaleXEnabled() && !mHbBarLineChartBase.isScaleYEnabled())) {
            if (!mHbBarLineChartBase.ismCustomGestureSingleTap()) {
                return false;
            }
        }

        int action = origionalAction & MotionEvent.ACTION_MASK;
        switch (action) {

            case MotionEvent.ACTION_DOWN:
                //startAction(event);

                mUserMoved = false;
                mDownEventList.clear();
                mDownEventList.add(new PointF(x, y));
                handDown(pointIndex);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() >= 2 && mHbBarLineChartBase.ismCanDoubleFingerTouchable()) {
                    mChart.disableScroll();
                    if (mChart.isPinchZoomEnabled()) {
                        mTouchMode = PINCH_ZOOM;
                    } else {
                        if (mChart.isScaleXEnabled() != mChart.isScaleYEnabled()) {
                            mTouchMode = mChart.isScaleXEnabled() ? X_ZOOM : Y_ZOOM;
                        } else {
                            mTouchMode = X_ZOOM;
                        }
                    }
                    try {
                        mDownEventList.add(pointIndex, new PointF(x, y));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    handDown(pointIndex);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mTouchMode == DRAG) {
                    mChart.disableScroll();
                } else if (mTouchMode == X_ZOOM || mTouchMode == Y_ZOOM || mTouchMode == PINCH_ZOOM) {
                    mChart.disableScroll();

                    if (!(mChart.isScaleXEnabled() || mChart.isScaleYEnabled())) {
                        //双指触摸事件
                        handMove(pointIndex, event);
                    }
                } else if (mTouchMode == NONE) {
                    if (mChart.hasNoDragOffset()) {
                        if (!mChart.isFullyZoomedOut() && mChart.isDragEnabled()) {
                            mTouchMode = DRAG;
                        } else {
                            if (mHbBarLineChartBase.ismCustomGestureSingleTap()) {
                                performHighlightDrag(event);
                            } else {
                                //单手指事件
                                handMove(pointIndex, event);
                            }
                        }
                    } else if (mChart.isDragEnabled()) {
                        mTouchMode = DRAG;
                    }
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
                mTouchMode = NONE;
                if (pointIndex < mDownEventList.size()) {
                    mDownEventList.remove(pointIndex);
                }
                //手指触摸事件
                //这里特别是要注意的是 pointIndex,因为手指触发该事件后,pointCount的数量并不会立刻变化
                try {
                    performCustomHighlightDrag(1 - pointIndex, event);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }

                break;

            case MotionEvent.ACTION_UP:
                mTouchMode = NONE;
                mUserMoved = false;
                mUserFocused = false;

                if (pointIndex < mDownEventList.size()) {
                    mDownEventList.remove(pointIndex);
                }
                handUp();

                //endAction(event);

                break;

            case MotionEvent.ACTION_CANCEL:
                mTouchMode = NONE;
                mUserFocused = false;
                //endAction(event);
                break;
        }

        return true;
    }

    /**
     * Highlights upon dragging, generates callbacks for the selection-listener.
     *
     * @param e
     */
    private void performHighlightDrag(MotionEvent e) {

        Highlight h = mChart.getHighlightByTouchPoint(e.getX(), e.getY());

        if (h != null && !h.equalTo(mLastHighlighted)) {
            mLastHighlighted = h;
            mChart.highlightValue(h, true);
        }
    }

    /**
     * 这里处理单/双手指事件
     *
     * @param event
     */
    private void performCustomHighlightDrag(int pointIndexer, MotionEvent event) throws IllegalArgumentException {
        if (!mHbBarLineChartBase.ismCustomFingerTouchEnable()) {
            return;
        }
        Highlight[] highlightArr = null;

        if (mDownEventList.size() == 1) {
            highlightArr = new Highlight[1];
            Highlight highlights1 = mChart.getHighlightByTouchPoint(event.getX(pointIndexer), event.getY(pointIndexer));
            Entry entry1 = mChart.getEntryByTouchPoint(event.getX(pointIndexer), event.getY(pointIndexer));
            IBarLineScatterCandleBubbleDataSet set1 = mChart.getDataSetByTouchPoint(event.getX(pointIndexer), event.getY(pointIndexer));
            if (set1 == null || entry1 == null) {
                return;
            }

            highlightArr[0] = highlights1;
            int index = set1.getEntryIndex(entry1);
            //把数据回传到界面
            if (mHbBarLineChartBase.getFingerTouchListener() != null) {
                mHbBarLineChartBase.getFingerTouchListener().onFitSingleTouchIndex(index);
            }

        } else if (mDownEventList.size() >= 2) {
            int[] indexs = new int[2];
            highlightArr = new Highlight[2];

            Highlight highlights1 = mChart.getHighlightByTouchPoint(event.getX(0), event.getY(0));
            Entry entry1 = mChart.getEntryByTouchPoint(event.getX(0), event.getY(0));
            IBarLineScatterCandleBubbleDataSet set1 = mChart.getDataSetByTouchPoint(event.getX(0), event.getY(0));
            if (set1 == null || entry1 == null || highlights1 == null) {
                return;
            }
            int dataIndex1 = set1.getEntryIndex(entry1);

            Highlight highlights2 = mChart.getHighlightByTouchPoint(event.getX(1), event.getY(1));
            Entry entry2 = mChart.getEntryByTouchPoint(event.getX(1), event.getY(1));
            IBarLineScatterCandleBubbleDataSet set2 = mChart.getDataSetByTouchPoint(event.getX(1), event.getY(1));

            if (set2 == null || entry2 == null || highlights2 == null) {
                return;
            }

            int dataIndex2 = 0;
            if (set2 != null && entry2 != null) {
                dataIndex2 = set2.getEntryIndex(entry2);
            }

            /**
             * 为了在下层不用对数据 手指1 和手指2的数据进行先后处理
             * 这里把两个点的数据作简单的排序,把x轴坐标值大的数据放在集合中的第二个位置
             * 如果对于高亮来说 不处理 就会导致 绘制矩形阴影的时候 矩形的边框不正确
             */
            if (mDownEventList.get(0).x < mDownEventList.get(1).x) {
                highlightArr[0] = highlights1;
                highlightArr[1] = highlights2;

                indexs[0] = dataIndex1;
                indexs[1] = dataIndex2;
            } else {
                highlightArr[0] = highlights2;
                highlightArr[1] = highlights1;

                indexs[0] = dataIndex2;
                indexs[1] = dataIndex1;
            }

            //把数据回传到界面
            if (mHbBarLineChartBase.getFingerTouchListener() != null) {
                mHbBarLineChartBase.getFingerTouchListener().onFitDoubleTouchIndexs(indexs[0], indexs[1]);
            }
        }

        //单/双手指线(两端和中间的阴影区)
        mChart.highlightValues(highlightArr);

    }


    public void resetTouch(boolean enable) {
        if (!enable) {
            mWaitForUser = true;
            mUserFocused = false;
            mUserMoved = false;
        }
    }

    /**
     * 如果pointIndex =0;说明是第一个手指刚触摸上来的
     * 如果不为0, 说明是第二个手指触摸上来的(双手指事件)
     */
    private void handDown(int pointIndex) {
        if (pointIndex == 0) {
            mWaitForUser = true; // 是否在等用户交互。
            mUserFocused = false;// 是否用户已经交互上了。

        }

    }

    private void handMove(int pointIndexer, MotionEvent e) {
        if (!mWaitForUser && !mUserFocused) {
            return;
        }

        if (!mUserFocused) {
            mWaitForUser = mTouchSlopTime > e.getEventTime() - e.getDownTime();


            if (!mWaitForUser) {
                mUserFocused = true;
                mUserMoved = true;
                //手指触摸事件
                try {
                    performCustomHighlightDrag(pointIndexer, e);
                } catch (IllegalArgumentException e1) {
                    e1.printStackTrace();
                }
            }
        } else {
            //手指触摸事件
            try {
                performCustomHighlightDrag(pointIndexer, e);
            } catch (IllegalArgumentException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * 当手指离开后,设置焦点为false
     *
     * @return
     */
    private void handUp() {
        if (mHbBarLineChartBase.getFingerTouchListener() != null) {
            mHbBarLineChartBase.getFingerTouchListener().onChartViewFocus(false);
        }
    }
}
