package com.triplec.triway.common;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;

public class TriPlace implements Serializable {

//    private double rating;
    @SerializedName("place")
    private PlaceDetails mPlaceDetail;

    @SerializedName("name")
    private String name;
    public static class PlaceDetails {
        @SerializedName("geometry")
        private TriPoint mTriPoint;
        @SerializedName("properties")
        private TriAddress mTriAddress;
        public static class TriPoint {
            @SerializedName("coordinates")
            public ArrayList<String> coordinates;
        }
        public static class TriAddress {
            @SerializedName("city")
            public String city;
            @SerializedName("stateCode")
            public String stateCode;
            @SerializedName("postalCode")
            public String postalCode;
            @SerializedName("countryCode")
            public String countryCode;
            @SerializedName("street")
            public String street;
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
        return this.mPlaceDetail.mTriPoint.coordinates.indexOf(1);
    }

    public double getLongitude(){
        return this.mPlaceDetail.mTriPoint.coordinates.indexOf(0);
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

}
