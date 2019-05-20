package com.triplec.triway.route;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.triplec.triway.R;
import com.triplec.triway.RouteActivity;
import com.triplec.triway.common.TriPlace;
import com.triplec.triway.common.TriPlan;
import com.triplec.triway.mvp.MvpFragment;

import java.util.ArrayList;
import java.util.List;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static android.view.View.AUTOFILL_FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends MvpFragment<RouteContract.Presenter> implements RouteContract.View {

    private MapView mMapView;
    private GoogleMap mMap;
    List<LatLng> markerPoints;
    MapListAdapter mapListAdapter;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView recyclerView;


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

        mMapView = view.findViewById(R.id.mapView);
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

        recyclerView = view.findViewById(R.id.map_recycler);
        mapListAdapter = new MapListAdapter(getActivity(),null);
        recyclerView.setAdapter(mapListAdapter);
        layoutManager = new LinearLayoutManager(getActivity());
        ((LinearLayoutManager) layoutManager).setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == SCROLL_STATE_IDLE) {
                    int itemSelected = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(markerPoints.get(itemSelected)));
                }
            }
        });
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);
        return view;
    }

    @Override
    public RouteContract.Presenter getPresenter() {
        return new RoutePresenter();
    }

    @Override
    public void showRoutes(TriPlan placePlan) {
        // testing data
        markerPoints= new ArrayList<LatLng>();
        List<TriPlace> resultPlaces = placePlan.getPlaceList();
        if (resultPlaces == null || resultPlaces.size() == 0)
            return;
        for (int i=0; i<resultPlaces.size(); i++) {
            markerPoints.add(new LatLng(resultPlaces.get(i).getLatitude(),
                                resultPlaces.get(i).getLongitude()));
        }
        Marker[] markers = new Marker[resultPlaces.size()];

        Log.d("SHOWING PLAN::", String.valueOf(placePlan.getPlaceList().size()));
        TriPlan.TriPlanBuilder builder = new TriPlan.TriPlanBuilder();
        builder.addPlaceList(placePlan.getPlaceList());
        TriPlan plan = builder.buildPlan();
        mapListAdapter = new MapListAdapter(getActivity(), plan.getPlaceList());
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap.clear();
                // pin all the places to the map
                for(int i=0; i< markerPoints.size(); i++){
                    MarkerOptions options = new MarkerOptions();
                    options.position(markerPoints.get(i));
                    Marker marker = mMap.addMarker(options);
                    marker.setTag(i);
                    markers[i] = marker;
                }

                Log.d("TAG", "onMapReady: zoom in animation" + markerPoints.get(0).toString());
                recyclerView.setAdapter(mapListAdapter);
                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                      recyclerView.setVisibility(View.INVISIBLE);
                      mMap.setPadding(0,0,0,0);
                    }
                });

                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        int markerPosition = 0;
                        try {
                            markerPosition = (Integer) marker.getTag();
                        } catch (NullPointerException e) {
                            Toast.makeText(getContext(), "Marker has no Tag", Toast.LENGTH_SHORT).show();
                        }
                        //                Toast.makeText(getContext(), "Marker " + markerPosition + " is selected", Toast.LENGTH_SHORT).show();
                        recyclerView.setVisibility(View.VISIBLE);
                        mMap.setPadding(0,0,0,(int) (200 * Resources.getSystem().getDisplayMetrics().density));
                        recyclerView.scrollToPosition(markerPosition);
                        return false;
                    }
                });

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for( Marker marker : markers ){
                    builder.include(marker.getPosition());
                }
                LatLngBounds bounds = builder.build();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 100);
                mMap.moveCamera(cameraUpdate);
                fetchRoutes(placePlan);
            }
        });

    }
    @Override
    public void onError(String message) {
        Toast.makeText(getActivity(), "Error: "
                                    + message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSavedSuccess(String planName) {
        Toast.makeText(getActivity(), "Plan saved as "
                                    + planName, Toast.LENGTH_SHORT).show();
    }
    private void fetchRoutes(TriPlan placePlan) {
        this.presenter.fetchRoutes(placePlan);
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
        Toast.makeText(getContext(), "Add a new place: " + newPlace.getName(), Toast.LENGTH_SHORT).show();
        return this.presenter.addPlace(newPlace);
    }

    @Override
    public TriPlan getPassedPlan() {
        TriPlan mPlan = (TriPlan)getArguments().getSerializable("plan");
        Log.d("received: ", mPlan.getName());
        if (getArguments() != null)
            return (TriPlan) getArguments().getSerializable("plan");
        else
            return null;
    }

    @Override
    public Context getContext() {
        return this.getActivity();
    }

    @Override
    public void addPolyline(PolylineOptions lineOptions) {
        mMap.addPolyline(lineOptions);
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        mapListAdapter.notifyDataSetChanged();
    }
}
