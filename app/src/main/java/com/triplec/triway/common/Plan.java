package com.triplec.triway.common;
import java.util.ArrayList;

import android.support.v4.app.FragmentActivity;

public class Plan extends FragmentActivity {
    private ArrayList<TriPlace> list;
    //private TriPlace triPlace;
   // private String name;

    public Plan(){
        //name = p;
        list = new ArrayList<TriPlace>();
    }

    public void addPlace(TriPlace p){
        list.add(p);
    }

    public void removePlace(TriPlace p){
        list.remove(p);
    }

}
