package com.triplec.triway.common;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TriPlace implements Serializable {

//    private double rating;

    @SerializedName("name")
    private String name;

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
    public TriPlace(String n){
        name = n;
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

}
