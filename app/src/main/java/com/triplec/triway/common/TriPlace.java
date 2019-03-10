package com.triplec.triway.common;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TriPlace implements Serializable {

//    private double rating;


    @SerializedName("name")
    private String name;
    private String ID;

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

    public void setID(String id){ID = id;}
    public String getID(){return ID;}

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

}
