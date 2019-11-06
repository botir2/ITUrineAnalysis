package com.ahqlab.hodooopencv.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.ahqlab.hodooopencv.domain.HodooRect;

import java.util.List;

public class MaskView extends RelativeLayout {
    private final String TAG = MaskView.class.getSimpleName();

    private float mX1, mX2, mY1, mY2 = -1;
    private List<HodooRect> mRects;
    public MaskView(@NonNull Context context) {
        super(context);
    }

    public MaskView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MaskView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if ( mRects != null ) {
            Log.e(TAG, "null check");
            if ( mRects.size() > 0 ) {
                Log.e(TAG, "size check");
                if ( mRects.size() >= 4 ) {
                    Log.e(TAG, "path start");
                    Path path = new Path();
                    path.moveTo(mRects.get(0).getX(), mRects.get(0).getY());
                    Paint paint = new Paint();
                    paint.setColor(Color.BLUE);
                    paint.setAlpha(125);
                    paint.setStyle(Paint.Style.FILL);

                    for ( int i = 0; i < mRects.size(); i++ ) {
                        path.lineTo(mRects.get(i).getX(), mRects.get(i).getY());
                    }
                    path.close();
                    canvas.drawPath(path, paint);
                }
            }
        }
    }

    public void setPoint ( float x1, float y1 ) {
        mX1 = x1;
        mY1 = y1;
        postInvalidate();
    }

    public void setRect ( List<HodooRect> rects ) {
        mRects = rects;
        postInvalidate();
    }

    private void scaleXandY () {
        double scaledWidth = this.getWidth();
        double scaledHeight = this.getHeight();
    }
}
