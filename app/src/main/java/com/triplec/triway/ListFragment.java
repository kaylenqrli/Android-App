package com.triplec.triway;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.triplec.triway.common.TriPlace;
import com.triplec.triway.common.TriPlan;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListFragment extends Fragment {


    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        TriPlan plan = ((RouteActivity)getActivity()).getPlan();

        /*String [] names = new String[5];
        for(int i = 0; i < 5; i++){
            names[i] = "name" + i;
        }*/

        System.out.println("===== HERE!!! =====");
        ListView list = (ListView)view.findViewById(R.id.route_list);
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>
        //        (getActivity().getApplicationContext(), R.layout.list_item,R.id.place_name, names);

        PlaceListAdapter adapter = new PlaceListAdapter(getActivity().getApplicationContext(), plan);
        list.setAdapter(adapter);

        return view;
    }


    public class PlaceListAdapter extends BaseAdapter {
        TriPlan plan;
        List<TriPlace> places;
        Context mContext;

        public PlaceListAdapter (Context context, TriPlan plan) {
            this.plan = plan;
            this.mContext = context;
            //places = plan.getPlaceList();
        }

        @Override
        public int getCount(){
            //return places.size();
            return 5;
        }

        @Override
        public Object getItem(int position){
            return places.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            //TriPlace p = places.get(position);
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item,null);

            TextView name = (TextView)convertView.findViewById(R.id.place_name);
            TextView description = (TextView)convertView.findViewById(R.id.place_description);
            ImageView photo = (ImageView)convertView.findViewById(R.id.place_photo);

            //name.setText(p.getName());
            //description.setText(p.getStreet() + p.getCity());
            //photo.setImageDrawable();

            name.setText("name " + position);
            description.setText("description " + position);
            //photo.setImageDrawable();

            return convertView;
        }
    }
}
