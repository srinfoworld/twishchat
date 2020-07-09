package com.app.twishchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.app.twishchat.model.CountModel;
import com.app.twishchat.model.SeenModel;
import com.app.twishchat.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import static com.app.twishchat.util.Helper.allUsersList;
import static com.app.twishchat.util.Helper.contactList;
import static com.app.twishchat.util.Helper.countList;
import static com.app.twishchat.util.Helper.getChatRef;
import static com.app.twishchat.util.Helper.getUserRef;
import static com.app.twishchat.util.Helper.seenList;

public class SplashActivity extends AppCompatActivity {

    FirebaseAuth auth;
    String name = "",
            id = "",
            image = "",
            number = "",
            about = "",
            online = "",
            timeStamp = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        auth = FirebaseAuth.getInstance();
        collectUsersData();

        int SPLASH_SCREEN_TIME_OUT = 3000;

        new Handler().postDelayed(() -> {

            if (auth.getCurrentUser() == null){
                Intent i=new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }else {
                String currentUserID = auth.getCurrentUser().getUid();
                getUserRef().child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild("name") && snapshot.hasChild("number")){
                            Intent i=new Intent(SplashActivity.this, MainActivity.class);
                            startActivity(i);
                            finish();
                        }else {
                            Intent i=new Intent(SplashActivity.this, AfterLoginProfileActivity.class);
                            startActivity(i);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

        }, SPLASH_SCREEN_TIME_OUT);
    }

    public void collectUsersData() {
        contactList.clear();
        allUsersList.clear();

        getUserRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        String key = dataSnapshot1.getKey();

                        assert key != null;
                        if (dataSnapshot.child(key).hasChild("Chats")) {

                            for (DataSnapshot dataSnapshot2 : dataSnapshot.child(key).child("Chats").getChildren()) {
                                String chatsKey = dataSnapshot2.getKey();

                                assert chatsKey != null;
                                if (dataSnapshot.child(key).child("Chats").child(chatsKey).hasChild("messageCount")) {

                                    String count = dataSnapshot.child(key).child("Chats").child(chatsKey).child("messageCount").getValue().toString();

                                    CountModel model = new CountModel(key, count, chatsKey);
                                    countList.add(model);
                                }

                            }
                        }
                    }
                }

                if (dataSnapshot.exists()) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        String key = dataSnapshot1.getKey();
                        saveData(key);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        getChatRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        String key = dataSnapshot.getKey();
                        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                            String subKey = dataSnapshot1.getKey();
                            for (DataSnapshot dataSnapshot2 : dataSnapshot1.getChildren()){
                                if (dataSnapshot2.hasChild("seen")){
                                    boolean seen = Boolean.parseBoolean(dataSnapshot2.child("seen").getValue().toString());
                                    SeenModel model = new SeenModel(key,subKey,seen);
                                    seenList.add(model);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void saveData(String key) {

        getUserRef().child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("name")) {
                        name = dataSnapshot.child("name").getValue().toString();
                    }

                    if (dataSnapshot.hasChild("uid")) {
                        id = dataSnapshot.child("uid").getValue().toString();
                    }

                    if (dataSnapshot.hasChild("profile_pic")) {
                        image = dataSnapshot.child("profile_pic").getValue().toString();
                    }

                    if (dataSnapshot.hasChild("number")) {
                        number = dataSnapshot.child("number").getValue().toString();
                    }

                    if (dataSnapshot.hasChild("about")) {
                        about = dataSnapshot.child("about").getValue().toString();
                    }

                    if (dataSnapshot.hasChild("online")) {
                        online = dataSnapshot.child("online").getValue().toString();
                    }

                    if (dataSnapshot.hasChild("timeStamp")) {
                        timeStamp = dataSnapshot.child("timeStamp").getValue().toString();
                    }

                    UserModel model = new UserModel(id, name, image, number,about,online,timeStamp);
                    allUsersList.add(model);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}