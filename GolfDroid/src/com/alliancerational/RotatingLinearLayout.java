package com.alliancerational;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.LinearLayout;
import android.view.MotionEvent;
import android.view.View;

public class RotatingLinearLayout extends LinearLayout {

    private final int mDiagonal;
    private float mBearing = 60;
    private Matrix mMatrix = new Matrix();
    private float[] mTemp = new float[2];
    private static final float SQ2 = 1.414213562373095f;

    int screenW;
    int screenH;

    public RotatingLinearLayout(final Context pContext, final AttributeSet pAttrs) {
        super(pContext, pAttrs);
        final DisplayMetrics dm = pContext.getResources().getDisplayMetrics();
        mDiagonal = (int) Math.hypot(dm.widthPixels, dm.heightPixels);
    }

    public void setBearing(final float pBearing) {
        mBearing = pBearing;
//        mBearing = 60;
    }

/*    @Override
    protected void dispatchDraw(final Canvas pCanvas) {
        pCanvas.rotate(-mBearing, getWidth() >> 1, getHeight() >> 1);
//        pCanvas.translate(200, 200);
        super.dispatchDraw(pCanvas);
    }*/
    
    @Override
    protected void dispatchDraw(Canvas canvas) {
    	System.out.println("RotationgLayout dispatching degrees: "+mBearing);
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
//        canvas.rotate(-mBearing, getWidth() * 0.5f, getHeight() * 0.5f);
        canvas.rotate(-mBearing, 450, 450);
        if(mBearing<50){
        	canvas.translate(-200, -200);
        }
        else if(mBearing<100){
        	canvas.translate(00, -400);
        }
        else if(mBearing<120){
        	canvas.translate(100, -300);
        }
        else if(mBearing<150){
        	canvas.translate(200, -100);
        }
        else if(mBearing<180){
        	canvas.translate(300, 00);
        }
        else if(mBearing<210){
        	canvas.translate(200, 100);
        }
        else if(mBearing<240){
        	canvas.translate(50, 200);
        }
        else if(mBearing<270){
        	canvas.translate(0, 300);
        }
        else if(mBearing<300){
        	canvas.translate(-150, 100);
        }
        else if(mBearing<330){
        	canvas.translate(-200, 50);
        }
        else if(mBearing<360){
        	canvas.translate(-200, 00);
        }
        canvas.getMatrix().invert(mMatrix);
       super.dispatchDraw(canvas);
        canvas.restore();
    }
    

    
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final float[] temp = mTemp;
        temp[0] = event.getX();
        temp[1] = event.getY();
        mMatrix.mapPoints(temp);
        event.setLocation(temp[0], temp[1]);
        return super.dispatchTouchEvent(event);
        
    }

    @Override
    protected void onMeasure(final int pWidthMeasureSpec,
            final int pHeightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(pWidthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(pHeightMeasureSpec);
        super.onMeasure(MeasureSpec.makeMeasureSpec(mDiagonal, widthMode), MeasureSpec.makeMeasureSpec(mDiagonal, heightMode));
        
    }
    

}
