package com.ahqlab.hodooopencv.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.hardware.camera2.CameraDevice;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.ahqlab.hodooopencv.activity.TestCameraActivity;
import com.ahqlab.hodooopencv.activity.draw.BasicDrawer;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.Policy;
import java.util.ArrayList;
import java.util.List;

import static com.ahqlab.hodooopencv.constant.HodooConstant.DEBUG;
import static org.opencv.imgproc.Imgproc.MORPH_ELLIPSE;

public class CameraPreview extends ViewGroup implements SurfaceHolder.Callback, Camera.PreviewCallback, Camera.PictureCallback, View.OnTouchListener {

    public interface CameraCallback {
        void onResult( String fileName );
    }

    private String mPictureFileName;
    private String mFolerName;
    private CameraCallback mCameraCallback;
    private static final String TAG = CameraPreview.class.getSimpleName();

    private Context mContext;
    private int mCameraID;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;

    private Camera mCamera;
    private Camera.CameraInfo mCameraInfo;

    private List<Camera.Size> mSupportedPreviewSizes;
    private Camera.Size mPreviewSize;
    private boolean isPreview = false;

    private AppCompatActivity mActivity;
    private TestCameraActivity mTestActivity;

    private int mDisplayOrientation;

    public static int mWidth = 0;
    public static int mHeight = 0;

    private BasicDrawer mBasicDrawer;

    private Mat mYuv, mGraySubmat;

    private byte [] rgbbuffer = new byte[256 * 256];
    private int [] rgbints = new int[256 * 256];

    private boolean autoFocusState = false;

    public CameraPreview(Context context) {
        super(context);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public CameraPreview (Context context, AppCompatActivity activity, int cameraId, SurfaceView surfaceView) {
        super(context);

        mActivity = activity;
        mTestActivity = (TestCameraActivity) mActivity;
        mContext = context;
        mCameraID = cameraId;
        mSurfaceView = surfaceView;

        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mBasicDrawer = new BasicDrawer(mContext);
        mSurfaceView.setOnTouchListener(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if ( mCamera == null ) {
            try {
                mCamera = Camera.open(mCameraID);
            } catch (Exception e) {
                Log.e(TAG, "Camera " + mCameraID + " is not available: " + e.getMessage());
            }

            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(mCameraID, cameraInfo);

            mCameraInfo = cameraInfo;
            mDisplayOrientation = mActivity.getWindowManager().getDefaultDisplay().getRotation();

            int orientation = calculatePreviewOrientation(mCameraInfo, mDisplayOrientation);
            mCamera.setDisplayOrientation(orientation);



            mSupportedPreviewSizes =  mCamera.getParameters().getSupportedPreviewSizes();
            requestLayout();

            Camera.Parameters params = mCamera.getParameters();

            List<String> focusModes = params.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                mCamera.setParameters(params);
            }


            try {

                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();

                mWidth = mCamera.getParameters().getPreviewSize().width;
                mHeight = mCamera.getParameters().getPreviewSize().height;
                mPreviewSize = mCamera.getParameters().getPreviewSize();
                isPreview = true;
                Log.d(TAG, "Camera preview started.");
            } catch (IOException e) {
                Log.d(TAG, "Error setting camera preview: " + e.getMessage());
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mHolder.getSurface() == null) {
            Log.d(TAG, "Preview surface does not exist");
            return;
        }


        if ( mCamera != null ) {
            try {
                mCamera.stopPreview();
                Log.d(TAG, "Preview stopped.");
            } catch (Exception e) {
                // ignore: tried to stop a non-existent preview
                Log.d(TAG, "Error starting camera preview: " + e.getMessage());
            }

            int orientation = calculatePreviewOrientation(mCameraInfo, mDisplayOrientation);
            mCamera.setDisplayOrientation(orientation);

            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.setPreviewCallback(this);
                mCamera.startPreview();

                Camera.Parameters params = mCamera.getParameters();
                if ( DEBUG ) Log.e(TAG, String.format("preview size width : %d, height : %d", params.getPreviewSize().width, params.getPreviewSize().height));
                Log.d(TAG, "Camera preview started.");
            } catch (Exception e) {
                Log.d(TAG, "Error starting camera preview: " + e.getMessage());
            }
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
//            if (isPreview)
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            isPreview = false;
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        mWidth = width;
        mHeight = height;

        if ( DEBUG ) Log.e(TAG, String.format("onMeasure width : %d, height : %d", mWidth, mHeight));
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }
    }
    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public static int calculatePreviewOrientation(Camera.CameraInfo info, int rotation) {
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        return result;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if ( mTestActivity.mBlurState ) {

        }

        if ( mWidth != 0 && mHeight != 0 ) {
            if ( mYuv == null ) {
                mYuv = new Mat(mHeight, mWidth, CvType.CV_8UC1);
                mGraySubmat = mYuv.submat(0, mHeight, 0, mWidth);
            }
            Mat mImgInput = new Mat(mHeight + mHeight / 2, mWidth, CvType.CV_8UC3), mImgGray = new Mat(), mImgResult, mRgba, hovIMG, dsIMG, usIMG, cIMG, croppedMat, mTargetMat = new Mat(), tempMat = new Mat();
            Core.rotate(mGraySubmat, mImgInput, Core.ROTATE_90_CLOCKWISE);
            mImgInput.copyTo(tempMat);

            mYuv.put(0, 0, data);


            Imgproc.cvtColor(tempMat, mImgInput, Imgproc.COLOR_GRAY2RGB, 4);
            mTestActivity.setMap(mImgInput);
            Point point1, point2, point3, point4;
            Imgproc.cvtColor(mImgInput, mImgGray, Imgproc.COLOR_RGB2GRAY);
            dsIMG = new Mat();
            usIMG = new Mat();
            hovIMG = new Mat();
            mImgResult = new Mat();
            MatOfPoint2f approxCurve = new MatOfPoint2f();

            Imgproc.pyrDown(mImgGray, dsIMG, new org.opencv.core.Size(mImgGray.cols() / 2, mImgGray.rows() / 2));
            Imgproc.pyrUp(dsIMG, usIMG, mImgGray.size());

            mRgba = mImgGray.clone();
            Imgproc.GaussianBlur(mImgGray, mRgba, new org.opencv.core.Size(11, 11), 2);
            //50, 120
            Imgproc.Canny(mRgba, mImgResult, 100, 150); //윤곽선만 가져오기
//        Imgproc.dilate(mImgResult, mImgResult, new Mat(), new Point(-1, -1), 1); //노이즈 제거
            Imgproc.dilate(mImgResult, mImgResult, Imgproc.getStructuringElement(MORPH_ELLIPSE, new org.opencv.core.Size(3, 3)), new Point(-1, -1), 3);
            Imgproc.erode(mImgResult, mImgResult, Imgproc.getStructuringElement(MORPH_ELLIPSE, new org.opencv.core.Size(1, 1)), new Point(-1, -1), 3);

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
//                if ( DEBUG ) Log.e(TAG, String.format("width : %d, size : %d", rect.width, approxCurve.total()));
                if ( rect.width > 300 ) { // 일정 면적일 경우 실행
                    int size = (int) approxCurve.total();
                    if ( size >= 4 ) {
                        List<Point> points = new ArrayList<>();

                        point1 = approxCurve.toArray()[0];
                        point2 = approxCurve.toArray()[1];
                        point3 = approxCurve.toArray()[2];
                        point4 = approxCurve.toArray()[3];

                        List<Point> pts = new ArrayList<Point>();
                        Converters.Mat_to_vector_Point2f(approxCurve, pts);

                        Point[] pointArr = new Point[4];
                        pointArr[0] = point1;
                        pointArr[1] = point2;
                        pointArr[2] = point3;
                        pointArr[3] = point4;

                        points.add(point1);
                        points.add(point2);
                        points.add(point3);
                        points.add(point4);

                        mTestActivity.setPoint(pts);
                        return;
                    }
                }
            }
            mTestActivity.setPoint(null);
        }
    }
    public Camera.Size getCameraSize () {
        return mPreviewSize;
    }
    public void stopCamera () {
        mCamera.setPreviewCallback(null);
        mSurfaceView.getHolder().removeCallback(this);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    public void takePicture(final String folder, final String fileName, final CameraPreview.CameraCallback callback) {
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                mPictureFileName = fileName;
                mCameraCallback = callback;
                mFolerName = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES) + File.separator + folder;
                mCamera.setPreviewCallback(null);
                mCamera.takePicture(null, null, CameraPreview.this);
//                mCamera.stopPreview();
                Log.i(TAG, "Taking picture");
            }
        });
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        mCamera.stopPreview();
        File folder = new File(mFolerName);
        if ( !folder.isDirectory() ) {
            folder.mkdirs(); //폴더 생성
        }

        try {
            FileOutputStream fos = new FileOutputStream(mFolerName + mPictureFileName);

            fos.write(data);
            fos.close();
            File file = new File(mFolerName + mPictureFileName);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                final Uri contentUri = Uri.fromFile(file);
                scanIntent.setData(contentUri);
                getContext().sendBroadcast(scanIntent);
            } else {
                final Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory()));
                getContext().sendBroadcast(intent);
            }
            if ( mCameraCallback != null )
                mCameraCallback.onResult(file.getAbsolutePath());

        } catch (java.io.IOException e) {
            Log.e("PictureDemo", "Exception in photoCallback", e);
        } finally {
            mCameraCallback = null;
            mCamera.startPreview();
            mCamera.setPreviewCallback(this);
        }
    }

    @Override
    public boolean onTouch(View v, final MotionEvent event) {
        if ( DEBUG ) Log.e(TAG, String.format("action : %d", event.getAction()));
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN :
                mTestActivity.setFocusPoint(event.getX(), event.getY() );
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        Log.e(TAG, String.format("success : %b", success));
                        mTestActivity.setFocusState(success);
                    }
                });
                break;
        }
        return false;
    }
    public int getPreviewWidth () {
        return mWidth;
    }
    public int getPreviewHeight() {
        return mHeight;
    }
}
