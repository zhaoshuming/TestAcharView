# TestAcharView（该demo为内部测试demo，多余功能请忽略）
利用MpAndroidChart，实现多点的特殊标记（类似默认显示多个markview）

最近在开发时遇到这样一种需求，为一些特殊点显示标签，类似默认显示多个markview。如下图：

![](https://zhaoshuming.github.io/2019/03/12/mpchart-point-label/chart_label.png)
在网上并没有相关资料，在此做下记录分享

下面上代码:

首先创建一个类继承LineChart,重写init()方法：

	@Override
    protected void init() {
        super.init();
        //获取屏幕宽度,上图最边上标签，会根据屏幕宽度适配
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        mRenderer = new HbFundLineChartRenderer(this, mAnimator, mViewPortHandler, metrics.widthPixels);
    }


接下来是主要内容,也就是自己实现的LineChartRenderer即渲染器,用来画点、线等.
首先是一些变量,分别是标记控件的宽高边距等,这里写的是一些根据我们需求来的默认值：

	private int mWidth;//屏幕宽度,在构造方法中传进来赋值
    private float hViewLength = Utils.convertDpToPixel(30f);//vie宽30dp
    private float vViewLength = Utils.convertDpToPixel(20f);//view高20dp
    private float viewRect= Utils.convertDpToPixel(4f);//矩形高低差

然后,在LineChartRenderer中有一个drawValues,它是主要负责根据值来画点的,我们要做的就是在super()之后加上我们自己的东西：

	@Override
    public void drawValues(Canvas c) {
        super.drawValues(c);
        if (isShowLabel) {
            LineDataSet dataSetByIndex = (LineDataSet) mChart.getLineData().getDataSetByIndex(0);
            Transformer trans = mChart.getTransformer(dataSetByIndex.getAxisDependency());
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);//抗锯齿画笔
            paint.setTextSize(Utils.convertDpToPixel(textSixe));//设置字体大小

            //画首中尾三个label
            float[] firstFloat = getFloat(dataSetByIndex.getValues(), 0);//根据数据集获取点
            drawPointLabel(trans, paint, c, firstFloat);
            float[] middleFloat = getFloat(dataSetByIndex.getValues(), (dataSetByIndex.getValues().size() - 1) / 2);
            drawPointLabel(trans, paint, c, middleFloat);
            float[] endFloat = getFloat(dataSetByIndex.getValues(), dataSetByIndex.getValues().size() - 1);
            drawPointLabel(trans, paint, c, endFloat);
        }
    }

首先获取点的数据集,然后得到Transformer,它可以根据点数据集里的某一点来得到这个点在屏幕中的位置
然后分别传入transformer、画笔、画布对象、点,进行绘制：

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

此处，我们随意定义几个点，可以根据实际需求进行设置：

	private float[] getFloat(List<Entry> lists, int index) {
        float[] maxEntry = new float[2];
        maxEntry[0] = lists.get(index).getX();
        maxEntry[1] = lists.get(index).getY();
        return maxEntry;
    }

view转bitmap方法如下：

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

参考资料：
https://www.jianshu.com/p/1877b8c2fc6c

