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