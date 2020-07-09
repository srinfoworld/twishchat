package com.app.twishchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import com.app.twishchat.util.Util;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hbb20.CountryCodePicker;
import java.util.Date;
import java.util.HashMap;
import de.hdodenhof.circleimageview.CircleImageView;

public class AfterLoginProfileActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int IMAGE_GALLERY_REQUEST = 1;

    DatabaseReference Rootref;
    FirebaseAuth auth;
    EditText edName, edAbout, edNumber;
    CircleImageView icon, changeImage;
    ImageView demoImage;
    ProgressDialog progressDialog;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    String currentuserID,getProfilePic="";
    ProgressBar progressBar;
    Toolbar toolbar;
    LinearLayout mRevealView;
    ImageButton btnGallery, btnRemove;
    CountryCodePicker ccp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_login_profile);
        init();
    }

    private void init() {
        ccp = (CountryCodePicker) findViewById(R.id.ccp);
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

        ccp.registerCarrierNumberEditText(edNumber);

        mRevealView = findViewById(R.id.reveal_items);
        btnRemove = findViewById(R.id.btnRemove);
        btnGallery = findViewById(R.id.btnGallery);

        btnRemove.setOnClickListener(this);
        btnGallery.setOnClickListener(this);
        setSupportActionBar(toolbar);

        Rootref = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        currentuserID = auth.getCurrentUser().getUid();

        icon.setOnClickListener(this);
        changeImage.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
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
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imageBtn:
            case R.id.icon:
                GalleryIntent();
                break;
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
            }).addOnSuccessListener(taskSnapshot -> imageGalleryRef.getDownloadUrl().addOnSuccessListener(uri -> {
                getProfilePic = uri.toString();
                Glide.with(AfterLoginProfileActivity.this).load(uri).into(icon);
                progressBar.setVisibility(View.GONE);
                progressDialog.dismiss();
                icon.setVisibility(View.VISIBLE);
            }));
        }

    }

    private void confirm() {

        String about = edAbout.getText().toString();
        String name = edName.getText().toString();
        String number = ccp.getFullNumberWithPlus();
        String email = auth.getCurrentUser().getEmail();
        String deviceToken = FirebaseInstanceId.getInstance().getToken();

        if (about.isEmpty()){
            about = "Hey, I am a Twish User";
        }

        final HashMap<String, Object> map = new HashMap<>();
        map.put("uid", currentuserID);
        map.put("email", email);
        map.put("name", name);
        map.put("about", about);
        map.put("number", number);
        map.put("profile_pic", getProfilePic);
        map.put("device_token", deviceToken);

        Rootref.child("UsersAccount").child(currentuserID).setValue(map).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                progressDialog.dismiss();
                startActivity(new Intent(AfterLoginProfileActivity.this,MainActivity.class));
                finish();
            }
        });
    }
}