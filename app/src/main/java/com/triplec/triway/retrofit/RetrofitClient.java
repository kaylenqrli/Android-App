package com.triplec.triway.retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "https://www.mapquestapi.com/search/v4/";
    private static Retrofit instance;

    public static Retrofit getInstance() {
        if (instance == null) {
            instance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(getHttpClient())
                    .build();
        }
        return instance;
    }

    private static class PlaceInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {

            Request original = chain.request();
            Request request = original
                    .newBuilder()
                    .build();

            return chain.proceed(request);
        }
    }

    private static OkHttpClient getHttpClient() {

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(new PlaceInterceptor())
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS);
        return builder.build();
    }

}