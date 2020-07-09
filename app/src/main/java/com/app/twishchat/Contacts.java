package com.app.twishchat;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.app.twishchat.adapater.ContactsAdapater;
import com.app.twishchat.model.ContactsModel;
import com.app.twishchat.util.AlertDialogHelper;
import com.app.twishchat.util.RecyclerItemClickListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

import static com.app.twishchat.util.Helper.FRIEND_ABOUT;
import static com.app.twishchat.util.Helper.FRIEND_NAME;
import static com.app.twishchat.util.Helper.FRIEND_PIC;
import static com.app.twishchat.util.Helper.FRIEND_UID;
import static com.app.twishchat.util.Helper.checkInPhoneList;
import static com.app.twishchat.util.Helper.currentuserID;
import static com.app.twishchat.util.Helper.getUserRef;
import static com.app.twishchat.util.Helper.getUsersAbout;
import static com.app.twishchat.util.Helper.getUsersName;
import static com.app.twishchat.util.Helper.getUsersProfilePic;
import static com.app.twishchat.util.Helper.isInPhoneList;

public class Contacts extends AppCompatActivity implements AlertDialogHelper.AlertDialogListener {

    Toolbar toolbar;
    RecyclerView recyclerView;
    LinearLayoutManager mLayoutManager;
    ContactsAdapater adapater;
    ArrayList<ContactsModel> list = new ArrayList<>();
    FirebaseAuth auth;
    AlertDialogHelper alertDialogHelper;
    ContactsModel model;
    TextView txtContacts;
    boolean isStopped = true;
    boolean isEmpty = true;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        toolbar = findViewById(R.id.toolbar);
        txtContacts = findViewById(R.id.txtContact);
        recyclerView = findViewById(R.id.recyclerView);
        alertDialogHelper = new AlertDialogHelper(this);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(toolbar.getNavigationIcon()).setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        toolbar.setNavigationOnClickListener(view -> {
            isStopped = false;
            Contacts.this.onBackPressed();
        });

        auth = FirebaseAuth.getInstance();
        currentuserID = auth.getCurrentUser().getUid();

        retriveUsers();

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                isStopped = false;
                String uid = list.get(position).getUid();

                Intent i = new Intent(Contacts.this, TwoUsersChatActivity.class);
                i.putExtra("id", uid);
                startActivity(i);

            }

            @Override
            public void onItemLongClick(View view, int position) {
                model = list.get(position);
                if (model.isInPhoneList()) return;
                alertDialogHelper.showAlertDialog("", "Remove " + model.getName() +"?", "REMOVE", "CANCEL", position, false);
            }
        }));
    }

    private void retriveUsers() {
        list.clear();

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
                            if (isInPhoneList){
                                if (!key.equals(currentuserID)){
                                    getData(key, true);
                                    txtContacts.setVisibility(View.GONE);
                                    isEmpty = false;
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

        getUserRef().child(currentuserID).child("Friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        String key = dataSnapshot1.getKey();
                        getData(key, false);
                    }
                } else {
                    if (isEmpty){
                        txtContacts.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getData(String key, boolean isInPhoneList) {

        FRIEND_NAME = getUsersName(key);
        FRIEND_PIC = getUsersProfilePic(key);
        FRIEND_ABOUT = getUsersAbout(key);
        FRIEND_UID = key;

        ContactsModel model = new ContactsModel(FRIEND_NAME, FRIEND_PIC, FRIEND_UID, FRIEND_ABOUT,isInPhoneList);
        list.add(model);
        setRecyclerview();

        /*Rootref.child("UsersAccount").child(key).orderByChild("name").addListenerForSingleValueEvent(new ValueEventListener() {
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
                    if (dataSnapshot.hasChild("about")) {
                        FRIEND_ABOUT = dataSnapshot.child("about").getValue().toString();
                    }



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/
    }

    private void setRecyclerview() {

        adapater = new ContactsAdapater(this, list);
        mLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.scheduleLayoutAnimation();
        recyclerView.setAdapter(adapater);
        recyclerView.setVisibility(View.VISIBLE);
        adapater.notifyDataSetChanged();
    }

    @Override
    public void onPositiveClick(final int from) {

        getUserRef().child(currentuserID).child("Friends").child(model.getUid()).removeValue();
        getUserRef().child(model.getUid()).child("Friends").child(currentuserID).removeValue().addOnSuccessListener(aVoid -> {
            list.remove(from);
            adapater.notifyDataSetChanged();
            Toast.makeText(Contacts.this, "Removed", Toast.LENGTH_SHORT).show();
        });

    }

    @Override
    public void onNegativeClick(int from) {

    }

    @Override
    public void onNeutralClick(int from) {

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
