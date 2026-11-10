package org.taskdeck.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.ResultReceiver;

/** Dispatches an explicit, app-private Quick Swap request to the Shizuku client process. */
public final class QuickSwapReceiver extends BroadcastReceiver {
    static final String REPLY="quickSwapReply";

    @Override public void onReceive(Context context,Intent intent){
        ResultReceiver reply=intent.getParcelableExtra(REPLY);
        ((TaskDeckApp)context.getApplicationContext()).quickSwitchFromShortcut(reply);
    }
}
