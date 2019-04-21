package com.triplec.triway;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.triplec.triway.common.TriPlace;

import java.util.List;

public class MapListAdapter extends RecyclerView.Adapter<MapListAdapter.MyViewHolder> {
    private List<TriPlace> places;
    Context mContext;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        private final RelativeLayout view;
        private final TextView name;
        private final TextView description;
        private final ImageView photo;
        public MyViewHolder(RelativeLayout v) {
            super(v);
            view = v;
            name = v.findViewById(R.id.map_place_name);
            description = v.findViewById(R.id.map_place_description);
            photo = v.findViewById(R.id.map_place_photo);
        }

        public TextView getNameTV() {
            return name;
        }

        public TextView getDescriptionTV() {
            return description;
        }

        public ImageView getPhotoIV() {
            return photo;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MapListAdapter(Context context, List<TriPlace> places) {
        this.mContext = context;
        this.places = places;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MapListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        // create a new view
        RelativeLayout v = (RelativeLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.route_map_list_item, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.getNameTV().setText(places.get(position).getName());
        holder.getDescriptionTV().setText(places.get(position).getDescription());
        holder.getPhotoIV().setImageResource(R.drawable.album_city3);
        if (places.get(position).getId() != null && !places.get(position).getId().isEmpty()) {
            holder.getPhotoIV().setImageBitmap(places.get(position).getPhoto(mContext, this));
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (places==null) {
            return 0;
        }
        return places.size();
    }
}