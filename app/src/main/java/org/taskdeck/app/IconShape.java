package org.taskdeck.app;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
final class IconShape {
    static Path path(RectF box,String shape){Path path=new Path();if("squircle".equals(shape)){float a=box.width()/2,b=box.height()/2;for(int i=0;i<=80;i++){double t=i*Math.PI*2/80,c=Math.cos(t),s=Math.sin(t);float x=box.centerX()+a*(float)Math.copySign(Math.sqrt(Math.abs(c)),c),y=box.centerY()+b*(float)Math.copySign(Math.sqrt(Math.abs(s)),s);if(i==0)path.moveTo(x,y);else path.lineTo(x,y);}path.close();}else{float radius=Math.min(box.width(),box.height())*("square".equals(shape)?.08f:.5f);path.addRoundRect(box,radius,radius,Path.Direction.CW);}return path;}
    static final class Surface extends GradientDrawable {
        final String shape;private int stroke,width;private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        Surface(int fill,String shape){this.shape=shape;setColor(fill);}
        @Override public void setStroke(int width,int color){super.setStroke(width,color);this.width=width;stroke=color;}
        @Override public void draw(Canvas canvas){RectF box=new RectF(getBounds());box.inset(width/2f,width/2f);Path path=path(box,shape);paint.setStyle(Paint.Style.FILL);paint.setColor(getColor()==null?Color.TRANSPARENT:getColor().getDefaultColor());canvas.drawPath(path,paint);if(width>0){paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(width);paint.setColor(stroke);canvas.drawPath(path,paint);}}
    }
}
