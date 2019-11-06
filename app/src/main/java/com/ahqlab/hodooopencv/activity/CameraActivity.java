package com.ahqlab.hodooopencv.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.ahqlab.hodooopencv.R;
import com.ahqlab.hodooopencv.base.BaseActivity;
import com.ahqlab.hodooopencv.databinding.ActivityCameraBinding;
import com.ahqlab.hodooopencv.domain.HodooWrapping;
import com.ahqlab.hodooopencv.presenter.HodooCameraPresenterImpl;
import com.ahqlab.hodooopencv.presenter.interfaces.HodooCameraPresenter;
import com.ahqlab.hodooopencv.util.HodooUtil;
import com.ahqlab.hodooopencv.view.HodooJavaCamera;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static com.ahqlab.hodooopencv.constant.HodooConstant.DEBUG;
import static org.opencv.imgproc.Imgproc.MORPH_ELLIPSE;

public class CameraActivity extends BaseActivity<CameraActivity> implements CameraBridgeViewBase.CvCameraViewListener2, HodooCameraPresenter.VIew {
    private ActivityCameraBinding binding;
    private Mat mImgInput, mImgResult, mImgGray, mRgba, hovIMG, dsIMG, usIMG, cIMG, croppedMat, mTargetMat = new Mat();
    private MatOfPoint2f approxCurve;
    private HodooWrapping mWrapping;
    private HodooCameraPresenter.Precenter mPrecenter;
    private Bitmap warppingResult;
    private ImageView mImageView;
    Point point1, point2, point3, point4;
    private boolean mBlurState = false;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback( this ) {
        @Override
        public void onManagerConnected(int status) {
            super.onManagerConnected(status);
            switch ( status ) {
                case LoaderCallbackInterface.SUCCESS :
                    mImgResult = mImgGray = mRgba = dsIMG = usIMG = cIMG = hovIMG = croppedMat = new Mat();
                    approxCurve = new MatOfPoint2f();
                    binding.hodooCameraView.enableView();
                    break;
                    default:
                        super.onManagerConnected(status);
                        break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera);
        binding.setActivity(this);
        mPrecenter = new HodooCameraPresenterImpl(this);
//        binding.hodooCameraView.start();
        /* permission check (s) */
        /* permission check (e) */

        binding.hodooCameraView.setCvCameraViewListener(this);
        binding.hodooCameraView.setCameraIndex(0);

        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        int mDeviceWidth = dm.widthPixels;
        int mDeviceHeight = dm.heightPixels;

        binding.hodooCameraView.setMaxFrameSize(720, 480);
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
    }

    /**
     * 카메라의 방향 및 상태를 체크/설정한다.
     * @param width -  the width of the frames that will be delivered
     * @param height - the height of the frames that will be delivered
     */
    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.e(TAG, String.format("width : %d, height : %d", width, height));
        binding.hodooCameraView.setRotationCamera(90);
        binding.hodooCameraView.getResolutionList();
    }

    @Override
    public void onCameraViewStopped() {

    }

    /**
     * 카메라에서 프레임 값을 받아온다.
     * @param inputFrame
     * @return
     */
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mImgInput = inputFrame.rgba();
        mImgGray = inputFrame.gray();
        mImgInput.copyTo(mTargetMat);

        Imgproc.pyrDown(mImgGray, dsIMG, new Size(mImgGray.cols() / 2, mImgGray.rows() / 2));
        Imgproc.pyrUp(dsIMG, usIMG, mImgGray.size());

        mRgba = mImgGray.clone();
        Imgproc.GaussianBlur(mImgGray, mRgba, new Size(11, 11), 2);

        Imgproc.Canny(mRgba, mImgResult, 100, 150); //윤곽선만 가져오기
//        Imgproc.dilate(mImgResult, mImgResult, new Mat(), new Point(-1, -1), 1); //노이즈 제거

        Imgproc.dilate(mImgResult, mImgResult, Imgproc.getStructuringElement(MORPH_ELLIPSE, new Size(3, 3)), new Point(-1, -1), 3);
        Imgproc.erode(mImgResult, mImgResult, Imgproc.getStructuringElement(MORPH_ELLIPSE, new Size(1, 1)), new Point(-1, -1), 3);
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        cIMG = mImgResult.clone();

        Imgproc.findContours(cIMG, contours, hovIMG, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE); //윤곽선 검출

        for ( int i = 0; i < contours.size(); i++ ) {
            MatOfPoint cnt = contours.get(i);
            MatOfPoint2f curve = new MatOfPoint2f(cnt.toArray());
            Imgproc.approxPolyDP(curve, approxCurve, 0.1 * Imgproc.arcLength(curve, true), true); //다각형 검출

            Rect rect = Imgproc.boundingRect(cnt);
            Mat overlay = new Mat();
//            Mat overlay = mImgInput.clone();
            mImgInput.copyTo(overlay);
            if ( rect.width > 300 && rect.width != 1920 ) { // 일정 면적일 경우 실행
                int size = (int) approxCurve.total();
                if ( size >= 4 ) {
                        List<Point> points = new ArrayList<>();
                        mRgba = mImgInput.clone();
                        for ( int j = 0; j < approxCurve.total(); j++ ) {
                            Point point = approxCurve.toArray()[j];
                            points.add(point);
                            Imgproc.circle(overlay, point, 20, new Scalar(255, 0, 0, 0.2), 10, Core.FILLED);
                            Imgproc.putText(overlay, String.valueOf( j + 1 ), point, Core.FONT_HERSHEY_SIMPLEX, 3, new Scalar(255, 0, 0), 3);
                        }

                        point1 = approxCurve.toArray()[0];
                        point2 = approxCurve.toArray()[1];
                        point3 = approxCurve.toArray()[2];
                        point4 = approxCurve.toArray()[3];


                        points.add(point1);
                        points.add(point2);
                        points.add(point3);
                        points.add(point4);
                    mWrapping = HodooWrapping.builder().points(points).build();


                    Mat destination = new Mat(overlay.rows(), overlay.cols(), overlay.type());
                    Imgproc.drawContours(overlay, contours, i, new Scalar(255, 255, 255, 20), Core.FILLED);
                    Imgproc.drawContours(mImgInput, contours, i, new Scalar(255, 255, 255), 5);
                    Core.addWeighted(overlay, 0.5, mImgInput,0.5, 0, mImgInput);

                }
            }
        }
        if ( mBlurState ) {
            Imgproc.blur(mImgInput, mImgInput, new Size(100, 100));
        }
//        HodooUtil.compareFeature2(mImgInput);
        return mImgInput;
//        return HodooUtil.compareFeature2(mImgInput);
    }
    private static double angle(Point pt1, Point pt2, Point pt0) {
        double dx1 = pt1.x - pt0.x;
        double dy1 = pt1.y - pt0.y;
        double dx2 = pt2.x - pt0.x;
        double dy2 = pt2.y - pt0.y;
        return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
    }

    /* 클릭 이벤트 */
    public void onClick ( View v ) {
        switch ( v.getId() ) {
            case R.id.take_picture :
                    binding.hodooCameraView.takePicture(getString(R.string.app_name), "/test.jpg", new HodooJavaCamera.CameraCallback() {
                        @Override
                        public void onResult(final String fileName) {
                            if ( DEBUG ) Log.e(TAG, "fileName : " + fileName);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    HodooUtil.compareFeature(fileName);

                                    mWrapping.setFileName(fileName);
                                    mWrapping.setTr(point1);
                                    mWrapping.setTl(point2);
                                    mWrapping.setBl(point3);
                                    mWrapping.setBr(point4);
                                    mWrapping.setTarget(mTargetMat);
                                    mPrecenter.wrappingProcess(mWrapping);

                                }
                            });
                        }
                    });
                break;
            case R.id.setting_btn :
                Toast.makeText(this, "현재 환경설정을 준비중입니다.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.auto_process_btn :
                break;
        }
    }

    public native void convertRGBtoGray ( long matAddrInput, long matAddrResult );
    public native void convertBinary ( long matGray );

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }

    /**
     * 검출된 사각형에 대하여 Wrapping을 시작한다.
     * @param resultMat
     */
    @Override
    public void setWrappingImg(Bitmap resultMat) {
        Log.e(TAG, "setWrappingImg start");

        final Dialog dialog = new Dialog(this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.camera_alert_layout, null);
        dialog.setContentView(v);
        dialog.setCancelable(false);
        dialog.show();

        Display display = getWindowManager().getDefaultDisplay();
        android.graphics.Point point = new android.graphics.Point();
        display.getSize(point);

        int width = (int) (point.x * 0.8f);
        int height = (int) (point.y * 0.7f);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mBlurState = false;
            }
        });
        final ImageView pictureView = dialog.findViewById(R.id.progress_img);
        pictureView.setAdjustViewBounds(true);

        Button usePicture = v.findViewById(R.id.use_picture);
        usePicture.setText(R.string.camera_confirm);
        usePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( warppingResult != null ) {
                    mPrecenter.saveWrappingImg(CameraActivity.this, warppingResult);
                    dialog.dismiss();
                }
            }
        });
        Button retry = v.findViewById(R.id.retry);
        retry.setText(R.string.camera_cancel);
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                pictureView.setImageBitmap(null);
            }
        });
        usePicture.requestLayout();
        retry.requestLayout();

        pictureView.setImageBitmap(resultMat);
        warppingResult = resultMat;
        mBlurState = true;
        Log.e(TAG, "setWrappingImg end");
    }

    /**
     * 검출 완료된 이미지 저장한다.
     * @param state
     * @param path
     */
    @Override
    public void saveImgResult(boolean state, String path) {
        if ( state ) {
            Intent intent = new Intent(CameraActivity.this, AnalysisActivity.class);
            intent.putExtra("path", path);
            startActivity(intent);
            if ( mImageView != null ) {
                binding.maskView.setVisibility(View.GONE);
                mImageView = null;
            }
            mBlurState = false;
        } else {
            Toast.makeText(this, "사진 저장에 실패했습니다.\n잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void toast(String RectSignal) {

    }

    @Override
    public void setPresenter(HodooCameraPresenter.Precenter presenter) {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if ( DEBUG ) Log.e(TAG, String.format("orientation : %d", newConfig.orientation));
    }
    private void rotationIcon (int angle ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if ( binding.autoProcessBtn.getRotation() != angle )
                binding.autoProcessBtn.animate().rotation(angle).withLayer();
            if ( binding.settingBtn.getRotation() != angle )
                binding.settingBtn.animate().rotation(angle).withLayer();
        }
    }

    @Override
    protected BaseActivity<CameraActivity> getActivityClass() {
        return null;
    }
}
