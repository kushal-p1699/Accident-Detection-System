package com.example.accidentdetectionsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.Formatter;
import java.util.Locale;

public class Start_tracking extends AppCompatActivity implements SensorEventListener, LocationListener {

    private TextView gForceText, speedText, soundText, pressureText;
    private Button startTrackingBtn;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor, pressureSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_tracking);

        // Initialize all instance here
        Initialize();

        startTrackingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProcessSensors();
                onResume();

                // check for GPS permission
                CheckGPSPermission();
            }
        });


    }

    private void CheckGPSPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 1000);
        } else {
            // start the program if permission is granted
            doStuff();
        }
    }

    @SuppressLint("MissingPermission")
    private void doStuff() {
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
            speedText.setText(strCurrentSpeed + " km/h");
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                doStuff();
            } else {
                finish();
            }
        }
    }

    private void ProcessSensors() {
        /** Get an instance of the sensor service, and use that to get an instance of a particular sensor **/
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
    }

    private void Initialize() {
        gForceText = (TextView) findViewById(R.id.gforce_id);
        speedText = (TextView) findViewById(R.id.speed_id);
        pressureText = (TextView) findViewById(R.id.pressure_id);
        soundText = (TextView) findViewById(R.id.sound_id);

        startTrackingBtn = (Button) findViewById(R.id.startTracking_btn_id);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isAvailable(accelerometerSensor))
            sensorManager.registerListener(this, accelerometerSensor, sensorManager.SENSOR_DELAY_NORMAL);
        else
            DisplayNiL(gForceText);

        if(isAvailable(pressureSensor))
            sensorManager.registerListener(Start_tracking.this, pressureSensor, sensorManager.SENSOR_DELAY_NORMAL);
        else
            DisplayNiL(pressureText);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                processAccelerometerSensor(event.values[0], event.values[1], event.values[2]);
                break;

            case Sensor.TYPE_PRESSURE:
                pressureText.setText(event.values[0]+"hPa");
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
        gForceText.setText(df.format(gForce)+ " m/s sq.");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}