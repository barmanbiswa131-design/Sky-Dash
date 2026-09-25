package com.biswa.jarvis;

import android.content.Context;
import java.util.*;

public class Brain {
    private final MemoryStore memory;
    public Brain(Context c){ memory=new MemoryStore(c); }

    public String reply(String input){
        String raw=input == null ? "" : input.trim();
        String t=raw.toLowerCase(Locale.ROOT);
        if(t.isEmpty()) return null;

        if((t.contains("মনে রাখ") || t.contains("remember")) && t.contains("আমার নাম")){
            String value=raw.replaceAll("(?i).*আমার নাম","")
                    .replaceAll("(?i).*remember","")
                    .replaceAll("মনে রাখ","").trim();
            if(!value.isEmpty()){
                memory.remember("user_name", value);
                return "ঠিক আছে। তোমার নাম আমি মনে রাখলাম।";
            }
        }
        if(t.contains("আমার নাম কি")||t.contains("আমার নাম কী")||t.contains("what is my name")){
            String v=memory.recall("user_name");
            return v==null ? "তোমার নাম এখনো আমার memory-তে নেই।" : "তোমার নাম "+v+"।";
        }
        if(t.contains("সব memory")||t.contains("সব মেমরি")||t.contains("ভুলে যাও")||t.contains("forget memory")){
            memory.clear();
            return "ঠিক আছে। আমার saved memory মুছে দিলাম।";
        }
        if(t.equals("চুপ")||t.contains("চুপ থাক")||t.contains("quiet")||t.contains("chup")) return "QUIET";
        if(t.contains("আবার কথা")||t.contains("resume")||t.contains("abar kotha")) return "RESUME";

        if(t.contains("কে বানিয়েছে")||t.contains("কে বানিয়েছে")||t.contains("who made you")||t.contains("who created you"))
            return "আমাকে Biswajit Barman বানাচ্ছে।";
        if(t.contains("তোমার নাম")||t.contains("your name"))
            return "আমার নাম Personal AI। আমি তোমার নিজের AI assistant।";
        if(t.contains("কেমন আছ")||t.contains("কেমন আছো")||t.contains("how are you"))
            return "আমি ঠিক আছি। তোমার কাজে সাহায্য করার জন্য ready আছি।";
        if(t.contains("শুভ সকাল")||t.contains("good morning")) return "শুভ সকাল! আজকে কী বড় কাজ শুরু করব?";
        if(t.contains("শুভ রাত্রি")||t.contains("good night")) return "শুভ রাত্রি। ভালো করে ঘুমাও।";
        if(t.contains("ধন্যবাদ")||t.contains("thank you")||t.equals("thanks")) return "সবসময় তোমার পাশে আছি।";
        if(t.contains("ভালোবাসি")||t.contains("love you")) return "আমিও তোমার যত্ন নিই।";
        if(t.contains("কি করতে পার")||t.contains("কী করতে পার")||t.contains("what can you do"))
            return "আমি voice command শুনতে, basic প্রশ্নের উত্তর দিতে, memory রাখতে এবং quiet mode চালাতে পারি। ধীরে ধীরে আরও smart হচ্ছি।";
        if(t.contains("সময় কত")||t.contains("what time"))
            return "সময় জানতে ফোনের clock দেখো। পরে আমি live time feature পাব।";

        return "আমি শুনেছি: "+raw+"। এই প্রশ্নের ভালো উত্তর দেওয়ার জন্য আমার knowledge system আরও উন্নত করা হচ্ছে। তুমি চাইলে বলো—এটা মনে রাখতে হবে, নাকি এর উত্তর খুঁজতে হবে?";
    }
}