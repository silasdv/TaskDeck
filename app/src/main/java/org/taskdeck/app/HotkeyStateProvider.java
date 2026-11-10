package org.taskdeck.app;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

/** Private IPC endpoint in the shortcut process. The main process never writes its preferences. */
public final class HotkeyStateProvider extends ContentProvider {
    static final String DECK_CHORD="shortcutChord", DECK_ENABLED="shortcutEnabled";
    static final String SWAP_CHORD="quickSwitchChord", SWAP_ENABLED="quickSwitchEnabled";
    static final String CONNECTED="org.taskdeck.app.HOTKEY_CONNECTED";
    private static final String STORE="hotkey_settings";
    private static volatile IBinder recordingOwner;
    private static IBinder.DeathRecipient recordingDeath;

    static boolean isShortcutPreference(String key){
        return key==null || DECK_CHORD.equals(key) || DECK_ENABLED.equals(key)
                || SWAP_CHORD.equals(key) || SWAP_ENABLED.equals(key);
    }

    static SharedPreferences configuration(Context context){
        SharedPreferences stored=context.getSharedPreferences(STORE,Context.MODE_PRIVATE);
        // Before the first sync, an updated installation can still use its existing shortcuts.
        return stored.contains(DECK_CHORD)?stored:context.getSharedPreferences("settings",Context.MODE_PRIVATE);
    }

    static boolean isRecording(){
        IBinder owner=recordingOwner;
        return owner!=null && owner.isBinderAlive();
    }

    private static synchronized void setRecordingOwner(IBinder owner){
        if(recordingOwner==owner) return;
        if(recordingOwner!=null && recordingDeath!=null) recordingOwner.unlinkToDeath(recordingDeath,0);
        recordingOwner=owner;
        recordingDeath=null;
        if(owner==null) return;
        IBinder.DeathRecipient death=()->{
            synchronized(HotkeyStateProvider.class){
                if(recordingOwner==owner){
                    recordingOwner=null;
                    recordingDeath=null;
                }
            }
        };
        try {
            owner.linkToDeath(death,0);
            recordingDeath=death;
        } catch(RemoteException dead){
            recordingOwner=null;
        }
    }

    static void publish(Context context,String method,Bundle values){
        Uri uri=Uri.parse("content://"+context.getPackageName()+".hotkeys");
        context.getContentResolver().call(uri,method,null,values);
    }

    @Override public boolean onCreate(){ return true; }

    @Override public Bundle call(String method,String arg,Bundle values){
        if(values==null) throw new IllegalArgumentException("Missing shortcut state");
        if("configuration".equals(method)){
            getContext().getSharedPreferences(STORE,Context.MODE_PRIVATE).edit()
                    .putString(DECK_CHORD,values.getString(DECK_CHORD,""))
                    .putBoolean(DECK_ENABLED,values.getBoolean(DECK_ENABLED))
                    .putString(SWAP_CHORD,values.getString(SWAP_CHORD,""))
                    .putBoolean(SWAP_ENABLED,values.getBoolean(SWAP_ENABLED))
                    .apply();
        } else if("recording".equals(method)){
            setRecordingOwner(values.getBinder("owner"));
        } else throw new IllegalArgumentException("Unknown shortcut state operation");
        return Bundle.EMPTY;
    }

    @Override public Cursor query(Uri uri,String[] projection,String selection,String[] args,String order){ throw new UnsupportedOperationException(); }
    @Override public String getType(Uri uri){ return null; }
    @Override public Uri insert(Uri uri,ContentValues values){ throw new UnsupportedOperationException(); }
    @Override public int delete(Uri uri,String selection,String[] args){ throw new UnsupportedOperationException(); }
    @Override public int update(Uri uri,ContentValues values,String selection,String[] args){ throw new UnsupportedOperationException(); }
}
