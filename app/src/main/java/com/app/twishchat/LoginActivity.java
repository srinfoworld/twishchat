package com.app.twishchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.app.twishchat.util.Util;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.Objects;
import static com.app.twishchat.util.Helper.currentuserID;
import static com.app.twishchat.util.Helper.getUserRef;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    //Constants
    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 9001;

    //UI
    FloatingActionButton mSignInButton;


    //Firebase and GoogleApiClient
    GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mFirebaseAuth;

    private EditText inputemail, inputpassword;
    Button login, forgot, register;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (!Util.verifyConnection(this)) {
            Util.initToast(this, "Internet not available");
            finish();
        }

        inputemail = findViewById(R.id.email);
        inputpassword = findViewById(R.id.password);
        login = findViewById(R.id.login);
        forgot = findViewById(R.id.forgot_password);
        register = findViewById(R.id.register);
        mSignInButton = findViewById(R.id.sign_in_button);

        mSignInButton.setOnClickListener(this);
        login.setOnClickListener(this);
        register.setOnClickListener(this);
        forgot.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        // Initialize FirebaseAuth
        mFirebaseAuth = FirebaseAuth.getInstance();

    }

    private void FORGOT() {
        final String email = inputemail.getText().toString();
        mFirebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.hide();

                });

    }

    private void LOGIN() {
        final String email = inputemail.getText().toString();
        final String password = inputpassword.getText().toString();

        mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, task -> {

                    if (!task.isSuccessful()) {
                        if (password.length() < 6) {
                            inputpassword.setError("Password too short, enter minimum 6 characters!");
                            progressDialog.hide();
                        } else {
                            progressDialog.hide();
                            Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        loginUser();
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                assert account != null;
                firebaseAuthWithGoogle(account);

            } catch (ApiException e) {
                progressDialog.dismiss();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.login:

                progressDialog.show();
                if (inputemail.length() == 0) {
                    inputemail.setError("Enter Email");
                    progressDialog.hide();
                } else if (inputpassword.length() == 0) {
                    inputpassword.setError("Enter Password");
                    progressDialog.hide();
                } else {
                    LOGIN();
                }
                break;

            case R.id.register:

                Intent i = new Intent(LoginActivity.this, SignupActivity.class);
                i.putExtra("finish_all", new ResultReceiver(null) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        LoginActivity.this.finish();
                    }
                });
                startActivityForResult(i, 1);
                break;

            case R.id.forgot_password:

                progressDialog.show();
                if (inputemail.length() == 0) {
                    inputemail.setError("Enter Email");
                    progressDialog.hide();
                } else {
                    FORGOT();
                }
                break;

            default:
        }
    }

    private void signIn() {
        progressDialog.show();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGooogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "signInWithCredential", task.getException());
                        Util.initToast(LoginActivity.this, "Authentication failed");
                    } else {

                        loginUser();
                    }
                });
    }

    private void loginUser() {
        currentuserID = Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).getUid();
        getUserRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(currentuserID)) {
                    progressDialog.hide();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    progressDialog.hide();
                    startActivity(new Intent(LoginActivity.this, AfterLoginProfileActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
