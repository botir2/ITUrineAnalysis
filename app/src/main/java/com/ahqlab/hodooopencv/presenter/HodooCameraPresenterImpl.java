package com.ahqlab.hodooopencv.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.ahqlab.hodooopencv.R;
import com.ahqlab.hodooopencv.activity.draw.ImageUtils;
import com.ahqlab.hodooopencv.activity.draw.PerspectiveTransformation;
import com.ahqlab.hodooopencv.activity.draw.RectFinder;
import com.ahqlab.hodooopencv.domain.HodooWrapping;
import com.ahqlab.hodooopencv.presenter.interfaces.HodooCameraPresenter;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import static com.ahqlab.hodooopencv.constant.HodooConstant.DEBUG;

public class HodooCameraPresenterImpl implements HodooCameraPresenter.Precenter {
    private final String TAG = HodooCameraPresenterImpl.class.getSimpleName();
    private HodooCameraPresenter.VIew mView;
    public HodooCameraPresenterImpl (HodooCameraPresenter.VIew view) {
        mView = view; // this is mPresenter
        mView.setPresenter(this);
    }

    /************ Here is the real wrapping and save function is going on *****************/
    @Override
    public void wrappingProcess(HodooWrapping wrapping) {

        MatOfPoint2f approxCurve = new MatOfPoint2f();
        Mat inputMat = Imgcodecs.imread(wrapping.getFileName(), Imgcodecs.CV_LOAD_IMAGE_COLOR);

        Imgproc.cvtColor(inputMat, inputMat, Imgproc.COLOR_BGR2RGBA);



        if ( DEBUG ) Log.e(TAG, String.format("width : %d, height : %d", inputMat.width(), inputMat.height()));

        Mat grayMat, tempMat, contourMat, inputProcMat, outputProcMat, mTransMat, resultMat;

        grayMat = tempMat = contourMat = resultMat = new Mat();


        inputProcMat = new Mat(4, 1, CvType.CV_32FC2);
        outputProcMat = new Mat(4, 1, CvType.CV_32FC2);



        inputProcMat.put(0, 0, wrapping.getTl().x, wrapping.getTl().y, wrapping.getTr().x, wrapping.getTr().y, wrapping.getBr().x, wrapping.getBr().y, wrapping.getBl().x, wrapping.getBl().y);

        outputProcMat.put(0, 0, 0, 0, inputMat.cols() - 1, 0, inputMat.cols() - 1, inputMat.rows() - 1, 0, inputMat.rows() - 1);

        mTransMat = Imgproc.getPerspectiveTransform(inputProcMat, outputProcMat);
        Imgproc.warpPerspective(inputMat, resultMat, mTransMat, inputMat.size());

        if ( resultMat.width() > resultMat.height() ) {
            Bitmap bitmap = Bitmap.createBitmap(resultMat.cols(), resultMat.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(resultMat, bitmap);


            /*****************************************************************************************************/
            /***/
            /***/                       /****Prespective transform IMAGE CORELATION******/
            /***/
            /***/
            //  Bitmap bitmap = drawable.getBitmap();
            // Create an OpenCV mat from the bitmap.
            Mat srcMat = ImageUtils.bitmapToMat(bitmap);
            // Find the largest rectangle.
            // Find image views.
            RectFinder rectFinder = new RectFinder(0.2, 0.98);
            MatOfPoint2f rectangle = rectFinder.findRectangle(srcMat);
            // Transform the rectangle.
            if (rectangle == null){
                mView.toast("Rectangle is not detected");
                return;
            }

            PerspectiveTransformation perspective = new PerspectiveTransformation();
            Mat dstMat = perspective.transform(srcMat, rectangle);
            Bitmap resultBitmap = ImageUtils.matToBitmap(dstMat);

            mView.setWrappingImg(resultBitmap);
            return;
        }


        if ( DEBUG ) return;

//        Mat grayMat, tempMat, contourMat, inputProcMat, outputProcMat, mTransMat, resultMat;
        Imgproc.cvtColor(inputMat, inputMat, Imgproc.COLOR_BGR2RGBA);

        grayMat = tempMat = contourMat = resultMat = new Mat();
        inputMat.copyTo(grayMat);


        Imgproc.cvtColor(inputMat, grayMat, Imgproc.COLOR_BGRA2GRAY);
        Imgproc.GaussianBlur(grayMat, grayMat, new Size(11, 11), 2);
        Imgproc.Canny(grayMat, tempMat, 80, 120);
        Imgproc.dilate(tempMat, contourMat, new Mat(), new Point(-1, -1), 1);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(tempMat, contours, contourMat, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        for ( int i = 0; i < contours.size(); i++ ) {
            MatOfPoint cnt = contours.get(i);
            MatOfPoint2f curve = new MatOfPoint2f(cnt.toArray());
            Imgproc.approxPolyDP(curve, approxCurve, 0.1 * Imgproc.arcLength(curve, true), true); //다각형 검출

            Rect rect = Imgproc.boundingRect(cnt);
            if ( DEBUG ) Log.e(TAG, String.format("면적 계산 width : %d", rect.width));
            if (rect.width > 1900) { // 일정 면적일 경우 실행
                Log.e(TAG, String.format("approxCurve : %d", approxCurve.total()));
                for (int j = 0; j < approxCurve.total(); j++) {
                    if ( DEBUG ) Log.e(TAG, String.format("approxCurve = x : %f, y : %f", approxCurve.toArray()[j].x, approxCurve.toArray()[j].y));
                }
                if ( approxCurve.total() >= 4  && approxCurve.total() < 5) {
                    Point point1 = approxCurve.toArray()[0]; //오른쪽위
                    Point point2 = approxCurve.toArray()[1]; //왼쪽위
                    Point point3 = approxCurve.toArray()[2]; //왼쪽아래
                    Point point4 = approxCurve.toArray()[3]; //오른쪽아래


                    inputProcMat = new Mat(4, 1, CvType.CV_32FC2);
                    outputProcMat = new Mat(4, 1, CvType.CV_32FC2);

                    inputProcMat.put(0, 0, point2.x, point2.y, point1.x, point1.y, point4.x, point4.y, point3.x, point3.y);

                    outputProcMat.put(0, 0, 0, 0, inputMat.cols() - 1, 0, inputMat.cols() - 1, inputMat.rows() - 1, 0, inputMat.rows() - 1);


                    mTransMat = Imgproc.getPerspectiveTransform(inputProcMat, outputProcMat);
                    Imgproc.warpPerspective(inputMat, resultMat, mTransMat, inputMat.size());

                    if ( resultMat.width() > resultMat.height() ) {
                        Bitmap bitmap = Bitmap.createBitmap(resultMat.cols(), resultMat.rows(), Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(resultMat, bitmap);
                        mView.setWrappingImg(bitmap);
                        return;
                    }
                }
            }
        }

    }

    /***********************may be Save the analyzing image to folder may be***************/
    @Override
    public void saveWrappingImg(Context context, Bitmap bitmap) {
        String root = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + File.separator + context.getString(R.string.app_name);
        File myDir = new File(root);
        myDir.mkdirs();
        String fname = "test.jpg";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        Log.i("LOAD", root + fname);
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            mView.saveImgResult(true, file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            mView.saveImgResult(false, "");
        }
    }
}
