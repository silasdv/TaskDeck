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
    enum Page { APPS, SETUP, CONNECT, CHORD, CONFIRM, SHORTCUTS, RESULTS, WINDOWS, SETTINGS, MEMORY, COMPLETE, MAPPING, BIND, DOCKSTYLE, DOCKBEHAVIOR, PICKER, PAGING, ACCENT }
    private static final int DECK_GAP=8;
    private TaskDeckApp app;
    private Page page;
    private DeckTheme theme;
    private LinearLayout root,cardRow,sidebar;
    private FrameLayout appContent;
    private ScrollView appScroll;
    private GridLayout appGrid;
    private TextView compactMemory;
    private QuickAccessDock dock; private Page pickerReturn=Page.SETTINGS; private boolean dockZone; private int dockIndex,pickerPage;
    private Button ramButton,pickerPrevious,pickerNext,pagePrevious,pageNext; private List<JSONObject> pickerApps;
    private List<String> previewApps; private int previewCount=-1; private LinearLayout previewHost; private QuickAccessDock previewDock; private Button previewMinus,previewPlus;
    private String pickerQuery=""; private LinearLayout pickerRows; private EditText pickerSearch; private final List<Button> pickerOptions=new ArrayList<>();
    private Button setupPrevious,setupNext;
    private int gridColumns=1,renderWidth,renderHeight,iconAccent;
    private HorizontalScrollView rail;
    private TextView status,memory,recordLive,recordSaved,backgroundInfo;
    private LinearLayout formMain,formInfo,memoryBars,systemRamPanel,systemRamChart;
    private int settingsCategory,ramPage;
    private final List<Button> categories=new ArrayList<>(),colorDots=new ArrayList<>(),setupThemes=new ArrayList<>();
    private int colorColumns=13;
    private final List<JSONObject> backgroundApps=new ArrayList<>();
    private Button saveChord;
    private PopupWindow protectionPopup;
    private ScrollView pageScroll;
    private boolean pendingRefresh,controllerMode=true,resetSelection=true,captureEntry=true;
    private int setupStep,controlIndex=-1,catalogGeneration,detailsPage;
    private boolean recordingQuickSwitch,readError,fitOverflow;
    private String memoryStatus="Not sampled";
    private JSONObject windowGroup;
    private String bindingAction="open";
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
        Button b=new Button(this); b.setText(value); b.setAllCaps(false); b.setTextSize(13); b.setTextColor(theme.text); b.setFocusableInTouchMode(controllerMode); b.setDefaultFocusHighlightEnabled(false);
        String kind=ActionGlyph.kind(value); int tint=ActionGlyph.color(kind,theme); b.setTextColor(tint);
        Drawable glyph=new ActionGlyph(kind,tint); glyph.setBounds(0,0,dp(18),dp(18)); b.setCompoundDrawables(glyph,null,null,null); b.setCompoundDrawablePadding(dp(7)); b.setMinWidth(0); b.setMinimumWidth(0);
        StateListDrawable bg=new StateListDrawable(); GradientDrawable focused=rounded(theme.selected); focused.setStroke(dp(3),tint);
        GradientDrawable selected=rounded(theme.selected); selected.setStroke(dp(1),theme.accent);
        bg.addState(new int[]{android.R.attr.state_activated},focused); bg.addState(new int[]{android.R.attr.state_focused},focused); bg.addState(new int[]{android.R.attr.state_pressed},rounded(theme.selected)); bg.addState(new int[]{android.R.attr.state_selected},selected); bg.addState(new int[]{},rounded(theme.raised));
        boolean compact=getResources().getConfiguration().screenHeightDp<440;
        b.setBackground(new InsetDrawable(bg,0,dp(6),0,dp(6))); b.setBackgroundTintList(null); b.setMinHeight(dp(48)); b.setMinimumHeight(dp(48)); b.setPadding(dp(10),dp(2),dp(10),dp(2)); b.setOnClickListener(v->action.run());
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-2,-2); params.setMargins(0,dp(2),dp(6),dp(2)); parent.addView(b,params); controls.add(b); return b;
    }
    private LinearLayout row(LinearLayout parent){ LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL); parent.addView(row); return row; }
    private LinearLayout section(LinearLayout parent,String title,String description){
        LinearLayout panel=new LinearLayout(this); panel.setOrientation(LinearLayout.VERTICAL); panel.setPadding(dp(18),dp(12),dp(18),dp(12)); panel.setBackground(rounded(theme.surface));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.bottomMargin=dp(12); parent.addView(panel,p);
        text(panel,title,20,false).setTypeface(null,Typeface.BOLD); if(!description.isEmpty()) text(panel,description,14,true); return panel;
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state); app=(TaskDeckApp)getApplication(); setupStep=state==null?0:state.getInt("setupStep",0); settingsCategory=state==null?0:state.getInt("settingsCategory",0); ramPage=state==null?0:state.getInt("ramPage",0);
        if(Build.VERSION.SDK_INT>=30) getWindow().setDecorFitsSystemWindows(false);
        else getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        if(state!=null&&"APPS".equals(state.getString("pickerReturn"))) pickerReturn=Page.APPS;
        Page initial=prefs().getBoolean("setupComplete",false)?Page.APPS:Page.SETUP;
        if(state!=null) try { initial=Page.valueOf(state.getString("page",initial.name())); } catch(IllegalArgumentException ignored){}
        if(initial==Page.CHORD || initial==Page.CONFIRM || initial==Page.WINDOWS || initial==Page.COMPLETE) initial=prefs().getBoolean("setupComplete",false)?Page.APPS:Page.SETUP;
        if(initial==Page.BIND) initial=Page.MAPPING; if(initial==Page.ACCENT){ initial=Page.SETTINGS; settingsCategory=2; }
        if(initial==Page.SETUP&&prefs().getBoolean("setupComplete",false)) initial=Page.SETTINGS;
        if(getLastNonConfigurationInstance() instanceof JSONObject){ entryTask=(JSONObject)getLastNonConfigurationInstance(); captureEntry=false; }
        show(initial); if(initial==Page.PICKER) showPicker();
    }
    @Override public Object onRetainNonConfigurationInstance(){ return entryTask; }
    @Override public void onSaveInstanceState(Bundle out){ out.putString("page",page.name()); out.putInt("setupStep",setupStep); out.putInt("settingsCategory",settingsCategory); out.putInt("ramPage",ramPage); out.putString("pickerReturn",pickerReturn.name()); super.onSaveInstanceState(out); }
    @Override protected void onResume(){
        super.onResume(); TaskDeckApp.recordingShortcut=page==Page.CHORD || page==Page.CONFIRM || page==Page.BIND;
        app.changed=this::connectionChanged; app.verifyLiveAccess(); if(page==Page.SETUP || page==Page.SETTINGS) show(page); updateStatus();
        if(app.backend!=null) refresh(); else app.connect(false);
    }
    @Override protected void onPause(){ app.changed=null; TaskDeckApp.recordingShortcut=false; stopStick(); super.onPause(); }
    @Override protected void onNewIntent(Intent intent){ super.onNewIntent(intent); setIntent(intent); if(intent.getBooleanExtra("controllerEntry",false)) controllerMode=true; resetSelection=true; captureEntry=true; entryTask=null; show(prefs().getBoolean("setupComplete",false)?Page.APPS:Page.SETUP); if(app.backend!=null) refresh(); }
    private void connectionChanged(){
        if(isFinishing() || isDestroyed()) return;
        if(page==Page.SETUP){ show(Page.SETUP); }
        if(app.backend==null){ busy=false; catalogGeneration++; rawTasks.clear(); backgroundApps.clear(); tasks.clear(); selection.update(Collections.emptyList()); if(page==Page.APPS){ renderCards(); updateMemory(); } updateStatus(); }
        else { updateStatus(); refresh(); }
        if(page==Page.SETTINGS && settingsCategory==0) show(Page.SETTINGS);
    }
    private void show(Page next){
        if(next==Page.ACCENT){ next=Page.SETTINGS; settingsCategory=2; }
        int restoreY=page==Page.SETUP && next==Page.SETUP && pageScroll!=null?pageScroll.getScrollY():0;
        int restoreFocus=page==next&&next!=Page.APPS&&next!=Page.PICKER?controlIndex:-1;
        if(next==Page.APPS){ resetSelection=true; selection.first(); }
        if(protectionPopup!=null) protectionPopup.dismiss(); pageScroll=null;
        stopStick(); feedback=""; TaskDeckApp.recordingShortcut=next==Page.CHORD || next==Page.CONFIRM || next==Page.BIND; page=next; toolZone=false; dockZone=false; dock=null; ramButton=null; pagePrevious=null; pageNext=null;
        controls.clear(); controlIndex=-1; tools.clear(); cards.clear(); cardRow=null; rail=null; appScroll=null; appGrid=null; appContent=null; compactMemory=null; operationInfo=null; memoryBars=null; systemRamPanel=null; systemRamChart=null; categories.clear(); colorDots.clear(); setupThemes.clear(); setupPrevious=null; setupNext=null; renderWidth=0; renderHeight=0; status=null; memory=null;
        theme=DeckTheme.resolve(prefs().getString("theme","dark"),getResources().getConfiguration().uiMode).withAccent(prefs().getString("accentColor","theme"));
        if(iconAccent!=theme.accent){ iconCache.evictAll(); iconAccent=theme.accent; }
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(theme.background); root.setPadding(dp(12),dp(6),dp(12),dp(6));
        root.setOnApplyWindowInsetsListener((view,insets)->{
            if(Build.VERSION.SDK_INT>=30){ android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars()); view.setPadding(dp(12)+bars.left,dp(6)+bars.top,dp(12)+bars.right,dp(6)+bars.bottom); }
            else view.setPadding(dp(12)+insets.getSystemWindowInsetLeft(),dp(6)+insets.getSystemWindowInsetTop(),dp(12)+insets.getSystemWindowInsetRight(),dp(6)+insets.getSystemWindowInsetBottom());
            return insets;
        }); root.setFocusableInTouchMode(true); setContentView(root); if(!controllerMode) root.requestFocus();
        if(page==Page.APPS) appsPage(); else if(page==Page.SETUP) setupPage(); else if(page==Page.CONNECT) connectPage(); else if(page==Page.SHORTCUTS) shortcutsPage(); else if(page==Page.RESULTS) resultsPage(); else if(page==Page.WINDOWS) windowsPage(); else if(page==Page.SETTINGS) settingsPage(); else if(page==Page.MEMORY) memoryPage(); else if(page==Page.COMPLETE) completionPage(); else if(page==Page.MAPPING) mappingPage(); else if(page==Page.BIND) bindPage(); else if(page==Page.DOCKSTYLE) dockStylePage(); else if(page==Page.DOCKBEHAVIOR) dockBehaviorPage(); else if(page==Page.PICKER) pickerPage(); else if(page==Page.PAGING) pagingMappingPage();  else recordPage();
        if(restoreY>0 && pageScroll!=null){ ScrollView scroll=pageScroll; main.post(()->scroll.scrollTo(0,restoreY)); }
        if(controllerMode && restoreFocus>=0 && restoreFocus<controls.size()){ int index=restoreFocus; LinearLayout renderedRoot=root; main.post(()->{ if(root==renderedRoot) focusControl(index); }); }
        if(controllerMode&&restoreFocus<0&&next!=Page.APPS&&next!=Page.COMPLETE&&controls.size()>1) focusControl(next==Page.ACCENT?Math.max(0,Arrays.asList(DeckTheme.ACCENT_KEYS).indexOf(prefs().getString("accentColor","theme")))+1:next==Page.RESULTS?0:next==Page.SETTINGS?1+settingsCategory:next==Page.PICKER&&!pickerOptions.isEmpty()?controls.indexOf(pickerOptions.get(0)):1);
        boundMenuButtons(formMain); boundMenuButtons(formInfo); applySystemBars(); root.requestApplyInsets();
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
    private String actionFor(String key){ switch(key){ case "A":return "open"; case "B":return "exit"; case "X":return "close"; case "Y":return "clear"; default:return "others"; } }
    private Button controllerButton(LinearLayout parent,String key,String label,String icon,Runnable action){
        String printed=ControllerBindings.badge(prefs(),actionFor(key)); Button b=compactButton(button(parent,label,action));
        Drawable badge=new ControllerBadge(printed,theme.text); boolean shoulder=printed.length()>1&&!ControllerBadge.symbol(printed); badge.setBounds(0,0,dp(shoulder?21:20),dp(shoulder?16:20));
        b.setCompoundDrawables(badge,null,null,null); b.setCompoundDrawablePadding(dp(3)); String full=ControllerBindings.NAMES[Arrays.asList(ControllerBindings.ACTIONS).indexOf(actionFor(key))]; b.setContentDescription(printed+": "+full); b.setTooltipText(full); b.setTextSize(key.equals("R1")?9:9.5f); b.setPadding(dp(shoulder?9:7),dp(4),dp(shoulder?9:7),dp(4)); b.setSingleLine(true);
        ((LinearLayout.LayoutParams)b.getLayoutParams()).rightMargin=dp(DECK_GAP); return b;
    }

    private void applySystemBars(){
        boolean light=theme.background==DeckTheme.named("light").background; getWindow().setStatusBarColor(theme.background); getWindow().setNavigationBarColor(theme.background);
        if(Build.VERSION.SDK_INT>=30&&getWindow().getInsetsController()!=null){ int flags=android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS|android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS; getWindow().getInsetsController().setSystemBarsAppearance(light?flags:0,flags); } else if(Build.VERSION.SDK_INT<30){ int mask=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR,flags=getWindow().getDecorView().getSystemUiVisibility(); getWindow().getDecorView().setSystemUiVisibility((flags&~mask)|(light?mask:0)); }
    }
    private void roundSettings(Button button){
        StateListDrawable states=new StateListDrawable(); for(int state:new int[]{android.R.attr.state_activated,android.R.attr.state_focused,android.R.attr.state_pressed,0}){ GradientDrawable shape=new GradientDrawable(); shape.setShape(GradientDrawable.OVAL); shape.setColor(state==0?theme.raised:theme.selected); if(state!=0) shape.setStroke(dp(2),theme.accent); states.addState(state==0?new int[]{}:new int[]{state},shape); } button.setBackground(states); button.setPadding(dp(9),0,dp(9),0);
    }
    private boolean isCurrent(String pkg){ return entryTask!=null&&pkg.equals(entryTask.optString("package"))&&rawTasks.stream().anyMatch(t->pkg.equals(t.optString("package"))); }
    private String launchLabel(String pkg){ try { return getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(pkg,0)).toString(); } catch(Exception unavailable){ return pkg; } }
    private boolean processReported(String pkg){ for(List<JSONObject> rows:Arrays.asList(rawTasks,backgroundApps)) for(JSONObject task:rows) if(pkg.equals(task.optString("package"))&&Set.of("sampled","shared","partial").contains(task.optString("memoryState"))) return true; return false; }
    private void createDock(){
        dock=new QuickAccessDock(this,prefs(),new QuickAccessDock.Host(){
            public Drawable icon(String pkg){ return appIcon(pkg); } public String label(String pkg){ return launchLabel(pkg); } public boolean running(String pkg){ return processReported(pkg); } public boolean current(String pkg){ return isCurrent(pkg); }
            public void launch(String pkg){ dockZone=true; toolZone=false; dockIndex=Math.max(0,QuickAccess.read(prefs()).indexOf(pkg)); launchFavorite(pkg); }
            public void options(String pkg){ dockOptions(pkg); } public void add(){ showPicker(); } public void changed(){ renderDock(); }
            public boolean controller(){ return controllerMode; }
            public void touch(){ setInputMode(false); dockZone=false; if(dock!=null) dock.blur(); }
        }); root.addView(dock,new LinearLayout.LayoutParams(-1,-2)); renderDock();
    }
    private void renderDock(){ if(dock==null||page!=Page.APPS) return; ((LinearLayout.LayoutParams)dock.getLayoutParams()).topMargin=dp(QuickAccess.read(prefs()).isEmpty()?DECK_GAP:DECK_GAP-4); dock.update(theme); if(dockZone&&controllerMode){ dockIndex=Math.max(0,Math.min(dock.items.size()-1,dockIndex)); dock.focusItem(dockIndex); } }
    private void launchFavorite(String pkg){
        if(busy) return; if(app.backend!=null) for(JSONObject task:rawTasks) if(pkg.equals(task.optString("package"))){ openWindow(task); return; }
        Intent intent=getPackageManager().getLaunchIntentForPackage(pkg); if(intent==null){ feedback="App is no longer installed or has no launchable screen. Select it to remove the shortcut."; updateStatus(); return; }
        try { startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); finish(); } catch(RuntimeException denied){ feedback="Android could not open "+launchLabel(pkg); updateStatus(); }
    }
    private void pin(String pkg){
        if(!QuickAccess.read(prefs()).contains(pkg)&&getPackageManager().getLaunchIntentForPackage(pkg)==null){ feedback="This app has no launchable shortcut"; updateStatus(); return; }
        QuickAccess.toggle(prefs(),pkg); renderDock();
    }
    private void choice(String title,String[] labels,int selected,java.util.function.IntConsumer picked){
        AlertDialog.Builder builder=new AlertDialog.Builder(this).setTitle(title); DialogInterface.OnClickListener pick=(d,which)->{ d.dismiss(); picked.accept(which); }; if(selected<0) builder.setItems(labels,pick); else builder.setSingleChoiceItems(labels,selected,pick); AlertDialog dialog=builder.setNegativeButton("Cancel",null).create();
        dialog.setOnKeyListener((d,key,event)->{ setInputMode(true);
            if(key==ControllerBindings.code(prefs(),"exit")||key==KeyEvent.KEYCODE_BACK){ if(event.getAction()==0) d.dismiss(); return true; }
            if(key==ControllerBindings.code(prefs(),"open")||key==KeyEvent.KEYCODE_DPAD_CENTER){ if(event.getAction()==0&&event.getRepeatCount()==0){ int value=dialog.getListView().getSelectedItemPosition(); if(value<0) value=dialog.getListView().getCheckedItemPosition(); d.dismiss(); picked.accept(Math.max(0,value)); } return true; } return false;
        });
        dialog.setOnShowListener(ignored->{ int[] axis={0}; dialog.getListView().setOnTouchListener((view,event)->{ if(event.getActionMasked()==MotionEvent.ACTION_DOWN) setInputMode(false); return false; }); dialog.getListView().setOnGenericMotionListener((view,event)->{ if(!event.isFromSource(InputDevice.SOURCE_JOYSTICK)) return false; setInputMode(true); float y=event.getAxisValue(MotionEvent.AXIS_HAT_Y); if(Math.abs(y)<.55f) y=event.getAxisValue(MotionEvent.AXIS_Y); int next=y>.55f?1:y<-.55f?-1:0;
            if(next!=0&&next!=axis[0]){ int value=dialog.getListView().getSelectedItemPosition(); if(value<0) value=dialog.getListView().getCheckedItemPosition(); dialog.getListView().setSelection(Math.max(0,Math.min(labels.length-1,value+next))); } axis[0]=next; return true; }); }); dialog.show();
    }
    private void dockOptions(String pkg){
        choice("Quick Access",new String[]{"Open "+launchLabel(pkg),"Move left","Move right","Remove from Quick Access"},-1,index->{ if(index==0){ launchFavorite(pkg); return; } List<String> order=QuickAccess.read(prefs()); int position=order.indexOf(pkg); if(index==3) order.remove(pkg); else order=QuickAccess.move(order,pkg,position+(index==1?-1:1)); QuickAccess.save(prefs(),order); dockIndex=Math.max(0,order.indexOf(pkg)); renderDock(); });
    }
    private void dockStylePage(){
        formPage("Dock style & size"); String[] styles={"Floating","Compact","Shelf","Thumb corners"},styleKeys={"floating","compact","shelf","thumb"}; int current=Math.max(0,Arrays.asList(styleKeys).indexOf(prefs().getString("dockStyle","thumb")));
        button(formMain,"Style: "+styles[current],()->choice("Dock style",styles,current,index->{ prefs().edit().putString("dockStyle",styleKeys[index]).apply(); show(Page.DOCKSTYLE); }));
        String[] themes={"Follow app","Light","Dark","OLED"},themeKeys={"app","light","dark","oled"}; int chosen=Math.max(0,Arrays.asList(themeKeys).indexOf(prefs().getString("dockTheme","app")));
        button(formMain,"Theme: "+themes[chosen],()->choice("Dock theme",themes,chosen,index->{ prefs().edit().putString("dockTheme",themeKeys[index]).apply(); show(Page.DOCKSTYLE); }));
        note(formMain,"Size: "+QuickAccess.size(prefs())+"%"); LinearLayout sizes=row(formMain);
        button(sizes,"− Size",()->{ prefs().edit().putInt("dockSize",Math.max(60,QuickAccess.size(prefs())-10)).apply(); show(Page.DOCKSTYLE); }); button(sizes,"Size +",()->{ prefs().edit().putInt("dockSize",Math.min(160,QuickAccess.size(prefs())+10)).apply(); show(Page.DOCKSTYLE); });
        text(formInfo,"Live preview",20,false); note(formInfo,"Actual app icons, at the same size and bottom position as your dock. Preview changes never alter your favorites."); LinearLayout counts=row(formInfo); previewMinus=button(counts,"− Apps",()->{ previewCount=Math.max(1,previewCount-1); renderPreview(); }); previewPlus=button(counts,"Apps +",()->{ if(previewApps!=null) previewCount=Math.min(previewApps.size(),previewCount+1); renderPreview(); }); dockPreview(root);
    }
    private void dockBehaviorPage(){
        formPage("Dock behavior"); boolean focus="focus".equals(prefs().getString("dockBehavior","always"));
        button(formMain,"Visibility: "+(focus?"On navigation":"Always visible"),()->choice("Dock visibility",new String[]{"Always visible","Show on navigation / tap"},focus?1:0,index->{ prefs().edit().putString("dockBehavior",index==1?"focus":"always").apply(); show(Page.DOCKBEHAVIOR); }));
        toggle(formMain,"Magnify focused icons",prefs().getBoolean("dockMagnify",true),()->{ prefs().edit().putBoolean("dockMagnify",!prefs().getBoolean("dockMagnify",true)).apply(); show(Page.DOCKBEHAVIOR); }); toggle(formMain,"Running indicators",prefs().getBoolean("dockIndicators",true),()->{ prefs().edit().putBoolean("dockIndicators",!prefs().getBoolean("dockIndicators",true)).apply(); show(Page.DOCKBEHAVIOR); });
        text(formInfo,"Touch and controller",20,false); note(formInfo,"Down from the last app row opens Quick Access. Up returns to the app list. On-navigation mode collapses it again when you leave it; touch its handle to reveal it."); note(formInfo,"Hold and drag icons to reorder. With a controller, press Select for move/remove options. Dots appear only when a process is reported, or for the Current app.");
    }
    private void showPicker(){
        if(page!=Page.PICKER) pickerReturn=page==Page.APPS?Page.APPS:Page.SETTINGS; pickerApps=null; pickerPage=0; pickerQuery=""; show(Page.PICKER);
        app.requestMemory("Installed apps",5000,()->{ Map<String,JSONObject> launchers=new TreeMap<>(); Intent launcher=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
            for(android.content.pm.ResolveInfo info:getPackageManager().queryIntentActivities(launcher,0)) if(info.activityInfo!=null&&!getPackageName().equals(info.activityInfo.packageName)) launchers.put(info.activityInfo.packageName,new JSONObject().put("package",info.activityInfo.packageName).put("label",info.loadLabel(getPackageManager()).toString())); return new JSONObject().put("apps",new JSONArray(launchers.values()));
        },result->{ if(page!=Page.PICKER||isDestroyed()) return; pickerApps=new ArrayList<>(); JSONArray values=result.optJSONArray("apps"); if(values!=null) for(int i=0;i<values.length();i++) if(values.optJSONObject(i)!=null) pickerApps.add(values.optJSONObject(i)); pickerApps.sort((a,b)->String.CASE_INSENSITIVE_ORDER.compare(a.optString("label"),b.optString("label"))); fillPicker(); if(result.has("error")){ feedback=result.optString("error"); updateStatus(); } });
    }
    private void pickerPage(){
        LinearLayout heading=row(root); TextView title=text(heading,"Choose apps",22,false); title.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1)); button(heading,"Back",this::back);
        LinearLayout search=row(root); pickerSearch=new EditText(this); pickerSearch.setSingleLine(true); pickerSearch.setTextSize(16); pickerSearch.setTextColor(theme.text); pickerSearch.setHintTextColor(theme.muted); pickerSearch.setHint("Search apps"); pickerSearch.setContentDescription("Search apps"); pickerSearch.setText(pickerQuery); search.addView(pickerSearch,new LinearLayout.LayoutParams(0,dp(48),1));
        button(search,"Search",()->{ pickerSearch.requestFocus(); getSystemService(android.view.inputmethod.InputMethodManager.class).showSoftInput(pickerSearch,android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT); });
        pickerRows=new LinearLayout(this); pickerRows.setOrientation(LinearLayout.HORIZONTAL); pickerRows.setGravity(Gravity.CENTER); root.addView(pickerRows,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout navigation=row(root); pickerPrevious=pagingButton(navigation,"page_previous","Previous",()->pickerTurn(-1)); pagePrevious=pickerPrevious; TextView help=text(navigation,"Selected apps appear in Quick Access",11,true); help.setGravity(Gravity.CENTER); help.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1)); pickerNext=pagingButton(navigation,"page_next","Next",()->pickerTurn(1)); pageNext=pickerNext;
        pickerOptions.clear(); fillPicker();
        pickerSearch.addTextChangedListener(new android.text.TextWatcher(){ public void beforeTextChanged(CharSequence s,int a,int c,int f){} public void onTextChanged(CharSequence value,int start,int before,int count){ pickerQuery=value.toString(); pickerPage=0; fillPicker(); } public void afterTextChanged(android.text.Editable e){} });
        if(controllerMode&&!pickerOptions.isEmpty()) focusControl(controls.indexOf(pickerOptions.get(0)));
    }
    private void pickerTurn(int direction){
        if(direction<0&&pickerPage==0) return;
        if(direction>0&&(pickerNext==null||!pickerNext.isEnabled())) return;
        pickerPage=Math.max(0,pickerPage+direction); controlIndex=-1; show(Page.PICKER);
    }
    private void fillPicker(){
        if(pickerRows==null) return; boolean editing=pickerSearch.hasFocus(); controls.removeAll(pickerOptions); pickerOptions.clear(); pickerRows.removeAllViews();
        List<JSONObject> filtered=new ArrayList<>(); if(pickerApps!=null) for(JSONObject item:pickerApps) if(item.optString("label").toLowerCase(Locale.ROOT).contains(pickerQuery.toLowerCase(Locale.ROOT))) filtered.add(item);
        filtered.sort((a,b)->String.CASE_INSENSITIVE_ORDER.compare(a.optString("label"),b.optString("label")));
        int count=filtered.size(); pickerPage=Math.max(0,Math.min(pickerPage,Math.max(0,(count-1)/4)));
        if(pickerApps==null) note(pickerRows,"Loading installed apps…"); else if(count==0) note(pickerRows,"No apps found");
        int insert=controls.indexOf(pickerPrevious);
        for(int i=pickerPage*4;i<Math.min(count,pickerPage*4+4);i++){
            JSONObject item=filtered.get(i); String pkg=item.optString("package"),name=item.optString("label");
            Button option=button(pickerRows,name,()->{ pin(pkg); for(Button row:pickerOptions){ String target=(String)row.getTag(); boolean selected=QuickAccess.read(prefs()).contains(target); row.setSelected(selected); row.setContentDescription(row.getText()+ (selected?", in Quick Access":", not in Quick Access")); } });
            option.setTag(pkg); option.setGravity(Gravity.CENTER); option.setTextSize(16); option.setMaxLines(2); option.setEllipsize(android.text.TextUtils.TruncateAt.END); option.setPadding(dp(10),dp(60),dp(10),0); FrameLayout tileFrame=new FrameLayout(this); pickerRows.removeView(option); LinearLayout.LayoutParams tile=new LinearLayout.LayoutParams(0,-1,1); tile.setMargins(dp(4),dp(4),dp(4),dp(4)); pickerRows.addView(tileFrame,tile); tileFrame.addView(option,new FrameLayout.LayoutParams(-1,-1));
            option.setCompoundDrawables(null,null,null,null); ImageView image=new ImageView(this); image.setTag("pickerIcon"); image.setImageDrawable(appIcon(pkg)); image.setScaleType(ImageView.ScaleType.FIT_CENTER); image.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO); FrameLayout.LayoutParams picture=new FrameLayout.LayoutParams(dp(48),dp(48),Gravity.CENTER); picture.topMargin=-dp(48); tileFrame.addView(image,picture); tileFrame.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{ int height=b-t; int lines=height<dp(112)?1:2; option.setMaxLines(lines); android.graphics.Paint.FontMetrics metrics=option.getPaint().getFontMetrics(); int label=(int)Math.ceil(metrics.bottom-metrics.top)*lines; int iconSize=Math.max(dp(12),Math.min(dp(48),height-dp(24)-label)); FrameLayout.LayoutParams position=(FrameLayout.LayoutParams)image.getLayoutParams(); position.width=iconSize; position.height=iconSize; position.gravity=Gravity.TOP|Gravity.CENTER_HORIZONTAL; position.topMargin=Math.max(dp(8),(height-iconSize-dp(8)-label)/2); image.setLayoutParams(position); image.setVisibility(View.VISIBLE); option.setPadding(dp(10),iconSize+dp(8),dp(10),0); }); boolean selected=QuickAccess.read(prefs()).contains(pkg); option.setSelected(selected); option.setContentDescription(name+(selected?", in Quick Access":", not in Quick Access")); controls.remove(option); controls.add(insert++,option); pickerOptions.add(option);
        }
        pickerPrevious.setText("Previous"); pickerPrevious.setEnabled(pickerPage>0); pickerNext.setEnabled((pickerPage+1)*4<count); pickerNext.setContentDescription("Next page: "+ControllerBindings.badge(prefs(),"page_next"));
        if(count>0) for(int slot=pickerOptions.size();slot<4;slot++) pickerRows.addView(new View(this),new LinearLayout.LayoutParams(0,1,1));
        if(editing){ pickerSearch.requestFocus(); controlIndex=-1; } else if(controllerMode&&!pickerOptions.isEmpty()) focusControl(controls.indexOf(pickerOptions.get(0)));
    }
    private void dockPreview(LinearLayout parent){
        previewHost=new LinearLayout(this); previewHost.setOrientation(LinearLayout.VERTICAL); previewHost.setContentDescription("Live dock preview"); parent.addView(previewHost,new LinearLayout.LayoutParams(-1,-2));
        if(previewApps!=null){ renderPreview(); return; }
        previewMinus.setEnabled(false); previewPlus.setEnabled(false);
        app.requestMemory("Preview apps",5000,()->{ Map<String,String> installed=new TreeMap<>(); Intent intent=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER); for(android.content.pm.ResolveInfo info:getPackageManager().queryIntentActivities(intent,0)) if(info.activityInfo!=null) installed.put(info.activityInfo.packageName,info.loadLabel(getPackageManager()).toString()); List<String> keys=new ArrayList<>(installed.keySet()); keys.sort((a,b)->String.CASE_INSENSITIVE_ORDER.compare(installed.get(a),installed.get(b))); return new JSONObject().put("packages",new JSONArray(keys)); },result->{
            if(isDestroyed()) return; previewApps=new ArrayList<>(); JSONArray values=result.optJSONArray("packages"); if(values!=null) for(int i=0;i<values.length();i++) previewApps.add(values.optString(i)); if(page==Page.DOCKSTYLE) renderPreview();
        });
    }
    private void renderPreview(){
        if(page!=Page.DOCKSTYLE||previewHost==null||previewApps==null) return;
        previewHost.removeAllViews(); if(previewCount<0) previewCount=Math.max(1,QuickAccess.read(prefs()).size()); previewCount=Math.max(1,Math.min(previewCount,previewApps.size()));
        previewMinus.setEnabled(previewCount>1); previewPlus.setEnabled(previewCount<previewApps.size());
        if(previewApps.isEmpty()){ note(previewHost,"No launchable apps available for preview"); return; }
        previewDock=new QuickAccessDock(this,prefs(),new QuickAccessDock.Host(){
            public Drawable icon(String pkg){ return appIcon(pkg); } public String label(String pkg){ return launchLabel(pkg); } public boolean running(String pkg){ return processReported(pkg); } public boolean current(String pkg){ return isCurrent(pkg); }
            public void launch(String pkg){} public void options(String pkg){} public void add(){} public void changed(){} public void touch(){} public boolean controller(){ return false; }
        }); previewDock.preview(previewApps.subList(0,previewCount)); previewHost.addView(previewDock,new LinearLayout.LayoutParams(-1,-2)); previewDock.update(theme); previewDock.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
    }
    private void scrollPopup(LinearLayout panel){
        LinearLayout body=new LinearLayout(this); body.setOrientation(LinearLayout.VERTICAL); body.setPadding(panel.getPaddingLeft(),panel.getPaddingTop(),panel.getPaddingRight(),panel.getPaddingBottom()); panel.setPadding(0,0,0,0);
        while(panel.getChildCount()>0){ View child=panel.getChildAt(0); panel.removeView(child); body.addView(child); } int width=Math.min(dp(400),getResources().getDisplayMetrics().widthPixels-dp(32)); body.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED));
        ScrollView scroll=new ScrollView(this); scroll.addView(body); panel.addView(scroll,new LinearLayout.LayoutParams(-1,Math.min(body.getMeasuredHeight(),getResources().getDisplayMetrics().heightPixels-dp(32))));
    }
    private void finishSetupAnimation(){ if(page!=Page.COMPLETE) return; show(Page.APPS); if(app.backend!=null) refresh(); }

    private GradientDrawable ramSurface(){ GradientDrawable shape=rounded(theme.surface); shape.setStroke(dp(1),theme.raised); return shape; }
    private int sidebarWidth(){ return getResources().getConfiguration().screenWidthDp>=760?180:152; }
    private Drawable appIcon(String name){
        Drawable.ConstantState cached=iconCache.get(name); if(cached!=null) return cached.newDrawable(getResources()).mutate();
        Drawable value=null; try { value=getPackageManager().getApplicationIcon(name); } catch(PackageManager.NameNotFoundException ignored){}
        if(value==null){ value=getDrawable(R.drawable.ic_tasks); if(value!=null){ value=value.mutate(); value.setTint(theme.accent); } }
        if(value!=null&&value.getConstantState()!=null) iconCache.put(name,value.getConstantState()); return value;
    }

    private Button compactButton(Button b){
        b.setMinHeight(dp(36)); b.setMinimumHeight(dp(36)); b.setTextSize(11); b.setPadding(dp(10),dp(4),dp(10),dp(4));
        Drawable background=b.getBackground(); if(background instanceof InsetDrawable) b.setBackground(((InsetDrawable)background).getDrawable());
        LinearLayout.LayoutParams size=(LinearLayout.LayoutParams)b.getLayoutParams(); size.height=dp(36); size.topMargin=0; size.bottomMargin=0; return b;
    }
    private void appsPage(){
        LinearLayout header=row(root); header.setLayoutParams(new LinearLayout.LayoutParams(-1,-2));
        tools.add(compactButton(button(header,"Refresh",this::refresh))); tools.add(compactButton(button(header,"Sort",this::sortMenu))); tools.add(compactButton(button(header,"View",this::viewMenu)));
        View gap=new View(this); header.addView(gap,new LinearLayout.LayoutParams(0,1,0));
        HorizontalScrollView hints=new HorizontalScrollView(this); hints.setHorizontalScrollBarEnabled(false); hints.setFillViewport(true); header.addView(hints,new LinearLayout.LayoutParams(0,-2,1));
        LinearLayout actions=new LinearLayout(this); actions.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT); hints.addView(actions);
        controllerButton(actions,"A","Open","open",this::openSelected); controllerButton(actions,"B","Exit","exit",this::finish); controllerButton(actions,"X","Close","close",this::closeSelected);
        controllerButton(actions,"Y","Close all","all",this::closeAll); controllerButton(actions,"R1","Close all but current","others",this::closeOthers);
        Button settings=compactButton(button(header,"Settings",()->show(Page.SETTINGS))); settings.setText(""); settings.setContentDescription("Settings"); settings.setTooltipText("Settings"); settings.setGravity(Gravity.CENTER); settings.setCompoundDrawablePadding(0); ((LinearLayout.LayoutParams)settings.getLayoutParams()).width=dp(36); roundSettings(settings); tools.add(settings);
        LinearLayout stats=row(root); stats.setOrientation(LinearLayout.VERTICAL); status=text(stats,"",11,true); status.setPadding(0,0,0,0); status.setLayoutParams(new LinearLayout.LayoutParams(-1,-2)); compactMemory=text(stats,"",11,true); compactMemory.setPadding(dp(10),dp(8),dp(10),dp(8)); compactMemory.setBackground(ramSurface()); compactMemory.setLayoutParams(new LinearLayout.LayoutParams(-1,-2)); status.setVisibility(View.GONE); status.setMaxLines(1); status.setEllipsize(android.text.TextUtils.TruncateAt.END);
        operationInfo=text(root,"",12,true); operationInfo.setMaxLines(2); operationInfo.setEllipsize(android.text.TextUtils.TruncateAt.END); updateOperationInfo();
        LinearLayout deck=row(root); LinearLayout.LayoutParams deckSize=new LinearLayout.LayoutParams(-1,0,1); deckSize.topMargin=dp(DECK_GAP); deck.setLayoutParams(deckSize); deck.setGravity(Gravity.TOP);
        appContent=new FrameLayout(this); deck.addView(appContent,new LinearLayout.LayoutParams(0,-1,1));
        sidebar=new LinearLayout(this); sidebar.setOrientation(LinearLayout.VERTICAL); sidebar.setPadding(0,0,0,0);
        if(landscape()){ LinearLayout.LayoutParams sideSize=new LinearLayout.LayoutParams(dp(sidebarWidth()),-1); sideSize.leftMargin=dp(DECK_GAP); deck.addView(sidebar,sideSize); } else root.addView(sidebar);
        appContent.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{ if(r-l>0&&b-t>0&&(r-l!=renderWidth||b-t!=renderHeight)){ renderWidth=r-l; renderHeight=b-t; renderCards(); } });
        systemRamPanel=new LinearLayout(this); systemRamPanel.setOrientation(LinearLayout.VERTICAL); systemRamPanel.setBackground(ramSurface()); systemRamPanel.setPadding(dp(10),dp(10),dp(10),dp(10)); systemRamPanel.setContentDescription("System RAM information"); sidebar.addView(systemRamPanel,new LinearLayout.LayoutParams(-1,-2));
        memory=text(systemRamPanel,"",11,false); memory.setPadding(0,0,0,0); systemRamChart=new LinearLayout(this); systemRamChart.setOrientation(LinearLayout.VERTICAL); systemRamPanel.addView(systemRamChart);
        memoryBars=new LinearLayout(this); memoryBars.setOrientation(LinearLayout.VERTICAL); memoryBars.setBackground(ramSurface()); memoryBars.setPadding(dp(10),dp(10),dp(10),dp(10)); LinearLayout.LayoutParams appRamSize=new LinearLayout.LayoutParams(-1,0,1); appRamSize.topMargin=dp(DECK_GAP); appRamSize.bottomMargin=dp(DECK_GAP); sidebar.addView(memoryBars,appRamSize);
        backgroundInfo=new TextView(this); backgroundInfo.setVisibility(View.GONE); sidebar.addView(backgroundInfo);
        Button allRam=button(landscape()?sidebar:header,"RAM details",()->{ ramPage=0; show(Page.MEMORY); }); ramButton=allRam; tools.add(allRam); compactButton(allRam); ((LinearLayout.LayoutParams)allRam.getLayoutParams()).rightMargin=0; if(landscape()) ((LinearLayout.LayoutParams)allRam.getLayoutParams()).width=-1;
        if(!landscape()){ header.removeView(allRam); header.addView(allRam,3); tools.remove(allRam); tools.add(3,allRam); allRam.setText("RAM"); }
        sidebar.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{ if(b-t!=ob-ot) updateBackground(); });
        for(Button tool:tools){ if(tool!=settings&&tool!=allRam) ((LinearLayout.LayoutParams)tool.getLayoutParams()).rightMargin=dp(DECK_GAP); } ((LinearLayout.LayoutParams)settings.getLayoutParams()).rightMargin=0;
        updateStatus(); updateMemory(); renderCards();
        createDock();
    }

    private void renderCards(){
        if(appContent==null) return;
        String mode=viewMode(); boolean horizontal="cards".equals(mode),list="list".equals(mode),fit="fit".equals(mode);
        sidebar.setVisibility(landscape()?View.VISIBLE:View.GONE); compactMemory.setVisibility(sidebar.getVisibility()==View.VISIBLE?View.GONE:View.VISIBLE);
        appContent.removeAllViews(); cards.clear(); rail=null; appScroll=null; appGrid=null;
        cardRow=new LinearLayout(this); cardRow.setGravity(Gravity.CENTER_VERTICAL);
        int available=appContent.getWidth()>0?Math.round(appContent.getWidth()/getResources().getDisplayMetrics().density):getResources().getConfiguration().screenWidthDp-40-(landscape()&&sidebar.getVisibility()==View.VISIBLE?sidebarWidth():0);
        int height=appContent.getHeight()>0?Math.round(appContent.getHeight()/getResources().getDisplayMetrics().density):Math.max(120,getResources().getConfiguration().screenHeightDp-130);
        if(horizontal){ rail=new HorizontalScrollView(this); rail.setHorizontalScrollBarEnabled(false); appContent.addView(rail,new FrameLayout.LayoutParams(-1,-1)); rail.addView(cardRow,new HorizontalScrollView.LayoutParams(-2,-1)); gridColumns=1; }
        else {
            gridColumns=list?1:fit?Math.max(1,(int)Math.ceil(Math.sqrt(Math.max(1,tasks.size())*available/(double)Math.max(1,height)*1.2))):Math.max(2,available/128);
            appGrid=new GridLayout(this); appGrid.setColumnCount(gridColumns); appGrid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
            int minimum=Math.max(52,(int)Math.ceil(prefs().getInt("minLabelSp",12)*getResources().getDisplayMetrics().scaledDensity/getResources().getDisplayMetrics().density*1.4)+32);
            fitOverflow=fit&&(height-DECK_GAP*(Math.max(1,(tasks.size()+gridColumns-1)/gridColumns)-1))/Math.max(1,(tasks.size()+gridColumns-1)/gridColumns)<minimum;
            if(fit&&!fitOverflow) appContent.addView(appGrid,new FrameLayout.LayoutParams(-1,-1));
            else { appScroll=new ScrollView(this); appContent.addView(appScroll,new FrameLayout.LayoutParams(-1,-1)); appScroll.addView(appGrid,new ScrollView.LayoutParams(-1,-2)); }
        }
        if(tasks.isEmpty()){ if(!horizontal){ appContent.removeAllViews(); appContent.addView(cardRow,new FrameLayout.LayoutParams(-1,-1)); } text(cardRow,app.backend==null?"Settings → Connection to load apps":"Nothing to close",20,true); if(!dockZone){ toolZone=true; toolIndex=0; } markSelection(); return; }

        int width=horizontal?dp(Math.max(116,Math.min(190,available/3-14))):dp(Math.max(1,(available-DECK_GAP*(gridColumns-1))/gridColumns));
        int minLabel=prefs().getInt("minLabelSp",12),minimum=Math.max(52,(int)Math.ceil(minLabel*getResources().getDisplayMetrics().scaledDensity/getResources().getDisplayMetrics().density*1.4)+32);
        int cellHeight=horizontal?-1:dp(list?Math.max(58,minimum+18):fit?Math.max(minimum,(height-DECK_GAP*(Math.max(1,(tasks.size()+gridColumns-1)/gridColumns)-1))/Math.max(1,(tasks.size()+gridColumns-1)/gridColumns)):Math.max(104,Math.min(144,height/2-6)));
        boolean compact=getResources().getConfiguration().screenHeightDp<440;
        boolean tiny=fit&&cellHeight<dp(minimum+48),micro=fit&&cellHeight<dp(minimum+18);
        for(JSONObject task:tasks){
            LinearLayout card=new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setGravity(list?Gravity.CENTER_VERTICAL:Gravity.CENTER); card.setPadding(dp(horizontal?12:5),dp(horizontal?(compact?6:14):2),dp(horizontal?12:5),dp(horizontal?(compact?6:14):2)); card.setFocusableInTouchMode(controllerMode); card.setDefaultFocusHighlightEnabled(false);
            if(horizontal){ LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(width,-1); p.setMargins(0,0,cards.size()<tasks.size()-1?dp(DECK_GAP):0,0); cardRow.addView(card,p); }
            else { GridLayout.LayoutParams p=new GridLayout.LayoutParams(); p.width=width; p.height=cellHeight; p.setMargins(0,0,cards.size()%gridColumns<gridColumns-1?dp(DECK_GAP):0,cards.size()/gridColumns<(tasks.size()-1)/gridColumns?dp(DECK_GAP):0); appGrid.addView(card,p); }
            LinearLayout stack=new LinearLayout(this); stack.setOrientation(LinearLayout.VERTICAL); stack.setGravity(Gravity.CENTER); card.addView(stack,new LinearLayout.LayoutParams(-1,-2));
            if(isCurrent(task.optString("package"))){ TextView current=text(stack,"Current",9,false); current.setTag("currentBadge"); current.setTextColor(successColor()); GradientDrawable chip=rounded(theme.surface); chip.setStroke(dp(1),successColor()); current.setBackground(chip); current.setGravity(Gravity.CENTER); current.setPadding(dp(6),dp(1),dp(6),dp(1)); LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-2,dp(18)); cp.gravity=Gravity.CENTER_HORIZONTAL; cp.bottomMargin=dp(6); current.setLayoutParams(cp); current.setContentDescription("Current app: "+task.optString("label")); }
            LinearLayout body=new LinearLayout(this); body.setOrientation(list?LinearLayout.HORIZONTAL:LinearLayout.VERTICAL); body.setGravity(Gravity.CENTER); stack.addView(body,new LinearLayout.LayoutParams(-1,-2));
            ImageView icon=new ImageView(this); icon.setImageDrawable(appIcon(task.optString("package"))); icon.setContentDescription(task.optString("label")); int iconSize=horizontal?(compact?40:60):list?30:24; if(tiny) icon.setVisibility(View.GONE); body.addView(icon,new LinearLayout.LayoutParams(dp(iconSize),dp(iconSize)));
            LinearLayout labels=body; if(list){ labels=new LinearLayout(this); labels.setOrientation(LinearLayout.VERTICAL); LinearLayout.LayoutParams labelSize=new LinearLayout.LayoutParams(0,-2,1); labelSize.leftMargin=dp(10); body.addView(labels,labelSize); }
            TextView name=text(labels,task.optString("label")+(task.optInt("windowCount",1)>1?" · "+task.optInt("windowCount")+" windows":""),Math.max(minLabel,horizontal?(compact?16:18):micro?9:tiny?11:14),false); name.setGravity(list?Gravity.START:Gravity.CENTER); name.setMaxLines(1); name.setEllipsize(android.text.TextUtils.TruncateAt.END); name.setTypeface(null,Typeface.BOLD);
            long kb=task.optLong("pssKb",-1); String state=task.optString("memoryState","unavailable");
            String ram=kb>=0?"~"+MemoryUnits.pss(kb)+" MB app RAM"+(state.equals("shared")?" · shared":""):state.equals("loading")?"RAM loading…":state.equals("not-reported")?"No process in snapshot":"RAM unavailable";
            TextView usage=text(labels,ram,horizontal?12:10,true); usage.setMaxLines(1); usage.setEllipsize(android.text.TextUtils.TruncateAt.END); if(micro) usage.setVisibility(View.GONE); usage.setGravity(list?Gravity.START:Gravity.CENTER);
            boolean locked=locked(task);
            TextView badge=text(labels,task.optBoolean("launcher")?(locked?"Launcher · protected":launcherIdle(task)?"Launcher · not running":"Launcher · unlocked"):locked?"Protected":"X to close",12,true); badge.setTextColor(locked?theme.muted:theme.accent); badge.setGravity(Gravity.CENTER);
            if(!horizontal){ name.setPadding(0,0,0,0); usage.setPadding(0,0,0,0); badge.setPadding(0,0,0,0); badge.setVisibility(list||tiny||!locked||cellHeight<dp(90+Math.max(0,minLabel-14)*2)?View.GONE:View.VISIBLE); }

            if(horizontal&&compact){ name.setPadding(0,dp(5),0,dp(2)); usage.setPadding(0,dp(2),0,dp(2)); badge.setPadding(0,dp(2),0,dp(2)); }
            if(locked){ Drawable lock=getDrawable(R.drawable.ic_lock); if(lock!=null){ lock.setTint(theme.accent); lock.setBounds(0,0,dp(horizontal?18:12),dp(horizontal?18:12)); TextView target=badge.getVisibility()==View.GONE?name:badge; target.setCompoundDrawables(lock,null,null,null); target.setCompoundDrawablePadding(dp(5)); } }
            card.setContentDescription(task.optString("label")+(isCurrent(task.optString("package"))?", Current app":""));
            card.setOnClickListener(v->{ dockZone=false; selection.select(task.optInt("id")); toolZone=false; markSelection(); openSelected(); });
            card.setOnLongClickListener(v->{ dockZone=false; selection.select(task.optInt("id")); toolZone=false; markSelection(); protectSelected(); return true; });
            cards.add(card);
        }
        markSelection();
    }
    private void markSelection(){
        if(page!=Page.APPS) return;
        if(cards.isEmpty()&&!dockZone) toolZone=true;
        if(!tools.isEmpty()){ toolIndex=Math.max(0,Math.min(toolIndex,tools.size()-1)); for(int i=0;i<tools.size();i++) tools.get(i).setActivated(controllerMode&&toolZone&&i==toolIndex); if(controllerMode&&toolZone) tools.get(toolIndex).requestFocus(); }
        for(int i=0;i<cards.size();i++){
            boolean chosen=i==selection.index(); GradientDrawable bg=rounded(chosen?theme.selected:theme.surface);
            bg.setStroke(dp(chosen&&!toolZone&&!dockZone?3:1),chosen?theme.accent:isCurrent(tasks.get(i).optString("package"))?successColor():theme.raised); cards.get(i).setBackground(bg); cards.get(i).setSelected(chosen);
        }
        updateStatus();
        if(dockZone&&dock!=null){ if(controllerMode) dock.focusItem(dockIndex); return; } else if(dock!=null) dock.blur();
        if(!toolZone && !cards.isEmpty()){
            View card=cards.get(selection.index()); if(controllerMode) card.requestFocus();
            main.post(()->{ if(page==Page.APPS && rail!=null && card.getParent()==cardRow) rail.smoothScrollTo(Math.max(0,card.getLeft()-(rail.getWidth()-card.getWidth())/2),0); });
            if(appScroll!=null) main.post(()->{ if(page==Page.APPS&&card.isAttachedToWindow()) card.requestRectangleOnScreen(new android.graphics.Rect(0,0,card.getWidth(),card.getHeight()),false); });
        }
    }
    private boolean shortcutServiceEnabled(){
        android.view.accessibility.AccessibilityManager manager=getSystemService(android.view.accessibility.AccessibilityManager.class);
        if(!manager.isEnabled()) return false;
        for(android.accessibilityservice.AccessibilityServiceInfo info:manager.getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK)){
            if(info==null||info.getResolveInfo()==null||info.getResolveInfo().serviceInfo==null) continue;
            android.content.pm.ServiceInfo service=info.getResolveInfo().serviceInfo;
            if(getPackageName().equals(service.packageName) && HotkeyService.class.getName().equals(service.name)) return true;
        } return false;
    }
    private void setupPage(){
        setupStep=Math.max(0,Math.min(7,setupStep));
        formPage("Setup · "+(setupStep+1)+" / 8");
        String[] titles={"Connect Shizuku","Confirm button","Open-menu shortcut","Direct app-switch shortcut","Enable shortcuts","Appearance","Closing behavior","Notifications"};
        text(formInfo,titles[setupStep],20,false).setTypeface(null,Typeface.BOLD);
        if(setupStep==0){
            status=text(formMain,app.backend==null?app.status:"✓ Shizuku connected",14,false); note(formMain,app.backend==null?"Required · Start Shizuku to continue":"Required step complete"); button(formMain,app.backend==null?"Connect Shizuku":"Connection details",()->show(Page.CONNECT));
            note(formInfo,"Start Shizuku, then authorize TaskDeck. Without root, start it again after reboot. Pairing alone does not start its service.");
        } else if(setupStep==1){
            LinearLayout labels=row(formMain); for(String style:new String[]{"abxy","playstation"}){ Button choice=button(labels,style.equals("abxy")?"A/B/X/Y":"PlayStation",()->{ ControllerBindings.selectProfile(prefs(),style+(prefs().getBoolean("swapped",false)?"_swapped":"")); show(Page.SETUP); }); choice.setSelected(style.equals(prefs().getBoolean("psLabels",false)?"playstation":"abxy")); setupThemes.add(choice); }
            button(formMain,"Calibrate confirm button",()->show(Page.CONFIRM));
            note(formMain,prefs().getBoolean("confirmCalibrated",false)?"✓ Confirm calibrated":"Default confirm mapping");
            note(formInfo,"Press printed A or PlayStation Cross to correct common swapped button reports. D-pad and left stick move selection. This step is optional.");
        } else if(setupStep==2||setupStep==3){
            boolean direct=setupStep==3; String key=direct?"quickSwitchChord":"shortcutChord";
            button(formMain,direct?"Record direct switch":"Record deck shortcut",()->startRecording(direct));
            note(formMain,ShortcutKeys.decode(prefs().getString(key,"")).isEmpty()?"Not configured · optional":"Saved: "+ShortcutKeys.labels(ShortcutKeys.decode(prefs().getString(key,"")))+(shortcutServiceEnabled()?" · service enabled":" · service not enabled"));
            note(formInfo,direct?"An optional different combination switches between your last two apps without opening TaskDeck. You can skip this step.":"Hold your chosen buttons together, release, then save. This optional shortcut opens TaskDeck from another app.");
            note(formInfo,"D-pad, Confirm, Back and Start stay available for navigation and saving. Avoid combinations already used by games.");
        } else if(setupStep==4){
            button(formMain,"Enable shortcut service",()->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
            note(formMain,shortcutServiceEnabled()?"✓ Service enabled":"Service not enabled · optional");
            note(formInfo,"Enable TaskDeck's Accessibility service only if you recorded shortcuts. No screen contents or typed text are read. Otherwise, use the notification or Quick Settings tile.");
        } else if(setupStep==5){
            LinearLayout options=null; String[] themes={"light","dark","oled","auto"}; for(int i=0;i<themes.length;i++){ String value=themes[i]; if(i%2==0) options=row(formMain); Button option=button(options,value.equals("auto")?"Auto":value.equals("oled")?"OLED":value.equals("light")?"Light":"Dark",()->{ prefs().edit().putString("theme",value).apply(); show(Page.SETUP); }); option.setSelected(value.equals(prefs().getString("theme","dark"))); option.setPadding(dp(16),dp(8),dp(16),dp(8)); setupThemes.add(option); }

            note(formInfo,"Fit all is the default view. Cards, Grid and List remain available from View. All changes can be revisited in Settings.");
        } else if(setupStep==6){
            toggle(formMain,"Stay open after clearing",!prefs().getBoolean("exitAfterBulk",true),()->{ prefs().edit().putBoolean("exitAfterBulk",!prefs().getBoolean("exitAfterBulk",true)).apply(); show(Page.SETUP); });
            note(formInfo,"On keeps the menu open after clearing. Off returns HOME or to the app you kept. Protected apps are skipped.");
        } else {
            toggle(formMain,"RAM feedback",prefs().getBoolean("ramNotifications",true),()->{ prefs().edit().putBoolean("ramNotifications",!prefs().getBoolean("ramNotifications",true)).apply(); show(Page.SETUP); });
            toggle(formMain,"Notification shortcut",shortcutVisible(),()->{ if(shortcutVisible()) getSystemService(NotificationManager.class).cancel(1); else shortcutNotification(); show(Page.SETUP); });
            button(formMain,notificationsAllowed()?"Notification settings":"Allow notifications",()->{ if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) notificationPermission(); else startActivity(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,getPackageName())); });
            note(formInfo,!notificationsAllowed()?"Notifications: not allowed":!notificationsAllowed("ram_results")||!notificationsAllowed("ram")?"Notification access granted · feedback channel blocked":"Notifications: allowed");
            note(formInfo,"RAM feedback is a preference, not a permission grant. Toast feedback can work without notification access. Android controls banner visibility.");
        }
        LinearLayout nav=row(root); nav.setLayoutParams(new LinearLayout.LayoutParams(-1,-2));
        setupPrevious=button(nav,"Previous",()->{ setupStep--; controlIndex=-1; show(Page.SETUP); }); setupPrevious.setEnabled(setupStep>0); Drawable l1=new ControllerBadge("L1",theme.text); l1.setBounds(0,0,dp(32),dp(24)); setupPrevious.setCompoundDrawables(l1,null,null,null);
        View spacer=new View(this); nav.addView(spacer,new LinearLayout.LayoutParams(0,1,1));
        setupNext=button(nav,setupStep==7?"Finish setup":"Next step",()->{ controlIndex=-1; if(setupStep<7){ setupStep++; show(Page.SETUP); } else { prefs().edit().putBoolean("setupComplete",true).apply(); show(Page.COMPLETE); } });
        Drawable r1=new ControllerBadge("R1",theme.text); r1.setBounds(0,0,dp(32),dp(24)); setupNext.setCompoundDrawables(r1,null,null,null); ((LinearLayout.LayoutParams)setupNext.getLayoutParams()).rightMargin=0;
        if(setupStep>=1&&setupStep<=4){ boolean complete=setupStep==1?prefs().getBoolean("confirmCalibrated",false):setupStep==4?shortcutServiceEnabled():!ShortcutKeys.decode(prefs().getString(setupStep==2?"shortcutChord":"quickSwitchChord","")).isEmpty(); if(!complete) setupNext.setText("Skip for now"); }
        setupNext.setEnabled(setupStep>0||app.backend!=null);
        setupPrevious.setContentDescription("L1: Previous"); setupNext.setContentDescription("R1: "+setupNext.getText());
    }
    private boolean shortcutConflict(Set<String> keys){ return ShortcutKeys.conflicts(keys,ShortcutKeys.decode(prefs().getString(recordingQuickSwitch?"shortcutChord":"quickSwitchChord",""))); }
    private void startRecording(boolean direct){ recordingQuickSwitch=direct; show(Page.CHORD); }
    private TextView smallNote(LinearLayout parent,String value){ TextView text=note(parent,value); text.setTextSize(12); text.setMaxLines(2); text.setEllipsize(android.text.TextUtils.TruncateAt.END); return text; }
    private void shortcutsPage(){
        formPage("Controller shortcuts"); text(formInfo,"Two independent actions",20,false);
        button(formMain,"Record deck shortcut",()->startRecording(false));
        button(formMain,"Record direct switch",()->startRecording(true));
        for(boolean direct:new boolean[]{false,true}){
            String enabled=direct?"quickSwitchEnabled":"shortcutEnabled",chord=direct?"quickSwitchChord":"shortcutChord";
            toggle(formMain,direct?"Direct switch shortcut":"Open-menu shortcut",prefs().getBoolean(enabled,false),()->{
                if(ShortcutKeys.decode(prefs().getString(chord,"")).isEmpty()){ startRecording(direct); return; }
                prefs().edit().putBoolean(enabled,!prefs().getBoolean(enabled,false)).apply(); show(Page.SHORTCUTS);
            });
        }
        button(formMain,"Shortcut service settings",()->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        smallNote(formInfo,"Deck: "+ShortcutKeys.labels(ShortcutKeys.decode(prefs().getString("shortcutChord",""))));
        smallNote(formInfo,"Direct: "+ShortcutKeys.labels(ShortcutKeys.decode(prefs().getString("quickSwitchChord",""))));
        smallNote(formInfo,"Service: "+(shortcutServiceEnabled()?"enabled":"access required"));
        smallNote(formInfo,"Direct switches the last two distinct apps without opening this menu. Nested/identical chords are rejected to prevent accidental triggers.");
    }
    private void detailNavigation(int count,Page target){
        LinearLayout nav=row(root); nav.setLayoutParams(new LinearLayout.LayoutParams(-1,-2)); Button previous=pagingButton(nav,"page_previous","Previous page",()->{ detailsPage--; show(target); }); pagePrevious=previous; previous.setEnabled(detailsPage>0); View gap=new View(this); nav.addView(gap,new LinearLayout.LayoutParams(0,1,1));
        pageNext=pagingButton(nav,"page_next","Next page",()->{ detailsPage++; show(target); }); pageNext.setEnabled((detailsPage+1)*2<count);
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
        smallNote(formInfo,windows.size()+" reported windows. Choose one to open it. Use the Group app windows toggle in Settings to close individual windows.");
        for(int i=detailsPage*2;i<Math.min(windows.size(),detailsPage*2+2);i++){
            JSONObject window=windows.get(i); Button option=button(formMain,window.optString("windowLabel","Window "+window.optInt("id")),()->openWindow(window)); option.setMaxLines(1); option.setEllipsize(android.text.TextUtils.TruncateAt.END);
        }
        detailNavigation(windows.size(),Page.WINDOWS);
    }
    private void completionPage(){
        root.setGravity(Gravity.CENTER);
        SetupCheckView check=new SetupCheckView(this,theme.accent); root.addView(check,new LinearLayout.LayoutParams(dp(96),dp(96)));
        TextView done=text(root,"You're ready",22,false); done.setGravity(Gravity.CENTER); check.play();
        root.announceForAccessibility("Setup complete");
        main.postDelayed(()->{ if(page==Page.COMPLETE&&!isFinishing()&&!isDestroyed()) finishSetupAnimation(); },1900);
    }
    private void toggle(LinearLayout parent,String label,boolean enabled,Runnable action){
        Button option=button(parent,label+(enabled?" · On":" · Off"),action); option.setSelected(enabled); option.setContentDescription(label+", "+(enabled?"on":"off"));
        option.setCompoundDrawables(null,null,null,null);
        StateListDrawable states=new StateListDrawable();
        GradientDrawable onFocus=rounded(theme.accent); onFocus.setStroke(dp(3),theme.text);
        GradientDrawable offFocus=rounded(theme.raised); offFocus.setStroke(dp(3),theme.accent);
        GradientDrawable off=rounded(Color.TRANSPARENT); off.setStroke(dp(1),theme.muted);
        states.addState(new int[]{android.R.attr.state_selected,android.R.attr.state_activated},onFocus); states.addState(new int[]{android.R.attr.state_selected,android.R.attr.state_focused},onFocus);
        states.addState(new int[]{android.R.attr.state_selected},rounded(theme.accent)); states.addState(new int[]{android.R.attr.state_activated},offFocus); states.addState(new int[]{android.R.attr.state_focused},offFocus); states.addState(new int[]{},off);
        option.setBackground(new InsetDrawable(states,0,dp(6),0,dp(6))); int ink=Color.red(theme.accent)*.299+Color.green(theme.accent)*.587+Color.blue(theme.accent)*.114>150?Color.BLACK:Color.WHITE;
        option.setTextColor(enabled?ink:theme.text);
        option.setLayoutParams(new LinearLayout.LayoutParams(-1,dp(48)));
    }
    private void testFeedback(){
        if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){ prefs().edit().putBoolean("pendingRamFeedbackTest",true).apply(); notificationPermission(); }
        else { app.testMemoryFeedback(); if(page==Page.SETTINGS) show(Page.SETTINGS); }
    }
    private void settingsPage(){
        formPage("Settings"); String[] names={"Connection","Controller","Appearance","Dock","Notifications","About"};
        settingsCategory=Math.max(0,Math.min(names.length-1,settingsCategory));
        if(landscape()){ ((LinearLayout.LayoutParams)formMain.getLayoutParams()).weight=.7f; ((LinearLayout.LayoutParams)formInfo.getLayoutParams()).weight=1.7f; }
        for(int i=0;i<names.length;i++){ final int category=i; Button item=button(formMain,names[i],()->{ settingsCategory=category; controlIndex=-1; show(Page.SETTINGS); if(controllerMode&&controls.size()>1+names.length) focusControl(1+names.length); }); item.setSelected(i==settingsCategory); if(i==0&&app.backend!=null){ Drawable check=new ActionGlyph("check",successColor()); check.setBounds(0,0,dp(18),dp(18)); item.setCompoundDrawables(check,null,null,null); item.setContentDescription("Connection: connected"); } categories.add(item); item.setMinHeight(dp(44)); item.setMinimumHeight(dp(44)); item.setLayoutParams(new LinearLayout.LayoutParams(-1,dp(44))); }
        if(settingsCategory!=2&&settingsCategory!=5) text(formInfo,names[settingsCategory],18,false).setTypeface(null,Typeface.BOLD);
        if(settingsCategory==0){
            TextView connection=smallNote(formInfo,app.backend==null?"Shizuku disconnected":"✓ Shizuku connected"); if(app.backend!=null) connection.setTextColor(successColor());
            button(formInfo,app.backend==null?"Connect Shizuku":"Connection settings",()->show(Page.CONNECT));
            button(formInfo,"Last action",()->{ detailsPage=0; show(Page.RESULTS); });
            smallNote(formInfo,"Start Shizuku again after a device reboot. Connection is automatic when available.");
        } else if(settingsCategory==1){
            button(formInfo,"Button mapping",()->show(Page.MAPPING)); button(formInfo,"Configure shortcuts",()->show(Page.SHORTCUTS));
            smallNote(formInfo,"Service: "+(shortcutServiceEnabled()?"enabled":"access required"));
            smallNote(formInfo,"Choose letter or PlayStation labels, calibrate Confirm or remap individual deck actions.");
        } else if(settingsCategory==2){
            LinearLayout options=row(formInfo); for(String value:new String[]{"light","dark","oled","auto"}){ Button option=button(options,value.equals("auto")?"Auto":value.equals("oled")?"OLED":value.equals("light")?"Light":"Dark",()->{ prefs().edit().putString("theme",value).apply(); show(Page.SETTINGS); }); option.setSelected(value.equals(prefs().getString("theme","dark"))); option.setPadding(dp(10),0,dp(10),0); }
            AppearanceOptions appearanceOptions=new AppearanceOptions(this); formInfo.addView(appearanceOptions,new LinearLayout.LayoutParams(-1,-2));
            toggle(appearanceOptions,"Group app windows",prefs().getBoolean("groupApps",true),()->{ prefs().edit().putBoolean("groupApps",!prefs().getBoolean("groupApps",true)).apply(); rebuildTasks(); show(Page.SETTINGS); });
            toggle(appearanceOptions,"Stay open after clearing",!prefs().getBoolean("exitAfterBulk",true),()->{ prefs().edit().putBoolean("exitAfterBulk",!prefs().getBoolean("exitAfterBulk",true)).apply(); show(Page.SETTINGS); });
            accentColors(formInfo);

        } else if(settingsCategory==3){
            button(formInfo,"Add apps to Quick Access",this::showPicker); button(formInfo,"Dock style & size",()->show(Page.DOCKSTYLE)); button(formInfo,"Dock behavior",()->show(Page.DOCKBEHAVIOR));
            smallNote(formInfo,QuickAccess.read(prefs()).size()+" favorites · hold and drag dock icons to reorder, or use Select.");
        } else if(settingsCategory==4){
            toggle(formInfo,"RAM feedback",prefs().getBoolean("ramNotifications",true),()->{ boolean enabled=!prefs().getBoolean("ramNotifications",true); prefs().edit().putBoolean("ramNotifications",enabled).apply(); if(!enabled) getSystemService(NotificationManager.class).cancel(2); show(Page.SETTINGS); });
            toggle(formInfo,"Notification shortcut",shortcutVisible(),()->{ if(shortcutVisible()){ getSystemService(NotificationManager.class).cancel(1); prefs().edit().putBoolean("pendingNotificationShortcut",false).apply(); } else shortcutNotification(); show(Page.SETTINGS); });
            LinearLayout actions=row(formInfo); button(actions,"Test feedback",this::testFeedback); button(actions,"Permissions",()->startActivity(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,getPackageName())));
            smallNote(formInfo,"Notifications "+(notificationsAllowed("ram_results")?"allowed":"blocked")+". Android controls banner visibility.");
        } else {
            smallNote(formInfo,"TaskDeck "+versionName()+" · by SilasDV"); smallNote(formInfo,"Free and open source · MIT license"); smallNote(formInfo,"Vibecoded with OpenAI Codex, guided by SilasDV.");
            button(formInfo,"Support SilasDV on Ko-fi",()->openLink("https://ko-fi.com/silasdv")); button(formInfo,"GitHub project",()->openLink("https://github.com/silasdv/TaskDeck"));
        }
    }
    private String profileName(){ String profile=ControllerBindings.profile(prefs()); return (profile.startsWith("playstation")?"PlayStation":"A/B/X/Y")+(profile.endsWith("_swapped")?" (swapped)":""); }
    private void mappingPage(){
        formPage("Button mapping");
        for(int i=0;i<ControllerBindings.ACTIONS.length;i++){
            String action=ControllerBindings.ACTIONS[i],name=ControllerBindings.NAMES[i];
            Button option=compactButton(button(formMain,name,()->{ bindingAction=action; show(Page.BIND); })); String printed=ControllerBindings.badge(prefs(),action);
            Drawable badge=new ControllerBadge(printed,theme.text); badge.setBounds(0,0,dp(printed.length()>1&&!ControllerBadge.symbol(printed)?30:22),dp(22)); option.setCompoundDrawables(badge,null,null,null); option.setContentDescription(name+": "+printed); option.setLayoutParams(new LinearLayout.LayoutParams(-1,dp(40)));
        }
        Button profile=button(formInfo,"Labels: "+profileName(),this::profileMenu); Drawable chevron=new ActionGlyph("next",theme.text); chevron.setBounds(0,0,dp(16),dp(16)); profile.setCompoundDrawables(null,null,chevron,null);
        button(formInfo,"Calibrate confirm button",()->show(Page.CONFIRM));
        button(formInfo,"Page navigation",()->show(Page.PAGING));
        button(formInfo,"Reset mapping",()->{ ControllerBindings.reset(prefs()); show(Page.MAPPING); });
        smallNote(formInfo,"Choose labels, calibrate Confirm or remap actions and page navigation. Setup keeps L1/R1.");
    }
    private void bindPage(){
        formPage("Assign controller button"); int index=java.util.Arrays.asList(ControllerBindings.ACTIONS).indexOf(bindingAction);
        text(formMain,ControllerBindings.paging(bindingAction)?(bindingAction.equals("page_previous")?"Previous page":"Next page"):ControllerBindings.NAMES[Math.max(0,index)],18,false); note(formMain,"Current: "+ControllerBindings.badge(prefs(),bindingAction));
        note(formInfo,"Press a controller button. Assigning an occupied button swaps its previous action. D-pad, Android Back and the on-screen Back button remain available.");
    }
    private Button pagingButton(LinearLayout parent,String action,String label,Runnable callback){ Button b=button(parent,label,callback); if(action.equals("page_next")) ((LinearLayout.LayoutParams)b.getLayoutParams()).rightMargin=0; String hint=ControllerBindings.badge(prefs(),action); Drawable badge=new ControllerBadge(hint,theme.text); badge.setBounds(0,0,dp(hint.length()>1&&!ControllerBadge.symbol(hint)?28:20),dp(20)); b.setCompoundDrawables(badge,null,null,null); b.setContentDescription(label+": "+hint); return b; }
    private void pagingMappingPage(){ formPage("Page navigation"); pagingButton(formMain,"page_previous","Previous page",()->{ bindingAction="page_previous"; show(Page.BIND); }); pagingButton(formMain,"page_next","Next page",()->{ bindingAction="page_next"; show(Page.BIND); }); note(formInfo,"Applies to app selection, Last action, RAM details and window pages. Main-screen actions are independent. Confirm and Back stay reserved."); }
    private void accentColors(LinearLayout parent){
        colorColumns=8; LinearLayout heading=row(parent); TextView title=text(heading,"Accent Colors",12,false); title.setPadding(0,dp(2),0,0); title.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1)); Button reset=button(heading,"Reset",()->{ prefs().edit().remove("accentColor").apply(); show(Page.SETTINGS); }); reset.setTextSize(11); reset.setMinHeight(dp(24)); reset.setMinimumHeight(dp(24)); reset.setPadding(dp(8),0,dp(8),0); reset.setCompoundDrawables(null,null,null,null); reset.setLayoutParams(new LinearLayout.LayoutParams(-2,dp(24))); reset.setContentDescription("Restore theme accent");
        AccentPalette palette=new AccentPalette(this); parent.addView(palette,new LinearLayout.LayoutParams(-1,landscape()?0:-2,landscape()?1:0));
        for(int i=0;i<DeckTheme.ACCENT_KEYS.length;i++){ String key=DeckTheme.ACCENT_KEYS[i],name=DeckTheme.ACCENT_NAMES[i]; Button dot=button(parent,"",()->{ prefs().edit().putString("accentColor",key).apply(); show(Page.SETTINGS); }); parent.removeView(dot); dot.setTag("accent:"+key); dot.setContentDescription(name+" accent"); dot.setTooltipText(name); dot.setPadding(0,0,0,0); dot.setMinHeight(0); dot.setMinimumHeight(0); dot.setCompoundDrawables(null,null,null,null); int color=key.equals("theme")?DeckTheme.resolve(prefs().getString("theme","dark"),getResources().getConfiguration().uiMode).accent:DeckTheme.accentColor(key); dot.setBackground(new AccentDot(color,theme.text,getResources().getDisplayMetrics().density)); dot.setSelected(key.equals(prefs().getString("accentColor","theme"))); colorDots.add(dot); GridLayout.LayoutParams size=new GridLayout.LayoutParams(GridLayout.spec(i/8),GridLayout.spec(i%8)); size.width=dp(48); size.height=dp(48); palette.addView(dot,size); }
    }
    private void boundMenuButtons(LinearLayout pane){
        if(pane==null||!inside(pane,root)||page==Page.APPS||page==Page.PICKER||page==Page.SETTINGS&&pane==formMain) return;
        Runnable fit=()->{ if(pane.getWidth()<=0) return; List<Button> group=new ArrayList<>(); int width=0,padding=dp(16); for(int i=0;i<pane.getChildCount();i++) if(pane.getChildAt(i) instanceof Button){ Button button=(Button)pane.getChildAt(i); width=Math.max(width,ReadableButtons.naturalWidth(button,padding)); group.add(button); } width=Math.min(width,Math.max(1,pane.getWidth()-pane.getPaddingLeft()-pane.getPaddingRight())); for(Button button:group) ReadableButtons.fit(button,width,padding,page==Page.MAPPING?dp(40):dp(48)); if(page==Page.MAPPING&&pane==formMain&&pane.getHeight()>0){ int needed=0; for(Button button:group){ LinearLayout.LayoutParams size=(LinearLayout.LayoutParams)button.getLayoutParams(); needed+=size.height+size.topMargin+size.bottomMargin; } int vertical=Math.min(dp(7),Math.max(0,(pane.getHeight()-needed)/2)); if(pane.getPaddingTop()!=vertical) pane.setPadding(pane.getPaddingLeft(),vertical,pane.getPaddingRight(),vertical); } };

        pane.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{ if(r-l!=or-ol) fit.run(); }); pane.post(fit);
    }
    private void profileMenu(){
        String[] labels={"A/B/X/Y","A/B/X/Y (swapped)","PlayStation symbols","PlayStation symbols (swapped)"},values={"abxy","abxy_swapped","playstation","playstation_swapped"};
        int selected=java.util.Arrays.asList(values).indexOf(ControllerBindings.profile(prefs()));
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Button labels").setSingleChoiceItems(labels,Math.max(0,selected),(d,which)->{ ControllerBindings.selectProfile(prefs(),values[which]); d.dismiss(); show(Page.MAPPING); }).setNegativeButton("Cancel",null).create();
        dialog.setOnKeyListener((d,key,event)->{
            if(key==ControllerBindings.code(prefs(),"exit")||key==KeyEvent.KEYCODE_BACK){ if(event.getAction()==0) d.dismiss(); return true; }
            if(key==ControllerBindings.code(prefs(),"open")||key==KeyEvent.KEYCODE_DPAD_CENTER){ if(event.getAction()==0&&event.getRepeatCount()==0){ int position=dialog.getListView().getSelectedItemPosition(); if(position<0) position=dialog.getListView().getCheckedItemPosition(); if(position>=0){ ControllerBindings.selectProfile(prefs(),values[position]); d.dismiss(); show(Page.MAPPING); } } return true; } return false;
        });
        dialog.setOnShowListener(ignored->{
            int[] axis={0}; dialog.getListView().setOnGenericMotionListener((v,event)->{ if(!event.isFromSource(InputDevice.SOURCE_JOYSTICK)) return false; setInputMode(true); float y=event.getAxisValue(MotionEvent.AXIS_HAT_Y); if(Math.abs(y)<.55f) y=event.getAxisValue(MotionEvent.AXIS_Y); int next=y>.55f?1:y<-.55f?-1:0;
                if(next!=0&&next!=axis[0]){ int current=dialog.getListView().getSelectedItemPosition(); if(current<0) current=dialog.getListView().getCheckedItemPosition(); dialog.getListView().setSelection(Math.max(0,Math.min(values.length-1,current+next))); } axis[0]=next; return true; });
        }); dialog.show();
    }
    private int successColor(){ return theme.currentColor(); }
    private String versionName(){ try { return getPackageManager().getPackageInfo(getPackageName(),0).versionName; } catch(Exception absent){ return "Unknown"; } }
    private void openLink(String url){ try { startActivity(new Intent(Intent.ACTION_VIEW,android.net.Uri.parse(url))); } catch(ActivityNotFoundException unavailable){ feedback="No browser available"; updateStatus(); } }
    private void connectPage(){
        formPage("Shizuku connection"); status=text(formMain,app.status,15,false);
        button(formMain,"Open Shizuku",()->{ Intent intent=getPackageManager().getLaunchIntentForPackage("moe.shizuku.privileged.api"); if(intent!=null) startActivity(intent); else { feedback="Install Shizuku from shizuku.rikka.app"; updateStatus(); } });
        button(formMain,app.backend==null?"Authorize / Connect":"Reconnect Shizuku",()->{ if(app.backend!=null) app.recoverBackend("Manual reconnect"); else app.connect(true); });
        button(formMain,prefs().getBoolean("setupComplete",false)?"Done":"Continue setup",()->{ show(prefs().getBoolean("setupComplete",false)?Page.SETTINGS:Page.SETUP); if(app.backend!=null) refresh(); });
        text(formInfo,"Connect once",20,false); note(formInfo,"1. Start Shizuku.\n2. Return and choose Authorize / Connect.\n3. Approve TaskDeck access.\n\nWithout root, restart Shizuku after reboot. TaskDeck uses it to access Android tasks.");
    }
    private void recordPage(){
        heldKeys.clear(); capturedKeys.clear(); captureDevice=null; formPage(page==Page.CHORD?(recordingQuickSwitch?"Record direct switch":"Record deck shortcut"):"Calibrate confirm");
        recordLive=text(formMain,"Held now: none",16,false); recordSaved=text(formMain,"Recorded: none",16,false);
        recordLive.setMaxLines(2); recordSaved.setMaxLines(3); recordLive.setTextSize(14); recordSaved.setTextSize(14); recordLive.setEllipsize(android.text.TextUtils.TruncateAt.END); recordSaved.setEllipsize(android.text.TextUtils.TruncateAt.END);
        text(formInfo,page==Page.CHORD?"Hold, release, save":prefs().getBoolean("psLabels",false)?"Press Cross":"Press printed A",20,false);
        note(formInfo,page==Page.CHORD?"Hold up to four buttons together, then release. Confirm or Start saves. D-pad, Confirm, Back and Start remain reserved for navigation.":"Press printed A or PlayStation Cross, matching your chosen labels. This calibrates the common face-button pair swap.");
        if(page==Page.CHORD){
            saveChord=button(formMain,"Save combination · Start",()->{ if(capturedKeys.isEmpty()||!heldKeys.isEmpty()||capturedKeys.size()>4) return; if(shortcutConflict(capturedKeys)){ recordSaved.setText("Conflicts with the other shortcut"); return; } prefs().edit().putString(recordingQuickSwitch?"quickSwitchChord":"shortcutChord",ShortcutKeys.encode(capturedKeys)).putBoolean(recordingQuickSwitch?"quickSwitchEnabled":"shortcutEnabled",true).apply(); show(prefs().getBoolean("setupComplete",false)?Page.SHORTCUTS:Page.SETUP); }); saveChord.setEnabled(false);
            button(formMain,"Record again",()->show(Page.CHORD));
            note(formInfo,"Choose an unused combination. Partial presses can reach your game. If both rear buttons report identical key and scan codes, assign different outputs in Retroid settings.");
        }
    }

    private void record(KeyEvent event){
        String key=ShortcutKeys.id(event);
        if(page==Page.CONFIRM){
            if(event.getAction()==KeyEvent.ACTION_DOWN && event.getRepeatCount()==0 && (event.getKeyCode()==96 || event.getKeyCode()==97)){
                ControllerBindings.calibrate(prefs(),event.getKeyCode()); feedback="Confirm button calibrated"; show(prefs().getBoolean("setupComplete",false)?Page.MAPPING:Page.SETUP);
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
    private String recoveryMessage(String message){ String lower=message.toLowerCase(Locale.ROOT); if(lower.contains("deadobject")||lower.contains("timed out")||lower.contains("timeout")) return "Connection interrupted. Reconnect in Settings → Connection."; if(lower.contains("securityexception")) return "Android denied access. Check Shizuku access in Settings → Connection."; if(lower.contains("activitynotfoundexception")||lower.contains("namenotfoundexception")) return "App unavailable. Refresh the list or remove its shortcut."; return message; }
    private void updateStatus(){
        if(status==null) return;
        if(!feedback.isEmpty()){ status.setText(recoveryMessage(feedback)); status.setVisibility(View.VISIBLE); }
        else if(page==Page.APPS){ status.setText(""); status.setVisibility(View.GONE); }
        else status.setText(app.status);
    }
    private long available(){ ActivityManager.MemoryInfo info=new ActivityManager.MemoryInfo(); getSystemService(ActivityManager.class).getMemoryInfo(info); return info.availMem; }
    private String viewMode(){ String mode=prefs().getString("view","fit"); return Set.of("fit","grid","list","cards").contains(mode)?mode:"fit"; }
    private void updateMemory(){
        if(memory==null) return;
        ActivityManager.MemoryInfo info=new ActivityManager.MemoryInfo(); getSystemService(ActivityManager.class).getMemoryInfo(info);
        if(compactMemory!=null) compactMemory.setText("Used "+MemoryUnits.bytes(info.totalMem-info.availMem)+" / "+MemoryUnits.bytes(info.totalMem)+" MB");
        memory.setText("System RAM\n"+MemoryUnits.bytes(info.totalMem-info.availMem)+" / "+MemoryUnits.bytes(info.totalMem)+" MB used"); updateBackground();
    }
    private int chartLabelHeight(){ float scale=getResources().getDisplayMetrics().scaledDensity/getResources().getDisplayMetrics().density; return Math.max(22,(int)Math.ceil(16*scale)+4); }
    private void chart(LinearLayout parent,String label,long value,long maximum){ chart(parent,label,value,maximum,true); }
    private void chart(LinearLayout parent,String label,long value,long maximum,boolean showLabel){
        TextView name=text(parent,label,12,true); name.setPadding(0,dp(1),0,dp(1)); name.setSingleLine(true); name.setEllipsize(android.text.TextUtils.TruncateAt.END); name.setIncludeFontPadding(false); name.setLayoutParams(new LinearLayout.LayoutParams(-1,dp(chartLabelHeight()))); name.setContentDescription(label); name.setVisibility(showLabel?View.VISIBLE:View.GONE);
        ProgressBar bar=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal); bar.setMax(1000); bar.setProgress(maximum>0?(int)Math.min(1000,Math.max(0,value)*1000d/maximum):0); bar.setProgressTintList(android.content.res.ColorStateList.valueOf(theme.accent)); bar.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(theme.raised)); bar.setContentDescription(label); bar.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        LinearLayout.LayoutParams size=new LinearLayout.LayoutParams(-1,dp(5)); size.bottomMargin=dp(3); parent.addView(bar,size);
    }
    private List<JSONObject> ramEntries(){
        Map<String,JSONObject> entries=new LinkedHashMap<>(); for(JSONObject task:rawTasks) entries.put(task.optString("package"),task);
        for(JSONObject task:backgroundApps) entries.put(task.optString("package",task.optString("label")),task);
        List<JSONObject> result=new ArrayList<>(entries.values()); result.sort((a,b)->Long.compare(b.optLong("pssKb",-1),a.optLong("pssKb",-1))); return result;
    }
    private void updateBackground(){
        if(backgroundInfo==null||memoryBars==null) return;
        List<JSONObject> entries=ramEntries(); memoryBars.removeAllViews(); ActivityManager.MemoryInfo info=new ActivityManager.MemoryInfo(); getSystemService(ActivityManager.class).getMemoryInfo(info);
        if(systemRamChart!=null){ systemRamChart.removeAllViews(); chart(systemRamChart,"Used RAM",info.totalMem-info.availMem,info.totalMem,false); }
        TextView title=text(memoryBars,"App RAM · approximate",11,false); title.setPadding(0,dp(3),0,dp(1));
        int height=memoryBars.getHeight()>0?Math.round(memoryBars.getHeight()/getResources().getDisplayMetrics().density):100;
        height-=Math.round((memoryBars.getPaddingTop()+memoryBars.getPaddingBottom())/getResources().getDisplayMetrics().density); int limit=Math.max(0,(height-24)/(chartLabelHeight()+8)); long max=info.totalMem/1024;
        for(int i=0;i<Math.min(entries.size(),limit);i++){ JSONObject task=entries.get(i); long kb=task.optLong("pssKb",-1); chart(memoryBars,task.optString("label")+" · "+(kb>=0?MemoryUnits.pss(kb)+" MB":"unavailable"),kb,max); }
        if(entries.isEmpty()&&height>48) smallNote(memoryBars,"No app sample yet");
        StringBuilder value=new StringBuilder("System & cache also use RAM.\nCan't close here");
        for(JSONObject item:backgroundApps) value.append("\n").append(item.optString("label")).append(item.optLong("pssKb",-1)>=0?" · ~"+MemoryUnits.pss(item.optLong("pssKb"))+" MB":" · n/a");
        backgroundInfo.setText(value.toString());
    }
    private void memoryPage(){
        formPage("RAM usage"); ActivityManager.MemoryInfo info=new ActivityManager.MemoryInfo(); getSystemService(ActivityManager.class).getMemoryInfo(info);
        text(formMain,"System RAM",18,false); chart(formMain,"Used "+MemoryUnits.bytes(info.totalMem-info.availMem)+" MB",info.totalMem-info.availMem,info.totalMem);
        smallNote(formMain,"Total "+MemoryUnits.bytes(info.totalMem)+" MB");
        smallNote(formMain,"RAM includes Android, services and cache.");
        smallNote(formMain,"All bars scale to total RAM. App PSS estimates do not add up to system usage.");
        smallNote(formMain,"1 MB = 1,000,000 bytes. Background-only apps cannot be closed here.");
        List<JSONObject> entries=ramEntries(); int size=Math.max(1,(getResources().getConfiguration().screenHeightDp-190)/(chartLabelHeight()+8)),pages=Math.max(1,(entries.size()+size-1)/size); ramPage=Math.max(0,Math.min(ramPage,pages-1));
        text(formInfo,"App RAM · "+(ramPage+1)+" / "+pages,18,false); long max=info.totalMem/1024;
        for(int i=ramPage*size;i<Math.min(entries.size(),(ramPage+1)*size);i++){ JSONObject task=entries.get(i); long kb=task.optLong("pssKb",-1); chart(formInfo,task.optString("label")+" · "+(kb>=0?MemoryUnits.pss(kb)+" MB":"unavailable"),kb,max); }
        if(entries.isEmpty()) smallNote(formInfo,"No app sample available");
        LinearLayout nav=row(root); pagePrevious=pagingButton(nav,"page_previous","Previous page",()->{ ramPage--; show(Page.MEMORY); }); pagePrevious.setEnabled(ramPage>0); View spacer=new View(this); nav.addView(spacer,new LinearLayout.LayoutParams(0,1,1)); pageNext=pagingButton(nav,"page_next","Next page",()->{ ramPage++; show(Page.MEMORY); }); pageNext.setEnabled(ramPage+1<pages);
    }
    private void rebuildTasks(){
        List<JSONObject> visible=TaskOrder.arrange(rawTasks,"recent",this::locked);
        tasks.clear(); tasks.addAll(TaskOrder.arrange(TaskGroups.group(visible,prefs().getBoolean("groupApps",true)),prefs().getString("sort","recent"),this::locked));
        List<Integer> ids=new ArrayList<>(); for(JSONObject task:tasks) ids.add(task.optInt("id")); selection.update(ids);
        if(resetSelection){ selection.first(); toolZone=tasks.isEmpty(); toolIndex=0; resetSelection=false; }
        if(page==Page.APPS){ renderCards(); renderDock(); } updateStatus(); updateOperationInfo();
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
            rawTasks.clear(); backgroundApps.clear(); JSONArray rows=result.optJSONArray("tasks");
            if(rows!=null) for(int i=0;i<rows.length();i++){ JSONObject task=rows.optJSONObject(i); if(task!=null){ try { task.put("recentRank",i); } catch(JSONException ignored){} rawTasks.add(task); } }
            if(captureEntry){ entryTask=result.optJSONObject("previousTask"); captureEntry=false; }
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
                } rebuildTasks(); if(page==Page.MEMORY) show(Page.MEMORY);
            });
            if(pendingRefresh){ pendingRefresh=false; refresh(); }
        });
    }

    private JSONObject selected(){ Integer id=selection.id(); if(id!=null) for(JSONObject task:tasks) if(task.optInt("id")==id) return task; return null; }
    private void openSelected(){
        if(dockZone&&dock!=null){ dock.openSelected(); return; }
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
    private void closeSelected(){ if(dockZone){ feedback="Select an open app card to close it"; updateStatus(); return; } JSONObject task=selected(); if(task==null) return; if(!closable(task)){ feedback=locked(task)?"This app is protected":"Launcher is not running · Select to restart"; updateStatus(); return; } remove(TaskGroups.expand(task),0); }
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
                setInputMode(true); int key=event.getKeyCode(),confirm=ControllerBindings.code(prefs(),"open"),back=ControllerBindings.code(prefs(),"exit");
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
            }); options.add(option); option.setSelected(mode.equals(prefs().getString("view","fit"))); if(option.isSelected()){ first=option; cursor[0]=options.size()-1; }
        }

        for(Button option:options){ option.setMinHeight(dp(48)); option.setMinimumHeight(dp(48)); option.setPadding(dp(10),dp(3),dp(10),dp(3)); LinearLayout.LayoutParams size=(LinearLayout.LayoutParams)option.getLayoutParams(); size.topMargin=dp(2); size.bottomMargin=dp(2); }
        while(controls.size()>previous) controls.remove(controls.size()-1);
        GradientDrawable outline=rounded(theme.surface); outline.setStroke(dp(2),theme.muted); panel.setBackground(outline);
        protectionPopup=new PopupWindow(panel,Math.min(dp(340),getResources().getDisplayMetrics().widthPixels-dp(32)),-2,true);
        protectionPopup.setBackgroundDrawable(rounded(theme.surface)); protectionPopup.setOutsideTouchable(true); protectionPopup.setElevation(dp(16)); protectionPopup.showAtLocation(root,Gravity.CENTER,0,0);
        if(controllerMode&&first!=null){ first.requestFocus(); first.setActivated(true); }
    }
    private void sortMenu(){
        List<Button> options=new ArrayList<>(); int[] cursor={0},axis={0};
        LinearLayout panel=new LinearLayout(this){
            private void move(int delta){ cursor[0]=Math.max(0,Math.min(options.size()-1,cursor[0]+delta)); for(int i=0;i<options.size();i++) options.get(i).setActivated(i==cursor[0]); options.get(cursor[0]).requestFocus(); }
            @Override public boolean dispatchKeyEvent(KeyEvent event){
                setInputMode(true); int key=event.getKeyCode(),confirm=ControllerBindings.code(prefs(),"open"),back=ControllerBindings.code(prefs(),"exit");
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
        GradientDrawable outline=rounded(theme.surface); outline.setStroke(dp(2),theme.muted); panel.setBackground(outline);
        protectionPopup=new PopupWindow(panel,Math.min(dp(340),getResources().getDisplayMetrics().widthPixels-dp(32)),-2,true);
        protectionPopup.setBackgroundDrawable(rounded(theme.surface)); protectionPopup.setOutsideTouchable(true); protectionPopup.setElevation(dp(16)); protectionPopup.showAtLocation(root,Gravity.CENTER,0,0);
        if(controllerMode&&first!=null){ first.requestFocus(); first.setActivated(true); }
    }
    private void reorder(){ rebuildTasks(); }

    private void protectSelected(){
        if(dockZone&&dock!=null){ dock.selectedOptions(); return; }
        JSONObject task=selected(); if(task==null) return; boolean launcher=task.optBoolean("launcher"),mandatory=TaskOrder.mandatory(task);
        List<Button> options=new ArrayList<>(); int[] cursor={0},axis={0};
        LinearLayout panel=new LinearLayout(this){
            private void move(int delta){ cursor[0]=Math.max(0,Math.min(options.size()-1,cursor[0]+delta)); for(int i=0;i<options.size();i++) options.get(i).setActivated(i==cursor[0]); options.get(cursor[0]).requestFocus(); }
            @Override public boolean dispatchKeyEvent(KeyEvent event){
                setInputMode(true); int key=event.getKeyCode(),a=ControllerBindings.code(prefs(),"open"),b=ControllerBindings.code(prefs(),"exit");
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
        options.add(button(panel,QuickAccess.read(prefs()).contains(task.optString("package"))?"Remove from Quick Access":"Pin to Quick Access",()->{ protectionPopup.dismiss(); pin(task.optString("package")); }));
        while(controls.size()>previous) controls.remove(controls.size()-1);
        GradientDrawable outline=rounded(theme.surface); outline.setStroke(dp(2),theme.muted); panel.setBackground(outline);
        scrollPopup(panel);
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
        operationInfo.setVisibility(problems?View.VISIBLE:View.GONE); if(problems){ operationInfo.setText(report.optString("summary")+" · Settings → Connection → Last action"); operationInfo.setTextColor(theme.muted); }
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
        if(page==Page.SETUP||page==Page.SETTINGS) show(page); feedback=granted?"Notification permission granted":"Notification permission denied"; updateStatus();
    }
    private void back(){
        if(page==Page.SETTINGS&&controllerMode&&controlIndex>=0&&controlIndex<controls.size()&&inside(controls.get(controlIndex),formInfo)){ focusControl(1+settingsCategory); return; }
        if(page==Page.APPS) finish();
        else if(page==Page.RESULTS) show(Page.SETTINGS);
        else if(page==Page.SETTINGS||page==Page.WINDOWS||page==Page.MEMORY||page==Page.COMPLETE) show(Page.APPS);
        else if(page==Page.PICKER) show(pickerReturn);
        else if(page==Page.BIND) show(ControllerBindings.paging(bindingAction)?Page.PAGING:Page.MAPPING);
        else if(page==Page.PAGING) show(Page.MAPPING);
        else if(page==Page.ACCENT){ settingsCategory=2; show(Page.SETTINGS); }
        else if(page==Page.CHORD) show(prefs().getBoolean("setupComplete",false)?Page.SHORTCUTS:Page.SETUP);
        else if(page==Page.SETUP){ if(setupStep>0){ setupStep--; show(Page.SETUP); } else finish(); }
        else show(prefs().getBoolean("setupComplete",false)?Page.SETTINGS:Page.SETUP);
    }
    private void focusMode(View view,boolean controller){
        if(view instanceof Button||cards.contains(view)||dock!=null&&dock.items.contains(view)){ view.setFocusableInTouchMode(controller); if(!controller) view.setActivated(false); }
        if(view instanceof ViewGroup) for(int i=0;i<((ViewGroup)view).getChildCount();i++) focusMode(((ViewGroup)view).getChildAt(i),controller);
    }
    private void setInputMode(boolean controller){
        if(controllerMode==controller) return; boolean searching=page==Page.PICKER&&pickerSearch!=null&&pickerSearch.hasFocus(); controllerMode=controller;
        if(root!=null){ focusMode(root,controller); if(!controller){ stopStick(); for(Button button:controls) button.setActivated(false); View focus=getCurrentFocus(); if(focus!=null) focus.clearFocus(); root.requestFocus(); } }
        if(protectionPopup!=null&&protectionPopup.isShowing()){ View panel=protectionPopup.getContentView(); focusMode(panel,controller); if(!controller) panel.clearFocus(); }
        if(controller&&searching){ pickerSearch.requestFocus(); return; }
        if(controller && protectionPopup!=null&&protectionPopup.isShowing()) focusFirstButton(protectionPopup.getContentView());
        else if(controller && page==Page.APPS) markSelection();
        else if(controller && !(getCurrentFocus() instanceof Button) && !(getCurrentFocus() instanceof EditText)) focusControl(controlIndex<0?0:controlIndex);
    }
    private boolean focusFirstButton(View view){ if(view instanceof Button&&view.isEnabled()){ view.requestFocus(); return true; } if(view instanceof ViewGroup) for(int i=0;i<((ViewGroup)view).getChildCount();i++) if(focusFirstButton(((ViewGroup)view).getChildAt(i))) return true; return false; }
    @Override public boolean dispatchTouchEvent(MotionEvent event){
        if(event.getActionMasked()==MotionEvent.ACTION_DOWN) setInputMode(false);
        boolean outside=false; QuickAccessDock before=dock;
        if(event.getActionMasked()==MotionEvent.ACTION_UP&&before!=null&&before.isExpanded()&&"focus".equals(prefs().getString("dockBehavior","always"))){ android.graphics.Rect bounds=new android.graphics.Rect(); before.getGlobalVisibleRect(bounds); outside=!bounds.contains((int)event.getRawX(),(int)event.getRawY()); }
        boolean handled=super.dispatchTouchEvent(event); if(outside&&dock==before){ dockZone=false; before.expand(false); } return handled;
    }

    @Override public void onBackPressed(){ back(); }
    @Override public boolean dispatchKeyEvent(KeyEvent event){
        int key=event.getKeyCode();
        if(ShortcutKeys.eligible(event)||key>=19&&key<=23) setInputMode(true);
        int a=ControllerBindings.code(prefs(),"open"),b=ControllerBindings.code(prefs(),"exit"),x=ControllerBindings.code(prefs(),"close"),y=ControllerBindings.code(prefs(),"clear");
        if(page==Page.COMPLETE&&(key==a||key==KeyEvent.KEYCODE_DPAD_CENTER)){ if(event.getAction()==0&&event.getRepeatCount()==0) finishSetupAnimation(); return true; }
        if(page==Page.BIND&&ControllerBindings.paging(bindingAction)&&(key==a||key==b)){ if(event.getAction()==0&&event.getRepeatCount()==0) show(Page.PAGING); return true; }
        if(page==Page.BIND&&ControllerBindings.BUTTONS.contains(key)){ if(event.getAction()==0&&event.getRepeatCount()==0){ if(ControllerBindings.paging(bindingAction)&&(key==a||key==b)){ feedback="Confirm and Back remain reserved"; updateStatus(); return true; } ControllerBindings.assign(prefs(),bindingAction,key); show(ControllerBindings.paging(bindingAction)?Page.PAGING:Page.MAPPING); } return true; }
        if(page==Page.CHORD && key==KeyEvent.KEYCODE_BUTTON_START && heldKeys.isEmpty() && !capturedKeys.isEmpty()){
            if(event.getAction()==KeyEvent.ACTION_DOWN && event.getRepeatCount()==0 && saveChord.isEnabled()) saveChord.performClick(); return true;
        }
        boolean reserved=key>=19&&key<=23 || key==a || key==b || key==KeyEvent.KEYCODE_BACK || key==KeyEvent.KEYCODE_BUTTON_START;
        if((page==Page.CONFIRM&&(key==96||key==97) || page==Page.CHORD&&!reserved) && ShortcutKeys.eligible(event)){ record(event); return true; }
        if(page==Page.SETUP&&(key==KeyEvent.KEYCODE_BUTTON_L1||key==KeyEvent.KEYCODE_BUTTON_R1)){
            if(event.getAction()==0&&event.getRepeatCount()==0){ Button target=key==KeyEvent.KEYCODE_BUTTON_L1?setupPrevious:setupNext; if(target!=null&&target.isEnabled()) target.performClick(); } return true;
        }
        if((page==Page.PICKER||page==Page.RESULTS||page==Page.WINDOWS||page==Page.MEMORY)&&key!=a&&key!=b&&(key==ControllerBindings.code(prefs(),"page_previous")||key==ControllerBindings.code(prefs(),"page_next"))){ if(event.getAction()==0&&event.getRepeatCount()==0){ Button target=key==ControllerBindings.code(prefs(),"page_previous")?pagePrevious:pageNext; if(target!=null&&target.isEnabled()) target.performClick(); } return true; }
        if(page==Page.PICKER&&pickerSearch!=null&&pickerSearch.hasFocus()){
            if(key==KeyEvent.KEYCODE_DPAD_DOWN){ if(event.getAction()==0){ getSystemService(android.view.inputmethod.InputMethodManager.class).hideSoftInputFromWindow(pickerSearch.getWindowToken(),0); pickerSearch.clearFocus(); if(!pickerOptions.isEmpty()) focusControl(controls.indexOf(pickerOptions.get(0))); } return true; }
            if(key!=b&&key!=KeyEvent.KEYCODE_BACK) return super.dispatchKeyEvent(event);
        }
        if(key==b){ if(event.getAction()==KeyEvent.ACTION_DOWN&&event.getRepeatCount()==0) back(); return true; }
        if(page==Page.APPS){
            if(key==ControllerBindings.code(prefs(),"others")){ if(event.getAction()==0&&event.getRepeatCount()==0) closeOthers(); return true; }
            if(key==ControllerBindings.code(prefs(),"options")){ if(event.getAction()==KeyEvent.ACTION_DOWN && event.getRepeatCount()==0 && !toolZone) protectSelected(); return true; }
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
                if(event.getAction()==KeyEvent.ACTION_DOWN) navigate(key==KeyEvent.KEYCODE_DPAD_LEFT?-1:key==KeyEvent.KEYCODE_DPAD_RIGHT?1:key==KeyEvent.KEYCODE_DPAD_UP?-2:2); return true;
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
        if(controls.get(next).isEnabled()) focusControl(next);
    }
    private Button findSettingsTool(){ for(Button tool:tools) if(android.text.TextUtils.equals("Settings",tool.getContentDescription())) return tool; return tools.get(0); }
    private boolean inside(View view,ViewGroup parent){ for(android.view.ViewParent p=view.getParent();p instanceof View;p=p.getParent()) if(p==parent) return true; return false; }
    private void navigate(int direction){
        if(page!=Page.APPS){
            if(page==Page.SETUP&&(setupStep==1||setupStep==5)&&controlIndex>=0&&controlIndex<controls.size()&&setupThemes.contains(controls.get(controlIndex))){ int index=setupThemes.indexOf(controls.get(controlIndex)),next=index; if(direction==-2){ if(index<2){ focusControl(0); return; } next-=2; } else if(direction==2){ if(index+2>=setupThemes.size()){ focusControl(setupStep==1?controls.indexOf(setupThemes.get(setupThemes.size()-1))+1:controls.indexOf(setupNext)); return; } next+=2; } else if(direction==-1&&index%2==1) next--; else if(direction==1&&index%2==0) next++; focusControl(controls.indexOf(setupThemes.get(next))); return; }
            if(page==Page.PICKER&&pickerSearch!=null&&pickerSearch.hasFocus()){ if(direction==2&&!pickerOptions.isEmpty()){ pickerSearch.clearFocus(); getSystemService(android.view.inputmethod.InputMethodManager.class).hideSoftInputFromWindow(pickerSearch.getWindowToken(),0); focusControl(controls.indexOf(pickerOptions.get(0))); } return; }
            if(page==Page.SETTINGS&&controlIndex>=0&&controlIndex<controls.size()&&colorDots.contains(controls.get(controlIndex))){ colorColumns=((GridLayout)colorDots.get(0).getParent()).getColumnCount(); int index=colorDots.indexOf(controls.get(controlIndex)),next=index; if(direction==-1){ if(index%colorColumns==0){ focusControl(1+settingsCategory); return; } next--; } else if(direction==1&&index%colorColumns<colorColumns-1) next++; else if(direction==-2) next-=colorColumns; else if(direction==2) next+=colorColumns; if(next<0) focusControl(controls.indexOf(colorDots.get(0))-1); else if(next<colorDots.size()) focusControl(controls.indexOf(colorDots.get(next))); return; }
            if(page==Page.SETTINGS&&landscape()&&!categories.isEmpty()){
                int first=controls.indexOf(categories.get(0)),last=first+categories.size()-1,content=last+1;
                if(controlIndex<first){ if(direction==2||direction==-1) focusControl(first+settingsCategory); return; }
                if(controlIndex<=last){
                    if(direction==1){ if(content<controls.size()) focusControl(content); return; }
                    if(direction==-1) return;
                    if(direction==-2&&controlIndex==first){ focusControl(0); return; }
                    int index=Math.max(first,Math.min(last,controlIndex+(direction<0?-1:1)));
                    settingsCategory=index-first; controlIndex=index; show(Page.SETTINGS); focusControl(index); return;
                }
                View parent=(View)controls.get(controlIndex).getParent(); boolean horizontal=parent instanceof LinearLayout&&((LinearLayout)parent).getOrientation()==LinearLayout.HORIZONTAL;
                if(direction==1||direction==-1){
                    int next=controlIndex+direction;
                    if(horizontal&&next>=content&&next<controls.size()&&controls.get(next).getParent()==parent) focusControl(next);
                    else if(direction==-1) focusControl(first+settingsCategory);
                    return;
                }
                int next=controlIndex+(direction<0?-1:1);
                while(horizontal&&next>=content&&next<controls.size()&&controls.get(next).getParent()==parent) next+=direction<0?-1:1;
                if(next<content) focusControl(0); else focusControl(Math.min(controls.size()-1,next)); return;
            }
            if(landscape()&&(direction==1||direction==-1)&&controlIndex>=0&&controlIndex<controls.size()&&formMain!=null&&formInfo!=null){
                View from=controls.get(controlIndex); boolean inMain=inside(from,formMain),inInfo=inside(from,formInfo); LinearLayout target=direction==1&&inMain?formInfo:direction==-1&&inInfo?formMain:null;
                if(target!=null){ View parent=(View)from.getParent(); int adjacent=controlIndex+direction; if(parent instanceof LinearLayout&&((LinearLayout)parent).getOrientation()==LinearLayout.HORIZONTAL&&adjacent>=0&&adjacent<controls.size()&&controls.get(adjacent).getParent()==parent){ focusControl(adjacent); return; }
                    int best=-1; float distance=Float.MAX_VALUE; int[] xy=new int[2]; from.getLocationOnScreen(xy); for(int i=0;i<controls.size();i++) if(inside(controls.get(i),target)&&controls.get(i).isEnabled()){ int[] to=new int[2]; controls.get(i).getLocationOnScreen(to); float d=Math.abs((xy[1]+from.getHeight()/2f)-(to[1]+controls.get(i).getHeight()/2f)); if(d<distance){distance=d;best=i;} } if(best>=0) focusControl(best); return;
                }
            }
            pageNavigate(direction<0?-1:1); return;
        }
        if(tools.isEmpty()) return;
        if(dockZone&&dock!=null){
            if(direction==-2){ boolean rightCorner=landscape()&&"thumb".equals(prefs().getString("dockStyle","thumb"))&&dock.items.size()>1&&dockIndex>=(dock.items.size()+1)/2; dockZone=false; toolZone=rightCorner||cards.isEmpty(); if(rightCorner) toolIndex=tools.indexOf(ramButton); if("focus".equals(prefs().getString("dockBehavior","always"))) dock.expand(false); markSelection(); }
            else if(direction==1||direction==-1){ dockIndex=Math.max(0,Math.min(dock.items.size()-1,dockIndex+direction)); dock.focusItem(dockIndex); } return;
        }
        if(toolZone&&toolIndex==tools.indexOf(ramButton)){
            if(direction==-2){ toolIndex=tools.indexOf(findSettingsTool()); markSelection(); return; }
            if((direction==1||direction==2)&&dock!=null){ dockZone=true; toolZone=false; dockIndex="thumb".equals(prefs().getString("dockStyle","thumb"))?Math.min(Math.max(0,dock.items.size()-1),(dock.items.size()+1)/2):Math.max(0,dock.items.size()-1); dock.expand(true); markSelection(); return; }
            if(direction==-1){ toolZone=cards.isEmpty(); if(toolZone) toolIndex=0; markSelection(); return; }
        }
        if(!toolZone&&direction==1&&(!"cards".equals(viewMode())&&selection.index()%Math.max(1,gridColumns)==Math.max(1,gridColumns)-1)&&landscape()){ toolZone=true; toolIndex=tools.indexOf(ramButton); markSelection(); return; }
        if(!toolZone&&direction==1&&selection.index()==cards.size()-1&&landscape()){ toolZone=true; toolIndex=tools.indexOf(ramButton); markSelection(); return; }
        if(direction==2&&dock!=null&&((!toolZone&&("cards".equals(viewMode())||selection.index()+Math.max(1,gridColumns)>=cards.size()))||(toolZone&&cards.isEmpty()))){ dockZone=true; toolZone=false; dockIndex=0; dock.expand(true); markSelection(); return; }
        if(cards.isEmpty()) toolZone=true; toolIndex=Math.max(0,Math.min(toolIndex,tools.size()-1));
        boolean dense=!"cards".equals(viewMode());
        if(dense&&!toolZone&&!cards.isEmpty()){
            int index=selection.index();
            if(direction==-2&&index<gridColumns){ toolZone=true; toolIndex=0; }
            else { if(direction==-2) selection.move(-gridColumns); else if(direction==2) selection.move(Math.min(gridColumns,cards.size()-1-index)); else if(direction<0&&index%gridColumns>0) selection.move(-1); else if(direction>0&&index%gridColumns<gridColumns-1&&index+1<cards.size()) selection.move(1); markSelection(); return; }
        }
        if(direction==-2){ toolZone=true; toolIndex=0; if(!tools.isEmpty()) tools.get(toolIndex).requestFocus(); }
        else if(direction==2){ if(toolZone&&tools.get(toolIndex)==findSettingsTool()&&landscape()){ toolIndex=tools.indexOf(ramButton); toolZone=true; } else toolZone=cards.isEmpty(); }
        else if(toolZone){ int last=landscape()?tools.indexOf(findSettingsTool()):tools.size()-1; toolIndex=Math.max(0,Math.min(last,toolIndex+direction)); tools.get(toolIndex).requestFocus(); }
        else selection.move(direction);
        for(int i=0;i<tools.size();i++) tools.get(i).setActivated(controllerMode&&toolZone&&i==toolIndex);
        markSelection();
    }
    @Override public boolean onGenericMotionEvent(MotionEvent event){
        if(!event.isFromSource(InputDevice.SOURCE_JOYSTICK)) return super.onGenericMotionEvent(event);
        setInputMode(true);
        float x=event.getAxisValue(MotionEvent.AXIS_HAT_X),y=event.getAxisValue(MotionEvent.AXIS_HAT_Y);
        if(Math.abs(x)<.55f) x=event.getAxisValue(MotionEvent.AXIS_X); if(Math.abs(y)<.55f) y=event.getAxisValue(MotionEvent.AXIS_Y);
        int next=Math.abs(x)>=Math.abs(y)?x>.55f?1:x<-.55f?-1:0:y>.55f?2:y<-.55f?-2:0;
        if(next!=stickDirection){ stopStick(); stickDirection=next; if(next!=0){ navigate(next); main.postDelayed(repeat,360); } }
        return true;
    }
    private void stopStick(){ stickDirection=0; main.removeCallbacks(repeat); }
    @Override public void onWindowFocusChanged(boolean focused){ super.onWindowFocusChanged(focused); if(!focused) stopStick(); }
}
