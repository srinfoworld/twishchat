package com.app.twishchat.model;

public class FindFriendModel {
    private Boolean requestSent = false;
    private MainModel mainModel;

    public FindFriendModel(Boolean requestSent, MainModel mainModel) {
        this.requestSent = requestSent;
        this.mainModel = mainModel;
    }

    public Boolean getRequestSent() {
        return requestSent;
    }

    public MainModel getMainModel() {
        return mainModel;
    }

    public void changeButton(boolean change) {
        requestSent = change;
    }

}
