package com.app.twishchat.model;

public class FriendModel {
    String name;
    String profile_pic;
    String uid;
    String about;

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

    public FriendModel(String name, String profile_pic, String uid, String about) {
        this.name = name;
        this.profile_pic = profile_pic;
        this.uid = uid;
        this.about = about;
    }
}
