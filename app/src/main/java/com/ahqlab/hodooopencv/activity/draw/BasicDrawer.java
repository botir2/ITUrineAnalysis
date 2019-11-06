package com.ahqlab.hodooopencv.activity.draw;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.ahqlab.hodooopencv.view.CameraPreview;

import org.opencv.core.Point;

import java.util.List;

public class BasicDrawer extends View {

    private final static String TAG = BasicDrawer.class.getSimpleName();
    
    RectDrawer drawer = null;
    MaskRectDrawer maskRectDrawer = null;

    private boolean focusState = false;
    private float x;
    private float y;

    public BasicDrawer(Context context) {
        this(context, null);
    }

    public BasicDrawer(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BasicDrawer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init() {
        drawer = new RectDrawer(this);
        maskRectDrawer = new MaskRectDrawer(1440, 2560);
        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawer.draw(canvas);
        maskRectDrawer.draw(canvas);
    }

    public void setPoint (List<Point> point) {
//        drawer.setPoint(point);
        maskRectDrawer.setPoint(point);
    }

    public void setFocusState ( boolean state ) {
        drawer.setFocusState(state);
    }
    public void setFocusPoint (float x, float y) {
        drawer.setFocusPoint(x, y);
    }
    public float getScale() {
        return Math.min(CameraPreview.mWidth / 1920, CameraPreview.mHeight / 1620);
    }
}
