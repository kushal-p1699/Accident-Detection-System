package com.example.accidentdetectionsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class Start_tracking extends AppCompatActivity implements SensorEventListener, LocationListener {

    private TextView gForceText, speedText, soundText, pressureText;
    Button startTrackingBtn, stopTrackingBtn, logoutBtn;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor, pressureSensor;

    MediaRecorder mediaRecorder;
    Thread runner;
    private static double mEMA = 0.0;
    static final private double EMA_FILTER = 0.6;
    private static final double AMP = 1;// Math.pow(1, 1);
    public static final int MY_PERMISSIONS_REQUEST = 10;

    DecimalFormat df = new DecimalFormat("00.00");


    final Runnable updater = new Runnable() {
        @Override
        public void run() {
            updateSoundText();
        };
    };

    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_tracking);

        // Initialize all instance here
        Initialize();

        startTrackingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                stopTrackingBtn.setVisibility(View.VISIBLE);
                startTrackingBtn.setVisibility(View.GONE);

                Toast.makeText(Start_tracking.this, "Tracking is started...", Toast.LENGTH_SHORT).show();

                ProcessSensors();
                onResume();

                ProcessGPSSpeed();
                ProcessSoundTest();

            }
        });

        // on stop tracking
        stopTrackingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Start_tracking.this, "Tracking is stoped..", Toast.LENGTH_SHORT).show();
                finish();
                startActivity(getIntent());
            }
        });

        onLogout();
    }

    private void onLogout() {
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), Login_Activity.class));
                finish();
            }
        });
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

    @SuppressLint("MissingPermission")
    private void ProcessGPSSpeed() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
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
            Toast.makeText(this, "waiting for gps connection!", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onLocationChanged(Location location) {
        if(location != null) {
            CLocation myLocation = new CLocation(location, this.useMetricUnits());
            this.updateSpeed(myLocation);
        }
    }

    private boolean useMetricUnits() {
        return  true;
    }

    private void ProcessSensors() {
        /** Get an instance of the sensor service, and use that to get an instance of a particular sensor **/
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
    }

    private void Initialize() {
        gForceText = (TextView) findViewById(R.id.gforce_id);
        speedText = (TextView) findViewById(R.id.speed_id);
        pressureText = (TextView) findViewById(R.id.pressure_id);
        soundText = (TextView) findViewById(R.id.sound_id);

        startTrackingBtn = (Button) findViewById(R.id.startTracking_btn_id);
        stopTrackingBtn = (Button) findViewById(R.id.stopTrackingBtn_id);
        logoutBtn = (Button) findViewById(R.id.btn_logout);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // start recording
        startRecorder();

        // register sensor listeners
        if(isAvailable(accelerometerSensor))
            sensorManager.registerListener(this, accelerometerSensor, sensorManager.SENSOR_DELAY_NORMAL);
//        else
//             DisplayNiL(gForceText);

        if(isAvailable(pressureSensor))
            sensorManager.registerListener(Start_tracking.this, pressureSensor, sensorManager.SENSOR_DELAY_NORMAL);
//        else
//            DisplayNiL(pressureText);
    }

    @Override
    protected void onPause() {
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

    private boolean isAvailable(Sensor sensor) {
        if(sensor == null)
            return false;
        return true;
    }

    private void DisplayNiL(TextView tv){
        tv.setText("NIL");
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

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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

    public double soundDb(double amp1){
        return 20*Math.log10(getAmplitudeEMA() / amp1);
    }

    private double getAmplitude() {
        if(mediaRecorder != null)
            return mediaRecorder.getMaxAmplitude();
        else
            return 0;
    }
}