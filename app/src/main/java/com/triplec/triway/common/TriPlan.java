package com.triplec.triway.common;

import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TriPlan {
    private List<TriPlace> list;
    //private TriPlace triPlace;
   // private String name;

    private TriPlan(TriPlanBuilder builder){
        //name = p;
        list = getTopFive(builder.list);
    }
    public List<TriPlace> getPlaceList() {
        return this.list;
    }
    private List<TriPlace> getTopFive(List<TriPlace> allPlaces) {
        List<TriPlace> list = allPlaces;
        //if(list.size() > 5)
            return list.subList(0,5);
       // return list;
    }

    public static class TriPlanBuilder {
        private List<TriPlace> list;
        public TriPlanBuilder() {
            list = new ArrayList<TriPlace>();
        }
        public TriPlanBuilder addPlace(TriPlace p){
            list.add(p);
            return this;
        }
        public TriPlanBuilder removePlace(TriPlace p){
            list.remove(p);
            return this;
        }
        public TriPlanBuilder addPlaceList(List<TriPlace> newList) {
            list=newList;
            return this;
        }
        public TriPlan buildPlan() {
            return new TriPlan(this);
        }
    }
}
