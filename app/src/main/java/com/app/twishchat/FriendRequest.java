package com.app.twishchat;


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
import com.app.twishchat.adapater.FriendRequestAdapater;
import com.app.twishchat.model.FriendModel;
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
import static com.app.twishchat.util.Helper.currentUserAbout;
import static com.app.twishchat.util.Helper.currentUserName;
import static com.app.twishchat.util.Helper.currentUserProfilePic;
import static com.app.twishchat.util.Helper.currentuserID;
import static com.app.twishchat.util.Helper.getUserRef;
import static com.app.twishchat.util.Helper.getUsersAbout;
import static com.app.twishchat.util.Helper.getUsersName;
import static com.app.twishchat.util.Helper.getUsersProfilePic;

public class FriendRequest extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    LinearLayoutManager mLayoutManager;
    FriendRequestAdapater adapater;
    ArrayList<FriendModel> list = new ArrayList<>();
    FirebaseAuth auth;
    TextView txtFriends;
    boolean isStopped =true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);

        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerView);
        txtFriends = findViewById(R.id.txtFriend);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(toolbar.getNavigationIcon()).setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        toolbar.setNavigationOnClickListener(view -> {
            isStopped = false;
            FriendRequest.this.onBackPressed();
        });

        auth = FirebaseAuth.getInstance();
        currentuserID = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        currentUserName = getUsersName(currentuserID);
        currentUserProfilePic = getUsersProfilePic(currentuserID);
        currentUserAbout = getUsersAbout(currentuserID);

        retriveUsers();

    }

    private void retriveUsers() {

        getUserRef().child(currentuserID).child("FriendRequest").orderByChild("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    list.clear();
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        String key = dataSnapshot1.getKey();
                        getData(key);
                    }

                }else{
                    list.clear();
                    txtFriends.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getData(String key) {
        FRIEND_NAME = getUsersName(key);
        FRIEND_PIC = getUsersProfilePic(key);
        FRIEND_ABOUT = getUsersAbout(key);
        FRIEND_UID = key;

        FriendModel model = new FriendModel(FRIEND_NAME,FRIEND_PIC,FRIEND_UID,FRIEND_ABOUT);
        list.add(model);
        setRecyclerview();

        /*getUserRef().child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.hasChild("name")){
                        FRIEND_NAME = dataSnapshot.child("name").getValue().toString();
                    }
                    if (dataSnapshot.hasChild("profile_pic")){
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
        adapater = new FriendRequestAdapater(this, list);
        mLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.scheduleLayoutAnimation();
        recyclerView.setAdapter(adapater);

        adapater.setOnItemClickListener(new FriendRequestAdapater.OnItemClickListener() {
            @Override
            public void onaddClick(final int position) {
                final FriendModel currentItem = list.get(position);

                getUserRef().child(currentuserID).child("Friends").child(currentItem.getUid()).setValue(currentItem.getUid());
                getUserRef().child(currentItem.getUid()).child("Friends").child(currentuserID).setValue(currentuserID)
                        .addOnSuccessListener(aVoid -> {
                            getUserRef().child(currentuserID).child("FriendRequest").child(currentItem.getUid()).removeValue();
                            list.remove(position);
                            adapater.notifyDataSetChanged();
                            Toast.makeText(FriendRequest.this, "You are now friend with " + currentItem.getName(), Toast.LENGTH_SHORT).show();
                        });
            }

            @Override
            public void oncancelClick(int position) {
                final FriendModel currentItem = list.get(position);
                getUserRef().child(currentuserID).child("FriendRequest").child(currentItem.getUid()).removeValue();
                list.remove(position);
                adapater.notifyDataSetChanged();
            }
        });
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
        HashMap<String,Object> map = new HashMap<>();
        map.put("online", "false");
        map.put("timeStamp", Long.toString(Calendar.getInstance().getTime().getTime()));
        getUserRef().child(currentuserID).updateChildren(map);
    }
}
