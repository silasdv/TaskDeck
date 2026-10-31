package org.taskdeck.app;

import android.graphics.*;
import android.graphics.drawable.*;

/** Apply the selected mask, including to adaptive layers without the system's mask. */
final class DockIcon extends Drawable {
    private final Drawable source; private final String shape;
    DockIcon(Drawable source,boolean round){this(source,round?"round":"square");}
    DockIcon(Drawable source,String shape){this.source=source;this.shape=shape;}
    @Override public void draw(Canvas canvas){
        if(source==null)return;
        Rect bounds=getBounds();Path mask=IconShape.path(new RectF(bounds),shape);int saved=canvas.save();canvas.clipPath(mask);
        if(source instanceof AdaptiveIconDrawable){AdaptiveIconDrawable icon=(AdaptiveIconDrawable)source;Rect layers=new Rect(bounds);layers.inset(-Math.round(bounds.width()*AdaptiveIconDrawable.getExtraInsetFraction()),-Math.round(bounds.height()*AdaptiveIconDrawable.getExtraInsetFraction()));drawLayer(canvas,icon.getBackground(),layers);drawLayer(canvas,icon.getForeground(),layers);}
        else drawLayer(canvas,source,bounds);
        canvas.restoreToCount(saved);
    }
    private void drawLayer(Canvas canvas,Drawable layer,Rect bounds){if(layer!=null){layer.setBounds(bounds);layer.draw(canvas);}}
    @Override public void setAlpha(int alpha){if(source!=null)source.setAlpha(alpha);invalidateSelf();}
    @Override public void setColorFilter(ColorFilter filter){if(source!=null)source.setColorFilter(filter);invalidateSelf();}
    @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
