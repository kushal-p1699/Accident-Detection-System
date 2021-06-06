package com.example.accidentdetectionsystem.ui.home;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.accidentdetectionsystem.R;

public class AlarmSound {
    MediaPlayer mediaPlayer;
    Context context;

    AlarmSound(Context context){
        this.context = context;
    }

    public void Play(){
        if(mediaPlayer == null){
            mediaPlayer = MediaPlayer.create(context, R.raw.alramsound);
            mediaPlayer.setLooping(true);
        }

        mediaPlayer.start();
    }

    public void Stop(){
        if(mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
            Toast.makeText(context, "Alram canceld", Toast.LENGTH_SHORT).show();
        }
    }
}
