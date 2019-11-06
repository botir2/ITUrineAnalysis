package com.ahqlab.hodooopencv.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

import static android.content.ContentValues.TAG;

public class ComburImageView extends android.support.v7.widget.AppCompatImageView {
    public static final int DRAW_NONE = 0;
    public static final int DRAW_BOARD = 1;
    private boolean state = false;

    private int mWidth, mHeight;
    public ComburImageView(Context context) {
        this(context, null);
    }

    public ComburImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ComburImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public void setDrawState( int drawState ) {
        if (drawState == DRAW_BOARD) {
            state = true;
        } else {
            state = false;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if ( state ) {
            Log.e(TAG, "draw board");
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            Rect rect = new Rect();
            rect.top = 0;
            rect.left = 0;
            rect.bottom = mHeight;
            rect.right = mWidth;
            canvas.drawRect(rect, paint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = widthMeasureSpec;
        mHeight = heightMeasureSpec;
    }
}
