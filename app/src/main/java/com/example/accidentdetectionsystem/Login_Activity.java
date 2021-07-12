package com.example.accidentdetectionsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

public class Login_Activity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST = 1000;
    Button loginButton;
    TextView goToRegister, forgotPass;
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

        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText restMail = new EditText(v.getContext());
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("Reset Password ?");
                passwordResetDialog.setMessage("Enter your Email ID to receive the reset link");
                passwordResetDialog.setView(restMail);

                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String mail = restMail.getText().toString();
                        fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(Login_Activity.this, "Password reset link sent to your mail", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull @NotNull Exception e) {
                                Toast.makeText(Login_Activity.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.d("Login" , "error :"+e.getMessage());
                            }
                        });
                    }
                });

                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                passwordResetDialog.create().show();
            }
        });

    }

    private void CheckUserSession() {
        if(fAuth.getCurrentUser() != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
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
        forgotPass = findViewById(R.id.tv_forgotPass);

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
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);

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
                Intent intent = new Intent(getApplicationContext(), Register_Activity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }
}