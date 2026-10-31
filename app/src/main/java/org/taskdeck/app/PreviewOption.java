package org.taskdeck.app;
import android.graphics.*;
import android.graphics.drawable.*;
/** Every choice uses the same app artwork; only the demonstrated property changes. */
final class PreviewOption extends Drawable {
    final String type,value;private final Drawable icon;private final DeckTheme theme;private final String iconShape,backgroundShape;private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    PreviewOption(Drawable icon,DeckTheme theme,String type,String value,String iconShape,String backgroundShape){this.icon=icon;this.theme=theme;this.type=type;this.value=value;this.iconShape=iconShape;this.backgroundShape=backgroundShape;}
    @Override public void draw(Canvas canvas){Rect b=getBounds();if(type.equals("background")){IconShape.Surface plate=new IconShape.Surface(theme.raised,value);plate.setStroke(1,theme.muted);plate.setBounds(b);plate.draw(canvas);return;}else if(type.equals("icon")){GradientDrawable plate=new GradientDrawable();plate.setColor(theme.raised);plate.setCornerRadius(b.height()*.24f);plate.setBounds(b);plate.draw(canvas);}Drawable sample=new DockIcon(icon,type.equals("icon")?value:iconShape);if(type.equals("look"))sample=new IconArtwork(sample,value,false,theme.accent);int size=Math.round(Math.min(b.width(),b.height())*(type.equals("look")?.92f:.70f));sample.setBounds(b.centerX()-size/2,b.centerY()-size/2,b.centerX()+(size+1)/2,b.centerY()+(size+1)/2);sample.draw(canvas);}
    @Override public void setAlpha(int alpha){}@Override public void setColorFilter(ColorFilter filter){}@Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
