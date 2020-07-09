package com.app.twishchat.model;

public class ContactsModel {
    String name;
    String profile_pic;
    String uid="";
    String about ="";
    String number;
    String online;
    String timeStamp;
    String state;
    boolean isInPhoneList;
    boolean isCalling;
    boolean isRunning;
    private Boolean selected = false;

    public ContactsModel() {
    }

    public String getNumber() {
        return number;
    }

    public ContactsModel( String name,String number) {
        this.name = name;
        this.number = number;
    }

    public ContactsModel(String number) {
        this.number = number;
    }

    public ContactsModel(String name, String profile_pic, String uid, String about, boolean isInPhoneList) {
        this.name = name;
        this.profile_pic = profile_pic;
        this.uid = uid;
        this.about = about;
        this.isInPhoneList = isInPhoneList;
    }

    public ContactsModel(String name, String profile_pic, String uid, String about, String online, String timeStamp) {
        this.name = name;
        this.profile_pic = profile_pic;
        this.uid = uid;
        this.about = about;
        this.online = online;
        this.timeStamp = timeStamp;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public String getAbout() {
        return about;
    }

    public boolean isInPhoneList() {
        return isInPhoneList;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public String getOnline() {
        return online;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public boolean isCalling() {
        return isCalling;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setCalling(boolean calling) {
        isCalling = calling;
    }

    public String getState() {
        return state;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }
}
