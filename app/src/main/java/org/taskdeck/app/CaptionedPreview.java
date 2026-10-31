package org.taskdeck.app;
import android.graphics.*;
import android.graphics.drawable.Drawable;
/** An icon and its centered caption share one selectable target. */
final class CaptionedPreview extends Drawable {
    final Drawable artwork;final String caption;
    private final String[] lines;private final float density;private final int ink;private final boolean compact;private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    CaptionedPreview(Drawable artwork,String caption,int ink,float density,boolean compact){this.artwork=artwork;this.caption=caption;this.lines=caption.split("\n");this.ink=ink;this.density=density;this.compact=compact;}
    @Override public void draw(Canvas c){
        Rect b=getBounds();p.setTypeface(Typeface.DEFAULT);p.setTextAlign(Paint.Align.CENTER);p.setTextSize((compact?9:11)*density);
        float widest=0;for(String value:lines)widest=Math.max(widest,p.measureText(value));if(widest>b.width()-6*density)p.setTextSize(p.getTextSize()*Math.max(0,(b.width()-6*density)/widest));
        p.setColor(ink);float line=(compact?10:13)*density,textHeight=line*(artwork instanceof PreviewOption&&((PreviewOption)artwork).type.equals("look")?2:lines.length),gap=2*density,pad=3*density,aspect=artwork instanceof DockStyleIcon?2:1;
        int icon=Math.max(1,Math.min(Math.round((compact?28:56)*density),Math.min((int)((b.width()-pad*2)/aspect),Math.round(b.height()-textHeight-gap-pad*2))));
        float total=icon+gap+textHeight,top=b.exactCenterY()-total/2;int iconWidth=Math.round(icon*aspect);
        artwork.setBounds(Math.round(b.exactCenterX()-iconWidth/2f),Math.round(top),Math.round(b.exactCenterX()+iconWidth/2f),Math.round(top)+icon);artwork.draw(c);
        float baseline=top+icon+gap-p.ascent();for(String value:lines){c.drawText(value,b.exactCenterX(),baseline,p);baseline+=line;}
    }
    @Override public void setAlpha(int a){}@Override public void setColorFilter(ColorFilter f){}@Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
