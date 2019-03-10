package com.triplec.triway.route;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.triplec.triway.R;
import com.triplec.triway.common.DataParser;
import com.triplec.triway.common.TriPlace;
import com.triplec.triway.common.TriPlan;
import com.triplec.triway.mvp.MvpFragment;

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

/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends MvpFragment<RouteContract.Presenter> implements RouteContract.View {

    private MapView mMapView;
    private GoogleMap mMap;
    private List<LatLng> MarkerPoints;
    private ArrayList<String> IDs = new ArrayList<String>();

    public static MapFragment newInstance() {

        Bundle args = new Bundle();

        MapFragment fragment = new MapFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        // parsing plan(list)

        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                //initialize
                mMap = googleMap;
                mMap.getUiSettings().setZoomControlsEnabled(true);
            }
        });

        return view;

    }

    @Override
    public void showRoutes(TriPlan placePlan) {
        // testing data
        MarkerPoints = new ArrayList<LatLng>();
        List<TriPlace> resultPlaces = placePlan.getPlaceList();
        for (int i=0; i<resultPlaces.size(); i++) {
            MarkerPoints.add(new LatLng(resultPlaces.get(i).getLatitude(),
                    resultPlaces.get(i).getLongitude()));
        }

//        Bundle bundle = getArguments();
//        ArrayList<String> list = bundle.getStringArrayList("strll");
//        for(int i=0; i<list.size(); i++) {
//            String[] ll = list.get(i).split(" ");
//            double lat = Double.valueOf(ll[0]);
//            double lng = Double.valueOf(ll[1]);
//            MarkerPoints.add(new LatLng(lat, lng));
//        }

        // pin all the places to the map
        for(int i=0; i< MarkerPoints.size(); i++){
            MarkerOptions options = new MarkerOptions();
            options.position(MarkerPoints.get(i));
            mMap.addMarker(options);
        }

        for(int i=0; i< MarkerPoints.size()-1; i++){
            LatLng from = MarkerPoints.get(i);
            LatLng to = MarkerPoints.get(i+1);
            String url = getUrl(from,to);
            FetchUrl fetch = new FetchUrl();
            fetch.execute(url);

            String id1 = IDs.get(0);
            String id2 = IDs.get(1);

            presenter.setPlaceId(i, id1);
            presenter.setPlaceId(i+1, id2);
        }
        if (MarkerPoints.size() > 0) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(MarkerPoints.get(0)));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
        }
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
            Log.d("downloadUrl", data.toString());
            br.close();
        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    @Override
    public RouteContract.Presenter getPresenter() {
        return new RoutePresenter();
    }



    @Override
    public void onError(String message) {
        Toast.makeText(getActivity(), "Failed to save plan. "
                                    + message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSavedSuccess(String planName) {
        Toast.makeText(getActivity(), "Plan saved as "
                                    + planName, Toast.LENGTH_SHORT).show();
    }

    @Override
    public String getMainPlace() {
        if (getArguments() != null)
            return getArguments().getString("place");
        else
            return "";
    }
    public void setTriPlanId(String id) {
        presenter.setPlanId(id);
    }
    @Override
    public String savePlans(String plan_name) {
        return this.presenter.savePlans(plan_name);
    }

    @Override
    public boolean addPlace(TriPlace newPlace) {
        return this.presenter.addPlace(newPlace);
    }

    @Override
    public ArrayList<String> getPassedPlan() {
        if (getArguments() != null)
            return getArguments().getStringArrayList("plan");
        else
            return null;
    }

    @Override
    public Context getContext() {
        return this.getActivity();
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
                Log.d("Background Task data", data.toString());
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
                Log.d("ParserTask",jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                IDs = parser.getIDs();
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
                mMap.addPolyline(lineOptions);
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }
    }





}
