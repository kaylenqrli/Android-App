package common;
import java.util.ArrayList;

public class User {
    private String name;
    private ArrayList<Plan> list;

    public User(String name){
        this.name = name;
        list = new ArrayList<Plan>();
    }

    public void addPlan(Plan p){
        list.add(p);
    }

    public void removePlan(Plan p){
        list.remove(p);
    }
}
