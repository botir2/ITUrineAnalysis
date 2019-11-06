package com.ahqlab.hodooopencv.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import static com.ahqlab.hodooopencv.constant.HodooConstant.SHARED_NAME;

public abstract class BaseActivity<D extends Activity> extends AppCompatActivity {
    protected final String TAG = getClass().getSimpleName();
    protected abstract BaseActivity<D> getActivityClass();
    public Activity setActivity () {
        return this;
    }
    public SharedPreferences getPreferences(int mode){
        SharedPreferences pref = getSharedPreferences(SHARED_NAME, mode);
        return pref;
    }

    public AlertDialog.Builder showAlertDialog(String title, String content, int cancelStr) {
        AlertDialog.Builder builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(getActivityClass())
                    .setTitle(title)
                    .setNegativeButton(cancelStr, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setMessage(content);
        }
        return builder;
    }
}
