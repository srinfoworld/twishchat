package com.app.twishchat.model;

public class CallModel {
    String id;
    String token;
    String status;
    String pick = "";
    String name;
    String profile_pic;
    String uid;
    CallModel callModel;

    public CallModel() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPick() {
        return pick;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public void setProfile_pic(String profile_pic) {
        this.profile_pic = profile_pic;
    }

    public CallModel getCallModel() {
        return callModel;
    }

    public String getUid() {
        return uid;
    }

    public CallModel(String name, String profile_pic, String uid, CallModel callModel) {
        this.name = name;
        this.profile_pic = profile_pic;
        this.uid = uid;
        this.callModel = callModel;
    }
}
