package com.triplec.triway.common;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;


import java.util.*;
import java.lang.*;

public class TriPlace extends AppCompatActivity {
    private Place place;

    public TriPlace(String id) {

        // Specify the fields to return (all fields are returned).
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME);

        FetchPlaceRequest request = FetchPlaceRequest.builder(id, placeFields).build();

        Places.initialize(getApplicationContext(), "AIzaSyD0M31_rt5lyug3SxPeZ3NHdJcHhWZifPs");
        PlacesClient placesClient = Places.createClient(this);

        placesClient.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
            @Override
            public void onSuccess(FetchPlaceResponse response) {
                place = response.getPlace();
            }
        });

    }


    public CharSequence getAddress(){
        return place.getAddress();
    }

    public double getRating() {
        return place.getRating();
    }

    public LatLng getLatlng() {
        return place.getLatLng();
    }

    public String getName(){

        return place.getName();
    }




}
