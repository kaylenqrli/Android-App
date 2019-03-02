package com.triplec.triway;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;


public class RouteActivity extends FragmentActivity {

    private static final int LIST = 0;
    private static final int MAP = 1;

    private AutoCompleteTextView searchAddEditText;
    private ArrayAdapter<String> madapter;
    //private String city = "北京";

    private boolean isSaved = false;


    private PlacesClient placesClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);
        //Intent intent = getIntent();
        //String tmp = intent.getStringExtra("city");
        //if(tmp != null){
        //    city = tmp;
        //}

        SwitchFragment(MAP);
        searchAddEditText = (AutoCompleteTextView) findViewById(R.id.search_add_text);
        madapter = new ArrayAdapter<String>(RouteActivity.this,
                android.R.layout.simple_dropdown_item_1line);
        searchAddEditText.setAdapter(madapter);

        Places.initialize(getApplicationContext(), "AIzaSyDYKAtsvLfqJnT_t1VhAjvrLMb2cddLcVQ");
        placesClient = Places.createClient(this);

        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        searchAddEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Create a RectangularBounds object.
                RectangularBounds bounds = RectangularBounds.newInstance(
                        new LatLng(-33.880490, 151.184363),
                        new LatLng(-33.858754, 151.229596));
                // Use the builder to create a FindAutocompletePredictionsRequest.
                FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                        // Call either setLocationBias() OR setLocationRestriction().
                        //.setLocationRestriction(bounds)
                        .setTypeFilter(TypeFilter.ADDRESS)
                        .setSessionToken(token)
                        .setQuery(searchAddEditText.getText().toString())
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
                        Toast.makeText(RouteActivity.this, "Not found :(", Toast.LENGTH_SHORT).show();
                        Log.e("not found", "Place not found: " + apiException.getMessage());
                    }
                });

            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


    }

    public void SearchAndAdd(View v) {
        String toSearch = searchAddEditText.getText().toString();
        if (!toSearch.equals("")) {
            Toast.makeText(RouteActivity.this, "Searching " + toSearch, Toast.LENGTH_SHORT).show();
        }
    }

    public void ShowList(View v) {
        SwitchFragment(LIST);
    }

    public void ShowMap(View  v) {
        SwitchFragment(MAP);
    }

    // Pseudo Function for AddToMyList, assumes the same List.
    public void AddToMyList(View v){
        if (!isSaved) {
            Toast.makeText(RouteActivity.this, "Saved to My List", Toast.LENGTH_SHORT).show();
            ((FloatingActionButton) v).setImageResource(R.drawable.ic_star_black_24dp);
            isSaved = true;
        }
        else {
            Toast.makeText(RouteActivity.this, "Removed from My List", Toast.LENGTH_LONG).show();
            ((FloatingActionButton) v).setImageResource(R.drawable.ic_star_border_black_24dp);
            isSaved = false;
        }
    }

    /* AddToMyList with save and delete List realized. UI update included.
    public void AddToMyList(View v){
        List curList = getCurrentList();
        if (!isSaved(curList)) {
            Toast.makeText(RouteActivity.this, "Saved to My List", Toast.LENGTH_LONG).show();
            ((FloatingActionButton) v).setImageResource(R.drawable.ic_star_black_24dp);
            saveList(curList);
        }
        else {
            Toast.makeText(RouteActivity.this, "Removed from My List", Toast.LENGTH_LONG).show();
            ((FloatingActionButton) v).setImageResource(R.drawable.ic_star_border_black_24dp);
            deleteList(curList);
        }
    }*/

    public void SwitchFragment(int frag) {
        Fragment fragment = null;
        switch (frag) {
            case LIST:
                fragment = new ListFragment();
                break;
            case MAP:
                fragment = new MapFragment();
                break;
        }
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.show, fragment).commit();
    }

    /* ArrayAdapter for ListView
    class PlaceAdapter extends ArrayAdapter<Place>{

        public PlaceAdapter(@androidx.annotation.NonNull Context context, int resource, @androidx.annotation.NonNull List<Place> objects) {
            super(context, R.layout.list_item, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            Place place = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
            }
            // Lookup view for data population
            ImageView photo = (ImageView) convertView.findViewById(R.id.place_photo);
            TextView name = (TextView) convertView.findViewById(R.id.place_name);
            TextView description = (TextView) convertView.findViewById(R.id.place_description);
            // Populate the data into the template view using the data object
            photo.setImageBitmap(place.getPhoto());
            name.setText(place.getName());
            description.setText(place.getDescription());
            // Return the completed view to render on screen
            return convertView;
        }
    }
    */

}
