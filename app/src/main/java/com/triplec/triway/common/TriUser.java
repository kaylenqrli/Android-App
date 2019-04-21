package com.triplec.triway.common;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.List;
@IgnoreExtraProperties
public class TriUser {
    private String username;
    private List<TriPlan> list;
    private String email;
    public TriUser() {

    }
    public TriUser(String name, String email){
        this.username = name;
        this.email = email;
    }
    public void addPlan(TriPlan p){
        list.add(p);
    }

    public void removePlan(TriPlan p){
        list.remove(p);
    }
}
