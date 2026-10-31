package org.taskdeck.app;
import android.graphics.*;
import android.graphics.drawable.Drawable;
/** Center artwork independently of TextView compound-drawable alignment. */
final class CenteredGlyph extends Drawable {
    final Drawable artwork;final int width,height;
    CenteredGlyph(Drawable artwork,int width,int height){this.artwork=artwork;this.width=width;this.height=height;}
    @Override public void draw(Canvas canvas){Rect b=getBounds();float scale=Math.min(1,Math.min(Math.max(1,b.width()-4)/(float)width,Math.max(1,b.height()-4)/(float)height));int w=Math.round(width*scale),h=Math.round(height*scale),x=b.centerX()-w/2,y=b.centerY()-h/2;artwork.setBounds(x,y,x+w,y+h);artwork.draw(canvas);}
    @Override public void setAlpha(int alpha){artwork.setAlpha(alpha);}
    @Override public void setColorFilter(ColorFilter filter){artwork.setColorFilter(filter);}
    @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
