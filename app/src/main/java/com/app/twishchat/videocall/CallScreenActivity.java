package com.app.twishchat.videocall;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.app.twishchat.R;
import com.app.twishchat.TwoUsersChatActivity;
import com.app.twishchat.agvideocall.ui.BaseActivity;
import com.app.twishchat.service.SinchService;
import com.app.twishchat.util.AudioPlayer;
import com.app.twishchat.util.OnDragTouchListener;
import com.bumptech.glide.Glide;
import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallState;
import com.sinch.android.rtc.video.VideoCallListener;
import com.sinch.android.rtc.video.VideoController;
import com.sinch.android.rtc.video.VideoScalingType;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.app.twishchat.util.Helper.FRIEND_NAME;
import static com.app.twishchat.util.Helper.FRIEND_PIC;
import static com.app.twishchat.util.Helper.getUsersName;
import static com.app.twishchat.util.Helper.getUsersProfilePic;


public class CallScreenActivity extends BaseActivity implements SensorEventListener {
    static final String TAG = CallScreenActivity.class.getSimpleName();
    static final String ADDED_LISTENER = "addedListener";
    static String USER_PROFILE_PICTURE = "userprofile";

    private AudioPlayer mAudioPlayer;
    private Timer mTimer;
    private UpdateCallDurationTask mDurationTask;

    private String mCallId;
    private boolean mAddedListener, mLocalVideoViewAdded, mRemoteVideoViewAdded, isVideo, isMute, isSpeaker, alphaInvisible;
    int mCallDurationSecond = 0;
    private RelativeLayout mainContainer;
    private View view;
    private TextView mCallDuration, mCallState, mCallerName, mCallType;
    private ImageView userImage, switchVideo, switchMic, switchVolume;
    private FrameLayout localVideo, remoteVideo, rv2;
    private LinearLayout mySwitchCameraLLY, btnsLayout;

    private SensorManager mSensorManager;
    private Sensor mProximity;
    PowerManager.WakeLock wlOff = null, wlOn = null;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (wlOff != null && wlOff.isHeld()) {
                wlOff.release();
            } else if (wlOn != null && wlOn.isHeld()) {
                wlOn.release();
            }

        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }
    }

    @SuppressLint("InvalidWakeLockTag")
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float distance = sensorEvent.values[0];
        if (!isVideo && !isSpeaker) {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (distance < 4) {
                if (wlOn != null && wlOn.isHeld()) {
                    wlOn.release();
                }
                if (pm != null) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        if (wlOff == null)
                            wlOff = pm.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "tag");
                        if (!wlOff.isHeld()) wlOff.acquire(10 * 60 * 1000L /*10 minutes*/);
                    }
                }
            } else {
                if (wlOff != null && wlOff.isHeld()) {
                    wlOff.release();
                }
                if (pm != null) {
                    if (wlOn == null)
                        wlOn = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "tag");
                    if (!wlOn.isHeld()) wlOn.acquire(10 * 60 * 1000L /*10 minutes*/);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    private class UpdateCallDurationTask extends TimerTask {

        @Override
        public void run() {
            CallScreenActivity.this.runOnUiThread(() -> updateCallDuration());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(ADDED_LISTENER, mAddedListener);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mAddedListener = savedInstanceState.getBoolean(ADDED_LISTENER);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_screen);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        Intent intent = getIntent();
        mCallId = intent.getStringExtra(SinchService.CALL_ID);

        mAudioPlayer = new AudioPlayer(this);
        mCallDuration = findViewById(R.id.callDuration);
        mCallerName = findViewById(R.id.callerName);
        mCallState = findViewById(R.id.callState);
        userImage = findViewById(R.id.icon);
        mCallType = findViewById(R.id.callType);
        localVideo = findViewById(R.id.localVideo);
        remoteVideo = findViewById(R.id.remoteVideo);
        switchVideo = findViewById(R.id.switchVideo);
        switchMic = findViewById(R.id.switchMic);
        switchVolume = findViewById(R.id.switchVolume);
        view = findViewById(R.id.view);
        mySwitchCameraLLY = findViewById(R.id.switchVideo_LLY);
        btnsLayout = findViewById(R.id.layout_btns);
        mainContainer = findViewById(R.id.main_container);

        rv2 = findViewById(R.id.rv2);

        findViewById(R.id.hangupButton).setOnClickListener(v -> endCall());


        switchMic.setOnClickListener(view -> {
            isMute = !isMute;
            setMuteUnmute();
        });

        switchVolume.setOnClickListener(view -> {
            isSpeaker = !isSpeaker;
            enableSpeaker(isSpeaker);
        });

        switchVideo.setClickable(false);

        remoteVideo.setOnClickListener(v -> {
            if (btnsLayout.getVisibility() == View.VISIBLE) {
                btnsLayout.setVisibility(View.GONE);
                findViewById(R.id.hangupButton).setVisibility(View.GONE);
            } else {
                btnsLayout.setVisibility(View.VISIBLE);
                findViewById(R.id.hangupButton).setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void initUIandEvent() {

    }

    @Override
    protected void deInitUIandEvent() {

    }

    @Override
    public void onPause() {
        super.onPause();
        mDurationTask.cancel();
        mTimer.cancel();
        mSensorManager.unregisterListener(this);
        removeVideoViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        mTimer = new Timer();
        mDurationTask = new UpdateCallDurationTask();
        mTimer.schedule(mDurationTask, 0, 500);
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
        updateUI();
    }

    @Override
    public void onBackPressed() {
        // User should exit activity by ending call, not by going back.
    }

    private void endCall() {
        mAudioPlayer.stopProgressTone();
        Call call = getSinchServiceInterface().getCall(mCallId);

        if (call != null) {
            call.hangup();
        }

        finish();
    }

    private String formatTimespan(int totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    private void updateCallDuration() {
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            mCallDurationSecond = call.getDetails().getDuration();
            mCallDuration.setText(formatTimespan(call.getDetails().getDuration()));
        }
    }

    @Override
    public void onServiceConnected() {
        Call call = getSinchServiceInterface().getCall(mCallId);
        FRIEND_NAME = getUsersName(call.getRemoteUserId());
        FRIEND_PIC = getUsersProfilePic(call.getRemoteUserId());

        if (call != null) {
            if (!mAddedListener) {
                call.addCallListener(new SinchCallListener());
                mAddedListener = true;
            }
        } else {
            Log.e(TAG, "Started with invalid callId, aborting.");
            finish();
        }

        updateUI();
    }

    private void updateUI() {
        if (getSinchServiceInterface() == null) {
            return; // early
        }

        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {

            mCallerName.setText(FRIEND_NAME);
            mCallState.setText(call.getState().toString());
            if (!TextUtils.isEmpty(FRIEND_PIC)) {
                Glide.with(getApplicationContext()).load(FRIEND_PIC).into(userImage);
            }

            isVideo = call.getDetails().isVideoOffered();
            if (isVideo) {
                view.setVisibility(View.GONE);
                mainContainer.setBackgroundColor(Color.BLACK);
                addLocalView();
                if (call.getState() == CallState.ESTABLISHED) {
                    addRemoteView();
                }
                mySwitchCameraLLY.setVisibility(View.VISIBLE);
            } else {
                mySwitchCameraLLY.setVisibility(View.GONE);
            }

            mCallType.setText((isVideo ? " Twish Video Call" : "Twish Voice Call"));
            localVideo.setVisibility(!isVideo ? View.GONE : View.VISIBLE);
        }
    }

    private void removeVideoViews() {
        if (getSinchServiceInterface() == null) {
            return; // early
        }

        VideoController vc = getSinchServiceInterface().getVideoController();
        if (vc != null) {
            remoteVideo.removeView(vc.getRemoteView());

            localVideo.removeView(vc.getLocalView());
            mLocalVideoViewAdded = false;
            mRemoteVideoViewAdded = false;
        }
    }

    private void addLocalView() {
        if (mLocalVideoViewAdded || getSinchServiceInterface() == null) {
            return; //early
        }
        final VideoController vc = getSinchServiceInterface().getVideoController();
        if (vc != null) {

            localVideo.addView(vc.getLocalView());
            switchVideo.setOnClickListener(v -> vc.toggleCaptureDevicePosition());
            mLocalVideoViewAdded = true;
            localVideo.setOnTouchListener(new OnDragTouchListener(localVideo, remoteVideo));
        }
    }

    private void addRemoteView() {
        if (mRemoteVideoViewAdded || getSinchServiceInterface() == null) {
            return; //early
        }
        final VideoController vc = getSinchServiceInterface().getVideoController();
        if (vc != null) {
            vc.setResizeBehaviour(VideoScalingType.ASPECT_FILL);
            remoteVideo.addView(vc.getRemoteView());
            mRemoteVideoViewAdded = true;
        }
    }

    private void startAlphaAnimation() {
        AlphaAnimation animation1 = new AlphaAnimation(alphaInvisible ? 0.0f : 1.0f, alphaInvisible ? 1.0f : 0.0f);
        animation1.setDuration(500);
        animation1.setStartOffset(25);
        animation1.setFillAfter(true);

        //   myTxtCalling.startAnimation(animation1);
        //    userImage2.startAnimation(animation1);
        //  mCallerName.startAnimation(animation1);
        //   mCallState.startAnimation(animation1);
        //  mCallDuration.startAnimation(animation1);
        //   bottomButtons.startAnimation(animation1);

        alphaInvisible = !alphaInvisible;
    }

    private void enableSpeaker(boolean enable) {
        AudioController audioController = getSinchServiceInterface().getAudioController();
        if (enable)
            audioController.enableSpeaker();
        else
            audioController.disableSpeaker();
        switchVolume.setImageDrawable(ContextCompat.getDrawable(this, isSpeaker ? R.drawable.ic_baseline_volume_up_24 : R.drawable.ic_baseline_volume_off_24));
    }

    private void setMuteUnmute() {
        AudioController audioController = getSinchServiceInterface().getAudioController();
        if (isMute) {
            audioController.mute();
        } else {
            audioController.unmute();
        }
        switchMic.setImageDrawable(ContextCompat.getDrawable(this, isMute ? R.drawable.ic_baseline_mic_off_24 : R.drawable.ic_baseline_mic_24));
    }


    private class SinchCallListener implements VideoCallListener {
        @Override
        public void onCallEnded(Call call) {
            CallEndCause cause = call.getDetails().getEndCause();
            Log.d(TAG, "Call ended. Reason: " + cause.toString());
            mAudioPlayer.stopProgressTone();
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            String endMsg = call.getDetails().toString();
            Log.d(TAG, "Call ended. Reason: " + endMsg);
            endCall();
        }

        @Override
        public void onCallEstablished(Call call) {
            Log.d(TAG, "Call established");
            mAudioPlayer.stopProgressTone();
            mCallState.setVisibility(View.GONE);
            mCallDuration.setVisibility(View.VISIBLE);

            if (isVideo) {
                mCallType.setVisibility(View.GONE);
                mCallerName.setVisibility(View.GONE);
                userImage.setVisibility(View.GONE);
            }

            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
            isSpeaker = call.getDetails().isVideoOffered();
            enableSpeaker(isSpeaker);
            isMute = false;

            switchVideo.setClickable(call.getDetails().isVideoOffered());
            if (call.getDetails().isVideoOffered()) {
                mySwitchCameraLLY.setVisibility(View.VISIBLE);
            } else {
                mySwitchCameraLLY.setVisibility(View.GONE);
            }
            switchVideo.setAlpha(call.getDetails().isVideoOffered() ? 1f : 0.4f);
            Log.d(TAG, "Call offered video: " + call.getDetails().isVideoOffered());
        }

        @Override
        public void onCallProgressing(Call call) {
            Log.d(TAG, "Call progressing");
            mAudioPlayer.playProgressTone();
            mCallState.setText(call.getState().toString());
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
            // Send a push through your push provider here, e.g. GCM
        }

        @Override
        public void onVideoTrackAdded(Call call) {
            Log.d(TAG, "Video track added");
            addRemoteView();
        }

        @Override
        public void onVideoTrackPaused(Call call) {

        }

        @Override
        public void onVideoTrackResumed(Call call) {

        }

    }

    public static Intent newIntent(Context context, String callId, String profile) {
        Intent intent = new Intent(context, CallScreenActivity.class);
        intent.putExtra(USER_PROFILE_PICTURE, profile);
        intent.putExtra(SinchService.CALL_ID, callId);
        return intent;
    }

}
