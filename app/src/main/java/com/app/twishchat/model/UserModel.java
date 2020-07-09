package com.app.twishchat.model;

import com.google.firebase.database.Exclude;

/**
 * Created by Alessandro Barreto on 22/06/2016.
 */
public class UserModel {

    private String id;
    private String name;
    private String profile_pic;
    private String number;
    private String about;
    private String online;
    private String timeStamp;

    public UserModel() {

    }

    public UserModel(String name, String profile_pic, String id) {
        this.name = name;
        this.profile_pic = profile_pic;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto_profile() {
        return profile_pic;
    }

    public void setPhoto_profile(String profile_pic) {
        this.profile_pic = profile_pic;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public String getAbout() {
        return about;
    }

    public String getOnline() {
        return online;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public UserModel(String id, String name, String profile_pic, String number, String about, String online, String timeStamp) {
        this.id = id;
        this.name = name;
        this.profile_pic = profile_pic;
        this.number = number;
        this.about = about;
        this.online = online;
        this.timeStamp = timeStamp;
    }
}
