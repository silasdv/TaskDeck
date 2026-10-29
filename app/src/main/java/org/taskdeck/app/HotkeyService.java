package org.taskdeck.app;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

/** No UI-content access and no keystroke history. Intercepts only the opt-in shortcut. */
public class HotkeyService extends AccessibilityService {
    private final java.util.Set<Long> consumed=new java.util.HashSet<>();
    @Override protected boolean onKeyEvent(KeyEvent event) {
        android.content.SharedPreferences prefs=getSharedPreferences("settings",MODE_PRIVATE);
        long token=((long)event.getDeviceId()<<32)|(event.getKeyCode()&0xffffffffL);
        if(event.getAction()==KeyEvent.ACTION_UP) return consumed.remove(token);
        if(event.getAction()==KeyEvent.ACTION_DOWN && consumed.contains(token)) return true;
        if(event.getAction()==KeyEvent.ACTION_DOWN && event.getRepeatCount()>0) return false;
        if(!prefs.getBoolean("shortcutEnabled",false) || event.getKeyCode()!=prefs.getInt("shortcut",KeyEvent.KEYCODE_BUTTON_MODE)) return false;
        if(event.getAction()==KeyEvent.ACTION_DOWN) {
            if(consumed.add(token) && event.getRepeatCount()==0) {
                try { startActivity(new Intent(this,SwitcherActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP)); }
                catch(RuntimeException ignored) { consumed.remove(token); return false; }
            }
            return true;
        }
        return false;
    }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { consumed.clear(); }
    @Override public void onDestroy() { consumed.clear(); super.onDestroy(); }
}
