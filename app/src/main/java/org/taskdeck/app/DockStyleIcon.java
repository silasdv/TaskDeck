package org.taskdeck.app;
import android.graphics.*;
import android.graphics.drawable.Drawable;
/** Small, distinct previews of each dock arrangement. */
public final class DockStyleIcon extends Drawable {
 private final String style;private final int color;private final Paint p=new Paint(3);
 public DockStyleIcon(String style,int color){this.style=style;this.color=color;}
 @Override public void draw(Canvas c){int save=c.save();c.translate(getBounds().left,getBounds().top);c.scale(getBounds().width()/64f,getBounds().height()/32f);p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);if(style.equals("thumb")){c.drawRoundRect(1,11,23,30,4,4,p);c.drawRoundRect(41,11,63,30,4,4,p);}else c.drawRoundRect(style.equals("shelf")?1:12,style.equals("compact")?15:9,style.equals("shelf")?63:52,30,4,4,p);p.setStyle(Paint.Style.FILL);int[] xs=style.equals("thumb")?new int[]{7,17,47,57}:new int[]{22,32,42};for(int x:xs)c.drawRoundRect(x-3,18,x+3,25,1,1,p);c.restoreToCount(save);}
 @Override public void setAlpha(int a){}@Override public void setColorFilter(ColorFilter f){}@Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
