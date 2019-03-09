package com.triplec.triway.route;

import android.content.Context;

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
    public void savePlans(TriPlan placePlan) {

    }

    @Override
    public void onError() {
        if (this.view != null) {
            view.onError();
        }
    }

    @Override
    public void onSavedSuccess() {
        if (view != null) {
            view.onSavedSuccess();
        }
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
        this.model.fetchData(this.view.getMainPlace());
    }

    @Override
    public void onViewDetached() {
        this.view = null;
    }
}
