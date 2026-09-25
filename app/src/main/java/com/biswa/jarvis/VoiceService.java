package com.biswa.jarvis;

import android.app.*; import android.content.*; import android.os.*; import android.speech.*; import android.speech.tts.TextToSpeech; import java.util.*;

public class VoiceService extends Service {
    SpeechRecognizer recognizer; TextToSpeech tts; boolean quiet=false;
    @Override public void onCreate(){ super.onCreate();
        String ch="AI_SERVICE"; NotificationChannel nc=new NotificationChannel(ch,"Personal AI",NotificationManager.IMPORTANCE_LOW); getSystemService(NotificationManager.class).createNotificationChannel(nc);
        Notification n=new Notification.Builder(this,ch).setContentTitle("Personal AI active").setContentText("Microphone service is running").setSmallIcon(android.R.drawable.ic_btn_speak_now).build(); startForeground(7,n);
        tts=new TextToSpeech(this,s->{}); startListening();
    }
    void startListening(){ if(!SpeechRecognizer.isRecognitionAvailable(this)) return; recognizer=SpeechRecognizer.createSpeechRecognizer(this); recognizer.setRecognitionListener(new RecognitionListener(){ public void onResults(Bundle b){ ArrayList<String> r=b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION); if(r!=null&&!r.isEmpty()) handle(r.get(0)); startListening(); } public void onError(int e){ new Handler().postDelayed(()->startListening(),1000); } public void onReadyForSpeech(Bundle b){} public void onBeginningOfSpeech(){} public void onRmsChanged(float v){} public void onBufferReceived(byte[] b){} public void onEndOfSpeech(){} public void onPartialResults(Bundle b){} public void onEvent(int a,Bundle b){} }); Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"bn-IN"); i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); recognizer.startListening(i); }
    void handle(String x){ String t=x.toLowerCase(Locale.ROOT); if(t.contains("চুপ")||t.contains("chup")){quiet=true;say("ঠিক আছে, আমি চুপ থাকছি।");} else if(t.contains("আবার কথা")||t.contains("abar kotha")){quiet=false;say("ঠিক আছে, আবার active আছি।");} else if(t.contains("watch")||t.contains("ওয়াচ")||t.contains("ঘড়ি")){say("Watch-এ যেতে হলে Bluetooth এবং Watch companion permission লাগতে পারে। Permission দিলে আমি connection শুরু করব।");} else if(!quiet) say("জি, বলুন। আমি শুনছি।"); }
    void say(String s){ if(tts!=null&&!quiet) tts.speak(s,TextToSpeech.QUEUE_FLUSH,null,"jarvis"); }
    @Override public int onStartCommand(Intent i,int f,int id){ if(i!=null&&"QUIET".equals(i.getAction())) quiet=true; return START_STICKY; }
    @Override public void onDestroy(){ if(recognizer!=null) recognizer.destroy(); if(tts!=null) tts.shutdown(); super.onDestroy(); }
    @Override public android.os.IBinder onBind(Intent i){ return null; }
}
