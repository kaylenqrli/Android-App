package com.triplec.triway.common;

import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;

import java.util.ArrayList;

public class TriPlace {

    private String name,address;
    private double latitude, longitude,rating,shopHour;


    public TriPlace(String n){
        name = n;
    }

    private void setAddress(String a){
        address = a;
    }

    public String getAddress(){
        return address;
    }

    private void setRating(double r){
        rating = r;
    }

    public double getRating() {
        return rating;
    }

    public double getLatitude(){
        return latitude;
    }

    public double getLongitude(){
        return longitude;
    }

    public String getName(){
        return name;
    }

    public ArrayList<TriPlace> getTopFive(){
        ArrayList<TriPlace> list = new ArrayList<TriPlace>();
        PoiSearch mPoiSearch = PoiSearch.newInstance();
        OnGetPoiSearchResultListener listener = new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                for(int i=0; i<5; i++) {
                    PoiInfo poi = poiResult.getAllPoi().get(i);
                    double latitude = poi.getLocation().latitude;
                    double longitude = poi.getLocation().longitude;
                    TriPlace curr = new TriPlace(poi.getName());
                    curr.setAddress(poi.getAddress());
                    curr.setRating(poi.poiDetailInfo.overallRating);

                    list.add(curr);
                }
            }
            @Override
            public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {

            }
            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

            }
            //废弃
            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

            }
        };
        mPoiSearch.setOnGetPoiSearchResultListener(listener);
        //search city
        mPoiSearch.searchInCity(new PoiCitySearchOption()
                .city(getName())
                .keyword("景点")
                .pageCapacity(5));
        //search nearby tourist places
//        mPoiSearch.searchNearby(new PoiNearbySearchOption()
//                .location(new com.baidu.mapapi.model.LatLng(getLatitude(),getLongitude()))
//                .radius(100)
//                .keyword("景点")
//                .pageCapacity(5)
//                .scope(2));

        return list;
    }


}
