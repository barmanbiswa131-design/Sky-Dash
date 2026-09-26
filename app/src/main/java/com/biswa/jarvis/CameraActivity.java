package com.biswa.jarvis;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends Activity {
    private PreviewView preview;
    private ExecutorService executor;
    private TextToSpeech tts;
    private boolean analyzed=false;
    private String mode="rear";

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        mode=getIntent().getStringExtra("camera");
        if(mode==null)mode="rear";

        preview=new PreviewView(this);
        preview.setLayoutParams(new android.view.ViewGroup.LayoutParams(-1,-1));
        setContentView(preview);

        executor=Executors.newSingleThreadExecutor();
        tts=new TextToSpeech(this,status->{ if(status==TextToSpeech.SUCCESS) tts.setLanguage(new Locale("hi","IN")); });

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},50);
        }else startCamera();
    }

    private void startCamera(){
        ListenableFuture<ProcessCameraProvider> future=ProcessCameraProvider.getInstance(this);
        future.addListener(()->{
            try{
                ProcessCameraProvider provider=future.get();
                provider.unbindAll();

                Preview p=new Preview.Builder().build();
                p.setSurfaceProvider(preview.getSurfaceProvider());

                CameraSelector selector=mode.equalsIgnoreCase("front")
                        ?CameraSelector.DEFAULT_FRONT_CAMERA
                        :CameraSelector.DEFAULT_BACK_CAMERA;

                ImageAnalysis analysis=new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                analysis.setAnalyzer(executor,new ImageAnalysis.Analyzer(){
                    @Override public void analyze(@NonNull ImageProxy image){
                        if(!analyzed){
                            analyzed=true;
                            analyzeFrame(image);
                        }else image.close();
                    }
                });

                provider.bindToLifecycle(this,selector,p,analysis);
                new Handler().postDelayed(()->{
                    if(!analyzed){
                        // Camera may need a little more time; keep waiting.
                    }
                },2500);
            }catch(Exception e){
                speakAndFinish("Sir, camera चालू नहीं हो पाया।");
            }
        },ContextCompat.getMainExecutor(this));
    }

    private void analyzeFrame(ImageProxy proxy){
        try{
            if(proxy.getImage()==null){ proxy.close(); analyzed=false; return; }
            InputImage input=InputImage.fromMediaImage(proxy.getImage(),proxy.getImageInfo().getRotationDegrees());

            ImageLabeler labeler=ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
            FaceDetector detector=FaceDetection.getClient(
                    new FaceDetectorOptions.Builder()
                            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                            .build());

            labeler.process(input).addOnSuccessListener(labels->{
                detector.process(input).addOnSuccessListener(faces->{
                    String answer=buildAnswer(labels,faces);
                    labeler.close();
                    detector.close();
                    proxy.close();
                    speakAndFinish(answer);
                }).addOnFailureListener(e->{
                    detector.close(); labeler.close(); proxy.close();
                    speakAndFinish("Sir, আমি ছবিটা ঠিকমতো বুঝতে পারিনি।");
                });
            }).addOnFailureListener(e->{
                labeler.close(); detector.close(); proxy.close();
                speakAndFinish("Sir, আমি ছবিটা ঠিকমতো বুঝতে পারিনি।");
            });
        }catch(Exception e){
            proxy.close();
            speakAndFinish("Sir, camera frame analyse করা যায়নি।");
        }
    }

    private String buildAnswer(List<ImageLabel> labels,List<Face> faces){
        if(mode.equalsIgnoreCase("front") && !faces.isEmpty()){
            Face f=faces.get(0);
            Float smile=f.getSmilingProbability();
            if(smile!=null && smile<0.25f) return "Sir, আপনার মুখে এখন হাসি কম। একটু মন খারাপ বা চিন্তিত মনে হচ্ছে। কী হয়েছে?";
            if(smile!=null && smile>0.75f) return "Sir, আপনাকে হাসিখুশি লাগছে।";
            return "জি Sir, আমি আপনাকে দেখছি।";
        }

        StringBuilder s=new StringBuilder("জি Sir, আমি দেখছি");
        int count=0;
        for(ImageLabel l:labels){
            if(l.getConfidence()>=0.65f){
                if(count==0)s.append(": ");
                else s.append(", ");
                s.append(l.getText());
                count++;
                if(count>=3)break;
            }
        }
        if(count==0)s.append("। কিন্তু জিনিসটা নিশ্চিতভাবে শনাক্ত করতে পারিনি।");
        else s.append("।");
        return s.toString();
    }

    private void speakAndFinish(String text){
        runOnUiThread(()->{
            if(tts!=null) tts.speak(text,TextToSpeech.QUEUE_FLUSH,null,"camera_answer");
            new Handler().postDelayed(()->finish(),3500);
        });
    }

    @Override protected void onDestroy(){
        if(executor!=null)executor.shutdownNow();
        if(tts!=null)tts.shutdown();
        super.onDestroy();
    }

    @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){
        super.onRequestPermissionsResult(r,p,g);
        if(r==50 && g.length>0 && g[0]==PackageManager.PERMISSION_GRANTED) startCamera();
        else speakAndFinish("Sir, camera permission না দিলে আমি দেখতে পারব না।");
    }
}