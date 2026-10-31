package org.taskdeck.app;
import android.graphics.*;
import android.graphics.drawable.Drawable;
/** Distinct None/Theme samples, with saved selection separate from focus. */
final class DockColorSwatch extends Drawable {
    final int color,surface,accent;final boolean none,theme;private boolean focused,selected;private final float density;private final Paint p=new Paint(3);
    DockColorSwatch(int color,int surface,int accent,boolean none,float density){this(color,surface,accent,none,false,density);}
    DockColorSwatch(int color,int surface,int accent,boolean none,boolean theme,float density){this.color=color;this.surface=surface;this.accent=accent;this.none=none;this.theme=theme;this.density=density;}
    @Override public boolean isStateful(){return true;}
    @Override protected boolean onStateChange(int[] states){focused=selected=false;for(int s:states){focused|=s==android.R.attr.state_focused||s==android.R.attr.state_activated;selected|=s==android.R.attr.state_selected;}invalidateSelf();return true;}
    boolean needsEdge(){double a=Color.luminance(color)+.05,b=Color.luminance(surface)+.05;return none||theme||Math.max(a,b)/Math.min(a,b)<1.8;}
    @Override public void draw(Canvas c){RectF b=new RectF(getBounds());b.inset(2*density,2*density);p.setStyle(Paint.Style.FILL);p.setColor(selected?((accent&0xffffff)|0x33000000):surface);c.drawRoundRect(b,7*density,7*density,p);if(focused){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2*density);p.setColor(accent);c.drawRoundRect(b,7*density,7*density,p);}float r=Math.max(1,Math.min(b.width(),b.height())/2-5*density);p.setStyle(Paint.Style.FILL);p.setColor(none?surface:color);c.drawCircle(b.centerX(),b.centerY(),r,p);if(theme){p.setColor(QuickAccess.foregroundColor(surface));RectF circle=new RectF(b.centerX()-r,b.centerY()-r,b.centerX()+r,b.centerY()+r);c.drawArc(circle,-90,180,true,p);}if(needsEdge()){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.75f*density);p.setColor(QuickAccess.foregroundColor(surface));c.drawCircle(b.centerX(),b.centerY(),r,p);}if(none){p.setStrokeWidth(1.2f*density);c.drawLine(b.centerX()-r*.7f,b.centerY()+r*.7f,b.centerX()+r*.7f,b.centerY()-r*.7f,p);}if(selected){float x=b.right-5*density,y=b.top+5*density;p.setColor(accent);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.8f*density);c.drawLine(x-3*density,y,x-density,y+2*density,p);c.drawLine(x-density,y+2*density,x+3*density,y-3*density,p);}}
    @Override public void setAlpha(int a){}@Override public void setColorFilter(ColorFilter f){}@Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
