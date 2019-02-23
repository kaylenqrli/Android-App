package com.triplec.triway;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Views, Layouts, and Adapters
    Toolbar toolbar;
    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    NavigationView navigationView;
    AutoSlideViewPager viewPager;
    PagerAdapter adapter;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_main);

        // set up toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // set up drawer
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                        this, drawer, toolbar, R.string.navigation_drawer_open,
                         R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // change toolbar icon
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_avatar);

        // set up navigation
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // set up album
        viewPager = (AutoSlideViewPager) findViewById(R.id.viewPager);
        adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setAutoPlay(true);

        // set up searchView
        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query) {

                System.out.println("submit: " + query); // TODO: delete after done testing
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText){
                System.out.println(newText); // TODO: delete after done testing
                return false;
            }
        });

        //TODO: delete after done testing
        //removeItem(R.id.nav_plan2);
        //addItem("New plan");
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_plan1) {
            // go to saved plan 1
        } else if (id == R.id.nav_plan2) {
            // go to saved plan 2
        } else if (id == R.id.nav_plan3) {
            // go to saved plan 3
        } else if (id == R.id.nav_plan4) {
            // go to saved plan 4
        } else if (id == R.id.nav_plan5) {
            // go to saved plan 5
        }

        // close drawer
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // test adding menu item
    public void removeItem(int itemId) {
        navigationView.getMenu().removeItem(itemId);
    }

    public void addItem(String title) {
        navigationView.getMenu().add(title);
    }
}
