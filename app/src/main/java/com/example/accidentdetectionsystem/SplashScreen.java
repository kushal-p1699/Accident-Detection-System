package com.example.accidentdetectionsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

public class SplashScreen extends AppCompatActivity {

    private int WAIT_TIME = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
//        getActionBar().hide();

        Thread splash = new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    sleep(WAIT_TIME);
                    startActivity(new Intent(getApplicationContext(), Login_Activity.class));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        splash.start();
    }
}