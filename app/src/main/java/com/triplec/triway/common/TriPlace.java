package com.triplec.triway.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.OpeningHours;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.gson.annotations.SerializedName;
import com.triplec.triway.route.MapListAdapter;
import com.triplec.triway.route.PlaceListAdapter;
import com.triplec.triway.R;
import com.triplec.triway.RouteActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
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

    static PlacesClient placesClient;
    static final String apiKey = "AIzaSyCmALKlEfyw3eOrW1jPnf6_xrrS7setOFU";
    private Place ggPlace;


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

    // new constructor base on given latlng and context
    public TriPlace(double lat, double lng, Context context){
        //old code copied from the default constructor
        this.mPlaceDetail = new PlaceDetails();
        this.mPlaceDetail.mTriAddress = new PlaceDetails.TriAddress();
        this.mPlaceDetail.mTriPoint = new PlaceDetails.TriPoint();
        this.mPlaceDetail.mTriPoint.coordinates = new ArrayList<Double>();
        this.mPlaceDetail.mTriPoint.coordinates.add(0.0);
        this.mPlaceDetail.mTriPoint.coordinates.add(0.0);

        //set latlng by given values
        this.setLatitude(lat);
        this.setLongitude(lng);

        //set ID from given latlng
        setIDfromLL(lat,lng);

        //set ggPlace
        initializePlacesClient(context);
        fetchPlaces();
    }


    public TriPlace() {
        this.mPlaceDetail = new PlaceDetails();
        this.mPlaceDetail.mTriAddress = new PlaceDetails.TriAddress();
        this.mPlaceDetail.mTriPoint = new PlaceDetails.TriPoint();
        this.mPlaceDetail.mTriPoint.coordinates = new ArrayList<Double>();
        this.mPlaceDetail.mTriPoint.coordinates.add(0.0);
        this.mPlaceDetail.mTriPoint.coordinates.add(0.0);
    }

    // wrapper for setting id from ll
    private void setIDfromLL(double lat, double lng){
        String url = getPlaceIdUrl(new LatLng(lat,lng));
        FetchPlaceIdUrl fetchPlaceIdUrl = new FetchPlaceIdUrl();
        fetchPlaceIdUrl.execute(url);
    }


    private String getPlaceIdUrl(LatLng curLatLng) {
        // Origin of route
        String str_location = "location=" + getLatitude() + "," + getLongitude();
        // Destination of route
        String str_radius = "radius=5000";
        // Building the parameters to the web service
        String parameters = str_location + "&" + str_radius;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/" + output + "?"
                + parameters + "&key=" + "AIzaSyCmALKlEfyw3eOrW1jPnf6_xrrS7setOFU";
        return url;
    }

    // Fetches data from url passed

    private class FetchPlaceIdUrl extends AsyncTask<String, Void, String> {
        private int index = 0;
        @Override
        protected String doInBackground(String... url) {
            // For storing data from web service
            String data = "";
            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                index = Integer.parseInt(url[1]);
                Log.d("Background Task data", data);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            JSONObject jObject;
            try {
                jObject = new JSONObject(result);
                JSONArray jgeocoders = jObject.getJSONArray("results");
                String id = jgeocoders.getJSONObject(0).getString("place_id");
                setId(id);
                Log.d("get ID", id);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
            // Connecting to url
            urlConnection.connect();
            // Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            Log.d("downloadUrl", data);
            br.close();
        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
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
//      return "test description for " + this.getName();
        if (getCity() == null || getCity().length() == 0)
            return getStreet();
        return getStreet() + ", " + getCity();
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
    public void setName(String Name){
        this.name = Name;
    }

    public double getRating() {
        return 0;
        //return ggPlace.getRating();
    }

    /*public OpeningHours getOpeningHour(){
        return ggPlace.getOpeningHours();
    }*/

    public void setId(String id){
        placeId = id;
    }
    public String getId(){
        return placeId;
    }

    /**
     * Fetch photo for current place using Google Place SDK.
     * Notify adapter to update photo.
     * @param context
     * @param adapter for list fragment
     */
    private void fetchPhoto(Context context, PlaceListAdapter adapter) {
        // Set up Google PlacesClient
        Places.initialize(context,  context.getResources().getString(R.string.google_maps_key));
        PlacesClient placesClient = Places.createClient(context);
        // Verify place id
        if (placeId == null || placeId.length() == 0) {
            Log.e("PlaceID not found ", getName());
            return;
        }

        // Specify fields. Requests for photos must always have the PHOTO_METADATAS field.
        List<Place.Field> fields = Arrays.asList(Place.Field.PHOTO_METADATAS);
        // Get a Place object (this example uses fetchPlace(), but you can also use findCurrentPlace())
        FetchPlaceRequest placeRequest = FetchPlaceRequest.builder(placeId, fields).build();
        // Set up listener for PlacesClient
        placesClient.fetchPlace(placeRequest).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            // Get the photo metadata.
            if (place.getPhotoMetadatas() == null) {
                Log.e("Photo not found ", getName() + placeId);
                return;
            }

            PhotoMetadata photoMetadata = place.getPhotoMetadatas().get(0);

            // Get the attribution text.
            String attributions = photoMetadata.getAttributions();

            // Create a FetchPhotoRequest.
            FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata).build();
            placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                Log.e("Photo found ", getName() + placeId);
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

    /**
     * Store fetched bitmap as photo
     * @param bitmap
     */
    public void setPhoto(Bitmap bitmap){
        photo = bitmap;
    }


    /**
     * Get photo for current place. Fetch photo if it's null
     * @param context
     * @param adapter for list fragment
     * @return photo for current place
     */
    public Bitmap getPhoto(Context context, PlaceListAdapter adapter){
        if(photo == null ) {
            fetchPhoto(context, adapter);
        }
        Log.d("=====", "getPhoto() ");
        return photo;
    }

    /**
     * Get photo for current place. Fetch photo if it's null
     * @param context
     * @param adapter for map fragment
     * @return photo for current place
     */
    public Bitmap getPhoto(Context context, MapListAdapter adapter){
        if(photo == null) {
            fetchPhoto(context, adapter);
        }
        Log.d("=====", "getPhoto() ");
        return photo;
    }

    /**
     * Fetch photo for current place using Google Place SDK.
     * Notify adapter to update photo.
     * @param context
     * @param adapter for map fragment
     */
    private void fetchPhoto(Context context, MapListAdapter adapter) {
        // Set up Google PlacesClient
        Places.initialize(context,  context.getResources().getString(R.string.google_maps_key));
        PlacesClient placesClient = Places.createClient(context);
        // Verify place id
        if (placeId == null || placeId.length() == 0)
            return;

        // Specify fields. Requests for photos must always have the PHOTO_METADATAS field.
        List<Place.Field> fields = Arrays.asList(Place.Field.PHOTO_METADATAS);
        // Get a Place object (this example uses fetchPlace(), but you can also use findCurrentPlace())
        FetchPlaceRequest placeRequest = FetchPlaceRequest.builder(placeId, fields).build();
        // Set up listener for PlacesClient
        placesClient.fetchPlace(placeRequest).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            // Get the photo metadata.
            if (place.getPhotoMetadatas() == null) {
                Log.e("Photo not found ", getName() + placeId);
                return;
            }
            PhotoMetadata photoMetadata = place.getPhotoMetadatas().get(0);

            // Get the attribution text.
            String attributions = photoMetadata.getAttributions();

            // Create a FetchPhotoRequest.
            FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata).build();
            placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                Log.e("Photo found ", getName() + placeId);
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

    /** initialize the placesClient to fetch places */
    public void initializePlacesClient(Context context){
        Places.initialize(context, apiKey);
        placesClient = Places.createClient(context);
    }

    private void fetchPlaces(){
        List<Place.Field> placeField = Arrays.asList(Place.Field.ID, Place.Field.NAME);
        FetchPlaceRequest request = FetchPlaceRequest.builder(this.getId(), placeField).build();
        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            ggPlace = response.getPlace();
            Log.i("TAG", "Place found: " + ggPlace.getName());
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                int statusCode = apiException.getStatusCode();
                // Handle error with given status code.
                Log.e("TAG", "Place not found: " + exception.getMessage());
            }
        });
    }

}
