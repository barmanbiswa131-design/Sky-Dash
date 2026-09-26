package com.biswa.jarvis;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.graphics.Color;
import android.widget.*;

public class MainActivity extends Activity {
    TextView status;
    Button power;

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
        status.setText("Starting AI...");
        status.setTextColor(Color.LTGRAY);
        status.setTextSize(17);
        status.setPadding(0,8,0,28);
        root.addView(status);

        power=new Button(this);
        power.setText("START AI");
        root.addView(power);

        TextView hint=new TextView(this);
        hint.setText("Voice control: “chup raho” / “phir se baat karo” / “AI band ho jao”");
        hint.setTextColor(Color.GRAY);
        hint.setTextSize(14);
        hint.setPadding(8,24,8,0);
        root.addView(hint);

        power.setOnClickListener(v->startAI());

        setContentView(root);
        requestPermissionsIfNeeded();

        if(Build.VERSION.SDK_INT < 23 ||
           checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED)
            startAI();
    }

    void requestPermissionsIfNeeded(){
        if(Build.VERSION.SDK_INT>=23 &&
           checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},10);
            return;
        }

        if(Build.VERSION.SDK_INT>=33 &&
           checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},11);
    }

    void startAI(){
        if(Build.VERSION.SDK_INT>=23 &&
           checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){
            requestPermissionsIfNeeded();
            return;
        }

        Intent i=new Intent(this,VoiceService.class);
        if(Build.VERSION.SDK_INT>=26)startForegroundService(i);
        else startService(i);

        power.setText("AI ACTIVE");
        power.setEnabled(false);
        status.setText("AI active. Speak normally.");
    }

    @Override protected void onResume(){
        super.onResume();
        if(Build.VERSION.SDK_INT < 23 ||
           checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED)
            startAI();
    }

    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if(requestCode==10 && grantResults.length>0 &&
           grantResults[0]==PackageManager.PERMISSION_GRANTED)
            startAI();
    }
}
