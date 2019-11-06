package com.ahqlab.hodooopencv.view;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.ahqlab.hodooopencv.domain.HodooCamera;

import java.io.IOException;

public class HodooCameraView extends SurfaceView implements SurfaceHolder.Callback {
    private Camera mCamera;
    private SurfaceHolder mHolder;
    private int mCameraDirection = Camera.CameraInfo.CAMERA_FACING_BACK;

    public HodooCameraView(Context context) {
        this(context, null);
    }

    public HodooCameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HodooCameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setCamera (HodooCamera cameraInfo) {
        mCameraDirection = cameraInfo.getCameraDirection();
    }
    public void start () {

    }
    public void stop () {

    }
    private void init () {
        mHolder = getHolder();
        mHolder.addCallback(this);
    }
    public void initCamera () {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mCamera == null)
            mCamera = Camera.open(mCameraDirection);

        Camera.Parameters params = mCamera.getParameters();
        // 카메라의 회전이 가로/세로일때 화면을 설정한다.
        if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            params.set("orientation", "portrait");
            mCamera.setDisplayOrientation(90);
            params.setRotation(90);
        } else {
            params.set("orientation", "landscape");
            mCamera.setDisplayOrientation(0);
            params.setRotation(0);
        }
        mCamera.setParameters(params);

        try {
            mCamera.setPreviewDisplay(holder);

        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
