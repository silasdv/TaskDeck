package org.taskdeck.app;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.GridLayout;
/** Four compact columns on landscape handhelds; a two-by-two form on phones. */
final class DockOptionsGrid extends GridLayout {
    private final GradientDrawable[] sections={new GradientDrawable(),new GradientDrawable()};
    void sectionColor(int color){for(GradientDrawable shape:sections){shape.setColor(color);shape.setCornerRadius(12*getResources().getDisplayMetrics().density);}}
    @Override protected void onLayout(boolean changed,int l,int t,int r,int b){super.onLayout(changed,l,t,r,b);for(int i=0;i<2;i++)if(getChildCount()>i*2+1){View first=getChildAt(i*2),last=getChildAt(i*2+1);sections[i].setBounds(first.getLeft(),Math.min(first.getTop(),last.getTop()),last.getRight(),Math.max(first.getBottom(),last.getBottom()));}}
    @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);for(GradientDrawable shape:sections)shape.draw(canvas);}
    private int placedColumns=-1,placedCount=-1;
    private void placeCells(int columns){if(placedColumns==columns&&placedCount==getChildCount())return;for(int i=0;i<getChildCount();i++){LayoutParams p=(LayoutParams)getChildAt(i).getLayoutParams();p.rowSpec=spec(i/columns);p.columnSpec=spec(i%columns);p.setGravity(android.view.Gravity.TOP);getChildAt(i).setLayoutParams(p);}placedColumns=columns;placedCount=getChildCount();}
    DockOptionsGrid(Context context){super(context);setColumnCount(4);setWillNotDraw(false);}
    @Override protected void onMeasure(int ws,int hs){
        float d=getResources().getDisplayMetrics().density;int width=MeasureSpec.getSize(ws),columns=width>=560*d?4:2,gap=Math.round(8*d),cell=(width-gap*(columns-1))/columns;
        setColumnCount(Math.max(columns,getColumnCount()));placeCells(columns);int rows=(getChildCount()+columns-1)/columns,natural=0;
        for(int i=0;i<getChildCount();i++){android.view.ViewGroup col=(android.view.ViewGroup)getChildAt(i);for(int j=0;j<col.getChildCount();j++)if(col.getChildAt(j) instanceof OptionGrid){OptionGrid g=(OptionGrid)col.getChildAt(j);g.rowHeight(g.baseRowHeight);}col.forceLayout();col.measure(MeasureSpec.makeMeasureSpec(cell,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED));natural=Math.max(natural,col.getMeasuredHeight());}
        int available=MeasureSpec.getMode(hs)==MeasureSpec.UNSPECIFIED?natural*rows+gap*(rows-1):MeasureSpec.getSize(hs),rowHeight=Math.max(1,(available-gap*(rows-1))/Math.max(1,rows));
        for(int i=0;i<getChildCount();i++){
            android.view.ViewGroup child=(android.view.ViewGroup)getChildAt(i);int flexibleRows=0;for(int j=0;j<child.getChildCount();j++)if(child.getChildAt(j) instanceof OptionGrid)flexibleRows+=((OptionGrid)child.getChildAt(j)).rows();
            int extra=(int)Math.floor((rowHeight-child.getMeasuredHeight())/(double)Math.max(1,flexibleRows));for(int j=0;j<child.getChildCount();j++)if(child.getChildAt(j) instanceof OptionGrid){OptionGrid g=(OptionGrid)child.getChildAt(j);g.rowHeight(Math.max(Math.round(18*d),g.baseRowHeight+extra));}
            child.forceLayout();LayoutParams p=(LayoutParams)child.getLayoutParams();int right=i%columns<columns-1?gap:0,bottom=i/columns<rows-1?gap:0;if(p.width!=cell||p.height!=rowHeight||p.rightMargin!=right||p.bottomMargin!=bottom){p.width=cell;p.height=rowHeight;p.rightMargin=right;p.bottomMargin=bottom;p.setGravity(android.view.Gravity.TOP);child.setLayoutParams(p);}
        }setColumnCount(columns);super.onMeasure(ws,MeasureSpec.makeMeasureSpec(available,MeasureSpec.EXACTLY));
    }
}
