package com.ahqlab.hodooopencv.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.ahqlab.hodooopencv.R;
import com.ahqlab.hodooopencv.base.BaseActivity;
import com.ahqlab.hodooopencv.constant.HodooConstant;
import com.ahqlab.hodooopencv.databinding.ActivitySplashBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.ahqlab.hodooopencv.constant.HodooConstant.DEBUG;

public class Splash extends BaseActivity<Splash> {
    private ActivitySplashBinding binding;
    private int PERMISSION_REQUEST_CODE = 100;
    private final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
        permissionCheck();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        int autoProcess = pref.getInt(HodooConstant.AUTO_PROCESS_KEY, 0);
    }
    /* 퍼미션 체크를 해준다. */
    private void permissionCheck () {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            for ( int i = 0; i < PERMISSIONS.length; i++ )
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSIONS[i]))
                    ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);
                else
                    ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    /* 퍼미션 체크값을 받아온다. */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull final String[] permissions, @NonNull int[] grantResults) {
        int permissionCount = 0;
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    permissionCount++;
                    if (permissions.length == permissionCount) {
                        finishLoad();
                    } else if ( i == permissions.length ) {
                        Log.e(TAG, "권한 부족");
                        return;
                    }

                } else {
                    Toast.makeText(this, permissions[i] + " permission denied.", Toast.LENGTH_LONG).show();
                    super.showAlertDialog("권한을 허용해주세요.", "아래 권한을 허용해주세요.", R.string.cancel)
                            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    for (int i = 0; i < permissions.length; i++) {
                                        if (!ActivityCompat.shouldShowRequestPermissionRationale(Splash.this, PERMISSIONS[i])) {
                                            /* 데이터베이스 생성 */
                                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                                    .setData(Uri.parse("package:" + Splash.this.getPackageName()));
                                            startActivityForResult(intent, PERMISSION_REQUEST_CODE);
                                            return;
                                        } else
                                            ActivityCompat.requestPermissions(Splash.this, PERMISSIONS, PERMISSION_REQUEST_CODE);
                                    }
                                }
                            })
                            .show();
                    return;
                }
            }

        }
    }

    /* 퍼미션 체크값을 받아온다. */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ( requestCode == PERMISSION_REQUEST_CODE ) permissionCheck();
    }

    @Override
    protected BaseActivity<Splash> getActivityClass() {
        return this;
    }
    /* 카메라 액티비티로 넘어가기전 체크해준다. */
    private void finishLoad() {
        File targetFile = getFileStreamPath("target.jpg");
        if (!targetFile.isFile()) {
            copyFile("target.jpg");
            move();
        } else {
            move();
        }


    }
    private void move() {
        startActivity(new Intent(this, TestCameraActivity.class));
        finish();
    }
    /* 에셋 파일을 복사해준다. */
    public void copyFile(String srcFile) {
        AssetManager assetMgr = this.getAssets();

        InputStream is = null;
        OutputStream os = null;
        try {
            /* 폴더 체크 */
            String dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "HodooOpenCV";
            File dir = new File(dirPath);
            if ( !dir.isDirectory() ) {
                dir.mkdir();
            }

            /* 파일 체크 */
            String destFile = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES) + File.separator + "HodooOpenCV" + File.separator + srcFile;
            File file = new File(destFile);
            if ( !file.isFile() ) {
                is = assetMgr.open(srcFile);
                os = new FileOutputStream(destFile);

                byte[] buffer = new byte[1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                is.close();
                os.flush();
                os.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
