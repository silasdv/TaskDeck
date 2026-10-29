package org.taskdeck.app;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

/** Partial combinations remain available to games. No screen or typed-text history. */
public class HotkeyService extends AccessibilityService {
    private final ChordGate gate=new ChordGate();
    @Override protected boolean onKeyEvent(KeyEvent event) {
        if(event.getAction()!=KeyEvent.ACTION_DOWN && event.getAction()!=KeyEvent.ACTION_UP) return false;
        if(!ShortcutKeys.eligible(event)) return false;
        android.content.SharedPreferences prefs=getSharedPreferences("settings",MODE_PRIVATE);
        gate.configure(ShortcutKeys.decode(prefs.getString("shortcutChord","")));
        ChordGate.Result result=gate.update(event.getDeviceId(),ShortcutKeys.id(event),event.getAction()==KeyEvent.ACTION_DOWN,event.getRepeatCount(),prefs.getBoolean("shortcutEnabled",false) && !TaskDeckApp.recordingShortcut);
        if(result==ChordGate.Result.TRIGGER){
            try { startActivity(new Intent(this,SwitcherActivity.class).putExtra("controllerEntry",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP)); }
            catch(RuntimeException ignored){ /* Still consume the complete captured down/up pair. */ }
        }
        return result!=ChordGate.Result.PASS;
    }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { gate.clear(); }
    @Override public void onDestroy() { gate.clear(); super.onDestroy(); }
}
