package com.app.twishchat.agvideocall.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import com.app.twishchat.R;
import com.app.twishchat.agvideocall.model.ConstantApp;
import com.app.twishchat.util.AudioPlayer;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import static com.app.twishchat.agvideocall.model.ConstantApp.ACTION_KEY_RECEIVER_ID;
import static com.app.twishchat.util.Helper.Denied;
import static com.app.twishchat.util.Helper.Pickup;
import static com.app.twishchat.util.Helper.RECEIVING_REF;
import static com.app.twishchat.util.Helper.Ringing;
import static com.app.twishchat.util.Helper.currentuserID;
import static com.app.twishchat.util.Helper.endVideoCall;
import static com.app.twishchat.util.Helper.getUserRef;
import static com.app.twishchat.util.Helper.getUsersName;
import static com.app.twishchat.util.Helper.getUsersProfilePic;

public class IncomingCallActivity extends BaseActivity {

    private AudioPlayer mAudioPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call_screen);

        currentuserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        TextView tv = findViewById(R.id.callerName);
        ImageView iv = findViewById(R.id.icon);
        tv.setText(getUsersName(ACTION_KEY_RECEIVER_ID));
        if (!TextUtils.isEmpty(getUsersProfilePic(ACTION_KEY_RECEIVER_ID))){
            Glide.with(this).load(getUsersProfilePic(ACTION_KEY_RECEIVER_ID)).into(iv);
        }
        findViewById(R.id.answerButton).setOnClickListener(mClickListener);
        findViewById(R.id.declineButton).setOnClickListener(mClickListener);
    }

    @Override
    protected void initUIandEvent() {
        mAudioPlayer = new AudioPlayer(this);
        mAudioPlayer.playRingtone();
        Ringing(ACTION_KEY_RECEIVER_ID);
        FirebaseCallEvents();
    }

    private void FirebaseCallEvents() {
        getUserRef().child(currentuserID).child(RECEIVING_REF).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    mAudioPlayer.stopRingtone();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void deInitUIandEvent() {

    }

    private View.OnClickListener mClickListener = v -> {
        switch (v.getId()) {
            case R.id.answerButton:
                if (!permissionsAvailable(permissionsSinch)) {
                    ActivityCompat.requestPermissions(IncomingCallActivity.this, permissionsSinch, 7);
                } else {
                    answerClicked();
                }
                break;
            case R.id.declineButton:
                declineClicked();
                break;
        }
    };

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 7){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                answerClicked();
            } else {
                Toast.makeText(this, "This application needs permission to use your microphone to function properly.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void declineClicked() {
        Denied(ACTION_KEY_RECEIVER_ID);
        mAudioPlayer.stopRingtone();
        endVideoCall(currentuserID, ACTION_KEY_RECEIVER_ID,true);
        finish();
    }

    private void answerClicked() {
        Pickup(ACTION_KEY_RECEIVER_ID);
        mAudioPlayer.stopRingtone();
        getUserRef().child(ACTION_KEY_RECEIVER_ID).child("Calling").child(currentuserID).child("pick").setValue("true");
        ConstantApp.OUTGOING = false;
        Intent i = new Intent(this, CallActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onBackPressed() {
        // User should exit activity by ending call, not by going back.
    }
}
