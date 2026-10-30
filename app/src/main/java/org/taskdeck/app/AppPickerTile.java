package org.taskdeck.app;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.widget.*;
public final class AppPickerTile extends FrameLayout {
    final ImageView image; final Button button;
    public AppPickerTile(Context context,Button button,Drawable icon){super(context);this.button=button;button.setStateListAnimator(null);button.setElevation(0);if(button.getBackground() instanceof android.graphics.drawable.InsetDrawable)button.setBackground(((android.graphics.drawable.InsetDrawable)button.getBackground()).getDrawable());button.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL);button.setTextSize(12);button.setMaxLines(1);button.setPadding(6,0,6,6);addView(button,new LayoutParams(-1,-1));image=new ImageView(context);image.setTag("pickerIcon");image.setImageDrawable(icon);image.setScaleType(ImageView.ScaleType.FIT_CENTER);image.setElevation(1);image.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);LayoutParams p=new LayoutParams(32,32,Gravity.TOP|Gravity.CENTER_HORIZONTAL);p.topMargin=6;addView(image,p);}
    @Override protected void onMeasure(int w,int h){
        float d=getResources().getDisplayMetrics().density;int height=MeasureSpec.getSize(h),gap=Math.round(6*d),edge=Math.round(8*d);button.setPadding(edge,0,edge,0);button.measure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(w),MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED));int text=button.getLayout()==null?button.getLineHeight():button.getLayout().getHeight();int size=Math.max(1,Math.min(Math.round(36*d),height-text-gap-edge*2));int top=Math.max(edge,(height-size-gap-text)/2);LayoutParams p=(LayoutParams)image.getLayoutParams();p.width=size;p.height=size;p.topMargin=top;image.setLayoutParams(p);button.setPadding(edge,top+size+gap,edge,edge);super.onMeasure(w,h);
    }
}
