package com.app.twishchat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import android.view.ViewAnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.app.twishchat.util.Util;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.app.twishchat.util.Helper.changeDP;
import static com.app.twishchat.util.Helper.getUsersAbout;
import static com.app.twishchat.util.Helper.getUsersName;
import static com.app.twishchat.util.Helper.getUsersNumber;
import static com.app.twishchat.util.Helper.getUsersProfilePic;
import static com.app.twishchat.util.Helper.removeDP;

public class Profile extends AppCompatActivity implements TextWatcher, View.OnClickListener {
    private static final int IMAGE_GALLERY_REQUEST = 1;

    DatabaseReference Rootref;
    FirebaseAuth auth;
    EditText edName, edAbout, edNumber;
    CircleImageView icon, changeImage;
    ImageView demoImage;
    ProgressDialog progressDialog;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    String currentuserID;
    ProgressBar progressBar;
    Toolbar toolbar;
    String getName = "", getAbout = "", getID = "", getProfilePic = "", getNumber = "";
    boolean isVisitor = false;
    boolean isHidden = true;
    boolean isStopped = true;
    MenuItem itemConfirm;
    LinearLayout mRevealView;
    ImageButton btnGallery, btnRemove;
    int cy, cx, radius;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        getID = getIntent().getStringExtra("id");
        getName = getUsersName(getID);
        getProfilePic = getUsersProfilePic(getID);
        getAbout = getUsersAbout(getID);
        getNumber = getUsersNumber(getID);
        isVisitor = Boolean.parseBoolean(getIntent().getStringExtra("visitor"));

        bindViews();
        changeImage.setOnClickListener(v -> {
            isStopped = false;
            if (!TextUtils.isEmpty(getProfilePic)) {
                animateView();
            } else {
                GalleryIntent();
            }

        });

        icon.setOnClickListener(v -> {

            if (!TextUtils.isEmpty(getProfilePic)) {
                isStopped = false;
                Intent i = new Intent(Profile.this, FullScreenImageActivity.class);
                i.putExtra("profile_url", getProfilePic);
                i.putExtra("name", getName);
                startActivity(i);
            } else {
                if (!isVisitor) {
                    isStopped = false;
                    GalleryIntent();
                }
            }


        });
    }

    private void animateView() {
        cx = (mRevealView.getLeft() + mRevealView.getRight());
        cy = mRevealView.getTop();
        radius = Math.max(mRevealView.getWidth(), mRevealView.getHeight());

        if (isHidden) {
            Animator anim = ViewAnimationUtils.createCircularReveal(mRevealView, cx, cy, 0, radius);
            mRevealView.setVisibility(View.VISIBLE);
            anim.start();
            isHidden = false;

        } else {
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
    }

    private void bindViews() {

        toolbar = findViewById(R.id.toolbar);
        edName = findViewById(R.id.name);
        edAbout = findViewById(R.id.about);
        edNumber = findViewById(R.id.number);
        icon = findViewById(R.id.icon);
        changeImage = findViewById(R.id.imageBtn);
        demoImage = findViewById(R.id.demoImage);
        progressBar = findViewById(R.id.progressBar);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        mRevealView = findViewById(R.id.reveal_items);
        btnRemove = findViewById(R.id.btnRemove);
        btnGallery = findViewById(R.id.btnGallery);

        btnRemove.setOnClickListener(this);
        btnGallery.setOnClickListener(this);

        if (isVisitor) {
            toolbar.setSubtitle("");
        }

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(toolbar.getNavigationIcon()).setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        toolbar.setNavigationOnClickListener(view -> {
            isStopped = false;
            Profile.this.onBackPressed();
        });


        edName.setText(getName);
        edAbout.setText(getAbout);
        edNumber.setText(getNumber);
        if (!TextUtils.isEmpty(getProfilePic)) {
            Glide.with(Profile.this).load(getProfilePic).into(icon);
        }

        Rootref = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        currentuserID = auth.getCurrentUser().getUid();


        if (!getID.equals(currentuserID)) {
            changeImage.setVisibility(View.GONE);
            edName.setEnabled(false);
            edAbout.setEnabled(false);
            edNumber.setEnabled(false);
            edName.setTextColor(Color.BLACK);
            edAbout.setTextColor(Color.BLACK);
            edNumber.setTextColor(Color.BLACK);
        } else {
            edName.setEnabled(true);
            edAbout.setEnabled(true);
            edNumber.setEnabled(true);
            changeImage.setVisibility(View.VISIBLE);
        }

    }

    private void GalleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture_title)), IMAGE_GALLERY_REQUEST);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        itemConfirm = menu.findItem(R.id.confirm);
        itemConfirm.setVisible(false);
        edName.addTextChangedListener(this);
        edNumber.addTextChangedListener(this);
        edAbout.addTextChangedListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.confirm) {
            if (TextUtils.isEmpty(edName.getText())) {
                edName.setError("Empty");
            } else if (TextUtils.isEmpty(edNumber.getText())) {
                edNumber.setError("Empty");
            } else {
                progressDialog.show();
                confirm();
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        StorageReference storageRef = storage.getReferenceFromUrl(Util.URL_STORAGE_REFERENCE).child(Util.FOLDER_STORAGE_IMG);

        if (requestCode == IMAGE_GALLERY_REQUEST) {
            if (resultCode == RESULT_OK) {
                progressDialog.show();
                progressBar.setVisibility(View.VISIBLE);
                demoImage.setVisibility(View.GONE);
                icon.setVisibility(View.GONE);
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
                getProfilePic = uri.toString();
                Glide.with(Profile.this).load(uri).into(icon);
                progressBar.setVisibility(View.GONE);
                progressDialog.dismiss();
                icon.setVisibility(View.VISIBLE);
                changeDP(currentuserID, getProfilePic);
            }));
        }

    }

    private void confirm() {
        String deviceToken = FirebaseInstanceId.getInstance().getToken();
        String about = edAbout.getText().toString();

        if (about.isEmpty()){
            about = "Hey, I am a Twish User";
        }

        final HashMap<String, Object> map = new HashMap<>();
        map.put("uid", currentuserID);
        map.put("email", auth.getCurrentUser().getEmail());
        map.put("name", edName.getText().toString());
        map.put("about", about);
        map.put("number", edNumber.getText().toString());
        map.put("profile_pic", getProfilePic);
        map.put("device_token", deviceToken);

        Rootref.child("UsersAccount").child(currentuserID).updateChildren(map).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                progressDialog.dismiss();
                itemConfirm.setVisible(false);
                Toast.makeText(Profile.this, "Success", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mRevealView.getVisibility() == View.VISIBLE) {
            mRevealView.setVisibility(View.INVISIBLE);
            isHidden = true;
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
        Rootref.child("UsersAccount").child(currentuserID).updateChildren(map);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        if (getAbout.equals(edAbout.getText().toString()) &
                getNumber.equals(edNumber.getText().toString()) &
                getName.equals(edName.getText().toString())) {
            itemConfirm.setVisible(false);
        } else {
            itemConfirm.setVisible(true);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnRemove:
                isHidden = true;
                mRevealView.setVisibility(View.INVISIBLE);
                demoImage.setVisibility(View.VISIBLE);
                icon.setVisibility(View.GONE);
                getProfilePic = "";
                removeDP(currentuserID);
                break;
            case R.id.btnGallery:
                isHidden = true;
                mRevealView.setVisibility(View.INVISIBLE);
                GalleryIntent();
                break;

        }
    }
}
