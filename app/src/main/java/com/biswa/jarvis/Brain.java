package com.biswa.jarvis;

import android.content.Context;
import java.text.SimpleDateFormat;
import java.util.*;

public class Brain {
    private final MemoryStore memory;
    public Brain(Context c){ memory=new MemoryStore(c); }

    public String reply(String input){
        String raw=input==null?"":input.trim();
        String t=raw.toLowerCase(Locale.ROOT);
        if(t.isEmpty()) return null;

        if((t.contains("মনে রাখ")||t.contains("remember"))&&t.contains("আমার নাম")){
            String value=extractAfter(raw,"আমার নাম");
            if(!value.isEmpty()){memory.remember("identity","user_name",cleanMemoryValue(value));return finish(raw,"ঠিক আছে। তোমার নাম memory-তে রাখলাম।");}
        }
        if((t.contains("মনে রাখ")||t.contains("remember"))&&(t.contains("পছন্দ")||t.contains("ভালো লাগে")||t.contains("favorite"))){
            String value=extractAfterAny(raw,"পছন্দ","ভালো লাগে","favorite");
            if(!value.isEmpty()){memory.remember("preference","user_preference",cleanMemoryValue(value));return finish(raw,"ঠিক আছে। তোমার পছন্দটা memory-তে রাখলাম।");}
        }
        if((t.contains("মনে রাখ")||t.contains("remember"))&&(t.contains("স্যান্ডহ্যা")||t.contains("sandhya"))){
            memory.remember("people","important_person",raw);
            return finish(raw,"ঠিক আছে। এই কথাটা important memory-তে রাখলাম।");
        }

        if(t.contains("আমার নাম কি")||t.contains("আমার নাম কী")||t.contains("what is my name")){
            String v=memory.recall("user_name");
            return finish(raw,v==null?"তোমার নাম এখনো memory-তে নেই।":"তোমার নাম "+v+"।");
        }
        if(t.contains("আমি কি পছন্দ করি")||t.contains("আমার পছন্দ কি")||t.contains("my preference")){
            String v=memory.recall("user_preference");
            return finish(raw,v==null?"তোমার পছন্দ এখনো save করা নেই।":"তুমি বলেছিলে: "+v+"।");
        }
        if(t.contains("কি মনে রেখেছ")||t.contains("কী মনে রেখেছ")||t.contains("what do you remember")){
            String m=memory.memories();
            return finish(raw,m.isEmpty()?"এখনো কোনো personal memory নেই।":"আমার memory-তে আছে:\n"+m);
        }
        if(t.contains("আগের কথা")||t.contains("previous conversation")||t.contains("কি বলেছিলাম")||t.contains("কী বলেছিলাম")){
            String log=memory.conversation();
            return finish(raw,log.isEmpty()?"আমাদের কোনো পুরোনো conversation নেই।":"সাম্প্রতিক conversation:\n"+log);
        }
        if(t.contains("সব memory")||t.contains("সব মেমরি")||t.contains("সবকিছু ভুলে")||t.contains("ভুলে যাও")||t.contains("forget memory")){
            memory.clear(); return "ঠিক আছে। আমার personal memory এবং conversation history মুছে দিলাম।";
        }

        if(t.equals("চুপ")||t.contains("চুপ থাক")||t.contains("quiet")||t.contains("chup")) return "QUIET";
        if(t.contains("আবার কথা")||t.contains("resume")||t.contains("abar kotha")) return "RESUME";

        if(t.contains("সময় কত")||t.contains("সময় কত")||t.contains("what time"))
            return finish(raw,"এখন "+new SimpleDateFormat("h:mm a",Locale.ENGLISH).format(new Date())+"।");
        if(t.contains("আজকের তারিখ")||t.contains("আজ কত তারিখ")||t.contains("today's date")||t.contains("what date"))
            return finish(raw,"আজ "+new SimpleDateFormat("dd MMMM yyyy",Locale.ENGLISH).format(new Date())+"।");

        if(isMath(t)){
            String ans=calculate(t);
            if(ans!=null)return finish(raw,"উত্তর: "+ans);
        }

        if(t.contains("কে বানিয়েছে")||t.contains("কে বানিয়েছে")||t.contains("who made you")||t.contains("who created you"))
            return finish(raw,"আমাকে Biswajit Barman বানাচ্ছে।");
        if(t.contains("তোমার নাম")||t.contains("your name"))
            return finish(raw,"আমার নাম Personal AI। আমি তোমার নিজের AI assistant।");
        if(t.contains("কেমন আছ")||t.contains("কেমন আছো")||t.contains("how are you"))
            return finish(raw,"আমি ঠিক আছি। তোমার কাজে সাহায্য করার জন্য ready আছি।");
        if(t.contains("শুভ সকাল")||t.contains("good morning"))
            return finish(raw,"শুভ সকাল! আজকে বড় কিছু করি।");
        if(t.contains("শুভ রাত্রি")||t.contains("good night"))
            return finish(raw,"শুভ রাত্রি। ভালো করে ঘুমাও।");
        if(t.contains("ধন্যবাদ")||t.contains("thank you")||t.equals("thanks"))
            return finish(raw,"সবসময়।");
        if(t.contains("কি করতে পার")||t.contains("কী করতে পার")||t.contains("what can you do"))
            return finish(raw,"আমি voice command, personal memory, conversation history, time/date, basic calculations এবং quiet mode handle করতে পারি।");

        return finish(raw,"আমি শুনেছি: "+raw+"। এই বিষয়ে এখনো আমার local knowledge engine-এ যথেষ্ট তথ্য নেই।");
    }

    private String finish(String user,String answer){ memory.addConversation(user,answer); return answer; }

    private String extractAfter(String raw,String marker){
        int i=raw.toLowerCase(Locale.ROOT).lastIndexOf(marker.toLowerCase(Locale.ROOT));
        return i<0?"":raw.substring(i+marker.length()).replace("মনে রাখ","").replace("remember","").trim();
    }
    private String extractAfterAny(String raw,String... markers){
        String best="";
        for(String m:markers){String v=extractAfter(raw,m);if(v.length()>best.length())best=v;}
        return best;
    }
    private String cleanMemoryValue(String v){return v.replaceAll("^[ :,-]+","").replaceAll("[.!]+$","").trim();}

    private boolean isMath(String t){
        return t.matches(".*\\d+\\s*[+\\-*/x×÷]\\s*\\d+.*") || t.matches("^[0-9+\\-*/(). x×÷]+$");
    }
    private String calculate(String t){
        try{
            String e=t.replace("x","*").replace("×","*").replace("÷","/").replaceAll("[^0-9+\\-*/().]","");
            if(e.isEmpty())return null;
            return format(eval(e));
        }catch(Exception e){return null;}
    }
    private double eval(String s){
        return new Object(){int p=-1,c;
            void next(){c=++p<s.length()?s.charAt(p):-1;}
            double parse(){next();double x=parseExpr();if(p<s.length())throw new RuntimeException();return x;}
            double parseExpr(){double x=parseTerm();for(;;){if(c=='+'){next();x+=parseTerm();}else if(c=='-'){next();x-=parseTerm();}else return x;}}
            double parseTerm(){double x=parseFactor();for(;;){if(c=='*'){next();x*=parseFactor();}else if(c=='/'){next();x/=parseFactor();}else return x;}}
            double parseFactor(){if(c=='+'){next();return parseFactor();}if(c=='-'){next();return -parseFactor();}double x;int st=p;if(c=='('){next();x=parseExpr();if(c!=')')throw new RuntimeException();next();return x;}while((c>='0'&&c<='9')||c=='.')next();x=Double.parseDouble(s.substring(st,p));return x;}
        }.parse();
    }
    private String format(double n){if(Double.isInfinite(n)||Double.isNaN(n))return "হিসাব করা যায়নি";if(n==(long)n)return String.valueOf((long)n);return String.valueOf(n);}
}