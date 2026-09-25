package com.biswa.jarvis;

import android.app.*; import android.content.*; import android.os.*; import android.speech.*; import android.speech.tts.TextToSpeech; import java.util.*;

public class VoiceService extends Service {
    private SpeechRecognizer recognizer; private TextToSpeech tts; private Brain brain;
    private boolean quiet=false, stopping=false; private final Handler handler=new Handler(Looper.getMainLooper());

    @Override public void onCreate(){
        super.onCreate(); brain=new Brain(this);
        NotificationChannel ch=new NotificationChannel("AI_SERVICE","Personal AI",NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(ch);
        Notification n=new Notification.Builder(this,"AI_SERVICE").setContentTitle("Personal AI active").setContentText("Microphone listening service is running").setSmallIcon(android.R.drawable.ic_btn_speak_now).build();
        startForeground(7,n); tts=new TextToSpeech(this,status->{}); startListening();
    }
    private void startListening(){
        if(stopping||!SpeechRecognizer.isRecognitionAvailable(this))return;
        if(recognizer!=null){recognizer.destroy();recognizer=null;}
        recognizer=SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new RecognitionListener(){
            public void onResults(Bundle b){ArrayList<String> r=b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);if(r!=null&&!r.isEmpty())handle(r.get(0));restart(500);}
            public void onError(int e){restart(1200);}
            public void onReadyForSpeech(Bundle b){} public void onBeginningOfSpeech(){} public void onRmsChanged(float v){} public void onBufferReceived(byte[] b){} public void onEndOfSpeech(){} public void onPartialResults(Bundle b){} public void onEvent(int a,Bundle b){}
        });
        Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"bn-IN"); i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); recognizer.startListening(i);
    }
    private void restart(long ms){if(!stopping)handler.postDelayed(this::startListening,ms);}
    private void handle(String text){
        String r=brain.reply(text);
        if("QUIET".equals(r)){say("ঠিক আছে, আমি চুপ থাকছি।");quiet=true;return;}
        if("RESUME".equals(r)){quiet=false;say("ঠিক আছে, আবার active আছি।");return;}
        if(r!=null&&!quiet)say(r);
    }
    private void say(String s){if(tts!=null)tts.speak(s,TextToSpeech.QUEUE_FLUSH,null,"personal_ai");}
    @Override public int onStartCommand(Intent i,int flags,int id){if(i!=null&&"QUIET".equals(i.getAction()))quiet=true;return START_STICKY;}
    @Override public void onDestroy(){stopping=true;handler.removeCallbacksAndMessages(null);if(recognizer!=null)recognizer.destroy();if(tts!=null)tts.shutdown();super.onDestroy();}
    @Override public IBinder onBind(Intent i){return null;}
}