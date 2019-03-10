package com.triplec.triway.route;

import android.content.Context;

import com.triplec.triway.common.TriPlace;
import com.triplec.triway.common.TriPlan;

class RoutePresenter implements RouteContract.Presenter {
    private RouteModel model;
    private RouteContract.View view;

    public RoutePresenter() {
        this.model = new RouteModel();
        this.model.setPresenter(this);
    }

    @Override
    public void showRoutes(TriPlan placePlan) {
        if (this.view != null) {
            view.showRoutes(placePlan);
        }
    }

    @Override
    public String savePlans(String planName) {
        return this.model.savePlans(planName);
    }

    @Override
    public void onError(String message) {
        if (this.view != null) {
            view.onError(message);
        }
    }

    @Override
    public void onSavedSuccess(String planName) {
        if (view != null) {
            view.onSavedSuccess(planName);
        }
    }

    @Override
    public boolean addPlace(TriPlace newPlace) {
        return this.model.addPlace(newPlace);
    }

    @Override
    public void setPlanId(String id) {
        this.model.setPlanId(id);
    }

    @Override
    public Context getContext() {
        return this.view.getContext();
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onViewAttached(RouteContract.View view) {
        this.view = view;
        this.model.setGeocoder(view.getContext());
        if (this.view.getMainPlace() == null || this.view.getMainPlace().length() == 0) {
            this.model.upDatePlan(view.getPassedPlan());
            this.view.showRoutes(view.getPassedPlan());
        }
        else {
            this.model.fetchData(this.view.getMainPlace());
        }
    }

    @Override
    public void onViewDetached() {
        this.view = null;
    }
}
