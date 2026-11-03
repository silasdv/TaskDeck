package org.taskdeck.app;

import android.graphics.*;
import android.graphics.drawable.Drawable;
import java.util.Locale;

/** Scalable neutral action vectors. Face-button colors belong to ControllerBadge. */
public final class ActionGlyph extends Drawable {
    final String kind; final int color;
    public ActionGlyph(String kind,int color){ this.kind=kind; this.color=color; }
    public static String kind(String label){
        String s=label.toLowerCase(Locale.ROOT);
        if(s.equals("search")) return "search";
        if(s.startsWith("unlock")) return "unlock";
        if(s.startsWith("clear")) return "all";
        if(s.contains("calibrate confirm")||s.startsWith("record")||s.equals("configure shortcuts")) return "record";
        if(s.contains("playstation")) return "playstation";
        if(s.equals("google play")) return "googleplay";
        if(s.startsWith("ram")) return "ramchip";
        if(s.equals("settings backup")) return "diskette";
        if(s.equals("about")) return "info";
        if(s.startsWith("github")) return "github";
        if(s.startsWith("redo")||s.startsWith("reset setup")) return "undo";
        if(s.contains("shizuku")||s.contains("connect")||s.contains("authorize")) return "shizuku";
        if(s.contains("button mapping")||s.equals("controller")||s.startsWith("a/b/x/y")||s.startsWith("playstation")) return "dpad";
        if(s.contains("shortcut")||s.contains("quick swap")) return "shortcut";
        if(s.equals("light")||s.equals("dark")||s.equals("oled")||s.equals("auto")) return "none";
        if(s.contains("add apps")) return "plus";
        if(s.equals("dock / background")||s.equals("icons")) return "brush";
        if(s.contains("dock style")||s.contains("dock appearance")) return "brush";
        if(s.contains("dock behavior")) return "sliders";
        if(s.equals("dock")) return "grid";
        if(s.contains("dock")||s.startsWith("pin ")||s.startsWith("unpin ")) return "pin";
        if(s.equals("dock")) return "grid";
        if(s.equals("auto")) return "theme";
        if(s.contains("feedback")) return "notify";
        if(s.contains("ko-fi")) return "kofi";
        if(s.equals("swap sides")||s.equals("switch apps")) return "switch";
        if(s.startsWith("ram")||s.equals("manage tasks")) return "grid";
        if(s.equals("permissions")) return "info";
        if(s.equals("appearance")) return "theme";
        if(s.equals("controller")) return "record";
        if(s.equals("closing behavior")) return "all";
        if(s.startsWith("−")) return "minus";
        if(s.endsWith("+")) return "plus";
        if(s.equals("diagnostics")) return "info";
        if(s.startsWith("last action")) return "list";
        if(s.startsWith("switch:")) return "switch";
        if(s.contains("windows")||s.equals("choose window")) return "grid";
        if(s.equals("view")||s.equals("grid")||s.equals("cards")||s.equals("fit all")) return "grid";
        if(s.equals("list")) return "list";
        if(s.contains("sort")||s.equals("recent")||s.equals("alphabetical")||s.contains("ram size")) return "sort";
        if(s.contains("refresh")||s.contains("restart")) return "refresh";
        if(s.contains("notification")||s.contains("ram feedback")) return "notify";
        if(s.contains("others")||s.contains("other apps")) return "others";
        if(s.contains("close all")||s.contains("all +")||s.startsWith("y ·")) return "all";
        if(s.contains("close")&&!s.contains("setup")) return "close";
        if(s.contains("exit")||s.contains("stay open")) return "exit";
        if(s.contains("connect")||s.contains("shizuku")||s.contains("authorize")) return "connect";
        if(s.contains("record")||s.contains("combination")||s.contains("calibrate")) return "record";
        if(s.contains("next")||s.contains("continue")||s.contains("finish")) return "next";
        if(s.contains("back")||s.contains("previous")) return "back";
        if(s.contains("open")) return "open";
        if(s.contains("light")||s.contains("dark")||s.contains("oled")) return "theme";
        if(s.contains("save")||s.contains("enable")) return "check";
        return "setup";
    }
    public static int color(String kind,DeckTheme theme){
        return theme.text;
    }
    @Override public void draw(Canvas canvas){
        int save=canvas.save(); canvas.translate(getBounds().left,getBounds().top); canvas.scale(getBounds().width()/24f,getBounds().height()/24f);
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); p.setColor(color); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(2); p.setStrokeCap(Paint.Cap.ROUND); p.setStrokeJoin(Paint.Join.ROUND);
        switch(kind){
            case "none": break;
            case "chevronDown": line(canvas,p,6,9,12,15,18,9);break;
            case "eye": canvas.drawOval(new RectF(2,6,22,18),p);canvas.drawCircle(12,12,3,p);break;
            case "activity": for(int x=5;x<=19;x+=7)canvas.drawCircle(x,12,2,p);break;
            case "search": canvas.drawCircle(10,10,6,p);canvas.drawLine(15,15,21,21,p);break;
            case "unlock": canvas.drawRoundRect(5,10,19,22,2,2,p);canvas.drawArc(new RectF(8,1,18,13),180,190,false,p);canvas.drawCircle(12,16,1,p);break;
            case "dpad": {Path cross=new Path();cross.moveTo(8,2);cross.lineTo(16,2);cross.lineTo(16,8);cross.lineTo(22,8);cross.lineTo(22,16);cross.lineTo(16,16);cross.lineTo(16,22);cross.lineTo(8,22);cross.lineTo(8,16);cross.lineTo(2,16);cross.lineTo(2,8);cross.lineTo(8,8);cross.close();canvas.drawPath(cross,p);break;}
            case "shortcut": canvas.drawOval(new RectF(2,3,14,13),p);canvas.drawOval(new RectF(10,11,22,21),p);canvas.drawLine(8,9,16,15,p);break;
            case "diskette": {
                Path outline=new Path();
                outline.moveTo(4,3);
                outline.lineTo(16,3);
                outline.lineTo(21,8);
                outline.lineTo(21,21);
                outline.lineTo(4,21);
                outline.close();
                canvas.drawPath(outline,p);
                canvas.drawRect(8,3,16,9,p);
                canvas.drawRect(8,13,17,21,p);
                break;
            }
            case "undo": {Path loop=new Path();loop.moveTo(5,8);loop.cubicTo(8,3,15,3,19,7);loop.cubicTo(23,12,20,20,14,21);loop.cubicTo(8,22,3,18,3,13);canvas.drawPath(loop,p);line(canvas,p,5,2,5,8,11,8);break;}
            case "ramchip": canvas.drawRoundRect(6,6,18,18,2,2,p);canvas.drawRect(9,9,15,15,p);for(int n=8;n<=16;n+=4){canvas.drawLine(n,3,n,6,p);canvas.drawLine(n,18,n,21,p);canvas.drawLine(3,n,6,n,p);canvas.drawLine(18,n,21,n,p);}break;
            case "brush": canvas.drawLine(8,15,19,3,p);canvas.drawLine(12,18,22,6,p);canvas.drawLine(19,3,22,6,p);Path brush=new Path();brush.moveTo(8,14);brush.cubicTo(2,13,7,20,2,22);brush.cubicTo(9,24,15,20,12,17);brush.close();canvas.drawPath(brush,p);break;
            case "sliders": for(int x=5;x<=19;x+=7){canvas.drawLine(x,3,x,21,p);canvas.drawCircle(x,x==12?8:16,2,p);}break;
            case "pin": { Path bookmark=new Path(); bookmark.moveTo(6,3); bookmark.lineTo(18,3); bookmark.lineTo(18,21); bookmark.lineTo(12,16); bookmark.lineTo(6,21); bookmark.close(); canvas.drawPath(bookmark,p); break; }
            case "heart": { Path heart=new Path(); heart.moveTo(12,21); heart.cubicTo(-4,10,4,0,12,7); heart.cubicTo(20,0,28,10,12,21); canvas.drawPath(heart,p); break; }
            case "minus": canvas.drawLine(4,12,20,12,p); break;
            case "plus": canvas.drawLine(4,12,20,12,p); canvas.drawLine(12,4,12,20,p); break;
            case "info": canvas.drawCircle(12,12,9,p); canvas.drawCircle(12,7,0.6f,p); canvas.drawLine(12,11,12,17,p); break;
            case "switch": canvas.drawLine(3,7,21,7,p); line(canvas,p,16,3,21,7,16,11); canvas.drawLine(3,17,21,17,p); line(canvas,p,8,13,3,17,8,21); break;
            case "grid": for(int x=0;x<2;x++) for(int y=0;y<2;y++) canvas.drawRoundRect(3+x*11,3+y*11,10+x*11,10+y*11,1,1,p); break;
            case "list": for(int y=5;y<=19;y+=7){ canvas.drawCircle(4,y,1,p); canvas.drawLine(9,y,21,y,p); } break;
            case "open": case "next": canvas.drawLine(4,12,20,12,p); line(canvas,p,13,5,20,12,13,19); break;
            case "back": canvas.drawLine(4,12,20,12,p); line(canvas,p,11,5,4,12,11,19); break;
            case "close": canvas.drawCircle(12,12,9,p); canvas.drawLine(8,8,16,16,p); canvas.drawLine(16,8,8,16,p); break;
            case "exit": canvas.drawRect(3,3,12,21,p); canvas.drawLine(9,12,22,12,p); line(canvas,p,17,7,22,12,17,17); break;
            case "all": canvas.drawRoundRect(6,6,21,21,2,2,p); line(canvas,p,3,17,3,3,17,3); canvas.drawLine(10,10,17,17,p); canvas.drawLine(17,10,10,17,p); break;
            case "others": canvas.drawRoundRect(3,4,10,20,2,2,p); canvas.drawRoundRect(14,4,21,20,2,2,p); canvas.drawLine(15,9,20,15,p); canvas.drawLine(20,9,15,15,p); break;
            case "refresh": canvas.drawArc(new RectF(4,4,20,20),-40,285,false,p); line(canvas,p,6,3,8.6f,4.7f,7.8f,8); break;
            case "sort": canvas.drawLine(3,6,21,6,p); canvas.drawLine(3,12,16,12,p); canvas.drawLine(3,18,10,18,p); break;
            case "connect": canvas.drawRoundRect(6,7,18,17,3,3,p); canvas.drawLine(9,2,9,7,p); canvas.drawLine(15,2,15,7,p); canvas.drawLine(12,17,12,22,p); break;
            case "notify": { Path path=new Path(); path.moveTo(5,17); path.lineTo(7,14); path.lineTo(7,9); path.cubicTo(7,2,17,2,17,9); path.lineTo(17,14); path.lineTo(19,17); path.close(); canvas.drawPath(path,p); canvas.drawArc(new RectF(9,17,15,23),0,180,false,p); break; }
            case "record": canvas.drawCircle(12,12,9,p); p.setStyle(Paint.Style.FILL); canvas.drawCircle(12,12,4,p); break;
            case "check": line(canvas,p,4,12,10,18,21,5); break;
            case "theme": canvas.drawCircle(12,12,9,p); p.setStyle(Paint.Style.FILL); canvas.drawArc(new RectF(3,3,21,21),90,180,true,p); break;
            default: { Path gear=new Path(); for(int i=0;i<32;i++){ double angle=Math.PI*i/16; float radius=i%4==0||i%4==3?10:8; float x=12+(float)Math.cos(angle)*radius,y=12+(float)Math.sin(angle)*radius; if(i==0) gear.moveTo(x,y); else gear.lineTo(x,y); } gear.close(); canvas.drawPath(gear,p); canvas.drawCircle(12,12,3,p); }
        } canvas.restoreToCount(save);
    }
    private void line(Canvas c,Paint p,float a,float b,float x,float y,float d,float e){ Path path=new Path(); path.moveTo(a,b); path.lineTo(x,y); path.lineTo(d,e); c.drawPath(path,p); }
    @Override public void setAlpha(int alpha){}
    @Override public void setColorFilter(ColorFilter filter){}
    @Deprecated @Override public int getOpacity(){ return PixelFormat.TRANSLUCENT; }
}
