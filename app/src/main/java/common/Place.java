package common;
import com.google.android.gms.maps.model.LatLng;


import java.util.*;
import java.lang.*;

public class Place {
    private com.google.android.gms.location.places.Place place;

    public Place(String id) {
        // Specify the fields to return (all fields are returned).
        List<com.google.android.gms.location.places.Place.Field> placeFields =
                +Arrays.asList(com.google.android.gms.location.places.Place.Field.ID,
                        +com.google.android.gms.location.places.Place.Field.NAME);

        FetchPlaceRequest request = FetchPlaceRequest.builder(id, placeFields).build();

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            place = response.getPlace();
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                int statusCode = apiException.getStatusCode();
                // Handle error with given status code.
                Log.e(TAG, "Place not found: " + exception.getMessage());
            }
        });
    }

    public CharSequence getAddress(){
        return place.getAddress();
    }

    public double getRating() {
        return place.getRating();
    }

    public LatLng getLatlng() {
        return place.getLatLng();
    }


}
