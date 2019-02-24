package com.triplec.triway;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

public class SavedPlanActivity extends Activity {
    ArrayList<String> listItems;
    ListView listView;
    SavedPlanAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_plan);

        listItems = new ArrayList<String>();
        listItems.add("item 1");
        listItems.add("item 2");

        adapter = new SavedPlanAdapter(listItems, this);

        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        if(name != null){
            addItem(name);
        }
    }

    private void addItem(String name) {
        listItems.add(name);
        adapter.notifyDataSetChanged();
    }
}
