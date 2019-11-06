package com.ahqlab.hodooopencv.http;

import android.content.Context;
import android.os.AsyncTask;

import com.ahqlab.hodooopencv.R;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HodooRetrofit {
    public interface HodooCallback {
        <T> void onResponse( Response<T> response );
        void onFailure( String error );
    }
    private Retrofit mRetrofit;
    public HodooRetrofit(Context context) {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.base_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
    public Retrofit getRetrofit() {
        return mRetrofit;
    }
    public static String getUrl( Context context, int url ){
        return context.getString(url);
    }
    public <T> T request (final Call<T> requester, final HodooCallback callback) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                requester.enqueue(new Callback<T>() {
                    @Override
                    public void onResponse(Call<T> call, Response<T> response) {
                        callback.onResponse(response);
                    }

                    @Override
                    public void onFailure(Call<T> call, Throwable t) {
                        callback.onFailure(t.getMessage());
                    }
                });
                try {
                    requester.clone().execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
        return null;
    }

}
