package com.biswa.jarvis;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.graphics.Color;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity {
    TextView status;
    Button power, quiet;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);

        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32,48,32,32);
        root.setBackgroundColor(Color.rgb(5,7,10));

        TextView title=new TextView(this);
        title.setText("PERSONAL AI");
        title.setTextColor(Color.WHITE);
        title.setTextSize(30);
        root.addView(title);

        status=new TextView(this);
        status.setText("AI stopped. Tap START AI.");
        status.setTextColor(Color.LTGRAY);
        status.setTextSize(17);
        status.setPadding(0,8,0,28);
        root.addView(status);

        power=new Button(this);
        power.setText("START AI");
        root.addView(power);

        quiet=new Button(this);
        quiet.setText("QUIET MODE");
        root.addView(quiet);

        TextView hint=new TextView(this);
        hint.setText("You can control me by voice: “chup raho” / “phir se baat karo”");
        hint.setTextColor(Color.GRAY);
        hint.setTextSize(14);
        hint.setPadding(8,24,8,0);
        root.addView(hint);

        power.setOnClickListener(v->{
            if(power.getText().toString().startsWith("START")){
                startAI();
            }else{
                stopAI();
            }
        });

        quiet.setOnClickListener(v->{
            if(quiet.getText().toString().startsWith("QUIET")){
                sendCommand("QUIET");
                quiet.setText("RESUME AI");
                status.setText("Quiet mode enabled. Say “phir se baat karo” to resume.");
            }else{
                sendCommand("RESUME");
                quiet.setText("QUIET MODE");
                status.setText("AI active again.");
            }
        });

        setContentView(root);
        requestPermissionsIfNeeded();
    }

    void requestPermissionsIfNeeded(){
        if(Build.VERSION.SDK_INT>=23 &&
           checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},10);

        if(Build.VERSION.SDK_INT>=33 &&
           checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},11);
    }

    void startAI(){
        if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){
            requestPermissionsIfNeeded();
            return;
        }

        Intent i=new Intent(this,VoiceService.class);
        if(Build.VERSION.SDK_INT>=26)startForegroundService(i);
        else startService(i);

        power.setText("STOP AI");
        status.setText("AI active. Speak normally.");
    }

    void stopAI(){
        stopService(new Intent(this,VoiceService.class));
        power.setText("START AI");
        quiet.setText("QUIET MODE");
        status.setText("AI stopped.");
    }

    void sendCommand(String action){
        Intent i=new Intent(this,VoiceService.class);
        i.setAction(action);
        if(Build.VERSION.SDK_INT>=26)startForegroundService(i);
        else startService(i);
    }
}