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

        if((contains(t,"याद रख","yaad rakh","remember")) && (contains(t,"मेरा नाम","mera naam","my name"))){
            String value=extractAfterAny(raw,"मेरा नाम","mera naam","my name");
            if(!value.isEmpty()){memory.remember("identity","user_name",cleanMemoryValue(value));return finish(raw,"ठीक है। तुम्हारा नाम memory में save कर लिया।");}
        }
        if((contains(t,"याद रख","yaad rakh","remember")) && (contains(t,"पसंद","pasand","favorite"))){
            String value=extractAfterAny(raw,"पसंद","pasand","favorite");
            if(!value.isEmpty()){memory.remember("preference","user_preference",cleanMemoryValue(value));return finish(raw,"ठीक है। तुम्हारी पसंद memory में save कर ली।");}
        }
        if((contains(t,"याद रख","yaad rakh","remember")) && contains(t,"संध्या","sandhya")){
            memory.remember("people","important_person",raw);
            return finish(raw,"ठीक है। यह बात important memory में save कर ली।");
        }

        if(contains(t,"मेरा नाम क्या","mera naam kya","what is my name")){
            String v=memory.recall("user_name");
            return finish(raw,v==null?"तुम्हारा नाम अभी memory में नहीं है।":"तुम्हारा नाम "+v+" है।");
        }
        if(contains(t,"मुझे क्या पसंद","मेरी पसंद","meri pasand","my preference")){
            String v=memory.recall("user_preference");
            return finish(raw,v==null?"तुम्हारी पसंद अभी save नहीं है।":"तुमने कहा था: "+v+"।");
        }
        if(contains(t,"क्या याद है","क्या याद रखा","tumhe kya yaad","what do you remember")){
            String m=memory.memories();
            return finish(raw,m.isEmpty()?"अभी कोई personal memory नहीं है।":"मेरी memory में है:
"+m);
        }
        if(contains(t,"पिछली बात","पहले क्या कहा","pichli baat","previous conversation")){
            String log=memory.conversation();
            return finish(raw,log.isEmpty()?"हमारी कोई पुरानी conversation नहीं है।":"हाल की conversation:
"+log);
        }
        if(contains(t,"सब भूल जाओ","सब memory","सब कुछ भूल","forget memory","bhool jao")){
            memory.clear(); return "ठीक है। मेरी personal memory और conversation history मिटा दी।";
        }

        if(t.equals("चुप")||contains(t,"चुप रह","चुप हो जाओ","quiet","chup raho","chup")){
            return "QUIET";
        }
        if(contains(t,"फिर से बात","फिर बात करो","resume","dobara bolo","phir se baat")){
            return "RESUME";
        }
        if(contains(t,"front camera","front camera dekho","मुझे देखो","mujhe dekho","mera face dekho")) return "CAMERA:front";
        if(contains(t,"rear camera","back camera","piche camera","पीछे camera","camera se dekho","camera se dekh")) return "CAMERA:rear";
        if(contains(t,"instagram kholo","instagram खोलो","instagram open","open instagram","instagram khol")) return "PHONE:open_app:Instagram";
        if(contains(t,"whatsapp kholo","whatsapp खोलो","whatsapp open","open whatsapp","whatsapp khol")) return "PHONE:open_app:WhatsApp";
        if(contains(t,"facebook kholo","facebook खोलो","facebook open","open facebook")) return "PHONE:open_app:Facebook";
        if(contains(t,"youtube kholo","youtube खोलो","youtube open","open youtube")) return "PHONE:open_app:YouTube";
        if(contains(t,"telegram kholo","telegram खोलो","telegram open","open telegram")) return "PHONE:open_app:Telegram";
        if(contains(t,"search","search e","search in","খুঁজে","খোজ","search karo","search kar")){
            String q=extractSearchQuery(raw);
            if(!q.isEmpty()) return "PHONE:search:"+q;
        }

        if(contains(t,"back jao","पीछे जाओ","back karo","go back")) return "PHONE:back";
        if(contains(t,"home jao","home kholo","home screen","go home")) return "PHONE:home";
        if(contains(t,"neeche scroll","नीचे scroll","scroll down")) return "PHONE:scroll_down";
        if(contains(t,"upar scroll","ऊपर scroll","scroll up")) return "PHONE:scroll_up";
        if(contains(t,"पूरी तरह बंद","बंद हो जाओ","बंद हो जा","ai बंद","ai band","band ho jao","band ho ja","shutdown","stop ai")){
            return "STOP";
        }

        if(contains(t,"समय क्या","अभी कितने बजे","time kya","what time"))
            return finish(raw,"अभी "+new SimpleDateFormat("h:mm a",Locale.ENGLISH).format(new Date())+" है।");
        if(contains(t,"आज की तारीख","आज कितनी तारीख","aaj ki date","what date"))
            return finish(raw,"आज "+new SimpleDateFormat("dd MMMM yyyy",new Locale("hi","IN")).format(new Date())+" है।");

        if(isMath(t)){
            String ans=calculate(t);
            if(ans!=null)return finish(raw,"जवाब: "+ans);
        }

        if(contains(t,"किसने बनाया","तुम्हें किसने बनाया","who made you","kisne banaya"))
            return finish(raw,"मुझे Biswajit Barman बना रहे हैं।");
        if(contains(t,"तुम्हारा नाम","आपका नाम","your name","tumhara naam"))
            return finish(raw,"मेरा नाम Personal AI है। मैं तुम्हारा अपना AI assistant हूँ।");
        if(contains(t,"कैसे हो","कैसी हो","how are you","kaise ho"))
            return finish(raw,"मैं ठीक हूँ। तुम्हारी मदद करने के लिए ready हूँ।");
        if(contains(t,"सुप्रभात","good morning","suprabhat"))
            return finish(raw,"सुप्रभात! आज कुछ अच्छा करते हैं।");
        if(contains(t,"शुभ रात्रि","good night","shubh ratri"))
            return finish(raw,"शुभ रात्रि। अच्छी नींद लो।");
        if(contains(t,"धन्यवाद","thank you","thanks","shukriya"))
            return finish(raw,"हमेशा।");
        if(contains(t,"क्या कर सकते हो","क्या कर सकते","what can you do","kya kar sakte ho"))
            return finish(raw,"मैं voice commands, personal memory, conversation history, time/date, calculations और local AI brain संभाल सकता हूँ।");

        return "__LOCAL_LLM__";
    }

    public String systemPrompt(){
        return "तुम Personal AI हो। तुम Biswajit के निजी assistant हो। सम्मान से Sir कह सकते हो, लेकिन हर वाक্যে Sir मत बोलो। "+
               "Hindi, Hinglish, Bangla और English বুঝো। छोटे, natural और useful जवाब दो। बिना जरूरत लंबा lecture मत दो। "+
               "নিজেকে ChatGPT/Gemini বলবে না। তুমি movie JARVIS-এর copy নও; তোমার নিজের personality আছে। "+
               "তুমি যা নিশ্চিত জানো না, তা বানিয়ে বলবে না।";
    }

    public String buildPrompt(String userText){
        String mem=memory.memories();
        String recent=memory.conversation();
        StringBuilder p=new StringBuilder();
        if(!mem.isEmpty())p.append("Relevant personal memory:
").append(mem).append("

");
        if(!recent.isEmpty())p.append("Recent conversation:
").append(recent).append("

");
        p.append("User said:
").append(userText);
        return p.toString();
    }

    public void saveLLMAnswer(String userText,String answer){
        memory.addConversation(userText,answer);
    }

    private boolean contains(String t,String... words){for(String w:words)if(t.contains(w.toLowerCase(Locale.ROOT)))return true;return false;}
    private String finish(String user,String answer){ memory.addConversation(user,answer); return answer; }

    private String extractAfterAny(String raw,String... markers){
        String best="";
        for(String m:markers){
            int i=raw.toLowerCase(Locale.ROOT).lastIndexOf(m.toLowerCase(Locale.ROOT));
            if(i>=0){String v=raw.substring(i+m.length()).replace("याद रख","").replace("yaad rakh","").replace("remember","").trim();if(v.length()>best.length())best=v;}
        }
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
    private String format(double n){if(Double.isInfinite(n)||Double.isNaN(n))return "हिसाब नहीं हो पाया";if(n==(long)n)return String.valueOf((long)n);return String.valueOf(n);}
}