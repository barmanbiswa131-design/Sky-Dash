package com.biswa.jarvis;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class JarvisAccessibilityService extends AccessibilityService {
    private static volatile JarvisAccessibilityService instance;

    @Override public void onServiceConnected(){ super.onServiceConnected(); instance=this; }
    @Override public void onAccessibilityEvent(AccessibilityEvent event){}
    @Override public void onInterrupt(){ instance=null; }

    public static boolean isConnected(){ return instance!=null; }

    public static boolean clickText(String text){
        JarvisAccessibilityService s=instance;
        if(s==null||text==null||text.trim().isEmpty())return false;
        AccessibilityNodeInfo root=s.getRootInActiveWindow();
        if(root==null)return false;
        boolean ok=clickRecursive(root,text.trim());
        root.recycle();
        return ok;
    }

    private static boolean clickRecursive(AccessibilityNodeInfo node,String text){
        if(node==null)return false;
        CharSequence label=node.getText(), desc=node.getContentDescription();
        boolean match=(label!=null&&text.equalsIgnoreCase(label.toString().trim()))
                ||(desc!=null&&text.equalsIgnoreCase(desc.toString().trim()));
        if(match&&node.isClickable()) return node.performAction(AccessibilityNodeInfo.ACTION_CLICK);

        for(int i=0;i<node.getChildCount();i++){
            AccessibilityNodeInfo child=node.getChild(i);
            if(child!=null){
                boolean ok=clickRecursive(child,text);
                child.recycle();
                if(ok)return true;
            }
        }
        return false;
    }

    public static boolean inputText(String text){
        JarvisAccessibilityService s=instance;
        if(s==null)return false;
        AccessibilityNodeInfo root=s.getRootInActiveWindow();
        if(root==null)return false;
        boolean ok=inputRecursive(root,text==null?"":text);
        root.recycle();
        return ok;
    }

    private static boolean inputRecursive(AccessibilityNodeInfo node,String text){
        if(node==null)return false;
        if(node.isEditable()){
            android.os.Bundle args=new android.os.Bundle();
            args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,text);
            if(node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,args))return true;
        }
        for(int i=0;i<node.getChildCount();i++){
            AccessibilityNodeInfo child=node.getChild(i);
            if(child!=null){
                boolean ok=inputRecursive(child,text);
                child.recycle();
                if(ok)return true;
            }
        }
        return false;
    }

    public static boolean longClickText(String text){
        JarvisAccessibilityService s=instance;
        if(s==null||text==null||text.trim().isEmpty())return false;
        AccessibilityNodeInfo root=s.getRootInActiveWindow();
        if(root==null)return false;
        boolean ok=longClickRecursive(root,text.trim());
        root.recycle();
        return ok;
    }

    private static boolean longClickRecursive(AccessibilityNodeInfo node,String text){
        if(node==null)return false;
        CharSequence label=node.getText(), desc=node.getContentDescription();
        String wanted=text.toLowerCase(java.util.Locale.ROOT);
        boolean match=(label!=null&&label.toString().trim().toLowerCase(java.util.Locale.ROOT).contains(wanted))
                ||(desc!=null&&desc.toString().trim().toLowerCase(java.util.Locale.ROOT).contains(wanted));
        if(match&&node.isLongClickable()) return node.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK);
        for(int i=0;i<node.getChildCount();i++){
            AccessibilityNodeInfo child=node.getChild(i);
            if(child!=null){
                boolean ok=longClickRecursive(child,text);
                child.recycle();
                if(ok)return true;
            }
        }
        return false;
    }

    public static boolean back(){
        return instance!=null && instance.performGlobalAction(GLOBAL_ACTION_BACK);
    }
    public static boolean home(){
        return instance!=null && instance.performGlobalAction(GLOBAL_ACTION_HOME);
    }
    public static boolean scroll(boolean up){
        JarvisAccessibilityService s=instance;
        if(s==null)return false;
        AccessibilityNodeInfo root=s.getRootInActiveWindow();
        if(root==null)return false;
        boolean ok=scrollRecursive(root,up);
        root.recycle();
        return ok;
    }
    private static boolean scrollRecursive(AccessibilityNodeInfo node,boolean up){
        if(node==null)return false;
        if(node.isScrollable()){
            int action=up?AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD:AccessibilityNodeInfo.ACTION_SCROLL_FORWARD;
            if(node.performAction(action))return true;
        }
        for(int i=0;i<node.getChildCount();i++){
            AccessibilityNodeInfo child=node.getChild(i);
            if(child!=null){
                boolean ok=scrollRecursive(child,up);
                child.recycle();
                if(ok)return true;
            }
        }
        return false;
    }
}