package com.biswa.jarvis;

import android.content.Context;
import java.util.*;

public class MemoryStore {
    private final MemoryDatabase db;
    public MemoryStore(Context c){ db=new MemoryDatabase(c.getApplicationContext()); }

    public void remember(String key,String value){ remember("personal",key,value); }
    public void remember(String category,String key,String value){ if(key!=null&&value!=null)db.remember(category==null?"personal":category,key,value); }
    public String recall(String key){ return db.recall(key); }
    public String memories(){ return db.recentMemories(); }

    public void addConversation(String user,String assistant){ if(user!=null&&assistant!=null)db.addConversation(user,assistant); }
    public String conversation(){ return db.recentConversation(12); }

    public void forget(String key){ db.getWritableDatabase().delete("memories","key_name=?",new String[]{key}); }
    public void clear(){ db.clearAll(); }
}