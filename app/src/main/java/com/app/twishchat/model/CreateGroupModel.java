package com.app.twishchat.model;

public class CreateGroupModel {
    String uid;
    String email;
    String name;
    String profile_pic;

    public CreateGroupModel(String uid, String name, String profile_pic) {
        this.uid = uid;
        this.name = name;
        this.profile_pic = profile_pic;
    }

    private Boolean selected = false;

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }
}
