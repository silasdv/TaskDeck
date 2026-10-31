package org.taskdeck.app;
import android.graphics.*;
import android.graphics.drawable.Drawable;
/** An icon and its centered caption share one selectable target. */
final class CaptionedPreview extends Drawable {
    final Drawable artwork;final String caption;private final float density;private final int ink;private final boolean compact;private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    CaptionedPreview(Drawable artwork,String caption,int ink,float density,boolean compact){this.artwork=artwork;this.caption=caption;this.ink=ink;this.density=density;this.compact=compact;}
    @Override public void draw(Canvas c){Rect b=getBounds();String[] lines=caption.split("\n");p.setTypeface(Typeface.DEFAULT);p.setTextAlign(Paint.Align.CENTER);p.setTextSize((compact?8:10)*density);p.setColor(ink);float line=(compact?9:12)*density,textHeight=line*lines.length,gap=2*density,pad=2*density;int icon=Math.max(1,Math.min(Math.round((compact?20:32)*density),Math.min(b.width()-4,Math.round(b.height()-textHeight-gap-pad*2))));float total=icon+gap+textHeight,top=b.exactCenterY()-total/2;int iconWidth=artwork instanceof DockStyleIcon?Math.min(b.width()-4,Math.round(icon*1.6f)):icon;artwork.setBounds(Math.round(b.exactCenterX()-iconWidth/2f),Math.round(top),Math.round(b.exactCenterX()+iconWidth/2f),Math.round(top)+icon);artwork.draw(c);float baseline=top+icon+gap-p.ascent();for(String value:lines){c.drawText(value,b.exactCenterX(),baseline,p);baseline+=line;}}
    @Override public void setAlpha(int a){}@Override public void setColorFilter(ColorFilter f){}@Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
