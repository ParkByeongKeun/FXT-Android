package com.example.fxt.RestApi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static String URL = "http://118.67.142.85:8000";

    public static ApiService getApiService() {
        return getInstance().create(ApiService.class);
    }
    static OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(chain -> {
                Request newRequest  = chain.request().newBuilder()
                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
                        .build();
                return chain.proceed(newRequest);
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