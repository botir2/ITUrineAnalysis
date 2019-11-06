package com.ahqlab.hodooopencv.http.service;

import com.ahqlab.hodooopencv.domain.HsvValue;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface RetrofitService {
    @POST
    Call<HsvValue> getHsv(@Url String url,@Body HsvValue value);

    @GET("color/test")
    Call<String> test();
}
