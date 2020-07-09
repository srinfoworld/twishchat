package com.app.twishchat.model;


/**
 * Created by Alessandro Barreto on 17/06/2016.
 */
public class ChatModel {

    private String id;
    private UserModel userModel;
    private String message;
    private String timeStamp;
    private FileModel file;
    private MapModel mapModel;
    private ContactsModel contactsModel;
    private MainModel mainModel;
    private String message_type;
    private String seen;

    public ChatModel() {
    }

    public ChatModel(String id, String message_type, UserModel userModel, String message,String seen, String timeStamp, FileModel file) {
        this.id = id;
        this.message_type = message_type;
        this.userModel = userModel;
        this.message = message;
        this.seen = seen;
        this.timeStamp = timeStamp;
        this.file = file;
    }

    public ChatModel(String id,String message_type, UserModel userModel,String seen, String timeStamp, ContactsModel contactsModel) {
        this.id = id;
        this.message_type = message_type;
        this.seen = seen;
        this.userModel = userModel;
        this.timeStamp = timeStamp;
        this.contactsModel = contactsModel;
    }

    public ChatModel(String id,String message_type,UserModel userModel,String seen, String timeStamp, MapModel mapModel) {
        this.id = id;
        this.message_type = message_type;
        this.seen = seen;
        this.userModel = userModel;
        this.timeStamp = timeStamp;
        this.mapModel = mapModel;
    }



    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserModel getUserModel() {
        return userModel;
    }

    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public FileModel getFile() {
        return file;
    }

    public void setFile(FileModel file) {
        this.file = file;
    }

    public MapModel getMapModel() {
        return mapModel;
    }

    public void setMapModel(MapModel mapModel) {
        this.mapModel = mapModel;
    }

    public String getMessage_type() {
        return message_type;
    }

    public ContactsModel getContactsModel() {
        return contactsModel;
    }

    @Override
    public String toString() {
        return "ChatModel{" +
                "mapModel=" + mapModel +
                ", file=" + file +
                ", timeStamp='" + timeStamp + '\'' +
                ", message='" + message + '\'' +
                ", userModel=" + userModel +
                '}';
    }
}
