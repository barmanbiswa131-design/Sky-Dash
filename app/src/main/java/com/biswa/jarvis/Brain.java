package com.biswa.jarvis;

import android.content.Context;
import java.util.*;

public class Brain {
    private final MemoryStore memory;
    public Brain(Context c){ memory=new MemoryStore(c); }

    public String reply(String input){
        String t=input.trim().toLowerCase(Locale.ROOT);
        if(t.isEmpty()) return null;

        if(t.contains("আমার নাম") && (t.contains("মনে রাখ")||t.contains("remember"))){
            String value=input.replaceAll("(?i).*আমার নাম","").replaceAll("(?i).*remember","").replaceAll("মনে রাখ","").trim();
            if(!value.isEmpty()){ memory.remember("user_name",value); return "ঠিক আছে। তোমার নাম আমি মনে রাখলাম।"; }
        }
        if(t.contains("আমার নাম কি")||t.contains("আমার নাম কী")||t.contains("what is my name")){
            String v=memory.recall("user_name");
            return v==null ? "তোমার নাম এখনো আমার memory-তে নেই।" : "তোমার নাম "+v+"।";
        }
        if(t.contains("ভুলে যাও")||t.contains("forget memory")){ memory.clear(); return "ঠিক আছে। আমার saved memory মুছে দিলাম।"; }
        if(t.contains("চুপ")||t.contains("chup")) return "QUIET";
        if(t.contains("আবার কথা")||t.contains("abar kotha")) return "RESUME";

        if(t.contains("কে বানিয়েছে")||t.contains("who made you"))
            return "আমাকে Biswajit বানাচ্ছে।";
        if(t.contains("তোমার নাম")||t.contains("your name"))
            return "আমার নাম Personal AI।";
        if(t.contains("কেমন আছ")||t.contains("how are you"))
            return "আমি ঠিক আছি। তোমার কাজে সাহায্য করার জন্য ready আছি।";

        return "আমি বুঝেছি তুমি বলছো: "+input+"। এই বিষয়ে ভালো উত্তর দেওয়ার জন্য আমার knowledge system আরও উন্নত করা হচ্ছে।";
    }
}