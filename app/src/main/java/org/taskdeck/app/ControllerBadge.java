package org.taskdeck.app;
import android.graphics.*;
import android.graphics.drawable.Drawable;

/** Consistent vector face symbols, SNES letter colors and compact shoulder labels. */
public final class ControllerBadge extends Drawable {
    private final String key; private final int neutral;
    public ControllerBadge(String key,int neutral){ this.key=key; this.neutral=neutral; }
    public static int color(String key,int fallback){
        switch(key){ case "A":return Color.rgb(224,60,68); case "B":return Color.rgb(247,207,58); case "X":return Color.rgb(47,119,215); case "Y":return Color.rgb(0,157,91); case "Cross":return Color.rgb(80,150,235); case "Circle":return Color.rgb(245,100,115); case "Square":return Color.rgb(226,135,205); case "Triangle":return Color.rgb(65,200,165); default:return fallback; }
    }
    public static boolean symbol(String key){ return key.equals("Cross")||key.equals("Circle")||key.equals("Square")||key.equals("Triangle"); }
    @Override public void draw(Canvas c){
        Rect b=getBounds(); boolean ps=symbol(key),shoulder=key.length()>1&&!ps; Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color(key,neutral)); p.setStyle(shoulder||ps?Paint.Style.STROKE:Paint.Style.FILL); p.setStrokeWidth(Math.max(1.5f,b.height()*.07f)); p.setStrokeCap(Paint.Cap.ROUND); p.setStrokeJoin(Paint.Join.ROUND);
        float x=b.exactCenterX(),y=b.exactCenterY(),r=b.height()*.27f;
        if(ps){
            if(key.equals("Cross")){ c.drawLine(x-r,y-r,x+r,y+r,p); c.drawLine(x+r,y-r,x-r,y+r,p); }
            else if(key.equals("Circle")) c.drawCircle(x,y,r,p);
            else if(key.equals("Square")) c.drawRect(x-r,y-r,x+r,y+r,p);
            else { Path triangle=new Path(); triangle.moveTo(x,y-r*1.15f); triangle.lineTo(x+r*1.1f,y+r); triangle.lineTo(x-r*1.1f,y+r); triangle.close(); c.drawPath(triangle,p); }
            return;
        }
        if(shoulder) c.drawRoundRect(new RectF(b.left+1,b.top+1,b.right-1,b.bottom-1),5,5,p); else c.drawCircle(x,y,b.height()/2f-1,p);
        p.setStyle(Paint.Style.FILL); p.setColor(shoulder?neutral:key.equals("B")?Color.rgb(35,30,10):Color.WHITE);
        if(key.equals("Start")){ Path play=new Path(); play.moveTo(x-r*.6f,y-r); play.lineTo(x+r,y); play.lineTo(x-r*.6f,y+r); play.close(); c.drawPath(play,p); return; }
        if(key.equals("Options")){ p.setStyle(Paint.Style.STROKE); for(int i=-1;i<=1;i++) c.drawLine(x-r,y+i*r*.65f,x+r,y+i*r*.65f,p); return; }
        p.setTypeface(Typeface.DEFAULT_BOLD); p.setTextSize(b.height()*(shoulder?(key.length()>2?.33f:.44f):.57f)); p.setTextAlign(Paint.Align.CENTER); c.drawText(key,x,y-(p.ascent()+p.descent())/2,p);
    }
    @Override public void setAlpha(int alpha){}
    @Override public void setColorFilter(ColorFilter filter){}
    @Deprecated @Override public int getOpacity(){ return PixelFormat.TRANSLUCENT; }
}
