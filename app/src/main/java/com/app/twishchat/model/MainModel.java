package com.app.twishchat.model;

public class MainModel {
    String id="";
    String key = "";
    String type="";
    String name="";
    String profile_pic="";
    String timeStamp="";
    String uid="";
    String email ="";
    String displayMessage ="";
    String senderName ="";
    String attachment ="";
    String about ="";
    String createTime ="";
    String messageCount ="";
    String blocked ="";
    String msgSeen ="";
    String device_token ="";
    String number ="";
    String online ="";
    MainModel mainModel;

    public MainModel(String key, String name, String profile, MainModel data) {
        this.key = key;
        this.name = name;
        this.profile_pic = profile;
        this.mainModel = data;
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public String getKey() {
        return key;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getDisplayMessage() {
        return displayMessage;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getAttachment() {
        return attachment;
    }

    public String getAbout() {
        return about;
    }

    public String getCreateTime() {
        return createTime;
    }

    public String getMessageCount() {
        return messageCount;
    }

    public String getBlocked() {
        return blocked;
    }

    public String getMsgSeen() {
        return msgSeen;
    }

    public String getDevice_token() {
        return device_token;
    }

    public String getNumber() {
        return number;
    }

    public MainModel getMainModel() {
        return mainModel;
    }

    public String getOnline() {
        return online;
    }

    public MainModel() {
    }

}
