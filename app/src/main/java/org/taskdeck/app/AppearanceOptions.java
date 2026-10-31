package org.taskdeck.app;

import android.content.Context;
import android.view.View;
import android.widget.*;

/** Two settings share a row when their complete labels fit; otherwise keep a column. */
final class AppearanceOptions extends LinearLayout {
    AppearanceOptions(Context context){ super(context); setOrientation(HORIZONTAL); }
    @Override protected void onMeasure(int widthSpec,int heightSpec){
        int available=View.MeasureSpec.getSize(widthSpec),padding=Math.round((getChildCount()==3?8:16)*getResources().getDisplayMetrics().density),gap=Math.round(8*getResources().getDisplayMetrics().density),width=0;
        for(int i=0;i<getChildCount();i++) width=Math.max(width,ReadableButtons.naturalWidth((Button)getChildAt(i),padding));
        boolean horizontal=width*getChildCount()+gap*(getChildCount()-1)<=available||available>=Math.round(360*getResources().getDisplayMetrics().density);
        setOrientation(horizontal?HORIZONTAL:VERTICAL); width=Math.min(width,horizontal?(available-gap*(getChildCount()-1))/Math.max(1,getChildCount()):available);
        for(int i=0;i<getChildCount();i++){ Button button=(Button)getChildAt(i); LayoutParams size=(LayoutParams)button.getLayoutParams(); size.rightMargin=horizontal&&i<getChildCount()-1?gap:0; size.bottomMargin=!horizontal&&i<getChildCount()-1?gap:0; button.setLayoutParams(size); ReadableButtons.fit(button,width,padding,Math.round(48*getResources().getDisplayMetrics().density)); }
        if(horizontal){int height=0;for(int i=0;i<getChildCount();i++)height=Math.max(height,getChildAt(i).getLayoutParams().height);for(int i=0;i<getChildCount();i++)getChildAt(i).getLayoutParams().height=height;}
        super.onMeasure(widthSpec,heightSpec);
    }
}
