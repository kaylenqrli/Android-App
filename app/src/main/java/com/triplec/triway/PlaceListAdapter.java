package com.triplec.triway;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
        holder.description.setText(places.get(position).getDescription());
        // Drawable dr = places.getPhoto(position);
        // Adjust image size
        Drawable dr = mContext.getResources().getDrawable(R.drawable.album_city3);
        Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
        Drawable d = new BitmapDrawable(mContext.getResources(), Bitmap.createScaledBitmap(bitmap, 100, 100, true));
        holder.photo.setImageDrawable(d);

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