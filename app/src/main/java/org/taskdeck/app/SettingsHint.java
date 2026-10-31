package org.taskdeck.app;
import android.graphics.*;
import android.graphics.drawable.Drawable;
/** The Start hint uses the same 21x16 dp badge as Select, inside one Settings button. */
final class SettingsHint extends Drawable {
    private final int ink;private final float density;
    SettingsHint(int ink,float density){this.ink=ink;this.density=density;}
    @Override public void draw(Canvas c){Rect b=getBounds();int x=b.centerX()-Math.round(22.5f*density),y=b.centerY();Drawable badge=new ControllerBadge("Start",ink,true);badge.setBounds(x,y-Math.round(8*density),x+Math.round(21*density),y+Math.round(8*density));badge.draw(c);Drawable gear=new ActionGlyph("settings",ink);gear.setBounds(x+Math.round(27*density),y-Math.round(9*density),x+Math.round(45*density),y+Math.round(9*density));gear.draw(c);}
    @Override public void setAlpha(int a){}@Override public void setColorFilter(ColorFilter f){}@Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
