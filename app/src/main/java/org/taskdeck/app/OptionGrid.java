package org.taskdeck.app;
import android.content.Context;
import android.view.View;
import android.widget.GridLayout;
/** Fixed, equal visual choices with small symmetric gaps. */
final class OptionGrid extends GridLayout {
    private final int columns,rowHeight,gap;private int placedCount=-1;
    OptionGrid(Context context){this(context,4,Math.round(32*context.getResources().getDisplayMetrics().density),Math.round(3*context.getResources().getDisplayMetrics().density));}
    OptionGrid(Context context,int columns,int rowHeight,int gap){super(context);this.columns=columns;this.rowHeight=rowHeight;this.gap=gap;setColumnCount(columns);}
    private void placeCells(){if(placedCount==getChildCount())return;for(int i=0;i<getChildCount();i++){LayoutParams p=(LayoutParams)getChildAt(i).getLayoutParams();p.rowSpec=spec(i/columns);p.columnSpec=spec(i%columns);getChildAt(i).setLayoutParams(p);}placedCount=getChildCount();}
    @Override protected void onMeasure(int ws,int hs){placeCells();int space=MeasureSpec.getSize(ws),cell=Math.max(1,(space-gap*(columns-1))/columns);for(int i=0;i<getChildCount();i++){LayoutParams p=(LayoutParams)getChildAt(i).getLayoutParams();int right=i%columns<columns-1?gap:0,bottom=i/columns<(getChildCount()-1)/columns?gap:0;if(p.width!=cell||p.height!=rowHeight||p.rightMargin!=right||p.bottomMargin!=bottom){p.width=cell;p.height=rowHeight;p.rightMargin=right;p.bottomMargin=bottom;getChildAt(i).setLayoutParams(p);}}super.onMeasure(ws,hs);}
}
