package com.example.accidentdetectionsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

public class Start_tracking extends AppCompatActivity implements SensorEventListener{

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

        ProcessSensors();
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
        gForceText.setText(gForce+"\n");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}