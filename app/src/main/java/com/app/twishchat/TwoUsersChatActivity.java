package com.app.twishchat;

import android.Manifest;
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
import android.os.Handler;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.app.twishchat.agvideocall.model.ConstantApp;
import com.app.twishchat.agvideocall.ui.BaseActivity;
import com.app.twishchat.agvideocall.ui.CallActivity;
import com.app.twishchat.model.ChatModel;
import com.app.twishchat.model.ContactsModel;
import com.app.twishchat.model.FileModel;
import com.app.twishchat.model.MapModel;
import com.app.twishchat.model.UserModel;
import com.app.twishchat.util.AlertDialogHelper;
import com.app.twishchat.util.AudioPlayer;
import com.app.twishchat.util.RecyclerItemClickListener;
import com.app.twishchat.util.Util;
import com.app.twishchat.videocall.CallScreenActivity;
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
import com.sinch.android.rtc.calling.Call;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

import static com.app.twishchat.agvideocall.model.ConstantApp.CALL_LOADED;
import static com.app.twishchat.util.Helper.CALLING_REF;
import static com.app.twishchat.util.Helper.converteTimestamp;
import static com.app.twishchat.util.Helper.currentUserName;
import static com.app.twishchat.util.Helper.currentUserProfilePic;
import static com.app.twishchat.util.Helper.currentuserID;
import static com.app.twishchat.util.Helper.endVideoCall;
import static com.app.twishchat.util.Helper.getChatRef;
import static com.app.twishchat.util.Helper.getNotificationRef;
import static com.app.twishchat.util.Helper.getUserRef;
import static com.app.twishchat.util.Helper.getUsersName;
import static com.app.twishchat.util.Helper.getUsersProfilePic;
import static com.app.twishchat.util.Helper.isCallingStart;
import static com.app.twishchat.util.Helper.startCalling;


public class TwoUsersChatActivity extends BaseActivity implements View.OnClickListener, AlertDialogHelper.AlertDialogListener {

    private static final int IMAGE_GALLERY_REQUEST = 1;
    private static final int IMAGE_CAMERA_REQUEST = 2;
    private static final int PLACE_PICKER_REQUEST = 3;
    static final int PICK_CONTACT = 4;

    static final String TAG = MainActivity.class.getSimpleName();

    String receiverName = "", receiverProfilePic = "", receiverID = "", messageCount = "";
    int count = 0;

    String push_key = "";
    String token;
    FirebaseStorage storage = FirebaseStorage.getInstance();

    UserModel userModel;

    //Views UI
    ActionMode mActionMode;
    Menu context_menu;
    MenuItem itemBlocked, itemUnblocked, itemClear;
    Toolbar toolbar;
    RecyclerView rvListMessage;
    LinearLayoutManager mLinearLayoutManager;
    ImageView btSendMessage, btEmoji, btAttachment;
    EmojiconEditText edMessage;
    View contentRoot;
    EmojIconActions emojIcon;
    CircleImageView userOrgroupIcon, callIcon;
    TextView userOrgroupName, userStatus, msgSeen, callState;
    RelativeLayout chatContainer, profileContainer;
    ProgressDialog progressDialog;
    Animation animation, animationSlideDown, animationSlideUp;
    LinearLayout mRevealView;
    ImageButton btnCamera, btnGallery, btnLocation, btnContact;
    //File
    File filePathImageCamera;

    AlertDialogHelper alertDialogHelper;
    ChatAdapater chatAdapater;
    ArrayList<ChatModel> chatList = new ArrayList<>();
    ArrayList<ChatModel> multiselect_list = new ArrayList<>();

    int cy, cx, radius;
    String contactNumber, contactName;
    boolean isMultiSelect = false;
    boolean isStatus = false;
    boolean isLoaded = false;
    boolean isBlocked = false;
    boolean isMsgRead = false;
    boolean isStopped = true;
    boolean isOnline = false;
    boolean isChatOpen = true;
    boolean isAvailable = false;
    boolean isMsgSend = false;
    boolean callIsVideo = false;
    boolean isHidden = true;
    boolean isBusy = false;

    // Storage Permissions
    protected String[] permissionsSinch = {Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS, Manifest.permission.READ_PHONE_STATE};
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 7;

    Handler handler = new Handler();
    private AudioPlayer mAudioPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (!Util.verifyConnection(this)) {
            Util.initToast(this, "Internet not available");
            finish();
        } else {

            currentuserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

            //RECEIVER
            receiverID = getIntent().getStringExtra("id");
            receiverName = getUsersName(receiverID);
            receiverProfilePic = getUsersProfilePic(receiverID);

            //CURRENT
            currentUserName = getUsersName(currentuserID);
            currentUserProfilePic = getUsersProfilePic(currentuserID);

            isBlocked = Boolean.parseBoolean(getIntent().getStringExtra("blocked"));

            updateCallUi();

            bindViews();

            rvListMessage.addOnItemTouchListener(new RecyclerItemClickListener(this, rvListMessage, new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    if (isMultiSelect) {
                        multi_select(position);
                    } else {
                        ChatModel model = chatList.get(position);
                        if (model.getMapModel() != null) {
                            clickImageMap(model.getMapModel().getLatitude(), model.getMapModel().getLongitude());
                        } else if (model.getFile() != null) {
                            clickImage(model.getUserModel().getName(), model.getFile().getUrl_file());
                        }
                    }

                    if (mRevealView.getVisibility() == View.VISIBLE) {
                        exitReveal();
                    }

                }

                @Override
                public void onItemLongClick(View view, int position) {
                    if (!isMultiSelect) {
                        multiselect_list = new ArrayList<>();
                        isMultiSelect = true;

                        if (mActionMode == null) {
                            mActionMode = startSupportActionMode(mActionModeCallback);
                        }
                    }

                    multi_select(position);
                }
            }));

            profileContainer.setOnClickListener(v -> {
                if (!isBlocked) {
                    isStopped = false;
                    Intent i = new Intent(TwoUsersChatActivity.this, Profile.class);
                    i.putExtra("id", receiverID);
                    i.putExtra("visitor", "true");
                    startActivity(i);
                }
            });
        }
    }

    @Override
    protected void initUIandEvent() {
        FirebaseCallingHandler();
    }

    @Override
    protected void deInitUIandEvent() {

    }


    public void multi_select(int position) {
        if (mActionMode != null) {
            if (multiselect_list.contains(chatList.get(position)))
                multiselect_list.remove(chatList.get(position));
            else
                multiselect_list.add(chatList.get(position));

            if (multiselect_list.size() > 0)
                mActionMode.setTitle("" + multiselect_list.size());
            else
                mActionMode.setTitle("");

            refreshAdapter();
        }
    }

    public void refreshAdapter() {
        chatAdapater.mSelcetedList = multiselect_list;
        chatAdapater.mList = chatList;
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
                    chatList.remove(multiselect_list.get(i));
                    getChatRef().child(currentuserID).child(receiverID)
                            .child(multiselect_list.get(i).getId()).removeValue();
                    getUserRef().child(currentuserID).child("Chats").child(receiverID)
                            .child("displayMessage").setValue("Last message was deleted by you");
                    chatAdapater.notifyDataSetChanged();
                }

                if (mActionMode != null) {
                    mActionMode.finish();
                }

            }
        } else if (from == 2) {
            clearChat();
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
                    sendNewMessage(push_key, model, "Map");
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
                    sendNewMessage(push_key, model, "Contact");
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        context_menu = menu;
        itemBlocked = menu.findItem(R.id.block);
        itemUnblocked = menu.findItem(R.id.unblock);
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
            case R.id.block:
                blockContact();
                break;
            case R.id.unblock:
                unblockContact();
                break;
            case R.id.voiceCall:
                if (!isBlocked) {
                    callIsVideo = false;
                    sinchCall();
                } else {
                    Toast.makeText(this, "Please unblock the contact first", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.videoCall:
                if (!isBlocked) {
                    callIsVideo = true;
                    //sinchCall();
                    agoraCall();
                } else {
                    Toast.makeText(this, "Please unblock the contact first", Toast.LENGTH_SHORT).show();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void agoraCall() {
        if (ConstantApp.OUTGOING & !Util.verifyConnection(this)) return;
        System.out.println("calling");
        ConstantApp.OUTGOING = true;
        ConstantApp.ACTION_KEY_ROOM_ID = currentuserID;
        vSettings().mChannelName = currentuserID;
        vSettings().mEncryptionKey = "";
        showCallContainer();
        startCalling(currentuserID, receiverID, false);

        handler.postDelayed(() -> {
            if (!isCallingStart) {
                isBusy = true;
                callState.setText(R.string.hold);
            }
        }, 1000);
    }

    private void showCallContainer() {
        System.out.println("show");
        callIcon = findViewById(R.id.callIcon);
        if (!TextUtils.isEmpty(ConstantApp.ACTION_KEY_PROFILE_PIC) && !isBlocked) {
            Glide.with(getApplicationContext()).load(ConstantApp.ACTION_KEY_PROFILE_PIC).into(callIcon);
        }
        handler.postDelayed(() -> {
            findViewById(R.id.calling_container).setVisibility(View.VISIBLE);
        }, 200);

    }

    private void hideCallContainer() {
        mAudioPlayer.stopProgressTone();
        findViewById(R.id.calling_container).setVisibility(View.GONE);
        callState.setText(R.string.calling);
        System.out.println("hide");
    }

    private void updateCallUi() {
        if (ConstantApp.OUTGOING) {
            showCallContainer();
        } else {
            ConstantApp.ACTION_KEY_RECEIVER_ID = receiverID;
            ConstantApp.ACTION_KEY_PROFILE_PIC = receiverProfilePic;
        }
    }

    private void FirebaseCallingHandler() {

        getUserRef().child(currentuserID).child(CALLING_REF).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(ConstantApp.ACTION_KEY_RECEIVER_ID).hasChild("status")) {
                    String state = snapshot.child(ConstantApp.ACTION_KEY_RECEIVER_ID).child("status").getValue().toString();
                    if (state.equals("Ringing") && !CALL_LOADED) {
                        System.out.println(state);
                        CALL_LOADED = true;
                        mAudioPlayer.playProgressTone();
                    }
                    callState.setText(state);
                }
                if (snapshot.child(ConstantApp.ACTION_KEY_RECEIVER_ID).hasChild("pick")) {
                    System.out.println("pick");
                    hideCallContainer();
                    boolean pick = Boolean.parseBoolean(snapshot.child(ConstantApp.ACTION_KEY_RECEIVER_ID).child("pick").getValue().toString());
                    if (pick && !ConstantApp.CALLING_START) {
                        ConstantApp.CALLING_START = true;
                        Intent i = new Intent(TwoUsersChatActivity.this, CallActivity.class);
                        i.putExtra(ConstantApp.ACTION_KEY_ENCRYPTION_KEY, "");
                        i.putExtra(ConstantApp.ACTION_KEY_ENCRYPTION_MODE, getResources().getStringArray(R.array.encryption_mode_values)[vSettings().mEncryptionModeIndex]);
                        startActivity(i);
                    } else {
                        if (!ConstantApp.CALLING_START)
                            endVideoCall(currentuserID, ConstantApp.ACTION_KEY_RECEIVER_ID, true);
                    }
                }

                if (!snapshot.exists()) {
                    if (!ConstantApp.OUTGOING) return;
                    mAudioPlayer.stopProgressTone();
                    ConstantApp.CALLING_START = false;
                    ConstantApp.OUTGOING = false;
                    CALL_LOADED = false;
                    System.out.println("loaded false");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sinchCall() {

        if (permissionsAvailable(permissionsSinch)) {
            try {
                Call call = callIsVideo ? getSinchServiceInterface().callUserVideo(receiverID) : getSinchServiceInterface().callUser(receiverID);
                if (call == null) {
                    // Service failed for some reason, show a Toast and abort
                    Toast.makeText(TwoUsersChatActivity.this, "Service is not started. Try stopping the service and starting it again before placing a call.", Toast.LENGTH_LONG).show();
                    return;
                }
                String callId = call.getCallId();
                startActivity(CallScreenActivity.newIntent(TwoUsersChatActivity.this, callId, receiverProfilePic));

            } catch (Exception e) {
                Log.e("CHECK", Objects.requireNonNull(e.getMessage()));
            }
        } else {
            ActivityCompat.requestPermissions(TwoUsersChatActivity.this, permissionsSinch, 69);
        }
    }

    private void unblockContact() {
        progressDialog.show();
        getUserRef().child(currentuserID).child("Chats").child(receiverID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    checkReceiver();
                } else {
                    getUserRef().child("UsersAccount").child(currentuserID).child("BlockedList").child(receiverID).removeValue();
                    progressDialog.dismiss();
                }
            }

            private void checkReceiver() {
                getUserRef().child(receiverID).child("Chats").child(currentuserID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            getUserRef().child(currentuserID).child("BlockedList").child(receiverID).removeValue();
                            getUserRef().child(currentuserID).child("Chats").child(receiverID).child("blocked").setValue("false");
                            getUserRef().child(receiverID).child("Chats").child(currentuserID).child("blocked").setValue("false");
                            progressDialog.dismiss();
                        } else {
                            getUserRef().child(currentuserID).child("BlockedList").child(receiverID).removeValue();
                            getUserRef().child(currentuserID).child("Chats").child(receiverID).child("blocked").setValue("false");
                            progressDialog.dismiss();
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

    private void blockContact() {
        progressDialog.show();
        getUserRef().child(currentuserID).child("Chats").child(receiverID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    checkReceiver();
                } else {
                    getUserRef().child(currentuserID).child("BlockedList").child(receiverID).setValue(receiverID);
                    progressDialog.dismiss();
                }
            }

            private void checkReceiver() {
                getUserRef().child(receiverID).child("Chats").child(currentuserID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            getUserRef().child(currentuserID).child("BlockedList").child(receiverID).setValue(receiverID);
                            getUserRef().child(currentuserID).child("Chats").child(receiverID).child("blocked").setValue("true");
                            getUserRef().child(receiverID).child("Chats").child(currentuserID).child("blocked").setValue("true");
                            progressDialog.dismiss();
                        } else {
                            getUserRef().child(currentuserID).child("BlockedList").child(receiverID).setValue(receiverID);
                            getUserRef().child(currentuserID).child("Chats").child(receiverID).child("blocked").setValue("true");
                            progressDialog.dismiss();
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

    private void clearChat() {
        HashMap<String, Object> updateMap = new HashMap<>();
        updateMap.put("displayMessage", "You cleared the chat");
        updateMap.put("attachment", "");
        getChatRef().child(currentuserID).child(receiverID).removeValue().addOnSuccessListener(aVoid -> {
            getUserRef().child(currentuserID).child("Chats").child(receiverID).updateChildren(updateMap);
            Toast.makeText(TwoUsersChatActivity.this, "Cleared", Toast.LENGTH_SHORT).show();
            chatAdapater.notifyDataSetChanged();
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
                isStopped = false;
                isHidden = true;
                mRevealView.setVisibility(View.INVISIBLE);
                verifyPermissions();
                break;
            case R.id.btnGallery:
                isStopped = false;
                isHidden = true;
                mRevealView.setVisibility(View.INVISIBLE);
                photoGalleryIntent();
                break;
            case R.id.btnLocation:
                isStopped = false;
                isHidden = true;
                mRevealView.setVisibility(View.INVISIBLE);
                locationPlacesIntent();
                break;
            case R.id.btnContact:
                isStopped = false;
                isHidden = true;
                mRevealView.setVisibility(View.INVISIBLE);
                phoneBookIntent();
                break;
            case R.id.declineButton:
                declinedCall();
                break;
        }
    }

    private void declinedCall() {
        mAudioPlayer.stopProgressTone();
        hideCallContainer();
        if (!isBusy) {
            endVideoCall(currentuserID, ConstantApp.ACTION_KEY_RECEIVER_ID, true);
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
        intent.putExtra("name", name);
        intent.putExtra("profile_url", url_file);
        startActivity(intent);
    }

    private void sendFileFirebase(StorageReference storageReference, final Uri file) {
        if (storageReference != null) {
            push_key = FirebaseDatabase.getInstance().getReference().push().getKey();
            final String name = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();
            final StorageReference imageGalleryRef = storageReference.child(name + "_gallery");
            final UploadTask uploadTask = imageGalleryRef.putFile(file);

            uploadTask.addOnFailureListener(e -> Log.e(TAG, "onFailure sendFileFirebase " + e.getMessage())).addOnSuccessListener(taskSnapshot -> {
                Log.i(TAG, "onSuccess sendFileFirebase");
                imageGalleryRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    FileModel fileModel = new FileModel("img", uri.toString(), name, "");
                    ChatModel model = new ChatModel(push_key, "", userModel, "", "", Calendar.getInstance().getTime().getTime() + "", fileModel);
                    sendNewMessage(push_key, model, "Img");
                });
            });
        }

    }

    private void sendFileFirebase(StorageReference storageRef, final Intent data) {
        if (storageRef != null) {
            push_key = FirebaseDatabase.getInstance().getReference().push().getKey();
            final StorageReference imageCameraRef = storageRef.child(Objects.requireNonNull(data.getData()).getLastPathSegment() + "_camera");

            UploadTask uploadTask = imageCameraRef.putFile(data.getData());
            uploadTask.addOnFailureListener(e -> Log.e(TAG, "onFailure sendFileFirebase " + e.getMessage())).addOnSuccessListener(taskSnapshot -> {
                Log.i(TAG, "onSuccess sendFileFirebase");
                imageCameraRef.getDownloadUrl().addOnSuccessListener(uri -> {

                    FileModel fileModel = new FileModel("img", uri.toString(), data.getData().getLastPathSegment(), "");
                    ChatModel model = new ChatModel(push_key, "", userModel, "", "", Calendar.getInstance().getTime().getTime() + "", fileModel);
                    sendNewMessage(push_key, model, "Img");

                });
            });
        }
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
        ChatModel model = new ChatModel(push_key, "", userModel, edMessage.getText().toString(), "", Calendar.getInstance().getTime().getTime() + "", null);
        //   sendMessage(push_key, model, "Message");
        sendNewMessage(push_key, model, "Message");
        edMessage.setText(null);
    }

    private void sendNewMessage(String push_key, ChatModel model, String attachment) {
        final String mess = edMessage.getText().toString();

        final HashMap<String, Object> Map = new HashMap<>();
        Map.put("attachment", attachment);
        Map.put("type", "1to1");
        Map.put("timeStamp", Long.toString(Calendar.getInstance().getTime().getTime()));
        Map.put("senderName", currentUserName);
        Map.put("displayMessage", mess);
        Map.put("msgRead", "false");
        Map.put("messageCount", "0");
        Map.put("id", receiverID);

        getChatRef().child(currentuserID).child(receiverID).child(push_key).setValue(model);
        getUserRef().child(currentuserID).child("Chats").child(receiverID).updateChildren(Map);

        if (!isBlocked) {
            Map.put("messageCount", Integer.toString(count + 1));
            Map.put("id", currentuserID);
            getChatRef().child(receiverID).child(currentuserID).child(push_key).setValue(model);
            getUserRef().child(receiverID).child("Chats").child(currentuserID).updateChildren(Map);
        }

        if (!isBlocked) {
            if (!isMsgRead) {
                HashMap<String, String> notificationMap = new HashMap<>();
                notificationMap.put("from", currentuserID);
                notificationMap.put("message", mess);
                notificationMap.put("name", currentUserName);
                notificationMap.put("pic", currentUserProfilePic);
                getNotificationRef().child(receiverID).push().setValue(notificationMap);
            }
        }
        progressDialog.dismiss();
    }


    private void retriveInfo() {

        getUserRef().child(receiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child("BlockedList").hasChild(currentuserID)) {
                    isBlocked = true;
                    itemBlocked.setVisible(false);
                    userStatus.setVisibility(View.GONE);
                    userOrgroupIcon.setImageResource(R.drawable.person);
                }

                if (dataSnapshot.hasChild("online")) {
                    isOnline = Boolean.parseBoolean(dataSnapshot.child("online").getValue().toString());
                    String timeStamp = dataSnapshot.child("timeStamp").getValue().toString();

                    if (!isBlocked) {
                        if (isOnline) {
                            userStatus.setText(R.string.online);
                        } else {
                            userStatus.setText(converteTimestamp(timeStamp, 3));
                        }

                        if (!isStatus) {
                            isStatus = true;
                            userStatus.setVisibility(View.VISIBLE);
                            userStatus.startAnimation(animation);
                            userStatus.setSelected(true);
                        }
                    }
                }

                if (dataSnapshot.child("Chats").hasChild(currentuserID)) {
                    isAvailable = true;
                    if (dataSnapshot.child("Chats").child(currentuserID).hasChild("messageCount")) {
                        messageCount = dataSnapshot.child("Chats").child(currentuserID).child("messageCount").getValue().toString();
                    }
                    if (dataSnapshot.child("Chats").child(currentuserID).hasChild("msgRead")) {
                        isMsgRead = Boolean.parseBoolean(dataSnapshot.child("Chats").child(currentuserID).child("msgRead").getValue().toString());
                    }
                    if (!isMsgRead) {
                        if (!TextUtils.isEmpty(messageCount)) {
                            count = Integer.parseInt(messageCount);
                        }
                    }
                } else {
                    isAvailable = false;
                    count = 0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        getUserRef().child(currentuserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    if (dataSnapshot.child("Chats").child(receiverID).hasChild("msgRead")) {
                        if (isChatOpen) {
                            getUserRef().child(currentuserID).child("Chats")
                                    .child(receiverID).child("msgRead").setValue("true");
                        }
                    }

                    if (dataSnapshot.child("Chats").child(receiverID).hasChild("blocked")) {
                        isBlocked = Boolean.parseBoolean(dataSnapshot.child("Chats").child(receiverID).child("blocked").getValue().toString());
                    }

                    if (dataSnapshot.child("Chats").child(receiverID).hasChild("messageCount")) {
                        if (isChatOpen) {
                            messageCount = dataSnapshot.child("Chats").child(receiverID).child("messageCount").getValue().toString();
                            if (Integer.parseInt(messageCount) > 0) {
                                if (!chatList.isEmpty()) {
                                    ChatModel last = chatList.get(chatList.size() - 1);
                                    String id = last.getId();
                                    if (isAvailable) {
                                        getChatRef().child(receiverID).child(currentuserID).child(id).child("seen").setValue("true");
                                    }
                                    getUserRef().child(currentuserID).child("Chats").child(receiverID).child("messageCount").setValue("0");
                                }
                            }

                        }
                    }

                }

                if (!dataSnapshot.child("BlockedList").hasChild(receiverID)) {
                    if (!isBlocked) {
                        itemBlocked.setVisible(true);
                        itemUnblocked.setVisible(false);
                        userStatus.setVisibility(View.VISIBLE);
                        if (!TextUtils.isEmpty(receiverProfilePic) && !isBlocked) {
                            Glide.with(getApplicationContext()).load(receiverProfilePic).into(userOrgroupIcon);
                        }
                    } else {
                        itemBlocked.setVisible(false);
                        userStatus.setVisibility(View.GONE);
                        userOrgroupIcon.setImageResource(R.drawable.person);
                    }
                } else {
                    isBlocked = true;
                    itemBlocked.setVisible(false);
                    itemUnblocked.setVisible(true);
                    userStatus.setVisibility(View.GONE);
                    userOrgroupIcon.setImageResource(R.drawable.person);
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
                if (dataSnapshot.hasChild(receiverID)) {
                    itemClear.setVisible(true);
                    chatList.clear();
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.child(receiverID).getChildren()) {
                        ChatModel model = dataSnapshot1.getValue(ChatModel.class);
                        chatList.add(model);
                        setRecyclerView();
                    }
                } else {
                    chatList.clear();
                    itemClear.setVisible(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setRecyclerView() {
        chatAdapater = new ChatAdapater(this, chatList, multiselect_list, userModel.getName());
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
        chatContainer.setVisibility(View.VISIBLE);
        userOrgroupIcon = findViewById(R.id.avatar);
        userOrgroupName = findViewById(R.id.title);
        userStatus = findViewById(R.id.status);
        msgSeen = findViewById(R.id.msgSeen);
        btAttachment = findViewById(R.id.buttonAttechment);
        mRevealView = findViewById(R.id.reveal_items);
        mRevealView.setVisibility(View.INVISIBLE);
        btnCamera = findViewById(R.id.btnCamera);
        btnContact = findViewById(R.id.btnContact);
        btnGallery = findViewById(R.id.btnGallery);
        btnLocation = findViewById(R.id.btnLocation);
        toolbar = findViewById(R.id.toolbar);
        callState = findViewById(R.id.callState);
        setSupportActionBar(toolbar);

        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        animationSlideDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
        animationSlideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);

        btSendMessage.setOnClickListener(this);
        btAttachment.setOnClickListener(this);
        btnCamera.setOnClickListener(this);
        btnContact.setOnClickListener(this);
        btnGallery.setOnClickListener(this);
        btnLocation.setOnClickListener(this);
        findViewById(R.id.declineButton).setOnClickListener(this);

        emojIcon = new EmojIconActions(this, contentRoot, edMessage, btEmoji);
        emojIcon.setIconsIds(R.drawable.ic_baseline_keyboard_24, R.drawable.ic_round_emoji_emotions_24);
        emojIcon.ShowEmojIcon();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        userOrgroupName.setText(receiverName);
        if (!TextUtils.isEmpty(receiverProfilePic) && !isBlocked) {
            Glide.with(getApplicationContext()).load(receiverProfilePic).into(userOrgroupIcon);
        } else {
            userOrgroupIcon.setImageResource(R.drawable.person);
        }
        alertDialogHelper = new AlertDialogHelper(this);
        mAudioPlayer = new AudioPlayer(this);
    }


    public void verifyPermissions() {

        if (!permissionsAvailable(permissionsCamera)) {
            ActivityCompat.requestPermissions(TwoUsersChatActivity.this, permissionsCamera, REQUEST_ID_MULTIPLE_PERMISSIONS);
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
            if (findViewById(R.id.calling_container).getVisibility() == View.VISIBLE) {
                Toast.makeText(this, "Call Running ", Toast.LENGTH_SHORT).show();
                return;
            }
            super.onBackPressed();
            isStopped = false;
            isChatOpen = false;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            messageReading();
            isStopped = true;
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            messagenotReading();
            if (isStopped) {
                updateOfflineStatus();
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edMessage.getWindowToken(), 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
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

    private void messageReading() {
        isChatOpen = true;
    }

    private void messagenotReading() {
        isChatOpen = false;
        getUserRef().child(currentuserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("Chats").child(receiverID).hasChild("msgRead")) {
                    getUserRef().child(currentuserID).child("Chats")
                            .child(receiverID).child("msgRead").setValue("false");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
