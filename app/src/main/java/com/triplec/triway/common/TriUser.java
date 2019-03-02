package com.triplec.triway.common;
import java.util.ArrayList;
import java.util.List;

public class TriUser {
    private String name;
    private List<TriPlan> list;

    public TriUser(String name){
        this.name = name;
        list = new ArrayList<>();
    }

    public void addPlan(TriPlan p){
        list.add(p);
    }

    public void removePlan(TriPlan p){
        list.remove(p);
    }
}
