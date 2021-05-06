package com.example.accidentdetectionsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login_Activity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST = 1000;
    Button loginButton;
    TextView goToRegister;
    EditText email, password;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_);

        // check for all permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.SEND_SMS,
                            Manifest.permission.RECEIVE_SMS
                    }, MY_PERMISSIONS_REQUEST);

                }

        // initialize variables
        InitializeFields();

        // check user already logged in or not
        CheckUserSession();

        // validate and login
        Validate();

        // If new user, go to registration page
        GoToRegister();

    }

    private void CheckUserSession() {
        if(fAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST:

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    finish();
                }
                return;
        }
    }

    private void InitializeFields() {
        loginButton = findViewById(R.id.id_login_btnLogin);
        goToRegister = (TextView) findViewById(R.id.tv_goToregister);
        email = findViewById(R.id.id_login_email);
        password = findViewById(R.id.id_login_password);

        fAuth = FirebaseAuth.getInstance();
    }

    private void Validate() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emails = email.getText().toString().trim();
                String passwords = password.getText().toString().trim();

                if (TextUtils.isEmpty(emails)) {
                    email.setError("Email is required");
                }
                if (TextUtils.isEmpty(passwords)) {
                    password.setError("password is required");
                    return;
                }
                if (passwords.length() < 6) {
                    password.setError("Password must have at least 6 characters");
                    return;
                }
//                progressbar.setVisibility(View.VISIBLE);


                fAuth.signInWithEmailAndPassword(emails, passwords).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(Login_Activity.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));

                        }else{
                            Toast.makeText(Login_Activity.this, "Error occurred"+ task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                            progressbar.setVisibility(View.GONE);
                        }

                    }
                });
            }
        });
    }

    private void GoToRegister() {
        goToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Register_Activity.class));
            }
        });
    }
}