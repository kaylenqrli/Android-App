package com.triplec.triway;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.triplec.triway.common.TriPlace;
import com.triplec.triway.common.TriPlan;
import com.triplec.triway.route.ListFragment;
import com.triplec.triway.route.MapFragment;

import java.util.Arrays;
import java.util.List;


public class RouteActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    int AUTOCOMPLETE_REQUEST_CODE = 1;

    private boolean isSaved = false;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private ActionBar actionbar;

    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        actionbar = getSupportActionBar();
        actionbar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        actionbar.setDisplayHomeAsUpEnabled(true);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);

        int daynum = getIntent().getIntExtra("dayNum", 1);
        Toast.makeText(this, Integer.toString(daynum), Toast.LENGTH_SHORT).show();
        TabLayout daysTab = findViewById(R.id.days);
        if (daynum == 1) {
            daysTab.setVisibility(View.GONE);
        }
        else {
            daysTab.setVisibility(View.VISIBLE);
            for (int i = 0; i < daynum; i++) {
                daysTab.addTab(daysTab.newTab().setText("Day " + (i + 1)), i);
            }
            daysTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    // TODO: Show plan on day tab.getPosition() <- (begin from 0)
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                    // TODO: Hide plan on this days
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });
        }

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        Bundle bundle = getIntent().getExtras();
        TriPlan mPlan = (TriPlan) bundle.getSerializable("plan");
        if (mPlan != null)
            actionbar.setTitle(mPlan.getName());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_route, menu);

        // Associate searchable configuration with the SearchView
        /*SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.Tabs_menu_add).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(new ComponentName(getApplicationContext(), SearchResultActivity.class)));
        searchView.setQueryHint("Search for another place");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                menu.findItem(R.id.Tabs_menu_add).collapseActionView();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });*/

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.Tabs_menu_save:
                getDialog();
                return true;
            case R.id.Tabs_menu_add:
                // Set the fields to specify which types of place data to
                // return after the user has made a selection.
                List<Place.Field> fields = Arrays.asList(Place.Field.ID,
                                                         Place.Field.NAME,
                                                         Place.Field.LAT_LNG,
                                                         Place.Field.ADDRESS,
                                                         Place.Field.PHOTO_METADATAS);


                // Start the autocomplete intent.
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.FULLSCREEN, fields)
                        .build(this);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);

                Toast.makeText(getApplicationContext(), "Add a new place", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.Tabs_menu_edit:
                Toast.makeText(getApplicationContext(), "Editing plan", Toast.LENGTH_SHORT).show();
                if (getViewPager().getCurrentItem() == 1) {
                    ((ListFragment)mSectionsPagerAdapter.getItem(1)).setEdit();
                    menu.findItem(R.id.Tabs_menu_add).setVisible(false);
                    menu.findItem(R.id.Tabs_menu_save).setVisible(false);
                    menu.findItem(R.id.Tabs_menu_edit).setVisible(false);
                    menu.findItem(R.id.Tabs_menu_delete).setVisible(false);
                    menu.findItem(R.id.Tabs_menu_delete_place).setVisible(true);
                    menu.findItem(R.id.Tabs_menu_cancel_delete).setVisible(true);
                }
                else {
                    ((ListFragment)mSectionsPagerAdapter.getItem(1)).setEdit();
                    getViewPager().setCurrentItem(1);
                }
                return true;
            case R.id.Tabs_menu_delete:
                Toast.makeText(getApplicationContext(), "Plan deleted", Toast.LENGTH_SHORT).show();
                actionbar.setTitle("Try your way");
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                addPlace(place);
                Log.i("----- autocomplete", "Place: " + place.getName() + ", " + place.getId());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("----- autocomplete", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {}
        }
    }

    private void addPlace(Place place){
        LatLng latLng = place.getLatLng();
        if (latLng == null) {
            Toast.makeText(RouteActivity.this,
                    place.getName() + " can't be found.", Toast.LENGTH_SHORT).show();
            return;
        }
        TriPlace newPlace = new TriPlace();
        newPlace.setLatitude(latLng.latitude);
        newPlace.setLongitude(latLng.longitude);
        newPlace.setName(place.getName());
        newPlace.setStreet(place.getAddress());
        newPlace.setCity("");
        newPlace.setId(place.getId());
        ListFragment lf = (ListFragment) findFragmentByPosition(1);
        MapFragment mf = (MapFragment) findFragmentByPosition(0);
        mf.addPlace(newPlace);
        lf.addPlace(newPlace);
    }

    private void getDialog() {
//        MapFragment test = (MapFragment) this.getSupportFragmentManager().findFragmentById(R.id.test_fragment);

        LayoutInflater linf = LayoutInflater.from(this);
        final View inflator = linf.inflate(R.layout.route_change_name_dialog, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Rename Plan");
        alert.setView(inflator);
        final TextInputEditText plan_rename = inflator.findViewById(R.id.plan_rename_text);
        final TextInputLayout plan_rename_layout = inflator.findViewById(R.id.plan_rename_layout);
        if (actionbar.getTitle().equals("Try your way"))
            plan_rename.setText("My Plan");
        else
            plan_rename.setText(actionbar.getTitle());
        plan_rename.requestFocusFromTouch();
        plan_rename.setSelection(0, plan_rename.getText().length());
        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                String plan_name=plan_rename.getText().toString();
                //do operations using s1 and s2 here...
                MapFragment mf = (MapFragment)findFragmentByPosition(0);
                ListFragment lf = (ListFragment)findFragmentByPosition(1);
                if (getViewPager().getCurrentItem() == 0) {
                    String planId = mf.savePlans(plan_name);
                    if (planId.length() !=0 ) {
                        lf.setTriPlanId(planId);
                        actionbar.setTitle(plan_name);
                    }

                }
                else {
                    String planId = lf.savePlans(plan_name);
                    if (planId.length() !=0 ) {
                        mf.setTriPlanId(planId);
                        actionbar.setTitle(plan_name);
                    }
                }
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = alert.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.show();
    }
    private Fragment findFragmentByPosition(int position) {
        FragmentPagerAdapter fragmentPagerAdapter = getFragmentPagerAdapter();
        return getSupportFragmentManager().findFragmentByTag(
                "android:switcher:" + getViewPager().getId() + ":"
                        + fragmentPagerAdapter.getItemId(position));
    }

    private ViewPager getViewPager() {
        return mViewPager;
    }

    private FragmentPagerAdapter getFragmentPagerAdapter() {
        return mSectionsPagerAdapter;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        private MapFragment mapFragment;
        private ListFragment listFragment;

        private SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            //Bundle bundle = intent.getBundleExtra("strlist");
            Bundle bundle = getIntent().getExtras();
            switch (position) {
                case 0:
                    if (mapFragment == null) {
                        mapFragment = new MapFragment().newInstance();
                    }
                    mapFragment.setArguments(bundle);
                    return mapFragment;
                case 1:
                    if (listFragment == null) {
                        listFragment = new ListFragment().newInstance();
                    }
                    listFragment.setArguments(bundle);
                    return listFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }
    }
}
