package org.taskdeck.app;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.*;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.*;

/** Explicit card selection never depends on ListView focus or Android's touch mode. */
public class SwitcherActivity extends Activity {
    enum Page { APPS, SETUP, CONNECT, CHORD, CONFIRM, SHORTCUTS, LAYOUT, RESULTS, WINDOWS, DIAGNOSTICS }
    private TaskDeckApp app;
    private Page page;
    private DeckTheme theme;
    private LinearLayout root,cardRow,sidebar;
    private FrameLayout appContent;
    private ScrollView appScroll;
    private GridLayout appGrid;
    private TextView compactMemory;
    private Button setupPrevious,setupNext;
    private int gridColumns=1,renderWidth,renderHeight,iconAccent;
    private HorizontalScrollView rail;
    private TextView status,memory,recordLive,recordSaved,backgroundInfo;
    private LinearLayout formMain,formInfo;
    private final List<JSONObject> backgroundApps=new ArrayList<>();
    private Button saveChord;
    private PopupWindow protectionPopup;
    private ScrollView pageScroll;
    private boolean pendingRefresh,controllerMode=true,resetSelection=true,captureEntry=true;
    private int setupStep,controlIndex=-1,catalogGeneration,detailsPage;
    private boolean recordingQuickSwitch,readError,fitOverflow;
    private String memoryStatus="Not sampled";
    private JSONObject windowGroup;
    private TextView operationInfo;
    private final List<JSONObject> rawTasks=new ArrayList<>();
    private JSONObject entryTask;
    private final List<JSONObject> tasks=new ArrayList<>();
    private final List<View> cards=new ArrayList<>();
    private final android.util.LruCache<String,Drawable.ConstantState> iconCache=new android.util.LruCache<>(64);
    private final List<Button> controls=new ArrayList<>(),tools=new ArrayList<>();
    private final DeckSelection selection=new DeckSelection();
    private final Handler main=new Handler(Looper.getMainLooper());
    private boolean busy,toolZone;
    private int toolIndex,stickDirection;
    private final Set<String> heldKeys=new LinkedHashSet<>(),capturedKeys=new LinkedHashSet<>();
    private Integer captureDevice;
    private String feedback="";
    private final Runnable repeat=new Runnable(){ public void run(){ if(stickDirection!=0){ navigate(stickDirection); main.postDelayed(this,140); } } };
    private android.content.SharedPreferences prefs(){ return getSharedPreferences("settings",MODE_PRIVATE); }
    private int dp(int n){ return Math.round(n*getResources().getDisplayMetrics().density); }
    private GradientDrawable rounded(int color){ GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(18)); return d; }
    private TextView text(LinearLayout parent,String value,int size,boolean muted){
        TextView view=new TextView(this); view.setText(value); view.setTextSize(size); view.setTextColor(muted?theme.muted:theme.text); view.setPadding(0,dp(5),0,dp(5)); parent.addView(view); return view;
    }
    private Button button(LinearLayout parent,String value,Runnable action){
        Button b=new Button(this); b.setText(value); b.setAllCaps(false); b.setTextSize(14); b.setTextColor(theme.text); b.setFocusableInTouchMode(controllerMode); b.setDefaultFocusHighlightEnabled(false);
        String kind=ActionGlyph.kind(value); int tint=ActionGlyph.color(kind,theme); b.setTextColor(tint);
        Drawable glyph=new ActionGlyph(kind,tint); glyph.setBounds(0,0,dp(18),dp(18)); b.setCompoundDrawables(glyph,null,null,null); b.setCompoundDrawablePadding(dp(7)); b.setMinWidth(0); b.setMinimumWidth(0);
        StateListDrawable bg=new StateListDrawable(); GradientDrawable focused=rounded(theme.selected); focused.setStroke(dp(3),tint);
        GradientDrawable selected=rounded(theme.selected); selected.setStroke(dp(1),theme.accent);
        bg.addState(new int[]{android.R.attr.state_activated},focused); bg.addState(new int[]{android.R.attr.state_focused},focused); bg.addState(new int[]{android.R.attr.state_pressed},rounded(theme.selected)); bg.addState(new int[]{android.R.attr.state_selected},selected); bg.addState(new int[]{},rounded(theme.raised));
        boolean compact=getResources().getConfiguration().screenHeightDp<440;
        b.setBackground(bg); b.setBackgroundTintList(null); b.setMinHeight(dp(compact?36:44)); b.setMinimumHeight(dp(compact?36:44)); b.setPadding(dp(13),dp(5),dp(13),dp(5)); b.setOnClickListener(v->action.run());
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-2,-2); params.setMargins(0,dp(compact?3:5),dp(8),dp(compact?3:5)); parent.addView(b,params); controls.add(b); return b;
    }
    private LinearLayout row(LinearLayout parent){ LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL); parent.addView(row); return row; }
    private LinearLayout section(LinearLayout parent,String title,String description){
        LinearLayout panel=new LinearLayout(this); panel.setOrientation(LinearLayout.VERTICAL); panel.setPadding(dp(18),dp(12),dp(18),dp(12)); panel.setBackground(rounded(theme.surface));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.bottomMargin=dp(12); parent.addView(panel,p);
        text(panel,title,20,false).setTypeface(null,Typeface.BOLD); if(!description.isEmpty()) text(panel,description,14,true); return panel;
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state); app=(TaskDeckApp)getApplication(); setupStep=state==null?0:state.getInt("setupStep",0);
        if(Build.VERSION.SDK_INT>=30) getWindow().setDecorFitsSystemWindows(false);
        else getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        Page initial=prefs().getBoolean("setupComplete",false)?Page.APPS:Page.SETUP;
        if(state!=null) try { initial=Page.valueOf(state.getString("page",initial.name())); } catch(IllegalArgumentException ignored){}
        if(initial==Page.CHORD || initial==Page.CONFIRM || initial==Page.WINDOWS) initial=Page.SETUP;
        show(initial);
    }
    @Override public void onSaveInstanceState(Bundle out){ out.putString("page",page.name()); out.putInt("setupStep",setupStep); super.onSaveInstanceState(out); }
    @Override protected void onResume(){
        super.onResume(); TaskDeckApp.recordingShortcut=page==Page.CHORD || page==Page.CONFIRM;
        app.changed=this::connectionChanged; if(page==Page.SETUP) show(Page.SETUP); updateStatus();
        if(app.backend!=null) refresh(); else app.connect(false);
    }
    @Override protected void onPause(){ app.changed=null; TaskDeckApp.recordingShortcut=false; stopStick(); super.onPause(); }
    @Override protected void onNewIntent(Intent intent){ super.onNewIntent(intent); setIntent(intent); if(intent.getBooleanExtra("controllerEntry",false)) controllerMode=true; resetSelection=true; captureEntry=true; entryTask=null; show(prefs().getBoolean("setupComplete",false)?Page.APPS:Page.SETUP); if(app.backend!=null) refresh(); }
    private void connectionChanged(){
        if(isFinishing() || isDestroyed()) return;
        if(app.backend==null){ busy=false; catalogGeneration++; rawTasks.clear(); tasks.clear(); selection.update(Collections.emptyList()); if(page==Page.DIAGNOSTICS) show(Page.DIAGNOSTICS); else { if(page==Page.APPS) renderCards(); updateStatus(); } }
        else if(page==Page.DIAGNOSTICS){ show(Page.DIAGNOSTICS); }
        else { updateStatus(); refresh(); }
    }
    private void show(Page next){
        int restoreY=page==Page.SETUP && next==Page.SETUP && pageScroll!=null?pageScroll.getScrollY():0;
        int restoreFocus=page==next&&next!=Page.APPS?controlIndex:-1;
        if(next==Page.APPS && page!=Page.APPS) resetSelection=true;
        if(protectionPopup!=null) protectionPopup.dismiss(); pageScroll=null;
        stopStick(); feedback=""; TaskDeckApp.recordingShortcut=next==Page.CHORD || next==Page.CONFIRM; page=next; toolZone=false;
        controls.clear(); controlIndex=-1; tools.clear(); cards.clear(); cardRow=null; rail=null; appScroll=null; appGrid=null; appContent=null; compactMemory=null; operationInfo=null; setupPrevious=null; setupNext=null; renderWidth=0; renderHeight=0; status=null; memory=null;
        theme=DeckTheme.named(prefs().getString("theme","dark"));
        if(iconAccent!=theme.accent){ iconCache.evictAll(); iconAccent=theme.accent; }
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(theme.background); root.setPadding(dp(20),dp(12),dp(20),dp(12));
        root.setOnApplyWindowInsetsListener((view,insets)->{
            if(Build.VERSION.SDK_INT>=30){ android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars()); view.setPadding(dp(20)+bars.left,dp(10)+bars.top,dp(20)+bars.right,dp(10)+bars.bottom); }
            else view.setPadding(dp(20)+insets.getSystemWindowInsetLeft(),dp(10)+insets.getSystemWindowInsetTop(),dp(20)+insets.getSystemWindowInsetRight(),dp(10)+insets.getSystemWindowInsetBottom());
            return insets;
        }); root.setFocusableInTouchMode(true); setContentView(root); if(!controllerMode) root.requestFocus();
        if(page==Page.APPS) appsPage(); else if(page==Page.SETUP) setupPage(); else if(page==Page.CONNECT) connectPage(); else if(page==Page.SHORTCUTS) shortcutsPage(); else if(page==Page.LAYOUT) layoutPage(); else if(page==Page.RESULTS) resultsPage(); else if(page==Page.WINDOWS) windowsPage(); else if(page==Page.DIAGNOSTICS) diagnosticsPage(); else recordPage();
        if(restoreY>0 && pageScroll!=null){ ScrollView scroll=pageScroll; main.post(()->scroll.scrollTo(0,restoreY)); }
        if(controllerMode && restoreFocus>=0 && restoreFocus<controls.size()){ int index=restoreFocus; main.post(()->focusControl(index)); }
        root.requestApplyInsets();
    }
    private boolean landscape(){ return getResources().getConfiguration().orientation==android.content.res.Configuration.ORIENTATION_LANDSCAPE; }
    private void formPage(String title){
        LinearLayout heading=row(root); TextView label=text(heading,title,23,false); label.setTypeface(null,Typeface.BOLD); label.setMaxLines(1); label.setEllipsize(android.text.TextUtils.TruncateAt.END); label.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1));
        Button back=button(heading,"Back",this::back); ((LinearLayout.LayoutParams)back.getLayoutParams()).leftMargin=dp(24);
        LinearLayout content=new LinearLayout(this); content.setOrientation(landscape()?LinearLayout.HORIZONTAL:LinearLayout.VERTICAL);
        if(landscape()) root.addView(content,new LinearLayout.LayoutParams(-1,0,1));
        else { pageScroll=new ScrollView(this); root.addView(pageScroll,new LinearLayout.LayoutParams(-1,0,1)); pageScroll.addView(content); }
        formMain=new LinearLayout(this); formInfo=new LinearLayout(this);
        for(LinearLayout pane:new LinearLayout[]{formMain,formInfo}){ pane.setOrientation(LinearLayout.VERTICAL); pane.setPadding(dp(12),dp(7),dp(12),dp(7)); pane.setBackground(rounded(theme.surface)); }
        if(landscape()){
            LinearLayout.LayoutParams left=new LinearLayout.LayoutParams(0,-1,1.15f); left.rightMargin=dp(12); content.addView(formMain,left); content.addView(formInfo,new LinearLayout.LayoutParams(0,-1,1));
        } else { content.addView(formMain,new LinearLayout.LayoutParams(-1,-2)); LinearLayout.LayoutParams info=new LinearLayout.LayoutParams(-1,-2); info.topMargin=dp(10); content.addView(formInfo,info); }
        status=smallNote(formInfo,""); status.setVisibility(View.GONE);
    }
    private TextView note(LinearLayout parent,String value){ TextView v=text(parent,value,13,true); v.setPadding(0,dp(2),0,dp(2)); return v; }
    private Button controllerButton(LinearLayout parent,String key,String label,String icon,Runnable action){
        Button b=button(parent,label,action); Drawable badge=new ControllerBadge(key,theme.text); badge.setBounds(0,0,dp(key.length()>1?30:24),dp(24));
        Drawable glyph=null; if(key.equals("R1")){ glyph=new ActionGlyph(icon,theme.text); glyph.setBounds(0,0,dp(16),dp(16)); }
        b.setCompoundDrawables(badge,null,glyph,null); b.setCompoundDrawablePadding(dp(5)); b.setContentDescription(key+": "+label);
        b.setTextSize(getResources().getConfiguration().screenWidthDp<760?12:14); b.setSingleLine(true); b.setPadding(dp(6),dp(6),dp(6),dp(6)); b.setMinHeight(dp(44)); b.setMinimumHeight(dp(44));
        ((LinearLayout.LayoutParams)b.getLayoutParams()).rightMargin=dp(5); return b;
    }

    private int sidebarWidth(){ return getResources().getConfiguration().screenWidthDp>=760?200:180; }
    private Drawable appIcon(String name){
        Drawable.ConstantState cached=iconCache.get(name); if(cached!=null) return cached.newDrawable(getResources()).mutate();
        Drawable value=null; try { value=getPackageManager().getApplicationIcon(name); } catch(PackageManager.NameNotFoundException ignored){}
        if(value==null){ value=getDrawable(R.drawable.ic_tasks); if(value!=null){ value=value.mutate(); value.setTint(theme.accent); } }
        if(value!=null&&value.getConstantState()!=null) iconCache.put(name,value.getConstantState()); return value;
    }

    private void appsPage(){
        LinearLayout header=row(root); TextView title=text(header,"TaskDeck",24,false); title.setTypeface(null,Typeface.BOLD); title.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1));
        LinearLayout toolRow=getResources().getConfiguration().screenWidthDp<600?row(root):header;
        tools.add(button(toolRow,"Refresh",this::refresh)); tools.add(button(toolRow,"Sort",this::sortMenu)); tools.add(button(toolRow,"View",this::viewMenu));
        if(getResources().getConfiguration().screenWidthDp<600) toolRow=row(root);
        tools.add(button(toolRow,"Connect",()->show(Page.CONNECT))); tools.add(button(toolRow,"Setup",()->{ setupStep=0; show(Page.SETUP); }));
        LinearLayout stats=row(root); status=text(stats,"",12,true); status.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1)); compactMemory=text(stats,"",12,true); status.setMaxLines(1); status.setEllipsize(android.text.TextUtils.TruncateAt.END);
        operationInfo=text(root,"",12,true); operationInfo.setMaxLines(2); operationInfo.setEllipsize(android.text.TextUtils.TruncateAt.END); updateOperationInfo();
        LinearLayout deck=row(root); deck.setLayoutParams(new LinearLayout.LayoutParams(-1,0,1)); deck.setGravity(Gravity.TOP);
        appContent=new FrameLayout(this); deck.addView(appContent,new LinearLayout.LayoutParams(0,-1,1));
        sidebar=new LinearLayout(this); sidebar.setOrientation(LinearLayout.VERTICAL); sidebar.setPadding(dp(12),0,0,0);
        if(landscape()) deck.addView(sidebar,new LinearLayout.LayoutParams(dp(sidebarWidth()),-1)); else root.addView(sidebar);
        appContent.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{ if(r-l>0&&b-t>0&&(r-l!=renderWidth||b-t!=renderHeight)){ renderWidth=r-l; renderHeight=b-t; renderCards(); } });
        memory=text(sidebar,"",13,false); backgroundInfo=text(sidebar,"",11,true); backgroundInfo.setPadding(0,dp(2),0,dp(2)); backgroundInfo.setMaxLines(7); backgroundInfo.setEllipsize(android.text.TextUtils.TruncateAt.END);
        updateStatus(); updateMemory(); renderCards();
        HorizontalScrollView footer=new HorizontalScrollView(this); footer.setHorizontalScrollBarEnabled(false); root.addView(footer,new LinearLayout.LayoutParams(-1,-2));
        LinearLayout actions=new LinearLayout(this); actions.setGravity(Gravity.CENTER_VERTICAL); footer.addView(actions);
        controllerButton(actions,"A","Open app","open",this::openSelected); controllerButton(actions,"B","Exit menu","exit",this::finish); controllerButton(actions,"X","Close app","close",this::closeSelected);
        controllerButton(actions,"Y",prefs().getBoolean("exitAfterBulk",true)?"Close all & exit":"Close all apps","all",this::closeAll);
        controllerButton(actions,"R1","Close other apps","others",this::closeOthers);
    }

    private void renderCards(){
        if(appContent==null) return;
        String mode=prefs().getString("view","grid"); boolean horizontal="cards".equals(mode),list="list".equals(mode),fit="fit".equals(mode);
        sidebar.setVisibility(horizontal||tasks.stream().noneMatch(this::closable)?View.VISIBLE:View.GONE); compactMemory.setVisibility(sidebar.getVisibility()==View.VISIBLE?View.GONE:View.VISIBLE);
        appContent.removeAllViews(); cards.clear(); rail=null; appScroll=null; appGrid=null;
        cardRow=new LinearLayout(this); cardRow.setGravity(Gravity.CENTER_VERTICAL);
        int available=appContent.getWidth()>0?Math.round(appContent.getWidth()/getResources().getDisplayMetrics().density):getResources().getConfiguration().screenWidthDp-40-(landscape()&&sidebar.getVisibility()==View.VISIBLE?sidebarWidth():0);
        int height=appContent.getHeight()>0?Math.round(appContent.getHeight()/getResources().getDisplayMetrics().density):Math.max(120,getResources().getConfiguration().screenHeightDp-130);
        if(horizontal){ rail=new HorizontalScrollView(this); rail.setHorizontalScrollBarEnabled(false); appContent.addView(rail,new FrameLayout.LayoutParams(-1,-1)); rail.addView(cardRow,new HorizontalScrollView.LayoutParams(-2,-1)); gridColumns=1; }
        else {
            gridColumns=list?1:fit?Math.max(1,(int)Math.ceil(Math.sqrt(Math.max(1,tasks.size())*available/(double)Math.max(1,height)*1.2))):Math.max(2,prefs().getInt("columns",0)>0?prefs().getInt("columns",0):available/128);
            appGrid=new GridLayout(this); appGrid.setColumnCount(gridColumns); appGrid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
            int minimum=(int)Math.ceil(prefs().getInt("minLabelSp",12)*getResources().getDisplayMetrics().scaledDensity/getResources().getDisplayMetrics().density*1.4)+8;
            fitOverflow=fit&&height/Math.max(1,(tasks.size()+gridColumns-1)/gridColumns)-6<minimum;
            if(fit&&!fitOverflow) appContent.addView(appGrid,new FrameLayout.LayoutParams(-1,-1));
            else { appScroll=new ScrollView(this); appContent.addView(appScroll,new FrameLayout.LayoutParams(-1,-1)); appScroll.addView(appGrid,new ScrollView.LayoutParams(-1,-2)); }
        }
        if(tasks.isEmpty()){ if(!horizontal){ appContent.removeAllViews(); appContent.addView(cardRow,new FrameLayout.LayoutParams(-1,-1)); } text(cardRow,app.backend==null?"Connect Shizuku to load your apps.":"Nothing to close",20,true); toolZone=true; toolIndex=0; markSelection(); return; }
        if(tasks.stream().noneMatch(this::closable)){ toolZone=true; toolIndex=0; }
        int width=horizontal?dp(Math.max(116,Math.min(190,available/3-14))):dp(Math.max(1,available/gridColumns-6));
        int minLabel=prefs().getInt("minLabelSp",12),minimum=(int)Math.ceil(minLabel*getResources().getDisplayMetrics().scaledDensity/getResources().getDisplayMetrics().density*1.4)+8;
        int cellHeight=horizontal?-1:dp(list?Math.max(58,minimum+18):fit?Math.max(minimum,height/Math.max(1,(tasks.size()+gridColumns-1)/gridColumns)-6):Math.max(70,Math.min(104,height/2-6)));
        boolean compact=getResources().getConfiguration().screenHeightDp<440;
        boolean tiny=fit&&cellHeight<dp(minimum+48),micro=fit&&cellHeight<dp(minimum+18);
        for(JSONObject task:tasks){
            LinearLayout card=new LinearLayout(this); card.setOrientation(list?LinearLayout.HORIZONTAL:LinearLayout.VERTICAL); card.setGravity(list?Gravity.CENTER_VERTICAL:Gravity.CENTER); card.setPadding(dp(horizontal?12:5),dp(horizontal?(compact?6:14):2),dp(horizontal?12:5),dp(horizontal?(compact?6:14):2)); card.setFocusableInTouchMode(controllerMode); card.setDefaultFocusHighlightEnabled(false);
            if(horizontal){ LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(width,-1); p.setMargins(dp(2),dp(8),dp(12),dp(8)); cardRow.addView(card,p); }
            else { GridLayout.LayoutParams p=new GridLayout.LayoutParams(); p.width=width; p.height=cellHeight; p.setMargins(dp(3),dp(3),dp(3),dp(3)); appGrid.addView(card,p); }
            ImageView icon=new ImageView(this); icon.setImageDrawable(appIcon(task.optString("package"))); icon.setContentDescription(task.optString("label")); int iconSize=horizontal?(compact?40:60):list?30:24; if(tiny) icon.setVisibility(View.GONE); card.addView(icon,new LinearLayout.LayoutParams(dp(iconSize),dp(iconSize)));
            LinearLayout labels=card; if(list){ labels=new LinearLayout(this); labels.setOrientation(LinearLayout.VERTICAL); LinearLayout.LayoutParams labelSize=new LinearLayout.LayoutParams(0,-2,1); labelSize.leftMargin=dp(10); card.addView(labels,labelSize); }
            TextView name=text(labels,task.optString("label")+(task.optInt("windowCount",1)>1?" · "+task.optInt("windowCount")+" windows":""),Math.max(minLabel,horizontal?(compact?16:18):micro?9:tiny?11:14),false); name.setGravity(list?Gravity.START:Gravity.CENTER); name.setMaxLines(1); name.setEllipsize(android.text.TextUtils.TruncateAt.END); name.setTypeface(null,Typeface.BOLD);
            long kb=task.optLong("pssKb",-1); String state=task.optString("memoryState","unavailable");
            String ram=kb>=0?"~"+Math.round(kb/1024d)+" MiB app RAM"+(state.equals("shared")?" · shared":""):state.equals("loading")?"RAM loading…":state.equals("not-reported")?"No process in snapshot":"RAM unavailable";
            TextView usage=text(labels,ram,horizontal?12:10,true); usage.setMaxLines(1); usage.setEllipsize(android.text.TextUtils.TruncateAt.END); if(micro) usage.setVisibility(View.GONE); usage.setGravity(list?Gravity.START:Gravity.CENTER);
            boolean locked=locked(task);
            TextView badge=text(labels,task.optBoolean("launcher")?(locked?"Launcher · protected":launcherIdle(task)?"Launcher · not running":"Launcher · unlocked"):locked?"Protected":"X to close",12,true); badge.setTextColor(locked?theme.muted:theme.accent); badge.setGravity(Gravity.CENTER);
            if(!horizontal){ name.setPadding(0,0,0,0); usage.setPadding(0,0,0,0); badge.setPadding(0,0,0,0); badge.setVisibility(list||tiny||!locked||cellHeight<dp(90+Math.max(0,minLabel-14)*2)?View.GONE:View.VISIBLE); }
            if(horizontal&&compact){ name.setPadding(0,dp(5),0,dp(2)); usage.setPadding(0,dp(2),0,dp(2)); badge.setPadding(0,dp(2),0,dp(2)); }
            if(locked){ Drawable lock=getDrawable(R.drawable.ic_lock); if(lock!=null){ lock.setTint(theme.accent); lock.setBounds(0,0,dp(horizontal?18:12),dp(horizontal?18:12)); TextView target=badge.getVisibility()==View.GONE?name:badge; target.setCompoundDrawables(lock,null,null,null); target.setCompoundDrawablePadding(dp(5)); } }
            card.setOnClickListener(v->{ selection.select(task.optInt("id")); toolZone=false; markSelection(); openSelected(); });
            card.setOnLongClickListener(v->{ selection.select(task.optInt("id")); toolZone=false; markSelection(); protectSelected(); return true; });
            cards.add(card);
        }
        markSelection();
    }
    private void markSelection(){
        if(page!=Page.APPS) return;
        if(cards.isEmpty()) toolZone=true;
        if(!tools.isEmpty()){ toolIndex=Math.max(0,Math.min(toolIndex,tools.size()-1)); for(int i=0;i<tools.size();i++) tools.get(i).setActivated(controllerMode&&toolZone&&i==toolIndex); if(controllerMode&&toolZone) tools.get(toolIndex).requestFocus(); }
        for(int i=0;i<cards.size();i++){
            boolean chosen=i==selection.index(); GradientDrawable bg=rounded(chosen?theme.selected:theme.surface);
            bg.setStroke(dp(chosen&&!toolZone?4:1),chosen?theme.accent:theme.raised); cards.get(i).setBackground(bg); cards.get(i).setSelected(chosen);
        }
        updateStatus();
        if(!toolZone && !cards.isEmpty()){
            View card=cards.get(selection.index()); if(controllerMode) card.requestFocus();
            main.post(()->{ if(page==Page.APPS && rail!=null && card.getParent()==cardRow) rail.smoothScrollTo(Math.max(0,card.getLeft()-(rail.getWidth()-card.getWidth())/2),0); });
            if(appScroll!=null) main.post(()->{ if(page==Page.APPS&&card.isAttachedToWindow()) card.requestRectangleOnScreen(new android.graphics.Rect(0,0,card.getWidth(),card.getHeight()),false); });
        }
    }
    private boolean shortcutServiceEnabled(){
        android.view.accessibility.AccessibilityManager manager=getSystemService(android.view.accessibility.AccessibilityManager.class);
        for(android.accessibilityservice.AccessibilityServiceInfo info:manager.getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK)){
            android.content.pm.ServiceInfo service=info.getResolveInfo().serviceInfo;
            if(getPackageName().equals(service.packageName) && HotkeyService.class.getName().equals(service.name)) return true;
        } return false;
    }
    private void setupPage(){
        setupStep=Math.max(0,Math.min(3,setupStep));
        formPage((prefs().getBoolean("setupComplete",false)?"Settings":"Setup")+" · "+(setupStep+1)+" / 4");
        text(formInfo,new String[]{"Connection","Controller","Appearance","Notifications & closing"}[setupStep],20,false).setTypeface(null,Typeface.BOLD);
        if(setupStep==0){
            status=text(formMain,app.status,15,false); note(formMain,app.backend!=null?"✓ Shizuku connected":"Connection required");
            button(formMain,app.backend==null?"Connect Shizuku":"Connection details",()->show(Page.CONNECT));
            note(formInfo,"Start Shizuku, then authorize TaskDeck. Without root, start Shizuku again after reboot. Pairing alone does not start its service.");
        } else if(setupStep==1){
            button(formMain,"Calibrate printed A",()->show(Page.CONFIRM));
            button(formMain,"Configure shortcuts",()->show(Page.SHORTCUTS));
            button(formMain,"Controller shortcut service",()->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
            note(formInfo,prefs().getBoolean("confirmCalibrated",prefs().contains("swapped"))?"✓ Confirm calibrated":"Confirm: default mapping");
            note(formInfo,"Deck shortcut: "+ShortcutKeys.decode(prefs().getString("shortcutChord","")).size()+" buttons · "+(prefs().getBoolean("shortcutEnabled",false)?"on":"off"));
            note(formInfo,"Direct switch: "+ShortcutKeys.decode(prefs().getString("quickSwitchChord","")).size()+" buttons · "+(prefs().getBoolean("quickSwitchEnabled",false)?"on":"off"));
            note(formInfo,"Service: "+(shortcutServiceEnabled()?"enabled":"access required"));
            note(formInfo,"Optional Accessibility access handles your shortcuts. No screen content or typed text is read or stored.");
        } else if(setupStep==2){
            note(formMain,"Selected: "+prefs().getString("theme","dark").toUpperCase(Locale.ROOT));
            LinearLayout themeOptions=row(formMain);
            for(String value:new String[]{"light","dark","oled"}){
                Button option=button(themeOptions,value.equals("oled")?"OLED":value.equals("light")?"Light":"Dark",()->{ prefs().edit().putString("theme",value).apply(); show(Page.SETUP); }); option.setSelected(value.equals(prefs().getString("theme","dark"))); option.setTextSize(12); option.setPadding(dp(6),dp(5),dp(6),dp(5));
            }
            button(formMain,"Layout options",()->show(Page.LAYOUT));
            note(formInfo,"Changes are saved immediately. Touch activates an option in one tap. Controller focus uses a rounded outline. Menu icons stay neutral; colored button badges identify A, B, X and Y.");
        } else {
            boolean ram=prefs().getBoolean("ramNotifications",true),visible=shortcutVisible(),exit=prefs().getBoolean("exitAfterBulk",true);
            button(formMain,ram?"Disable RAM feedback":"Enable RAM feedback",()->{ boolean enable=!prefs().getBoolean("ramNotifications",true); prefs().edit().putBoolean("ramNotifications",enable).apply(); if(!enable) getSystemService(NotificationManager.class).cancel(2); show(Page.SETUP); });
            button(formMain,visible?"Hide notification shortcut":"Show notification shortcut",()->{ if(shortcutVisible()){ getSystemService(NotificationManager.class).cancel(1); prefs().edit().putBoolean("pendingNotificationShortcut",false).apply(); } else shortcutNotification(); show(Page.SETUP); });
            LinearLayout feedbackTools=row(formMain);
            Button permissions=button(feedbackTools,"Diagnostics",()->show(Page.DIAGNOSTICS));
            Button testFeedback=button(feedbackTools,"Test RAM feedback",()->{
                if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){ prefs().edit().putBoolean("pendingRamFeedbackTest",true).apply(); notificationPermission(); }
                else { app.testMemoryFeedback(); show(Page.SETUP); }
            });
            for(Button option:new Button[]{permissions,testFeedback}){ option.setTextSize(12); option.setPadding(dp(8),dp(5),dp(8),dp(5)); }
            button(formMain,exit?"Stay open after close actions":"Exit after close actions",()->{ prefs().edit().putBoolean("exitAfterBulk",!exit).apply(); show(Page.SETUP); });
            note(formInfo,"RAM feedback: "+(ram?"on":"off")+" · notifications "+(notificationsAllowed("ram_results")&&notificationsAllowed("ram")?"allowed":"blocked"));
            if(!app.lastMemoryMessage.isEmpty()) note(formInfo,"Last result: "+app.lastMemoryMessage.replace('\n',' '));
            note(formInfo,"Shortcut notification: "+(visible?"visible":notificationsAllowed("switcher")?"hidden":"blocked by Android"));
            note(formInfo,"After bulk close: "+(exit?"exit menu":"stay open"));
            note(formInfo,"Close all returns HOME. Close other apps keeps the previous app. Test checks RAM feedback.");
        }
        LinearLayout nav=row(root); nav.setLayoutParams(new LinearLayout.LayoutParams(-1,-2)); Button previous=button(nav,"Previous",()->{ setupStep--; controlIndex=-1; show(Page.SETUP); }); previous.setEnabled(setupStep>0); setupPrevious=previous; Drawable l1=new ControllerBadge("L1",theme.text); l1.setBounds(0,0,dp(32),dp(24)); previous.setCompoundDrawables(l1,null,null,null);
        View spacer=new View(this); nav.addView(spacer,new LinearLayout.LayoutParams(0,1,1));
        setupNext=button(nav,setupStep==3?"Finish setup":"Next step",()->{ controlIndex=-1; if(setupStep<3){ setupStep++; show(Page.SETUP); } else { prefs().edit().putBoolean("setupComplete",true).apply(); show(Page.APPS); if(app.backend!=null) refresh(); } });
        Drawable r1=new ControllerBadge("R1",theme.text); r1.setBounds(0,0,dp(32),dp(24)); setupNext.setCompoundDrawables(r1,null,null,null); ((LinearLayout.LayoutParams)setupNext.getLayoutParams()).rightMargin=0;
        setupPrevious.setContentDescription("L1: Previous"); setupNext.setContentDescription("R1: "+setupNext.getText());
    }
    private boolean shortcutConflict(Set<String> keys){ return ShortcutKeys.conflicts(keys,ShortcutKeys.decode(prefs().getString(recordingQuickSwitch?"shortcutChord":"quickSwitchChord",""))); }
    private void startRecording(boolean direct){ recordingQuickSwitch=direct; show(Page.CHORD); }
    private TextView smallNote(LinearLayout parent,String value){ TextView text=note(parent,value); text.setTextSize(12); text.setMaxLines(2); text.setEllipsize(android.text.TextUtils.TruncateAt.END); return text; }
    private void shortcutsPage(){
        formPage("Controller shortcuts"); text(formInfo,"Two independent actions",20,false);
        button(formMain,"Record deck shortcut",()->startRecording(false));
        button(formMain,"Record direct switch",()->startRecording(true));
        LinearLayout toggles=row(formMain);
        for(boolean direct:new boolean[]{false,true}){
            String enabled=direct?"quickSwitchEnabled":"shortcutEnabled",chord=direct?"quickSwitchChord":"shortcutChord";
            Button toggle=button(toggles,(direct?"Switch: ":"Deck: ")+(prefs().getBoolean(enabled,false)?"on":"off"),()->{
                if(ShortcutKeys.decode(prefs().getString(chord,"")).isEmpty()){ startRecording(direct); return; }
                prefs().edit().putBoolean(enabled,!prefs().getBoolean(enabled,false)).apply(); show(Page.SHORTCUTS);
            }); toggle.setTextSize(12); toggle.setPadding(dp(8),dp(5),dp(8),dp(5));
        }
        button(formMain,"Shortcut service settings",()->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        smallNote(formInfo,"Deck: "+ShortcutKeys.labels(ShortcutKeys.decode(prefs().getString("shortcutChord",""))));
        smallNote(formInfo,"Direct: "+ShortcutKeys.labels(ShortcutKeys.decode(prefs().getString("quickSwitchChord",""))));
        smallNote(formInfo,"Service: "+(shortcutServiceEnabled()?"enabled":"access required"));
        smallNote(formInfo,"Direct switches the last two distinct apps without opening this menu. Nested/identical chords are rejected to prevent accidental triggers.");
    }
    private void layoutPage(){
        formPage("Layout options"); text(formInfo,"Density and readability",20,false);
        button(formMain,prefs().getBoolean("groupApps",true)?"Show separate windows":"Group app windows",()->{ prefs().edit().putBoolean("groupApps",!prefs().getBoolean("groupApps",true)).apply(); rebuildTasks(); show(Page.LAYOUT); });
        int columns=prefs().getInt("columns",0),font=prefs().getInt("minLabelSp",12);
        smallNote(formInfo,"Grid columns: "+(columns==0?"automatic":columns)); LinearLayout density=row(formMain);
        button(density,"− Columns",()->{ prefs().edit().putInt("columns",Math.max(2,(columns==0?Math.max(3,gridColumns):columns)-1)).apply(); show(Page.LAYOUT); });
        button(density,"Columns +",()->{ prefs().edit().putInt("columns",Math.min(10,(columns==0?Math.max(2,gridColumns):columns)+1)).apply(); show(Page.LAYOUT); });
        button(formMain,"Automatic columns",()->{ prefs().edit().putInt("columns",0).apply(); show(Page.LAYOUT); });
        smallNote(formInfo,"Minimum label: "+font+" sp"); LinearLayout fonts=row(formMain);
        button(fonts,"− Text",()->{ prefs().edit().putInt("minLabelSp",Math.max(10,font-1)).apply(); show(Page.LAYOUT); });
        button(fonts,"Text +",()->{ prefs().edit().putInt("minLabelSp",Math.min(22,font+1)).apply(); show(Page.LAYOUT); });
        smallNote(formInfo,"Grouped apps show a window count. Open uses their most recent window; Close app closes all reported windows in that group.");
        smallNote(formInfo,"Choose window is available through Select / long press. Separate-window mode lets you close one window at a time.");
        smallNote(formInfo,"Columns apply to Grid. Fit all adapts automatically; if your minimum text cannot fit, it scrolls rather than clipping unreadable labels.");
        smallNote(formInfo,"Changes save immediately. Reopen the deck to preview them.");
    }
    private void detailNavigation(int count,Page target){
        LinearLayout nav=row(root); Button previous=button(nav,"Previous page",()->{ detailsPage--; show(target); }); previous.setEnabled(detailsPage>0);
        button(nav,"Next page",()->{ detailsPage++; show(target); }).setEnabled((detailsPage+1)*2<count);
    }
    private void resultsPage(){
        formPage("Last action"); JSONObject report=app.lastOperation; JSONArray items=report==null?null:report.optJSONArray("items"); int count=items==null?0:items.length(); detailsPage=Math.max(0,Math.min(detailsPage,Math.max(0,(count-1)/2)));
        text(formInfo,"Per-app outcome",20,false); smallNote(formInfo,report==null?"No action recorded":report.optString("summary"));
        smallNote(formInfo,"Confirmed means Android no longer reports the task or the old launcher PID. Unverified results are not counted as successful closes.");
        if(count==0) note(formMain,"Nothing to report");
        for(int i=detailsPage*2;i<Math.min(count,detailsPage*2+2);i++){
            JSONObject item=items.optJSONObject(i); if(item==null) continue;
            TextView name=text(formMain,item.optString("label")+" · #"+item.optInt("id"),14,false); name.setMaxLines(1); name.setEllipsize(android.text.TextUtils.TruncateAt.END);
            smallNote(formMain,item.optString("action")+" · "+item.optString("status")); smallNote(formMain,item.optString("message"));
        }
        detailNavigation(count,Page.RESULTS);
    }
    private void windowsPage(){
        formPage("Choose window"); List<JSONObject> windows=windowGroup==null?Collections.emptyList():TaskGroups.expand(windowGroup); detailsPage=Math.max(0,Math.min(detailsPage,Math.max(0,(windows.size()-1)/2)));
        text(formInfo,windowGroup==null?"No app selected":windowGroup.optString("label"),20,false);
        smallNote(formInfo,windows.size()+" reported windows. Choose one to open it. Use separate-window mode in Layout to close individual windows.");
        for(int i=detailsPage*2;i<Math.min(windows.size(),detailsPage*2+2);i++){
            JSONObject window=windows.get(i); Button option=button(formMain,window.optString("windowLabel","Window "+window.optInt("id")),()->openWindow(window)); option.setMaxLines(1); option.setEllipsize(android.text.TextUtils.TruncateAt.END);
        }
        detailNavigation(windows.size(),Page.WINDOWS);
    }
    private void diagnosticsPage(){
        formPage("Diagnostics"); text(formInfo,"Connection and feedback",20,false);
        smallNote(formInfo,app.status); smallNote(formInfo,Build.MODEL+" · Android API "+Build.VERSION.SDK_INT);
        smallNote(formInfo,memoryStatus); smallNote(formInfo,"Notifications: "+(notificationsAllowed("ram")&&notificationsAllowed("ram_results")?"allowed":"blocked"));
        smallNote(formInfo,app.lastFeedbackDelivery); smallNote(formInfo,"Last RAM result: "+(app.lastMemoryMessage.isEmpty()?"none":app.lastMemoryMessage.replace('\n',' ')));
        button(formMain,"Test RAM feedback",()->{ if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){ prefs().edit().putBoolean("pendingRamFeedbackTest",true).apply(); notificationPermission(); } else { app.testMemoryFeedback(); show(Page.DIAGNOSTICS); } });
        button(formMain,"Notification permissions",()->startActivity(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,getPackageName())));
        button(formMain,"Reconnect Shizuku",()->{ app.recoverBackend("Manual reconnect"); show(Page.DIAGNOSTICS); });
        button(formMain,"Last action details",()->{ detailsPage=0; show(Page.RESULTS); });
    }
    private void connectPage(){
        formPage("Shizuku connection"); status=text(formMain,app.status,15,false);
        button(formMain,"Open Shizuku",()->{ Intent intent=getPackageManager().getLaunchIntentForPackage("moe.shizuku.privileged.api"); if(intent!=null) startActivity(intent); else { feedback="Install Shizuku from shizuku.rikka.app"; updateStatus(); } });
        button(formMain,"Authorize / Connect",()->app.connect(true));
        button(formMain,prefs().getBoolean("setupComplete",false)?"Open app deck":"Continue setup",()->{ show(prefs().getBoolean("setupComplete",false)?Page.APPS:Page.SETUP); if(app.backend!=null) refresh(); });
        text(formInfo,"Connect once",20,false); note(formInfo,"1. Start Shizuku.\n2. Return and choose Authorize / Connect.\n3. Approve TaskDeck access.\n\nWithout root, restart Shizuku after reboot. TaskDeck uses it to access Android tasks.");
    }
    private void recordPage(){
        heldKeys.clear(); capturedKeys.clear(); captureDevice=null; formPage(page==Page.CHORD?(recordingQuickSwitch?"Record direct switch":"Record deck shortcut"):"Calibrate confirm");
        recordLive=text(formMain,"Held now: none",16,false); recordSaved=text(formMain,"Recorded: none",16,false);
        recordLive.setMaxLines(2); recordSaved.setMaxLines(3); recordLive.setTextSize(14); recordSaved.setTextSize(14); recordLive.setEllipsize(android.text.TextUtils.TruncateAt.END); recordSaved.setEllipsize(android.text.TextUtils.TruncateAt.END);
        text(formInfo,page==Page.CHORD?"Hold, release, save":"Press printed A",20,false);
        note(formInfo,page==Page.CHORD?"Hold up to four buttons together, then release. Confirm or Start saves. D-pad, Confirm, Back and Start remain reserved for navigation.":"Press the button printed A. TaskDeck adjusts the common A/B and X/Y swap.");
        if(page==Page.CHORD){
            saveChord=button(formMain,"Save combination · Start",()->{ if(capturedKeys.isEmpty()||!heldKeys.isEmpty()||capturedKeys.size()>4) return; if(shortcutConflict(capturedKeys)){ recordSaved.setText("Conflicts with the other shortcut"); return; } prefs().edit().putString(recordingQuickSwitch?"quickSwitchChord":"shortcutChord",ShortcutKeys.encode(capturedKeys)).putBoolean(recordingQuickSwitch?"quickSwitchEnabled":"shortcutEnabled",true).apply(); show(Page.SHORTCUTS); }); saveChord.setEnabled(false);
            button(formMain,"Record again",()->show(Page.CHORD));
            note(formInfo,"Choose an unused combination. Partial presses can reach your game. If both rear buttons report identical key and scan codes, assign different outputs in Retroid settings.");
        }
    }

    private void record(KeyEvent event){
        String key=ShortcutKeys.id(event);
        if(page==Page.CONFIRM){
            if(event.getAction()==KeyEvent.ACTION_DOWN && event.getRepeatCount()==0 && (event.getKeyCode()==96 || event.getKeyCode()==97)){
                prefs().edit().putBoolean("swapped",event.getKeyCode()==97).putBoolean("confirmCalibrated",true).apply(); feedback="Confirm button calibrated"; show(Page.SETUP);
            } return;
        }
        if(event.getAction()==KeyEvent.ACTION_DOWN && event.getRepeatCount()==0){
            if(heldKeys.isEmpty()) captureDevice=event.getDeviceId();
            if(!Objects.equals(captureDevice,event.getDeviceId())){ recordLive.setText("Use buttons from one controller"); return; }
            heldKeys.add(key);
            if(heldKeys.size()>=capturedKeys.size()){ capturedKeys.clear(); capturedKeys.addAll(heldKeys); }
        } else if(event.getAction()==KeyEvent.ACTION_UP && Objects.equals(captureDevice,event.getDeviceId())) heldKeys.remove(key);
        recordLive.setText("Held now: "+(heldKeys.isEmpty()?"none":ShortcutKeys.labels(heldKeys)));
        recordSaved.setText("Recorded: "+ShortcutKeys.labels(capturedKeys)+(shortcutConflict(capturedKeys)?" · conflicts with other shortcut":""));
        saveChord.setEnabled(!capturedKeys.isEmpty() && heldKeys.isEmpty() && capturedKeys.size()<=4&&!shortcutConflict(capturedKeys));
        if(controllerMode && saveChord.isEnabled()) focusControl(controls.indexOf(saveChord));
    }
    private void updateStatus(){
        if(status==null) return;
        if(!feedback.isEmpty()){ status.setText(feedback); status.setVisibility(View.VISIBLE); }
        else if(page==Page.APPS) status.setText(app.backend==null?"Not connected · open Connect":tasks.size()+" apps · "+prefs().getString("sort","recent")+(tasks.stream().noneMatch(this::closable)?" · Nothing to close":"")+(busy?" · updating…":"")+(!toolZone&&selected()!=null&&!"cards".equals(prefs().getString("view","grid"))?" · "+selected().optString("label"):""));
        else status.setText(app.status);
    }
    private long available(){ ActivityManager.MemoryInfo info=new ActivityManager.MemoryInfo(); getSystemService(ActivityManager.class).getMemoryInfo(info); return info.availMem; }
    private void updateMemory(){
        if(memory==null) return;
        ActivityManager.MemoryInfo info=new ActivityManager.MemoryInfo(); getSystemService(ActivityManager.class).getMemoryInfo(info);
        if(compactMemory!=null) compactMemory.setText("Free "+info.availMem/(1024*1024)+" MiB");
        memory.setText("RAM used  "+((info.totalMem-info.availMem)/(1024*1024))+" MiB\nAvailable  "+(info.availMem/(1024*1024))+" MiB");
        updateBackground();
    }
    private void updateBackground(){
        if(backgroundInfo==null) return;
        List<JSONObject> ordered=new ArrayList<>(backgroundApps); ordered.sort((a,b)->Long.compare(b.optLong("pssKb",-1),a.optLong("pssKb",-1)));
        StringBuilder value=new StringBuilder("System & cache also use RAM.\nCan't close here");
        int count=0; for(JSONObject item:ordered){ if(count++>=3) break; long kb=item.optLong("pssKb",-1); String label=item.optString("label"); if(label.length()>18) label=label.substring(0,17)+"…"; value.append("\n").append(label).append(kb>=0?" · ~"+Math.round(kb/1024d)+" MiB":" · n/a"); }
        if(ordered.isEmpty()) value.append(app.backend==null?"\nConnect to inspect apps.":"\nNo additional apps reported.");
        if(ordered.size()>3) value.append("\n+").append(ordered.size()-3).append(" background entries");
        backgroundInfo.setText(value.toString());
    }
    private void rebuildTasks(){
        List<JSONObject> visible=TaskOrder.arrange(rawTasks,"recent",this::locked);
        tasks.clear(); tasks.addAll(TaskOrder.arrange(TaskGroups.group(visible,prefs().getBoolean("groupApps",true)),prefs().getString("sort","recent"),this::locked));
        List<Integer> ids=new ArrayList<>(); for(JSONObject task:tasks) ids.add(task.optInt("id")); selection.update(ids);
        if(resetSelection){ selection.first(); toolZone=tasks.stream().noneMatch(this::closable); toolIndex=0; resetSelection=false; }
        if(page==Page.APPS) renderCards(); updateStatus(); updateOperationInfo();
    }
    private void refresh(){
        updateMemory(); ITaskBackend backend=app.backend; if(backend==null) return; if(busy){ pendingRefresh=true; return; }
        busy=true; int generation=++catalogGeneration; memoryStatus="Loading task list"; updateStatus();
        app.request("Task list",8000,()->{ configureBackend(backend); return new JSONObject(backend.readTasks()); },result->{
            if(generation!=catalogGeneration||isDestroyed()) return; busy=false;
            if(result.has("error")){ readError=true; pendingRefresh=false; feedback=result.optString("error"); updateStatus(); return; }
            if(app.backend!=backend) return;
            if(readError){ feedback=""; readError=false; } app.backendHealthy();
            app.status="Connected · backend "+result.optInt("backendVersion",8)+" · UID "+result.optInt("uid")+" · Android API "+result.optInt("sdk");
            if(!result.optString("protectionWarning").isEmpty()) feedback=result.optString("protectionWarning")+" · protection cannot be checked";
            rawTasks.clear(); JSONArray rows=result.optJSONArray("tasks");
            if(rows!=null) for(int i=0;i<rows.length();i++){ JSONObject task=rows.optJSONObject(i); if(task!=null){ try { task.put("recentRank",i); } catch(JSONException ignored){} rawTasks.add(task); } }
            if(captureEntry){ entryTask=result.optJSONObject("previousTask"); if(entryTask==null) for(JSONObject task:rawTasks) if(!TaskOrder.mandatory(task)){ entryTask=task; break; } captureEntry=false; }
            rebuildTasks(); memoryStatus="RAM loading; tasks ready";
            app.requestMemory("RAM sample",5000,()->new JSONObject(backend.readMemory()),sample->{
                if(generation!=catalogGeneration||isDestroyed()||isFinishing()||app.backend!=backend) return;
                if(sample.has("error")){
                    memoryStatus=sample.optString("error");
                    for(JSONObject task:rawTasks) try { task.put("pssKb",-1).put("memoryState","unavailable"); } catch(JSONException ignored){}
                } else {
                    memoryStatus="RAM snapshot ready"; Map<String,JSONObject> byPackage=new HashMap<>(); JSONArray values=sample.optJSONArray("samples");
                    if(values!=null) for(int i=0;i<values.length();i++){ JSONObject value=values.optJSONObject(i); if(value!=null) byPackage.put(value.optString("package"),value); }
                    for(JSONObject task:rawTasks){ JSONObject value=byPackage.get(task.optString("package")); try { task.put("pssKb",value==null?-1:value.optLong("pssKb",-1)).put("memoryState",value==null?"unavailable":value.optString("memoryState","unavailable")); } catch(JSONException ignored){} }
                    backgroundApps.clear(); JSONArray background=sample.optJSONArray("backgroundApps"); if(background!=null) for(int i=0;i<background.length();i++) if(background.optJSONObject(i)!=null) backgroundApps.add(background.optJSONObject(i)); updateBackground();
                } rebuildTasks();
            });
            if(pendingRefresh){ pendingRefresh=false; refresh(); }
        });
    }

    private JSONObject selected(){ Integer id=selection.id(); if(id!=null) for(JSONObject task:tasks) if(task.optInt("id")==id) return task; return null; }
    private void openSelected(){
        JSONObject task=selected(); ITaskBackend backend=app.backend; if(task==null || backend==null || busy) return;
        if(task.optBoolean("launcher")){
            Intent launch=task.optBoolean("home")?new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME):getPackageManager().getLaunchIntentForPackage(task.optString("package"));
            if(launch==null) launch=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).setPackage(task.optString("package"));
            try { startActivity(launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); finish(); } catch(RuntimeException failure){ feedback="Cannot open launcher: "+failure.getClass().getSimpleName(); updateStatus(); } return;
        }
        openWindow(task);
    }
    private void openWindow(JSONObject task){
        ITaskBackend backend=app.backend; if(backend==null||busy) return; busy=true;
        app.request("Open app",5000,()->new JSONObject(backend.openTask(task.optInt("id"),task.optString("package"))),result->{
            busy=false; if(isDestroyed()) return; if(!result.has("error")) finish(); else { feedback=result.optString("error"); updateStatus(); }
        });
    }
    private Set<String> protectedApps(){ return new HashSet<>(prefs().getStringSet("protectedApps",Collections.emptySet())); }
    private String[] protectedArray(){ return protectedApps().toArray(new String[0]); }
    private Set<String> unlockedLaunchers(){ return new HashSet<>(prefs().getStringSet("unlockedLaunchers",Collections.emptySet())); }
    private void configureBackend(ITaskBackend backend) throws android.os.RemoteException { backend.setProtectedPackages(protectedArray()); backend.setUnlockedLaunchers(unlockedLaunchers().toArray(new String[0])); }
    private boolean locked(JSONObject task){ return TaskOrder.mandatory(task)||protectedApps().contains(task.optString("package")) || task.optBoolean("launcher")&&!unlockedLaunchers().contains(task.optString("package")); }
    private boolean launcherIdle(JSONObject task){ return task.optBoolean("launcher")&&task.optBoolean("synthetic")&&"not-reported".equals(task.optString("memoryState")); }
    private boolean closable(JSONObject task){ return !locked(task)&&!launcherIdle(task); }
    private void closeSelected(){ JSONObject task=selected(); if(task==null) return; if(!closable(task)){ feedback=locked(task)?"This app is protected":"Launcher is not running · Select to restart"; updateStatus(); return; } remove(TaskGroups.expand(task),0); }
    private void closeAll(){
        List<JSONObject> eligible=new ArrayList<>(); for(JSONObject task:tasks) if(closable(task)) eligible.addAll(TaskGroups.expand(task));
        remove(eligible,prefs().getBoolean("exitAfterBulk",true)?1:0);
    }
    private void closeOthers(){
        if(entryTask==null && !tasks.isEmpty()){ feedback="Previous app could not be identified. Reopen TaskDeck from that app."; updateStatus(); return; }
        String keep=entryTask==null?"":entryTask.optString("package"); List<JSONObject> eligible=new ArrayList<>();
        for(JSONObject task:tasks) if(closable(task)&&!keep.equals(task.optString("package"))) eligible.addAll(TaskGroups.expand(task));
        remove(eligible,prefs().getBoolean("exitAfterBulk",true)?2:0);
    }
    private void viewMenu(){
        List<Button> options=new ArrayList<>(); int[] cursor={0},axis={0};
        LinearLayout panel=new LinearLayout(this){
            private void move(int delta){ cursor[0]=Math.max(0,Math.min(options.size()-1,cursor[0]+delta)); for(int i=0;i<options.size();i++) options.get(i).setActivated(i==cursor[0]); options.get(cursor[0]).requestFocus(); }
            @Override public boolean dispatchKeyEvent(KeyEvent event){
                setInputMode(true); int key=event.getKeyCode(),confirm=prefs().getBoolean("swapped",false)?97:96,back=prefs().getBoolean("swapped",false)?96:97;
                if(key==back||key==KeyEvent.KEYCODE_BACK){ if(event.getAction()==0) protectionPopup.dismiss(); return true; }
                if(key>=19&&key<=22){ if(event.getAction()==0) move(key==19||key==21?-1:1); return true; }
                if(key==confirm||key==23){ if(event.getAction()==0&&event.getRepeatCount()==0) options.get(cursor[0]).performClick(); return true; }
                return super.dispatchKeyEvent(event);
            }
            @Override public boolean onGenericMotionEvent(MotionEvent event){
                if(!event.isFromSource(InputDevice.SOURCE_JOYSTICK)) return super.onGenericMotionEvent(event);
                setInputMode(true); float x=event.getAxisValue(MotionEvent.AXIS_HAT_X),y=event.getAxisValue(MotionEvent.AXIS_HAT_Y);
                if(Math.abs(x)<.55f) x=event.getAxisValue(MotionEvent.AXIS_X); if(Math.abs(y)<.55f) y=event.getAxisValue(MotionEvent.AXIS_Y);
                float value=Math.abs(x)>Math.abs(y)?x:y; int next=value>.55f?1:value<-.55f?-1:0;
                if(next!=0&&next!=axis[0]) move(next); axis[0]=next; return true;
            }
            @Override public boolean dispatchTouchEvent(MotionEvent event){ if(event.getActionMasked()==0) setInputMode(false); return super.dispatchTouchEvent(event); }
        };
        panel.setOrientation(LinearLayout.VERTICAL); panel.setPadding(dp(20),dp(16),dp(20),dp(16)); panel.setBackground(rounded(theme.surface)); text(panel,"App view",21,false);
        int previous=controls.size(); Button first=null;
        for(String mode:new String[]{"cards","list","grid","fit"}){
            Button option=button(panel,mode.equals("cards")?"Cards":mode.equals("list")?"List":mode.equals("grid")?"Grid":"Fit all",()->{
                prefs().edit().putString("view",mode).apply(); protectionPopup.dismiss(); show(Page.APPS);
            }); options.add(option); option.setSelected(mode.equals(prefs().getString("view","grid"))); if(option.isSelected()){ first=option; cursor[0]=options.size()-1; }
        }
        options.add(button(panel,"Layout options",()->{ protectionPopup.dismiss(); show(Page.LAYOUT); }));
        options.add(button(panel,"Last action",()->{ protectionPopup.dismiss(); detailsPage=0; show(Page.RESULTS); }));
        for(Button option:options){ option.setMinHeight(dp(30)); option.setMinimumHeight(dp(30)); option.setPadding(dp(10),dp(3),dp(10),dp(3)); LinearLayout.LayoutParams size=(LinearLayout.LayoutParams)option.getLayoutParams(); size.topMargin=dp(2); size.bottomMargin=dp(2); }
        while(controls.size()>previous) controls.remove(controls.size()-1);
        protectionPopup=new PopupWindow(panel,Math.min(dp(340),getResources().getDisplayMetrics().widthPixels-dp(32)),-2,true);
        protectionPopup.setBackgroundDrawable(rounded(theme.surface)); protectionPopup.setOutsideTouchable(true); protectionPopup.setElevation(dp(16)); protectionPopup.showAtLocation(root,Gravity.CENTER,0,0);
        if(controllerMode&&first!=null){ first.requestFocus(); first.setActivated(true); }
    }
    private void sortMenu(){
        List<Button> options=new ArrayList<>(); int[] cursor={0},axis={0};
        LinearLayout panel=new LinearLayout(this){
            private void move(int delta){ cursor[0]=Math.max(0,Math.min(options.size()-1,cursor[0]+delta)); for(int i=0;i<options.size();i++) options.get(i).setActivated(i==cursor[0]); options.get(cursor[0]).requestFocus(); }
            @Override public boolean dispatchKeyEvent(KeyEvent event){
                setInputMode(true); int key=event.getKeyCode(),confirm=prefs().getBoolean("swapped",false)?97:96,back=prefs().getBoolean("swapped",false)?96:97;
                if(key==back||key==KeyEvent.KEYCODE_BACK){ if(event.getAction()==0) protectionPopup.dismiss(); return true; }
                if(key>=19&&key<=22){ if(event.getAction()==0) move(key==19||key==21?-1:1); return true; }
                if(key==confirm||key==23){ if(event.getAction()==0&&event.getRepeatCount()==0) options.get(cursor[0]).performClick(); return true; }
                return super.dispatchKeyEvent(event);
            }
            @Override public boolean onGenericMotionEvent(MotionEvent event){
                if(!event.isFromSource(InputDevice.SOURCE_JOYSTICK)) return super.onGenericMotionEvent(event);
                setInputMode(true); float x=event.getAxisValue(MotionEvent.AXIS_HAT_X),y=event.getAxisValue(MotionEvent.AXIS_HAT_Y);
                if(Math.abs(x)<.55f) x=event.getAxisValue(MotionEvent.AXIS_X); if(Math.abs(y)<.55f) y=event.getAxisValue(MotionEvent.AXIS_Y);
                float value=Math.abs(x)>Math.abs(y)?x:y; int next=value>.55f?1:value<-.55f?-1:0;
                if(next!=0&&next!=axis[0]) move(next); axis[0]=next; return true;
            }
            @Override public boolean dispatchTouchEvent(MotionEvent event){ if(event.getActionMasked()==0) setInputMode(false); return super.dispatchTouchEvent(event); }
        };
        panel.setOrientation(LinearLayout.VERTICAL); panel.setPadding(dp(20),dp(16),dp(20),dp(16)); panel.setBackground(rounded(theme.surface)); text(panel,"Sort apps",21,false);
        int previous=controls.size(); Button first=null;
        for(String mode:new String[]{"recent","ram","alphabetical"}){
            Button option=button(panel,mode.equals("recent")?"Recent":mode.equals("ram")?"RAM size":"Alphabetical",()->{
                prefs().edit().putString("sort",mode).apply(); protectionPopup.dismiss(); reorder();
            }); options.add(option); option.setSelected(mode.equals(prefs().getString("sort","recent"))); if(option.isSelected()){ first=option; cursor[0]=options.size()-1; }
        }
        while(controls.size()>previous) controls.remove(controls.size()-1);
        protectionPopup=new PopupWindow(panel,Math.min(dp(340),getResources().getDisplayMetrics().widthPixels-dp(32)),-2,true);
        protectionPopup.setBackgroundDrawable(rounded(theme.surface)); protectionPopup.setOutsideTouchable(true); protectionPopup.setElevation(dp(16)); protectionPopup.showAtLocation(root,Gravity.CENTER,0,0);
        if(controllerMode&&first!=null){ first.requestFocus(); first.setActivated(true); }
    }
    private void reorder(){ rebuildTasks(); }

    private void protectSelected(){
        JSONObject task=selected(); if(task==null) return; boolean launcher=task.optBoolean("launcher"),mandatory=TaskOrder.mandatory(task);
        List<Button> options=new ArrayList<>(); int[] cursor={0},axis={0};
        LinearLayout panel=new LinearLayout(this){
            private void move(int delta){ cursor[0]=Math.max(0,Math.min(options.size()-1,cursor[0]+delta)); for(int i=0;i<options.size();i++) options.get(i).setActivated(i==cursor[0]); options.get(cursor[0]).requestFocus(); }
            @Override public boolean dispatchKeyEvent(KeyEvent event){
                setInputMode(true); int key=event.getKeyCode(),a=prefs().getBoolean("swapped",false)?97:96,b=prefs().getBoolean("swapped",false)?96:97;
                if(key==b||key==KeyEvent.KEYCODE_BACK){ if(event.getAction()==0) protectionPopup.dismiss(); return true; }
                if(key>=19&&key<=22){ if(event.getAction()==0) move(key==19||key==21?-1:1); return true; }
                if(key==a||key==23){ if(event.getAction()==0&&event.getRepeatCount()==0) options.get(cursor[0]).performClick(); return true; } return super.dispatchKeyEvent(event);
            }
            @Override public boolean onGenericMotionEvent(MotionEvent event){
                if(!event.isFromSource(InputDevice.SOURCE_JOYSTICK)) return super.onGenericMotionEvent(event);
                setInputMode(true); float x=event.getAxisValue(MotionEvent.AXIS_HAT_X),y=event.getAxisValue(MotionEvent.AXIS_HAT_Y); if(Math.abs(x)<.55f) x=event.getAxisValue(MotionEvent.AXIS_X); if(Math.abs(y)<.55f) y=event.getAxisValue(MotionEvent.AXIS_Y);
                float value=Math.abs(x)>Math.abs(y)?x:y; int next=value>.55f?1:value<-.55f?-1:0; if(next!=0&&next!=axis[0]) move(next); axis[0]=next; return true;
            }
            @Override public boolean dispatchTouchEvent(MotionEvent event){ if(event.getActionMasked()==0) setInputMode(false); return super.dispatchTouchEvent(event); }
        };
        panel.setOrientation(LinearLayout.VERTICAL); panel.setPadding(dp(20),dp(14),dp(20),dp(14)); panel.setBackground(rounded(theme.surface));
        text(panel,task.optString("label"),21,false);
        note(panel,launcher?"Launchers start locked. Unlock to include in close actions, or restart once while keeping protection. Android can reopen its default HOME launcher.":mandatory?"Required keyboard or system-service protection.":"Protected apps are skipped by close actions. Your choice is saved.");
        int previous=controls.size();
        Button toggle=button(panel,mandatory?"Always protected · Close":locked(task)?"Unlock app":"Protect app",()->{
            if(!mandatory){
                String name=task.optString("package"); Set<String> manual=protectedApps();
                if(launcher){ Set<String> unlocked=unlockedLaunchers(); if(unlocked.contains(name)) unlocked.remove(name); else unlocked.add(name); manual.remove(name); prefs().edit().putStringSet("unlockedLaunchers",unlocked).putStringSet("protectedApps",manual).apply(); }
                else { if(manual.contains(name)) manual.remove(name); else manual.add(name); prefs().edit().putStringSet("protectedApps",manual).apply(); }
            }
            protectionPopup.dismiss(); reorder(); refresh();
        }); options.add(toggle);
        Drawable lock=getDrawable(R.drawable.ic_lock); if(lock!=null){ lock.setTint(theme.text); lock.setBounds(0,0,dp(22),dp(22)); toggle.setCompoundDrawables(lock,null,null,null); }
        if(task.optInt("windowCount",1)>1) options.add(button(panel,"Choose window",()->{ protectionPopup.dismiss(); windowGroup=task; detailsPage=0; show(Page.WINDOWS); }));
        if(launcher&&!mandatory) options.add(button(panel,"Restart launcher",()->{ protectionPopup.dismiss(); restartLauncher(task); }));
        while(controls.size()>previous) controls.remove(controls.size()-1);
        protectionPopup=new PopupWindow(panel,Math.min(dp(400),getResources().getDisplayMetrics().widthPixels-dp(32)),-2,true); protectionPopup.setBackgroundDrawable(rounded(theme.surface)); protectionPopup.setOutsideTouchable(true); protectionPopup.setElevation(dp(16)); protectionPopup.setOnDismissListener(this::markSelection);
        protectionPopup.showAtLocation(root,Gravity.CENTER,0,0); if(controllerMode){ toggle.requestFocus(); toggle.setActivated(true); }
    }
    private void restartLauncher(JSONObject task){
        ITaskBackend backend=app.backend; if(backend==null||busy) return; busy=true; catalogGeneration++; long before=available();
        app.request("Stop launcher for restart",6000,()->{ configureBackend(backend); return new JSONObject(backend.stopLauncher(task.optString("package"),true)); },result->{
            busy=false; if(isDestroyed()) return;
            if(result.has("error")){ app.lastOperation=OperationResults.report(new JSONArray().put(OperationResults.item(task,"Restart launcher",result))); feedback=app.lastOperation.optString("summary"); updateStatus(); updateOperationInfo(); return; }
            JSONArray values=result.optJSONArray("previousPids"); int[] previous=null; if(values!=null){ previous=new int[values.length()]; for(int i=0;i<previous.length;i++) previous[i]=values.optInt(i); }
            Intent launch=getPackageManager().getLaunchIntentForPackage(task.optString("package")); if(launch==null) launch=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).setPackage(task.optString("package"));
            try { startActivity(launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); app.verifyRestart(backend,task,previous,before); finish(); }
            catch(RuntimeException denied){ app.lastOperation=OperationResults.report(new JSONArray().put(OperationResults.item(task,"Restart launcher",BackendRequests.error("Cannot reopen launcher: "+denied.getClass().getSimpleName())))); feedback=app.lastOperation.optString("summary"); updateStatus(); updateOperationInfo(); }
        });
    }
    private void updateOperationInfo(){
        if(operationInfo==null) return; JSONObject report=app.lastOperation;
        boolean problems=report!=null&&(report.optInt("failed")>0||report.optInt("unverified")>0);
        operationInfo.setVisibility(problems?View.VISIBLE:View.GONE); if(problems){ operationInfo.setText(report.optString("summary")+" · View → Last action"); operationInfo.setTextColor(theme.muted); }
    }
    // Outcome verification is per item; uncertain results are not counted as successful closes.
    private void remove(List<JSONObject> snapshot,int destination){
        ITaskBackend backend=app.backend; if(busy||backend==null&&!snapshot.isEmpty()) return; busy=true; catalogGeneration++; long before=available();
        feedback="Closing "+snapshot.size()+" window(s)…"; updateStatus(); JSONObject previous=entryTask;
        List<JSONObject> progress=Collections.synchronizedList(new ArrayList<>());
        long timeout=Math.min(45000,8000+snapshot.size()*800L),deadline=SystemClock.uptimeMillis()+timeout;
        app.request("Close apps",timeout,()->{
            JSONArray items=new JSONArray(); Set<String> stopped=new HashSet<>();
            for(JSONObject task:snapshot){
                if(Thread.currentThread().isInterrupted()||SystemClock.uptimeMillis()>=deadline) break;
                if(task.optBoolean("launcher")&&!stopped.add(task.optString("package"))) continue;
                JSONObject result;
                try { configureBackend(backend); result=new JSONObject(task.optBoolean("launcher")?backend.stopLauncher(task.optString("package"),false):backend.removeTask(task.optInt("id"),task.optString("package"))); }
                catch(Exception failure){ result=BackendRequests.error(failure.getClass().getSimpleName()+": "+String.valueOf(failure.getMessage())); }
                JSONObject item=OperationResults.item(task,task.optBoolean("launcher")?"Stop launcher":"Close window",result); items.put(item); progress.add(item);
            } return OperationResults.report(items);
        },report->{
            busy=false;
            if(report.has("error")){
                JSONArray completed; synchronized(progress){ completed=new JSONArray(new ArrayList<>(progress)); }
                Set<String> known=new HashSet<>(),launchers=new HashSet<>(); for(int i=0;i<completed.length();i++){ JSONObject item=completed.optJSONObject(i); if(item!=null){ known.add(item.optInt("id")+":"+item.optString("package")); if(item.optString("action").equals("Stop launcher")) launchers.add(item.optString("package")); } }
                int unknown=0; for(JSONObject task:snapshot) if(!known.contains(task.optInt("id")+":"+task.optString("package"))&&!launchers.contains(task.optString("package"))){ completed.put(OperationResults.item(task,"Outcome unknown",report)); unknown++; }
                if(unknown==0) completed.put(OperationResults.item(new JSONObject(Collections.singletonMap("label","Connection outcome unknown")),"Close apps",report)); report=OperationResults.report(completed);
            }
            app.lastOperation=report; int done=report.optInt("confirmed"); if(done>0) app.reportMemoryResult(done,before);
            if(isDestroyed()) return; feedback=report.optString("summary"); updateStatus(); updateOperationInfo();
            if(report.optInt("failed")==0&&report.optInt("unverified")==0&&destination!=0){
                app.flushMemoryFeedback();
                if(destination==2&&previous!=null&&backend!=null&&!previous.optBoolean("home")){ returnToPrevious(backend,previous); return; }
                startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); finish();
            } else refresh();
        });
    }
    private void returnToPrevious(ITaskBackend backend,JSONObject previous){
        busy=true; app.request("Return to previous app",5000,()->new JSONObject(backend.openTask(previous.optInt("id"),previous.optString("package"))),result->{
            busy=false; if(isDestroyed()) return; if(!result.has("error")) finish(); else { feedback=result.optString("error"); updateStatus(); refresh(); }
        });
    }

    private void notificationPermission(){
        if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},1);
        else { feedback=notificationsAllowed()?"Notifications enabled":"Notifications blocked in Android settings"; updateStatus(); }
    }
    private boolean notificationsAllowed(){ return (Build.VERSION.SDK_INT<33 || checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED)&&getSystemService(NotificationManager.class).areNotificationsEnabled(); }
    private boolean notificationsAllowed(String channel){ NotificationChannel value=getSystemService(NotificationManager.class).getNotificationChannel(channel); return notificationsAllowed()&&(value==null||value.getImportance()!=NotificationManager.IMPORTANCE_NONE); }
    private boolean shortcutVisible(){ for(android.service.notification.StatusBarNotification item:getSystemService(NotificationManager.class).getActiveNotifications()) if(item.getId()==1) return true; return false; }

    private void shortcutNotification(){
        if(!notificationsAllowed()){ prefs().edit().putBoolean("pendingNotificationShortcut",true).apply(); notificationPermission(); return; }
        NotificationManager manager=getSystemService(NotificationManager.class); manager.createNotificationChannel(new NotificationChannel("switcher","TaskDeck shortcut",NotificationManager.IMPORTANCE_LOW));
        PendingIntent open=PendingIntent.getActivity(this,0,new Intent(this,SwitcherActivity.class),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        manager.notify(1,new Notification.Builder(this,"switcher").setSmallIcon(R.drawable.ic_tasks).setContentTitle("TaskDeck").setContentText("Open your app deck").setContentIntent(open).setOngoing(true).build());
    }
    @Override public void onRequestPermissionsResult(int code,String[] permissions,int[] grants){
        super.onRequestPermissionsResult(code,permissions,grants); if(code!=1) return;
        boolean granted=grants.length>0&&grants[0]==PackageManager.PERMISSION_GRANTED;
        boolean shortcut=prefs().getBoolean("pendingNotificationShortcut",false); prefs().edit().remove("pendingNotificationShortcut").apply();
        if(granted&&shortcut) shortcutNotification();
        if(prefs().getBoolean("pendingRamFeedbackTest",false)){ prefs().edit().remove("pendingRamFeedbackTest").apply(); app.testMemoryFeedback(); }
        if(page==Page.SETUP) show(Page.SETUP); feedback=granted?"Notification permission granted":"Notification permission denied"; updateStatus();
    }
    private void back(){ if(page==Page.APPS) finish(); else if(page==Page.RESULTS) show(Page.APPS); else if(page==Page.WINDOWS) show(Page.APPS); else if(page==Page.CHORD) show(Page.SHORTCUTS); else if(page==Page.CONFIRM || page==Page.CONNECT || page==Page.SHORTCUTS || page==Page.LAYOUT || page==Page.DIAGNOSTICS) show(Page.SETUP); else if(setupStep>0){ setupStep--; show(Page.SETUP); } else show(Page.APPS); }
    private void focusMode(View view,boolean controller){
        if(view instanceof Button||cards.contains(view)){ view.setFocusableInTouchMode(controller); if(!controller) view.setActivated(false); }
        if(view instanceof ViewGroup) for(int i=0;i<((ViewGroup)view).getChildCount();i++) focusMode(((ViewGroup)view).getChildAt(i),controller);
    }
    private void setInputMode(boolean controller){
        if(controllerMode==controller) return; controllerMode=controller;
        if(root!=null){ focusMode(root,controller); if(!controller){ stopStick(); for(Button button:controls) button.setActivated(false); View focus=getCurrentFocus(); if(focus!=null) focus.clearFocus(); root.requestFocus(); } }
        if(protectionPopup!=null&&protectionPopup.isShowing()){ View panel=protectionPopup.getContentView(); focusMode(panel,controller); if(!controller) panel.clearFocus(); }
        if(controller && protectionPopup!=null&&protectionPopup.isShowing()) focusFirstButton(protectionPopup.getContentView());
        else if(controller && page==Page.APPS) markSelection();
        else if(controller && !(getCurrentFocus() instanceof Button)) focusControl(controlIndex<0?0:controlIndex);
    }
    private boolean focusFirstButton(View view){ if(view instanceof Button&&view.isEnabled()){ view.requestFocus(); return true; } if(view instanceof ViewGroup) for(int i=0;i<((ViewGroup)view).getChildCount();i++) if(focusFirstButton(((ViewGroup)view).getChildAt(i))) return true; return false; }
    @Override public boolean dispatchTouchEvent(MotionEvent event){ if(event.getActionMasked()==MotionEvent.ACTION_DOWN) setInputMode(false); return super.dispatchTouchEvent(event); }

    @Override public void onBackPressed(){ back(); }
    @Override public boolean dispatchKeyEvent(KeyEvent event){
        int key=event.getKeyCode();
        if(ShortcutKeys.eligible(event)||key>=19&&key<=23) setInputMode(true);
        boolean swapped=prefs().getBoolean("swapped",false); int a=swapped?97:96,b=swapped?96:97,x=swapped?100:99,y=swapped?99:100;
        if(page==Page.CHORD && key==KeyEvent.KEYCODE_BUTTON_START && heldKeys.isEmpty() && !capturedKeys.isEmpty()){
            if(event.getAction()==KeyEvent.ACTION_DOWN && event.getRepeatCount()==0 && saveChord.isEnabled()) saveChord.performClick(); return true;
        }
        boolean reserved=key>=19&&key<=23 || key==a || key==b || key==KeyEvent.KEYCODE_BACK || key==KeyEvent.KEYCODE_BUTTON_START;
        if((page==Page.CONFIRM || page==Page.CHORD&&!reserved) && ShortcutKeys.eligible(event)){ record(event); return true; }
        if(key==b){ if(event.getAction()==KeyEvent.ACTION_DOWN && event.getRepeatCount()==0) back(); return true; }
        if(page==Page.SETUP&&(key==KeyEvent.KEYCODE_BUTTON_L1||key==KeyEvent.KEYCODE_BUTTON_R1)){
            if(event.getAction()==0&&event.getRepeatCount()==0){ Button target=key==KeyEvent.KEYCODE_BUTTON_L1?setupPrevious:setupNext; if(target!=null&&target.isEnabled()) target.performClick(); } return true;
        }
        if(page==Page.APPS){
            if(key==KeyEvent.KEYCODE_BUTTON_R1){ if(event.getAction()==0&&event.getRepeatCount()==0) closeOthers(); return true; }
            if(key==KeyEvent.KEYCODE_BUTTON_SELECT){ if(event.getAction()==KeyEvent.ACTION_DOWN && event.getRepeatCount()==0 && !toolZone) protectSelected(); return true; }
            if(key>=KeyEvent.KEYCODE_DPAD_UP && key<=KeyEvent.KEYCODE_DPAD_RIGHT){
                if(event.getAction()==KeyEvent.ACTION_DOWN) navigate(key==KeyEvent.KEYCODE_DPAD_LEFT?-1:key==KeyEvent.KEYCODE_DPAD_RIGHT?1:key==KeyEvent.KEYCODE_DPAD_UP?-2:2); return true;
            }
            if(key==a || key==x || key==y || key==KeyEvent.KEYCODE_DPAD_CENTER){
                if(event.getAction()==KeyEvent.ACTION_DOWN && event.getRepeatCount()==0){
                    if(key==a || key==KeyEvent.KEYCODE_DPAD_CENTER){ if(toolZone&&!tools.isEmpty()) tools.get(Math.max(0,Math.min(toolIndex,tools.size()-1))).performClick(); else openSelected(); }
                    else { if(key==y) closeAll(); else if(!toolZone) closeSelected(); }
                } return true;
            }
        } else {
            if(key==a || key==KeyEvent.KEYCODE_DPAD_CENTER){
                if(event.getAction()==0&&event.getRepeatCount()==0){ if(controlIndex<0) pageNavigate(1); if(controlIndex>=0&&controlIndex<controls.size()&&controls.get(controlIndex).isEnabled()) controls.get(controlIndex).performClick(); } return true;
            }
            if(key>=KeyEvent.KEYCODE_DPAD_UP && key<=KeyEvent.KEYCODE_DPAD_RIGHT){
                if(event.getAction()==KeyEvent.ACTION_DOWN) pageNavigate(key==KeyEvent.KEYCODE_DPAD_UP || key==KeyEvent.KEYCODE_DPAD_LEFT?-1:1); return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }
    private void focusControl(int index){
        if(index<0||index>=controls.size()) return; controlIndex=index;
        for(int i=0;i<controls.size();i++) controls.get(i).setActivated(controllerMode&&i==index);
        Button target=controls.get(index); target.requestFocus(); target.requestRectangleOnScreen(new android.graphics.Rect(0,0,target.getWidth(),target.getHeight()),false);
    }
    private void pageNavigate(int delta){
        if(controls.isEmpty()) return; int next=controlIndex<0?0:Math.max(0,Math.min(controls.size()-1,controlIndex+delta));
        while(!controls.get(next).isEnabled() && next+delta>=0 && next+delta<controls.size()) next+=delta;
        focusControl(next);
    }
    private void navigate(int direction){
        if(page!=Page.APPS){ pageNavigate(direction<0?-1:1); return; }
        if(tools.isEmpty()) return; if(cards.isEmpty()) toolZone=true; toolIndex=Math.max(0,Math.min(toolIndex,tools.size()-1));
        boolean dense=!"cards".equals(prefs().getString("view","grid"));
        if(dense&&!toolZone&&!cards.isEmpty()){
            int index=selection.index();
            if(direction==-2&&index<gridColumns){ toolZone=true; }
            else { if(direction==-2) selection.move(-gridColumns); else if(direction==2) selection.move(Math.min(gridColumns,cards.size()-1-index)); else if(direction<0&&index%gridColumns>0) selection.move(-1); else if(direction>0&&index%gridColumns<gridColumns-1&&index+1<cards.size()) selection.move(1); markSelection(); return; }
        }
        if(direction==-2){ toolZone=true; if(!tools.isEmpty()) tools.get(toolIndex).requestFocus(); }
        else if(direction==2){ toolZone=cards.isEmpty(); }
        else if(toolZone){ toolIndex=Math.max(0,Math.min(tools.size()-1,toolIndex+direction)); tools.get(toolIndex).requestFocus(); }
        else selection.move(direction);
        for(int i=0;i<tools.size();i++) tools.get(i).setActivated(controllerMode&&toolZone&&i==toolIndex);
        markSelection();
    }
    @Override public boolean onGenericMotionEvent(MotionEvent event){
        if(!event.isFromSource(InputDevice.SOURCE_JOYSTICK)) return super.onGenericMotionEvent(event);
        setInputMode(true); if(page==Page.CONFIRM) return true;
        float x=event.getAxisValue(MotionEvent.AXIS_HAT_X),y=event.getAxisValue(MotionEvent.AXIS_HAT_Y);
        if(Math.abs(x)<.55f) x=event.getAxisValue(MotionEvent.AXIS_X); if(Math.abs(y)<.55f) y=event.getAxisValue(MotionEvent.AXIS_Y);
        int next=Math.abs(x)>=Math.abs(y)?x>.55f?1:x<-.55f?-1:0:y>.55f?2:y<-.55f?-2:0;
        if(next!=stickDirection){ stopStick(); stickDirection=next; if(next!=0){ navigate(next); main.postDelayed(repeat,360); } }
        return true;
    }
    private void stopStick(){ stickDirection=0; main.removeCallbacks(repeat); }
    @Override public void onWindowFocusChanged(boolean focused){ super.onWindowFocusChanged(focused); if(!focused) stopStick(); }
}
