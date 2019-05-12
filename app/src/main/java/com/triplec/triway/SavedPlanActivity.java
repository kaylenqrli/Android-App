package com.triplec.triway;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.triplec.triway.common.TriPlace;
import com.triplec.triway.common.TriPlan;

import java.util.ArrayList;
import java.util.List;

// TODO: change database structure
public class SavedPlanActivity extends Activity {
    ArrayList<TriPlan> listItems;
    ListView listView;
    SavedPlanAdapter adapter;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_plan);

        listItems = new ArrayList<TriPlan>();
        adapter = new SavedPlanAdapter(listItems, this);

        listView = findViewById(R.id.listView);
        listView.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        String userId = user.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("plans");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapm: dataSnapshot.getChildren()) {
                    TriPlan mPlan = snapm.getValue(TriPlan.class);
                    List<TriPlace> newList = new ArrayList<TriPlace>();
                    mPlan.setDate(snapm.child("createdAt").getValue(String.class));
                    for ( DataSnapshot snapPlace : snapm.child("places").getChildren() ) {
                        TriPlace mPlace = snapm.getValue(TriPlace.class);
                        mPlace.setLongitude(snapPlace.child("longitude").getValue(Double.class));
                        mPlace.setLatitude(snapPlace.child("latitude").getValue(Double.class));
                        mPlace.setCity(snapPlace.child("city").getValue(String.class));
                        mPlace.setName(snapPlace.child("name").getValue(String.class));
                        mPlace.setPostalCode(snapPlace.child("postalCode").getValue(String.class));
                        mPlace.setStateCode(snapPlace.child("stateCode").getValue(String.class));
                        mPlace.setStreet(snapPlace.child("street").getValue(String.class));
                        mPlace.setId(snapPlace.child("id").getValue(String.class));
                        newList.add(mPlace);
                    }
                    mPlan.setList(newList);
                    mPlan.setId(snapm.getKey());
                    listItems.add(mPlan);
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
//                Log.e(TAG, "onCancelled", databaseError.toException());
            }
        });

    }

}
