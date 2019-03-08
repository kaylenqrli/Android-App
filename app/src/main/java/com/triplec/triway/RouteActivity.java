package com.triplec.triway;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
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
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;


import com.google.android.libraries.places.api.net.PlacesClient;
import com.triplec.triway.common.TriPlan;
import com.triplec.triway.route.ListFragment;
import com.triplec.triway.route.MapFragment;

import java.util.ArrayList;


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

    //private AutoCompleteTextView searchAddEditText;
    private ArrayAdapter<String> madapter;

    private boolean isSaved = false;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private TriPlan plan;
    private ActionBar actionbar;

    private PlacesClient placesClient;

    public TriPlan getPlan() {
        return plan;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        /* Autocomplete */

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionbar = getSupportActionBar();
        actionbar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        actionbar.setDisplayHomeAsUpEnabled(true);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_route, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
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
        });

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
                Toast.makeText(getApplicationContext(), "Add a new place", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.Tabs_menu_edit:
                Toast.makeText(getApplicationContext(), "Editing plan", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.Tabs_menu_delete:
                Toast.makeText(getApplicationContext(), "Plan deleted", Toast.LENGTH_SHORT).show();
                actionbar.setTitle("Try your way");
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getDialog() {
        LayoutInflater linf = LayoutInflater.from(this);
        final View inflator = linf.inflate(R.layout.route_change_name_dialog, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Rename Plan");
        alert.setView(inflator);
        final TextInputEditText plan_rename = inflator.findViewById(R.id.plan_rename_text);
        final TextInputLayout plan_rename_layout = inflator.findViewById(R.id.plan_rename_layout);
        plan_rename.setText("My Plan");
        plan_rename.setSelection(0, plan_rename.getText().length());
        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                String plan_name=plan_rename.getText().toString();
                //do operations using s1 and s2 here...
                Toast.makeText(getApplicationContext(), "Plan saved as " + plan_name, Toast.LENGTH_SHORT).show();
                actionbar.setTitle(plan_name);
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });
        alert.show();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
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
                    MapFragment mf = new MapFragment().newInstance();
                    mf.setArguments(bundle);
                    return mf;
                case 1:
                    ListFragment lf = new ListFragment().newInstance();
                    lf.setArguments(bundle);
                    return lf;
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
