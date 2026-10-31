package org.taskdeck.app;
import android.graphics.*;
import android.graphics.drawable.Drawable;
/** Saved choice uses fill and a tick; controller focus uses an independent outline. */
final class ChoiceSurface extends Drawable {
    private final DeckTheme theme;private final float d;private final Paint p=new Paint(3);private boolean focus,selected,pressed;
    ChoiceSurface(DeckTheme theme,float density){this.theme=theme;d=density;}
    @Override public boolean isStateful(){return true;}
    @Override protected boolean onStateChange(int[] states){focus=selected=pressed=false;for(int s:states){focus|=s==android.R.attr.state_focused||s==android.R.attr.state_activated;selected|=s==android.R.attr.state_selected;pressed|=s==android.R.attr.state_pressed;}invalidateSelf();return true;}
    @Override public void draw(Canvas c){RectF b=new RectF(getBounds());b.inset(2*d,2*d);p.setStyle(Paint.Style.FILL);p.setColor(selected||pressed?theme.selected:theme.raised);c.drawRoundRect(b,10*d,10*d,p);if(focus){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2*d);p.setColor(theme.accent);c.drawRoundRect(b,10*d,10*d,p);}if(selected){float x=b.right-9*d,y=b.top+8*d;p.setColor(theme.accent);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.8f*d);p.setStrokeCap(Paint.Cap.ROUND);c.drawLine(x-3*d,y,x-d,y+2*d,p);c.drawLine(x-d,y+2*d,x+3*d,y-3*d,p);}}
    @Override public void setAlpha(int a){}@Override public void setColorFilter(ColorFilter f){}@Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
