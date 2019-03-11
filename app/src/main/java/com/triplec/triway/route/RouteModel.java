package com.triplec.triway.route;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.triplec.triway.R;
import com.triplec.triway.common.DataParser;
import com.triplec.triway.common.RoutePlanner;
import com.triplec.triway.common.TriPlace;
import com.triplec.triway.common.TriPlan;
import com.triplec.triway.retrofit.PlaceRequestApi;
import com.triplec.triway.retrofit.RetrofitClient;
import com.triplec.triway.retrofit.response.PlaceResponse;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

class RouteModel implements RouteContract.Model {
    private RouteContract.Presenter presenter;
    private PlaceRequestApi placesRequestApi;
    private DatabaseReference mDatabase;
    private TriPlan mTriPlan;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private Geocoder coder;
    private int index = 0;
    RouteModel() {
        placesRequestApi = RetrofitClient.getInstance().create(PlaceRequestApi.class);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
    }
    public void setGeocoder(Context mContent) {
        coder = new Geocoder(mContent);
    }
    private LatLng getFromName(String place){
        List<Address> address;
        LatLng p1 = null;
        try {
            // May throw an IOException
            address = coder.getFromLocationName(place, 5);
            if (address == null || address.size() == 0) {
                return null;
            }
            Address location = address.get(0);
            p1 = new LatLng(location.getLatitude(), location.getLongitude() );
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return p1;
    }
    @Override
    public void fetchData(String place) {
        if (coder == null)
            return;
        LatLng latLng = getFromName(place);
        if (latLng == null) {
            presenter.onError(place + " can't be found.");
            return;
        }
        double lat = latLng.latitude;
        double lng = latLng.longitude;
        Map<String, String> paramMap = new HashMap<>();
        // longt, lat
        paramMap.put("location", lng + "," +lat);
        //paramMap.put("q", "san diego");
        paramMap.put("sort", "relevance");
        paramMap.put("feedback", "false");
        paramMap.put("key", "eG53wKfQK8DuhGn4xGwc5evrgBpfwx4w");
        // this sets the category to tourist attractions
        paramMap.put("category", "sic:799972");

        placesRequestApi.getPlaces(paramMap).enqueue(new Callback<PlaceResponse>() {
            @Override
            public void onResponse(Call<PlaceResponse> call, Response<PlaceResponse> response) {
                if (!response.isSuccessful()) {
                    presenter.onError(response.message());
                }
                TriPlan.TriPlanBuilder myBuilder = new TriPlan.TriPlanBuilder();
                myBuilder.addPlaceList(response.body().getPlaces());

                TriPlan newPlan = myBuilder.buildPlan();
                List<TriPlace> newList = newPlan.getPlaceList();

                RoutePlanner planner = new RoutePlanner(newList);
                TriPlan sortedPlan = planner.planRoute(); // sorted plan by algorithm

                mTriPlan = sortedPlan;
                presenter.showRoutes(sortedPlan);
            }

            @Override
            public void onFailure(Call<PlaceResponse> call, Throwable t) {
                    presenter.onError(t.getMessage());
            }
        });
    }

    @Override
    public String savePlans(String planName) {
        String userId = user.getUid();
        String key = mTriPlan.getId();
        mTriPlan.setName(planName);
        // This plan has not been saved before
        if (key.length() == 0) {
            key = mDatabase.child("users").child(userId).child("plans").push().getKey();
        }
        // This plan has been saved before
        String finalKey = key;
        mDatabase.child("users").child(userId).child("plans").child(finalKey).child("name")
                .setValue(planName)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Write was successful!
                        presenter.onSavedSuccess(mTriPlan.getName());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Write failed
                        presenter.onError("Failed to save plan. " + e.getMessage());
                    }
        });
        mDatabase.child("users").child(userId).child("plans").child(finalKey).child("places")
                .setValue(mTriPlan.getPlaceList())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Write was successful!
                        mTriPlan.setId(finalKey);
                        presenter.onSavedSuccess(mTriPlan.getName());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Write failed
                        presenter.onError("Failed to save plan. " + e.getMessage());
                    }
        });
        setPlanId(finalKey);
        return mTriPlan.getId();
    }

    private void setPlaceId(int i, String id) {
        mTriPlan.getPlaceList().get(i).setId(id);
    }

    @Override
    public void setPlanId(String id) {
        mTriPlan.setId(id);
    }
    @Override
    public boolean addPlace(TriPlace newPlace) {
        // TODO add place to current plan
        mTriPlan.getPlaceList().add(newPlace);
        presenter.showRoutes(mTriPlan);
        return true;
    }
    /* following helper methods from: https://github.com/hiteshbpatel/Android_Blog_Projects*/
    // generate url base on two locations
    private String getUrl(LatLng origin, LatLng dest) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + "driving";
        // sensor
        String sensor = "sensor=false";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?"
                + parameters + "&key=" + "AIzaSyCmALKlEfyw3eOrW1jPnf6_xrrS7setOFU";
        return url;
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
    // Fetches data from url passed

    private class FetchUrl extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            // For storing data from web service
            String data = "";
            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
}
    /**

     * A class to parse the Google Places in JSON format

     */

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask",jsonData[0]);
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);

                ArrayList<String> currid = parser.getIDs();
                setPlaceId(index,currid.get(0));
                setPlaceId(index+1,currid.get(1));
                index++;

                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());
            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;
            Log.d("onPostExecute ", ""+result.size());
            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();
                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);
                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(20);
                lineOptions.color(R.color.road);
                Log.d("onPostExecute","onPostExecute  lineoptions decoded");
            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                addPolyline(lineOptions);
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }
    }
    @Override
    public void updatePlan(TriPlan newPlan) {
        this.mTriPlan = newPlan;
    }

    @Override
    public void fetchRoutes(List<LatLng> allMarkerPoints) {
        for(int i=0; i< allMarkerPoints.size()-1; i++){
            LatLng from = allMarkerPoints.get(i);
            LatLng to = allMarkerPoints.get(i+1);
            String url = getUrl(from,to);
            FetchUrl fetch = new FetchUrl();
            fetch.execute(url);
        }
    }
    private void addPolyline(PolylineOptions lineOptions) {
        this.presenter.addPolyline(lineOptions);
    }
    @Override
    public void setPresenter(RouteContract.Presenter presenter) {
        this.presenter = presenter;
    }
}
