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
            case "refresh": canvas.drawArc(new RectF(4,4,20,20),35,285,false,p); line(canvas,p,15,3,20,5,20,10); break;
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
