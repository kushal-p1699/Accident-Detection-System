package com.example.accidentdetectionsystem.ui.home;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.Navigation;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.example.accidentdetectionsystem.CLocation;
import com.example.accidentdetectionsystem.Login_Activity;
import com.example.accidentdetectionsystem.MainActivity;
import com.example.accidentdetectionsystem.MyReceiver;
import com.example.accidentdetectionsystem.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import android.view.View.OnClickListener;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Phaser;

public class HomeFragment extends Fragment implements SensorEventListener, LocationListener, HospitalData.HospitalCallback {

    private TextView gForceText, speedText, soundText, pressureText, alarmTimeLeftText;
    private Button startTrackingBtn, stopTrackingBtn, cancelAlarm;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private ImageView carRunningGif;
    private double lastUpdate = 0;

    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    String userId;

    MediaRecorder mediaRecorder;
    Thread runner;
    private static double mEMA = 0.0;
    static final private double EMA_FILTER = 0.6;
    private static final double AMP = 1;// Math.pow(1, 1);
    public static final int MY_PERMISSIONS_REQUEST = 10;

    public double _gForce;
    public double _speed;
    public double _pressure;
    public double _sound;

    final double threshold_gForce = 1.99;
    final double threshold_pressure = 1010.00;
    final double threshold_speed = 24.00;
    final double threshold_sound = 80.00;

    final double SVP = 1d;
    final double ET = 1d;
    final double MP = 1d;

    Dialog dialog;

    CountDownTimer countDownTimer;

    String address, latitude, longitude;

    final Handler handler = new Handler();
    DecimalFormat df = new DecimalFormat("00.00");

    private final  String url = "https://api.openweathermap.org/data/2.5/weather";
    private final String appId = "13a249338dd35185b7e593a1453cbf5e";

    final Runnable updater = new Runnable() {
        @Override
        public void run() {
            updateSoundText();
        };
    };

    String sms_from;
    String sms_body;

    public boolean isProcessRunning = false;
    HospitalData hospitalData;

    GifDrawable drawable;

    AlarmSound alarmSound;

    private int ALARM_DELAY = 30000;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        Initialize(root);
//        startTrackingBtn.setVisibility(View.VISIBLE);
        // display car running image
        DisplayCarRunningImage();

        // get all hospital data
        readHospitalData();

        // get all Drivers Details data
        readDriversData();

        startTrackingBtn.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {

                isProcessRunning = true;



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

                            Log.d("isNULL--------> ", "Yes its null!!");

                            // alert message to update profile
                            new AlertDialog.Builder(v.getContext())
                                    .setIcon(R.drawable.ic_baseline_warning_24)
                                    .setTitle("UPDATE PROFILE")
                                    .setMessage("Please update your profile, before tracking")
                                    .setPositiveButton("Go to Profile", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // navigate to profile page
                                            Navigation.findNavController(v).navigate(R.id.action_nav_home_to_nav_profile);
                                        }
                                    }).create().show();
                        }else{

                            // store Profile Data in SharedPref
                            Map<String, String> pData = new HashMap<>();

                            pData.put("p_name", value.getString("name"));
                            pData.put("p_email", value.getString("email"));
                            pData.put("p_phone", value.getString("phone"));
                            pData.put("blood_group", value.getString("blood group"));
                            pData.put("help phone1", value.getString("help phone1"));
                            pData.put("help phone2", value.getString("help phone3"));
                            pData.put("help phone3", value.getString("help phone3"));

                            StoreProfileDataToPref(pData);

//                            Log.d("Profile Data --->", LoadProfilePref().toString());


                            /** if profile is updated then, Start tracking **/
                            // start gif
                            DisplayCarRunningGif();
                            stopTrackingBtn.setVisibility(View.VISIBLE);
                            startTrackingBtn.setVisibility(View.INVISIBLE);

//                            Toast.makeText(getActivity(), "Tracking is started...", Toast.LENGTH_SHORT).show();

                            ProcessSensors();
                            onResume();

                            ProcessGPSSpeed();
                            ProcessSoundTest();
                            getWeatherDetails();


                            // continuously process data to detect accident
                            ProcessDataToDetectAccident();



                        }
                    }
                });
            }
        });

        // on stop tracking
        stopTrackingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("OnStop---> ", "working....");
                new Handler().post(new Runnable() {

                    @Override
                    public void run()
                    {
                        StopTrackingData();
                    }
                });
            }
        });

        // receive message
        readSMSData();

        Log.d("Hospitals Data --->", LoadHospitalSharedPerf().toString());
        Log.d("Drivers Data --->", LoadDriversSharedPref().toString());


        return root;
    }

    private void DisplayCarRunningImage() {
        Glide.with(this).clear(carRunningGif);
        Glide.with(this).load(R.drawable.car_running_image).into(carRunningGif);
    }

    private void DisplayCarRunningGif() {
        Glide.with(this).clear(carRunningGif);
        Glide.with(this).asGif().load(R.drawable.car_running_gif).into(carRunningGif);
    }

    private void StoreProfileDataToPref(Map<String, String> pData) {
        Gson gson = new Gson();
        String mapString = gson.toJson(pData);
        Log.d("KEYS IDS-->",mapString);

        SharedPreferences hSharedPref = getContext().getSharedPreferences("ProfileData", Context.MODE_PRIVATE);
        hSharedPref.edit().putString("ProfileMap", mapString).apply();
    }

    private Map<String, String> LoadProfilePref(){
        SharedPreferences hSharedPref = getContext().getSharedPreferences("ProfileData", Context.MODE_PRIVATE);
        Map<String, String> map = new HashMap<>();
        String storedHashMapString = hSharedPref.getString("ProfileMap", "oopsDintWork");

        Gson gson = new Gson();
        java.lang.reflect.Type type = new TypeToken<Map<String, String>>(){}.getType();
        map = gson.fromJson(storedHashMapString, type);

        return map;
    }

    private void readHospitalData() {
        // read callback here
        hospitalData = new HospitalData();
        hospitalData.getHospitalData();
        hospitalData.setHospitalCallback(this);
    }


    @Override
    public void DisplayHospitalData(Map<String, Map<String, String>> map) {
        // store map in sharedPreference
        StoreMapHospitalMapInSharedPref(map);

    }

    private void StoreMapHospitalMapInSharedPref(Map<String, Map<String, String>> map) {

        Gson gson = new Gson();
        String mapString = gson.toJson(map);
//        Log.d("KEYS IDS-->",mapString);

        SharedPreferences hSharedPref = getContext().getSharedPreferences("HospitalPref", Context.MODE_PRIVATE);
        hSharedPref.edit().putString("HospitalMap", mapString).apply();

    }

    private Map<String, Map<String, String>> LoadHospitalSharedPerf(){
        SharedPreferences hSharedPref = getContext().getSharedPreferences("HospitalPref", Context.MODE_PRIVATE);
        Map<String, Map<String, String>> map = new HashMap<>();
        String storedHashMapString = hSharedPref.getString("HospitalMap", "oopsDintWork");

        Gson gson = new Gson();
        java.lang.reflect.Type type = new TypeToken<Map<String, Map<String, String>>>(){}.getType();
        try{
            map = gson.fromJson(storedHashMapString, type);
        }catch (IllegalStateException | JsonSyntaxException exception){
            exception.printStackTrace();
        }


        return map;
    }

    private Map<String, Map<String, Map<String, String>>> LoadDriversSharedPref(){

        SharedPreferences hSharedPref = getContext().getSharedPreferences("DriversPref", Context.MODE_PRIVATE);
        Map<String, Map<String, Map<String, String>>> map = new HashMap<>();
        String storedHashMapString = hSharedPref.getString("DriversMap", "oopsDintWork");
        Log.d("LoADed DATA-->",storedHashMapString);


        Gson gson = new Gson();
        java.lang.reflect.Type type = new TypeToken<Map<String, Map<String, Map<String, String>>>>(){}.getType();
        try{
            map = gson.fromJson(storedHashMapString, type);
        }catch (IllegalStateException | JsonSyntaxException exception){
            exception.printStackTrace();
        }


        return map;

    }


    private void readDriversData() {
        Map<String, Map<String, Map<String, String>>> drivers = new HashMap<>();
        int size = LoadHospitalSharedPerf().size();
        final int[] count = {0};
        for(Map.Entry<String, Map<String, String>> itr : LoadHospitalSharedPerf().entrySet()){
//            Log.d("KEYS IDS-->",itr.getKey());

            CollectionReference collectionReference = fStore.collection("Drivers Details").document(itr.getKey()).collection("Drivers");
            collectionReference.get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {

                        Map<String, Map<String, String>> map = new HashMap<>();
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for(QueryDocumentSnapshot queryDocumentSnapshot: queryDocumentSnapshots){
//                            Log.d("DRIVERS DATA-->",queryDocumentSnapshot.toString());
                                Map<String, String> map1 = new HashMap<>();
                                map1.put("ambulance_number", queryDocumentSnapshot.getString("ambulance_number"));
                                map1.put("d_phone", queryDocumentSnapshot.getString("d_phone"));
                                map1.put("d_name",queryDocumentSnapshot.getString("d_name"));
                                map.put(queryDocumentSnapshot.getId(), map1);
                            }

                            count[0]++;
                            drivers.put(itr.getKey(), map);

                            if(count[0] == size){
                                // add details to shared preference
                                Gson gson = new Gson();
                                String mapString = gson.toJson(drivers);

                                SharedPreferences hSharedPref = getContext().getSharedPreferences("DriversPref", Context.MODE_PRIVATE);
                                hSharedPref.edit().putString("DriversMap", mapString).apply();

                            }
                        }
                    });
        }

        Log.d("DRIVERS DATA-->",drivers.toString());

    }

    private void readSMSData() {
        Bundle bundle = this.getArguments();
        if(bundle != null){
            sms_body = bundle.getString("sms_body");
            sms_from = bundle.getString("sms_from");
            Log.d("TESTING----->", sms_body+"/////");
            Log.d("TESTING----->", sms_from+"/////");

            if(sms_body.equalsIgnoreCase("yes")){
                SendCancelResponseToOthers(sms_from);
            }

        }else{
//            Log.d("TESTING----->", "BVC work ala please");
        }

    }

    private void SendCancelResponseToOthers(String sms_from) {

        String cancelResponseMessage = "Patient is picked up by other driver, Thank you";

        Map<String, Map<String, Map<String, String>>> d_map = LoadDriversSharedPref();

        String sendTo = "";

        for(Map.Entry<String, Map<String, Map<String, String>>> itr : d_map.entrySet()){
            for(Map.Entry<String, Map<String, String>> itr1 : itr.getValue().entrySet()){
                Log.d("Name: ", itr1.getValue().get("d_phone"));
                sendTo = itr1.getValue().get("d_phone");
                if(!("+91"+sendTo).equals(sms_from)){
                    sendMessage(sendTo, cancelResponseMessage);
                }else{
                    // store the patient data in shared ref
                    String driver_name = itr1.getValue().get("d_name");
                    String HospitalKey = itr.getKey();
                    String p_name = LoadProfilePref().get("p_name");
                    String p_number = LoadProfilePref().get("p_phone");
                    String p_blood_group = LoadProfilePref().get("blood_group");
                    String help_contact = LoadProfilePref().get("help phone1")+" "+LoadProfilePref().get("help phone2")+" "+LoadProfilePref().get("help phone3");

                    SharedPreferences pref = getActivity().getSharedPreferences("LocationPref", Context.MODE_PRIVATE);
                    String gps_location = pref.getString("location", "nothing");
                    String AccidentDate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

                    Map<String, String> map = new HashMap<>();
                    map.put("p_name", p_name);
                    map.put("p_number", p_number);
                    map.put("driver_name", driver_name);
                    map.put("p_blood_group", p_blood_group);
                    map.put("help_contact", help_contact);
                    map.put("gps_location", gps_location);
                    map.put("AccidentDate", AccidentDate);



                    // store it into accident detected table
                    DocumentReference documentReference = fStore.collection("accident detected").document(HospitalKey);
                    documentReference.set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d("Detected Data", "detected data stored --------->"+HospitalKey);
                        }
                    });
                }
            }
        }
    }

    private void Initialize(View v) {
        gForceText = (TextView) v.findViewById(R.id.id_gforce);
        speedText = (TextView) v.findViewById(R.id.id_speed);
        pressureText = (TextView) v.findViewById(R.id.id_pressure);
        soundText = (TextView) v.findViewById(R.id.id_sound);

        startTrackingBtn = (Button) v.findViewById(R.id.id_startTrackingBtn);
        stopTrackingBtn = (Button) v.findViewById(R.id.id_stopTrackingBtn);

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();

        carRunningGif = (ImageView) v.findViewById(R.id.id_car_running_gif);

        dialog = new Dialog(getContext());

        alarmSound = new AlarmSound(getContext());

    }

    private void ProcessDataToDetectAccident() {

        if(isDetected(_gForce, _pressure, _speed, _sound, SVP, ET, MP)){
            Log.d("ALERT-->", "Accident Detected!!!");

            // alert alarm to the driver
            OpenAlarmDialog();


            return;

        }else{
            Log.d("ALERT-->", "DETECTING....");
        }

        if(isProcessRunning)
            refresh(1000);
    }

    private void OpenAlarmDialog() {

        // play alarm audio
        alarmSound.Play();

//        StopTrackingData();

        DisplayCarRunningImage();
        dialog.setContentView(R.layout.alarm_dialog_layout);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        cancelAlarm = dialog.findViewById(R.id.id_cancelAlarmBtn);
        alarmTimeLeftText = dialog.findViewById(R.id.id_alarm_time_left);
        boolean[] isCanceled = {false};

        // close dialog when cancel button is clicked
        cancelAlarm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                // stop alarm audio
                alarmSound.Stop();

                isCanceled[0] = true;
                CancelAlarmAlert();
                countDownTimer.cancel();

            }
        });

        // close dialog after 10sec send Location to Hospital
        if(!isCanceled[0]){
            countDownTimer = new CountDownTimer(ALARM_DELAY, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    alarmTimeLeftText.setText((millisUntilFinished / 1000) +" seconds");
                }

                @Override
                public void onFinish() {

                    // stop alarm audio
                    alarmSound.Stop();

//                    alarmTimeLeftText.setText("Accident is informed to Hospitals \n Waiting for response...");
//                    left_tv.setVisibility(View.GONE);


                    String gpsLink = "https://www.google.com/maps/search/?api=1&query="+latitude+","+longitude;

                    // store gps location
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("LocationPref", Context.MODE_PRIVATE).edit();
                    editor.putString("location", gpsLink);
                    editor.putString("address", address);
                    editor.apply();


                    String sendTo = "";
                    String name = "";
                    Map<String, String> p_map = LoadProfilePref();
                    Map<String, Map<String, Map<String, String>>> d_map = LoadDriversSharedPref();

                    for(Map.Entry<String, Map<String, Map<String, String>>> itr : d_map.entrySet()){
                        for(Map.Entry<String, Map<String, String>> itr1 : itr.getValue().entrySet()){
                          Log.d("Name: ", itr1.getValue().get("d_phone"));
                            sendTo = itr1.getValue().get("d_phone");
                            name = itr1.getValue().get("d_name");
                            String smsBody = "Hey, "+name+"\n\n"+"There is an emergency!!"+"\n"+"Person with name "+p_map.get("p_name")+" met with an accident"+"\n\n"
                                    +"Blood Group: "+p_map.get("blood_group")+"\n"+"Family Contact Number: "+"\n"
                                    +p_map.get("help phone1")+"\n"

                                    +"\n"+"Accident    +p_map.get(\"help phone2\")+\"\\n\"\n" +
                                    "                                    +p_map.get(\"help phone3\")+\"\\n\" Spot: "+address+"\n\n"+"Location: "+gpsLink+"\n\n"
                                    +"If you're available to pick him up, Please replay 'YES' to this message"+"\n";
                            sendMessage(sendTo, smsBody);
                        }
                    }

                    Toast.makeText(getContext(), "Location sent to hospital", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    StopTrackingData();
                }
            }.start();
        }

//        if(isProcessRunning)
            dialog.show();
    }

    private void sendMessage(String sendTo, String smsBody){
        SmsManager smsManager = SmsManager.getDefault();
        ArrayList<String> smsMessageParts = smsManager.divideMessage(smsBody);
        Log.d("MESSAGE TESTING ---->", "sentTo  :"+sendTo+" body: "+smsBody);
        smsManager.sendMultipartTextMessage(sendTo, null, smsMessageParts, null, null);
    }

    private void StopTrackingData() {
        isProcessRunning = false;
        new Handler().post(new Runnable() {
            @Override
            public void run()
            {
                Intent intent = getActivity().getIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                getActivity().overridePendingTransition(0, 0);
                getActivity().finish();

                getActivity().overridePendingTransition(0, 0);
                startActivity(intent);
            }
        });
    }

    private void CancelAlarmAlert() {
        StopTrackingData();
        dialog.dismiss();

    }

    private boolean isDetected(double gForce, double pressure, double speed, double sound, double SVP, double ET, double MP) {
        boolean b = (gForce / threshold_gForce) + (sound / threshold_sound) + (pressure / threshold_pressure) >= 1;
        if(b && speed >= threshold_speed)
            return true;
        else if((gForce / threshold_gForce) + (sound / threshold_sound) + (pressure / threshold_pressure) + (SVP / 2.06) >= 3)
            return true;
        else if(b && ET < MP)
            return true;
        else
            return false;
    }

    private void refresh(int ms){
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ProcessDataToDetectAccident();
            }
        };

        handler.postDelayed(runnable, ms);
    }

    private void ProcessSensors() {
        /** Get an instance of the sensor service, and use that to get an instance of a particular sensor **/
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @SuppressLint("MissingPermission")
    private void ProcessGPSSpeed() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return;
//            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) this);
//            Toast.makeText(getActivity(), "waiting for gps connection!", Toast.LENGTH_SHORT).show();
        }
    }

    private void ProcessSoundTest() {
        if(runner == null) {
            runner = new Thread() {
                public void run() {
                    while (runner != null){
                        try{
                            Thread.sleep(1000);
                            Log.i("Noise", "Tock");
                        }catch (InterruptedException e){};
                        handler.post(updater);
                    }
                }
            };
            runner.start();
            Log.d("Noise", "start runner()");
        }
    }

    private void updateSpeed(CLocation location){
        float nCurrentSpeed = 0f;

        if(location != null) {
            location.setUseMetricUnits(this.useMetricUnits());
            nCurrentSpeed = location.getSpeed();
        }

        Formatter fmt = new Formatter(new StringBuilder());
        fmt.format(Locale.US, "%5.1f", nCurrentSpeed);
        String strCurrentSpeed = fmt.toString();
        strCurrentSpeed = strCurrentSpeed.replace(" ", "0");

        if(this.useMetricUnits()) {
            speedText.setText(strCurrentSpeed);
            _speed = Double.valueOf(strCurrentSpeed);
        }
    }

    private boolean useMetricUnits() {
        return  true;
    }

    @Override
    public void onResume() {
        super.onResume();

        // start recording
        startRecorder();

        // register sensor listeners
        if(isAvailable(accelerometerSensor))
            sensorManager.registerListener(this, accelerometerSensor, sensorManager.SENSOR_DELAY_NORMAL);
//        else
//             DisplayNiL(gForceText);

    }

    @Override
    public void onPause() {
        super.onPause();

        // stop recording
        stopRecorder();

        // unregister sensor listener
        sensorManager.unregisterListener(this);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                processAccelerometerSensor(event.values[0], event.values[1], event.values[2]);
                break;
        }
    }

    private void processAccelerometerSensor(double xValue, double yValue, double zValue) {

        double curTime = System.currentTimeMillis();

        if ((curTime - lastUpdate) > 100) {
            double diffTime = (curTime - lastUpdate);
            lastUpdate = curTime;

            //this will give you the accelerometer values
            // index 0 for x axis, 1 for y axis and, z for z axis
            //accelerometerText.setText(event.values[0]+"\n"+event.values[1]+"\n"+event.values[2])
            double gForce = Math.sqrt((xValue * xValue) + (yValue * yValue) + (zValue * zValue));

            // convert m/sec sq to g
            gForce = (0.10197 * gForce) / 1;

            // display accelerometer value in textView
            DecimalFormat df1 = new DecimalFormat("00.00");
            gForceText.setText(df1.format(gForce)+"");
            _gForce = gForce;
        }
    }

    private boolean isAvailable(Sensor sensor) {
        if(sensor == null)
            return false;
        return true;
    }

    // sound Testing snippets
    private void startRecorder() {
        if(mediaRecorder == null) {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile("/dev/null");

            try
            {
                mediaRecorder.prepare();
            }catch (java.io.IOException ioe) {
                android.util.Log.e("[Monkey]", "IOException: " + android.util.Log.getStackTraceString(ioe));

            }catch (java.lang.SecurityException e) {
                android.util.Log.e("[Monkey]", "SecurityException: " + android.util.Log.getStackTraceString(e));
            }
            try
            {
                mediaRecorder.start();
            }catch (java.lang.SecurityException e) {
                android.util.Log.e("[Monkey]", "SecurityException: " + android.util.Log.getStackTraceString(e));
            }

        }
    }

    private void stopRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    private void updateSoundText() {
        double db = soundDb(AMP);
        if(db < 0) db = 0;
        soundText.setText(df.format(db));
        _sound = db;
    }

    private double getAmplitudeEMA() {
        double amp = getAmplitude();
        mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
        return mEMA;
    }

    private double getAmplitude() {
        if(mediaRecorder != null)
            return mediaRecorder.getMaxAmplitude();
        else
            return 0;
    }


    public double soundDb(double amp1){
        return 20*Math.log10(getAmplitudeEMA() / amp1);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if(location != null) {
            CLocation myLocation = new CLocation(location, this.useMetricUnits());
            this.updateSpeed(myLocation);
        }

        // log longitude and latitude value
        //Log.d("LONGITUDE/LATITUDE", location.getLatitude()+" | "+ location.getLongitude());
        latitude = String.valueOf(location.getLatitude());
        longitude = String.valueOf(location.getLongitude());
//        Log.d("LONGITUDE/LATITUDE", latitude+" | "+ longitude);

        // get address
        try{
            Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if(addresses.size() > 0){
                address = addresses.get(0).getAddressLine(0);
//            Log.d("CURRENT ADDRESS", address);

            }else{
                Log.d("CURRENT ADDRESS", null);
            }

        }catch (Exception e){
            e.printStackTrace();
        }


    }

    public void getWeatherDetails() {
        String tempUrl = "";
        String city = "bangalore";
        String country = "india";

        tempUrl = url + "?q=" + city + "," + country + "&appid=" + appId;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, tempUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response---->", response);
                String output = "";
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("weather");
                    JSONObject jsonObjectWeather = jsonArray.getJSONObject(0);
                    String description = jsonObjectWeather.getString("description");
                    JSONObject jsonObjectMain = jsonObject.getJSONObject("main");

                    double pressure = jsonObjectMain.getInt("pressure");
                    pressureText.setText((pressure)+"");
                    //checkAccidentDetection.set_pressure(pressure);
                    _pressure = pressure;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, error -> Log.d("response error-->", error.toString()));
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(stringRequest);
    }
}