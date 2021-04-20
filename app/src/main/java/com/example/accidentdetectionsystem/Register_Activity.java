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

public class Register_Activity extends AppCompatActivity {
    Button mregsiterbutton;
    TextView loginbutton;
    EditText fullname, email, password, confirmpassword, phone;
    ProgressBar progressbar;
    FirebaseAuth fauth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_);

        InitializeFields();

        Validate();

        OnLogin();

    }

    private void OnLogin() {
        loginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),Login_Activity.class));
            }
        });
    }

    private void Validate() {
        mregsiterbutton.setOnClickListener(new View.OnClickListener() {
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
                fauth.createUserWithEmailAndPassword(emails, passwords).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(Register_Activity.this, "User created", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));

                        }else{
                            Toast.makeText(Register_Activity.this, "Error occured"+ task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressbar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    }

    private void InitializeFields() {
        mregsiterbutton = findViewById(R.id.regsiterbutton);
        loginbutton = findViewById(R.id.id_goToRegister);

        fullname = findViewById(R.id.Fullname);
        email = findViewById(R.id.email);
        password = findViewById(R.id.Password);
        confirmpassword = findViewById(R.id.confirmPassword);
        phone = findViewById(R.id.Phone);
        fauth = FirebaseAuth.getInstance();
        progressbar = findViewById(R.id.progressbar);
    }
}