package com.triplec.triway;

import android.content.Intent;
import android.os.Bundle;
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
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;

import java.util.ArrayList;
import java.util.List;

public class RouteActivity extends FragmentActivity {

    private static final int LIST = 0;
    private static final int MAP = 1;

    private AutoCompleteTextView searchAddEditText;
    private ArrayAdapter<String> madapter;
    private String city = "北京";

    private boolean isSaved = false;

    SuggestionSearch mSuggestionSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);
        Intent intent = getIntent();
        String tmp = intent.getStringExtra("city");
        if(tmp != null){
            city = tmp;
        }

        SwitchFragment(MAP);
        searchAddEditText = (AutoCompleteTextView) findViewById(R.id.search_add_text);
        madapter = new ArrayAdapter<String>(RouteActivity.this,
                android.R.layout.simple_dropdown_item_1line);
        searchAddEditText.setAdapter(madapter);
        SDKInitializer.setHttpsEnable(true);
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(listener);

        searchAddEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSuggestionSearch.requestSuggestion((new SuggestionSearchOption())
                        .keyword(searchAddEditText.getText().toString())
                        .city(city));
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

    }

    @Override
    protected void onDestroy(){
        mSuggestionSearch.destroy();
        super.onDestroy();
    }
    OnGetSuggestionResultListener listener = new OnGetSuggestionResultListener() {
        @Override
        public void onGetSuggestionResult(SuggestionResult suggestionResult) {
            //处理sug检索结果
            if (suggestionResult == null || suggestionResult.getAllSuggestions() == null) {
                Toast.makeText(RouteActivity.this, "Suggestions Not Found", Toast.LENGTH_SHORT).show();
                return;
            }

            List<SuggestionResult.SuggestionInfo> resl = suggestionResult.getAllSuggestions();
            madapter.clear();
            for (int i = 0; i < resl.size(); i++) {
                madapter.add(resl.get(i).key);
                madapter.notifyDataSetChanged();
                Log.i("result: ", "adapter size = " + madapter.getCount() + " suggestions["  + i + "] = " + resl.get(i).key);
            }
            madapter.notifyDataSetChanged();
        }
    };

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
