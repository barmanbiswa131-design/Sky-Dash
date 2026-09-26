package com.biswa.jarvis;

import android.app.*;
import android.content.*;
import android.os.*;
import android.speech.*;
import android.speech.tts.*;
import java.util.*;

public class VoiceService extends Service {
    private SpeechRecognizer recognizer;
    private TextToSpeech tts;
    private Brain brain;
    private boolean quiet=false, stopping=false, proactiveEnabled=true, speaking=false, llmBusy=false;
    private long lastInteraction=System.currentTimeMillis();
    private String lastInput="";
    private long lastInputAt=0L;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Runnable proactiveCheck=()->checkProactive();

    @Override public void onCreate(){
        super.onCreate();
        brain=new Brain(this);

        NotificationChannel ch=new NotificationChannel(
                "AI_SERVICE","Personal AI",NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(ch);
        Notification n=new Notification.Builder(this,"AI_SERVICE")
                .setContentTitle("Personal AI active")
                .setContentText("Hindi voice assistant is running")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .build();
        startForeground(7,n);

        tts=new TextToSpeech(this,status->{
            if(status==TextToSpeech.SUCCESS){
                tts.setLanguage(new Locale("hi","IN"));
                selectHindiVoice();
                tts.setSpeechRate(0.90f);
                tts.setPitch(0.86f);
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener(){
                    @Override public void onStart(String id){ speaking=true; }
                    @Override public void onDone(String id){
                        speaking=false;
                        if(!stopping) handler.postDelayed(()->startListening(),450);
                    }
                    @Override public void onError(String id){
                        speaking=false;
                        if(!stopping) handler.postDelayed(()->startListening(),450);
                    }
                });
            }
        });

        handler.postDelayed(proactiveCheck,5*60*1000L);
        handler.postDelayed(()->startListening(),700);
    }

    private void selectHindiVoice(){
        if(tts==null)return;
        Set<Voice> voices=tts.getVoices();
        Voice best=null;
        if(voices!=null){
            for(Voice v:voices){
                Locale l=v.getLocale();
                if("hi".equalsIgnoreCase(l.getLanguage()) &&
                   "IN".equalsIgnoreCase(l.getCountry())){
                    String n=v.getName().toLowerCase(Locale.ROOT);
                    if(n.contains("male")||n.contains("prabhat")||n.contains("man")||n.contains("x-hia")){
                        best=v;
                        break;
                    }
                    if(best==null)best=v;
                }
            }
        }
        if(best!=null)tts.setVoice(best);
    }

    private void startListening(){
        if(stopping||quiet||speaking||llmBusy||!SpeechRecognizer.isRecognitionAvailable(this))return;

        if(recognizer!=null){
            try{recognizer.cancel();recognizer.destroy();}catch(Exception ignored){}
            recognizer=null;
        }

        recognizer=SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new RecognitionListener(){
            public void onResults(Bundle b){
                ArrayList<String> r=b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if(r!=null&&!r.isEmpty())handle(r.get(0));
                if(!speaking&&!llmBusy)restart(500);
            }
            public void onError(int e){if(!speaking&&!llmBusy)restart(1200);}
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
        i.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,3);
        recognizer.startListening(i);
    }

    private void restart(long ms){
        if(!stopping&&!quiet&&!speaking&&!llmBusy)handler.postDelayed(this::startListening,ms);
    }

    private void handle(String text){
        String clean=text==null?"":text.trim();
        if(clean.isEmpty())return;

        long now=System.currentTimeMillis();
        String key=clean.toLowerCase(Locale.ROOT);
        if(key.equals(lastInput)&&now-lastInputAt<2500)return;
        lastInput=key;
        lastInputAt=now;
        lastInteraction=now;

        String r=brain.reply(clean);

        if("QUIET".equals(r)){
            quiet=true;
            proactiveEnabled=false;
            say("ठीक है, Sir. मैं चुप रहूँगा।");
            return;
        }

        if("RESUME".equals(r)){
            quiet=false;
            proactiveEnabled=true;
            lastInteraction=System.currentTimeMillis();
            say("जी, Sir. मैं फिर से active हूँ।");
            return;
        }

        if(r!=null && r.startsWith("PHONE:")){\n            PhoneActionEngine engine=new PhoneActionEngine(this);\n            String answer=engine.execute(r.substring(6));\n            if(answer!=null) say(answer);\n            return;\n        }\n\n        if("STOP".equals(r)){
            quiet=true;
            proactiveEnabled=false;
            stopping=true;
            stopListeningNow();
            if(tts!=null){
                Bundle p=new Bundle();
                p.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME,1.0f);
                tts.speak("ठीक है, Sir. मैं बंद हो रहा हूँ।",TextToSpeech.QUEUE_FLUSH,p,"shutdown");
            }
            handler.postDelayed(()->stopSelf(),1800);
            return;
        }

        if("__LOCAL_LLM__".equals(r)){
            askLocalBrain(clean);
            return;
        }

        if(r!=null&&!quiet)say(makeNatural(r));
    }

    private void askLocalBrain(String userText){
        if(llmBusy||quiet)return;
        llmBusy=true;
        stopListeningNow();

        LocalLLMClient.generate(brain.systemPrompt(),brain.buildPrompt(userText),
                new LocalLLMClient.Callback(){
                    @Override public void onResult(String answer){
                        handler.post(()->{
                            llmBusy=false;
                            if(stopping||quiet)return;
                            brain.saveLLMAnswer(userText,answer);
                            lastInteraction=System.currentTimeMillis();
                            say(makeNatural(answer));
                        });
                    }
                    @Override public void onError(String message){
                        handler.post(()->{
                            llmBusy=false;
                            if(stopping||quiet)return;
                            say("Sir, मेरा local brain अभी चालू नहीं है।");
                        });
                    }
                });
    }

    private String makeNatural(String s){
        String t=s==null?"":s.trim();
        if(t.isEmpty())return t;
        if(t.startsWith("ठीक है।"))
            return "जी, Sir. "+t.substring("ठीक है।".length()).trim();
        return t;
    }

    private void stopListeningNow(){
        if(recognizer!=null){
            try{recognizer.cancel();recognizer.destroy();}catch(Exception ignored){}
            recognizer=null;
        }
    }

    private void checkProactive(){
        if(!stopping){
            long silent=System.currentTimeMillis()-lastInteraction;
            if(proactiveEnabled&&!quiet&&!speaking&&!llmBusy&&silent>=5*60*1000L){
                say("Sir, क्या हुआ? बहुत देर से चुप हैं। सब ठीक है?");
                lastInteraction=System.currentTimeMillis();
            }
            handler.postDelayed(proactiveCheck,5*60*1000L);
        }
    }

    private void say(String s){
        if(tts==null||s==null||s.trim().isEmpty())return;
        stopListeningNow();
        Bundle p=new Bundle();
        p.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME,1.0f);
        String id="personal_ai_"+System.currentTimeMillis();
        tts.speak(s,TextToSpeech.QUEUE_FLUSH,p,id);
    }

    @Override public int onStartCommand(Intent i,int flags,int id){
        if(i!=null){
            if("QUIET".equals(i.getAction())){
                quiet=true;
                proactiveEnabled=false;
                say("ठीक है, Sir. मैं चुप रहूँगा।");
            }else if("RESUME".equals(i.getAction())){
                quiet=false;
                proactiveEnabled=true;
                lastInteraction=System.currentTimeMillis();
                say("जी, Sir. मैं फिर से active हूँ।");
            }else if("STOP".equals(i.getAction())){
                stopping=true;
                quiet=true;
                proactiveEnabled=false;
                stopListeningNow();
                if(tts!=null){
                    Bundle p=new Bundle();
                    p.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME,1.0f);
                    tts.speak("ठीक है, Sir. मैं बंद हो रहा हूँ।",TextToSpeech.QUEUE_FLUSH,p,"shutdown");
                }
                handler.postDelayed(()->stopSelf(),1800);
            }
        }
        return START_STICKY;
    }

    @Override public void onDestroy(){
        stopping=true;
        handler.removeCallbacksAndMessages(null);
        stopListeningNow();
        if(tts!=null)tts.shutdown();
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent i){return null;}
}