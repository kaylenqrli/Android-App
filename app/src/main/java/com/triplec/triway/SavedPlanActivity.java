package com.triplec.triway;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.triplec.triway.common.TriPlace;

import java.util.ArrayList;

public class SavedPlanActivity extends Activity {
    ArrayList<String> listItems;
    ListView listView;
    SavedPlanAdapter adapter;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_plan);

        listItems = new ArrayList<String>();
        adapter = new SavedPlanAdapter(listItems, this);

        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

//        Intent intent = getIntent();
//        String name = intent.getStringExtra("name");
//        if(name != null){
//            addItem(name);
//        }
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        String userId = user.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("plans");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapm: dataSnapshot.getChildren()) {
                    String namePlan = snapm.child("name").getValue(String.class);
                    Log.d("plan name: ", namePlan);
                    listItems.add(namePlan);
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
//                Log.e(TAG, "onCancelled", databaseError.toException());
            }
        });

    }

    private void addItem(String name) {
        listItems.add(name);
        adapter.notifyDataSetChanged();
    }
}
