package common;
import java.util.ArrayList;


public class Plan {
    private ArrayList<Place> list;

    public Plan(){
        list = new ArrayList<Place>();
    }

    public void addPlace(Place p){
        list.add(p);
    }

    public void removePlace(Place p){
        list.remove(p);
    }
}
