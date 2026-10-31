package org.taskdeck.app;
import android.graphics.*;
import android.graphics.drawable.Drawable;
/** A large color sample; add a fine contrast edge only when it blends into its surface. */
final class DockColorSwatch extends Drawable {
    final int color,surface,accent;final boolean none;private boolean focused,selected;private final float density;private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    DockColorSwatch(int color,int surface,int accent,boolean none,float density){this.color=color;this.surface=surface;this.accent=accent;this.none=none;this.density=density;}
    @Override public boolean isStateful(){return true;}
    @Override protected boolean onStateChange(int[] states){focused=selected=false;for(int s:states){focused|=s==android.R.attr.state_focused||s==android.R.attr.state_activated;selected|=s==android.R.attr.state_selected;}invalidateSelf();return true;}
    boolean needsEdge(){double a=Color.luminance(color)+.05,b=Color.luminance(surface)+.05;return none||Math.max(a,b)/Math.min(a,b)<1.8;}
    @Override public void draw(Canvas c){RectF b=new RectF(getBounds());b.inset(1.5f*density,1.5f*density);if(focused||selected){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth((focused?2:1)*density);p.setColor(accent);c.drawRoundRect(b,6*density,6*density,p);}float r=Math.max(1,Math.min(b.width(),b.height())/2-2*density);p.setStyle(Paint.Style.FILL);p.setColor(none?surface:color);c.drawCircle(b.centerX(),b.centerY(),r,p);double a=Color.luminance(color)+.05,z=Color.luminance(surface)+.05,contrast=Math.max(a,z)/Math.min(a,z);if(none||contrast<1.8){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth((none?1.2f:.65f)*density);p.setColor(QuickAccess.foregroundColor(surface));c.drawCircle(b.centerX(),b.centerY(),r,p);if(none)c.drawLine(b.centerX()-r*.7f,b.centerY()+r*.7f,b.centerX()+r*.7f,b.centerY()-r*.7f,p);}}
    @Override public void setAlpha(int a){}@Override public void setColorFilter(ColorFilter f){}@Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
