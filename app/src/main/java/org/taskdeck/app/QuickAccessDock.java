package org.taskdeck.app;

import android.content.*;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;
import java.util.*;

/** macOS-inspired floating launch dock: magnification, indicators and native drag reordering. */
public final class QuickAccessDock extends LinearLayout {
    public interface Host {
        Drawable icon(String pkg); String label(String pkg); boolean running(String pkg); boolean current(String pkg);
        void launch(String pkg); void options(String pkg); void add(); void changed(); void touch();
        boolean controller();
    }
    private final android.content.SharedPreferences prefs; private final Host host;
    private DeckTheme theme; private LinearLayout icons,thumbLeft,thumbRight; private HorizontalScrollView scroll,leftScroll,rightScroll; private int split;
    public final List<View> items=new ArrayList<>(); private final List<String> packages=new ArrayList<>();
    private boolean expanded=true,dragging,focused; private String draggingPackage; private List<String> dragOrder;
    private List<String> preview; private int previewSplit=-1;
    public void previewSplit(int count){previewSplit=count;}
    public void preview(List<String> values){ preview=new ArrayList<>(values); expanded=true; }
    private boolean individualBoxes;
    private int selected=-1; private long lastScroll;
    public QuickAccessDock(Context context,android.content.SharedPreferences prefs,Host host){ super(context); this.prefs=prefs; this.host=host; setOrientation(VERTICAL); setGravity(Gravity.CENTER); setClipChildren(false); expanded=!"focus".equals(prefs.getString("dockBehavior","always")); setOnDragListener(this::drag); }
    private int dp(int value){ return Math.round(value*getResources().getDisplayMetrics().density); }
    private GradientDrawable rounded(int color){ GradientDrawable shape=new GradientDrawable(); shape.setColor(color); shape.setCornerRadius(dp(18)); return shape; }
    public boolean isExpanded(){ return expanded; }
    public void expand(boolean value){ if(expanded!=value){ expanded=value; render(); } }
    public void update(DeckTheme appTheme){ theme=appTheme; if(!dragging) render(); }
    public String selectedPackage(){ return selected>=0&&selected<packages.size()?packages.get(selected):""; }
    public void blur(){ focused=false; highlight(-2); }
    public void focusItem(int index){ focused=true; if(!expanded) expand(true); if(items.isEmpty()) return; selected=Math.max(0,Math.min(index,items.size()-1)); View item=items.get(selected); item.requestFocus(); highlight(selected); item.requestRectangleOnScreen(new Rect(0,0,item.getWidth(),item.getHeight()),false); }
    public void openSelected(){ if(items.isEmpty()) host.add(); else items.get(Math.max(0,Math.min(selected,items.size()-1))).performClick(); }
    public void selectedOptions(){ String pkg=selectedPackage(); if(pkg.isEmpty()) host.add(); else host.options(pkg); }
    private int currentColor(){ return theme.currentColor(); }
    private int currentFill(){ return theme.background==DeckTheme.named("light").background?Color.rgb(221,244,230):Color.rgb(18,65,44); }
    private void highlight(int index){
        boolean magnify=prefs.getBoolean("dockMagnify",true); for(int i=0;i<items.size();i++){ View item=items.get(i); boolean chosen=i==index,current=i<packages.size()&&host.current(packages.get(i)); boolean none="none".equals(prefs.getString("dockBackground","theme"));int fill=none?Color.TRANSPARENT:current?currentFill():chosen?theme.selected:individualBoxes||packages.get(i).isEmpty()?QuickAccess.backgroundColor(prefs,theme):Color.TRANSPARENT;GradientDrawable bg=new IconShape.Surface(fill,none?"rounded":prefs.getString("dockBackgroundShape","rounded"));if(item instanceof TextView)((TextView)item).setTextColor(QuickAccess.foregroundColor(none?theme.background:fill)); if(chosen||!none&&(current||individualBoxes||packages.get(i).isEmpty())) bg.setStroke(dp(chosen?2:1),current?currentColor():chosen?theme.accent:theme.raised); item.setBackground(bg); float scale=magnify?(chosen?1.12f:Math.abs(i-index)==1?1.03f:1):1; if(item instanceof ViewGroup) ((ViewGroup)item).getChildAt(0).animate().scaleX(scale).scaleY(scale).setDuration(110).start(); }
    }
    private void render(){
        if(theme==null) return; int offset=scroll==null?0:scroll.getScrollX(),leftOffset=leftScroll==null?0:leftScroll.getScrollX(),rightOffset=rightScroll==null?-1:rightScroll.getScrollX(); removeAllViews(); items.clear(); packages.clear(); thumbLeft=null; thumbRight=null; leftScroll=null; rightScroll=null;
        if(!expanded){ TextView handle=new TextView(getContext()); handle.setText("↑"); handle.setTextColor(theme.muted); handle.setTextSize(11); handle.setGravity(Gravity.CENTER); handle.setPadding(dp(12),dp(5),dp(12),dp(5)); handle.setContentDescription("Show Dock"); handle.setOnClickListener(v->{ host.touch(); expand(true); }); addView(handle); return; }
        if(preview==null&&!dragging&&QuickAccess.read(prefs).isEmpty()){
            TextView add=new TextView(getContext()); add.setText("+  Add apps to Dock"); add.setTextColor(QuickAccess.foregroundColor("none".equals(prefs.getString("dockBackground","theme"))?theme.background:QuickAccess.backgroundColor(prefs,theme))); add.setTextSize(11); add.setGravity(Gravity.CENTER); add.setPadding(dp(14),dp(7),dp(14),dp(7)); add.setBackground(rounded(QuickAccess.backgroundColor(prefs,theme))); add.setFocusable(true); add.setFocusableInTouchMode(host.controller()); add.setDefaultFocusHighlightEnabled(false); add.setContentDescription("Add apps to Dock"); add.setOnClickListener(v->host.add()); add.setOnHoverListener((v,event)->{ if(event.getActionMasked()==MotionEvent.ACTION_HOVER_ENTER) highlight(0); else if(event.getActionMasked()==MotionEvent.ACTION_HOVER_EXIT) highlight(-2); return false; }); add.setOnFocusChangeListener((v,focus)->{ if(focus){ selected=0; highlight(0); } }); addView(add,new LayoutParams(-2,dp(48))); items.add(add); packages.add(""); highlight(focused?0:-2); return;
        }
        String style=prefs.getString("dockStyle","thumb"); boolean thumbs="thumb".equals(style); individualBoxes=thumbs; int cell=thumbs?Math.max(24,Math.round(72*QuickAccess.size(prefs)/100f)-12):Math.max(24,Math.round(48*QuickAccess.size(prefs)/100f));
        int capacity=Math.max(8,(int)Math.floor((cell+(thumbs?12:0)-16)/1.12f));if(!"none".equals(prefs.getString("dockBackground","theme"))&&"round".equals(prefs.getString("dockBackgroundShape","rounded"))&&!"round".equals(prefs.getString("dockIconShape","round")))capacity=Math.max(8,(int)Math.floor(capacity*.7071f));int icon=Math.max(5,Math.round(capacity*QuickAccess.iconSize(prefs)/160f));
        scroll=new HorizontalScrollView(getContext()); scroll.setHorizontalScrollBarEnabled(false); scroll.setClipChildren(false); scroll.setClipToPadding(false); scroll.setFillViewport(true); addView(scroll,new LayoutParams(-1,dp(cell+20)));
        LinearLayout center=new LinearLayout(getContext()); center.setGravity(Gravity.CENTER); center.setClipChildren(false); center.setPadding(0,dp(4),0,dp(4)); scroll.addView(center,new HorizontalScrollView.LayoutParams(-1,-1));
        icons=new LinearLayout(getContext()); icons.setGravity(Gravity.CENTER); icons.setClipChildren(false); icons.setPadding(dp(6),dp(6),dp(6),dp(6)); GradientDrawable shell=new IconShape.Surface(QuickAccess.backgroundColor(prefs,theme),prefs.getString("dockBackgroundShape","rounded"));if(!"none".equals(prefs.getString("dockBackground","theme")))shell.setStroke(dp(1),theme.raised); icons.setBackground(shell);
        center.addView(icons,new LayoutParams("shelf".equals(style)||thumbs?-1:-2,-1));
        List<String> order=preview!=null?preview:dragging&&dragOrder!=null?dragOrder:QuickAccess.read(prefs);
        if(order.size()>QuickAccess.limit(prefs))order=new java.util.ArrayList<>(order.subList(0,QuickAccess.limit(prefs)));
        if(thumbs){
            order=QuickAccess.displayOrder(prefs,order); icons.setBackgroundColor(Color.TRANSPARENT); icons.setPadding(0,0,0,0); split=preview!=null&&previewSplit>=0?Math.min(order.size(),previewSplit):QuickAccess.leftCount(prefs,order.size());
            leftScroll=new HorizontalScrollView(getContext()); rightScroll=new HorizontalScrollView(getContext()); for(HorizontalScrollView side:new HorizontalScrollView[]{leftScroll,rightScroll}){ side.setHorizontalScrollBarEnabled(false); side.setFillViewport(true); side.setClipChildren(false); }
            LayoutParams leftSize=new LayoutParams(0,-1,1); leftSize.rightMargin=dp(12); icons.addView(leftScroll,leftSize); icons.addView(rightScroll,new LayoutParams(0,-1,1));
            FrameLayout leftFrame=new FrameLayout(getContext()),rightFrame=new FrameLayout(getContext()); leftScroll.addView(leftFrame,new HorizontalScrollView.LayoutParams(-2,-1)); rightScroll.addView(rightFrame,new HorizontalScrollView.LayoutParams(-2,-1));
            thumbLeft=new LinearLayout(getContext()); thumbRight=new LinearLayout(getContext()); for(LinearLayout side:new LinearLayout[]{thumbLeft,thumbRight}){ side.setGravity(Gravity.CENTER); side.setBaselineAligned(false); side.setClipChildren(false); side.setBackgroundColor(Color.TRANSPARENT); side.setPadding(0,0,0,0); }
            leftFrame.addView(thumbLeft,new FrameLayout.LayoutParams(-2,-1,Gravity.LEFT)); rightFrame.addView(thumbRight,new FrameLayout.LayoutParams(-2,-1,Gravity.RIGHT)); if(order.size()<=split) rightScroll.setVisibility(INVISIBLE);
        }
        if(order.isEmpty()) addItem("",icon,cell); else for(String pkg:order) addItem(pkg,icon,cell);
        HorizontalScrollView viewport=scroll; post(()->viewport.scrollTo(offset,0));
        if(thumbs){ HorizontalScrollView left=leftScroll,right=rightScroll; post(()->{ left.scrollTo(leftOffset,0); right.scrollTo(rightOffset<0?Integer.MAX_VALUE:rightOffset,0); }); }
        post(()->{ if(focused&&host.controller()&&isAttachedToWindow()&&!dragging) focusItem(selected); });
        if(selected>=items.size()) selected=items.size()-1; highlight(preview!=null?-2:focused?selected:-2);
    }
    private void addItem(String pkg,int icon,int cell){
        FrameLayout item=new FrameLayout(getContext()); item.setFocusable(true); item.setFocusableInTouchMode(host.controller()); item.setDefaultFocusHighlightEnabled(false); item.setClipChildren(false); item.setTag(pkg);
        String name=pkg.isEmpty()?"Add apps to Dock":host.label(pkg); boolean running=!pkg.isEmpty()&&host.running(pkg),current=!pkg.isEmpty()&&host.current(pkg);
        ImageView image=new ImageView(getContext()); image.setImageDrawable(pkg.isEmpty()?new ActionGlyph("plus",theme.text):new IconArtwork(new DockIcon(host.icon(pkg),prefs.getString("dockIconShape","round")),prefs.getString("dockIconLook","original"),false,theme.accent)); image.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO); FrameLayout.LayoutParams imageSize=new FrameLayout.LayoutParams(dp(icon),dp(icon),Gravity.CENTER);item.addView(image,imageSize);
        View indicator=new View(getContext()); GradientDrawable dot=new GradientDrawable(); dot.setShape(GradientDrawable.OVAL); dot.setColor(current?currentColor():theme.muted); indicator.setBackground(dot); FrameLayout.LayoutParams point=new FrameLayout.LayoutParams(dp(current?8:4),dp(4),Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL); point.bottomMargin=dp(2); item.addView(indicator,point); indicator.setVisibility(prefs.getBoolean("dockIndicators",true)&&(running||current)?VISIBLE:INVISIBLE);
        item.setContentDescription(name+(current?", Current app":running?", running":"")); item.setTooltipText(name); LinearLayout target=thumbLeft==null?icons:items.size()<split?thumbLeft:thumbRight; LayoutParams itemSize=new LayoutParams(dp(cell+(thumbLeft!=null?12:0)),-1); if(thumbLeft!=null&&target.getChildCount()>0) itemSize.leftMargin=dp(8); target.addView(item,itemSize); packages.add(pkg); items.add(item);
        item.setOnClickListener(v->{ selected=items.indexOf(item); if(pkg.isEmpty()) host.add(); else host.launch(pkg); });
        item.setOnFocusChangeListener((v,focused)->{ if(focused){ selected=items.indexOf(v); highlight(selected); } });
        item.setOnHoverListener((v,event)->{ if(event.getActionMasked()==MotionEvent.ACTION_HOVER_ENTER) highlight(items.indexOf(v)); else if(event.getActionMasked()==MotionEvent.ACTION_HOVER_EXIT) highlight(-2); return false; });
        item.setOnLongClickListener(v->{ if(preview!=null) return false; if(pkg.isEmpty()){ host.add(); return true; } host.touch(); dragging=true; draggingPackage=pkg; dragOrder=QuickAccess.read(prefs); boolean started=v.startDragAndDrop(ClipData.newPlainText("TaskDeck Dock",pkg),new View.DragShadowBuilder(v),pkg,0); if(!started){ dragging=false; dragOrder=null; } return started; });
    }
    private boolean drag(View view,DragEvent event){
        if(event.getAction()==DragEvent.ACTION_DRAG_STARTED) return event.getLocalState() instanceof String&&QuickAccess.read(prefs).contains((String)event.getLocalState());
        if(!dragging) return false;
        if(event.getAction()==DragEvent.ACTION_DRAG_LOCATION){
            int[] xy=new int[2]; getLocationOnScreen(xy); float screen=xy[0]+event.getX(); int nearest=-1; float distance=Float.MAX_VALUE;
            for(int i=0;i<items.size();i++){ Rect rect=new Rect(); if(items.get(i).getGlobalVisibleRect(rect)){ float delta=Math.abs(screen-rect.exactCenterX()); if(delta<distance){ nearest=i; distance=delta; } } }
            if(nearest>=0){ List<String> visible=QuickAccess.displayOrder(prefs,dragOrder); if(visible.indexOf(draggingPackage)!=nearest){ dragOrder=QuickAccess.storageOrder(prefs,QuickAccess.move(visible,draggingPackage,nearest)); render(); } }
            HorizontalScrollView edge=leftScroll==null?scroll:event.getX()<getWidth()/2f?leftScroll:rightScroll;
            long now=android.os.SystemClock.uptimeMillis(); if(now-lastScroll>80&&edge!=null){ int[] edgeXY=new int[2]; edge.getLocationOnScreen(edgeXY); float local=screen-edgeXY[0]; if(local<dp(40)) edge.scrollBy(-dp(20),0); else if(local>edge.getWidth()-dp(40)) edge.scrollBy(dp(20),0); lastScroll=now; } return true;
        }
        if(event.getAction()==DragEvent.ACTION_DROP){ QuickAccess.save(prefs,dragOrder); return true; }
        if(event.getAction()==DragEvent.ACTION_DRAG_ENDED){ dragging=false; dragOrder=null; render(); host.changed(); return true; }
        return true;
    }
}
