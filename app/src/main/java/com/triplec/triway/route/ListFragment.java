package com.triplec.triway.route;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.PolylineOptions;
import com.triplec.triway.PlaceListAdapter;
import com.triplec.triway.R;
import com.triplec.triway.common.TriPlace;
import com.triplec.triway.common.TriPlan;
import com.triplec.triway.mvp.MvpFragment;

/**
 * A simple {@link Fragment} subclass.
 */
        public class ListFragment extends MvpFragment<RouteContract.Presenter> implements RouteContract.View{
    PlaceListAdapter adapter;
    ListView list;

    public static ListFragment newInstance() {

        Bundle args = new Bundle();

        ListFragment fragment = new ListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public RouteContract.Presenter getPresenter() {
        return new RoutePresenter();
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
                SparseBooleanArray selected = adapter.getSelectedIds();
                for(int i = selected.size() - 1; i >= 0; i--){
                    if(selected.valueAt(i)){
                        TriPlace p = adapter.getItem(selected.keyAt(i));
                        adapter.remove(p);
                    }
                }
                adapter.removeSelection();
                return true;
            default:
                return false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        // set up list with adapter
        list = (ListView)view.findViewById(R.id.route_list);


        return view;
    }

    @Override
    public void showRoutes(TriPlan placePlan) {
        //TODO
        TriPlan.TriPlanBuilder builder = new TriPlan.TriPlanBuilder();
        builder.addPlaceList(placePlan.getPlaceList());
        TriPlan plan = builder.buildPlan();
        adapter = new PlaceListAdapter
                (getActivity().getApplicationContext(), R.layout.fragment_list, plan.getPlaceList());
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // toggle selected status
                adapter.toggleSelection(position);
            }
        });
    }

    @Override
    public void onError(String message) {
        Toast.makeText(getActivity(), "Error: " + message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSavedSuccess(String planName) {
        Toast.makeText(getActivity(), "Plan saved as " + planName, Toast.LENGTH_SHORT).show();
    }

    @Override
    public String getMainPlace() {
        if (getArguments() != null)
            return getArguments().getString("place");
        else
            return "";
    }

    @Override
    public String savePlans(String plan_name) {
        return this.presenter.savePlans(plan_name);
    }

    @Override
    public boolean addPlace(TriPlace newPlace) {
        adapter.add(newPlace);
        Toast.makeText(getContext(), "Add a new place: " + newPlace.getName(), Toast.LENGTH_SHORT).show();
        return this.presenter.addPlace(newPlace);
    }

    @Override
    public TriPlan getPassedPlan() {
        if (getArguments() != null)
            return (TriPlan) getArguments().getSerializable("plan");
        else
            return null;
    }

    @Override
    public Context getContext() {
        return this.getActivity();
    }

    @Override
    public void addPolyline(PolylineOptions lineOptions) {
        return;
    }

    public void setTriPlanId(String id) {
        presenter.setPlanId(id);
    }
}
