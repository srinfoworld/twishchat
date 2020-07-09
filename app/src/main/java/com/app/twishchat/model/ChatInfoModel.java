package com.app.twishchat.model;

public class ChatInfoModel {
    String name;
    String profile_pic;
    String uid;
    String about;
    boolean isBlocked = false;
    boolean isAdmin = false;

    public ChatInfoModel(String name, String profile_pic, String uid,String about, boolean isBlocked,boolean isAdmin) {
        this.name = name;
        this.profile_pic = profile_pic;
        this.uid = uid;
        this.about = about;
        this.isBlocked = isBlocked;
        this.isAdmin = isAdmin;
    }

    public String getName() {
        return name;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public String getUid() {
        return uid;
    }

    public String getAbout() {
        return about;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public boolean isAdmin() {
        return isAdmin;
    }
}
