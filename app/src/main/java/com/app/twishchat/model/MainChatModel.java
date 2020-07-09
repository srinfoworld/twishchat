package com.app.twishchat.model;

public class MainChatModel {
    private String name;
    private String profile_pic;
    private String id;
    private MainModel mainModel;

    public MainChatModel(String name, String profile_pic, String id, MainModel mainModel) {
        this.name = name;
        this.profile_pic = profile_pic;
        this.id = id;
        this.mainModel = mainModel;
    }

    public String getName() {
        return name;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public String getId() {
        return id;
    }

    public MainModel getMainModel() {
        return mainModel;
    }
}
