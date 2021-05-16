package com.example.accidentdetectionsystem.ui.ResetPassword;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.accidentdetectionsystem.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

public class ResetPasswordFragment extends Fragment {

    private EditText newPass, conNewPass, currentPass;
    private Button resetBtn;

    private FirebaseAuth fAuth;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_resetpassword, container, false);
        
        Initialize(root);
        
        Validate();
        
        return root;
    }

    private void Validate() {
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentPassword = currentPass.getText().toString().trim();
                String newPassword = newPass.getText().toString().trim();
                String confirmPassword = conNewPass.getText().toString().trim();
                
                if(TextUtils.isEmpty(newPassword)){
                    newPass.setError("new password required");
                    return;
                }

                if(TextUtils.isEmpty(currentPassword)){
                    currentPass.setError("current password required");
                    return;
                }

                if (newPassword.length() < 6) {
                    newPass.setError("Password must have at least 6 characters");
                    return;
                }

                if(TextUtils.isEmpty(confirmPassword)){
                    conNewPass.setError("confirm password required");
                    return;
                }

                if(!newPassword.equals(confirmPassword)){
                    newPass.setError("password must match");
                    return;
                }

                // update
                UpdatePassword(currentPassword, newPassword);

            }
        });
    }

    private void UpdatePassword(String currentPassword, String newPassword) {
        FirebaseUser user = fAuth.getCurrentUser();

        // re-authenticate user
        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
        user.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // update password
                        user.updatePassword(newPassword)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(getContext(), "Password changed!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull @NotNull Exception e) {
                                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    Toast.makeText(getContext(), "Current password is not Matching!!", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void Initialize(View v) {
        currentPass = (EditText) v.findViewById(R.id.id_resert_current_pass);
        newPass = (EditText) v.findViewById(R.id.id_reset_new_pass);
        conNewPass = (EditText) v.findViewById(R.id.id_reset_con_new_password);
        
        resetBtn = (Button) v.findViewById(R.id.id_reset_btn);

        fAuth = FirebaseAuth.getInstance();
    }
}