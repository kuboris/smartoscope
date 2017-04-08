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

public class HoleView extends FrameLayout {

    private static final float RADIUS = 300;
    private Paint mBackgroundPaint;
    private int mTutorialColor = Color.parseColor("#FFFFFFFF");

    public HoleView(Context context) {
        super(context);
        init();
    }

    public HoleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HoleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HoleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        setLayerType(LAYER_TYPE_HARDWARE, null);

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(mTutorialColor);
//        canvas.drawCircle(getWidth()/2, getHeight()/2, STATE_RADIUS, mBackgroundPaintState);
        canvas.drawCircle(getWidth()/2, getHeight()/2, RADIUS, mBackgroundPaint);
    }
}
