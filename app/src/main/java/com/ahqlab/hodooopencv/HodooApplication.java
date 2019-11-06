package com.ahqlab.hodooopencv;

import android.app.Application;
import android.util.Log;

import static com.ahqlab.hodooopencv.BuildConfig.DEBUG;

public class HodooApplication extends Application {
    private final String TAG = HodooApplication.class.getSimpleName();
    @Override
    public void onCreate() {
        super.onCreate();
        if ( DEBUG ) Log.e(TAG, "application class");
    }

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }
}
