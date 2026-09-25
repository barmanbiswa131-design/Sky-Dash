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
    private Brain brain;
    private boolean quiet=false, stopping=false, proactiveEnabled=true;
    private long lastInteraction=System.currentTimeMillis();
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Runnable proactiveCheck=()->checkProactive();

    @Override public void onCreate(){
        super.onCreate();
        brain=new Brain(this);
        NotificationChannel ch=new NotificationChannel("AI_SERVICE","Personal AI",NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(ch);
        Notification n=new Notification.Builder(this,"AI_SERVICE")
                .setContentTitle("Personal AI active")
                .setContentText("Hindi voice assistant is running")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now).build();
        startForeground(7,n);
        tts=new TextToSpeech(this,status->{
            if(status==TextToSpeech.SUCCESS){
                Locale hi=new Locale("hi","IN");
                tts.setLanguage(hi);
                tts.setSpeechRate(0.95f);
            }
        });
        handler.postDelayed(proactiveCheck,5*60*1000L);
        startListening();
    }

    private void startListening(){
        if(stopping||!SpeechRecognizer.isRecognitionAvailable(this))return;
        if(recognizer!=null){try{recognizer.cancel();recognizer.destroy();}catch(Exception ignored){} recognizer=null;}
        recognizer=SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new RecognitionListener(){
            public void onResults(Bundle b){
                ArrayList<String> r=b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if(r!=null&&!r.isEmpty())handle(r.get(0));
                restart(500);
            }
            public void onError(int e){restart(1200);}
            public void onReadyForSpeech(Bundle b){}
            public void onBeginningOfSpeech(){lastInteraction=System.currentTimeMillis();}
            public void onRmsChanged(float v){}
            public void onBufferReceived(byte[] b){}
            public void onEndOfSpeech(){}
            public void onPartialResults(Bundle b){}
            public void onEvent(int a,Bundle b){}
        });
        Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"hi-IN");
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,false);
        recognizer.startListening(i);
    }

    private void restart(long ms){if(!stopping)handler.postDelayed(this::startListening,ms);}

    private void handle(String text){
        lastInteraction=System.currentTimeMillis();
        String r=brain.reply(text);
        if("QUIET".equals(r)){
            quiet=true; proactiveEnabled=false; say("ठीक है, मैं चुप रहूँगा।");
            return;
        }
        if("RESUME".equals(r)){
            quiet=false; proactiveEnabled=true; say("ठीक है, मैं फिर से active हूँ।");
            return;
        }
        if(r!=null&&!quiet)say(r);
    }

    private void checkProactive(){
        if(!stopping){
            long silent=System.currentTimeMillis()-lastInteraction;
            if(proactiveEnabled&&!quiet&&silent>=5*60*1000L){
                say("क्या हुआ? बहुत देर से चुप हो। सब ठीक है?");
                lastInteraction=System.currentTimeMillis();
            }
            handler.postDelayed(proactiveCheck,5*60*1000L);
        }
    }

    private void say(String s){
        if(tts!=null&&!quiet || tts!=null&&s.contains("चुप") || tts!=null&&s.contains("active"))
            tts.speak(s,TextToSpeech.QUEUE_FLUSH,null,"personal_ai");
    }

    @Override public int onStartCommand(Intent i,int flags,int id){
        if(i!=null){
            if("QUIET".equals(i.getAction())){quiet=true;proactiveEnabled=false;}
            if("RESUME".equals(i.getAction())){quiet=false;proactiveEnabled=true;lastInteraction=System.currentTimeMillis();}
        }
        return START_STICKY;
    }

    @Override public void onDestroy(){
        stopping=true;
        handler.removeCallbacksAndMessages(null);
        if(recognizer!=null)try{recognizer.destroy();}catch(Exception ignored){}
        if(tts!=null)tts.shutdown();
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent i){return null;}
}