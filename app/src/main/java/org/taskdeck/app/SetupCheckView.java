package org.taskdeck.app;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import android.animation.ValueAnimator;

/** Short drawn completion check, with a text equivalent for accessibility. */
public final class SetupCheckView extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int color;
    private float progress;
    private ValueAnimator animation;
    public SetupCheckView(Context context,int color){ super(context); this.color=color; setContentDescription("Setup complete"); setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES); }
    public void play(){ animation=ValueAnimator.ofFloat(0,1); animation.setDuration(800); animation.addUpdateListener(a->{ progress=(float)a.getAnimatedValue(); invalidate(); }); animation.start(); }
    @Override protected void onDetachedFromWindow(){ if(animation!=null) animation.cancel(); super.onDetachedFromWindow(); }
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas); float size=Math.min(getWidth(),getHeight()),x=(getWidth()-size)/2,y=(getHeight()-size)/2;
        paint.setColor(color); paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(size*.055f); paint.setStrokeCap(Paint.Cap.ROUND); paint.setStrokeJoin(Paint.Join.ROUND);
        canvas.drawCircle(x+size*.5f,y+size*.5f,size*.43f,paint);
        Path path=new Path(); path.moveTo(x+size*.25f,y+size*.5f); path.lineTo(x+size*.43f,y+size*.68f); path.lineTo(x+size*.76f,y+size*.32f);
        PathMeasure measure=new PathMeasure(path,false); Path visible=new Path(); measure.getSegment(0,measure.getLength()*progress,visible,true); canvas.drawPath(visible,paint);
    }
}
