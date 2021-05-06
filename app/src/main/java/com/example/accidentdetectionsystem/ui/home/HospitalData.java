package com.example.accidentdetectionsystem.ui.home;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class HospitalData {

    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    Map<String, Map<String, String>> hospitals = new HashMap<>();
    HospitalCallback hospitalCallback;

    HospitalData(){
        // empty constructor
    }


    public void setHospitalCallback(HospitalCallback hospitalCallback) {
        this.hospitalCallback = hospitalCallback;
    }

    void getHospitalData(){
        CollectionReference collectionReference = fStore.collection("hospital Details");
        collectionReference.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(QueryDocumentSnapshot queryDocumentSnapshot: queryDocumentSnapshots){
//                            Log.d("HOSPITAL DATA-->",queryDocumentSnapshot.toString());
                            Map<String, String> map = new HashMap<>();
                            map.put("h_name", queryDocumentSnapshot.getString("h_name"));
                            map.put("h_email", queryDocumentSnapshot.getString("h_email"));
                            map.put("h_phone", queryDocumentSnapshot.getString("h_phone"));
                            map.put("h_road", queryDocumentSnapshot.getString("h_road"));
                            map.put("h_area", queryDocumentSnapshot.getString("h_area"));
                            map.put("h_city", queryDocumentSnapshot.getString("h_city"));
                            map.put("h_state", queryDocumentSnapshot.getString("h_state"));
                            map.put("h_pincode", queryDocumentSnapshot.getString("h_pincode"));

                            hospitals.put(queryDocumentSnapshot.getId(), map);
                        }
//                        hospital.setHospitalDetails("testing bvc.......");
//                        hospitalCallback.DisplayCallbackData("Kushal Testing Callback");
//                        Log.d("MAP GET-->" , hospitals.toString());
                        hospitalCallback.DisplayHospitalData(hospitals);
                    }
                });
    }

    interface HospitalCallback{
        public void DisplayHospitalData(Map<String, Map<String, String>> map);
    }
}
