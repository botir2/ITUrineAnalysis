package com.ahqlab.hodooopencv.activity.draw;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;

import com.ahqlab.hodooopencv.R;

import org.opencv.core.Point;

import java.util.List;

public class MaskRectDrawer {
    private final String TAG = MaskRectDrawer.class.getSimpleName();

    private Paint mTransparentPaint;
    private Path mPath = new Path();

    private int rectWidth = 800;
    private int rectHeight = 1100;
    private int color = Color.WHITE;


    MaskRectDrawer ( int width, int height ) {
        initPaints();
    }

    private void initPaints() {
        mTransparentPaint = new Paint();
        mTransparentPaint.setColor(Color.TRANSPARENT);
        mTransparentPaint.setStrokeWidth(10);


    }
    public void setPoint ( List<Point> point ) {
        if ( point != null && point.size() > 0 ) {
            /* 넓이 체크 ( 구역에 포인트가 들어왔는지 체크) (s) */

            // 예제 코드 from RectDrawer (s)

//            double scale = Math.min((double)2792/1920, (double) 1440/1080);
//
//
//            double xoffset = ((double)1440-scale*(double)1080)/2;
//            double yoffset = ((double)2792-scale*(double)1920)/2 - 40;
//
////            Point leftTop = new Point(1440, 2560), rightTop = new Point(0, 2560), leftBottom = new Point(1440, 0), rightBottom = new Point(0, 0);
//            float startX = (float) (mPoint.get(0).x*scale+xoffset), startY = (float) (mPoint.get(0).y*scale+yoffset);
//            Path path = new Path();
//            path.moveTo(startX, startY);
//            for (int i = 0; i < mPoint.size(); i++) {
//                path.lineTo((float) (mPoint.get(i).x*scale+xoffset), (float) (mPoint.get(i).y*scale+yoffset));
//            }

            // 예제 코드 from RectDrawer (e)



            /* 넓이 체크 ( 구역에 포인트가 들어왔는지 체크) (e) */


            color = Color.YELLOW;
        } else {
            color = Color.WHITE;
        }
    }
    private void setColor ( int color ) {
        this.color = color;
    }

    /* 이미지를 그린다. */
    protected void draw(Canvas canvas) {
        mPath.reset();

        RectF rect = new RectF(canvas.getWidth() / 2 - rectWidth / 2, canvas.getHeight() / 2 - rectHeight / 2, (canvas.getWidth() / 2) + rectWidth  - (rectWidth / 2), (canvas.getHeight() / 2 ) +  rectHeight - (rectHeight / 2) );

        mPath.addRoundRect(rect,50, 50, Path.Direction.CW);
        mPath.setFillType(Path.FillType.INVERSE_EVEN_ODD);

        Path path = new Path();

        float x = canvas.getWidth() / 2 - rectWidth / 2, y = canvas.getHeight() / 2 - rectHeight / 2;
        path.moveTo(x + 100, y);
        path.lineTo(x, y);
        path.lineTo(x, y + 100);

        x = (canvas.getWidth() / 2) + rectWidth  - (rectWidth / 2);
        path.moveTo(x - 100, y);
        path.lineTo(x, y);
        path.lineTo(x, y + 100);


        y = (canvas.getHeight() / 2 ) +  rectHeight - (rectHeight / 2);
        path.moveTo(x, y - 100);
        path.lineTo(x, y);
        path.lineTo(x - 100, y);

        x = canvas.getWidth() / 2 - rectWidth / 2;
        path.moveTo(x, y - 100);
        path.lineTo(x, y);
        path.lineTo(x + 100, y);

        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStrokeWidth(20);
        paint.setStyle(Paint.Style.STROKE);



        paint.setPathEffect(new CornerPathEffect(50));


        canvas.drawRoundRect(rect, 50 ,50, mTransparentPaint);
        canvas.drawPath(mPath, mTransparentPaint);

        canvas.clipPath(mPath);
        canvas.drawColor(Color.parseColor("#A6000000"));
        canvas.drawPath(path, paint);

    }
}
