package com.triplec.triway;

import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /* Views, Layouts, and Adapters */
    Toolbar toolbar;
    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    NavigationView navigationView;
    AutoSlideViewPager viewPager;
    PagerAdapter adapter;
    AutoCompleteTextView search;
    TextView user_name_tv, user_email_tv;
    boolean updated = false;
    private static boolean isclicked = false;

    private int days = 1;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    SharedPreferences sp;
    GoogleSignInClient mGoogleSignInClient;
    Spinner spinner;

    private PlacesClient placesClient;
    private ArrayAdapter<String> madapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupFirebaseListener();

        // used to logout user's google account if the user uses google account to login
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // set up toolbar
        toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        // set up drawer
        drawer = findViewById(R.id.drawer_layout);
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
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // set up album
        viewPager = findViewById(R.id.viewPager);
        adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setAutoPlay(true);

        // set up searchView
        Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        placesClient = Places.createClient(this);
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        search = findViewById(R.id.searchView);
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

        // autocomplete
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Use the builder to create a FindAutocompletePredictionsRequest.
                FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                        // Call either setLocationBias() OR setLocationRestriction().
                        //.setLocationRestriction(bounds)
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

        spinner = findViewById(R.id.main_days_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.days_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                days = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
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

    // Temporarily hide the top menu.
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        switch (id){
//            case R.id.action_settings:
//                Toast.makeText(HomeActivity.this,"setting clicked", Toast.LENGTH_SHORT).show();
//                return true;
//            case R.id.action_signout:
//                Toast.makeText(HomeActivity.this,"Sign Out Successful", Toast.LENGTH_SHORT).show();
//                FirebaseAuth.getInstance().signOut();
//                // logout user's google account if user uses google account to login
//                mGoogleSignInClient.signOut();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

    @Override
    public void onStart(){
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener);
        isclicked = false;
    }

    @Override
    public void onStop(){
        super.onStop();
        if(mAuthStateListener != null){
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        isclicked = false;
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
                // logout user's google account if user uses google account to login
                mGoogleSignInClient.signOut();
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
        startActivity(intent);
    }

    public void gotoRoute(String place) {
        if( isclicked ){
            return;
        }
        else{
            isclicked = true;
        }
        Toast.makeText(HomeActivity.this,
                "Searching "+ place + "...", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(HomeActivity.this, RouteActivity.class);
        intent.putExtra("place",place );
        intent.putExtra("dayNum", getDays());
        startActivity(intent);
    }

    /**
     * check if the auth state has changed, if auth logged out, go back to login activity
     */
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

    public static boolean getIsClicked(){
        return isclicked;
    }

    public static void setisClickedTrue(){
        isclicked = true;
    }

    public static void setIsclickedFalse(){
        isclicked = false;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }
}
