package org.taskdeck.app;

import android.content.Context;
import android.view.View;
import android.widget.GridLayout;

/** Reflow before measurement so every dot is included in the measured row height. */
public final class AccentPalette extends GridLayout {
    public AccentPalette(Context context){ super(context); setColumnCount(13); }
    @Override protected void onMeasure(int widthSpec,int heightSpec){
        int cell=Math.round(28*getResources().getDisplayMetrics().density);
        int available=View.MeasureSpec.getSize(widthSpec)-getPaddingLeft()-getPaddingRight();
        int columns=View.MeasureSpec.getMode(widthSpec)==View.MeasureSpec.UNSPECIFIED?13:Math.max(1,Math.min(13,available/Math.max(1,cell)));
        if(columns!=getColumnCount()){
            setColumnCount(Math.max(columns,getColumnCount()));
            for(int i=0;i<getChildCount();i++){ LayoutParams size=new LayoutParams(spec(i/columns),spec(i%columns)); size.width=cell; size.height=Math.round(36*getResources().getDisplayMetrics().density); getChildAt(i).setLayoutParams(size); }
            setColumnCount(columns);
        }
        super.onMeasure(widthSpec,heightSpec);
    }
}
