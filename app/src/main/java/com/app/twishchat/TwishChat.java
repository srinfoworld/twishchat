package com.app.twishchat;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.app.twishchat.agvideocall.model.AGEventHandler;
import com.app.twishchat.agvideocall.model.ConstantApp;
import com.app.twishchat.agvideocall.model.CurrentUserSettings;
import com.app.twishchat.agvideocall.model.EngineConfig;
import com.app.twishchat.agvideocall.model.MyEngineEventHandler;
import com.app.twishchat.agvideocall.ui.CallActivity;
import com.app.twishchat.agvideocall.ui.IncomingCallActivity;
import com.app.twishchat.util.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.HashMap;

import io.agora.rtc.Constants;
import io.agora.rtc.RtcEngine;

import static com.app.twishchat.util.Helper.RECEIVING_REF;
import static com.app.twishchat.util.Helper.currentuserID;
import static com.app.twishchat.util.Helper.getUserRef;
import static com.app.twishchat.util.Helper.getUsersName;

public class TwishChat extends Application implements LifecycleObserver {

    private CurrentUserSettings mVideoSettings = new CurrentUserSettings();

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private RtcEngine mRtcEngine;
    private EngineConfig mConfig;
    private MyEngineEventHandler mEventHandler;

    public RtcEngine rtcEngine() {
        return mRtcEngine;
    }

    public EngineConfig config() {
        return mConfig;
    }

    public CurrentUserSettings userSettings() {
        return mVideoSettings;
    }

    public void addEventHandler(AGEventHandler handler) {
        mEventHandler.addEventHandler(handler);
    }

    public void remoteEventHandler(AGEventHandler handler) {
        mEventHandler.removeEventHandler(handler);
    }

    private void createRtcEngine() {
        Context context = getApplicationContext();
        String appId = context.getString(R.string.agora_app_id);
        if (TextUtils.isEmpty(appId)) {
            throw new RuntimeException("NEED TO use your App ID, get your own ID at https://dashboard.agora.io/");
        }

        mEventHandler = new MyEngineEventHandler();
        try {
            // Creates an RtcEngine instance
            mRtcEngine = RtcEngine.create(context, appId, mEventHandler);
        } catch (Exception e) {
            log.error(Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }

        /*
          Sets the channel profile of the Agora RtcEngine.
          The Agora RtcEngine differentiates channel profiles and applies different optimization
          algorithms accordingly. For example, it prioritizes smoothness and low latency for a
          video call, and prioritizes video quality for a video broadcast.
         */
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);
        // Enables the video module.
        mRtcEngine.enableVideo();
        /*
          Enables the onAudioVolumeIndication callback at a set time interval to report on which
          users are speaking and the speakers' volume.
          Once this method is enabled, the SDK returns the volume indication in the
          onAudioVolumeIndication callback at the set time interval, regardless of whether any user
          is speaking in the channel.
         */
        mRtcEngine.enableAudioVolumeIndication(200, 3, false);

        mConfig = new EngineConfig();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    //FIREBASE
    FirebaseAuth auth;
    String currentuserID;

    @Override
    public void onCreate() {
        super.onCreate();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        auth = FirebaseAuth.getInstance();
        createRtcEngine();
        
        if (!Util.verifyConnection(this)) {
            Util.initToast(this, "Internet not available");
        }

        if (auth.getCurrentUser() != null){
            startCallEngine();
        }
    }

    private void startCallEngine() {
        currentuserID = auth.getCurrentUser().getUid();
        getUserRef().child(currentuserID).child(RECEIVING_REF).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DataSnapshot dataSnapshot = snapshot.getChildren().iterator().next();
                    String key = dataSnapshot.getKey();
                    assert key != null;
                    ConstantApp.ACTION_KEY_RECEIVER_ID = key;
                    ConstantApp.ACTION_KEY_ROOM_ID  = snapshot.child(key).child("id").getValue().toString();
                    ConstantApp.RECEIVE_ACCESS_TOKEN = snapshot.child(key).child("token").getValue().toString();

                    Intent i = new Intent(TwishChat.this, IncomingCallActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void appInResumeState() {
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            currentuserID = auth.getCurrentUser().getUid();
            if (!TextUtils.isEmpty(getUsersName(currentuserID))) {
                updateOnlineStatus();
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void appInPauseState() {
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            currentuserID = auth.getCurrentUser().getUid();
            if (!TextUtils.isEmpty(getUsersName(currentuserID))) {
                updateOfflineStatus();
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void appInDestroyState() {
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            currentuserID = auth.getCurrentUser().getUid();
            if (!TextUtils.isEmpty(getUsersName(currentuserID))) {
                updateOfflineStatus();
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void appIsStopped() {
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            currentuserID = auth.getCurrentUser().getUid();
            if (!TextUtils.isEmpty(getUsersName(currentuserID))) {
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
