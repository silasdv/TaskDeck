package org.taskdeck.app;

import android.content.Context;
import android.view.View;
import android.widget.GridLayout;

/** Complete symmetric rows, using the available palette area rather than tiny fixed cells. */
public final class AccentPalette extends GridLayout {
    private final int rowHeight;
    public AccentPalette(Context context){ this(context,52); }
    public AccentPalette(Context context,int rowHeight){ super(context); this.rowHeight=rowHeight;setColumnCount(8); }
    @Override protected void onMeasure(int widthSpec,int heightSpec){
        float density=getResources().getDisplayMetrics().density; int available=View.MeasureSpec.getSize(widthSpec),gap=Math.round(6*density);
        int columns=available>=Math.round(376*density)?8:available>=Math.round(184*density)?4:2;
        int rows=Math.max(1,(getChildCount()+columns-1)/columns);
        int wanted=Math.round(rowHeight*density)*rows+gap*(rows-1); int height=View.MeasureSpec.getMode(heightSpec)==View.MeasureSpec.UNSPECIFIED?wanted:Math.min(wanted,View.MeasureSpec.getSize(heightSpec));
        int width=Math.max(1,(available-gap*(columns-1))/columns),cellHeight=Math.max(1,(height-gap*(rows-1))/rows);
        setColumnCount(Math.max(columns,getColumnCount()));
        for(int i=0;i<getChildCount();i++){ LayoutParams size=new LayoutParams(spec(i/columns),spec(i%columns)); size.width=width; size.height=cellHeight; size.rightMargin=i%columns<columns-1?gap:0; size.bottomMargin=i/columns<rows-1?gap:0; getChildAt(i).setLayoutParams(size); }
        setColumnCount(columns); super.onMeasure(widthSpec,View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY));
    }
}
