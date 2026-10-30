package org.taskdeck.app;

import android.content.Context;
import android.view.View;
import android.widget.GridLayout;

/** Complete symmetric rows, using the available palette area rather than tiny fixed cells. */
public final class AccentPalette extends GridLayout {
    public AccentPalette(Context context){ super(context); setColumnCount(8); }
    @Override protected void onMeasure(int widthSpec,int heightSpec){
        float density=getResources().getDisplayMetrics().density; int available=View.MeasureSpec.getSize(widthSpec),gap=Math.round(8*density);
        int columns=available>=Math.round(376*density)?8:available>=Math.round(184*density)?4:2;
        int rows=Math.max(1,(getChildCount()+columns-1)/columns);
        int height=View.MeasureSpec.getMode(heightSpec)==View.MeasureSpec.EXACTLY?View.MeasureSpec.getSize(heightSpec):Math.round(52*density)*rows+gap*(rows-1);
        int width=Math.max(1,(available-gap*(columns-1))/columns),cellHeight=Math.max(1,(height-gap*(rows-1))/rows);
        setColumnCount(Math.max(columns,getColumnCount()));
        for(int i=0;i<getChildCount();i++){ LayoutParams size=new LayoutParams(spec(i/columns),spec(i%columns)); size.width=width; size.height=cellHeight; size.rightMargin=i%columns<columns-1?gap:0; size.bottomMargin=i/columns<rows-1?gap:0; getChildAt(i).setLayoutParams(size); }
        setColumnCount(columns); super.onMeasure(widthSpec,heightSpec);
    }
}
