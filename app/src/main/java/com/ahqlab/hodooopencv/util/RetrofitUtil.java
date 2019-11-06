package com.ahqlab.hodooopencv.util;

import android.content.Context;
import android.os.AsyncTask;

import com.ahqlab.hodooopencv.R;
import com.ahqlab.hodooopencv.http.service.RetrofitService;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitUtil {
    private Context mContext;
    public RetrofitUtil ( Context context ) {
        mContext = context;
    }
    public interface Callbacks {
        <T> void onResponse( T data );
    }
    public static String getUrl(Context context, int url) {
        return context.getString(R.string.base_url) + context.getString(url);
    }
    public void requester (String url, Call type, Callbacks callbacks) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(mContext.getString(R.string.base_url))
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                RetrofitService service = retrofit.create(RetrofitService.class);
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                return null;
            }
        };
    }
}
