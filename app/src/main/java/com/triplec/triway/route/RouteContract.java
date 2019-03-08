package com.triplec.triway.route;

import com.triplec.triway.common.TriPlan;
import com.triplec.triway.mvp.MvpContract;


public interface RouteContract {


    interface View extends MvpContract.View<Presenter> {
        void showRoutes(TriPlan placePlan);
        void onError();
        void onSavedSuccess();
        String getMainPlace();
    }

    interface Presenter extends MvpContract.Presenter<View, Model> {
        void showRoutes(TriPlan placePlan);
        void savePlans(TriPlan placePlan);
        void onError();
        void onSavedSuccess();
    }

    interface Model extends MvpContract.Model<Presenter> {
        //add fetchData here
        void fetchData(String place);
        void savePlans(TriPlan placePlan);
    }
}
