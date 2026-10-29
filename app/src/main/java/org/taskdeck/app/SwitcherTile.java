package org.taskdeck.app;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class SwitcherTile extends TileService {
    @Override public void onStartListening() { getQsTile().setState(Tile.STATE_ACTIVE); getQsTile().updateTile(); }
    // Intent overload is used only on OS versions predating the PendingIntent overload.
    @android.annotation.SuppressLint("StartActivityAndCollapseDeprecated")
    @Override public void onClick() {
        Intent intent=new Intent(this,SwitcherActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if(Build.VERSION.SDK_INT>=34) startActivityAndCollapse(PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE));
        else startActivityAndCollapse(intent);
    }
}
