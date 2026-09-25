package com.biswa.jarvis;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;

public class MainActivity extends android.app.Activity {
    TextView status;
    @Override public void onCreate(Bundle b){ super.onCreate(b);
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(32,48,32,32); root.setBackgroundColor(Color.rgb(5,7,10));
        TextView title=new TextView(this); title.setText("PERSONAL AI"); title.setTextColor(Color.WHITE); title.setTextSize(28); root.addView(title);
        status=new TextView(this); status.setText("Ready. Microphone permission is required."); status.setTextColor(Color.LTGRAY); status.setTextSize(17); root.addView(status);
        Button start=new Button(this); start.setText("START AI"); root.addView(start);
        Button quiet=new Button(this); quiet.setText("QUIET MODE"); root.addView(quiet);
        start.setOnClickListener(v->startAI());
        quiet.setOnClickListener(v->{ Intent i=new Intent(this,VoiceService.class); i.setAction("QUIET"); startService(i); status.setText("Quiet mode enabled."); });
        setContentView(root);
        requestPermissionsIfNeeded();
    }
    void requestPermissionsIfNeeded(){ if(Build.VERSION.SDK_INT>=23 && checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED) requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},10); }
    void startAI(){ if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){ requestPermissionsIfNeeded(); return; } Intent i=new Intent(this,VoiceService.class); if(Build.VERSION.SDK_INT>=26) startForegroundService(i); else startService(i); status.setText("AI service running. Say something."); }
}
