package com.app.twishchat.util;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.app.twishchat.agvideocall.model.ConstantApp;
import com.app.twishchat.model.ContactsModel;
import com.app.twishchat.model.CountModel;
import com.app.twishchat.model.SeenModel;
import com.app.twishchat.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import static com.app.twishchat.util.Helper.currentuserID;

public class Helper {

    public static final String USER_REFERENCE = "UsersAccount";
    public static final String CHAT_REFERENCE = "Chats";
    public static final String FRIEND_REFERENCE = "Friends";
    public static final String NOTIFICATION_REFERENCE = "Notifications";
    public static final String GROUP_USERS_KEY_REFERENCE = "GroupUsersKey";
    public static final String RECEIVING_REF = "Receiving";
    public static final String CALLING_REF = "Calling";
    public static String FRIEND_UID;
    public static String FRIEND_NAME;
    public static String FRIEND_PIC;
    public static String FRIEND_ABOUT;
    public static String FRIEND_NUMBER;
    public static String FRIEND_ONLINE;
    public static String FRIEND_TIMESTAMP;
    public static String MESSAGE_COUNT;
    public static String RESULT;
    public static ArrayList<SeenModel> seenList = new ArrayList<>();
    public static ArrayList<CountModel> countList = new ArrayList<>();
    public static ArrayList<ContactsModel> phoneContactList = new ArrayList<>();
    public static ArrayList<UserModel> allUsersList = new ArrayList<>();
    public static ArrayList<String> contactList = new ArrayList<>();
    public static boolean isInPhoneList = false;
    public static boolean isCallingStart;
    public static DatabaseReference userRef, chatRef, notificationRef, groupUsersKeyRef;
    public static String currentuserID;
    public static String currentUserName;
    public static String currentUserProfilePic;
    public static String currentUserNumber;
    public static String currentUserAbout;

    public static String getUsersName(String userID) {
        String result = "";
        for (UserModel model : allUsersList) {
            if (model.getId().equals(userID)) {
                result = model.getName();
            }
        }
        return result;
    }

    public static String getUsersProfilePic(String userID) {
        String result = "";
        for (UserModel model : allUsersList) {
            if (model.getId().equals(userID)) {
                result = model.getPhoto_profile();
            }
        }
        return result;
    }

    public static String getUsersNumber(String userID) {
        String result = "";
        for (UserModel model : allUsersList) {
            if (model.getId().equals(userID)) {
                result = model.getNumber();
            }
        }
        return result;
    }

    public static String getUsersAbout(String userID) {
        String result = "";
        for (UserModel model : allUsersList) {
            if (model.getId().equals(userID)) {
                result = model.getAbout();
            }
        }
        return result;
    }

    public static String getUsersOnline(String userID) {
        String result = "";
        for (UserModel model : allUsersList) {
            if (model.getId().equals(userID)) {
                result = model.getOnline();
            }
        }
        return result;
    }

    public static String getUsersTimeStamp(String userID) {
        String result = "";
        for (UserModel model : allUsersList) {
            if (model.getId().equals(userID)) {
                result = model.getTimeStamp();
            }
        }
        return result;
    }

    public static boolean getMessageSeen(String uid, String id) {
        boolean seen = false;
        for (SeenModel model : seenList) {
            if (model.getUid().equals(uid)) {
                if (model.getId().equals(id)) {
                    seen = model.isSeen();
                }
            }
        }
        return seen;
    }

    public static CharSequence converteTimestamp(String result, int i) {

        if (TextUtils.isEmpty(result)) return null;

        String time = null;
        String currentDate = new SimpleDateFormat("d/M/yy", Locale.getDefault()).format(new Date());
        String currentDay = new SimpleDateFormat("d", Locale.getDefault()).format(new Date());
        String currentHour = new SimpleDateFormat("h", Locale.getDefault()).format(new Date());
        String currentMin = new SimpleDateFormat("mm", Locale.getDefault()).format(new Date());

        @SuppressLint("SimpleDateFormat") SimpleDateFormat DateFormatter = new SimpleDateFormat("d/M/yy");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat DayDateFormatter = new SimpleDateFormat("E");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat DayFormatter = new SimpleDateFormat("d");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat HourFormatter = new SimpleDateFormat("h");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat MinFormatter = new SimpleDateFormat("mm");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat HourMinFormatter = new SimpleDateFormat("h:mm aaa");

        String afterFormatDate = DateFormatter.format(new Date(Long.parseLong(result)));
        String afterFormatDay = DayFormatter.format(new Date(Long.parseLong(result)));
        String afterFormatHour = HourFormatter.format(new Date(Long.parseLong(result)));
        String afterFormatMin = MinFormatter.format(new Date(Long.parseLong(result)));
        String afterHourMinFormat = HourMinFormatter.format(new Date(Long.parseLong(result)));
        String afterDayDateFormat = DayDateFormatter.format(new Date(Long.parseLong(result)));

        String yesterDay = String.valueOf(Integer.parseInt(currentDay) - 1);
        String agoTwoDay = String.valueOf(Integer.parseInt(currentDay) - 2);

        if (i == 1) {
            if (currentDate.equals(afterFormatDate)) {
                result = afterHourMinFormat;
            } else if (yesterDay.equals(afterFormatDay)) {
                result = "Yesterday";
            } else {
                result = afterFormatDate;
            }
        } else if (i == 2) {
            if (currentDate.equals(afterFormatDate)) {
                result = afterHourMinFormat;
            } else if (yesterDay.equals(afterFormatDay)) {
                result = "Yesterday, " + afterHourMinFormat;
            } else if (agoTwoDay.equals(afterFormatDay)) {
                result = afterDayDateFormat + ", " + afterHourMinFormat;
            } else {
                result = afterFormatDate + ", " + afterHourMinFormat;
            }
        } else {
            if (currentDate.equals(afterFormatDate)) {
                result = "last seen today at " + afterHourMinFormat;
            } else if (yesterDay.equals(afterFormatDay)) {
                result = "last seen yesterday at " + afterHourMinFormat;
            } else if (agoTwoDay.equals(afterFormatDay)) {
                result = "last seen " + afterDayDateFormat + " " + afterHourMinFormat;
            } else {
                result = "last seen " + afterFormatDate;
            }
        }

        return result;
    }

    public static boolean checkInPhoneList(String number) {
        boolean b = false;
        for (ContactsModel phoneContactModel : phoneContactList) {
            String userPNumber = phoneContactModel.getNumber();
            contactList.add(userPNumber);
        }
        if (contactList.contains(number)) {
            b = true;
        } else if (contactList.contains("+91" + number)) {
            b = true;
        }
        return b;
    }

    public static void removeDP(String currentuserID) {

        getUserRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(currentuserID)) {
                    getUserRef().child(currentuserID).child("profile_pic").setValue("");
                }
               /* if (dataSnapshot.exists()) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        String key = dataSnapshot1.getKey();
                        assert key != null;
                        if (dataSnapshot.child(key).child(CHAT_REFERENCE).hasChild(currentuserID)) {
                            getUserRef().child(key).child(CHAT_REFERENCE).child(currentuserID).child("profile_pic").setValue("");
                        }
                    }
                }*/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void changeDP(String currentuserID, String value) {

        getUserRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(currentuserID)) {
                    getUserRef().child(currentuserID).child("profile_pic").setValue(value);
                }
               /* if (dataSnapshot.exists()) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        String key = dataSnapshot1.getKey();
                        assert key != null;
                        if (dataSnapshot.child(key).child(CHAT_REFERENCE).hasChild(currentuserID)) {
                            getUserRef().child(key).child(CHAT_REFERENCE).child(currentuserID).child("profile_pic").setValue(value);
                        }
                    }
                }*/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public static DatabaseReference getUserRef() {
        if (userRef == null) {
            userRef = FirebaseDatabase.getInstance().getReference(USER_REFERENCE);
            userRef.keepSynced(true);
        }
        return userRef;
    }

    public static DatabaseReference getChatRef() {
        if (chatRef == null) {
            chatRef = FirebaseDatabase.getInstance().getReference(CHAT_REFERENCE);
            chatRef.keepSynced(true);
        }
        return chatRef;
    }

    public static DatabaseReference getNotificationRef() {
        if (notificationRef == null) {
            notificationRef = FirebaseDatabase.getInstance().getReference(NOTIFICATION_REFERENCE);
            notificationRef.keepSynced(true);
        }
        return notificationRef;
    }

    public static DatabaseReference getGroupUsersKeyRef() {
        if (groupUsersKeyRef == null) {
            groupUsersKeyRef = FirebaseDatabase.getInstance().getReference(GROUP_USERS_KEY_REFERENCE);
            groupUsersKeyRef.keepSynced(true);
        }
        return groupUsersKeyRef;
    }

    public static String getOnline(String result) {
        getUserRef().child(result).child("online").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    RESULT = dataSnapshot.getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return RESULT;
    }


    public static String getMessageCount(String key, String chatID) {
        String result = "";
        for (CountModel model : countList) {
            if (model.getKey().equals(key)) {
                if (model.getChatID().equals(chatID))
                    result = model.getCount();
            }
        }
        return result;
    }

    public static void startCalling(String currentuserID, String uid, boolean b) {

        getUserRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.child(uid).hasChild(RECEIVING_REF) && !snapshot.child(uid).hasChild(CALLING_REF)) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("id", ConstantApp.ACTION_KEY_ROOM_ID);
                    map.put("token", ConstantApp.ACCESS_TOKEN);
                    getUserRef().child(uid).child(RECEIVING_REF).child(currentuserID).updateChildren(map).addOnCompleteListener(task -> {
                        if (b) {
                            update();
                        } else {
                            if (!snapshot.child(currentuserID).hasChild(RECEIVING_REF) && !snapshot.child(currentuserID).hasChild(CALLING_REF)) {
                                update();
                            }
                        }
                    });
                } else {
                    isCallingStart = false;
                }
            }

            private void update() {
                HashMap<String, Object> map1 = new HashMap<>();
                map1.put("id", ConstantApp.ACTION_KEY_ROOM_ID);
                map1.put("token", ConstantApp.ACCESS_TOKEN);
                map1.put("status", "Calling");
                getUserRef().child(currentuserID).child(CALLING_REF).child(uid).updateChildren(map1);
                isCallingStart = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void endVideoCall(String currentuserID) {
        ConstantApp.OUTGOING = false;
        ConstantApp.CALLING_START = false;
        getUserRef().child(currentuserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(RECEIVING_REF)) {
                    getUserRef().child(currentuserID).child(RECEIVING_REF).removeValue();
                } else if (snapshot.hasChild(CALLING_REF)) {
                    getUserRef().child(currentuserID).child(CALLING_REF).removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void endVideoCall(String currentuserID, String uid, boolean isLeft) {

        getUserRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (isLeft){
                    ConstantApp.OUTGOING = false;
                    ConstantApp.CALLING_START = false;
                }

                if (snapshot.child(currentuserID).child(CALLING_REF).hasChild(uid)) {

                    getUserRef().child(currentuserID).child(CALLING_REF).child(uid).removeValue();

                } else if (snapshot.child(currentuserID).child(RECEIVING_REF).hasChild(uid)) {

                    getUserRef().child(currentuserID).child(RECEIVING_REF).child(uid).removeValue();

                }
                if (snapshot.child(uid).child(CALLING_REF).hasChild(currentuserID)) {

                    getUserRef().child(uid).child(CALLING_REF).child(currentuserID).removeValue();

                }else if (snapshot.child(uid).child(RECEIVING_REF).hasChild(currentuserID)) {

                    getUserRef().child(uid).child(RECEIVING_REF).child(currentuserID).removeValue();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void Ringing(String id) {
        String cid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getUserRef().child(id).child(CALLING_REF).child(cid).child("status").setValue("Ringing");
    }

    public static void Pickup(String id) {
        String cid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getUserRef().child(id).child(CALLING_REF).child(cid).child("pick").setValue("true");
        getUserRef().child(id).child(CALLING_REF).child(cid).child("status").setValue("Ongoing");
    }

    public static void Denied(String id) {
        String cid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getUserRef().child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(CALLING_REF)) {
                    getUserRef().child(id).child(CALLING_REF).child(cid).child("status").setValue("Call Denied");
                    getUserRef().child(id).child(CALLING_REF).child(cid).child("pick").setValue("false");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}
