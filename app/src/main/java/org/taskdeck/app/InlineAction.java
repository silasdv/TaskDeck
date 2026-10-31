package org.taskdeck.app;
import android.graphics.*;
import android.graphics.drawable.Drawable;
/** Center a compact action's icon and text as one group. */
final class InlineAction extends Drawable {
    final String title,glyph;private final int ink;private final float density;private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    InlineAction(String title,String glyph,int ink,float density){this.title=title;this.glyph=glyph;this.ink=ink;this.density=density;}
    @Override public void draw(Canvas c){Rect b=getBounds();p.setColor(ink);p.setTextSize(11*density);p.setTypeface(Typeface.DEFAULT);float size=16*density,gap=6*density,width=p.measureText(title)+size+gap,left=b.exactCenterX()-width/2;Drawable icon=new ActionGlyph(glyph,ink);icon.setBounds(Math.round(left),Math.round(b.exactCenterY()-size/2),Math.round(left+size),Math.round(b.exactCenterY()+size/2));icon.draw(c);c.drawText(title,left+size+gap,b.exactCenterY()-(p.ascent()+p.descent())/2,p);}
    @Override public void setAlpha(int a){}@Override public void setColorFilter(ColorFilter f){}@Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
