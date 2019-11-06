package com.ahqlab.hodooopencv.activity;

import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.ahqlab.hodooopencv.R;
import com.ahqlab.hodooopencv.adapter.CustomColorScrollView;
import com.ahqlab.hodooopencv.base.BaseActivity;
import com.ahqlab.hodooopencv.databinding.ActivityAnalsisBinding;
import com.ahqlab.hodooopencv.domain.ComburResult;
import com.ahqlab.hodooopencv.domain.HodooFindColor;
import com.ahqlab.hodooopencv.presenter.AnalysisPresenterImpl;
import com.ahqlab.hodooopencv.presenter.interfaces.AnalysisPresenter;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.io.File;
import java.util.List;

public class AnalysisActivity extends BaseActivity<AnalysisActivity> implements AnalysisPresenter.VIew {
    private ActivityAnalsisBinding binding;
    private List<HodooFindColor> colors;
    private Mat inputImg, mTransMat;
    private int litmusBoxNum = 11;
    private AnalysisPresenterImpl presenter;
    private Bitmap displayImg;
    private List<Rect> mRects;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_analsis);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView title = toolbar.findViewById(R.id.title);
        title.setText("검출 결과");

        String path = getIntent().getStringExtra("path");
        if ( path == null || path.equals("")) {
            path = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES ) + File.separator + getString( R.string.app_name ) + File.separator + "test.jpg";
        }
        presenter = new AnalysisPresenterImpl(this);
        presenter.imageProcessing(this, path);

    }

    /* Set the quality image displayed on the result screen.. */
    @Override
    public void setImage(Bitmap img) {
        binding.resultImg.setImageBitmap(img);
        displayImg = img;
    }

    @Override
    public void setProgressLayout(int state) {
        binding.progressWrap.setVisibility(state);
    }

    /* 결과화면에 참조할 리스트를 셋팅한다. Set the list to refer to the result screen*/
    @Override
    public void setColorList(List<HodooFindColor> colors, List<Rect> rects) {
        if ( colors.size() > 0 ) {
            mRects = rects;
            binding.resultImg.setRects(rects);
            presenter.requestRetrofit(this, colors);
        }
    }

    @Override
    public void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /* 결과화면에 참조할 리스트를 셋팅한다. Set the list to refer to the result screen*/
    @Override
    public void setCombur(List<ComburResult> results) {
        binding.comburList.setItem(results, new CustomColorScrollView.ColorListCallback() {
            @Override
            public void setOnItemClickListener(int position) {
                binding.resultImg.itemClick(position);
            }
        });
    }

    @Override
    public void setProgressUpdate(double value) {

    }

    @Override
    public void setPresenter(AnalysisPresenter.Precenter presenter) {

    }

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }

    @Override
    protected BaseActivity<AnalysisActivity> getActivityClass() {
        return null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home :
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
