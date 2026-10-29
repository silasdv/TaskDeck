package org.taskdeck.app;

import android.graphics.Color;

public final class DeckTheme {
    public final int background,surface,raised,text,muted,accent,selected;
    private DeckTheme(String bg,String surface,String raised,String text,String muted,String accent,String selected){
        background=Color.parseColor(bg); this.surface=Color.parseColor(surface); this.raised=Color.parseColor(raised);
        this.text=Color.parseColor(text); this.muted=Color.parseColor(muted); this.accent=Color.parseColor(accent); this.selected=Color.parseColor(selected);
    }
    public static DeckTheme named(String name){
        if("light".equals(name)) return new DeckTheme("#EFF3FA","#FFFFFF","#E0E8F5","#182238","#4C5C77","#245CC5","#D9E9FF");
        if("oled".equals(name)) return new DeckTheme("#000000","#101014","#202028","#FFFFFF","#B4B6C8","#B59AFF","#251A3B");
        return new DeckTheme("#101725","#1A263A","#28364D","#F1F6FF","#AABBD2","#6EE7D0","#163F45");
    }
}
