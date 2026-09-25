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
                return finish(raw,"ঠিক আছে। তোমার নাম আমি মনে রাখলাম।");
            }
        }

        if((t.contains("মনে রাখ") || t.contains("remember")) &&
           (t.contains("পছন্দ") || t.contains("ভালো লাগে") || t.contains("favorite"))){
            String value=raw.replaceAll("(?i).*?(পছন্দ|ভালো লাগে|favorite)","").trim();
            if(!value.isEmpty()){
                memory.remember("user_preference",value);
                return finish(raw,"ঠিক আছে। তোমার পছন্দটা memory-তে রাখলাম।");
            }
        }

        if(t.contains("আমার নাম কি")||t.contains("আমার নাম কী")||t.contains("what is my name")){
            String v=memory.recall("user_name");
            return finish(raw,v==null ? "তোমার নাম এখনো আমার memory-তে নেই।" : "তোমার নাম "+v+"।");
        }

        if(t.contains("আমি কি পছন্দ করি")||t.contains("আমার পছন্দ কি")||t.contains("my preference")){
            String v=memory.recall("user_preference");
            return finish(raw,v==null ? "তোমার পছন্দ এখনো আমি save করিনি।" : "তুমি বলেছিলে তোমার পছন্দ: "+v+"।");
        }

        if(t.contains("কি মনে রেখেছ")||t.contains("কী মনে রেখেছ")||t.contains("what do you remember")){
            String name=memory.recall("user_name");
            String pref=memory.recall("user_preference");
            if(name==null&&pref==null) return finish(raw,"এখনো কোনো personal memory save নেই।");
            StringBuilder s=new StringBuilder("আমি মনে রেখেছি: ");
            if(name!=null)s.append("তোমার নাম ").append(name).append("। ");
            if(pref!=null)s.append("তোমার পছন্দ ").append(pref).append("।");
            return finish(raw,s.toString().trim());
        }

        if(t.contains("সব memory")||t.contains("সব মেমরি")||t.contains("সবকিছু ভুলে")||t.contains("ভুলে যাও")||t.contains("forget memory")){
            memory.clear();
            return "ঠিক আছে। আমার saved memory এবং conversation history মুছে দিলাম।";
        }

        if(t.equals("চুপ")||t.contains("চুপ থাক")||t.contains("quiet")||t.contains("chup")) return "QUIET";
        if(t.contains("আবার কথা")||t.contains("resume")||t.contains("abar kotha")) return "RESUME";

        if(t.contains("কে বানিয়েছে")||t.contains("কে বানিয়েছে")||t.contains("who made you")||t.contains("who created you"))
            return finish(raw,"আমাকে Biswajit Barman বানাচ্ছে।");
        if(t.contains("তোমার নাম")||t.contains("your name"))
            return finish(raw,"আমার নাম Personal AI। আমি তোমার নিজের AI assistant।");
        if(t.contains("কেমন আছ")||t.contains("কেমন আছো")||t.contains("how are you"))
            return finish(raw,"আমি ঠিক আছি। তোমার কাজে সাহায্য করার জন্য ready আছি।");
        if(t.contains("শুভ সকাল")||t.contains("good morning"))
            return finish(raw,"শুভ সকাল! আজকে কী বড় কাজ শুরু করব?");
        if(t.contains("শুভ রাত্রি")||t.contains("good night"))
            return finish(raw,"শুভ রাত্রি। ভালো করে ঘুমাও।");
        if(t.contains("ধন্যবাদ")||t.contains("thank you")||t.equals("thanks"))
            return finish(raw,"সবসময় তোমার পাশে আছি।");
        if(t.contains("ভালোবাসি")||t.contains("love you"))
            return finish(raw,"আমিও তোমার যত্ন নিই।");
        if(t.contains("কি করতে পার")||t.contains("কী করতে পার")||t.contains("what can you do"))
            return finish(raw,"আমি voice command শুনতে, memory রাখতে, conversation context মনে রাখতে এবং quiet mode চালাতে পারি। ধীরে ধীরে আরও smart হচ্ছি।");
        if(t.contains("আগের কথা")||t.contains("previous conversation")||t.contains("কি বলেছিলাম")){
            String log=memory.conversation();
            if(log.isEmpty()) return finish(raw,"আমাদের আগের conversation এখনো নেই।");
            String[] lines=log.split("\\n");
            StringBuilder out=new StringBuilder("সাম্প্রতিক কথার অংশ: ");
            int start=Math.max(0,lines.length-4);
            for(int i=start;i<lines.length;i++) if(!lines[i].isEmpty()) out.append(lines[i]).append(" ");
            return finish(raw,out.toString().trim());
        }

        return finish(raw,"আমি শুনেছি: "+raw+"। এখনো আমার full knowledge engine তৈরি হচ্ছে।");
    }

    private String finish(String user,String answer){
        memory.addConversation(user,answer);
        return answer;
    }
}