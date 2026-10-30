package org.taskdeck.app;

import android.graphics.*;
import android.graphics.drawable.Drawable;

/** The color itself is the control: no rectangular swatch background. */
public final class AccentDot extends Drawable {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG); private final int color,ink; private final float density;
    private boolean selected,focused;
    public AccentDot(int color,int ink,float density){ this.color=color; this.ink=ink; this.density=density; }
    @Override public boolean isStateful(){ return true; }
    @Override protected boolean onStateChange(int[] states){ selected=false; focused=false; for(int state:states){ selected|=state==android.R.attr.state_selected; focused|=state==android.R.attr.state_focused||state==android.R.attr.state_activated||state==android.R.attr.state_pressed; } invalidateSelf(); return true; }
    @Override public void draw(Canvas canvas){ float x=getBounds().exactCenterX(),y=getBounds().exactCenterY(); paint.setStyle(Paint.Style.FILL); paint.setColor(color); canvas.drawCircle(x,y,9*density,paint); paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(density); paint.setColor(ink); canvas.drawCircle(x,y,9*density,paint); if(selected||focused){ paint.setStrokeWidth((focused?2:1)*density); canvas.drawCircle(x,y,(focused?12:11)*density,paint); } }
    @Override public void setAlpha(int value){ paint.setAlpha(value); }
    @Override public void setColorFilter(ColorFilter value){ paint.setColorFilter(value); }
    @Override public int getOpacity(){ return PixelFormat.TRANSLUCENT; }
}
