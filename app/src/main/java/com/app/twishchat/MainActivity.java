package com.app.twishchat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.app.twishchat.adapater.MainAdapater;
import com.app.twishchat.agvideocall.ui.BaseActivity;
import com.app.twishchat.model.ContactsModel;
import com.app.twishchat.model.MainModel;
import com.app.twishchat.util.AlertDialogHelper;
import com.app.twishchat.util.RecyclerItemClickListener;
import com.app.twishchat.util.Util;
import com.app.twishchat.videocall.BasicActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import static com.app.twishchat.util.Helper.currentUserAbout;
import static com.app.twishchat.util.Helper.currentUserName;
import static com.app.twishchat.util.Helper.currentUserProfilePic;
import static com.app.twishchat.util.Helper.currentuserID;
import static com.app.twishchat.util.Helper.getChatRef;
import static com.app.twishchat.util.Helper.getUserRef;
import static com.app.twishchat.util.Helper.getUsersAbout;
import static com.app.twishchat.util.Helper.getUsersName;
import static com.app.twishchat.util.Helper.getUsersProfilePic;
import static com.app.twishchat.util.Helper.phoneContactList;


public class MainActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener, AlertDialogHelper.AlertDialogListener {

    RecyclerView recyclerView;
    LinearLayoutManager mLayoutManager;
    MainAdapater adapater;
    ArrayList<MainModel> list = new ArrayList<>();
    ArrayList<MainModel> multiselect_list = new ArrayList<>();
    private FirebaseAuth auth;
    private GoogleApiClient mGoogleApiClient;
    FloatingActionButton allFriends_btn;
    AlertDialogHelper alertDialogHelper;
    Toolbar toolbar;
    ActionMode mActionMode;
    Menu context_menu;
    boolean isMultiSelect = false;
    boolean isStopped = true;
    TextView txtStart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alertDialogHelper = new AlertDialogHelper(this);
        recyclerView = findViewById(R.id.recyclerView);
        allFriends_btn = findViewById(R.id.allFriends_btn);
        toolbar = findViewById(R.id.toolbar);
        txtStart = findViewById(R.id.txtStart);
        setSupportActionBar(toolbar);

        auth = FirebaseAuth.getInstance();
        currentuserID = auth.getCurrentUser().getUid();

        currentUserName = getUsersName(currentuserID);
        currentUserProfilePic = getUsersProfilePic(currentuserID);
        currentUserAbout = getUsersAbout(currentuserID);

        if (!permissionsAvailable(permissionsAll)) {
            ActivityCompat.requestPermissions(MainActivity.this, permissionsAll, 69);
        } else {
            fetchMyContacts();
            generateToken(currentuserID);
        }
        retriveUsers();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        allFriends_btn.setOnClickListener(v -> {
            isStopped = false;
            startSinchClient();
            Intent i = new Intent(new Intent(MainActivity.this, Contacts.class));
            startActivity(i);
        });

        recyclerView.addOnItemTouchListener(new
                RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                if (isMultiSelect) {
                    multi_select(position);
                    return;
                }

                isStopped = false;
                String id = list.get(position).getId();
                String name = list.get(position).getName();
                String profile_url = list.get(position).getProfile_pic();
                String type = list.get(position).getType();
                String createTime = list.get(position).getCreateTime();
                String about = list.get(position).getAbout();
                String blocked = list.get(position).getBlocked();

                if (type.equals("group")) {
                    Intent i = new Intent(MainActivity.this, GroupChatActivity.class);
                    i.putExtra("id", id);
                    i.putExtra("name", name);
                    i.putExtra("profile_url", profile_url);
                    i.putExtra("createTime", createTime);
                    i.putExtra("about", about);
                    startActivity(i);

                } else if (type.equals("1to1")) {
                    Intent i = new Intent(MainActivity.this, TwoUsersChatActivity.class);
                    i.putExtra("id", id);
                    i.putExtra("blocked", blocked);
                    startActivity(i);
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

    }

    @Override
    protected void initUIandEvent() {

    }

    @Override
    protected void deInitUIandEvent() {

    }

    public void multi_select(int position) {
        if (mActionMode != null) {
            if (multiselect_list.contains(list.get(position)))
                multiselect_list.remove(list.get(position));
            else
                multiselect_list.add(list.get(position));

            if (multiselect_list.size() > 0)
                mActionMode.setTitle("" + multiselect_list.size());
            else
                mActionMode.setTitle("");

            refreshAdapter();
        }
    }

    public void refreshAdapter() {
        adapater.mSelcetedList = multiselect_list;
        adapater.mList = list;
        adapater.notifyDataSetChanged();
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
                    list.remove(multiselect_list.get(i));
                    getUserRef().child(currentuserID).child("Chats")
                            .child(multiselect_list.get(i).getId()).removeValue();
                    getChatRef().child(currentuserID).child(multiselect_list.get(i).getId()).removeValue();
                    adapater.notifyDataSetChanged();
                }

                if (mActionMode != null) {
                    mActionMode.finish();
                }

            }
        }
    }

    @Override
    public void onNegativeClick(int from) {

    }

    @Override
    public void onNeutralClick(int from) {

    }

    private void retriveUsers() {

        getUserRef().child(currentuserID).child("Chats").orderByChild("timeStamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    txtStart.setVisibility(View.GONE);
                    list.clear();
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        MainModel data = dataSnapshot1.getValue(MainModel.class);
                        list.add(data);
                    }
                    setRecyclerview();
                } else {
                    txtStart.setVisibility(View.VISIBLE);
                    list.clear();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void setRecyclerview() {
        adapater = new MainAdapater(this, list, multiselect_list, currentUserName);
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.scheduleLayoutAnimation();
        recyclerView.setAdapter(adapater);
        adapater.notifyDataSetChanged();
        startSinchClient();
    }

    private void startSinchClient() {
        try {
            if (!getSinchServiceInterface().isStarted()) {
                getSinchServiceInterface().startClient(currentuserID);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.profile:
                isStopped = false;
                openProfile();
                return true;
            case R.id.createGroup:
                isStopped = false;
                createGroup();
                return true;
            case R.id.findFriends:
                isStopped = false;
                openFindFriends();
                return true;
            case R.id.sign_out:
                signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openFindFriends() {
        startSinchClient();
        Intent i = new Intent(new Intent(MainActivity.this, FindFriends.class));
        startActivity(i);
    }

    private void openProfile() {
        startSinchClient();
        Intent i = new Intent(MainActivity.this, Profile.class);
        i.putExtra("id", currentuserID);
        i.putExtra("visitor", "false");
        startActivity(i);
    }

    private void signOut() {
        updateOfflineStatus();
        auth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void createGroup() {
        startSinchClient();
        Intent i = new Intent(MainActivity.this, CreateGroup.class);
        startActivity(i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Util.initToast(this, "Google Play Services error.");
    }


    public void fetchMyContacts() {

        phoneContactList = new ArrayList<>();
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        if (cursor != null && !cursor.isClosed()) {
            cursor.getCount();
            while (cursor.moveToNext()) {
                int hasPhoneNumber = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                if (hasPhoneNumber == 1) {
                    String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replaceAll("\\s+", "");
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
                    if (Patterns.PHONE.matcher(number).matches()) {
                        boolean hasPlus = String.valueOf(number.charAt(0)).equals("+");
                        number = number.replaceAll("[\\D]", "");
                        if (hasPlus) {
                            number = "+" + number;
                        }
                        ContactsModel contact = new ContactsModel(name, number);
                        if (!phoneContactList.contains(contact)) {
                            phoneContactList.add(contact);
                        }
                    }
                }
            }
            cursor.close();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 69) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchMyContacts();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            currentuserID = auth.getCurrentUser().getUid();
            if (isStopped) {
                updateOnlineStatus();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            currentuserID = auth.getCurrentUser().getUid();
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

    private void updateOnlineStatus() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("online", "true");
        map.put("timeStamp", Long.toString(Calendar.getInstance().getTime().getTime()));
        getUserRef().child(currentuserID).updateChildren(map);
    }

    private void updateOfflineStatus() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("online", "false");
        map.put("timeStamp", Long.toString(Calendar.getInstance().getTime().getTime()));
        getUserRef().child(currentuserID).updateChildren(map);
    }
}
