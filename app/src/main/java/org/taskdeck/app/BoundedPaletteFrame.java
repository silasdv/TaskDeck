package org.taskdeck.app;
import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
/** Cap the weighted palette to its actual rows, so following controls never sit at the panel bottom. */
final class BoundedPaletteFrame extends FrameLayout {
    BoundedPaletteFrame(Context context){super(context);}
    @Override protected void onMeasure(int ws,int hs){float d=getResources().getDisplayMetrics().density;int width=MeasureSpec.getSize(ws),columns=width>=376*d?8:width>=184*d?4:2,rows=(DeckTheme.ACCENT_KEYS.length+columns-1)/columns,max=Math.round((52*rows+6*(rows-1))*d),height=MeasureSpec.getMode(hs)==MeasureSpec.UNSPECIFIED?max:Math.min(max,MeasureSpec.getSize(hs));super.onMeasure(ws,MeasureSpec.makeMeasureSpec(Math.max(0,height),MeasureSpec.EXACTLY));}
}
