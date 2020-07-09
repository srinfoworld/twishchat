package com.app.twishchat;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.app.twishchat.adapater.FindFriendsAdapater;
import com.app.twishchat.model.FindFriendModel;
import com.app.twishchat.model.MainModel;
import com.app.twishchat.util.Converter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;
import static com.app.twishchat.util.Helper.checkInPhoneList;
import static com.app.twishchat.util.Helper.currentUserAbout;
import static com.app.twishchat.util.Helper.currentUserName;
import static com.app.twishchat.util.Helper.currentUserProfilePic;
import static com.app.twishchat.util.Helper.currentuserID;
import static com.app.twishchat.util.Helper.getUserRef;
import static com.app.twishchat.util.Helper.getUsersAbout;
import static com.app.twishchat.util.Helper.getUsersName;
import static com.app.twishchat.util.Helper.getUsersProfilePic;
import static com.app.twishchat.util.Helper.isInPhoneList;

public class FindFriends extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    LinearLayoutManager mLayoutManager;
    FindFriendsAdapater adapater;
    ArrayList<FindFriendModel> list = new ArrayList<>();
    ArrayList<String> request_list = new ArrayList<>();
    FirebaseAuth auth;
    TextView txtFriends;
    private MaterialSearchView searchView;
    boolean isChecked = false;
    boolean isStopped = true;
    MenuItem searchItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        toolbar = findViewById(R.id.toolbar);
        txtFriends = findViewById(R.id.txtFriend);
        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.search_view);
        searchView.setBackIcon(getDrawable(R.drawable.ic_arrow_back_color_primary_24dp));
        searchView.setCloseIcon(getDrawable(R.drawable.ic_close_color_primary_24dp));

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(toolbar.getNavigationIcon()).setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        toolbar.setNavigationOnClickListener(view -> {
            isStopped = false;
            FindFriends.this.onBackPressed();
        });

        auth = FirebaseAuth.getInstance();
        currentuserID = auth.getCurrentUser().getUid();
        currentUserName = getUsersName(currentuserID);
        currentUserProfilePic = getUsersProfilePic(currentuserID);
        currentUserAbout = getUsersAbout(currentuserID);

        retrieveUsers();


        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapater.getFilter().filter(newText);
                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                searchView.setVisibility(View.VISIBLE);
                toolbar.setVisibility(View.GONE);
            }

            @Override
            public void onSearchViewClosed() {
                searchView.setVisibility(View.GONE);
                toolbar.setVisibility(View.VISIBLE);
            }
        });
    }

    private void retrieveUsers() {
        getUserRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    list.clear();
                    for (final DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        final String key = dataSnapshot1.getKey();
                        assert key != null;
                        if (!key.equals(currentuserID)) {
                            txtFriends.setVisibility(View.GONE);
                            checkPhoneList(key, dataSnapshot1);
                        } else {
                            txtFriends.setVisibility(View.VISIBLE);
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        getUserRef().child(currentuserID).child("FriendRequest").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    request_list.clear();
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        String key = dataSnapshot1.getKey();
                        request_list.add(key);
                    }
                } else {
                    request_list.clear();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void checkPhoneList(String key, DataSnapshot dataSnapshot1) {
        getUserRef().child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("number")) {
                    txtFriends.setVisibility(View.GONE);
                    String number = dataSnapshot.child("number").getValue().toString();
                    isInPhoneList = checkInPhoneList(number);
                    if (!isInPhoneList) {
                        checkFriend(key, dataSnapshot1);
                    }

                } else {
                    txtFriends.setVisibility(View.GONE);
                    checkFriend(key, dataSnapshot1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkFriend(final String key, final DataSnapshot dataSnapshot1) {

        getUserRef().child(currentuserID).child("Friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(key)) {
                    txtFriends.setVisibility(View.GONE);
                    checksendRequest(key, dataSnapshot1);
                } else {
                    txtFriends.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void checksendRequest(final String key, final DataSnapshot dataSnapshot1) {
        getUserRef().child(currentuserID).child("FriendRequest").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(key)) {
                    txtFriends.setVisibility(View.GONE);
                    checkreceiveRequest(key, dataSnapshot1);
                } else {
                    txtFriends.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkreceiveRequest(final String key, final DataSnapshot dataSnapshot1) {
        getUserRef().child(key).child("FriendRequest").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                txtFriends.setVisibility(View.GONE);
                searchItem.setVisible(true);
                final MainModel data = dataSnapshot1.getValue(MainModel.class);

                if (dataSnapshot.hasChild(currentuserID)) {
                    isChecked = true;
                } else {
                    isChecked = false;
                }
                FindFriendModel model = new FindFriendModel(isChecked, data);
                list.add(model);
                setRecyclerview();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setRecyclerview() {
        adapater = new FindFriendsAdapater(this, list, currentuserID);
        mLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.scheduleLayoutAnimation();
        recyclerView.setAdapter(adapater);
        recyclerView.setVisibility(View.VISIBLE);


        adapater.setOnItemClickListener(new FindFriendsAdapater.OnItemClickListener() {
            @Override
            public void onAddClick(int position) {
                String uid = list.get(position).getMainModel().getUid();
                getUserRef().child(uid).child("FriendRequest").child(currentuserID).setValue(currentuserID);
                list.get(position).changeButton(true);
                adapater.notifyItemChanged(position);
            }

            @Override
            public void onCancelClick(int position) {
                String uid = list.get(position).getMainModel().getUid();
                getUserRef().child(uid).child("FriendRequest").child(currentuserID).removeValue();
                list.get(position).changeButton(false);
                adapater.notifyItemChanged(position);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_find_friends, menu);

        searchItem = menu.findItem(R.id.search);
        searchView.setMenuItem(searchItem);
        searchItem.setVisible(false);
        MenuItem menuItem = menu.findItem(R.id.request);
        menuItem.setIcon(Converter.convertLayoutToImage(FindFriends.this, request_list.size(), R.drawable.ic_person_white_24dp));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case R.id.request:
                isStopped = false;
                Intent i = new Intent(FindFriends.this, FriendRequest.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    searchView.setQuery(searchWrd, false);
                }
            }

            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
            isStopped = false;
        }
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
