package org.tensorflow.demo;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by numinga on 8.4.17.
 */

public class DetectionStateView extends FrameLayout {

    enum DetectionState {
        UNDEFINED, SKIN, DETECTED
    }

    private static final float RADIUS = 300;
    private static final float STATE_RADIUS = 320;

    private DetectionState mState;

    private Paint mBackgroundPaint;
    private Paint mBackgroundPaintState;


    private int mTutorialColor = Color.parseColor("#FFFFFFFF");

    public DetectionStateView(Context context) {
        super(context);
        init();
    }

    public DetectionStateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DetectionStateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DetectionStateView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        setLayerType(LAYER_TYPE_HARDWARE, null);

        mState = DetectionState.UNDEFINED;

        mBackgroundPaintState = new Paint();
        mBackgroundPaintState.setColor(Color.RED);
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    public void setState(DetectionState state){
        switch(state){
            case UNDEFINED:
                mBackgroundPaintState.setColor(Color.RED);
                break;
            case SKIN:
                mBackgroundPaintState.setColor(Color.YELLOW);
                break;
            case DETECTED:
                mBackgroundPaintState.setColor(Color.GREEN);
                break;
        }
        mState = state;
//        invalidate();
    }

    public DetectionState getState(){
        return mState;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(mTutorialColor);
        canvas.drawCircle(getWidth()/2, getHeight()/2, STATE_RADIUS, mBackgroundPaintState);
        canvas.drawCircle(getWidth()/2, getHeight()/2, RADIUS, mBackgroundPaint);
    }
}
