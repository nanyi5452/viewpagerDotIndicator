package com.nanyi545.ww.vpdotindicator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Administrator on 2016/11/7.
 */
public class VpIndicator extends View {


    public VpIndicator(Context context) {
        super(context);
        init();
    }

    public VpIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VpIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    float mDensity;

    int totalCounts=3;
    private static final int squareSizeInDp=40;
    private static final int circleRadiusInDp=12;
    int circleRadiusInPx=10;



    Paint bgCirclePaint;
    Paint colorPaint;
    Paint testPaint;


    private void init(){
        mDensity = getResources().getDisplayMetrics().density;
        circleRadiusInPx= (int) (circleRadiusInDp*mDensity);

        bgCirclePaint= new Paint();
        bgCirclePaint.setAntiAlias(true);
        bgCirclePaint.setColor(Color.rgb(255,255,255));

        colorPaint = new Paint();
        colorPaint.setAntiAlias(true);
        colorPaint.setColor(Color.rgb(255,20,20));

        testPaint = new Paint();
        testPaint.setAntiAlias(true);
        testPaint.setColor(Color.rgb(20,255,20));

        path =new Path();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthSize = View.resolveSize(getDesiredWidth(), widthMeasureSpec);
        viewWidth=widthSize;
        int heightSize = View.resolveSize(getDesiredHeight(), heightMeasureSpec);
        viewHeight=heightSize;

        setMeasuredDimension(widthSize, heightSize);
        if (firstStart){
            initFixedLabelPositions();
            preInitPath(0,0f);
            initPath();
            firstStart=false;
        }
    }

    boolean firstStart=true;

    int viewWidth,viewHeight;


    private int getDesiredHeight() {
        return (int) (squareSizeInDp*mDensity);
    }

    private int getDesiredWidth() {
        return (int) (squareSizeInDp*mDensity)*totalCounts;
    }


    PointF[] fixedLabels;
    private void initFixedLabelPositions(){
        fixedLabels=new PointF[totalCounts];
        for (int i=0;i<totalCounts;i++){
            fixedLabels[i]=new PointF( viewWidth/2f/totalCounts*(2*i+1) ,viewHeight/2f);
        }
    }


    private void drawFixedlabels(Canvas canvas){
        for (int i=0;i<totalCounts;i++){
            canvas.drawCircle(fixedLabels[i].x,fixedLabels[i].y,circleRadiusInPx,bgCirclePaint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawFixedlabels(canvas);


        canvas.drawPath(path, colorPaint);
        canvas.drawCircle(p1.x,p1.y,r1,colorPaint);
        canvas.drawCircle(p2.x,p2.y,r2,colorPaint);

//        canvas.drawCircle(centerPoint.x,centerPoint.y,1f,testPaint);
//        canvas.drawCircle(helperPoint1.x,helperPoint1.y,1f,testPaint);
//        canvas.drawCircle(helperPoint2.x,helperPoint2.y,1f,testPaint);
//        canvas.drawCircle(helperPoint3.x,helperPoint3.y,1f,testPaint);
//        canvas.drawCircle(helperPoint4.x,helperPoint4.y,1f,testPaint);
    }


    Path path;
    PointF p1=new PointF(),p2=new PointF();
    float r1,r2;



    private void preInitPath(int position,float progress){


        PointF start= fixedLabels[position];
        PointF end= fixedLabels[position+1];

        p1.y=start.y;
        p2.y=start.y;

        float p1_end_x=end.x - circleRadiusInPx;
        float p2_start_x=start.x + circleRadiusInPx;

        if (progress<=0.5f){
            p1.x=start.x;
            p2.x=p2_start_x+(end.x-p2_start_x)*progress*2;
        } else {
            p1.x=(p1_end_x-start.x)*(progress-0.5f)*2 + start.x;
            p2.x=end.x;
        }

        r1=(1f-progress)*circleRadiusInPx;
        r2=(progress)*circleRadiusInPx;

    }

    //---for debugging....
    PointF centerPoint,helperPoint1,helperPoint2,helperPoint3,helperPoint4;


    private void initPath(){

        PointF p1;
        float r1;
        PointF p2;
        float r2;

        p1=this.p1;
        p2=this.p2;
        r1=this.r1;
        r2=this.r2;



        float ratio=r1/(r1+r2);
        PointF temp1=new PointF((p2.x-p1.x)*ratio+p1.x,(p2.y-p1.y)*ratio+p1.y);
        centerPoint=temp1;


        PointF[] helperPoints_12=getTangentPoints(r1,p1,temp1);
        helperPoint1=helperPoints_12[0];
        helperPoint2=helperPoints_12[1];

        PointF[] helperPoints_21=getTangentPoints(r2,p2,temp1);
        helperPoint3=helperPoints_21[0];
        helperPoint4=helperPoints_21[1];

        path=new Path();
        path.moveTo(helperPoints_12[0].x, helperPoints_12[0].y);
        path.quadTo(temp1.x,temp1.y,helperPoints_21[0].x, helperPoints_21[0].y);
        path.lineTo(helperPoints_21[1].x, helperPoints_21[1].y);
        path.quadTo(temp1.x,temp1.y,helperPoints_12[1].x, helperPoints_12[1].y);

    }


    public void update(int position,float progress){
        if(position>=(totalCounts-1)){
            return;
        }
        preInitPath(position,progress);
        initPath();
        invalidate();
    }



    //  center and external should have the same y
    private PointF[] getTangentPoints(float radius, PointF center, PointF external){
        float dx=radius*radius/(center.x-external.x);
        float dy= (float) Math.sqrt(radius*radius-dx*dx);
        PointF[] ret=new PointF[2];
        ret[0]=new PointF(center.x-dx,center.y-dy);
        ret[1]=new PointF(center.x-dx,center.y+dy);
        return ret;
    }




    public void resetTotalCounts(int counts){
        totalCounts=counts;
        requestLayout();
        invalidate();
    }






}




