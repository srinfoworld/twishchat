package com.app.twishchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import java.util.Objects;


public class SignupActivity extends AppCompatActivity {

    private EditText inputemail;
    private EditText inputpassword;
    Button sign_up;

    private FirebaseAuth auth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        inputemail = findViewById(R.id.email);
        inputpassword = findViewById(R.id.password);
        sign_up = findViewById(R.id.sign_up);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        auth = FirebaseAuth.getInstance();

        sign_up.setOnClickListener(v -> {
            progressDialog.show();
            if (inputemail.length() == 0) {
                progressDialog.dismiss();
                inputemail.setError("Enter Email");
            } else if (inputpassword.length() == 0) {
                progressDialog.dismiss();
                inputpassword.setError("Enter Password");
            } else {
                SIGNUP();
            }
        });

    }

    private void SIGNUP() {

        final String email = inputemail.getText().toString();
        final String password = inputpassword.getText().toString();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignupActivity.this, task -> {

                    if (!task.isSuccessful()) {
                        if (password.length() < 6) {
                            progressDialog.dismiss();
                            inputpassword.setError("Password too short, enter minimum 6 characters!");
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(SignupActivity.this, "" + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progressDialog.dismiss();
                        startActivity(new Intent(SignupActivity.this,AfterLoginProfileActivity.class));
                        ((ResultReceiver) Objects.requireNonNull(getIntent().getParcelableExtra("finish_all"))).send(1, new Bundle());
                        finish();
                    }
                });
    }
}
