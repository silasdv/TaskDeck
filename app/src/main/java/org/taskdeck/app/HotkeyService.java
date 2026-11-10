package org.taskdeck.app;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

/** Partial combinations remain available to games. No screen or typed-text history. */
public class HotkeyService extends AccessibilityService {
    private final ChordGate gate=new ChordGate();
    private final ChordGate quickGate=new ChordGate();
    private int lastWindowId=-1;

    @Override protected void onServiceConnected(){
        super.onServiceConnected();
        lastWindowId=-1;
        gate.clear();
        quickGate.clear();
        sendBroadcast(new Intent(HotkeyStateProvider.CONNECTED).setPackage(getPackageName()));
    }
    @Override protected boolean onKeyEvent(KeyEvent event) {
        if(event.getAction()!=KeyEvent.ACTION_DOWN && event.getAction()!=KeyEvent.ACTION_UP) return false;
        if(!ShortcutKeys.eligible(event)) return false;
        android.content.SharedPreferences prefs=HotkeyStateProvider.configuration(this);
        java.util.Set<String> deck=ShortcutKeys.decode(prefs.getString("shortcutChord","")),quick=ShortcutKeys.decode(prefs.getString("quickSwitchChord",""));
        gate.configure(deck); quickGate.configure(quick);
        boolean recording=HotkeyStateProvider.isRecording();
        ChordGate.Result result=gate.update(event.getDeviceId(),ShortcutKeys.id(event),event.getAction()==KeyEvent.ACTION_DOWN,event.getRepeatCount(),prefs.getBoolean("shortcutEnabled",false) && !recording);
        ChordGate.Result direct=quickGate.update(event.getDeviceId(),ShortcutKeys.id(event),event.getAction()==KeyEvent.ACTION_DOWN,event.getRepeatCount(),prefs.getBoolean("quickSwitchEnabled",false)&&!recording&&!ShortcutKeys.conflicts(deck,quick));
        if(result==ChordGate.Result.TRIGGER){
            try { startActivity(new Intent(this,SwitcherActivity.class).putExtra("controllerEntry",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP)); }
            catch(RuntimeException failure){ Log.w("TaskDeckHotkey","Unable to open TaskDeck from the controller shortcut",failure); }
        }
        else if(direct==ChordGate.Result.TRIGGER) requestQuickSwap();
        return result!=ChordGate.Result.PASS||direct!=ChordGate.Result.PASS;
    }

    private void requestQuickSwap(){
        // HOME must be launched by the system-bound accessibility service.
        ResultReceiver reply=new ResultReceiver(new Handler(Looper.getMainLooper())){
            @Override protected void onReceiveResult(int code,Bundle data){
                if(code!=1 || data==null || !data.containsKey("package")) return;
                try {
                    startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                            .setPackage(data.getString("package")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                } catch(RuntimeException denied){
                    android.widget.Toast.makeText(HotkeyService.this,"Android blocked the HOME switch",android.widget.Toast.LENGTH_LONG).show();
                }
            }
        };
        sendBroadcast(new Intent(this,QuickSwapReceiver.class).putExtra(QuickSwapReceiver.REPLY,reply));
    }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if(event.getEventType()!=AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;
        int windowId=event.getWindowId();
        if(windowId<0 || windowId==lastWindowId) return;
        if(lastWindowId>=0){
            gate.resetHeldKeys();
            quickGate.resetHeldKeys();
        }
        lastWindowId=windowId;
    }
    @Override public void onInterrupt() { gate.clear(); quickGate.clear(); }
    @Override public void onDestroy() { gate.clear(); quickGate.clear(); super.onDestroy(); }
}
