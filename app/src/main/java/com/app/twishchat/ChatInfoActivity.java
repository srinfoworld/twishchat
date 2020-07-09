package com.app.twishchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.twishchat.adapater.ChatInfoAdapater;
import com.app.twishchat.model.ChatInfoModel;
import com.app.twishchat.model.ChatModel;
import com.app.twishchat.model.UserModel;
import com.app.twishchat.util.AlertDialogHelper;
import com.app.twishchat.util.Helper;
import com.app.twishchat.util.RecyclerItemClickListener;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.app.twishchat.util.Helper.FRIEND_ABOUT;
import static com.app.twishchat.util.Helper.FRIEND_NAME;
import static com.app.twishchat.util.Helper.FRIEND_PIC;
import static com.app.twishchat.util.Helper.FRIEND_UID;
import static com.app.twishchat.util.Helper.currentUserName;
import static com.app.twishchat.util.Helper.currentUserProfilePic;
import static com.app.twishchat.util.Helper.currentuserID;
import static com.app.twishchat.util.Helper.getChatRef;
import static com.app.twishchat.util.Helper.getGroupUsersKeyRef;
import static com.app.twishchat.util.Helper.getUserRef;
import static com.app.twishchat.util.Helper.getUsersAbout;
import static com.app.twishchat.util.Helper.getUsersName;
import static com.app.twishchat.util.Helper.getUsersProfilePic;

public class ChatInfoActivity extends AppCompatActivity implements AlertDialogHelper.AlertDialogListener, TextWatcher {

    private static final int IMAGE_GALLERY_REQUEST = 1;

    CircleImageView imageView, changeImage;
    TextView participants;
    RecyclerView recyclerView;
    LinearLayoutManager mLayoutManager;
    ChatInfoAdapater adapater;
    ArrayList<ChatInfoModel> list = new ArrayList<>();
    ArrayList<String> usersList = new ArrayList<>();
    String name = "", image = "", id = "", createTime = "",  about = "";
    ChatInfoModel model;
    FirebaseAuth auth;
    Toolbar toolbar;
    ProgressDialog progressDialog;
    ProgressBar progressBar;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    AlertDialogHelper alertDialogHelper;
    ImageButton aboutBtn;
    EditText edAbout;
    MenuItem itemAdd;
    UserModel userModel;
    boolean isAdmin = false;
    boolean isStopped = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_info);

        name = getIntent().getStringExtra("name");
        image = getIntent().getStringExtra("profile_url");
        id = getIntent().getStringExtra("id");
        createTime = getIntent().getStringExtra("createTime");
        about = getIntent().getStringExtra("about");

        auth = FirebaseAuth.getInstance();
        currentuserID = auth.getCurrentUser().getUid();
        currentUserName = getUsersName(currentuserID);
        currentUserProfilePic = getUsersProfilePic(currentuserID);

        bindViews();

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                isStopped = false;
                ChatInfoModel model = list.get(position);
                String a = model.getUid();
                if (!a.equals(currentuserID)) {
                    Intent i = new Intent(ChatInfoActivity.this, TwoUsersChatActivity.class);
                    i.putExtra("id", a);
                    startActivity(i);
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                ChatInfoModel model = list.get(position);
                if (!model.getUid().equals(currentuserID)) {
                    if (isAdmin) {
                        if (!model.isAdmin()) {
                            alertDialogHelper.showAlertDialog("", "Choose option for this contact.", "REMOVE",
                                    "CANCEL", "MAKE ADMIN", position, false);
                        }
                    }
                }
            }
        }));

        changeImage.setOnClickListener(v -> {
            isStopped = false;
            GalleryIntent();
        });

        imageView.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(image)) {
                isStopped = false;
                Intent i = new Intent(ChatInfoActivity.this, FullScreenImageActivity.class);
                i.putExtra("profile_url", image);
                i.putExtra("name", name);
                startActivity(i);
            } else {
                if (isAdmin) {
                    isStopped = false;
                    GalleryIntent();
                }
            }

        });

        aboutBtn.setOnClickListener(v -> {
            String updateAbout = edAbout.getText().toString();
            updateGroupDescription(updateAbout);
        });

    }

    private void updateGroupDescription(String updateAbout) {
        for (int i = 0; i < usersList.size(); i++) {
            if (!usersList.get(i).trim().equals("null")) {
                getUserRef().child(usersList.get(i).trim()).child("Chats").child(id).child("about").setValue(updateAbout)
                        .addOnCompleteListener(task -> aboutBtn.setVisibility(View.GONE));
            }
        }
    }

    private void GalleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture_title)), IMAGE_GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        StorageReference storageRef = storage.getReferenceFromUrl(Util.URL_STORAGE_REFERENCE).child(Util.FOLDER_STORAGE_IMG);

        if (requestCode == IMAGE_GALLERY_REQUEST) {
            if (resultCode == RESULT_OK) {
                progressDialog.show();
                progressBar.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.GONE);
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

            uploadTask.addOnFailureListener(e -> {
                //onFailed
            }).addOnSuccessListener(taskSnapshot -> imageGalleryRef.getDownloadUrl().addOnSuccessListener(uri -> {
                image = uri.toString();
                Glide.with(ChatInfoActivity.this).load(uri).into(imageView);
                imageView.setVisibility(View.VISIBLE);
                setgroupImage();
            }));
        }

    }

    private void setgroupImage() {
        getGroupUsersKeyRef().child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        final String key = dataSnapshot1.getKey();
                        assert key != null;
                        getUserRef().child(key).child("Chats").child(id).child("profile_pic").setValue(image);
                        progressBar.setVisibility(View.GONE);
                        progressDialog.dismiss();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void bindViews() {
        imageView = findViewById(R.id.icon);
        edAbout = findViewById(R.id.about);
        changeImage = findViewById(R.id.imageBtn);
        aboutBtn = findViewById(R.id.aboutBtn);
        toolbar = findViewById(R.id.toolbar);
        participants = findViewById(R.id.txtParticipants);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        edAbout.setText(about);
        if (!TextUtils.isEmpty(image)) {
            Glide.with(this).load(image).into(imageView);
        }

        toolbar.setTitle(name);
        toolbar.setSubtitle("Created on " + converteTimestamp(createTime));

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(toolbar.getNavigationIcon()).setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        toolbar.setNavigationOnClickListener(view -> {
            isStopped = false;
            ChatInfoActivity.this.onBackPressed();
        });

        alertDialogHelper = new AlertDialogHelper(this);
        edAbout.addTextChangedListener(this);

    }

    private void retrieveParticipants() {

        getGroupUsersKeyRef().child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    list.clear();
                    usersList.clear();
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        final String key = dataSnapshot1.getKey();
                        boolean checkAdmin = Boolean.parseBoolean(dataSnapshot1.getValue().toString());
                        usersList.add(key);
                        checkBlocked(key, checkAdmin);
                    }

                } else {
                    list.clear();
                    usersList.clear();
                }

                if (!dataSnapshot.hasChild(currentuserID)) {
                    itemAdd.setVisible(false);
                    changeImage.setVisibility(View.GONE);
                    edAbout.setEnabled(false);
                    edAbout.setTextColor(Color.BLACK);
                } else {
                    isAdmin = Boolean.parseBoolean(dataSnapshot.child(currentuserID).getValue().toString());
                    if (!isAdmin) {
                        itemAdd.setVisible(false);
                        changeImage.setVisibility(View.GONE);
                        edAbout.setEnabled(false);
                        edAbout.setTextColor(Color.BLACK);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void checkBlocked(final String key, final boolean checkAdmin) {
        getUserRef().child(currentuserID).child("BlockedList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(key)) {
                    if (checkAdmin) {
                        loadUsers(key, true, true);
                    } else {
                        loadUsers(key, true, false);
                    }
                } else {
                    checkMembersBlocked(key, checkAdmin);
                }

            }

            private void checkMembersBlocked(final String key, final boolean checkAdmin) {
                getUserRef().child(key).child("BlockedList").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(currentuserID)) {
                            if (checkAdmin) {
                                loadUsers(key, true, true);
                            } else {
                                loadUsers(key, true, false);
                            }
                        } else {
                            if (checkAdmin) {
                                loadUsers(key, false, true);
                            } else {
                                loadUsers(key, false, false);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadUsers(String key, final boolean isBlocked, final boolean checkAdmin) {

        FRIEND_NAME = getUsersName(key);
        FRIEND_PIC = getUsersProfilePic(key);
        FRIEND_ABOUT = getUsersAbout(key);
        FRIEND_UID = key;

        model = new ChatInfoModel(FRIEND_NAME, FRIEND_PIC, FRIEND_UID, FRIEND_ABOUT, isBlocked, checkAdmin);
        list.add(model);
        setRecyclerView();

        /*getUserRef().child(key).orderByChild("name").addListenerForSingleValueEvent(new ValueEventListener() {
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


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/
    }

    private void setRecyclerView() {
        participants.setText(list.size() + " Participants");
        adapater = new ChatInfoAdapater(this, list);
        mLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.scheduleLayoutAnimation();
        recyclerView.setAdapter(adapater);
        adapater.notifyDataSetChanged();
    }

    private static CharSequence converteTimestamp(String mileSegundos) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String dateString = formatter.format(new Date(Long.parseLong(mileSegundos)));
        return dateString;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_info, menu);
        itemAdd = menu.findItem(R.id.add);

        retrieveParticipants();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id1 = item.getItemId();
        if (id1 == R.id.add) {
            if (isAdmin) {
                isStopped = false;
                addParticipants();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addParticipants() {
        Intent i = new Intent(ChatInfoActivity.this, AddParticipants.class);
        i.putExtra("name", name);
        i.putExtra("id", id);
        i.putExtra("profile_url", image);
        i.putExtra("createTime", createTime);
        i.putExtra("about", about);
        startActivity(i);
    }

    @Override
    public void onPositiveClick(int from) {
        ChatInfoModel model = list.get(from);
        final String push_key = FirebaseDatabase.getInstance().getReference().push().getKey();
        final String message_type = "notification";

        userModel = new UserModel(currentUserName, currentUserProfilePic, currentuserID);

        getGroupUsersKeyRef().child(id).child(model.getUid()).removeValue();
        list.remove(from);
        adapater.notifyItemRemoved(from);

        final HashMap<String ,Object> update = new HashMap<>();
        update.put("senderName", currentUserName);
        update.put("attachment", "");
        update.put("timeStamp", Long.toString(Calendar.getInstance().getTime().getTime()));

        for (int i = 0; i < usersList.size(); i++) {

            if (!usersList.get(i).trim().equals("null")) {

                String key = usersList.get(i).trim();
                if (key.equals(model.getUid())) {
                    update.put("displayMessage", "Removed you");
                    String message = currentUserName + " Removed You";
                    final ChatModel chatModel = new ChatModel(push_key, message_type, userModel, message,"", Calendar.getInstance().getTime().getTime() + "", null);
                    getChatRef().child(key).child(id).child(push_key).setValue(chatModel);

                } else {
                    update.put("displayMessage", "Removed " + model.getName());

                    if (key.equals(currentuserID)){
                        String message = "You Removed " + model.getName();
                        final ChatModel chatModel = new ChatModel(push_key, message_type, userModel, message,"", Calendar.getInstance().getTime().getTime() + "", null);
                        getChatRef().child(key).child(id).child(push_key).setValue(chatModel);
                    }else {
                        String message = currentUserName + " Removed " + model.getName();
                        final ChatModel chatModel = new ChatModel(push_key, message_type, userModel, message,"", Calendar.getInstance().getTime().getTime() + "", null);
                        getChatRef().child(key).child(id).child(push_key).setValue(chatModel);
                    }
                }
                getUserRef().child(key).child("Chats").child(id).updateChildren(update);


            }
        }


    }

    @Override
    public void onNegativeClick(int from) {

    }

    @Override
    public void onNeutralClick(int from) {
        ChatInfoModel model = list.get(from);
        getGroupUsersKeyRef().child(id).child(model.getUid()).setValue("true");
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!TextUtils.isEmpty(edAbout.getText().toString().trim())) {
            aboutBtn.setVisibility(View.VISIBLE);
        } else {
            aboutBtn.setVisibility(View.GONE);
        }
        if (about.equals(edAbout.getText().toString())) {
            aboutBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

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
