package com.triplec.triway;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class RouteActivity extends FragmentActivity {

    private static final int LIST = 0;
    private static final int MAP = 1;

    private EditText searchAddEditText;

    private boolean isSaved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);
        SwitchFragment(MAP);
        searchAddEditText = (EditText) findViewById(R.id.search_add_text);
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
