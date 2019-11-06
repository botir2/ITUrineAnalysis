package com.ahqlab.hodooopencv.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.opencv.android.JavaCameraView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import static com.ahqlab.hodooopencv.constant.HodooConstant.DEBUG;

public class HodooJavaCamera extends JavaCameraView implements Camera.PictureCallback {
    public interface CameraCallback {
        void onResult( String fileName );
    }

    private final String TAG = HodooJavaCamera.class.getSimpleName();
    private String mPictureFileName;
    private String mFolerName;
    private CameraCallback mCameraCallback;
    public Camera camera;

    public HodooJavaCamera(Context context, int cameraId) {
        super(context, cameraId);
    }

    public HodooJavaCamera(Context context, AttributeSet attrs) {
        super(context, attrs);
        camera = mCamera;
    }

    public void setRotationCamera ( int degree ) {
        mCamera.setDisplayOrientation(degree);
    }

    public void getResolutionList() {
        if ( mCamera != null ) {
            List<Camera.Size> resolutionList = mCamera.getParameters().getSupportedPreviewSizes();
            for (Camera.Size size : resolutionList) {
                if (DEBUG)
                    Log.e(TAG, String.format("opencv parameter width : %d, height : %d", size.width, size.height));
            }
        } else {
            Log.e(TAG, "mCamera null !!!!");
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.i(TAG, "Saving a bitmap to file");
        // The camera preview was automatically stopped. Start it again.
        mCamera.startPreview();
        mCamera.setPreviewCallback(this);
        File folder = new File(mFolerName);
        if ( !folder.isDirectory() ) {
            folder.mkdirs(); //폴더 생성
        }


        // Write the image in a file (in jpeg format)
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
        }
    }

    public void setResolution(int width, int height) {
        disconnectCamera();
        connectCamera(width, height);
        enableFpsMeter();
    }

    public void takePicture(String folder, final String fileName, CameraCallback callback) {
        Log.i(TAG, "Taking picture");
        this.mPictureFileName = fileName;
        mCameraCallback = callback;
        mFolerName = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + File.separator + folder;
        mCamera.setPreviewCallback(null);
        mCamera.takePicture(null, null, this);
    }
}
