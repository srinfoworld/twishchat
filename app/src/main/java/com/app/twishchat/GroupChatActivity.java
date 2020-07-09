package com.app.twishchat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.app.twishchat.adapater.ChatAdapater;
import com.app.twishchat.agvideocall.ui.BaseActivity;
import com.app.twishchat.model.ChatModel;
import com.app.twishchat.model.ContactsModel;
import com.app.twishchat.model.FileModel;
import com.app.twishchat.model.MapModel;
import com.app.twishchat.model.UserModel;
import com.app.twishchat.util.AlertDialogHelper;
import com.app.twishchat.util.Helper;
import com.app.twishchat.util.RecyclerItemClickListener;
import com.app.twishchat.util.Util;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import static com.app.twishchat.util.Helper.MESSAGE_COUNT;
import static com.app.twishchat.util.Helper.currentUserName;
import static com.app.twishchat.util.Helper.currentUserProfilePic;
import static com.app.twishchat.util.Helper.currentuserID;
import static com.app.twishchat.util.Helper.getChatRef;
import static com.app.twishchat.util.Helper.getGroupUsersKeyRef;
import static com.app.twishchat.util.Helper.getNotificationRef;
import static com.app.twishchat.util.Helper.getUserRef;
import static com.app.twishchat.util.Helper.getUsersName;
import static com.app.twishchat.util.Helper.getUsersProfilePic;

public class GroupChatActivity extends BaseActivity implements View.OnClickListener, AlertDialogHelper.AlertDialogListener {

    private static final int IMAGE_GALLERY_REQUEST = 1;
    private static final int IMAGE_CAMERA_REQUEST = 2;
    private static final int PLACE_PICKER_REQUEST = 3;
    static final int PICK_CONTACT = 4;

    static final String TAG = MainActivity.class.getSimpleName();
    static final String CHAT_REFERENCE = "Chats";

    String gname = "", profile_url = "", id = "", createTime = "", about = "";

    //Firebase and GoogleApiClient
    FirebaseAuth auth;
    String push_key = "";
    int count = 0;
    FirebaseStorage storage = FirebaseStorage.getInstance();

    //CLass Model
    UserModel userModel;

    //Views UI
    ActionMode mActionMode;
    Menu context_menu;
    MenuItem itemClear, itemExit;
    Toolbar toolbar;
    RecyclerView rvListMessage;
    LinearLayoutManager mLinearLayoutManager;
    ImageView btSendMessage, btEmoji, btAttachment;
    EmojiconEditText edMessage;
    View contentRoot;
    EmojIconActions emojIcon;
    CircleImageView userOrgroupIcon;
    TextView userOrgroupName;
    RelativeLayout chatContainer, profileContainer;
    ProgressDialog progressDialog;
    LinearLayout mRevealView;
    ImageButton btnCamera, btnGallery, btnLocation, btnContact;

    //File
    File filePathImageCamera;

    String UsersKey;
    String contactNumber, contactName;

    AlertDialogHelper alertDialogHelper;
    ChatAdapater chatAdapater;
    ArrayList<ChatModel> chatModels = new ArrayList<>();
    ArrayList<ChatModel> multiselect_list = new ArrayList<>();
    ArrayList<String> list = new ArrayList<>();

    String a;
    int cy, cx, radius;

    boolean isMultiSelect = false;
    boolean isStopped = true;
    boolean isLoaded = false;
    boolean isExited = false;
    boolean isHidden = true;
    boolean isChatOpen = true;

    // Permissions
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 7;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (!Util.verifyConnection(this)) {
            Util.initToast(this, "Internet not available");
            finish();
        } else {
            gname = getIntent().getStringExtra("name");
            profile_url = getIntent().getStringExtra("profile_url");
            id = getIntent().getStringExtra("id");
            createTime = getIntent().getStringExtra("createTime");
            about = getIntent().getStringExtra("about");

            currentuserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            currentUserName = getUsersName(currentuserID);
            currentUserProfilePic = getUsersProfilePic(currentuserID);

            bindViews();

            rvListMessage.addOnItemTouchListener(new RecyclerItemClickListener(this, rvListMessage, new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    ChatModel model = chatModels.get(position);
                    if (isMultiSelect) {
                        if (!model.getMessage_type().equals("notification")) {
                            multi_select(position);
                        }
                    } else {
                        if (model.getMapModel() != null) {
                            clickImageMap(model.getMapModel().getLatitude(), model.getMapModel().getLongitude());
                        } else if (model.getFile() != null) {
                            clickImage(model.getUserModel().getName(), model.getFile().getUrl_file());
                        }
                    }

                }

                @Override
                public void onItemLongClick(View view, int position) {
                    ChatModel model = chatModels.get(position);
                    if (!model.getMessage_type().equals("notification")) {
                        if (!isMultiSelect) {
                            multiselect_list = new ArrayList<>();
                            isMultiSelect = true;

                            if (mActionMode == null) {
                                mActionMode = startSupportActionMode(mActionModeCallback);
                            }
                        }
                        multi_select(position);
                    }
                }
            }));

            profileContainer.setOnClickListener(v -> {
                isStopped = false;
                Intent i = new Intent(GroupChatActivity.this, ChatInfoActivity.class);
                i.putExtra("name", gname);
                i.putExtra("id", id);
                i.putExtra("profile_url", profile_url);
                i.putExtra("createTime", createTime);
                i.putExtra("about", about);
                startActivity(i);
            });


        }
    }

    @Override
    protected void initUIandEvent() {

    }

    @Override
    protected void deInitUIandEvent() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group_chat, menu);
        itemExit = menu.findItem(R.id.exitGroup);
        itemClear = menu.findItem(R.id.clearChat);

        getMessageFirebase();
        retriveInfo();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.clearChat:
                alertDialogHelper.showAlertDialog("", "Are you sure you want to clear chat?", "CLEAR", "CANCEL", 2, false);
                break;
            case R.id.exitGroup:
                alertDialogHelper.showAlertDialog("", "Exit Group?", "EXIT", "CANCEL", 3, false);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void multi_select(int position) {
        if (mActionMode != null) {
            if (multiselect_list.contains(chatModels.get(position)))
                multiselect_list.remove(chatModels.get(position));
            else
                multiselect_list.add(chatModels.get(position));

            if (multiselect_list.size() > 0)
                mActionMode.setTitle("" + multiselect_list.size());
            else
                mActionMode.setTitle("");
            refreshAdapter();
        }
    }

    public void refreshAdapter() {
        chatAdapater.mSelcetedList = multiselect_list;
        chatAdapater.mList = chatModels;
        chatAdapater.notifyDataSetChanged();
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_delete, menu);
            context_menu = menu;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.delete) {
                alertDialogHelper.showAlertDialog("", "Delete Message", "DELETE", "CANCEL", 1, false);
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            isMultiSelect = false;
            multiselect_list = new ArrayList<>();
            refreshAdapter();
        }
    };


    // AlertDialog Callback Functions

    @Override
    public void onPositiveClick(int from) {
        if (from == 1) {
            if (multiselect_list.size() > 0) {
                for (int i = 0; i < multiselect_list.size(); i++) {
                    chatModels.remove(multiselect_list.get(i));

                    final HashMap<String, Object> deleteMap = new HashMap<>();
                    deleteMap.put("displayMessage", "Last message was deleted by you");
                    deleteMap.put("attachment", "");
                    deleteMap.put("senderName", "");

                    getChatRef().child(currentuserID).child(id).child(multiselect_list.get(i).getId())
                            .removeValue().addOnSuccessListener(aVoid -> {
                        getUserRef().child(currentuserID).child("Chats").child(id)
                                .updateChildren(deleteMap);
                    });
                    chatAdapater.notifyDataSetChanged();
                }
                if (mActionMode != null) {
                    mActionMode.finish();
                }
                Toast.makeText(getApplicationContext(), "Message Deleted", Toast.LENGTH_SHORT).show();

            }
        } else if (from == 2) {
            clearChat();
        } else if (from == 3) {
            exitGroup();
        }
    }

    @Override
    public void onNegativeClick(int from) {

    }

    @Override
    public void onNeutralClick(int from) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        StorageReference storageRef = storage.getReferenceFromUrl(Util.URL_STORAGE_REFERENCE).child(Util.FOLDER_STORAGE_IMG);

        if (requestCode == IMAGE_GALLERY_REQUEST) {
            if (resultCode == RESULT_OK) {
                progressDialog.show();
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    sendFileFirebase(storageRef, selectedImageUri);
                }
            }
        } else if (requestCode == IMAGE_CAMERA_REQUEST) {
            if (resultCode == RESULT_OK) {
                progressDialog.show();
                sendFileFirebase(storageRef, data);
            }

        } else if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                progressDialog.show();
                Place place = PlacePicker.getPlace(this, data);
                if (place != null) {
                    push_key = FirebaseDatabase.getInstance().getReference().push().getKey();
                    LatLng latLng = place.getLatLng();
                    MapModel mapModel = new MapModel(latLng.latitude + "", latLng.longitude + "");
                    ChatModel model = new ChatModel(push_key, "", userModel, "", Calendar.getInstance().getTime().getTime() + "", mapModel);
                    sendMessage(push_key, model, "Map");

                }
            }
        } else if (requestCode == PICK_CONTACT) {
            if (resultCode == RESULT_OK) {
                Uri contactData = data.getData();
                Cursor c = managedQuery(contactData, null, null, null, null);
                if (c.moveToFirst()) {
                    String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

                    String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                    if (hasPhone.equalsIgnoreCase("1")) {
                        Cursor phones = getContentResolver().query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                                null, null);
                        phones.moveToFirst();
                        contactNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    }
                    contactName = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    push_key = FirebaseDatabase.getInstance().getReference().push().getKey();
                    ContactsModel contactsModel = new ContactsModel(contactName, contactNumber);
                    ChatModel model = new ChatModel(push_key, "", userModel, "", Calendar.getInstance().getTime().getTime() + "", contactsModel);
                    sendMessage(push_key, model, "Contact");
                }
            }
        }
    }

    private void clearChat() {

        final HashMap<String, Object> clearMap = new HashMap<>();
        clearMap.put("displayMessage", "You cleared the chat");
        clearMap.put("attachment", "");
        clearMap.put("senderName", "");

        getChatRef().child(currentuserID).child(id).removeValue().addOnSuccessListener(aVoid -> {
            getUserRef().child(currentuserID).child("Chats").child(id)
                    .updateChildren(clearMap);
            Toast.makeText(GroupChatActivity.this, "Cleared", Toast.LENGTH_SHORT).show();
            chatAdapater.notifyDataSetChanged();
        });
    }

    private void exitGroup() {
        push_key = FirebaseDatabase.getInstance().getReference().push().getKey();
        String message_type = "notification";

        for (int i = 0; i < list.size(); i++) {
            if (!list.get(i).trim().equals("null")) {

                if (!list.get(i).trim().equals(currentuserID)) {
                    String message = currentUserName + " left the group";
                    final ChatModel model = new ChatModel(push_key, message_type, userModel, message, "", Calendar.getInstance().getTime().getTime() + "", null);
                    getChatRef().child(list.get(i).trim()).child(id).child(push_key).setValue(model);

                } else {
                    String message = "You left the group";
                    final ChatModel model = new ChatModel(push_key, message_type, userModel, message, "", Calendar.getInstance().getTime().getTime() + "", null);
                    getChatRef().child(list.get(i).trim()).child(id).child(push_key).setValue(model);
                }

            }
        }

        final HashMap<String, Object> exitMap = new HashMap<>();
        exitMap.put("displayMessage", "Left the group");
        exitMap.put("attachment", "");
        exitMap.put("senderName", currentUserName);

        getGroupUsersKeyRef().child(id).child(currentuserID).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                getUserRef().child(currentuserID).child("Chats").child(id)
                        .updateChildren(exitMap);
                getGroupUsersKeyRef().child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                UsersKey = dataSnapshot1.getKey();
                                getUserRef().child(UsersKey).child("Chats").child(id)
                                        .updateChildren(exitMap);

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                itemExit.setVisible(false);
                Toast.makeText(GroupChatActivity.this, "You are no longer part of this group", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonAttechment:
                animateView();
                break;
            case R.id.buttonMessage:
                if (TextUtils.isEmpty(edMessage.getText().toString().trim())) return;
                sendMessageFirebase();
                break;
            case R.id.btnCamera:
                if (isExited) {
                    isHidden = true;
                    mRevealView.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Only members can send", Toast.LENGTH_SHORT).show();
                } else {
                    isStopped = false;
                    isHidden = true;
                    mRevealView.setVisibility(View.INVISIBLE);
                    verifyPermissions();
                }
                break;
            case R.id.btnGallery:
                if (isExited) {
                    isHidden = true;
                    mRevealView.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Only members can send", Toast.LENGTH_SHORT).show();
                } else {
                    isStopped = false;
                    isHidden = true;
                    mRevealView.setVisibility(View.INVISIBLE);
                    photoGalleryIntent();
                }
                break;
            case R.id.btnLocation:
                if (isExited) {
                    isHidden = true;
                    mRevealView.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Only members can send", Toast.LENGTH_SHORT).show();
                } else {
                    isStopped = false;
                    isHidden = true;
                    mRevealView.setVisibility(View.INVISIBLE);
                    locationPlacesIntent();
                }
                break;
            case R.id.btnContact:
                isStopped = false;
                isHidden = true;
                mRevealView.setVisibility(View.INVISIBLE);
                phoneBookIntent();
                break;
        }
    }

    private void phoneBookIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }

    private void animateView() {
        if (isHidden) {
            enterReveal();

        } else {
            exitReveal();
        }
    }

    private void enterReveal() {
        cx = mRevealView.getLeft() + mRevealView.getRight();
        cy = mRevealView.getBottom();
        radius = Math.max(mRevealView.getWidth(), mRevealView.getHeight());

        Animator anim = ViewAnimationUtils.createCircularReveal(mRevealView, cx, cy, 0, radius);
        mRevealView.setVisibility(View.VISIBLE);
        anim.start();
        isHidden = false;
    }

    private void exitReveal() {
        cx = mRevealView.getLeft();
        cy = mRevealView.getBottom();
        radius = Math.max(mRevealView.getWidth(), mRevealView.getHeight());
        Animator anim = ViewAnimationUtils.createCircularReveal(mRevealView, cx, cy, radius, 0);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mRevealView.setVisibility(View.INVISIBLE);
                isHidden = true;
            }
        });
        anim.start();
    }

    private void clickImageMap(String latitude, String longitude) {
        isStopped = false;
        String uri = String.format("geo:%s,%s?z=17&q=%s,%s", latitude, longitude, latitude, longitude);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(intent);
    }

    private void clickImage(String name, String url_file) {
        isStopped = false;
        Intent intent = new Intent(this, FullScreenImageActivity.class);
        intent.putExtra("nameUser", name);
        intent.putExtra("profile_url", url_file);
        startActivity(intent);
    }

    private void sendFileFirebase(StorageReference storageReference, final Uri file) {

        push_key = FirebaseDatabase.getInstance().getReference().push().getKey();
        if (storageReference != null) {
            final String name = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();
            final StorageReference imageGalleryRef = storageReference.child(name + "_gallery");
            final UploadTask uploadTask = imageGalleryRef.putFile(file);

            uploadTask.addOnFailureListener(e -> Log.e(TAG, "onFailure sendFileFirebase " + e.getMessage())).addOnSuccessListener(taskSnapshot -> {
                Log.i(TAG, "onSuccess sendFileFirebase");
                imageGalleryRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    FileModel fileModel = new FileModel("img", uri.toString(), name, "");
                    final ChatModel model = new ChatModel(push_key, "", userModel, "", "", Calendar.getInstance().getTime().getTime() + "", fileModel);
                    sendMessage(push_key, model, "Img");
                });
            });
        }

    }

    private void sendFileFirebase(StorageReference storageRef, final Intent data) {
        push_key = FirebaseDatabase.getInstance().getReference().push().getKey();
        final StorageReference imageCameraRef = storageRef.child(data.getData().getLastPathSegment() + "_camera");
        UploadTask uploadTask = imageCameraRef.putFile(data.getData());
        uploadTask.addOnFailureListener(e -> Log.e(TAG, "onFailure sendFileFirebase " + e.getMessage())).addOnSuccessListener(taskSnapshot -> {
            Log.i(TAG, "onSuccess sendFileFirebase");
            imageCameraRef.getDownloadUrl().addOnSuccessListener(uri -> {

                FileModel fileModel = new FileModel("img", uri.toString(), data.getData().getLastPathSegment(), "");
                ChatModel model = new ChatModel(push_key, "", userModel, "", "", Calendar.getInstance().getTime().getTime() + "", fileModel);
                sendMessage(push_key, model, "Img");

            });
        });
    }

    private void locationPlacesIntent() {
        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    private void photoCameraIntent() {

        String nomeFoto = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();
        filePathImageCamera = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), nomeFoto + "camera.jpg");
        Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(it, IMAGE_CAMERA_REQUEST);
    }

    private void photoGalleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture_title)), IMAGE_GALLERY_REQUEST);
    }


    private void sendMessageFirebase() {
        push_key = FirebaseDatabase.getInstance().getReference().push().getKey();
        final ChatModel model = new ChatModel(push_key, "", userModel, edMessage.getText().toString(), "", Calendar.getInstance().getTime().getTime() + "", null);
        sendMessage(push_key, model, "Message");
        edMessage.setText(null);
    }

    private void sendMessage(final String push_key, final ChatModel model, final String attachment) {
        if (isExited) {
            progressDialog.dismiss();
            Toast.makeText(this, "Can't send", Toast.LENGTH_SHORT).show();
            return;
        }
        final String mess = edMessage.getText().toString();

        for (int i = 0; i < list.size(); i++) {

            if (!list.get(i).trim().equals("null")) {
                String key = list.get(i).trim();

                MESSAGE_COUNT = Helper.getMessageCount(key, id);

                if (!TextUtils.isEmpty(MESSAGE_COUNT)) {
                    count = Integer.parseInt(MESSAGE_COUNT);
                }

                HashMap<String, Object> map = new HashMap<>();
                map.put("timeStamp", Long.toString(Calendar.getInstance().getTime().getTime()));
                map.put("displayMessage", mess);
                map.put("senderName", userModel.getName());
                map.put("attachment", attachment);
                if (!key.equals(currentuserID)) {
                    map.put("messageCount", Integer.toString(count + 1));
                }

                HashMap<String, String> notificationMap = new HashMap<>();
                notificationMap.put("from", currentuserID);
                notificationMap.put("message", mess);
                notificationMap.put("name", gname);
                notificationMap.put("pic", profile_url);

                getChatRef().child(key).child(id).child(push_key).setValue(model);
                getUserRef().child(key).child("Chats").child(id).updateChildren(map);

                if (!key.equals(currentuserID)) {
                    getNotificationRef().child(key).push().setValue(notificationMap);
                }

                progressDialog.dismiss();
            }
        }

    }

    private void retriveInfo() {

        getGroupUsersKeyRef().child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(currentuserID)) {
                    chatContainer.setVisibility(View.VISIBLE);
                    btSendMessage.setVisibility(View.VISIBLE);
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        a = dataSnapshot1.getKey();
                        list.add(a);
                    }

                } else {
                    isExited = true;
                    chatContainer.setVisibility(View.GONE);
                    btSendMessage.setVisibility(View.GONE);
                    itemExit.setVisible(false);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        getUserRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(currentuserID).child("Chats").hasChild(id)) {
                    gname = dataSnapshot.child(currentuserID).child("Chats").child(id).child("name").getValue().toString();
                    profile_url = dataSnapshot.child(currentuserID).child("Chats").child(id).child("profile_pic").getValue().toString();
                    about = dataSnapshot.child(currentuserID).child("Chats").child(id).child("about").getValue().toString();

                    userOrgroupName.setText(gname);
                    if (!TextUtils.isEmpty(profile_url)) {
                        Glide.with(getApplicationContext()).load(profile_url).into(userOrgroupIcon);
                    }
                }

                if (dataSnapshot.child(currentuserID).child("Chats").child(id).hasChild("messageCount")) {
                    if (isChatOpen)
                        getUserRef().child(currentuserID).child("Chats").child(id).child("messageCount").setValue("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void getMessageFirebase() {

        userModel = new UserModel(currentUserName, currentUserProfilePic, currentuserID);

        getChatRef().child(currentuserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(id)) {
                    itemClear.setVisible(true);
                    chatModels.clear();
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.child(id).getChildren()) {
                        ChatModel model = dataSnapshot1.getValue(ChatModel.class);
                        chatModels.add(model);
                    }
                    setRecyclerView();
                } else {
                    itemClear.setVisible(false);
                    chatModels.clear();
                    multiselect_list.clear();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void setRecyclerView() {
        chatAdapater = new ChatAdapater(this, chatModels, multiselect_list, userModel.getName());
        mLinearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mLinearLayoutManager.setStackFromEnd(true);
        rvListMessage.setLayoutManager(mLinearLayoutManager);
        rvListMessage.scheduleLayoutAnimation();
        rvListMessage.setAdapter(chatAdapater);
    }

    private void bindViews() {

        contentRoot = findViewById(R.id.contentRoot);
        edMessage = findViewById(R.id.editTextMessage);
        btSendMessage = findViewById(R.id.buttonMessage);
        btEmoji = findViewById(R.id.buttonEmoji);
        rvListMessage = findViewById(R.id.messageRecyclerView);
        chatContainer = findViewById(R.id.chat_container);
        profileContainer = findViewById(R.id.infoContainer);
        btAttachment = findViewById(R.id.buttonAttechment);
        mRevealView = findViewById(R.id.reveal_items);
        mRevealView.setVisibility(View.INVISIBLE);
        btnCamera = findViewById(R.id.btnCamera);
        btnContact = findViewById(R.id.btnContact);
        btnGallery = findViewById(R.id.btnGallery);
        btnLocation = findViewById(R.id.btnLocation);

        btSendMessage.setOnClickListener(this);
        btAttachment.setOnClickListener(this);
        btnCamera.setOnClickListener(this);
        btnContact.setOnClickListener(this);
        btnGallery.setOnClickListener(this);
        btnLocation.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        emojIcon = new EmojIconActions(this, contentRoot, edMessage, btEmoji);
        emojIcon.setIconsIds(R.drawable.ic_baseline_keyboard_24, R.drawable.ic_round_emoji_emotions_24);
        emojIcon.ShowEmojIcon();

        userOrgroupIcon = findViewById(R.id.avatar);
        userOrgroupName = findViewById(R.id.title);
        userOrgroupName.setText(gname);
        if (!TextUtils.isEmpty(profile_url)) {
            Glide.with(this).load(profile_url).into(userOrgroupIcon);
        } else {
            userOrgroupIcon.setImageResource(R.drawable.group);
        }

        alertDialogHelper = new AlertDialogHelper(this);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

    public void verifyPermissions() {
        if (!permissionsAvailable(permissionsCamera)) {
            ActivityCompat.requestPermissions(GroupChatActivity.this, permissionsCamera, REQUEST_ID_MULTIPLE_PERMISSIONS);
        } else {
            photoCameraIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_ID_MULTIPLE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                photoCameraIntent();
            }
        }
    }

    public void back(View view) {
        isStopped = false;
        isChatOpen = false;
        finish();
    }

    @Override
    public void onBackPressed() {
        if (mRevealView.getVisibility() == View.VISIBLE) {
            exitReveal();
        } else {
            super.onBackPressed();
            isStopped = false;
            isChatOpen = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isStopped = true;
        isChatOpen = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edMessage.getWindowToken(), 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        isChatOpen = false;
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            if (isStopped) {
                updateOfflineStatus();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isChatOpen = false;
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
