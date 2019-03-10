package com.triplec.triway.route;

import android.content.Context;

import com.triplec.triway.common.TriPlace;
import com.triplec.triway.common.TriPlan;
import com.triplec.triway.mvp.MvpContract;


public interface RouteContract {


    interface View extends MvpContract.View<Presenter> {
        void showRoutes(TriPlan placePlan);
        void onError(String message);
        void onSavedSuccess(String planName);
        String getMainPlace();
        // Return planId if success
        String savePlans(String plan_name);
        boolean addPlace(TriPlace newPlace);

        TriPlan getPassedPlan();

        Context getContext();
    }

    interface Presenter extends MvpContract.Presenter<View, Model> {
        void showRoutes(TriPlan placePlan);
        // Return planId if success
        String savePlans(String planName);
        void onError(String message);
        void onSavedSuccess(String planName);
        boolean addPlace(TriPlace newPlace);
        void setPlanId(String id);

        Context getContext();
    }

    interface Model extends MvpContract.Model<Presenter> {
        //add fetchData here
        void fetchData(String place);
        // Return planId if success
        String savePlans(String planName);
        void setPlanId(String id);
        boolean addPlace(TriPlace newPlace);
        void upDatePlan(TriPlan newPlan);
    }
}
