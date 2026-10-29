package org.taskdeck.app;
import android.graphics.*;
import android.graphics.drawable.Drawable;

/** International SNES face-button palette; shoulder buttons use a neutral rectangle. */
public final class ControllerBadge extends Drawable {
    private final String key; private final int neutral;
    public ControllerBadge(String key,int neutral){ this.key=key; this.neutral=neutral; }
    public static int color(String key,int fallback){
        switch(key){ case "A": return Color.rgb(224,60,68); case "B": return Color.rgb(247,207,58); case "X": return Color.rgb(47,119,215); case "Y": return Color.rgb(0,157,91); default:return fallback; }
    }
    @Override public void draw(Canvas c){
        Rect b=getBounds(); boolean shoulder=key.length()>1; Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); int color=color(key,neutral);
        p.setColor(color); p.setStyle(shoulder?Paint.Style.STROKE:Paint.Style.FILL); p.setStrokeWidth(2);
        if(shoulder) c.drawRoundRect(new RectF(b.left+1,b.top+1,b.right-1,b.bottom-1),5,5,p); else c.drawCircle(b.exactCenterX(),b.exactCenterY(),b.height()/2f-1,p);
        p.setStyle(Paint.Style.FILL); p.setColor(shoulder?neutral:key.equals("B")?Color.rgb(35,30,10):Color.WHITE); p.setTypeface(Typeface.DEFAULT_BOLD); p.setTextSize(b.height()*.57f); p.setTextAlign(Paint.Align.CENTER);
        c.drawText(key,b.exactCenterX(),b.exactCenterY()-(p.ascent()+p.descent())/2,p);
    }
    @Override public void setAlpha(int alpha){}
    @Override public void setColorFilter(ColorFilter filter){}
    @Deprecated @Override public int getOpacity(){ return PixelFormat.TRANSLUCENT; }
}
