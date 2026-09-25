package com.biswa.jarvis;

import android.content.Context;
import org.json.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class LocalLLMClient {
    public interface Callback {
        void onResult(String answer);
        void onError(String message);
    }

    private static final String ENDPOINT="http://127.0.0.1:8080/v1/chat/completions";

    public static void generate(String systemPrompt,String userPrompt,Callback cb){
        new Thread(()->{
            HttpURLConnection c=null;
            try{
                URL u=new URL(ENDPOINT);
                c=(HttpURLConnection)u.openConnection();
                c.setRequestMethod("POST");
                c.setConnectTimeout(1200);
                c.setReadTimeout(45000);
                c.setDoOutput(true);
                c.setRequestProperty("Content-Type","application/json");

                JSONArray messages=new JSONArray();
                messages.put(new JSONObject().put("role","system").put("content",systemPrompt));
                messages.put(new JSONObject().put("role","user").put("content",userPrompt));

                JSONObject body=new JSONObject();
                body.put("messages",messages);
                body.put("temperature",0.65);
                body.put("top_p",0.9);
                body.put("max_tokens",180);
                body.put("stream",false);

                byte[] data=body.toString().getBytes(StandardCharsets.UTF_8);
                OutputStream out=c.getOutputStream();
                out.write(data);
                out.close();

                int code=c.getResponseCode();
                InputStream in=code>=200&&code<300?c.getInputStream():c.getErrorStream();
                String response=readAll(in);

                if(code<200||code>=300)throw new IOException("Local brain HTTP "+code);
                JSONObject json=new JSONObject(response);
                JSONArray choices=json.optJSONArray("choices");
                if(choices==null||choices.length()==0)throw new IOException("No answer from local brain");
                JSONObject msg=choices.getJSONObject(0).optJSONObject("message");
                String answer=msg==null?"":msg.optString("content","").trim();
                if(answer.isEmpty())throw new IOException("Local brain returned empty answer");
                cb.onResult(answer);
            }catch(Exception e){
                cb.onError("Local brain is not running.");
            }finally{
                if(c!=null)c.disconnect();
            }
        }).start();
    }

    private static String readAll(InputStream in)throws IOException{
        if(in==null)return "";
        BufferedReader r=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8));
        StringBuilder s=new StringBuilder();
        String line;
        while((line=r.readLine())!=null)s.append(line);
        r.close();
        return s.toString();
    }
}