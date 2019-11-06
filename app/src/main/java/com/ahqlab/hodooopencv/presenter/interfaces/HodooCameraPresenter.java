package com.ahqlab.hodooopencv.presenter.interfaces;

import android.content.Context;
import android.graphics.Bitmap;

import com.ahqlab.hodooopencv.base.BasePresenter;
import com.ahqlab.hodooopencv.base.BaseView;
import com.ahqlab.hodooopencv.domain.HodooWrapping;

import org.opencv.core.Mat;

public interface HodooCameraPresenter {
    interface VIew extends BaseView<Precenter> {
        void setWrappingImg(Bitmap resultMat);
        void saveImgResult(boolean state, String path);
        void toast( String RectSignal );
    }
    interface Precenter extends BasePresenter {
        void wrappingProcess(HodooWrapping wrapping);
        void saveWrappingImg(Context context, Bitmap bitmap);
    }
}
