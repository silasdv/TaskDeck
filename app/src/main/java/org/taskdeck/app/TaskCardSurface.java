package org.taskdeck.app;

import android.graphics.*;
import android.graphics.drawable.Drawable;

/** Quiet glass edge, with independent controller-focus and current-app states. */
final class TaskCardSurface extends Drawable {
    private final DeckTheme theme;
    private final boolean focused,current;
    private final int currentColor;
    private final float density;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    TaskCardSurface(DeckTheme theme,boolean focused,boolean current,int currentColor,float density){
        this.theme=theme;this.focused=focused;this.current=current;this.currentColor=currentColor;this.density=density;
    }
    @Override public void draw(Canvas canvas){
        RectF box=new RectF(getBounds());float stroke=(focused?2:1)*density;box.inset(stroke/2,stroke/2);float radius=16*density;
        boolean light=Color.luminance(theme.background)>.5;
        int base=focused?theme.selected:theme.surface;
        paint.setStyle(Paint.Style.FILL);paint.setShader(new LinearGradient(0,box.top,0,Math.max(box.top+1,box.bottom),mix(base,light?Color.BLACK:Color.WHITE,light?.01f:.035f),base,Shader.TileMode.CLAMP));
        canvas.drawRoundRect(box,radius,radius,paint);
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(stroke);
        if(focused||current){paint.setShader(null);paint.setColor(focused?theme.accent:currentColor);}
        else paint.setShader(new LinearGradient(0,box.top,0,Math.max(box.top+1,box.bottom),mix(base,light?Color.BLACK:Color.WHITE,light?.17f:.24f),mix(base,light?Color.BLACK:Color.WHITE,.07f),Shader.TileMode.CLAMP));
        canvas.drawRoundRect(box,radius,radius,paint);paint.setShader(null);
    }
    private static int mix(int a,int b,float f){return Color.rgb(Math.round(Color.red(a)*(1-f)+Color.red(b)*f),Math.round(Color.green(a)*(1-f)+Color.green(b)*f),Math.round(Color.blue(a)*(1-f)+Color.blue(b)*f));}
    @Override public void setAlpha(int alpha){}
    @Override public void setColorFilter(ColorFilter filter){}
    @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
