package com.biswa.jarvis;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.*;

public class MemoryStore {
    private final SharedPreferences p;
    public MemoryStore(Context c){ p=c.getSharedPreferences("ai_memory",Context.MODE_PRIVATE); }

    public void remember(String key,String value){
        if(key==null||value==null)return;
        p.edit().putString(key,value).apply();
    }
    public String recall(String key){ return p.getString(key,null); }
    public Map<String,?> all(){ return p.getAll(); }
    public void forget(String key){ p.edit().remove(key).apply(); }
    public void clear(){ p.edit().clear().apply(); }

    public void addConversation(String user,String assistant){
        String old=p.getString("conversation_log","");
        String line="USER: "+user+"\nAI: "+assistant+"\n";
        String next=old+line;
        if(next.length()>12000) next=next.substring(next.length()-12000);
        p.edit().putString("conversation_log",next).apply();
    }

    public String conversation(){
        return p.getString("conversation_log","");
    }
}