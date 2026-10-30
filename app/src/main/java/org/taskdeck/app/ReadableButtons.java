package org.taskdeck.app;

import android.view.View;
import android.widget.Button;

/** Measure actual text layout and compound content, never an arbitrary percentage cap. */
final class ReadableButtons {
    static int naturalWidth(Button button,int padding){
        CharSequence value=button.getText().toString().replace(" · On"," · Off");
        int text=(int)Math.ceil(button.getPaint().measureText(value,0,value.length()));
        int compounds=button.getCompoundPaddingLeft()+button.getCompoundPaddingRight()-button.getPaddingLeft()-button.getPaddingRight();
        return text+compounds+padding*2+padding/2;
    }
    static void fit(Button button,int width,int padding,int baseHeight){
        int vertical=baseHeight>=padding*3?padding*3/8:padding/4; button.setPadding(padding,vertical,padding,vertical); button.setSingleLine(false); button.setMaxLines(Integer.MAX_VALUE); button.setEllipsize(null);
        button.setMinHeight(baseHeight); button.setMinimumHeight(baseHeight);
        button.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED));
        android.view.ViewGroup.LayoutParams size=button.getLayoutParams(); int height=Math.max(baseHeight,button.getMeasuredHeight()); if(size.width!=width||size.height!=height){ size.width=width; size.height=height; button.setLayoutParams(size); }

    }
}
