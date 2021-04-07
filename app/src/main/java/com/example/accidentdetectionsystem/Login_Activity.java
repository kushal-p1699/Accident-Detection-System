package com.example.accidentdetectionsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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
    Button mloginbutton;
    TextView loginbutton;
    EditText fullname, email, password, confirmpassword, phone;
    ProgressBar progressbar;
    FirebaseAuth fauth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_);
        mloginbutton = findViewById(R.id.regsiterbutton);
        loginbutton = findViewById(R.id.createtext);

        fullname = findViewById(R.id.Fullname);
        email = findViewById(R.id.email);
        password = findViewById(R.id.Password);
        confirmpassword = findViewById(R.id.confirmPassword);
        phone = findViewById(R.id.Phone);
        fauth = FirebaseAuth.getInstance();
        progressbar = findViewById(R.id.progressbar);
        mloginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emails = email.getText().toString().trim();
                String passwords = password.getText().toString().trim();

                if (TextUtils.isEmpty(emails)) {
                    email.setError("Email is required");
                }
                if (TextUtils.isEmpty(passwords)) {
                    email.setError("password is required");
                    return;
                }
                if (passwords.length() < 6) {
                    password.setError("Password must have atleast 6 characters");
                    return;
                }
                progressbar.setVisibility(View.VISIBLE);

                fauth.signInWithEmailAndPassword(emails, passwords).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(Login_Activity.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));

                        }else{
                            Toast.makeText(Login_Activity.this, "Error occured"+ task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressbar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
        loginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),Register_Activity.class));
            }
        });
    }
}