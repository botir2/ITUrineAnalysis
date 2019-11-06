package com.ahqlab.hodooopencv.activity;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ahqlab.hodooopencv.R;
import com.ahqlab.hodooopencv.base.BaseActivity;
import com.ahqlab.hodooopencv.databinding.ActivityTestBinding;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
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
import java.util.ArrayList;
import java.util.List;

import static com.ahqlab.hodooopencv.constant.HodooConstant.DEBUG;

public class TestActivity extends BaseActivity<TestActivity> {
    private final String TAG = TestActivity.class.getSimpleName();
    private ActivityTestBinding binding;

    Mat mImage, mInputMat, mOutputMat, mTemp, mGrayMat, mImgResult, hovIMG, mImgInput, mTransMat, mWrapMat, mWrapTemp;
    private MatOfPoint2f approxCurve;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_test);

        final String fileName = "test.jpg";
        File root = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + File.separator + getString(R.string.app_name) + File.separator + fileName);
        mOutputMat = mInputMat = mTemp = mImgResult = hovIMG = mImgInput = mTransMat = mWrapMat = mWrapTemp = new Mat();
//        contourProcessing(mImgResult.getNativeObjAddr(), fileName);

        Mat lambda = new Mat(2, 4, CvType.CV_32FC1);

        mImage = Imgcodecs.imread( root.getAbsolutePath(),Imgcodecs.CV_LOAD_IMAGE_COLOR);
        Imgproc.cvtColor(mImage, mImage, Imgproc.COLOR_BGR2RGBA);

        approxCurve = new MatOfPoint2f();

        mGrayMat = mImage.clone();
        Imgproc.cvtColor(mImage, mGrayMat, Imgproc.COLOR_BGRA2GRAY);
        Imgproc.GaussianBlur(mGrayMat, mGrayMat, new Size(11, 11), 2);
        Imgproc.Canny(mGrayMat, mImgResult, 90, 120);
        Imgproc.dilate(mImgResult, mImgResult, new Mat(), new Point(-1, -1), 1);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        mTemp = mImgResult.clone();

        Imgproc.findContours(mTemp, contours, hovIMG, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        for ( int i = 0; i < contours.size(); i++ ) {
            MatOfPoint cnt = contours.get(i);
            MatOfPoint2f curve = new MatOfPoint2f(cnt.toArray());
            Imgproc.approxPolyDP(curve, approxCurve, 0.1 * Imgproc.arcLength(curve, true), true); //다각형 검출


            Rect rect = Imgproc.boundingRect(cnt);
            if (rect.width > 300) { // 일정 면적일 경우 실행
//                Imgproc.drawContours(mImage, contours, -1, new Scalar(255, 255, 255), Core.FILLED);
//                mRgba = mImgInput.clone();

                Point point1 = approxCurve.toArray()[0]; //오른쪽위
                Point point2 = approxCurve.toArray()[1]; //왼쪽위
                Point point3 = approxCurve.toArray()[2]; //왼쪽아래
                Point point4 = approxCurve.toArray()[3]; //오른쪽아래

                Point newPoint1 = new Point(0, mImage.width() - (point2.x + point1.x));
                Point newPoint2 = new Point(0, mImage.width() - (point2.x + point1.x));


                Rect box = new Rect(point2, point4);


                mInputMat = new Mat(4, 1, CvType.CV_32FC2);
                mOutputMat = new Mat(4, 1, CvType.CV_32FC2);

                //Top left, top right, bottom right, bottom left
                mInputMat.put(0, 0, point2.x, point2.y, point1.x, point1.y, point4.x, point4.y, point3.x, point3.y);
                mOutputMat.put(0, 0, 0, 0, mImage.cols() - 1, 0, mImage.cols() - 1, mImage.rows() - 1, 0, mImage.rows() - 1);


                mWrapTemp = mImage.clone();
                mTransMat = Imgproc.getPerspectiveTransform(mInputMat, mOutputMat);
                if ( DEBUG ) Log.e(TAG, String.format("mImage.width() : %d", mTemp.width()));
                Imgproc.warpPerspective(mImage, mTemp, mTransMat, mTemp.size());
                if ( DEBUG ) Log.e(TAG, String.format("mImage.width() : %d", mTemp.width()));

//                for ( int j = 0; j < approxCurve.total(); j++ ) {
//                    Point point = approxCurve.toArray()[j];
////                    points.add(point);
//                    Imgproc.circle(mImage, point, 50, new Scalar(255, 0, 0), Core.FILLED);
//                    Imgproc.putText(mImage, String.valueOf( j + 1 ), point, Core.FONT_HERSHEY_SIMPLEX, 3, new Scalar(255, 0, 0), 3);
//
//
//                }
            }
        }
//
//        if ( DEBUG ) Log.e(TAG, String.format("contours size : %d", contours.size()));


//
//

        Mat reProcess = findContour(mTemp);
        Bitmap bitmap = Bitmap.createBitmap( reProcess.cols(), reProcess.rows(), Bitmap.Config.ARGB_8888 );
        Utils.matToBitmap(reProcess, bitmap);
        binding.imgWrap.setImageBitmap(bitmap);
    }

    private Mat findContour ( Mat target ) {
        Mat resultMat;
        resultMat = target.clone();
        return resultMat;
//        Imgproc.cvtColor(target, target, Imgproc.COLOR_RGB2BGR);
//        Imgproc.cvtColor(target, resultMat, Imgproc.COLOR_BGRA2GRAY);
////        Imgproc.threshold(resultMat, resultMat, 20, 255, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);
//        Imgproc.Canny(resultMat, resultMat, 90, 120);
//
//        return resultMat;
    }

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }
    public native void contourProcessing( long matResultAddr, String fileName );

    @Override
    protected BaseActivity<TestActivity> getActivityClass() {
        return null;
    }
}
