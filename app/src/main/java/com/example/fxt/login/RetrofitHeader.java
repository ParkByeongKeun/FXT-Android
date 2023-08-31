package com.example.fxt.login;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RetrofitHeader {
    private static String accessToken;
    private static OkHttpClient client;

    public static OkHttpClient getHeader(final String accessToken) {
        if(!accessToken.equals(RetrofitHeader.accessToken)) {
            RetrofitHeader.accessToken = accessToken;
            RetrofitHeader.client = new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request newRequest  = chain.request().newBuilder()
                                    .addHeader("Content-Type", "application/json")
                                    .addHeader("x-access-token", accessToken)
                                    .build();
                            return chain.proceed(newRequest);
                        }
                    }).build();
        }

        return RetrofitHeader.client;
    }
}
