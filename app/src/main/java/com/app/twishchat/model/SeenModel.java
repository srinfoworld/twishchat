package com.app.twishchat.model;

public class SeenModel {
    String uid;
    String id;
    boolean seen;

    public SeenModel(String uid, String id, boolean seen) {
        this.uid = uid;
        this.id = id;

        this.seen = seen;
    }

    public String getUid() {
        return uid;
    }

    public String getId() {
        return id;
    }

    public boolean isSeen() {
        return seen;
    }
}
