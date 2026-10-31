package org.taskdeck.app;
import android.content.Context;
import android.view.View;
import android.widget.GridLayout;
/** Four compact columns on landscape handhelds; a two-by-two form on phones. */
final class DockOptionsGrid extends GridLayout {
    private int placedColumns=-1,placedCount=-1;
    private void placeCells(int columns){if(placedColumns==columns&&placedCount==getChildCount())return;for(int i=0;i<getChildCount();i++){LayoutParams p=(LayoutParams)getChildAt(i).getLayoutParams();p.rowSpec=spec(i/columns);p.columnSpec=spec(i%columns);getChildAt(i).setLayoutParams(p);}placedColumns=columns;placedCount=getChildCount();}
    DockOptionsGrid(Context context){super(context);setColumnCount(4);}
    @Override protected void onMeasure(int ws,int hs){float d=getResources().getDisplayMetrics().density;int width=MeasureSpec.getSize(ws),columns=width>=560*d?4:2,gap=Math.round(8*d),cell=(width-gap*(columns-1))/columns;setColumnCount(Math.max(columns,getColumnCount()));placeCells(columns);int rows=(getChildCount()+columns-1)/columns;int natural=Math.round(144*d);if(MeasureSpec.getMode(hs)==MeasureSpec.UNSPECIFIED){for(int i=0;i<getChildCount();i++){getChildAt(i).measure(MeasureSpec.makeMeasureSpec(cell,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED));natural=Math.max(natural,getChildAt(i).getMeasuredHeight());}}int available=MeasureSpec.getMode(hs)==MeasureSpec.UNSPECIFIED?natural*rows+gap*(rows-1):MeasureSpec.getSize(hs);int height=Math.max(1,(available-gap*(rows-1))/rows);for(int i=0;i<getChildCount();i++){LayoutParams p=(LayoutParams)getChildAt(i).getLayoutParams();int right=i%columns<columns-1?gap:0,bottom=i/columns<rows-1?gap:0;if(p.width!=cell||p.height!=height||p.rightMargin!=right||p.bottomMargin!=bottom){p.width=cell;p.height=height;p.rightMargin=right;p.bottomMargin=bottom;getChildAt(i).setLayoutParams(p);}}setColumnCount(columns);super.onMeasure(ws,MeasureSpec.makeMeasureSpec(available,MeasureSpec.EXACTLY));}
}
