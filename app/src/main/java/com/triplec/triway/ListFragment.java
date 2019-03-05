package com.triplec.triway;


import android.content.ClipData;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.triplec.triway.common.TriPlace;
import com.triplec.triway.common.TriPlan;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListFragment extends Fragment {
    PlaceListAdapter adapter;
    ListView list;

    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.Tabs_menu_delete:
                //  delete all selected places
                //boolean[] selected = adapter.getSelected();
                SparseBooleanArray selected = adapter.getSelectedIds();
                for(int i = selected.size() - 1; i >= 0; i--){
                    if(selected.valueAt(i)){
                        TriPlace p = adapter.getItem(selected.keyAt(i));
                        adapter.remove(p);
                    }
                }
                adapter.removeSelection();
                return true;
            case R.id.Tabs_menu_edit:
                return true;
            default:
                return false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        //TriPlan plan = ((RouteActivity)getActivity()).getPlan();

        /*----- pseudo plan for testing -----*/
        TriPlan.TriPlanBuilder builder = new TriPlan.TriPlanBuilder();
        for(int i = 0; i < 5; i++){
            builder.addPlace(new TriPlace("place " + i));
        }
        TriPlan plan = builder.buildPlan();
        /*----- pseudo plan for testing -----*/

        list = (ListView)view.findViewById(R.id.route_list);
        adapter = new PlaceListAdapter
                (getActivity().getApplicationContext(), R.layout.fragment_list, plan.getPlaceList());
        list.setAdapter(adapter);
        list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // toggle selected status
                adapter.toggleSelection(position);
            }
        });

        return view;
    }

    public class PlaceListAdapter extends ArrayAdapter<TriPlace> {
        List<TriPlace> places;
        Context mContext;
        LayoutInflater inflater;
        private SparseBooleanArray mSelectedItemsIds;
        View convert;

        public PlaceListAdapter (Context context, int resourceId, List<TriPlace> places) {
            super(context, resourceId, places);
            this.mContext = context;
            mSelectedItemsIds = new SparseBooleanArray();
            inflater = LayoutInflater.from(context);
            this.places = places;
        }

        private class ViewHolder {
            CheckBox checkBox;
            TextView name;
            TextView description;
            ImageView photo;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            final ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.list_item, null);
                // Locate the TextViews in listview_item.xml
                holder.checkBox = (CheckBox) convertView.findViewById(R.id.place_checkbox);
                holder.name = (TextView) convertView.findViewById(R.id.place_name);
                holder.description = (TextView) convertView.findViewById(R.id.place_description);
                holder.photo = (ImageView) convertView.findViewById(R.id.place_photo);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            // Capture position and set to the TextViews
            holder.name.setText(places.get(position).getName());
            holder.description.setText(places.get(position).getStreet());
            //holder.photo.setImageResource(places.get(position).getPhoto());
            //holder.name.setText("name " + position);
            //holder.description.setText("description " + position);
            holder.photo.setImageResource(R.drawable.album_city3);

            holder.checkBox.setChecked(mSelectedItemsIds.get(position));

            convert = convertView;
            return convertView;
        }

        @Override
        public void remove(TriPlace p) {
            places.remove(p);
            notifyDataSetChanged();
        }

        public void selectView(int position, boolean value) {
            if (value) {
                mSelectedItemsIds.put(position, value);
            } else {
                mSelectedItemsIds.delete(position);
            }
            notifyDataSetChanged();
        }

        public void toggleSelection(int position) {
            selectView(position, !mSelectedItemsIds.get(position));
            notifyDataSetChanged();
        }

        public SparseBooleanArray getSelectedIds() {
            return mSelectedItemsIds;
        }

        public void removeSelection() {
            mSelectedItemsIds = new SparseBooleanArray();
            notifyDataSetChanged();
        }

    }
}
