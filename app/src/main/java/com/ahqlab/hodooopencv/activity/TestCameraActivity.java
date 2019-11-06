package com.ahqlab.hodooopencv.activity;

import android.animation.Animator;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.ahqlab.hodooopencv.R;
import com.ahqlab.hodooopencv.activity.draw.BasicDrawer;
import com.ahqlab.hodooopencv.base.BaseActivity;
import com.ahqlab.hodooopencv.databinding.LayoutBtnBinding;
import com.ahqlab.hodooopencv.databinding.TestCameraActivityBinding;
import com.ahqlab.hodooopencv.domain.HodooWrapping;
import com.ahqlab.hodooopencv.presenter.HodooCameraPresenterImpl;
import com.ahqlab.hodooopencv.presenter.interfaces.HodooCameraPresenter;
import com.ahqlab.hodooopencv.view.CameraPreview;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.List;

import static com.ahqlab.hodooopencv.constant.HodooConstant.DEBUG;

public class TestCameraActivity extends BaseActivity<TestCameraActivity> implements HodooCameraPresenter.VIew {
    TestCameraActivityBinding binding;
    LayoutBtnBinding btnBinding;

    private HodooWrapping mWrapping;
    private HodooCameraPresenter.Precenter mPrecenter;

    public static CameraPreview mCameraPreview;
    BasicDrawer mBasicDrawer;
    public static int mDeviceWidth;
    public static int mDeviceHeight;

    private List<Point> mPoints;

    private Bitmap warppingResult;
    public boolean mBlurState = false;

    private static final int CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_BACK;

    private OrientationEventListener orientEventListener;
    private boolean animState = false;
    int lastAngle = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        binding = DataBindingUtil.setContentView(this, R.layout.test_camera_activity);
        binding.setActivity(this);
        mBasicDrawer = new BasicDrawer(this);
        mPrecenter = new HodooCameraPresenterImpl(this);

        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        mDeviceWidth = dm.widthPixels;
        mDeviceHeight = dm.heightPixels;

        if ( DEBUG ) Log.e(TAG, String.format("device width : %d, height : %d", mDeviceWidth, mDeviceHeight));
        orientEventListener = new OrientationEventListener(this,
                SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                int angle = 0;
                if ( orientation > 315 || orientation < 45 ) {
                    angle = 0;
                }
                // 90
                else if(orientation >= 45 && orientation < 135) {
                    angle = 270;
                }
                // 180
                else if(orientation >= 135 && orientation < 225) {
                    angle = 180;
                }
                // 270
                else if(orientation >= 225 && orientation < 315) {
                    angle = 90;
                }
                rotationIcons(angle);
            }
        };

        addContentView(mBasicDrawer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LayoutInflater inflater = getLayoutInflater();
        btnBinding = DataBindingUtil.inflate(inflater, R.layout.layout_btn, null, false);
        btnBinding.setActivity(this);
        addContentView(btnBinding.getRoot(), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCamera();
        orientEventListener.enable();
    }

    /* Start camera preview. */
    private void startCamera () {
        mCameraPreview = new CameraPreview(this, this, CAMERA_FACING, binding.cameraPreview);
    }
    /* 카메라 프리뷰를 중지한다. */
    private void stopCamera() {
        mCameraPreview.stopCamera();
    }

    /* Set the resulting Mat. gives to camera preview image result set */
    public void setMap (Mat resultMat) {
        Bitmap bitmap = Bitmap.createBitmap(resultMat.cols(), resultMat.rows(), Bitmap.Config.ARGB_8888 );
        Utils.matToBitmap(resultMat, bitmap);
    }

    /* Set bitmap. */
    public void setBitmap ( Bitmap bitmap ) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if ( bitmap != null ) {
                Bitmap blurBitmap = blur(this, bitmap);
                binding.imgPreview.setImageBitmap(blurBitmap);
            }
        }

    }

    /* Update the image drawn over the camera preview.. */
    public void updateView () {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M)
            mBasicDrawer.postInvalidate();
        else
            mBasicDrawer.invalidate();
    }

    /* Set the point to use in the result. */
    public void setPoint (List<Point> point) {
        mBasicDrawer.setPoint(point);
        mPoints = point;
        mWrapping = HodooWrapping.builder().points(point).build();
        updateView();
    }

    /* Update the state of the image drawn over the camera preview. GIVE TO RESULT TO CAMERA PREVIEW */
    public void setFocusPoint ( float x, float y ) {
        mBasicDrawer.setFocusPoint(x, y);
        updateView();
    }

    /* Check the state of the image drawn over the camera preview. */
    public void setFocusState ( boolean state ) {
        mBasicDrawer.setFocusState(state);
        updateView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopCamera();
        orientEventListener.disable();
    }

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }

    /***************************Camera shot teake camera result picture *********************************/
    public void onTakePictureClick (View v) {
        mCameraPreview.takePicture(getString(R.string.app_name), "/test.jpg", new CameraPreview.CameraCallback() {
            @Override
            public void onResult(String fileName) {
                mWrapping.setFileName(fileName);
                mWrapping.setTl( new Point(1550, 700) );
                mWrapping.setTr( new Point(3800, 700) );
                mWrapping.setBl( new Point(1550, 2300) );
                mWrapping.setBr( new Point(3800, 2300) );
                mPrecenter.wrappingProcess(mWrapping);
            }
        });
    }

    /* Wrapping for camera photos Wrapping  from here can see result of camera */
    @Override
    public void setWrappingImg(final Bitmap resultMat) {
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

        /*****************************USed this picture image from camera result. *************************/
        Button usePicture = v.findViewById(R.id.use_picture);
        usePicture.setText(R.string.camera_confirm);
        usePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( warppingResult != null ) {
                    mPrecenter.saveWrappingImg(TestCameraActivity.this, resultMat);
                    dialog.dismiss();
                }
            }
        });

        /*****************************USed this picture image from camera result. *************************/
        Button retry = v.findViewById(R.id.retry);
        retry.setText(R.string.camera_cancel);
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                pictureView.setImageBitmap(null);
                binding.overlay.setVisibility(View.GONE);
            }
        });
        usePicture.requestLayout();
        retry.requestLayout();

        /*************************** Load to show image description function **************************/
        pictureView.setImageBitmap(resultMat);
        warppingResult = resultMat;
//        binding.overlay.setVisibility(View.VISIBLE);
        mBlurState = true;
        Log.e(TAG, "setWrappingImg end");
    }

    /* Save the resulting image. */
    @Override
    public void saveImgResult(boolean state, String path) {
        if ( state ) {
            Intent intent = new Intent(TestCameraActivity.this, AnalysisActivity.class);
            intent.putExtra("path", path);
            startActivity(intent);
            binding.overlay.setVisibility(View.GONE);
            mBlurState = false;
        } else {
            Toast.makeText(this, "사진 저장에 실패했습니다.\n잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void toast(String RectSignal) {
        Toast.makeText(this, RectSignal, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setPresenter(HodooCameraPresenter.Precenter presenter) {

    }
    private static final float BITMAP_SCALE = 0.4f;
    private static final float BLUR_RADIUS = 7.5f;


    /* Save the resulting image. whi is used is have not detected yet */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static Bitmap blur(Context context, Bitmap image) {
        int width = Math.round(image.getWidth() * BITMAP_SCALE);
        int height = Math.round(image.getHeight() * BITMAP_SCALE);

        Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
        theIntrinsic.setRadius(BLUR_RADIUS);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);

        return outputBitmap;
    }

    private void rotationIcons ( int angle ) {
        if ( lastAngle != angle ) {
        }
    }

    /* Get the size of the device. */
    public static double getScale () {
        return Math.min( (double) mDeviceHeight / mCameraPreview.getPreviewWidth(), (double) mDeviceWidth / mCameraPreview.getPreviewHeight() );
    }

    @Override
    protected BaseActivity<TestCameraActivity> getActivityClass() {
        return null;
    }
}
