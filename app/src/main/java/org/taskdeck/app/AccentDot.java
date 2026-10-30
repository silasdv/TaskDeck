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
    @Override public void draw(Canvas canvas){ float x=getBounds().exactCenterX(),y=getBounds().exactCenterY(),radius=Math.max(1,Math.min(18*density,Math.min(getBounds().width(),getBounds().height())/2f-2*density)); paint.setStyle(Paint.Style.FILL); paint.setColor(color); canvas.drawCircle(x,y,radius,paint); paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(density); paint.setColor(ink); canvas.drawCircle(x,y,radius,paint); if(selected){ paint.setStrokeWidth(2*density); canvas.drawCircle(x,y,Math.max(1,radius-4*density),paint); } if(focused){ paint.setStrokeWidth(2*density); canvas.drawCircle(x,y,radius+density,paint); } }

    @Override public void setAlpha(int value){ paint.setAlpha(value); }
    @Override public void setColorFilter(ColorFilter value){ paint.setColorFilter(value); }
    @Override public int getOpacity(){ return PixelFormat.TRANSLUCENT; }
}
