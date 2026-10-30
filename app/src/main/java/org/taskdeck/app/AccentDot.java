package org.taskdeck.app;

import android.graphics.*;
import android.graphics.drawable.Drawable;

/** Swatch boxes use the active accent; focus and stored selection remain distinct. */
public final class AccentDot extends Drawable {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int color,accent,surface,selectedFill; private final float density;
    private boolean selected,focused;
    public AccentDot(int color,int accent,int surface,int selectedFill,float density){ this.color=color;this.accent=accent;this.surface=surface;this.selectedFill=selectedFill;this.density=density; }
    @Override public boolean isStateful(){ return true; }
    @Override protected boolean onStateChange(int[] states){ selected=false;focused=false;for(int state:states){selected|=state==android.R.attr.state_selected;focused|=state==android.R.attr.state_focused||state==android.R.attr.state_activated||state==android.R.attr.state_pressed;}invalidateSelf();return true; }
    @Override public void draw(Canvas canvas){
        RectF box=new RectF(getBounds());box.inset(2*density,2*density);paint.setStyle(Paint.Style.FILL);paint.setColor(selected?selectedFill:surface);canvas.drawRoundRect(box,10*density,10*density,paint);
        if(selected||focused){paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth((focused?3:1)*density);paint.setColor(accent);canvas.drawRoundRect(box,10*density,10*density,paint);}
        paint.setStyle(Paint.Style.FILL);paint.setColor(color);canvas.drawCircle(box.centerX(),box.centerY(),Math.max(1,Math.min(18*density,Math.min(box.width(),box.height())/2-5*density)),paint);
    }
    @Override public void setAlpha(int value){paint.setAlpha(value);}
    @Override public void setColorFilter(ColorFilter value){paint.setColorFilter(value);}
    @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
