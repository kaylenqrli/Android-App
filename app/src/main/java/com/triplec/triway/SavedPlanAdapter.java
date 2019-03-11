package com.triplec.triway;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.button.MaterialButton;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.triplec.triway.common.TriPlan;

import java.util.ArrayList;

public class SavedPlanAdapter extends BaseAdapter implements ListAdapter {
    private ArrayList<TriPlan> list = new ArrayList<TriPlan>();
    private Context context;

    public SavedPlanAdapter(ArrayList<TriPlan> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int pos) {
        return list.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return 0;
        //just return 0 if your list items do not have an Id variable.
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.saved_plan_list_item, null);
        }

        //Handle TextView and display string from your list
        TextView listItemText = (TextView)view.findViewById(R.id.card_title);
        listItemText.setText(list.get(position).getName());
        TextView listItemDescription = (TextView)view.findViewById(R.id.card_description);
        listItemDescription.setText(list.get(position).getDateModified());
        //Handle buttons and add onClickListeners
        MaterialButton deleteBtn = view.findViewById(R.id.card_delete);
        MaterialButton editBtn = view.findViewById(R.id.card_edit);

        deleteBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(R.string.dialog_delete_message)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String key = list.get(position).getId();
                                FirebaseDatabase.getInstance().getReference().child("users")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child("plans").child(key).removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Write was successful!
                                                list.remove(position);
                                                notifyDataSetChanged();
                                                Toast.makeText(context,
                                                        "Plan successfully deleted",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Write failed
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });
                // Create the AlertDialog object
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
        editBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // start route activity
                Bundle bundle = new Bundle();
                bundle.putSerializable("plan", list.get(position) );
                Intent intent = new Intent(context, RouteActivity.class);
                intent.putExtras(bundle);
                context.startActivity(intent);
                ((Activity)context).finish();
            }
        });

        return view;
    }


}
