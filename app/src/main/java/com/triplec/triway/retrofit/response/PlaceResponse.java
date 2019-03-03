package com.triplec.triway.retrofit.response;
import com.google.gson.annotations.SerializedName;
import com.triplec.triway.common.TriPlace;

import java.util.ArrayList;
import java.util.List;

public class PlaceResponse {
    public String status;
    public int totalResult;
    @SerializedName("results")
    private List<TriPlace> places;
    public List<TriPlace> getPlaces() {
        return this.places;
    }
}
