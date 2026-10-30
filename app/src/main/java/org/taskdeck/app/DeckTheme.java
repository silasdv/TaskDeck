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
    public static final String[] ACCENT_KEYS={"theme","red","green","blue","white","purple","cyan","violet","pink","orange","amber","teal","lime"};
    public static final String[] ACCENT_NAMES={"Theme","Red","Green","Blue","White","Purple","Cyan","Violet","Pink","Orange","Amber","Teal","Lime"};
    public static int accentColor(String key){ switch(key){ case "red":return Color.rgb(244,83,97); case "green":return Color.rgb(75,207,127); case "blue":return Color.rgb(81,148,255); case "white":return Color.WHITE; case "purple":return Color.rgb(181,154,255); case "cyan":return Color.rgb(53,211,235); case "violet":return Color.rgb(144,105,255); case "pink":return Color.rgb(246,119,188); case "orange":return Color.rgb(255,157,73); case "amber":return Color.rgb(243,196,80); case "teal":return Color.rgb(63,205,180); case "lime":return Color.rgb(169,218,86); default:return 0; } }
    private DeckTheme(int background,int surface,int raised,int text,int muted,int accent,int selected){ this.background=background; this.surface=surface; this.raised=raised; this.text=text; this.muted=muted; this.accent=accent; this.selected=selected; }
    public DeckTheme withAccent(String key){ int color=accentColor(key); if(color==0) return this; int fill=Color.rgb((Color.red(background)*4+Color.red(color))/5,(Color.green(background)*4+Color.green(color))/5,(Color.blue(background)*4+Color.blue(color))/5); return new DeckTheme(background,surface,raised,text,muted,color,fill); }
    public int currentColor(){ return background==named("light").background?Color.rgb(22,130,68):Color.rgb(82,217,151); }
    public static DeckTheme resolve(String name,int mode){ return named("auto".equals(name)?((mode&android.content.res.Configuration.UI_MODE_NIGHT_MASK)==android.content.res.Configuration.UI_MODE_NIGHT_YES?"dark":"light"):name); }
}
