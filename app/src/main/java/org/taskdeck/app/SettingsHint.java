package org.taskdeck.app;
import android.graphics.*;
import android.graphics.drawable.Drawable;
/** A readable filled Start label next to the Settings glyph. */
final class SettingsHint extends Drawable {
    private final int ink;private final float density;
    SettingsHint(int ink,float density){this.ink=ink;this.density=density;}
    @Override public void draw(Canvas c){Rect b=getBounds();boolean compact=b.width()<65*density;float badgeWidth=(compact?28:34)*density,gap=(compact?6:8)*density,gearSize=(compact?16:18)*density,total=badgeWidth+gap+gearSize;int x=Math.round(b.exactCenterX()-total/2),y=b.centerY();Drawable badge=new ControllerBadge("Start",ink,true);badge.setBounds(x,y-Math.round(10*density),x+Math.round(badgeWidth),y+Math.round(10*density));badge.draw(c);Drawable gear=new ActionGlyph("settings",ink);int left=x+Math.round(badgeWidth+gap),half=Math.round(gearSize/2);gear.setBounds(left,y-half,left+Math.round(gearSize),y+half);gear.draw(c);}
    @Override public void setAlpha(int a){}@Override public void setColorFilter(ColorFilter f){}@Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
