package com.example.accidentdetectionsystem.ui.home;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.Navigation;

import com.example.accidentdetectionsystem.CLocation;
import com.example.accidentdetectionsystem.Login_Activity;
import com.example.accidentdetectionsystem.R;
import com.example.accidentdetectionsystem.ui.profile.ProfileFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import android.view.View.OnClickListener;

import java.text.DecimalFormat;
import java.util.Formatter;
import java.util.Locale;

public class HomeFragment extends Fragment implements SensorEventListener, LocationListener {
    private TextView gForceText, speedText, soundText, pressureText;
    Button startTrackingBtn, stopTrackingBtn, logoutBtn;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor, pressureSensor;

    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    String userId;

    MediaRecorder mediaRecorder;
    Thread runner;
    private static double mEMA = 0.0;
    static final private double EMA_FILTER = 0.6;
    private static final double AMP = 1;// Math.pow(1, 1);
    public static final int MY_PERMISSIONS_REQUEST = 10;

    final Runnable updater = new Runnable() {
        @Override
        public void run() {
            updateSoundText();
        };
    };

    final Handler handler = new Handler();

    DecimalFormat df = new DecimalFormat("00.00");
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize all instance here
        Initialize(root);

        startTrackingBtn.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {

                // validate user data
                // update profile before start tracking

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

                            /** if profile is updated then, Start tracking **/

                            stopTrackingBtn.setVisibility(View.VISIBLE);
                            startTrackingBtn.setVisibility(View.GONE);

                            Toast.makeText(getActivity(), "Tracking is started...", Toast.LENGTH_SHORT).show();

                            ProcessSensors();
                            onResume();

                            ProcessGPSSpeed();
                            ProcessSoundTest();
                        }
                    }
                });
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getActivity(), Login_Activity.class));
            }
        });

        // on stop tracking
        stopTrackingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });

        return root;
    }

    private boolean IsProfileUpdate() {

        boolean[] flag = {true};


        return flag[0];
    }

    private void Initialize(View v) {
        gForceText = (TextView) v.findViewById(R.id.id_gforce);
        speedText = (TextView) v.findViewById(R.id.id_speed);
        pressureText = (TextView) v.findViewById(R.id.id_pressure);
        soundText = (TextView) v.findViewById(R.id.id_sound);

        startTrackingBtn = (Button) v.findViewById(R.id.id_startTractkingBtn);
        stopTrackingBtn = (Button) v.findViewById(R.id.id_stopTrackingBtn);
        logoutBtn = (Button) v.findViewById(R.id.logout_btn_id);

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = fAuth.getCurrentUser().getUid();
    }

    private void ProcessSensors() {
        /** Get an instance of the sensor service, and use that to get an instance of a particular sensor **/
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
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
            Toast.makeText(getActivity(), "waiting for gps connection!", Toast.LENGTH_SHORT).show();
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

        if(isAvailable(pressureSensor))
            sensorManager.registerListener(this, pressureSensor, sensorManager.SENSOR_DELAY_NORMAL);
//        else
//            DisplayNiL(pressureText);
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

            case Sensor.TYPE_PRESSURE:
                pressureText.setText(event.values[0]+"");
                break;
        }
    }

    private void processAccelerometerSensor(double xValue, double yValue, double zValue) {
        //this will give you the accelerometer values
        // index 0 for x axis, 1 for y axis and, z for z axis
        //accelerometerText.setText(event.values[0]+"\n"+event.values[1]+"\n"+event.values[2])
        double gForce = Math.sqrt((xValue * xValue) + (yValue * yValue) + (zValue * zValue));

        // display accelerometer value in textView
        DecimalFormat df = new DecimalFormat("##.00");
        gForceText.setText(df.format(gForce)+"");
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
    }

}