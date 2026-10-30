package org.taskdeck.app;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.widget.*;
public final class AppPickerTile extends FrameLayout {
    final ImageView image; final Button button;
    public AppPickerTile(Context context,Button button,Drawable icon){super(context);this.button=button;button.setStateListAnimator(null);button.setElevation(0);button.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);button.setTextSize(12);button.setMaxLines(1);button.setPadding(6,0,6,6);addView(button,new LayoutParams(-1,-1));image=new ImageView(context);image.setTag("pickerIcon");image.setImageDrawable(icon);image.setScaleType(ImageView.ScaleType.FIT_CENTER);image.setElevation(1);image.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);LayoutParams p=new LayoutParams(32,32,Gravity.TOP|Gravity.CENTER_HORIZONTAL);p.topMargin=6;addView(image,p);}
    @Override protected void onMeasure(int w,int h){float d=getResources().getDisplayMetrics().density;int height=MeasureSpec.getSize(h);int text=(int)Math.ceil(button.getPaint().getFontMetrics().bottom-button.getPaint().getFontMetrics().top);int size=Math.max((int)(12*d),Math.min((int)(36*d),height-text-(int)(14*d)));LayoutParams p=(LayoutParams)image.getLayoutParams();p.width=size;p.height=size;p.topMargin=Math.max((int)(4*d),(height-text-(int)(6*d)-size)/2);image.setLayoutParams(p);super.onMeasure(w,h);}
}
