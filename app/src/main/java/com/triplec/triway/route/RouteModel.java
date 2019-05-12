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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.triplec.triway.R;
import com.triplec.triway.common.DataParser;
import com.triplec.triway.common.RoutePlanner;
import com.triplec.triway.common.TriPlace;
import com.triplec.triway.common.TriPlan;
import com.triplec.triway.retrofit.PlaceRequestApi;
import com.triplec.triway.retrofit.RetrofitClient;
import com.triplec.triway.retrofit.response.PlaceResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
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

    boolean owner = false;
    private ArrayList<ArrayList<TriPlace>> mTriPlans;

    RouteModel() {
        placesRequestApi = RetrofitClient.getInstance().create(PlaceRequestApi.class);
        //mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance("https://mytrip-a082d-7bc7a.firebaseio.com/").getReference();
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
                myBuilder.addPlaceList(response.body().getPlaces().subList(0, 5));

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

    // TODO: change database structure
    @Override
    public String savePlans(String planName) {
        String userId = user.getUid();
        String key = mTriPlan.getId();

        mTriPlan.setName(planName);
        // TODO: delete mTriPlans after done testing
        mTriPlans = new ArrayList<ArrayList<TriPlace>>();
        ArrayList<TriPlace> tempPlans = new ArrayList<TriPlace>();
        for(TriPlace p: mTriPlan.getPlaceList()){
            tempPlans.add(p);
        }
        mTriPlans.add(tempPlans);

        // This plan does not exist in plans table
        if(key.length() == 0) {
            // push to plan table
            key = mDatabase.child("plans").push().getKey();
        }

        // Set plans table
        String finalKey = key;
        mDatabase.child("plans").child(finalKey).child("name")
                .setValue(planName)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        // Write plan name was successful! Continue to set timestamp
                        mDatabase.child("plans").child(finalKey).child("createdAt")
                                .setValue(new Date().toString())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        // Write timestamp was successful! Continue to set data
                                        DatabaseReference dataRef = mDatabase.child("plans").child(finalKey).child("data");
                                        dataRef.setValue(mTriPlans).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                // Write data was successful! Continue to check user
                                                DatabaseReference usersRef = mDatabase.child("plans").child(finalKey).child("users");
                                                usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        // add current user to users list in plans table
                                                        List<String> userIds = new ArrayList<String>();
                                                        userIds.add(userId);

                                                        for(DataSnapshot snapUserId: dataSnapshot.getChildren()){
                                                            String currUserId = snapUserId.getValue(String.class);
                                                            if(!currUserId.equals(userId)){
                                                                userIds.add(currUserId);
                                                            } else {
                                                                owner = true;
                                                            }
                                                        }

                                                        usersRef.setValue(userIds).addOnSuccessListener(new OnSuccessListener<Void>() {
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
                                                                Log.e("** add user **", e.getMessage());
                                                                presenter.onError("Failed to save plan. " + e.getMessage());
                                                            }
                                                        });;

                                                    }
                                                    @Override
                                                    public void onCancelled(DatabaseError firebaseError) {}
                                                });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Write failed
                                                Log.e("** set data **", e.getMessage());
                                                presenter.onError("Failed to save plan. " + e.getMessage());
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                    // Write failed
                                    Log.e("** createdAt **", e.getMessage());
                                    presenter.onError("Failed to save plan. " + e.getMessage());
                                }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Write failed
                        Log.e("** name **", e.getMessage());
                        presenter.onError("Failed to save plan. " + e.getMessage());
                    }
                });

        // Set users table
        if(!owner){
            DatabaseReference planRef = mDatabase.child("users").child(userId).child("plans");
            planRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // add current plan to plans list in users table
                    List<String> planIds = new ArrayList<String>();
                    planIds.add(finalKey);

                    for(DataSnapshot snapUserId: dataSnapshot.getChildren()){
                        String currPlanId = snapUserId.getValue(String.class);
                        if(!currPlanId.equals(finalKey)){
                            planIds.add(currPlanId);
                        }
                    }

                    planRef.setValue(planIds).addOnSuccessListener(new OnSuccessListener<Void>() {
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
                            Log.e("** add user **", e.getMessage());
                            presenter.onError("Failed to save plan. " + e.getMessage());
                        }
                    });;
                }
                @Override
                public void onCancelled(DatabaseError firebaseError) {}
            });
        }

        setPlanId(finalKey);
        return mTriPlan.getId();
    }

    @Override
    public void setPlanId(String id) {
        mTriPlan.setId(id);
    }
    @Override
    public boolean addPlace(TriPlace newPlace) {
        // add place to current plan
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

    private String getPlaceIdUrl(LatLng curLatLng, String name) {
        // Origin of route
        String str_location = "location=" + curLatLng.latitude + "," + curLatLng.longitude;
        // Destination of route
        String str_radius = "radius=5000";
        String str_name = "name=" + name;
        // Building the parameters to the web service
        String parameters = str_location + "&" + str_radius + "&" + str_name;
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
                mTriPlan.getPlaceList().get(index).setId(id);
                Log.d("get ID", id + " with: " + index);
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
    public void fetchRoutes(TriPlan placePlan) {
        ArrayList<LatLng> allMarkerPoints= new ArrayList<LatLng>();
        List<TriPlace> resultPlaces = placePlan.getPlaceList();
        for (int i=0; i<resultPlaces.size(); i++) {
            allMarkerPoints.add(new LatLng(resultPlaces.get(i).getLatitude(),
                    resultPlaces.get(i).getLongitude()));
        }
        for(int i=0; i< allMarkerPoints.size(); i++){
            LatLng from = allMarkerPoints.get(i);
            if (i < allMarkerPoints.size()-1) {
                LatLng to = allMarkerPoints.get(i+1);
                String url = getUrl(from,to);
                FetchUrl fetch = new FetchUrl();
                fetch.execute(url);
            }
            String placeUrl = getPlaceIdUrl(from, resultPlaces.get(i).getName());
            FetchPlaceIdUrl fetchPlace = new FetchPlaceIdUrl();
            fetchPlace.execute(placeUrl, String.valueOf(i));
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
