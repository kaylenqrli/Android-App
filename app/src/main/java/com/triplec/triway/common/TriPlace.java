package com.triplec.triway.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.gson.annotations.SerializedName;
import com.triplec.triway.PlaceListAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TriPlace implements Serializable {

//    private double rating;

    @SerializedName("name")
    private String name;
    private String placeId;
    private Bitmap photo;
    private boolean photoSetup = false;

    @SerializedName("place")
    private PlaceDetails mPlaceDetail;
    private static class PlaceDetails {
        @SerializedName("geometry")
        private TriPoint mTriPoint;
        private static class TriPoint {
            @SerializedName("coordinates")
            private List<Double> coordinates;
        }
        @SerializedName("properties")
        private TriAddress mTriAddress;
        private static class TriAddress {
            @SerializedName("city")
            private String city;
            @SerializedName("stateCode")
            private String stateCode;
            @SerializedName("postalCode")
            private String postalCode;
            @SerializedName("countryCode")
            private String countryCode;
            @SerializedName("street")
            private String street;
        }
    }
    public String address;

    public TriPlace(String n){
        name = n;
    }

//    private void setAddress(String a){
//        address = a;
//    }
    public String getCity(){
        return this.mPlaceDetail.mTriAddress.city;
    }
    public String getStateCode(){
        return this.mPlaceDetail.mTriAddress.stateCode;
    }
    public String getPostalCode(){
        return this.mPlaceDetail.mTriAddress.postalCode;
    }
    public String getCountryCode(){
        return this.mPlaceDetail.mTriAddress.countryCode;
    }
    public String getStreet(){
        return this.mPlaceDetail.mTriAddress.street;
    }
    public String getDescription() {
      return "test description for " + this.getName();
    }
    public double getLatitude(){
        return this.mPlaceDetail.mTriPoint.coordinates.get(1);
    }

    public double getLongitude(){
        return this.mPlaceDetail.mTriPoint.coordinates.get(0);
    }

    public String getName(){
        return name;
    }
//    private void setRating(double r){
//        rating = r;
//    }
//
//    public double getRating() {
//        return rating;
//    }

    public void setId(String id){
        placeId = id;
    }

    private void fetchPhoto(Context context, PlaceListAdapter adapter) {
        Places.initialize(context, "AIzaSyDYKAtsvLfqJnT_t1VhAjvrLMb2cddLcVQ");
        PlacesClient placesClient = Places.createClient(context);

        List<Place.Field> fields = Arrays.asList(Place.Field.PHOTO_METADATAS);
        FetchPlaceRequest placeRequest = FetchPlaceRequest.builder(placeId, fields).build();

        placesClient.fetchPlace(placeRequest).addOnSuccessListener((response) -> {
            Place place = response.getPlace();

            // Get the photo metadata.
            PhotoMetadata photoMetadata = place.getPhotoMetadatas().get(0);

            // Get the attribution text.
            String attributions = photoMetadata.getAttributions();

            // Create a FetchPhotoRequest.
            FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata).build();
            placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                Bitmap bitmap = fetchPhotoResponse.getBitmap();
                setPhoto(bitmap);
                photoSetup = true;
                adapter.notifyDataSetChanged();
            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    int statusCode = apiException.getStatusCode();
                    // Handle error with given status code.
                    Log.e("Photo Setup Failed", "Place not found: " + exception.getMessage());
                }
            });
        });
    }

    public void setPhoto(Bitmap bitmap){
        photo = bitmap;
    }

    public Bitmap getPhoto(Context context, PlaceListAdapter adapter){
        if(!photoSetup) {
            fetchPhoto(context, adapter);
        }
        Log.e("=====", "getPhoto() ");
        return photo;
    }
}
