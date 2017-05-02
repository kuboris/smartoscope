package cz.binarytrio.molescope.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import cz.binarytrio.molescope.R;

/**
 * Created by numinga on 8.4.17
 */

public class DetectionStateAimView extends FrameLayout {

    // Add to DetectionStateView.java
    public static final int UNDEFINED = 1;
    public static final int SKIN = 2;
    public static final int DETECTED_MOLE = 3;
    public static final int DETECTED_MELANOMA = 4;

    private int mState;

    private Paint mStatePaint;

    private Bitmap mAimBitmap;


    public DetectionStateAimView(Context context) {
        super(context);
        init();
    }

    public DetectionStateAimView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DetectionStateAimView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DetectionStateAimView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        setLayerType(LAYER_TYPE_HARDWARE, null);
        mState = UNDEFINED;
        mStatePaint = new Paint();
        mAimBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.aim);
    }

    public void setState(int state){
        int color;
        switch(state){
            case SKIN:
                color = Color.YELLOW;
                break;
            case DETECTED_MOLE:
                color = Color.GREEN;
                break;
            case DETECTED_MELANOMA:
                color = Color.RED;
                break;
            default: // case UNDEFINED:
                color = Color.BLUE;
                break;
        }

        mState = state;
        mStatePaint.setColor(Color.RED);
        mStatePaint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
    }

    public int getState(){
        return mState;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mAimBitmap, getWidth()/2-mAimBitmap.getWidth()/2, getHeight()/2-mAimBitmap.getHeight()/2, mStatePaint);
    }
}
