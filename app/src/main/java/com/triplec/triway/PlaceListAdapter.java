package com.triplec.triway;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.triplec.triway.common.TriPlace;

import java.util.List;

/**
 * Adapter for list fragment. Allows multi-selection and multi-deletion.
 * source: https://www.androidbegin.com/tutorial/android-delete-multiple-selected-items-listview-tutorial/
 */
public class PlaceListAdapter extends ArrayAdapter<TriPlace> {
    List<TriPlace> places;
    Context mContext;
    LayoutInflater inflater;
    private SparseBooleanArray mSelectedItemsIds;
    View convert;

    /*----- Place Id for getPhoto() -----*/
//    private final String[] placeIds = {
//            "ChIJyYB_SZVU2YARR-I1Jjf08F0",  // San Diego Zoo
//            "ChIJA8tw-pZU2YARxPYVsDwL8-0",  // Balboa Park
//            "ChIJ7-bxRDmr3oARawtVV_lGLtw",  // Airport
//            "ChIJ54O2gpEG3IAR0YlUGyNK1GQ",  // Black's Beach
//            "ChIJT69MQcQG3IARpz6Rifyqtu8"   // UCSD
//    };
    /*----- Place Id for getPhoto() -----*/

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
            convertView = inflater.inflate(R.layout.route_list_item, null);
            // Locate the TextViews in listview_item.xml
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.place_checkbox);
            holder.name = (TextView) convertView.findViewById(R.id.place_name);
            holder.description = (TextView) convertView.findViewById(R.id.place_description);
            holder.photo = (ImageView) convertView.findViewById(R.id.place_photo);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        // set name and description
        holder.name.setText(places.get(position).getName());
        holder.description.setText(places.get(position).getDescription());
        // set photo
        Bitmap bitmap = places.get(position).getPhoto(mContext, this);
        holder.photo.setImageBitmap(bitmap);
        notifyDataSetChanged();

        // toggle checkbox and remove animation
        holder.checkBox.setChecked(mSelectedItemsIds.get(position));
        holder.checkBox.jumpDrawablesToCurrentState();

        convert = convertView;
        return convertView;
    }

    @Override
    public void remove(TriPlace p) {
        places.remove(p);
        notifyDataSetChanged();
    }

    @Override
    public void add(TriPlace p){
        places.add(p);
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
