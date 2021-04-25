package com.example.accidentdetectionsystem.ui.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.accidentdetectionsystem.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.jar.Attributes;

public class ProfileFragment extends Fragment {

    private Spinner spinner;
    private TextView userName, userEmail, userPhoneNumber, landMark, city, state, pinCode, helpPhoneNumber1, helpPhoneNumber2, helpPhoneNumber3;
    private Button saveProfile;

    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userId;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        Initialize(root);

        DisplaySpinnerOptions();

        DisplayProfileDetails();

        UpdateProfile();

        return root;
    }

    private void UpdateProfile() {
        saveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String Name = userName.getText().toString().trim();
                String Email = userEmail.getText().toString().trim();
                String Phone = userPhoneNumber.getText().toString().trim();
                String BloodGroup = spinner.getSelectedItem().toString();
                String LandMark = landMark.getText().toString().trim();
                String City = city.getText().toString().trim();
                String State = state.getText().toString().trim();
                String PinCode = pinCode.getText().toString().trim();
                String HelpPhone1 = helpPhoneNumber1.getText().toString().trim();
                String HelpPhone2 = helpPhoneNumber2.getText().toString().trim();
                String HelpPhone3 = helpPhoneNumber3.getText().toString().trim();

                // validate data
                if (TextUtils.isEmpty(Name)) userName.setError("Name is required");
                if (TextUtils.isEmpty(Email)) userEmail.setError("Email is required");
                if (TextUtils.isEmpty(Phone)) userPhoneNumber.setError("Phone Number is required");
                if (TextUtils.isEmpty(LandMark)) landMark.setError("Land mark is required");
                if (TextUtils.isEmpty(City)) city.setError("City is required");
                if (TextUtils.isEmpty(State)) state.setError("State is required");
                if (TextUtils.isEmpty(PinCode)) pinCode.setError("PIN CODE is required");
                if (TextUtils.isEmpty(HelpPhone1)) helpPhoneNumber1.setError("Phone Number Code is required");
                if (TextUtils.isEmpty(HelpPhone2)) helpPhoneNumber2.setError("Phone Number Code is required");
                if (TextUtils.isEmpty(HelpPhone3)) helpPhoneNumber3.setError("Phone Number Code is required");

                if(BloodGroup.equals("Blood Group")){
                    ((TextView)spinner.getSelectedView()).setError("None Selected");
                }

                // update here
                final DocumentReference documentReference = fStore.collection("users").document(userId);

                fStore.runTransaction(new Transaction.Function<Void>() {
                    @Override
                    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                        DocumentSnapshot snapshot = transaction.get(documentReference);

                        transaction.update(documentReference, "name", Name);
                        transaction.update(documentReference, "email", Email);
                        transaction.update(documentReference, "phone", Phone);
                        transaction.update(documentReference, "blood group", BloodGroup);
                        transaction.update(documentReference, "land mark", LandMark);
                        transaction.update(documentReference, "city", City);
                        transaction.update(documentReference, "state", State);
                        transaction.update(documentReference, "pin code", PinCode);
                        transaction.update(documentReference, "help phone1", HelpPhone1);
                        transaction.update(documentReference, "help phone2", HelpPhone2);
                        transaction.update(documentReference, "help phone3", HelpPhone3);

                        return null;
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Data Saved Successfully!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Failed to save data", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void Initialize(View v) {

        userName = (TextView) v.findViewById(R.id.id_profile_username);
        userEmail = (TextView) v.findViewById(R.id.id_profile_userEmail);
        userPhoneNumber = (TextView) v.findViewById(R.id.id_profile_userPhone);
        landMark = (TextView) v.findViewById(R.id.id_profile_landMark);
        city = (TextView) v.findViewById(R.id.id_profile_city);
        state = (TextView) v.findViewById(R.id.id_profile_state);
        pinCode = (TextView) v.findViewById(R.id.id_profile_pinCode);
        helpPhoneNumber1 = (TextView) v.findViewById(R.id.id_profile_helpPhone1);
        helpPhoneNumber2 = (TextView) v.findViewById(R.id.id_profile_helpPhone2);
        helpPhoneNumber3 = (TextView) v.findViewById(R.id.id_profile_helpPhone3);
        saveProfile = (Button) v.findViewById(R.id.id_profile_saveBtn);

        spinner = (Spinner) v.findViewById(R.id.dynamic_spinner);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();
    }

    private void  DisplayProfileDetails() {
        final DocumentReference documentReference = fStore.collection("users").document(userId);
        documentReference.addSnapshotListener(getActivity(), new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value.getString("blood group").equals("") ||
                        value.getString("land mark").equals("") ||
                        value.getString("city").equals("") ||
                        value.getString("state").equals("") ||
                        value.getString("pin code").equals("") ||
                        value.getString("help phone1").equals("") ||
                        value.getString("help phone2").equals("") ||
                        value.getString("help phone3").equals("")){
                    userName.setText(value.getString("name"));
                    userEmail.setText(value.getString("email"));
                    userPhoneNumber.setText(value.getString("phone"));
                }else{
                    // display all
                    userName.setText(value.getString("name"));
                    userEmail.setText(value.getString("email"));
                    userPhoneNumber.setText(value.getString("phone"));
                    spinner.setSelection(getSpinnerIndex(spinner, value.getString("blood group")));
                    landMark.setText(value.getString("land mark"));
                    city.setText(value.getString("city"));
                    state.setText(value.getString("state"));
                    pinCode.setText(value.getString("pin code"));
                    helpPhoneNumber1.setText(value.getString("help phone1"));
                    helpPhoneNumber2.setText(value.getString("help phone2"));
                    helpPhoneNumber3.setText(value.getString("help phone3"));

                }
            }
        });
    }

    private int getSpinnerIndex(Spinner spinner, String value) {
        for(int i=0; i<spinner.getCount(); i++){
            if(spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value))
                return i;
        }
        return 0;
    }

    private void DisplaySpinnerOptions() {
        String[] bloodGroups = new String[] {"A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-", "Blood Group"};

        HintAdapter arrayAdapter = new HintAdapter(getContext(), R.layout.spinner_item, bloodGroups);
        spinner.setAdapter(arrayAdapter);

        spinner.setSelection(arrayAdapter.getCount());

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextSize(18);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

}