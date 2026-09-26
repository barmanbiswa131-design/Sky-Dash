package com.biswa.jarvis;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class JarvisAccessibilityService extends AccessibilityService {
    private static volatile JarvisAccessibilityService instance;

    @Override public void onServiceConnected(){
        super.onServiceConnected();
        instance=this;
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event){}

    @Override public void onInterrupt(){}

    public static boolean isConnected(){
        return instance!=null;
    }

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

        CharSequence label=node.getText();
        CharSequence desc=node.getContentDescription();

        if((label!=null && text.equalsIgnoreCase(label.toString().trim())) ||
           (desc!=null && text.equalsIgnoreCase(desc.toString().trim()))){
            if(node.isClickable()){
                return node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }

        for(int i=0;i<node.getChildCount();i++){
            AccessibilityNodeInfo child=node.getChild(i);
            if(child!=null){
                if(clickRecursive(child,text)){
                    child.recycle();
                    return true;
                }
                child.recycle();
            }
        }
        return false;
    }
}
