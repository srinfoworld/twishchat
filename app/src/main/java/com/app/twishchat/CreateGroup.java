package com.app.twishchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import com.app.twishchat.util.Util;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.app.twishchat.util.Helper.FRIEND_ABOUT;
import static com.app.twishchat.util.Helper.FRIEND_NAME;
import static com.app.twishchat.util.Helper.FRIEND_PIC;
import static com.app.twishchat.util.Helper.FRIEND_UID;
import static com.app.twishchat.util.Helper.checkInPhoneList;
import static com.app.twishchat.util.Helper.isInPhoneList;

public class CreateGroup extends AppCompatActivity {

    private static final int IMAGE_GALLERY_REQUEST = 1;

    Toolbar toolbar;
    RecyclerView recyclerView;
    LinearLayoutManager mLayoutManager;
    CreateGroupAdapater adapater;
    ArrayList<CreateGroupModel> list = new ArrayList<>();
    DatabaseReference Rootref;
    FirebaseAuth auth;
    Button create_btn;
    EditText group_name;
    CircleImageView group_icon;
    Boolean validGroup = false;
    ProgressDialog progressDialog;
    ProgressBar progressBar;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    String currentuserID= "";
    String gName = "", profileUrl = "", currentuserName = "", currentuserProfilePic = "";
    TextView txtContacts;
    UserModel userModel;
    boolean isStopped =true;
    boolean isEmpty = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        toolbar = findViewById(R.id.toolbar);
        txtContacts = findViewById(R.id.txtContact);
        create_btn = findViewById(R.id.create_btn);
        recyclerView = findViewById(R.id.recyclerView);
        group_name = findViewById(R.id.group_name);
        group_icon = findViewById(R.id.group_icon);
        progressBar = findViewById(R.id.progressBar);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(toolbar.getNavigationIcon()).setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isStopped = false;
                CreateGroup.this.onBackPressed();
            }
        });

        auth = FirebaseAuth.getInstance();
        Rootref = FirebaseDatabase.getInstance().getReference();
        currentuserID = auth.getCurrentUser().getUid();

        retriveUsers();

        create_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (group_name.getText().length() == 0) {
                    group_name.setError("Enter group name");
                    return;
                }
                progressDialog.show();
                createGroup();
            }
        });

        group_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GalleryIntent();
            }
        });
    }

    private void GalleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture_title)), IMAGE_GALLERY_REQUEST);
    }

    private void createGroup() {
        String key = Rootref.push().getKey();
        gName = group_name.getText().toString();
        String MembersMessage = currentuserName + " created group " + gName;
        String CreatorMessage = "You created group " + gName;
        userModel = new UserModel(currentuserName, currentuserProfilePic, currentuserID);

        HashMap<String, String> map = new HashMap<>();
        map.put("name", gName);
        map.put("type", "group");
        map.put("profile_pic", profileUrl);
        map.put("id", key);
        map.put("createTime", Long.toString(Calendar.getInstance().getTime().getTime()));
        map.put("timeStamp", Long.toString(Calendar.getInstance().getTime().getTime()));
        map.put("displayMessage", "Create a group");
        map.put("senderName", currentuserName);
        map.put("about", "");

        HashMap<String, String> map1 = new HashMap<>();
        map1.put(currentuserID, "true");


        //CREATE FOR SELECTED MEMBERS
        for (CreateGroupModel model : list) {
            if (model.getSelected()) {
                validGroup = true;

                //SET GROUP DETAILS FOR CREATOR
                Rootref.child("UsersAccount").child(currentuserID).child("Chats").child(key).setValue(map);

                //SEND CREATE MESSAGE TO CREATOR
                final ChatModel myChatModel = new ChatModel(key, "notification", userModel,
                        CreatorMessage,"", Calendar.getInstance().getTime().getTime() + "", null);
                Rootref.child("Chats").child(currentuserID).child(key).push().setValue(myChatModel);

                //SET MEMBERS UID
                map1.put(model.getUid(), "false");
                Rootref.child("GroupUsersKey").child(key).setValue(map1);

                //SEND CREATE MESSAGE TO MEMBERS
                final ChatModel membersChatModel = new ChatModel(key, "notification", userModel,
                        MembersMessage,"", Calendar.getInstance().getTime().getTime() + "", null);
                Rootref.child("Chats").child(model.getUid()).child(key).push().setValue(membersChatModel);

                //SET GROUP DETAILS FOR MEMBERS
                Rootref.child("UsersAccount").child(model.getUid()).child("Chats").child(key).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            finish();
                        }
                    }
                });

            } else {
                validGroup = false;
            }
        }
        if (!validGroup) {
            progressDialog.dismiss();
            Toast.makeText(this, "Please select minimum 1 contact", Toast.LENGTH_SHORT).show();
        }

    }

    private void retriveUsers() {
        Rootref.child("UsersAccount").addListenerForSingleValueEvent(new ValueEventListener() {
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
                                    getData(key);
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

        Rootref.child("UsersAccount").orderByChild("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(currentuserID).child("Friends").exists()) {

                    if (dataSnapshot.child(currentuserID).hasChild("name")){
                        currentuserName = dataSnapshot.child(currentuserID).child("name").getValue().toString();
                    }
                    if (dataSnapshot.child(currentuserID).hasChild("profile_pic")){
                        currentuserProfilePic = dataSnapshot.child(currentuserID).child("profile_pic").getValue().toString();
                    }
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.child(currentuserID).child("Friends").getChildren()) {
                        String key = dataSnapshot1.getKey();
                        getData(key);
                    }
                    setRecyclerview();
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

    private void getData(String key) {

        Rootref.child("UsersAccount").child(key).orderByChild("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
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

                    CreateGroupModel model = new CreateGroupModel(FRIEND_UID, FRIEND_NAME, FRIEND_PIC);
                    list.add(model);
                    setRecyclerview();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setRecyclerview() {
        adapater = new CreateGroupAdapater(this, list);
        mLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.scheduleLayoutAnimation();
        recyclerView.setAdapter(adapater);
        recyclerView.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        StorageReference storageRef = storage.getReferenceFromUrl(Util.URL_STORAGE_REFERENCE).child(Util.FOLDER_STORAGE_IMG);

        if (requestCode == IMAGE_GALLERY_REQUEST) {
            if (resultCode == RESULT_OK) {
                progressDialog.show();
                progressBar.setVisibility(View.VISIBLE);
                group_icon.setVisibility(View.GONE);
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    sendFileFirebase(storageRef, selectedImageUri);
                }
            }
        }

    }

    private void sendFileFirebase(StorageReference storageReference, final Uri file) {
        if (storageReference != null) {
            final String name = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();
            final StorageReference imageGalleryRef = storageReference.child(name + "_gallery");
            final UploadTask uploadTask = imageGalleryRef.putFile(file);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //onFailed
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    imageGalleryRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            profileUrl = uri.toString();
                            Glide.with(CreateGroup.this).load(uri).into(group_icon);
                            progressBar.setVisibility(View.GONE);
                            progressDialog.dismiss();
                            group_icon.setVisibility(View.VISIBLE);
                        }
                    });
                }
            });
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
        HashMap<String,Object> map = new HashMap<>();
        map.put("online", "false");
        map.put("timeStamp", Long.toString(Calendar.getInstance().getTime().getTime()));
        Rootref.child("UsersAccount").child(currentuserID).updateChildren(map);
    }
}

