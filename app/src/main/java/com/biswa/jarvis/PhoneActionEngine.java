package com.biswa.jarvis;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;

public class PhoneActionEngine {
    private final Context context;
    public PhoneActionEngine(Context c){ context=c.getApplicationContext(); }

    public String execute(String command){
        String c=command==null?"":command.trim().toLowerCase();
        if(c.startsWith("open_app:")){
            String name=command.substring(command.indexOf(':')+1).trim();
            return openApp(name);
        }
        if(c.startsWith("type:")){
            String text=command.substring(command.indexOf(':')+1).trim();
            return JarvisAccessibilityService.inputText(text)
                    ? "জি Sir, text লিখে দিয়েছি।"
                    : "Sir, কোনো editable text box এখন পাইনি।";
        }
        if(c.startsWith("long_click:")){
            String text=command.substring(command.indexOf(':')+11).trim();
            return JarvisAccessibilityService.longClickText(text)
                    ? "জি Sir, long click করেছি।"
                    : "Sir, ওই text এখন screen-এ পাইনি।";
        }
        if(c.startsWith("click:")){
            String text=command.substring(command.indexOf(':')+1).trim();
            return JarvisAccessibilityService.clickText(text)
                    ? "জি Sir, আমি \"" + text + "\"-এ click করেছি।"
                    : "Sir, \"" + text + "\" button/text এখন screen-এ পাইনি।";
        }
        if(c.equals("back")) return JarvisAccessibilityService.back()
                ? "জি Sir, back করেছি।" : "Sir, back করা যায়নি।";
        if(c.equals("home")) return JarvisAccessibilityService.home()
                ? "জি Sir, home screen-এ গেছি।" : "Sir, home করা যায়নি।";
        if(c.equals("scroll_down")) return JarvisAccessibilityService.scroll(false)
                ? "জি Sir, নিচে scroll করেছি।" : "Sir, scroll করা যায়নি।";
        if(c.equals("scroll_up")) return JarvisAccessibilityService.scroll(true)
                ? "জি Sir, উপরে scroll করেছি।" : "Sir, scroll করা যায়নি।";
        return null;
    }

    private String openApp(String name){
        PackageManager pm=context.getPackageManager();
        String n=name.toLowerCase();
        String[] pkgs;
        if(n.contains("instagram")) pkgs=new String[]{"com.instagram.android"};
        else if(n.contains("whatsapp")) pkgs=new String[]{"com.whatsapp"};
        else if(n.contains("facebook")) pkgs=new String[]{"com.facebook.katana"};
        else if(n.contains("youtube")) pkgs=new String[]{"com.google.android.youtube"};
        else if(n.contains("telegram")) pkgs=new String[]{"org.telegram.messenger"};
        else return "Sir, এই app-এর package আমি এখনো add করিনি।";

        for(String pkg:pkgs){
            Intent i=pm.getLaunchIntentForPackage(pkg);
            if(i!=null){
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
                return "জি Sir, "+name+" খুলছি।";
            }
        }
        return "Sir, "+name+" ফোনে install করা নেই।";
    }
}