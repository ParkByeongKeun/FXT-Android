package com.fiberfox.fxt.login;

import com.fiberfox.fxt.RestApi.ApiService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static String URL = "http://1.246.219.189:20217/";
    private static String mToken;

    public static ApiService getApiService(String token) {
        mToken = token;
        return getInstance().create(ApiService.class);
    }
    static OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request newRequest  = chain.request().newBuilder()
                            .addHeader("Content-Type", "application/json")
                            .addHeader("x-access-token", mToken)
                            .build();
                    return chain.proceed(newRequest);
                }
            }).build();

    private static Retrofit getInstance() {
        Gson gson = new GsonBuilder().setLenient().create();
        return new Retrofit.Builder()
                .client(client)
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

//
//    Retrofit retrofit = new Retrofit.Builder()
//            .client(client)
//            .baseUrl(URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build();
//
//    ApiService server = retrofit.create(ApiService.class);



}