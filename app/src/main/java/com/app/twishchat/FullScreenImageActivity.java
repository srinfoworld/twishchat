package com.app.twishchat;

import android.app.ProgressDialog;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

public class FullScreenImageActivity extends AppCompatActivity {

    private TouchImageView mImageView;
    private ProgressDialog progressDialog;
    Toolbar toolbar;
    String nameUser= "",urlPhotoClick= "";
    private DatabaseReference Rootref;
    private FirebaseAuth auth;
    String currentuserID;
    boolean isStopped =true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);
        bindViews();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isStopped = false;
        System.gc();
        finish();
    }


    private void bindViews(){
        progressDialog = new ProgressDialog(this);
        mImageView = findViewById(R.id.imageView);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(toolbar.getNavigationIcon()).setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        toolbar.setNavigationOnClickListener(view -> {
            isStopped = false;
            FullScreenImageActivity.this.onBackPressed();
        });
    }

    private void setValues(){

        nameUser = getIntent().getStringExtra("name");
        urlPhotoClick = getIntent().getStringExtra("profile_url");
        toolbar.setTitle(nameUser);

        Glide.with(this).load(urlPhotoClick).into(new SimpleTarget<Drawable>() {

            @Override
            public void onLoadStarted(Drawable placeholder) {
                progressDialog.setMessage("Please wait...");
                progressDialog.show();
            }

            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                progressDialog.dismiss();
                mImageView.setImageDrawable(resource);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        auth = FirebaseAuth.getInstance();
        Rootref = FirebaseDatabase.getInstance().getReference();
        setValues();

    }

    @Override
    protected void onStop() {
        super.onStop();
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


    private void updateOfflineStatus() {
        HashMap<String ,Object> map = new HashMap<>();
        map.put("online", "false");
        map.put("timeStamp", Long.toString(Calendar.getInstance().getTime().getTime()));
        Rootref.child("UsersAccount").child(currentuserID).updateChildren(map);
    }
}
