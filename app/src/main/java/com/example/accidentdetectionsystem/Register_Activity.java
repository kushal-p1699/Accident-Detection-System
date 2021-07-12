package com.example.accidentdetectionsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register_Activity extends AppCompatActivity {
    private Button registerBtn;
    private TextView loginBtn;
    private EditText name, email, password, confirmPassword, phone;

    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_);

        InitializeFields();

        Validate();

        OnLogin();

    }

    private void InitializeFields() {
        registerBtn = findViewById(R.id.id_reg_btnRegister);
        loginBtn = findViewById(R.id.tv_gotoLogin);

        name = findViewById(R.id.id_reg_name);
        email = findViewById(R.id.id_reg_email);
        password = findViewById(R.id.id_reg_password);
        confirmPassword = findViewById(R.id.id_reg_confirmPassword);
        phone = findViewById(R.id.id_reg_Phone);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
    }

    private void OnLogin() {
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),Login_Activity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    private void Validate() {
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Email = email.getText().toString().trim();
                String Passwords = password.getText().toString().trim();
                String Name = name.getText().toString().trim();
                String Phone = phone.getText().toString().trim();

                if (TextUtils.isEmpty(Name)) {
                    name.setError("Name is required");
                }
                if (TextUtils.isEmpty(Phone)) {
                    phone.setError("Email is required");
                }
                if (TextUtils.isEmpty(Email)) {
                    email.setError("Email is required");
                }
                if (TextUtils.isEmpty(Passwords)) {
                    password.setError("password is required");
                    return;
                }
                if (Passwords.length() < 6) {
                    password.setError("Password must have at least 6 characters");
                    return;
                }

                fAuth.createUserWithEmailAndPassword(Email, Passwords).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(Register_Activity.this, "User created", Toast.LENGTH_SHORT).show();

                            // get current user ID
                            userId = fAuth.getCurrentUser().getUid();

                            // create user collection
                            final DocumentReference documentReference = fStore.collection("users").document(userId);
                            Map<String, Object> user = new HashMap<>();

                            // add registration details
                            // keep profile data null for now, and update it in profile

                            user.put("name", Name);
                            user.put("email", Email);
                            user.put("phone", Phone);
                            user.put("blood group", "");
                            user.put("land mark", "");
                            user.put("city", "");
                            user.put("state", "");
                            user.put("pin code", "");
                            user.put("help phone1", "");
                            user.put("help phone2", "");
                            user.put("help phone3", "");

                            // insert to database
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("OnSuccess", "data stored for the id --------->"+userId);
                                }
                            });

                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);

                        }else{
                            Toast.makeText(Register_Activity.this, "Error occured"+ task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}