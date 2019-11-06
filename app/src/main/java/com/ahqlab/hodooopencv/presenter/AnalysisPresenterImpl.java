package com.ahqlab.hodooopencv.presenter;

/*
 * 2018.11.15 AHQLab
 * 제작 : 송석우
 *
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.ahqlab.hodooopencv.R;
import com.ahqlab.hodooopencv.constant.HodooConstant;
import com.ahqlab.hodooopencv.domain.ComburResult;
import com.ahqlab.hodooopencv.domain.HodooFindColor;
import com.ahqlab.hodooopencv.domain.HsvValue;
import com.ahqlab.hodooopencv.http.HodooRetrofit;
import com.ahqlab.hodooopencv.http.service.RetrofitService;
import com.ahqlab.hodooopencv.presenter.interfaces.AnalysisPresenter;
import com.ahqlab.hodooopencv.util.HodooUtil;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.ahqlab.hodooopencv.constant.HodooConstant.DEBUG;
import static org.opencv.imgproc.Imgproc.MORPH_ELLIPSE;

public class AnalysisPresenterImpl implements AnalysisPresenter.Precenter {
    private final static String TAG = AnalysisPresenterImpl.class.getSimpleName();
    private AnalysisPresenter.VIew mView;
    private Mat originalMat;
    private int result = 0;

    public AnalysisPresenterImpl ( AnalysisPresenter.VIew view ) {
        mView = view;
        mView.setPresenter(this);
    }
    @Override
    public void imageProcessing(Context context, String path) {
        Mat readMat = readMatImg(path);
        Imgproc.cvtColor(readMat, readMat, Imgproc.COLOR_BGR2RGB);
        mView.setImage(convertMatToBitmap(readMat));
        OpenCVAsync async = new OpenCVAsync(context);
        async.execute(readMat);
    }

    private class OpenCVAsync extends AsyncTask<Mat, Double, Mat> {
        private List<HodooFindColor> colors;
        private int litmusBoxNum = 11;
        private Context mContext;
        private List<Rect> rectList;

        OpenCVAsync ( Context context ) {
            mContext = context;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mView.setProgressLayout(View.VISIBLE);
            colors = new ArrayList<>();
        }

        @Override
        protected void onProgressUpdate(Double... values) {
            super.onProgressUpdate(values);
//            if ( DEBUG ) Log.e(TAG, String.format("values : %f", values));
        }

        @Override
        protected Mat doInBackground(Mat... mats) {

            Mat inputMat = mats[0];

            originalMat= new Mat();
            inputMat.copyTo(originalMat);
            //Find feature points for an image (s)
            int feature = HodooUtil.compareFeature(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES) + File.separator + "HodooOpenCV" + File.separator + "test.jpg");
//
            if ( DEBUG ) Log.e(TAG, String.format("feature : %d", feature));

            if ( feature > 0 ) {
                if ( DEBUG ) Log.e(TAG, "Tow images are same.");
            } else {
                if ( DEBUG ) Log.e(TAG, "Tow images are different.");
                result = HodooConstant.NOT_MATCH;
                return originalMat;
            }

            String target = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES) + File.separator + "HodooOpenCV" + File.separator + "target.jpg";


            //Find feature points for an image (e)

            Mat grayMat, resultMat, cannyMat, downMat, upMat, contourMat, hovIMG, tempMat;
            grayMat = cannyMat = downMat = upMat = hovIMG = tempMat = new Mat();

            int threshold = 220;
            MatOfPoint2f approxCurve = new MatOfPoint2f();

            /* Image brightness adjustment (s) */
            List<Mat> hlsChannels = new ArrayList<>();
            Mat lignt = new Mat();
            tempMat = new Mat();

            /****Here is the inputMat has real image***************************/
            Imgproc.cvtColor(inputMat, tempMat, Imgproc.COLOR_RGB2HLS);
            Core.split(tempMat, hlsChannels);
            Core.add(hlsChannels.get(1), new Scalar(-30), lignt);
            hlsChannels.set(1, lignt);
            Core.merge(hlsChannels, tempMat);
            Imgproc.cvtColor(tempMat, tempMat, Imgproc.COLOR_HLS2RGB);
            Imgproc.cvtColor(tempMat, tempMat, Imgproc.COLOR_RGB2RGBA);
            Imgproc.cvtColor(tempMat, tempMat, Imgproc.COLOR_RGBA2RGB);
            tempMat.convertTo(tempMat, -1, 2, -100);
            /* Image brightness adjustment (e) */

            /* Find image outline (s) */
            Imgproc.cvtColor(tempMat, grayMat, Imgproc.COLOR_RGB2GRAY);
            Imgproc.pyrDown(grayMat, downMat, new Size(inputMat.cols() / 2, inputMat.rows() / 2));
            Imgproc.pyrUp(downMat, upMat, inputMat.size());
            Imgproc.Canny(upMat, cannyMat, 20, threshold);
            Imgproc.dilate(cannyMat, cannyMat, new Mat(), new Point(1, 1), 2);

            contourMat = cannyMat.clone();
            List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
            Imgproc.findContours(contourMat, contours, hovIMG, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            /* Find image outline(e) */

            for (int i = 0; i < contours.size(); i++) {
                MatOfPoint cnt = contours.get(i);
                MatOfPoint2f curve = new MatOfPoint2f(cnt.toArray());
                Imgproc.approxPolyDP(curve, approxCurve, 0.02 * Imgproc.arcLength(curve, true), true);
                int numberVertices = (int) approxCurve.total();

                if ( approxCurve.total() >= 3 && approxCurve.total() < 5  ) {
                    int topMargin = 0, bottomMargin = 0, height = inputMat.height(), rectHeight = 0;
                    double topY = 8000, bottomY = 0, topRightX = 0, topLeftX = 800, topLeftY = 0;
                    for (int j = 0; j < numberVertices; j++) {
                        Point point = approxCurve.toArray()[j];
                        if ( topY > point.y )
                            topY = point.y;
                        if ( topRightX < point.x) {
                            topRightX = point.x;
                        }
                        if ( topLeftX > point.x ) {
                            topLeftX = point.x;
                            topLeftY = point.y;
                        }
                        if ( bottomY < point.y )
                            bottomY = point.y;
                    }

                    /* Calculate points for angle calculation (s) */
                    Point leftBottom = approxCurve.toArray()[0];
                    Point rightBottom = approxCurve.toArray()[0];
                    Point top = approxCurve.toArray()[0];
                    Point temp = null;

                    Point t1 = new Point( 8000, 8000 );
                    Point t2 = new Point( 8000, 8000 );
                    Point b1 = new Point(0, 0);
                    Point b2 = new Point(0, 0);

                    Point tl, tr, bl, br;
                    for (int j = 0; j < numberVertices; j++) {
                        Point point = approxCurve.toArray()[j];
                        Point tempPoint = null;
                        if ( t1.y > point.y ) {
                            tempPoint = t1;
                            t1 = point;
                        }
                        if ( t2.y > point.y ) {
                            if ( t1 != point )
                                t2 = point;
                            else
                                t2 = tempPoint;
                        }
                        if ( b1.y < point.y ) {
                            tempPoint = b1;
                            b1 = point;
                        }

                        if ( b2.y < point.y ) {
                            if ( b1 != point )
                                b2 = point;
                            else
                                b2 = tempPoint;
                        }
                    }
                    if ( t2.x > t1.x ) {
                        tr = t2;
                        tl = t1;
                    } else {
                        tl = t2;
                        tr = t1;
                    }
                    if ( b2.x > b1.x ) {
                        br = b2;
                        bl = b1;
                    } else {
                        bl = b2;
                        br = b1;
                    }

                    if ( DEBUG ) Log.e(TAG, String.format("numberVertices : %d", numberVertices));

                    for (int j = 0; j < numberVertices - 1; j++) {
                        Point point = approxCurve.toArray()[j];
                        Point cPoint = approxCurve.toArray()[j + 1];
                        if ( temp == null )
                            temp = point;

                        if ( point.x > cPoint.x && point.y < cPoint.y ) {
                            leftBottom = cPoint;
                        }
                        if ( point.x < cPoint.x || point.y < cPoint.y ) {
                            rightBottom = cPoint;
                        }
                        if ( point.y > cPoint.y )
                            top = cPoint;
                        if ( DEBUG ) Log.e(TAG, String.format("position : %d, x : %f, y : %f, numberVertices : %d", j, point.x, point.y, numberVertices));

                    }
                    /* Calculate points for angle calculation (e) */

                    int bottom;
                    if ( bl.y < br.y )
                        bottom = (int) br.y;
                    else
                        bottom = (int) bl.y;
                    if ( tl.y < tr.y )
                        topMargin = (int) tl.y;
                    else
                        topMargin = (int) tr.y;

                    bottomMargin = height - bottom;
                    rectHeight = (int) Math.abs(height - topMargin - bottomMargin);
                    Mat rotationMat = inputMat.clone();
                    double angle = getAngle(leftBottom, rightBottom);
                    if ( rectHeight < 370 ) {
                        topY = 1240;
                        rectHeight = 400;
                        angle  = -1.2f;
                    }

                    if ( DEBUG ) Log.e(TAG, String.format("angle : %f", angle));
                    /* Calculate Square Frame Slope and Apply Rotation (s) */
                    if ( Math.abs(angle) > 10 )
                        angle = -0.7;

                    if ( DEBUG ) Log.e(TAG, String.format("angle : %f", angle));

                    Mat rotation = Imgproc.getRotationMatrix2D(new Point(rotationMat.width() / 2, rotationMat.height() / 2), angle, 1);
                    Imgproc.warpAffine(rotationMat, rotationMat, rotation, new Size(rotationMat.cols(), rotationMat.rows()));
                    /* Calculate Square Frame Slope and Apply Rotation (s) */
                    Imgproc.circle(originalMat, new Point(topRightX, topY), 50, new Scalar(0, 0, 255), 20);

                    Mat crop;
                    tempMat = new Mat();

                    if ( DEBUG ) Log.e(TAG, String.format("topY : %f, bottom : %f", topY, topY + rectHeight));

                    Rect roi = new Rect((int) topLeftX, (int) topY, (int) (topRightX - topLeftX ), rectHeight);



                    if ( DEBUG ) Log.e(TAG, String.format("crop point = topY : %.0f, %d", topY, rectHeight));
                        crop = rotationMat.submat(roi);
                        tempMat = crop.clone();
                        break;
                }
            }



            Mat roi = tempMat.clone();
            Mat cloneMat = tempMat.clone();
            resultMat = cloneMat.clone();

            if ( roi.height() < 150 )
                return null;

            Imgproc.cvtColor(tempMat, roi, Imgproc.COLOR_RGB2HSV);
            Imgproc.erode(tempMat, tempMat, Imgproc.getStructuringElement(MORPH_ELLIPSE, new Size(2, 2)), new Point(-1, -1), 3);
            Core.inRange(roi, new Scalar(15, 15, 15), new Scalar(200, 255, 255), tempMat);
            Imgproc.Canny(tempMat, tempMat, 50, 255);
            Imgproc.dilate(tempMat, tempMat, Imgproc.getStructuringElement(MORPH_ELLIPSE, new Size(4, 4)), new Point(-1, -1), 3);
            Imgproc.erode(tempMat, tempMat, Imgproc.getStructuringElement(MORPH_ELLIPSE, new Size(2, 2)), new Point(-1, -1), 3);

            contours = new ArrayList<>();
            approxCurve = new MatOfPoint2f();
            Imgproc.findContours(tempMat, contours, hovIMG, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

            List<Integer> widthList = new ArrayList<>();
            List<Rect> rects = new ArrayList<>();
            for ( int i = 0; i < contours.size(); i++ ) {
                MatOfPoint cnt = contours.get(i);
                MatOfPoint2f curve = new MatOfPoint2f(cnt.toArray());
                Rect rect = Imgproc.boundingRect(cnt);
                if (rect.width < 30) continue;
                Imgproc.approxPolyDP(curve, approxCurve, 0.04 * Imgproc.arcLength(curve, true), true); //Polygon detection

                if ( approxCurve.total() >= 4 ) {
                    if ( rect.width >= 300 && rect.width < 400) {
                        rects.add(rect);
                    }
                }
            }

            List<Rect> newRect = new ArrayList<>();


            //Overall width
            int matWidth = roi.width();
            //Full height
            int matHeight = roi.height();

            int litmusWidth = (matWidth - 180) / litmusBoxNum,
                litmusSpacing = 100;


            int width = 200, margin = 0, xPosition = 200, yPosition = resultMat.height() / 2 - 70, height = 200, marginVal = 225;
            int marginValue = 50, rectWidth = ( matWidth - (100 * 2) ) / litmusBoxNum - marginValue, rectY = (matHeight / 2) - (rectWidth / 2), rectHeight = rectWidth;

            // litmusBoxNum are 11 rectangles count

            xPosition = 150;

            for (int i = 0; i < litmusBoxNum; i++) {
                newRect.add( new Rect(xPosition + margin, rectY, rectWidth, rectHeight) );
                margin += rectWidth + marginValue;
            }

            rects = rectList = newRect;

            for ( int i = 0; i < rects.size(); i++ ) {
                int startX = rects.get(i).x + 120, startY = rects.get(i).y + 120;
                int endX = rects.get(i).x + rects.get(i).width - 120, endY = rects.get(i).y + rects.get(i).height - 120;
                Point a = new Point(startX, startY);
                Point b = new Point(endX, endY);

                int colorCount = 0;
                float hAverageTotal = 0;
                float sAverageTotal = 0;
                float vAverageTotal = 0;

                float hAverage = 0;
                float sAverage = 0;
                float vAverage = 0;
                Imgproc.cvtColor(cloneMat, cloneMat, Imgproc.COLOR_RGB2BGR);
                Mat hsvMat = new Mat();
                cloneMat.copyTo(hsvMat);
                Imgproc.cvtColor(hsvMat, hsvMat, Imgproc.COLOR_BGR2HSV);
//                if ( DEBUG ) return hsvMat;
                /* To find the average for loop (s) */

                for (int j = (int) a.y; j < b.y; j++) {
                    for (int k = (int) a.x; k < b.x; k++) {
                        double[] color = cloneMat.get(j, k);
                        if ( color != null ) {
                            float[] hsv = new float[3];
                            hsv[0] = (float) color[0];
                            hsv[1] = (float) color[1];
                            hsv[2] = (float) color[2];

                            hAverageTotal += hsv[0];
                            sAverageTotal += hsv[1];
                            vAverageTotal += hsv[2];
                        }
                        colorCount++;
                    }
                }
                hAverage = hAverageTotal / colorCount;
                sAverage = sAverageTotal / colorCount;
                vAverage = vAverageTotal / colorCount;

                float[] hsv = new float[3];
                hsv[0] = hAverage;
                hsv[1] = sAverage;
                hsv[2] = vAverage;

                /******finding HSV color from here******/

                HodooFindColor findColor = HodooFindColor.builder().index(i + 1).hsv(hsv).build();
                colors.add(findColor);

                if ( Float.isNaN(hAverage) || Float.isNaN(sAverage) || Float.isNaN(vAverage) ) {
                    result = HodooConstant.RECT_NOT_FOUND;
                    return null;
                }


                if ( DEBUG ) Log.e(TAG, String.format("%d The value HSV are = H : %f, S : %f, V : %f", i, hAverage, sAverage, vAverage));
                /* To find the average for loop (e) */
            }
            result = HodooConstant.SUCCESS;
            return resultMat;
        }

        @Override
        protected void onPostExecute(Mat mat) {
            super.onPostExecute(mat);
            mView.setProgressLayout(View.GONE);
            mView.setColorList(colors, rectList);
            setImg(mat);
        }
    }

    @Override
    public void setImg(Mat inputMat) {

        switch ( result ) {
            case HodooConstant.NOT_MATCH :
                inputMat = originalMat.clone();
                mView.toast("Is not Hodo product.\n호두 제품으로 다시 시도해주세요.");
                break;
            case HodooConstant.RECT_NOT_FOUND :
                inputMat = originalMat.clone();
                mView.toast("사각형이 검출되지 않았습니다.\n 사진을 다시 찍어주세요.");
                break;
            case HodooConstant.LITMUS_NOT_FOUNT :
                inputMat = originalMat.clone();
                break;
            case HodooConstant.SUCCESS :
                break;
        }
        mView.setImage( convertMatToBitmap(inputMat) );
    }

    @Override
    public void requestRetrofit(final Context context, List<HodooFindColor> colors) {
        List<String> values = new ArrayList<>();
        for (int i = 0; i < colors.size(); i++) {
            float[] hsv = colors.get(i).getHsv();
            StringBuffer sb = new StringBuffer();
            for (int j = 0; j < hsv.length; j++) {
                if ( j == 0 )
                    sb.append(String.format("%.0f", Math.floor(Math.abs(colors.get(i).getHsv()[j]))));
                else
                    sb.append(String.format("%.0f", Math.floor(Math.abs(colors.get(i).getHsv()[j] * 100))));
                if ( j != hsv.length - 1 )
                    sb.append("/");
            }
            values.add(sb.toString());
        }
        if ( values.size() >= 11 ) {
            HsvValue hsvValue = new HsvValue();
            hsvValue.setSg(values.get(0));
            hsvValue.setPh(values.get(1));
            hsvValue.setLeu(values.get(2));
            hsvValue.setNit(values.get(3));
            hsvValue.setPro(values.get(4));
            hsvValue.setGlu(values.get(5));
            hsvValue.setKet(values.get(6));
            hsvValue.setUbg(values.get(7));
            hsvValue.setBil(values.get(8));
            hsvValue.setEry(values.get(9));
            hsvValue.setHb(values.get(10));

            HodooRetrofit retrofit = new HodooRetrofit(context);
            RetrofitService service = retrofit.getRetrofit().create(RetrofitService.class);
            retrofit.request(service.getHsv(HodooRetrofit.getUrl(context, R.string.color_detector), hsvValue), new HodooRetrofit.HodooCallback() {
                @Override
                public <T> void onResponse(Response<T> response) {
                    if ( DEBUG ) Log.e(TAG, "debug check");

                    HsvValue hsv = (HsvValue) response.body();

                    if ( hsv != null ) {
                        int[] msgResource = {
                                R.array.sg_str_arr,
                                R.array.ph_str_arr,
                                R.array.leu_str_arr,
                                R.array.nit_str_arr,
                                R.array.pro_str_arr,
                                R.array.glu_str_arr,
                                R.array.ket_str_arr,
                                R.array.ubg_str_arr,
                                R.array.bil_str_arr,
                                R.array.ery_str_arr,
                                R.array.hb_str_arr
                        };
                        String[] name = {
                                "SG",
                                "pH",
                                "LEU",
                                "NIT",
                                "PRO",
                                "GLU",
                                "KET",
                                "UBG",
                                "BIL",
                                "ERY",
                                "Hb"
                        };
                        List<ComburResult> results = new ArrayList<>();
                        String[] arr = hsv.toArray();
                        for (int i = 0; i < arr.length; i++) {
                            int index = Integer.parseInt(arr[i]);
                            int position = 0;
                            int count = 0;
                            String[] msg = context.getResources().getStringArray(msgResource[i]);
                            if ( i == 0 ) {
                                if ( index < 2 )
                                    position = 0;
                                else if ( index >= 2 && index < 5 )
                                    position = 1;
                                else
                                    position = 2;
                            } else if ( i == 1 ) {
                                if ( index < 1 )
                                    position = 0;
                                else if ( index >= 1 && index < 4 )
                                    position = 1;
                                else
                                    position = 2;
                            } else if ( i == 3 ) {
                                if ( index < 1 )
                                    position = 0;
                                else
                                    position = 1;
                            }  else {
                                if ( index < 2 )
                                    position = 0;
                                else if ( index == 2 )
                                    position = 1;
                                else
                                    position = 2;
                            }
                            ComburResult result = ComburResult.builder()
                                    .comburTitle(name[i])
                                    .resultMsg(msg[position])
                                    .position(i)
                                    .resultPosition( Integer.parseInt(arr[i]) )
                                    .imgPreStr(name[i].toLowerCase())
                                    .resultPosition(Integer.parseInt(arr[i]))
                                    .build();
                            result.imgSetting(context);
                            results.add(result);
                        }
                        mView.setCombur(results);
                    } else {
                        Log.e(TAG, "server error");
                    }
                }

                @Override
                public void onFailure(String error) {
//                    Log.e(TAG, error);
                }
            });
        }
    }

    private Mat readMatImg ( String path ) {
        Mat readMat = Imgcodecs.imread(path);
        return readMat;
    }
    private Bitmap convertMatToBitmap ( Mat bitmapMat ) {
        Bitmap bitmap = Bitmap.createBitmap( bitmapMat.cols(), bitmapMat.rows(), Bitmap.Config.ARGB_8888 );
        Utils.matToBitmap(bitmapMat, bitmap);
        return bitmap;
    }
    private Mat setHLS( Mat inputMat, int... hls ) {
        int max = hls.length;
        Mat resultMat = new Mat();

        List<Mat> hlsChannels = new ArrayList<>();
        Mat[] mats = new Mat[max];
        Imgproc.cvtColor(inputMat, resultMat, Imgproc.COLOR_RGB2HLS);
        Core.split(resultMat, hlsChannels);
        for (int i = 0; i < max; i++) {
            Core.add(hlsChannels.get(i), new Scalar(hls[i]), mats[i]);
            hlsChannels.set(i, mats[i]);
        }
        Core.merge(hlsChannels, resultMat);
        Imgproc.cvtColor(resultMat, resultMat, Imgproc.COLOR_HLS2RGB);
        Imgproc.cvtColor(resultMat, resultMat, Imgproc.COLOR_RGB2RGBA);
        Imgproc.cvtColor(resultMat, resultMat, Imgproc.COLOR_RGBA2RGB);
        return resultMat;
    }

    // Calculate the left bo0tton and right botton point from two points
    public double getAngle(Point start, Point end) {
        int dx = (int) (end.x - start.x);
        int dy = (int) (end.y - start.y);

        double rad= Math.atan2(dx, dy);
        double degree = (rad*180)/Math.PI;
        return 90 - degree;
    }


    public List<Rect> addRects( List<Rect> rects, List<Rect> newRect ) {
        List<Rect> tempRects = rects;
        rects = newRect;
        for (int i = 0; i < tempRects.size(); i++) {
            rects.add(tempRects.get(i));
        }
        return rects;
    }
    private List<Rect> sortRect ( List<Rect> rects ) {
        for ( int i = 0; i < rects.size() - 1; i++ ) {
            for ( int j = i + 1; j < rects.size(); j++ ) {
                if ( rects.get(i).x > rects.get(j).x ) {
                    Rect tempRect = rects.get(i);
                    rects.set(i, rects.get(j));
                    rects.set(j, tempRect);
                }
            }
        }
        return rects;
    }
}
