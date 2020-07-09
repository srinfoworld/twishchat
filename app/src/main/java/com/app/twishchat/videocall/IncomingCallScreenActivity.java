package com.app.twishchat.videocall;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import com.app.twishchat.R;
import com.app.twishchat.agvideocall.ui.BaseActivity;
import com.app.twishchat.service.SinchService;
import com.app.twishchat.util.AudioPlayer;
import com.bumptech.glide.Glide;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallListener;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;
import static com.app.twishchat.util.Helper.FRIEND_NAME;
import static com.app.twishchat.util.Helper.FRIEND_PIC;
import static com.app.twishchat.util.Helper.getUsersName;
import static com.app.twishchat.util.Helper.getUsersProfilePic;

public class IncomingCallScreenActivity extends BaseActivity {

    static final String TAG = IncomingCallScreenActivity.class.getSimpleName();

    private String mCallId;
    private AudioPlayer mAudioPlayer;
    CircleImageView userImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call_screen);

        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        mAudioPlayer = new AudioPlayer(this);
        mAudioPlayer.playRingtone();

        Intent intent = getIntent();
        mCallId = intent.getStringExtra(SinchService.CALL_ID);

        findViewById(R.id.answerButton).setOnClickListener(mClickListener);
        findViewById(R.id.declineButton).setOnClickListener(mClickListener);
    }

    @Override
    protected void initUIandEvent() {

    }

    @Override
    protected void deInitUIandEvent() {

    }

    @Override
    public void onServiceConnected() {
        Call call = getSinchServiceInterface().getCall(mCallId);

        FRIEND_NAME = getUsersName(call.getRemoteUserId());
        FRIEND_PIC = getUsersProfilePic(call.getRemoteUserId());

        if (call != null) {
            call.addCallListener(new SinchCallListener());

            TextView remoteUser = findViewById(R.id.callerName);
            remoteUser.setText(FRIEND_NAME);
            userImage = findViewById(R.id.icon);
            if (!TextUtils.isEmpty(FRIEND_PIC)) {
                Glide.with(getApplicationContext()).load(FRIEND_PIC).into(userImage);
            }
            TextView callingType = findViewById(R.id.txt_status);
            callingType.setText((call.getDetails().isVideoOffered() ? "Twish Video Call" : " Twish Voice Call"));
        } else {
            Log.e(TAG, "Started with invalid callId, aborting");
            finish();
        }
    }

    private void answerClicked() {
        mAudioPlayer.stopRingtone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            try {
                call.answer();
                startActivity(CallScreenActivity.newIntent(this, mCallId, "IN"));
                finish();
            } catch (Exception e) {
                Log.e("CHECK", e.getMessage());
            }
        } else {
            finish();
        }
    }

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
        mAudioPlayer.stopRingtone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.hangup();
        }
        finish();
    }

    private class SinchCallListener implements CallListener {

        @Override
        public void onCallEnded(Call call) {
            CallEndCause cause = call.getDetails().getEndCause();
            Log.d(TAG, "Call ended, cause: " + cause.toString());
            mAudioPlayer.stopRingtone();
            finish();
        }

        @Override
        public void onCallEstablished(Call call) {
            Log.d(TAG, "Call established");
        }

        @Override
        public void onCallProgressing(Call call) {
            Log.d(TAG, "Call progressing");
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
            // Send a push through your push provider here, e.g. GCM
        }

    }

    private View.OnClickListener mClickListener = v -> {
        switch (v.getId()) {
            case R.id.answerButton:

                if (!permissionsAvailable(permissionsSinch)) {
                    ActivityCompat.requestPermissions(IncomingCallScreenActivity.this, permissionsSinch, 7);
                } else {
                    answerClicked();
                }
                break;
            case R.id.declineButton:
                declineClicked();
                break;
        }
    };

    @Override
    public void onBackPressed() {
        // User should exit activity by ending call, not by going back.
    }

}
