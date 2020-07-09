package com.app.twishchat;

import android.app.ProgressDialog;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.app.twishchat.adapater.CreateGroupAdapater;
import com.app.twishchat.model.ChatModel;
import com.app.twishchat.model.CreateGroupModel;
import com.app.twishchat.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;
import static com.app.twishchat.util.Helper.FRIEND_NAME;
import static com.app.twishchat.util.Helper.FRIEND_PIC;
import static com.app.twishchat.util.Helper.FRIEND_UID;
import static com.app.twishchat.util.Helper.checkInPhoneList;
import static com.app.twishchat.util.Helper.currentUserName;
import static com.app.twishchat.util.Helper.currentUserProfilePic;
import static com.app.twishchat.util.Helper.currentuserID;
import static com.app.twishchat.util.Helper.getChatRef;
import static com.app.twishchat.util.Helper.getGroupUsersKeyRef;
import static com.app.twishchat.util.Helper.getUserRef;
import static com.app.twishchat.util.Helper.getUsersName;
import static com.app.twishchat.util.Helper.getUsersProfilePic;
import static com.app.twishchat.util.Helper.isInPhoneList;

public class AddParticipants extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    LinearLayoutManager mLayoutManager;
    CreateGroupAdapater adapater;
    ArrayList<CreateGroupModel> list = new ArrayList<>();
    FirebaseAuth auth;
    String name, image, id, createTime, about;
    ProgressDialog progressDialog;
    TextView txtContacts;
    Boolean isChecked = false;
    boolean isStopped = true;
    boolean isEmpty = true;
    UserModel userModel;
    MenuItem itemConfirm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_participants);

        name = getIntent().getStringExtra("name");
        image = getIntent().getStringExtra("profile_url");
        id = getIntent().getStringExtra("id");
        createTime = getIntent().getStringExtra("createTime");
        about = getIntent().getStringExtra("about");

        auth = FirebaseAuth.getInstance();
        currentuserID = auth.getCurrentUser().getUid();
        currentUserName = getUsersName(currentuserID);
        currentUserProfilePic = getUsersProfilePic(currentuserID);

        toolbar = findViewById(R.id.toolbar);
        txtContacts = findViewById(R.id.txtContact);
        recyclerView = findViewById(R.id.recyclerView);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(toolbar.getNavigationIcon()).setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        toolbar.setNavigationOnClickListener(view -> {
            AddParticipants.this.onBackPressed();
            isStopped = false;
        });
    }

    private void retriveUsers() {
        list.clear();
        userModel = new UserModel(currentUserName, currentUserProfilePic, currentuserID);

        getUserRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        String key = dataSnapshot1.getKey();
                        assert key != null;
                        if (dataSnapshot.child(key).hasChild("number")) {
                            String number = dataSnapshot.child(key).child("number").getValue().toString();
                            isInPhoneList = checkInPhoneList(number);
                            if (isInPhoneList) {
                                if (!key.equals(currentuserID)) {
                                    checkGroup(key);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        getUserRef().child(currentuserID).child("Friends").orderByChild("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (final DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        final String key = dataSnapshot1.getKey();
                        checkGroup(key);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkGroup(final String key) {
        getGroupUsersKeyRef().child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(key)) {
                    itemConfirm.setVisible(true);
                    txtContacts.setVisibility(View.GONE);
                    isChecked = true;
                    loadMembers(key);
                } else {
                    txtContacts.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadMembers(String key) {
        FRIEND_NAME = getUsersName(key);
        FRIEND_PIC = getUsersProfilePic(key);
        FRIEND_UID = key;

        CreateGroupModel model = new CreateGroupModel(FRIEND_UID, FRIEND_NAME, FRIEND_PIC);
        list.add(model);
        setRecyclerView();
        /*getUserRef().child(key).orderByChild("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("name")) {
                        FRIEND_NAME = dataSnapshot.child("name").getValue().toString();
                    }
                    if (dataSnapshot.hasChild("profile_pic")) {
                        FRIEND_PIC = dataSnapshot.child("profile_pic").getValue().toString();
                    }
                    if (dataSnapshot.hasChild("uid")) {
                        FRIEND_UID = dataSnapshot.child("uid").getValue().toString();
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/
    }

    private void setRecyclerView() {
        adapater = new CreateGroupAdapater(this, list);
        mLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.scheduleLayoutAnimation();
        recyclerView.setAdapter(adapater);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        itemConfirm = menu.findItem(R.id.confirm);
        itemConfirm.setVisible(false);
        retriveUsers();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.confirm:
                if (isChecked) {
                    progressDialog.show();
                    addParticipants();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addParticipants() {
        final String message_type = "notification";

        final HashMap<String, String> map = new HashMap<>();
        map.put("name", name);
        map.put("type", "group");
        map.put("profile_pic", image);
        map.put("id", id);
        map.put("createTime", createTime);
        map.put("timeStamp", Long.toString(Calendar.getInstance().getTime().getTime()));
        map.put("senderName", currentUserName);
        map.put("about", about);

        for (final CreateGroupModel model : list) {
            if (model.getSelected()) {

                getGroupUsersKeyRef().child(id).child(model.getUid()).setValue("false");
                getGroupUsersKeyRef().child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                String key = dataSnapshot1.getKey();
                                assert key != null;
                                if (key.equals(model.getUid())) {
                                    map.put("displayMessage", "Added you");

                                    String message = currentUserName + " Added You";
                                    final ChatModel chatModel = new ChatModel("", message_type, userModel, message,"", Calendar.getInstance().getTime().getTime() + "", null);
                                    getChatRef().child(key).child(id).push().setValue(chatModel);

                                } else {
                                    map.put("displayMessage", "Added " + model.getName());

                                    if (!key.equals(currentuserID)) {
                                        String message = currentUserName + " Added " + model.getName();
                                        final ChatModel chatModel = new ChatModel("", message_type, userModel, message,"", Calendar.getInstance().getTime().getTime() + "", null);
                                        getChatRef().child(key).child(id).push().setValue(chatModel);

                                    } else {
                                        String message = "You Added " + model.getName();
                                        final ChatModel chatModel = new ChatModel("", message_type, userModel, message,"", Calendar.getInstance().getTime().getTime() + "", null);
                                        getChatRef().child(key).child(id).push().setValue(chatModel);
                                    }

                                }
                                getUserRef().child(key).child("Chats").child(id).setValue(map);

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                progressDialog.dismiss();
                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                progressDialog.dismiss();
                Toast.makeText(this, "Please select minimum 1 contact", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isStopped = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (auth.getCurrentUser() != null) {
            if (isStopped) {
                updateOfflineStatus();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            currentuserID = auth.getCurrentUser().getUid();
            if (isStopped) {
                updateOfflineStatus();
            }
        }
    }

    private void updateOfflineStatus() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("online", "false");
        map.put("timeStamp", Long.toString(Calendar.getInstance().getTime().getTime()));
        getUserRef().child(currentuserID).updateChildren(map);
    }
}
