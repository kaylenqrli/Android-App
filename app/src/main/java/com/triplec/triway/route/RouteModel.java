package com.triplec.triway.route;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.triplec.triway.common.TriPlace;
import com.triplec.triway.common.TriPlan;
import com.triplec.triway.common.TriUser;
import com.triplec.triway.retrofit.PlaceRequestApi;
import com.triplec.triway.retrofit.RetrofitClient;
import com.triplec.triway.retrofit.response.PlaceResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RouteModel implements RouteContract.Model {
    private RouteContract.Presenter presenter;
    private PlaceRequestApi placesRequestApi;
    private DatabaseReference mDatabase;
    private TriPlan mTriPlan;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    RouteModel() {
        placesRequestApi = RetrofitClient.getInstance().create(PlaceRequestApi.class);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
    }
//    private LatLng getFromName(String place){
//
//        Geocoder coder = new Geocoder(this);
//        List<Address> address;
//        LatLng p1 = null;
//        try {
//            // May throw an IOException
//            address = coder.getFromLocationName(place, 5);
//            if (address == null) {
//                return null;
//            }
//            Address location = address.get(0);
//            p1 = new LatLng(location.getLatitude(), location.getLongitude() );
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//        return p1;
//    }
    @Override
    public void fetchData(String place) {
        Log.d("Fetch: ",place);
//        LatLng latLng = getFromName(place);
        Map<String, String> paramMap = new HashMap<>();
//
//        double lat = latLng.latitude;
//        double lng = latLng.longitude;

        double lat =32.8613052;
        double lng = -117.2352116;
                // longt, lat
        paramMap.put("location", lng + "," +lat);
        //paramMap.put("q", "san diego");
        paramMap.put("sort", "distance");
        paramMap.put("feedback", "false");
        paramMap.put("key", "eG53wKfQK8DuhGn4xGwc5evrgBpfwx4w");
        // this sets the category to tourist attractions
        paramMap.put("category", "sic:799972");

        placesRequestApi.getPlaces(paramMap).enqueue(new Callback<PlaceResponse>() {
            @Override
            public void onResponse(Call<PlaceResponse> call, Response<PlaceResponse> response) {
                if (!response.isSuccessful()) {
                    presenter.onError(response.message());
//                    Toast.makeText(HomeActivity.this, "No Success !" + response.message(), Toast.LENGTH_LONG).show();
                }
                List<TriPlace> mPlaceList = response.body().getPlaces();

                for(int i=0; i<mPlaceList.size(); i++) {
//                    System.out.println(mPlaceList.get(i).getName() + " : " + mPlaceList.get(i).getCity());
                }

                TriPlan.TriPlanBuilder myBuilder = new TriPlan.TriPlanBuilder();
                myBuilder.addPlaceList(response.body().getPlaces());

                TriPlan newPlan = myBuilder.buildPlan();
                List<TriPlace> newList = newPlan.getPlaceList();

//                RoutePlanner.setRoutePlanner(newList);
//                TriPlan sortedPlan = RoutePlanner.planRoute(); // sorted plan by algorithm
//                List<TriPlace> sortedList = sortedPlan.getPlaceList();
//
//                // TODO: pass sortedList to Map for pinning
//                ArrayList<String> strll = new ArrayList<String>();
//                for(int i = 0; i < sortedList.size(); i++){
//                    TriPlace curr = sortedList.get(i);
//                    String s = curr.getLatitude() + " " + curr.getLongitude();
//                    strll.add(s);
//                }
                mTriPlan = newPlan;
                presenter.showRoutes(newPlan);
//                Intent intent = new Intent(HomeActivity.this, RouteActivity.class);
//                Bundle bundle = new Bundle();
//                bundle.putStringArrayList("strll", strll);
//                intent.putExtra("BUNDLE", bundle);
//                startActivity(intent);
            }

            @Override
            public void onFailure(Call<PlaceResponse> call, Throwable t) {
                    presenter.onError(t.getMessage());
//                Toast.makeText(HomeActivity.this, "Failed !" + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public String savePlans(String planName) {
        String userId = user.getUid();
        String key = mTriPlan.getId();
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
                presenter.onError(e.getMessage());
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
                presenter.onError(e.getMessage());
            }
        });
        mTriPlan.setId(finalKey);
        return mTriPlan.getId();
    }

    @Override
    public void setPlanId(String id) {
        mTriPlan.setId(id);
    }
    @Override
    public boolean addPlace(TriPlace newPlace) {
        // TODO add place to current plan
        return true;
    }

    @Override
    public void setPresenter(RouteContract.Presenter presenter) {
        this.presenter = presenter;
    }
}
