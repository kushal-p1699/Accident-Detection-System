package com.example.accidentdetectionsystem;

import android.Manifest;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.accidentdetectionsystem.ui.home.HomeFragment;

public class MyReceiver extends BroadcastReceiver {



    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            Bundle bundle = intent.getExtras();
            SmsMessage[] messages = null;
            String msg_from;
            if(bundle != null) {
                try {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    messages = new SmsMessage[pdus.length];
                    for(int i=0; i<messages.length; i++) {
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        msg_from = messages[i].getOriginatingAddress();
                        String msgBody = messages[i].getMessageBody();

                        Toast.makeText(context, "Form: "+msg_from+" Body: "+msgBody, Toast.LENGTH_SHORT).show();

                        // send to activity
                        Intent intent1 = new Intent(context, MainActivity.class);
                        intent1.putExtra("msg-body", msgBody);
                        intent1.putExtra("msg-from", msg_from);
                        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent1);

//                        HomeFragment fragment = new HomeFragment();
//                        Bundle args = new Bundle();
//                        args.putString("msg-form", msg_from);
//                        args.putString("msg-body", msgBody);
//                        fragment.setArguments(args);

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

    }

}
