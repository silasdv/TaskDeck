package org.taskdeck.app;
import android.graphics.*;
import android.graphics.drawable.Drawable;
/** Detail-preserving monochrome/tinted artwork and a translucent glass highlight. */
final class IconArtwork extends Drawable {
    final Drawable source;final String look;final boolean accent;final int color;private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    IconArtwork(Drawable source,String look,boolean accent,int color){this.source=source;this.look=look;this.accent=accent;this.color=color;}
    @Override public void draw(Canvas canvas){if(source==null)return;RectF box=new RectF(getBounds());source.setBounds(getBounds());boolean glass="glass".equals(look),tint=accent||"accent".equals(look),mono=tint||glass||"mono".equals(look);if(!mono){source.draw(canvas);return;}int outer=canvas.saveLayer(box,null);ColorMatrix matrix=new ColorMatrix();matrix.setSaturation(0);if(tint){float rr=Color.red(color)/255f,gg=Color.green(color)/255f,bb=Color.blue(color)/255f;ColorMatrix shades=new ColorMatrix(new float[]{rr*.85f,0,0,0,Color.red(color)*.15f,0,gg*.85f,0,0,Color.green(color)*.15f,0,0,bb*.85f,0,Color.blue(color)*.15f,0,0,0,1,0});matrix.postConcat(shades);}paint.setColorFilter(new ColorMatrixColorFilter(matrix));int filtered=canvas.saveLayer(box,paint);source.draw(canvas);canvas.restoreToCount(filtered);paint.setColorFilter(null);if(glass){paint.setShader(new LinearGradient(box.left,box.top,box.right,box.bottom,new int[]{0x99FFFFFF,0x15FFFFFF,0x50759BA8},new float[]{0,.55f,1},Shader.TileMode.CLAMP));paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));canvas.drawRect(box,paint);paint.setShader(null);paint.setXfermode(null);}canvas.restoreToCount(outer);}
    @Override public void setAlpha(int value){if(source!=null)source.setAlpha(value);invalidateSelf();}
    @Override public void setColorFilter(ColorFilter filter){if(source!=null)source.setColorFilter(filter);invalidateSelf();}
    @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
