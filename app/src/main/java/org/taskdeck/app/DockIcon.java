package org.taskdeck.app;

import android.graphics.*;
import android.graphics.drawable.*;

/** Apply the selected mask, including to adaptive layers without the system's mask. */
final class DockIcon extends Drawable {
    private final Drawable source; private final boolean round;
    DockIcon(Drawable source,boolean round){this.source=source;this.round=round;}
    @Override public void draw(Canvas canvas){
        if(source==null)return;
        Rect bounds=getBounds();Path mask=new Path();RectF box=new RectF(bounds);
        float radius=round?Math.min(box.width(),box.height())/2:Math.min(box.width(),box.height())*.08f;
        mask.addRoundRect(box,radius,radius,Path.Direction.CW);int saved=canvas.save();canvas.clipPath(mask);
        if(source instanceof AdaptiveIconDrawable){AdaptiveIconDrawable icon=(AdaptiveIconDrawable)source;Rect layers=new Rect(bounds);layers.inset(-Math.round(bounds.width()*AdaptiveIconDrawable.getExtraInsetFraction()),-Math.round(bounds.height()*AdaptiveIconDrawable.getExtraInsetFraction()));drawLayer(canvas,icon.getBackground(),layers);drawLayer(canvas,icon.getForeground(),layers);}
        else drawLayer(canvas,source,bounds);
        canvas.restoreToCount(saved);
    }
    private void drawLayer(Canvas canvas,Drawable layer,Rect bounds){if(layer!=null){layer.setBounds(bounds);layer.draw(canvas);}}
    @Override public void setAlpha(int alpha){if(source!=null)source.setAlpha(alpha);invalidateSelf();}
    @Override public void setColorFilter(ColorFilter filter){if(source!=null)source.setColorFilter(filter);invalidateSelf();}
    @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
