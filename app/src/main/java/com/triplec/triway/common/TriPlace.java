package com.triplec.triway.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.gson.annotations.SerializedName;
import com.triplec.triway.MapListAdapter;
import com.triplec.triway.PlaceListAdapter;
import com.triplec.triway.R;

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
    private static class PlaceDetails implements Serializable{
        @SerializedName("geometry")
        private TriPoint mTriPoint;
        private static class TriPoint implements Serializable{
            @SerializedName("coordinates")
            private List<Double> coordinates;
        }
        @SerializedName("properties")
        private TriAddress mTriAddress;
        private static class TriAddress implements Serializable{
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
    public TriPlace() {
        this.mPlaceDetail = new PlaceDetails();
        this.mPlaceDetail.mTriAddress = new PlaceDetails.TriAddress();
        this.mPlaceDetail.mTriPoint = new PlaceDetails.TriPoint();
        this.mPlaceDetail.mTriPoint.coordinates = new ArrayList<Double>();
        this.mPlaceDetail.mTriPoint.coordinates.add(0.0);
        this.mPlaceDetail.mTriPoint.coordinates.add(0.0);
    }

//    private void setAddress(String a){
//        address = a;
//    }
    public String getCity(){
        return this.mPlaceDetail.mTriAddress.city;
    }
    public void setCity(String city){
        this.mPlaceDetail.mTriAddress.city = city;
    }
    public String getStateCode(){
        return this.mPlaceDetail.mTriAddress.stateCode;
    }
    public void setStateCode(String stateCode){
        this.mPlaceDetail.mTriAddress.city = stateCode;
    }

    public String getPostalCode(){
        return this.mPlaceDetail.mTriAddress.postalCode;
    }
    public void setPostalCode(String postalCode){
        this.mPlaceDetail.mTriAddress.postalCode = postalCode;
    }

    public String getCountryCode(){
        return this.mPlaceDetail.mTriAddress.countryCode;
    }
    public void setCountryCode(String countryCode){
        this.mPlaceDetail.mTriAddress.countryCode = countryCode;
    }

    public String getStreet(){
        return this.mPlaceDetail.mTriAddress.street;
    }
    public void setStreet(String street){
        this.mPlaceDetail.mTriAddress.street = street;
    }

    public String getDescription() {
      return "test description for " + this.getName();
    }
    public void setDescription(String description){

    }

    public double getLatitude(){
            return this.mPlaceDetail.mTriPoint.coordinates.get(1);
    }
    public void setLatitude(double latitude){
        this.mPlaceDetail.mTriPoint.coordinates.set(1, latitude);
    }

    public double getLongitude(){
            return this.mPlaceDetail.mTriPoint.coordinates.get(0);
    }
    public void setLongitude(double longitude){
        this.mPlaceDetail.mTriPoint.coordinates.set(0, longitude);
    }

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
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
    public String getId(){
        return placeId;
    }

    private void fetchPhoto(Context context, PlaceListAdapter adapter) {
        Places.initialize(context,  context.getResources().getString(R.string.google_maps_key));
        PlacesClient placesClient = Places.createClient(context);
        if (placeId == null || placeId.length() == 0) {
            Log.e("PlaceID not found ", getName());
            return;
        }
        Log.d("PlaceList PlaceID: ", placeId);
        List<Place.Field> fields = Arrays.asList(Place.Field.PHOTO_METADATAS);
        FetchPlaceRequest placeRequest = FetchPlaceRequest.builder(placeId, fields).build();

        placesClient.fetchPlace(placeRequest).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            // Get the photo metadata.
            if (place.getPhotoMetadatas() == null) {
                Log.e("Photo not found ", getName());
                return;
            }

            PhotoMetadata photoMetadata = place.getPhotoMetadatas().get(0);

            // Get the attribution text.
            String attributions = photoMetadata.getAttributions();

            // Create a FetchPhotoRequest.
            FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata).build();
            placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                Log.d("Found photo", getName());
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
        if(photo == null ) {
            fetchPhoto(context, adapter);
        }
        Log.d("=====", "getPhoto() ");
        return photo;
    }
    public Bitmap getPhoto(Context context, MapListAdapter adapter){
        if(photo == null) {
            fetchPhoto(context, adapter);
        }
        Log.d("=====", "getPhoto() ");
        return photo;
    }
    private void fetchPhoto(Context context, MapListAdapter adapter) {
        Places.initialize(context,  context.getResources().getString(R.string.google_maps_key));
        PlacesClient placesClient = Places.createClient(context);
        if (placeId == null || placeId.length() == 0)
            return;
        Log.d("MapList PlaceID: ", placeId);
        List<Place.Field> fields = Arrays.asList(Place.Field.PHOTO_METADATAS);
        FetchPlaceRequest placeRequest = FetchPlaceRequest.builder(placeId, fields).build();

        placesClient.fetchPlace(placeRequest).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            // Get the photo metadata.
            if (place.getPhotoMetadatas() == null) {
                Log.e("Photo not found ", getName());
                return;
            }
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

}
