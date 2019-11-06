package com.ahqlab.hodooopencv.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import org.opencv.core.Rect;

import java.util.List;

public class ResultImageView extends android.support.v7.widget.AppCompatImageView {
    private static final String TAG = ResultImageView.class.getSimpleName();
    private List<Rect> mRects;
    private int mPosition = 0;
    private boolean mClickState = false;
    private boolean mFullShowState = false;
    public ResultImageView(Context context) {
        this(context, null);
    }

    public ResultImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ResultImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( mClickState ) mClickState = false;
                mFullShowState = mFullShowState ? false : true;
                invalidate();
            }
        });
    }

    public void setRects(List<Rect> rects) {
        mRects = rects;
        invalidate();
    }

    public void itemClick ( int position ) {
        if ( mFullShowState ) mFullShowState = false;
        if ( mClickState && position == mPosition ) {
            mClickState = false;
            invalidate();
        } else {
            mClickState = false;
            invalidate();
            mClickState = true;
            mPosition = position;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint p = new Paint();
        p.setColor(Color.BLUE);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(4f);

        double scaledWidth = getWidth();
        double scaledHeight = getHeight();

        Bitmap imgbitmap = ((BitmapDrawable)getDrawable()).getBitmap();
        double xScaleFactor= scaledWidth/imgbitmap.getWidth();
        double yScaleFactor= scaledHeight/imgbitmap.getHeight();

        if ( mRects != null ) {
            if ( mClickState ) {
                org.opencv.core.Point point1 = new org.opencv.core.Point(mRects.get(mPosition).x, mRects.get(mPosition).y);
                org.opencv.core.Point point2 = new org.opencv.core.Point(mRects.get(mPosition).x + mRects.get(mPosition).width, mRects.get(mPosition).y + mRects.get(mPosition).height);

                Point cp1 = new android.graphics.Point((int)((point1.x*xScaleFactor)),(int)((point1.y*yScaleFactor)));
                Point cp2 = new android.graphics.Point((int)((point2.x*xScaleFactor)),(int)((point2.y*yScaleFactor)));
                canvas.drawRect(cp1.x, cp1.y, cp2.x,cp2.y, p);
            }
            if ( mFullShowState ) {
                for (int i = 0; i < mRects.size(); i++) {
                    org.opencv.core.Point point1 = new org.opencv.core.Point(mRects.get(i).x + 120, mRects.get(i).y + 120);
                    org.opencv.core.Point point2 = new org.opencv.core.Point(mRects.get(i).x + mRects.get(i).width - 120, mRects.get(i).y + mRects.get(i).height - 120);

                    Point cp1 = new android.graphics.Point((int)((point1.x*xScaleFactor)),(int)((point1.y*yScaleFactor)));
                    Point cp2 = new android.graphics.Point((int)((point2.x*xScaleFactor)),(int)((point2.y*yScaleFactor)));
                    canvas.drawRect(cp1.x, cp1.y, cp2.x,cp2.y, p);
                }
            }
        }
    }
}
