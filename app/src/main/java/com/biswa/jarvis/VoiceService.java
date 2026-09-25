package com.biswa.jarvis;

import android.app.*;
import android.content.*;
import android.os.*;
import android.speech.*;
import android.speech.tts.TextToSpeech;
import java.util.*;

public class VoiceService extends Service {
    private SpeechRecognizer recognizer;
    private TextToSpeech tts;
    private boolean quiet = false;
    private boolean stopping = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override public void onCreate() {
        super.onCreate();
        NotificationChannel ch = new NotificationChannel("AI_SERVICE","Personal AI",NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(ch);
        Notification n = new Notification.Builder(this,"AI_SERVICE")
                .setContentTitle("Personal AI active")
                .setContentText("Microphone listening service is running")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now).build();
        startForeground(7,n);
        tts = new TextToSpeech(this, status -> {});
        startListening();
    }

    private void startListening() {
        if (stopping || !SpeechRecognizer.isRecognitionAvailable(this)) return;
        if (recognizer != null) { recognizer.destroy(); recognizer = null; }
        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new RecognitionListener() {
            public void onResults(Bundle b) {
                ArrayList<String> r=b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if(r!=null&&!r.isEmpty()) handle(r.get(0));
                scheduleRestart(500);
            }
            public void onError(int e) { scheduleRestart(1200); }
            public void onReadyForSpeech(Bundle b) {}
            public void onBeginningOfSpeech() {}
            public void onRmsChanged(float v) {}
            public void onBufferReceived(byte[] b) {}
            public void onEndOfSpeech() {}
            public void onPartialResults(Bundle b) {}
            public void onEvent(int a,Bundle b) {}
        });
        Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"bn-IN");
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,false);
        recognizer.startListening(i);
    }

    private void scheduleRestart(long ms) {
        if(!stopping) handler.postDelayed(this::startListening,ms);
    }

    private void handle(String x) {
        String t=x.toLowerCase(Locale.ROOT);
        if(t.contains("চুপ")||t.contains("chup")) {
            say("ঠিক আছে, আমি চুপ থাকছি।");
            quiet=true;
        } else if(t.contains("আবার কথা")||t.contains("abar kotha")) {
            quiet=false;
            say("ঠিক আছে, আবার active আছি।");
        } else if(t.contains("watch")||t.contains("ওয়াচ")||t.contains("ঘড়ি")) {
            say("Watch-এর জন্য Bluetooth এবং supported watch companion permission লাগতে পারে। Permission দিলে আমি connection শুরু করব।");
        } else if(!quiet) {
            say("জি, বলুন। আমি শুনছি।");
        }
    }

    private void say(String s) {
        if(tts!=null) tts.speak(s,TextToSpeech.QUEUE_FLUSH,null,"personal_ai");
    }

    @Override public int onStartCommand(Intent i,int flags,int id) {
        if(i!=null && "QUIET".equals(i.getAction())) quiet=true;
        return START_STICKY;
    }

    @Override public void onDestroy() {
        stopping=true; handler.removeCallbacksAndMessages(null);
        if(recognizer!=null) recognizer.destroy();
        if(tts!=null) tts.shutdown();
        super.onDestroy();
    }

    @Override public android.os.IBinder onBind(Intent i){ return null; }
}