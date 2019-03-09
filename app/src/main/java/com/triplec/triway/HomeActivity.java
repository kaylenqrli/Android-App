package com.triplec.triway;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.triplec.triway.common.RoutePlanner;
import com.triplec.triway.common.TriPlace;
import com.triplec.triway.common.TriPlan;
import com.triplec.triway.retrofit.PlaceRequestApi;
import com.triplec.triway.retrofit.RetrofitClient;
import com.triplec.triway.retrofit.response.PlaceResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Views, Layouts, and Adapters
    Toolbar toolbar;
    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    NavigationView navigationView;
    AutoSlideViewPager viewPager;
    PagerAdapter adapter;
    AutoCompleteTextView search;
    TextView user_name_tv, user_email_tv;
    boolean updated = false;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    SharedPreferences sp;

    private PlacesClient placesClient;
    private ArrayAdapter<String> madapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupFirebaseListener();

        // set up toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        // set up drawer
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerStateChanged (int newState) {
                super.onDrawerStateChanged(newState);
                if (!updated) {
                    updateUserInfo();
                    updated = true;
                }
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // change toolbar icon and event
        toolbar.setNavigationIcon(R.drawable.ic_person_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                drawer.openDrawer(GravityCompat.START,true);
            }
        });

        // set up navigation
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // set up album
        viewPager = (AutoSlideViewPager) findViewById(R.id.viewPager);
        adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setAutoPlay(true);

        // set up searchView
        Places.initialize(getApplicationContext(), "AIzaSyDYKAtsvLfqJnT_t1VhAjvrLMb2cddLcVQ");
        placesClient = Places.createClient(this);
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        search = (AutoCompleteTextView) findViewById(R.id.searchView);
        madapter = new ArrayAdapter<String>(HomeActivity.this,
                android.R.layout.simple_dropdown_item_1line);
        search.setAdapter(madapter);

        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    gotoRoute(search.getText().toString());
                    handled = true;
                }
                return handled;
            }
        });
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Use the builder to create a FindAutocompletePredictionsRequest.
                FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                        // Call either setLocationBias() OR setLocationRestriction().
                        //.setLocationRestriction(bounds)
                        .setTypeFilter(TypeFilter.CITIES)
                        .setSessionToken(token)
                        .setQuery(search.getText().toString())
                        .build();

                placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
                    madapter.clear();
                    for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                        //Log.i("result: ", prediction.getPlaceId());
                        madapter.add(prediction.getPrimaryText(null).toString());
                        Log.i("result", prediction.getPrimaryText(null).toString());
                    }
                    madapter.notifyDataSetChanged();
                }).addOnFailureListener((exception) -> {
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        Toast.makeText(HomeActivity.this, "Not found :(", Toast.LENGTH_SHORT).show();
                        Log.e("not found", "Place not found: " + apiException.getMessage());
                    }
                });

            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

    }

    private void updateUserInfo() {
        // set up Name and Email
        user_name_tv = findViewById(R.id.user_name_text);
        user_email_tv = findViewById(R.id.user_email_text);
        String user_email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String user_name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        if ((user_name == null) || (user_name.isEmpty())) {
            user_name = user_email.substring(0,user_email.indexOf("@"));
        }
        user_name_tv.setText(user_name);
        user_email_tv.setText(user_email);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.action_settings:
                Toast.makeText(HomeActivity.this,"setting clicked", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_signout:
                Toast.makeText(HomeActivity.this,"Sign Out Successful", Toast.LENGTH_SHORT).show();
                FirebaseAuth.getInstance().signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop(){
        super.onStop();
        if(mAuthStateListener != null){
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthStateListener);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // close drawer
        drawer.closeDrawer(GravityCompat.START, true);

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.drawer_saved_plan:
                gotoSavedPlan(null);
                break;
            case R.id.drawer_sign_out:
                FirebaseAuth.getInstance().signOut();
                break;
        }

        return true;
    }

    // test adding menu item
    public void removeItem(int itemId) {
        navigationView.getMenu().removeItem(itemId);
    }

    public void addItem(String title) {
        navigationView.getMenu().add(title);
    }

    public void gotoSavedPlan(View v){
        Intent intent = new Intent(this, SavedPlanActivity.class);
        intent.putExtra("name", "item 3");
        startActivity(intent);
    }

    public void gotoRoute(String place) {
        Toast.makeText(HomeActivity.this,
                "Searching "+ place + "...", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(HomeActivity.this, RouteActivity.class);
        intent.putExtra("place",place );
        startActivity(intent);
    }

    private void setupFirebaseListener(){
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if( user == null ){
                    openLoginActivity();
                }
            }
        };
    }

    public void openLoginActivity(){
        sp = getSharedPreferences("login", MODE_PRIVATE);
        sp.edit().putBoolean("logged", false).apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
