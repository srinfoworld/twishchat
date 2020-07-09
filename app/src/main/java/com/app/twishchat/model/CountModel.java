package com.app.twishchat.model;

public class CountModel {
    String key;
    String count;
    String chatID;

    public CountModel() {
    }

    public CountModel(String key, String count, String chatID) {
        this.key = key;
        this.count = count;
        this.chatID = chatID;
    }

    public String getKey() {
        return key;
    }

    public String getCount() {
        return count;
    }

    public String getChatID() {
        return chatID;
    }
}
