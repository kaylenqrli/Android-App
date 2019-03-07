package com.triplec.triway.retrofit;

import com.triplec.triway.retrofit.response.PlaceResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.QueryMap;

public interface PlaceRequestApi {
    @Headers("Content-Type: Application/json")
    @GET("place")
    Call<PlaceResponse> getPlaces(@QueryMap Map<String, String> param);
}
