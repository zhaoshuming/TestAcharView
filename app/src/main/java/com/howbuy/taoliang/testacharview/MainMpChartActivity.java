package com.howbuy.taoliang.testacharview;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.List;


public class MainMpChartActivity extends Activity implements OnChartGestureListener, OnChartValueSelectedListener {

    private HbFundLineChart lineChart1;
    private TextView mTvResult;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    mTvResult.setText("当前的差值: " + msg.obj);
                    break;
            }
        }
    };
    private List<Entry> mData;
    private List<Entry> mData1;

    private List<String> mXList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mp_chart);

        mTvResult = (TextView) findViewById(R.id.tv_result);

        lineChart1 = (HbFundLineChart) findViewById(R.id.linechart1);

        lineChart1.setmCustomGestureSingleTap(true);

        lineChart1.getDescription().setEnabled(false);
        //禁用绽放
        lineChart1.setScaleEnabled(false); //这个等同于上面两个

        lineChart1.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart1.getXAxis().setLabelCount(7, true);

        lineChart1.getAxisLeft().setLabelCount(5, true);
        lineChart1.getAxisLeft().enableGridDashedLine(5, 5, 0);
        lineChart1.getAxisLeft().setDrawZeroLine(true);

        lineChart1.getAxisRight().setEnabled(false);

        //绘制网格
        lineChart1.getXAxis().setGridColor(0xffff0000);
//        lineChart1.getXAxis().setGridLineWidth(0.3f);
//        lineChart1.getAxisLeft().setGridLineWidth(0.3f);
//        lineChart1.getAxisLeft().setGridColor(0xffff0000);



        // for test axis
        lineChart1.getXAxis().setEnabled(true);
        lineChart1.getXAxis().setDrawGridLines(false);
//        lineChart1.getAxisLeft().setAxisMinimum(0);
        LegendEntry[] legendEntry = new LegendEntry[2];
        legendEntry[0] = new LegendEntry();
        legendEntry[0].label = "hhh";
        legendEntry[0].formColor = 0xffff0000;
        legendEntry[1] = new LegendEntry();
        legendEntry[1].label = "hhh";
        legendEntry[1].formColor = 0xffff0000;
        lineChart1.getLegend().setCustom(legendEntry);
        lineChart1.getLegend().setForm(Legend.LegendForm.CIRCLE);

        //设置边框
        lineChart1.setDrawBorders(false);
//        lineChart1.setBorderWidth(0.5f);
//        lineChart1.setBorderColor(0xffcccccc);

        lineChart1.setOnChartValueSelectedListener(this);


        //在双手指事件中不能设置这个为false,因为 BarLineChartTouchListener的onTouch中做了设置
        lineChart1.setDragEnabled(false);

        MyMarkerView markerView = new MyMarkerView(this, R.layout.custom_marker_view);

        markerView.setChartView(lineChart1); // For bounds control
        lineChart1.setMarker(markerView); // Set the marker to the chart

        mXList = setXValList();
        //设置矩形阴影的颜色
        mData = setData();

        LineDataSet testLineA = new LineDataSet(mData, "a");
        testLineA.setLineWidth(1.0f);
        testLineA.setColor(0xffff0000);//红色
        //画孔 true就是画孔,false画实心点
        testLineA.setDrawCircleHole(true);
        //初始化图的时候就在每个点上画了圆点
        testLineA.setDrawCircles(false); //坐标点上是否要画圆
        testLineA.setCircleColorHole(0xff00ff00); //空心圆的颜色
//        testLineA.setCircleColor(0xff00ff00);
        //开启才有用
        testLineA.setHighlightEnabled(false);
        testLineA.setDrawVerticalHighlightIndicator(true);
        testLineA.setDrawHorizontalHighlightIndicator(true); //隐藏水平的高亮线
//        Highlight highligh = lineChart1.getHighlightByTouchPoint(3, 0);
//        testLineA.setHighLightColor(0xffff0000); //设置点坐标十字线颜色

        testLineA.setDrawIcons(false);

        //是否显示点上的values值
        testLineA.setDrawValues(false);

//        testLineA.setDrawFilled(true);
//        testLineA.setFillColor(0x33E5F1FF);

        //部分填充
        LineDataSet testLineB = new LineDataSet(getDataB(), "b");
        testLineB.setLineWidth(1.0f);
        testLineB.setColor(0xffff0000);//红色
        testLineB.setHighlightEnabled(false);
        //初始化图的时候就在每个点上画了圆点
        testLineB.setDrawCircles(false); //坐标点上是否要画圆
        //是否显示点上的values值
        testLineB.setDrawValues(false);
        testLineB.setDrawFilled(true);
        testLineB.setFillColor(0x33E5F1FF);


        LimitLine xLimitLine = new LimitLine(5f,"");
        xLimitLine.setLineColor(Color.BLUE);
        xLimitLine.enableDashedLine(5, 5, 0);
        lineChart1.getXAxis().addLimitLine(xLimitLine);
//        xAxis.setDrawLimitLinesBehindData(false);

        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(testLineB);
        dataSets.add(testLineA);

        /**
         * 老版本,需要一个统一的x轴数据,在3.0.0-bate上可以不用
         * 但是如果是多线的话,如果设置每条数据的时候x轴上的点不一样,会可以单/双手指事件异常
         * 所以在多条线的情况下,需要统一一个x轴上的坐标数据
         */
        LineData ld = new LineData(dataSets);

        lineChart1.setData(ld);

        lineChart1.setCustomGridBgStyle(true);
        lineChart1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                lineChart1.getData().getDataSetByIndex(0).setHighlightEnabled(true);
                lineChart1.highlightValue(null, true);
                lineChart1.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                    @Override
                    public void onValueSelected(Entry e, Highlight h) {
                        Log.e("HIGHLIGHT", String.valueOf(h.getX()));
                    }

                    @Override
                    public void onNothingSelected() {

                    }
                });
                return false;
            }
        });

    }


    /**
     * 第一步: 得到所以线的最小和最大端点之前的总共点数据:
     * 比如: 第一条线:有10个实际点;第二条线有20个实际点;
     * 那么在图上显示的时候要以 20个点为基准画线
     *
     * @return
     */
    private List<String> setXValList() {
        List<String> listX = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            listX.add(i + "");
        }
        return listX;
    }

    /**
     * 第二步: 获取第一条线对应的实际数据点
     *
     * @return
     */
    private List<DataEntity> prepareLineAData() {
        List<DataEntity> list = new ArrayList<>();

        DataEntity entity = new DataEntity(2 + "", (20 - 2) + "");
        list.add(entity);

        return list;

    }

    /**
     * 第三步: 获取第二条线对应的实际数据点
     *
     * @return
     */
    private List<DataEntity> prepareLineBData() {
        List<DataEntity> list = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            DataEntity entity = new DataEntity(i + "", i + "");
            list.add(entity);
        }
        return list;

    }


    /**
     * 在多条线带有 单/双手指触摸事件的时候,就需要统一X轴,并且补点,
     * 否则在触摸获取点数据时,会因为对应不到数据而有问题,
     * 以下是解决办法:
     * <p>
     * 第四步: 按x轴上有最大的个点来计算,补点
     * 比如: line1实际只有10个实际点: line2有20个实际点
     * 以line2的实际点等分间隔数据来补点
     * 判断 line1上差了哪些点
     * 通过循环X轴上的数据,比较line1上的实际点的x坐标值与X轴上哪些值相等,
     * 相等就说明是实际点,找不到对应的x值就说明是缺少的点,需要补上
     * <p>
     * 补点逻辑如下:
     * 根据需要来选择处理逻辑
     * a. 补点的时候,设置缺少的点的y值为0;
     * b. 也可以使用上一个实际点的y值.
     * <p>
     * 如果要设置缺少的点的y值为0,就需要置缺少的点的y值为0;
     * 如果要设置缺少的点的y值为上一个实际点的y值,就要先保存一下上一个实际点的y值
     * 如果发再是缺少的点,就把其值赋值给它
     *
     * @return
     */
    private List<Entry> setData() {
        //初始化集合,保存线条所有的点,为画图数据
        List<Entry> entryList = new ArrayList<>();
        //获取初始点的数据集合
        List<DataEntity> list = prepareLineAData();

        //默认为0(保存上一个实际点的y值)
        String verticalVal = "0";

        //循环X轴上的所有点(mXList代表X轴)
        for (int i = 0; i < mXList.size(); i++) {
            String xVal = mXList.get(i);
            //设置变量,isRealEntry = true,说明这是个实际点
            boolean isRealEntry = false;

            //需要获取在X轴上有哪些点是实际点
            for (int j = 0; j < list.size(); j++) {
                DataEntity entity = list.get(j);
                String oritentationVal = entity.getOritentationVal();
                if (xVal.equals(oritentationVal)) {
                    verticalVal = entity.getVerticalVal(); //得这个这实际点的y值
                    entryList.add(i, new Entry(Float.valueOf(xVal), Float.valueOf(verticalVal)));
                    isRealEntry = true;
                    break;
                }
            }
            //设置缺少点的数据
            if (!isRealEntry) {
//                entryList.add(i, new Entry(Float.valueOf(xVal), Float.valueOf("0")));
                entryList.add(i, new Entry(Float.valueOf(xVal), Float.valueOf(verticalVal)));
            }

        }
        return entryList;
    }

    /**
     * 阴影部分测试数据
     */
    private List<Entry> getDataB() {
        //初始化集合,保存线条所有的点,为画图数据
        List<Entry> entryList = new ArrayList<>();
        //默认为0(保存上一个实际点的y值)
        String verticalVal = "0";
        List<DataEntity> list = prepareLineAData();

        //循环X轴上的所有点(mXList代表X轴)
        for (int i = 0; i < mXList.size(); i++) {
            String xVal = mXList.get(i);
            boolean isRealEntry = false;

            //需要获取在X轴上有哪些点是实际点
            for (int j = 0; j < list.size(); j++) {
                DataEntity entity = list.get(j);
                String oritentationVal = entity.getOritentationVal();
                if (xVal.equals(oritentationVal)) {
                    verticalVal = entity.getVerticalVal(); //得这个这实际点的y值
                    entryList.add(new Entry(Float.valueOf(xVal), Float.valueOf(verticalVal)));
                    isRealEntry = true;
                    break;
                }
            }
            //设置缺少点的数据
            if (!isRealEntry) {
                entryList.add(new Entry(Float.valueOf(xVal), Float.valueOf(verticalVal)));
            }
        }
        return entryList;
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
//        if (lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP)
            //lineChart1.highlightValues(null); // or highlightTouch(null) for callback to onNothingSelected(...)
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {

    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {

    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {

    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {

    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }
}
