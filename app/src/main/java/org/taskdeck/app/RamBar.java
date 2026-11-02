package org.taskdeck.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.widget.ProgressBar;

/** A bounded RAM bar without platform-dependent drawable insets. */
final class RamBar extends ProgressBar {
    private final int fill,track;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    RamBar(Context context,int fill,int track){super(context,null,android.R.attr.progressBarStyleHorizontal);this.fill=fill;this.track=track;setIndeterminate(false);setPadding(0,0,0,0);}
    @Override protected synchronized void onDraw(Canvas canvas){
        float width=getWidth(),height=getHeight(),radius=height/2;
        paint.setColor(track);canvas.drawRoundRect(0,0,width,height,radius,radius,paint);
        float fraction=getMax()>0?Math.max(0,Math.min(1,getProgress()/(float)getMax())):0;
        if(fraction>0){paint.setColor(fill);canvas.drawRoundRect(0,0,width*fraction,height,radius,radius,paint);}
    }
}
