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
    enum Page { APPS, SETUP, CONNECT, CHORD, CONFIRM, SHORTCUTS, RESULTS, WINDOWS, SETTINGS, MEMORY, COMPLETE, MAPPING, BIND, DOCKSTYLE, DOCKLOOK, DOCKBEHAVIOR, PICKER, PAGING, ACCENT }
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
    private List<String> previewApps; private int previewCount=-1,previewLeft=-1,previewRight=-1; private LinearLayout previewHost; private QuickAccessDock previewDock; private Button previewMinus,previewPlus,previewRightMinus,previewRightPlus;
    private String pickerQuery=""; private LinearLayout pickerRows; private EditText pickerSearch; private final List<Button> pickerOptions=new ArrayList<>();
    private Button setupPrevious,setupNext;
    private int gridColumns=1,renderWidth,renderHeight,iconAccent;
    private HorizontalScrollView rail;
    private TextView status,memory,recordLive,recordSaved,backgroundInfo;
    private LinearLayout formMain,formInfo,memoryBars,systemRamPanel,systemRamChart;
    private int settingsCategory,ramPage;
    private final List<Button> categories=new ArrayList<>(),colorDots=new ArrayList<>(),setupThemes=new ArrayList<>(),dockStyleChoices=new ArrayList<>(),dockSizeControls=new ArrayList<>();
    private int colorColumns=13;
    private final List<JSONObject> backgroundApps=new ArrayList<>();
    private Button saveChord;
    private PopupWindow protectionPopup;
    private ScrollView pageScroll;
    private boolean pendingRefresh,controllerMode=true,resetSelection=true,captureEntry=true;
    private boolean inlineCapture,inlineConfirm,setupProtection; private LinearLayout pickerRail;
    private TextView dockBehaviorTitle,dockBehaviorHelp;private String behaviorKey="always";private final Map<Button,String> behaviorKeys=new HashMap<>();private String settingsReturnLabel;private Button pickerSearchButton;private TextView protectionEdge;private boolean protectionDrag;private boolean refreshingTasks;private int pendingBulk;private float protectionTouchX,protectionTouchY; private int pickerAnimationSerial; private int setupAccentReturn=1; private int setupStep,controlIndex=-1,catalogGeneration,detailsPage;
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
        Drawable glyph=kind.equals("none")?null:kind.equals("kofi")?getDrawable(R.drawable.ic_kofi).mutate():kind.equals("github")?getDrawable(R.drawable.ic_github).mutate():kind.equals("shizuku")?getDrawable(R.drawable.ic_shizuku).mutate():kind.equals("playstation")?getDrawable(R.drawable.ic_playstation).mutate():kind.equals("googleplay")?getDrawable(R.drawable.ic_googleplay).mutate():new ActionGlyph(kind,tint);if(glyph!=null){if(kind.equals("github")||kind.equals("playstation")||kind.equals("googleplay"))glyph.setTint(theme.background==DeckTheme.named("light").background?Color.BLACK:Color.WHITE);glyph.setBounds(0,0,dp(18),dp(18));} b.setCompoundDrawables(glyph,null,null,null); b.setCompoundDrawablePadding(dp(7)); b.setMinWidth(0); b.setMinimumWidth(0);
        StateListDrawable bg=new StateListDrawable(); GradientDrawable focused=rounded(theme.selected); focused.setStroke(dp(3),theme.accent);
        GradientDrawable selected=rounded(theme.selected); selected.setStroke(dp(1),theme.accent);
        bg.addState(new int[]{android.R.attr.state_activated},focused); bg.addState(new int[]{android.R.attr.state_focused},focused); bg.addState(new int[]{android.R.attr.state_pressed},rounded(theme.selected)); bg.addState(new int[]{android.R.attr.state_selected},selected); bg.addState(new int[]{},rounded(theme.raised));
        boolean compact=getResources().getConfiguration().screenHeightDp<440;
        b.setBackground(new InsetDrawable(bg,0,dp(6),0,dp(6))); b.setBackgroundTintList(null); b.setMinHeight(dp(48)); b.setMinimumHeight(dp(48)); b.setPadding(dp(10),dp(2),dp(10),dp(2)); b.setOnClickListener(v->{if(page==Page.SETTINGS&&formInfo!=null&&inside(b,formInfo))settingsReturnLabel=b.getText().toString();action.run();});
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-2,-2); params.setMargins(0,dp(2),dp(6),dp(2)); parent.addView(b,params); controls.add(b); return b;
    }
    private LinearLayout row(LinearLayout parent){ LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL); parent.addView(row); return row; }
    private LinearLayout section(LinearLayout parent,String title,String description){
        LinearLayout panel=new LinearLayout(this); panel.setOrientation(LinearLayout.VERTICAL); panel.setPadding(dp(18),dp(12),dp(18),dp(12)); panel.setBackground(rounded(theme.surface));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.bottomMargin=dp(12); parent.addView(panel,p);
        text(panel,title,20,false).setTypeface(null,Typeface.BOLD); if(!description.isEmpty()) text(panel,description,14,true); return panel;
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state); app=(TaskDeckApp)getApplication();if("compact".equals(prefs().getString("dockStyle","thumb")))prefs().edit().putString("dockStyle","floating").apply(); if(prefs().contains("dockIconAccent")){if(prefs().getBoolean("dockIconAccent",false)&&prefs().getString("dockIconLook","original").equals("original"))prefs().edit().putString("dockIconLook","accent").apply();prefs().edit().remove("dockIconAccent").apply();}ControllerBindings.reserveStart(prefs()); if(!prefs().contains("dockIconSize"))prefs().edit().putInt("dockIconSize",QuickAccess.size(prefs())).apply(); setupStep=state==null?0:state.getInt("setupStep",0); settingsCategory=state==null?0:state.getInt("settingsCategory",0); ramPage=state==null?0:state.getInt("ramPage",0);
        if(Build.VERSION.SDK_INT>=30) getWindow().setDecorFitsSystemWindows(false);
        else getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        if(state!=null&&"APPS".equals(state.getString("pickerReturn"))) pickerReturn=Page.APPS;
        Page initial=prefs().getBoolean("setupComplete",false)?Page.APPS:Page.SETUP;
        if(state!=null) try { initial=Page.valueOf(state.getString("page",initial.name())); } catch(IllegalArgumentException ignored){}
        if(initial==Page.CHORD || initial==Page.CONFIRM || initial==Page.WINDOWS || initial==Page.COMPLETE) initial=prefs().getBoolean("setupComplete",false)?Page.APPS:Page.SETUP;
        if(initial==Page.BIND) initial=Page.MAPPING; if(initial==Page.ACCENT){ initial=Page.SETTINGS; settingsCategory=2; }
        if(initial==Page.SETUP&&prefs().getBoolean("setupComplete",false)) initial=Page.SETTINGS;
        if(getLastNonConfigurationInstance() instanceof JSONObject){ entryTask=(JSONObject)getLastNonConfigurationInstance(); captureEntry=false; }
        if(state!=null){recordingQuickSwitch=state.getBoolean("recordingQuickSwap");inlineCapture=state.getBoolean("inlineCapture");inlineConfirm=state.getBoolean("inlineConfirm");}
        show(initial); if(initial==Page.PICKER) showPicker();
    }
    @Override public Object onRetainNonConfigurationInstance(){ return entryTask; }
    @Override public void onSaveInstanceState(Bundle out){ out.putString("page",page.name()); out.putInt("setupStep",setupStep); out.putInt("settingsCategory",settingsCategory); out.putInt("ramPage",ramPage); out.putString("pickerReturn",pickerReturn.name()); out.putBoolean("recordingQuickSwap",recordingQuickSwitch);out.putBoolean("inlineCapture",inlineCapture);out.putBoolean("inlineConfirm",inlineConfirm);super.onSaveInstanceState(out); }
    @Override protected void onResume(){
        super.onResume(); TaskDeckApp.recordingShortcut=inlineCapture||inlineConfirm||page==Page.CHORD || page==Page.CONFIRM || page==Page.BIND;
        app.changed=this::connectionChanged; app.verifyLiveAccess(); if(page==Page.SETUP || page==Page.SETTINGS) show(page); updateStatus();
        if(app.backend!=null) refresh(); else app.connect(false);
    }
    @Override protected void onPause(){ app.changed=null; TaskDeckApp.recordingShortcut=false; stopStick(); super.onPause(); }
    @Override protected void onNewIntent(Intent intent){ super.onNewIntent(intent); setIntent(intent); if(intent.getBooleanExtra("controllerEntry",false)) controllerMode=true; resetSelection=true; captureEntry=true; entryTask=null; show(prefs().getBoolean("setupComplete",false)?Page.APPS:Page.SETUP); if(app.backend!=null) refresh(); }
    private void connectionChanged(){
        if(isFinishing() || isDestroyed()) return;
        if(page==Page.SETUP){ show(Page.SETUP); }
        if(app.backend==null){ busy=false;refreshingTasks=false;pendingBulk=0; catalogGeneration++; rawTasks.clear(); backgroundApps.clear(); tasks.clear(); selection.update(Collections.emptyList()); if(page==Page.APPS){ renderCards(); updateMemory(); } updateStatus(); }
        else { updateStatus(); refresh(); }
        if(page==Page.SETTINGS && settingsCategory==0) show(Page.SETTINGS);
    }
    private void show(Page next){
        String settingsFocus=next==Page.SETTINGS&&page!=Page.SETTINGS&&page!=Page.APPS?settingsReturnLabel:null;
        if(next!=Page.APPS)pendingBulk=0;
        if(dockConfig(next)&&!dockConfig(page)){previewApps=null;previewCount=-1;previewLeft=-1;previewRight=-1;}
        if(next==Page.ACCENT){ next=Page.SETTINGS; settingsCategory=2; }
        int restoreY=page==Page.SETUP && next==Page.SETUP && pageScroll!=null?pageScroll.getScrollY():0;
        int restoreFocus=page==next&&next!=Page.APPS&&next!=Page.PICKER?controlIndex:-1;
        if(next==Page.APPS){ resetSelection=true; selection.first(); }
        if(protectionPopup!=null) protectionPopup.dismiss(); pageScroll=null;
        stopStick(); feedback=""; TaskDeckApp.recordingShortcut=next==Page.CHORD || next==Page.CONFIRM || next==Page.BIND; page=next; toolZone=false; dockZone=false; dock=null; ramButton=null; pagePrevious=null; pageNext=null;
        controls.clear(); controlIndex=-1; tools.clear(); cards.clear(); cardRow=null; rail=null; appScroll=null; appGrid=null; appContent=null; compactMemory=null; operationInfo=null; memoryBars=null; systemRamPanel=null; systemRamChart=null; categories.clear(); colorDots.clear(); setupThemes.clear();behaviorKeys.clear();dockBehaviorTitle=null;dockBehaviorHelp=null; dockStyleChoices.clear(); dockSizeControls.clear(); setupPrevious=null; setupNext=null; renderWidth=0; renderHeight=0; status=null; memory=null;
        theme=DeckTheme.resolve(prefs().getString("theme","dark"),getResources().getConfiguration().uiMode).withAccent(prefs().getString("accentColor","theme"));
        if(iconAccent!=theme.accent){ iconCache.evictAll(); iconAccent=theme.accent; }
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(theme.background); root.setPadding(dp(12),dp(6),dp(12),dp(6));
        root.setOnApplyWindowInsetsListener((view,insets)->{
            if(Build.VERSION.SDK_INT>=30){ android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars()); view.setPadding(dp(12)+bars.left,dp(6)+bars.top,dp(12)+bars.right,dp(6)+bars.bottom); }
            else view.setPadding(dp(12)+insets.getSystemWindowInsetLeft(),dp(6)+insets.getSystemWindowInsetTop(),dp(12)+insets.getSystemWindowInsetRight(),dp(6)+insets.getSystemWindowInsetBottom());
            return insets;
        }); root.setFocusableInTouchMode(true); setContentView(root); if(!controllerMode) root.requestFocus();
        if(page==Page.APPS) appsPage(); else if(page==Page.SETUP) setupPage(); else if(page==Page.CONNECT) connectPage(); else if(page==Page.SHORTCUTS) shortcutsPage(); else if(page==Page.RESULTS) resultsPage(); else if(page==Page.WINDOWS) windowsPage(); else if(page==Page.SETTINGS) settingsPage(); else if(page==Page.MEMORY) memoryPage(); else if(page==Page.COMPLETE) completionPage(); else if(page==Page.MAPPING) mappingPage(); else if(page==Page.BIND) bindPage(); else if(dockConfig(page)) dockStylePage();  else if(page==Page.DOCKBEHAVIOR) dockBehaviorPage(); else if(page==Page.PICKER) pickerPage(); else if(page==Page.PAGING) pagingMappingPage();  else recordPage();
        if(restoreY>0 && pageScroll!=null){ ScrollView scroll=pageScroll; main.post(()->scroll.scrollTo(0,restoreY)); }
        if(controllerMode && restoreFocus>=0 && restoreFocus<controls.size()){ int index=restoreFocus;focusControl(index); LinearLayout renderedRoot=root; main.post(()->{ if(root==renderedRoot) focusControl(index); }); }
        if(controllerMode&&restoreFocus<0&&next!=Page.APPS&&next!=Page.COMPLETE&&controls.size()>1) focusControl(next==Page.ACCENT?Math.max(0,Arrays.asList(DeckTheme.ACCENT_KEYS).indexOf(prefs().getString("accentColor","theme")))+1:next==Page.RESULTS?0:next==Page.SETTINGS?1+settingsCategory:next==Page.PICKER&&!pickerOptions.isEmpty()?controls.indexOf(pickerOptions.get(0)):1);
        if(settingsFocus!=null){for(int i=0;i<controls.size();i++)if(controls.get(i) instanceof Button&&settingsFocus.contentEquals(((Button)controls.get(i)).getText())){focusControl(i);break;}}
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
    private String actionFor(String key){ switch(key){ case "A":return "open"; case "B":return "exit"; case "X":return "close"; case "Y":return "clear"; case "Select":return "options"; default:return "others"; } }
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
            public void launch(String pkg){ dockZone=true; toolZone=false; dockIndex=Math.max(0,QuickAccess.displayOrder(prefs(),QuickAccess.read(prefs())).indexOf(pkg)); launchFavorite(pkg); }
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
        boolean adding=!QuickAccess.read(prefs()).contains(pkg); QuickAccess.toggle(prefs(),pkg); if(adding) Toast.makeText(this,launchLabel(pkg)+" added to Dock",Toast.LENGTH_SHORT).show(); renderDock(); updatePickerRail();
    }
    private void choice(String title,String[] labels,int selected,java.util.function.IntConsumer picked){
        LinearLayout panel=new LinearLayout(this);panel.setOrientation(LinearLayout.VERTICAL);panel.setPadding(dp(14),dp(10),dp(14),dp(10));panel.setBackground(ramSurface());text(panel,title,18,false);List<Button> choices=new ArrayList<>();int[] cursor={Math.max(0,selected)};
        PopupWindow popup=new PopupWindow(panel,dp(Math.min(440,getResources().getConfiguration().screenWidthDp-32)),-2,true);protectionPopup=popup;
        for(int i=0;i<labels.length;i++){final int index=i;Button option=button(panel,labels[i],()->{popup.dismiss();picked.accept(index);});option.setSelected(i==selected);option.setLayoutParams(new LinearLayout.LayoutParams(-1,dp(44)));option.setOnTouchListener((v,event)->{if(event.getActionMasked()==MotionEvent.ACTION_DOWN){setInputMode(false);v.setFocusableInTouchMode(false);for(Button item:choices)item.setActivated(false);}return false;});choices.add(option);}
        popup.setBackgroundDrawable(ramSurface());popup.setOutsideTouchable(true);popup.setOnDismissListener(()->{controls.removeAll(choices);if(protectionPopup==popup)protectionPopup=null;});
        View.OnKeyListener keys=(v,key,e)->{setInputMode(true);if(e.getAction()!=0)return true;if(key==ControllerBindings.code(prefs(),"exit")||key==KeyEvent.KEYCODE_BACK){popup.dismiss();return true;}if(key==ControllerBindings.code(prefs(),"open")||key==23){choices.get(cursor[0]).performClick();return true;}int delta=key==19||key==21?-1:key==20||key==22?1:0;cursor[0]=Math.max(0,Math.min(choices.size()-1,cursor[0]+delta));for(int i=0;i<choices.size();i++)choices.get(i).setActivated(i==cursor[0]);return true;};
        panel.setOnKeyListener(keys);for(Button option:choices)option.setOnKeyListener(keys);panel.setFocusableInTouchMode(true);popup.showAtLocation(root,Gravity.CENTER,0,0);panel.requestFocus();if(controllerMode)choices.get(cursor[0]).setActivated(true);
        panel.setOnGenericMotionListener(new View.OnGenericMotionListener(){int direction;public boolean onGenericMotion(View v,MotionEvent e){float y=e.getAxisValue(MotionEvent.AXIS_HAT_Y);if(Math.abs(y)<.55f)y=e.getAxisValue(MotionEvent.AXIS_Y);int next=y>.55?1:y<-.55?-1:0;if(next!=0&&next!=direction){cursor[0]=Math.max(0,Math.min(choices.size()-1,cursor[0]+next));for(int i=0;i<choices.size();i++)choices.get(i).setActivated(i==cursor[0]);}direction=next;return true;}});
    }
    private void dockOptions(String pkg){
        choice("Dock",new String[]{"Open "+launchLabel(pkg),"Move left","Move right","Remove from Dock"},-1,index->{ if(index==0){ launchFavorite(pkg); return; } List<String> order=QuickAccess.displayOrder(prefs(),QuickAccess.read(prefs())); int position=order.indexOf(pkg); if(index==3) order.remove(pkg); else order=QuickAccess.move(order,pkg,position+(index==1?-1:1)); QuickAccess.saveDisplayOrder(prefs(),order); dockIndex=Math.max(0,order.indexOf(pkg)); renderDock(); });
    }
    private void dockStylePage(){
        boolean icons=page==Page.DOCKLOOK;
        formPage(icons?"Icons":"Dock / Background");LinearLayout heading=(LinearLayout)root.getChildAt(0);heading.setLayoutParams(new LinearLayout.LayoutParams(-1,dp(44)));compactButton((Button)heading.getChildAt(1));Button dockBack=(Button)heading.getChildAt(1);Drawable backBadge=new ControllerBadge(ControllerBindings.badge(prefs(),"exit"),theme.text);backBadge.setBounds(0,0,dp(22),dp(18));dockBack.setCompoundDrawables(backBadge,null,null,null);
        LinearLayout shell=(LinearLayout)formMain.getParent();shell.removeAllViews();shell.setOrientation(LinearLayout.VERTICAL);DockOptionsGrid grid=new DockOptionsGrid(this);grid.setTag("dockOptionsGrid");grid.sectionColor(theme.surface);shell.addView(grid,new LinearLayout.LayoutParams(-1,-1));LinearLayout[] columns=new LinearLayout[2];for(int i=0;i<2;i++){columns[i]=new LinearLayout(this);columns[i].setOrientation(LinearLayout.VERTICAL);columns[i].setGravity(Gravity.TOP);columns[i].setPadding(dp(8),dp(6),dp(8),dp(6));grid.addView(columns[i]);}formMain=columns[0];formInfo=columns[1];
        if(icons){sizeStepper(columns[0],true);shapeOptions(columns[0],true);iconLookControls(columns[1],true);}else{
        dockLabel(columns[0],"Dock Layout");OptionGrid layouts=new OptionGrid(this,3,dp(compactDock()?36:80),dp(3));columns[0].addView(layouts,new LinearLayout.LayoutParams(-1,-2));String[] labels={"Thumb corners","Floating","Shelf"},keys={"thumb","floating","shelf"};for(int i=0;i<keys.length;i++){String key=keys[i],label=labels[i];Button option=button(columns[0],"",()->{prefs().edit().putString("dockStyle",key).apply();show(Page.DOCKSTYLE);});columns[0].removeView(option);previewButton(option,new DockStyleIcon(key,theme.text),label);option.setForeground(new CaptionedPreview(new DockStyleIcon(key,theme.text),key.equals("thumb")?"Thumb\nCorners":label,theme.text,getResources().getDisplayMetrics().density,compactDock()));option.setSelected(key.equals(prefs().getString("dockStyle","thumb")));dockStyleChoices.add(option);layouts.addView(option);}
        if("thumb".equals(prefs().getString("dockStyle","thumb"))){Button swap=button(columns[0],"",()->{prefs().edit().putBoolean("dockSwapSides",!prefs().getBoolean("dockSwapSides",false)).apply();if(previewLeft>=0){int left=previewLeft;previewLeft=previewRight;previewRight=left;}show(Page.DOCKSTYLE);});previewButton(swap,new ActionGlyph("switch",theme.text),"Swap Dock sides");swap.setTooltipText("Swap sides");swap.setForeground(new InlineAction("Swap sides","switch",theme.text,getResources().getDisplayMetrics().density));LinearLayout.LayoutParams size=new LinearLayout.LayoutParams(dp(compactDock()?116:136),dp(compactDock()?24:32));size.gravity=Gravity.CENTER_HORIZONTAL;size.topMargin=dp(2);size.bottomMargin=dp(2);swap.setLayoutParams(size);}
        sizeStepper(columns[0],false);
        LinearLayout colorHeading=row(columns[1]);TextView colorTitle=dockLabel(colorHeading,"Background Color");colorTitle.setTag("dockColorTitle");colorTitle.setLayoutParams(new LinearLayout.LayoutParams(0,dp(compactDock()?14:20),1));TextView colorName=dockLabel(colorHeading,"");colorName.setTextSize(10);colorName.setGravity(Gravity.END);colorName.setLayoutParams(new LinearLayout.LayoutParams(-2,dp(compactDock()?14:20)));colorName.setTag("dockColorName");OptionGrid palette=new OptionGrid(this,5,dp(compactDock()?22:44),dp(3));columns[1].addView(palette,new LinearLayout.LayoutParams(-1,-2));for(String key:QuickAccess.BACKGROUND_KEYS){Button swatch=button(columns[1],"",()->{prefs().edit().putString("dockBackground",key).apply();show(Page.DOCKSTYLE);});columns[1].removeView(swatch);int color=key.equals("theme")?theme.surface:key.equals("none")?Color.TRANSPARENT:key.equals("black")?Color.BLACK:key.equals("graphite")?Color.rgb(104,111,124):DeckTheme.accentColor(key);swatch.setTag("dockBackground:"+key);swatch.setContentDescription((key.equals("theme")?"Follow theme":key)+" Dock background");swatch.setTooltipText(swatch.getContentDescription());swatch.setCompoundDrawables(null,null,null,null);swatch.setMinHeight(0);swatch.setMinimumHeight(0);swatch.setPadding(0,0,0,0);swatch.setBackground(new DockColorSwatch(color,theme.surface,theme.accent,key.equals("none"),key.equals("theme"),getResources().getDisplayMetrics().density));swatch.setSelected(key.equals(prefs().getString("dockBackground","theme")));colorDots.add(swatch);palette.addView(swatch);String name=key.equals("theme")?"Theme":key.equals("none")?"None":Character.toUpperCase(key.charAt(0))+key.substring(1);swatch.setOnFocusChangeListener((v,focus)->colorName.setText(focus?"Focus: "+name:""));swatch.setOnHoverListener((v,e)->{colorName.setText(e.getActionMasked()==MotionEvent.ACTION_HOVER_ENTER?"Focus: "+name:"");return false;});}
        if(!"none".equals(prefs().getString("dockBackground","theme")))shapeOptions(columns[1],false);
        }addDockPreview();
    }
    private boolean compactDock(){return getResources().getConfiguration().screenHeightDp<420;}
    private int visualHeight(){return dp(compactDock()?32:44);}
    private TextView dockLabel(LinearLayout parent,String value){TextView label=text(parent,value,11,false);label.setTypeface(null,Typeface.BOLD);label.setPadding(0,0,0,0);label.setSingleLine(true);label.setLayoutParams(new LinearLayout.LayoutParams(-1,dp(compactDock()?14:20)));return label;}
    private void dockControl(Button button,int height){if(height==30&&!compactDock())height=44;button.setMinHeight(0);button.setMinimumHeight(0);button.setTextSize(11);button.setPadding(dp(6),0,dp(6),0);if(button.getBackground() instanceof InsetDrawable)button.setBackground(((InsetDrawable)button.getBackground()).getDrawable());LinearLayout.LayoutParams size=new LinearLayout.LayoutParams(-1,dp(height));size.bottomMargin=dp(3);button.setLayoutParams(size);}
    private void previewButton(Button button,Drawable artwork,String description){button.setStateListAnimator(null);button.setBackground(new ChoiceSurface(theme,getResources().getDisplayMetrics().density));button.setText("");button.setCompoundDrawables(null,null,null,null);button.setForeground(new CenteredGlyph(artwork,dp(compactDock()?28:36),dp(compactDock()?28:36)));button.setForegroundGravity(Gravity.FILL);button.setContentDescription(description);button.setTooltipText(description);button.setMinHeight(0);button.setMinimumHeight(0);button.setPadding(0,0,0,0);if(button.getBackground() instanceof InsetDrawable)button.setBackground(((InsetDrawable)button.getBackground()).getDrawable());}
    private Drawable sampleAppIcon(){List<String> saved=QuickAccess.read(prefs());String name=!saved.isEmpty()?saved.get(0):entryTask!=null?entryTask.optString("package"):previewApps!=null&&!previewApps.isEmpty()?previewApps.get(0):"com.android.settings";return appIcon(name);}
    private void shapeOptions(LinearLayout parent,boolean icons){Page destination=page;dockLabel(parent,icons?"Icon Shape":"Background Shape");String key=icons?"dockIconShape":"dockBackgroundShape";String[] shapes=icons?new String[]{"round","square","squircle"}:new String[]{"rounded","round","square","squircle"};OptionGrid row=new OptionGrid(this,shapes.length,dp(compactDock()?36:72),dp(3));parent.addView(row,new LinearLayout.LayoutParams(-1,-2));for(String shape:shapes){Button choice=button(parent,"",()->{prefs().edit().putString(key,shape).apply();show(destination);});parent.removeView(choice);choice.setSelected(shape.equals(prefs().getString(key,icons?"round":"rounded")));previewButton(choice,new PreviewOption(sampleAppIcon(),theme,icons?"icon":"background",shape,prefs().getString("dockIconShape","round"),prefs().getString("dockBackgroundShape","rounded")),(icons?"Icon":"Background")+" shape: "+shape);choice.setForeground(new CaptionedPreview(new PreviewOption(sampleAppIcon(),theme,icons?"icon":"background",shape,prefs().getString("dockIconShape","round"),prefs().getString("dockBackgroundShape","rounded")),shape.equals("square")?(icons?"Squared":"Square"):shape.equals("round")?"Round":shape.equals("rounded")?"Rounded":"Squircle",theme.text,getResources().getDisplayMetrics().density,compactDock()));choice.setTag("visual:"+(icons?"icon":"background")+":"+shape);row.addView(choice);}}
    private boolean dockConfig(Page value){return value==Page.DOCKSTYLE||value==Page.DOCKLOOK;}
    private void sizeStepper(LinearLayout parent,boolean icons){
        Page destination=page;dockLabel(parent,icons?"Icon Size":"Dock Size");LinearLayout line=row(parent);line.setGravity(Gravity.CENTER);line.setTag(icons?"iconSizeStepper":"dockSizeStepper");line.setLayoutParams(new LinearLayout.LayoutParams(dp(compactDock()?132:180),dp(compactDock()?24:36)));
        for(int delta:new int[]{-10,10}){if(delta>0){TextView value=text(line,(icons?QuickAccess.iconSize(prefs()):QuickAccess.size(prefs()))+"%",12,false);value.setGravity(Gravity.CENTER);value.setPadding(0,0,0,0);value.setLayoutParams(new LinearLayout.LayoutParams(0,dp(compactDock()?24:36),1));}
            Button option=button(line,"",()->{String key=icons?"dockIconSize":"dockSize";int current=icons?QuickAccess.iconSize(prefs()):QuickAccess.size(prefs());prefs().edit().putInt(key,Math.max(50,Math.min(160,current+delta))).apply();show(destination);});previewButton(option,new ActionGlyph(delta<0?"minus":"plus",theme.text),(delta<0?"Decrease ":"Increase ")+(icons?"icon size":"Dock size"));option.setForeground(new CenteredGlyph(new ActionGlyph(delta<0?"minus":"plus",theme.text),dp(14),dp(14)));LinearLayout.LayoutParams size=new LinearLayout.LayoutParams(dp(compactDock()?26:36),dp(compactDock()?24:36));size.setMargins(0,0,0,0);option.setLayoutParams(size);int current=icons?QuickAccess.iconSize(prefs()):QuickAccess.size(prefs());option.setEnabled(delta<0?current>50:current<160);option.setAlpha(option.isEnabled()?1:.4f);dockSizeControls.add(option);}
    }
    private String previewHint(){return controllerMode?"Live preview · Preview apps only":"Live preview · Drag Dock apps on the main screen to reorder";}
    private void addDockPreview(){
        boolean thumb="thumb".equals(prefs().getString("dockStyle","thumb"));LinearLayout counts=row(root);counts.setTag("dockPreviewControls");LinearLayout.LayoutParams countSize=new LinearLayout.LayoutParams(-1,-2);countSize.topMargin=dp(8);countSize.bottomMargin=dp(8);counts.setLayoutParams(countSize);
        previewMinus=button(counts,"− Apps",()->changePreview(-1,true));previewMinus.setContentDescription(thumb?"Remove preview app from left":"Remove preview app");
        previewPlus=button(counts,"Apps +",()->changePreview(1,true));previewPlus.setContentDescription(thumb?"Add preview app on left":"Add preview app");
        TextView space=smallNote(counts,previewHint());space.setTag("dockPreviewHint");space.setGravity(Gravity.CENTER);space.setPadding(0,0,0,0);space.setTextSize(10);space.setLayoutParams(new LinearLayout.LayoutParams(0,dp(28),1));previewRightMinus=null;previewRightPlus=null;
        if(thumb){previewRightMinus=button(counts,"− Apps",()->changePreview(-1,false));previewRightMinus.setContentDescription("Remove preview app from right");previewRightPlus=button(counts,"Apps +",()->changePreview(1,false));previewRightPlus.setContentDescription("Add preview app on right");}
        for(Button count:new Button[]{previewMinus,previewPlus,previewRightMinus,previewRightPlus})if(count!=null){count.setText(R.string.preview_apps);dockControl(count,32);count.getLayoutParams().width=dp(62);((LinearLayout.LayoutParams)count.getLayoutParams()).rightMargin=count==previewMinus||count==previewRightMinus?dp(8):0;}dockPreview(root);
    }
    private String lookCaption(String mode){return mode.equals("original")?"Normal":mode.equals("mono")?"Black and\nWhite":mode.equals("accent")?"Accent\nColor":"Glass";}
    private void iconLookControls(LinearLayout parent,boolean dockIcons){
        String key=dockIcons?"dockIconLook":"taskIconLook";Page destination=page;TextView title=dockLabel(parent,dockIcons?"Icon Style":"App icon style");if(!dockIcons){((LinearLayout.LayoutParams)title.getLayoutParams()).topMargin=dp(8);title.setTag("appIconStyleTitle");}OptionGrid choices=new OptionGrid(this,dockIcons?2:4,dp(compactDock()?42:dockIcons?80:60),dp(3));parent.addView(choices,new LinearLayout.LayoutParams(dockIcons?-1:dp(240),-2));for(String mode:new String[]{"original","mono","accent","glass"}){Button choice=button(parent,"",()->{prefs().edit().putString(key,mode).apply();show(destination);});parent.removeView(choice);previewButton(choice,new PreviewOption(sampleAppIcon(),theme,"look",mode,"round","rounded"),(dockIcons?"Dock":"Task")+" icon look: "+mode);choice.setForeground(new CaptionedPreview(new PreviewOption(sampleAppIcon(),theme,"look",mode,"round","rounded"),lookCaption(mode),theme.text,getResources().getDisplayMetrics().density,compactDock()));choice.setSelected(mode.equals(prefs().getString(key,"original")));choice.setTag("visual:look:"+mode);choices.addView(choice);}
    }
    private void refreshVisualSamples(){for(Button option:controls)if(option.getTag() instanceof String&&((String)option.getTag()).startsWith("visual:")){String[] spec=((String)option.getTag()).split(":");Drawable art=new PreviewOption(sampleAppIcon(),theme,spec[1],spec[2],prefs().getString("dockIconShape","round"),prefs().getString("dockBackgroundShape","rounded"));String caption=spec[1].equals("look")?lookCaption(spec[2]):spec[2].equals("square")?(spec[1].equals("icon")?"Squared":"Square"):spec[2].equals("round")?"Round":spec[2].equals("rounded")?"Rounded":"Squircle";option.setForeground(new CaptionedPreview(art,caption,theme.text,getResources().getDisplayMetrics().density,compactDock()));}}

    private void dockBehaviorPage(){
        formPage("Dock behavior");behaviorToggle("always","Always show Dock","eye",!"focus".equals(prefs().getString("dockBehavior","always")),()->prefs().edit().putString("dockBehavior","focus".equals(prefs().getString("dockBehavior","always"))?"always":"focus").apply());behaviorToggle("magnify","Magnify focused icons","search",prefs().getBoolean("dockMagnify",true),()->prefs().edit().putBoolean("dockMagnify",!prefs().getBoolean("dockMagnify",true)).apply());behaviorToggle("indicators","Running indicators","activity",prefs().getBoolean("dockIndicators",true),()->prefs().edit().putBoolean("dockIndicators",!prefs().getBoolean("dockIndicators",true)).apply());dockBehaviorTitle=text(formInfo,"",20,false);dockBehaviorHelp=note(formInfo,"");updateBehaviorHelp(behaviorKey);
    }
    private void behaviorToggle(String key,String label,String glyph,boolean enabled,Runnable change){toggle(formMain,label,enabled,()->{behaviorKey=key;change.run();show(Page.DOCKBEHAVIOR);});Button option=(Button)formMain.getChildAt(formMain.getChildCount()-1);behaviorKeys.put(option,key);Drawable icon=new ActionGlyph(glyph,option.getCurrentTextColor());icon.setBounds(0,0,dp(18),dp(18));option.setCompoundDrawables(icon,null,null,null);option.setCompoundDrawablePadding(dp(8));option.setOnFocusChangeListener((v,focused)->{if(focused)updateBehaviorHelp(key);});option.setOnHoverListener((v,event)->{if(event.getActionMasked()==MotionEvent.ACTION_HOVER_ENTER||event.getActionMasked()==MotionEvent.ACTION_HOVER_MOVE)updateBehaviorHelp(key);return false;});}
    private void updateBehaviorHelp(String key){behaviorKey=key;if(dockBehaviorHelp==null)return;String title=key.equals("magnify")?"Magnify focused icons":key.equals("indicators")?"Running indicators":"Always show Dock";dockBehaviorTitle.setText(title);dockBehaviorHelp.setText(key.equals("magnify")?"Enlarge the selected icon slightly while navigating with the controller or hovering. The box stays fixed and reserves space for this effect.":key.equals("indicators")?"Show a small dot below apps with a reported running process. Green marks the app used before TaskDeck. A recent task alone does not prove an app is running.":"Keep Dock visible on the main screen. When off, navigate down to reveal it or touch its handle; leaving Dock collapses it again.");}
    private void showPicker(){
        if(page!=Page.PICKER) pickerReturn=page==Page.APPS?Page.APPS:Page.SETTINGS; pickerApps=null; pickerPage=0; pickerQuery=""; show(Page.PICKER);
        loadPickerApps();
    }
    private void loadPickerApps(){
        app.requestMemory("Installed apps",5000,()->{ Map<String,JSONObject> launchers=new TreeMap<>(); Intent launcher=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
            for(android.content.pm.ResolveInfo info:getPackageManager().queryIntentActivities(launcher,0)) if(info.activityInfo!=null&&!getPackageName().equals(info.activityInfo.packageName)) launchers.put(info.activityInfo.packageName,new JSONObject().put("package",info.activityInfo.packageName).put("label",info.loadLabel(getPackageManager()).toString())); return new JSONObject().put("apps",new JSONArray(launchers.values()));
        },result->{ if(!pickerActive()||isDestroyed()) return; boolean initial=pickerApps==null;pickerApps=new ArrayList<>(); JSONArray values=result.optJSONArray("apps"); if(values!=null) for(int i=0;i<values.length();i++) if(values.optJSONObject(i)!=null) pickerApps.add(values.optJSONObject(i)); pickerApps.sort((a,b)->String.CASE_INSENSITIVE_ORDER.compare(a.optString("label"),b.optString("label"))); if(initial&&!pickerApps.isEmpty()&&android.animation.ValueAnimator.areAnimatorsEnabled())pickerRows.setAlpha(0);fillPicker();if(initial&&!pickerApps.isEmpty()&&android.animation.ValueAnimator.areAnimatorsEnabled()){LinearLayout rows=pickerRows;int serial=++pickerAnimationSerial;rows.post(()->{if(rows==pickerRows&&pickerActive()&&serial==pickerAnimationSerial)rows.animate().alpha(1).setDuration(220).start();});} if(result.has("error")){ feedback=result.optString("error"); updateStatus(); } });
    }
    private boolean pickerActive(){return page==Page.PICKER||page==Page.SETUP&&setupStep==4;}
    private void pickerPage(){ buildPicker(false); }
    private void buildPicker(boolean protection){
        setupProtection=protection;
        if(!protection){LinearLayout heading=row(root);TextView title=text(heading,"Add apps to Dock",20,false);title.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1));button(heading,"Back",this::back);}
        LinearLayout search=row(root);pickerSearch=new EditText(this);pickerSearch.setSingleLine(true);pickerSearch.setTextSize(14);pickerSearch.setTextColor(theme.text);pickerSearch.setHintTextColor(theme.muted);pickerSearch.setHint("Search apps");pickerSearch.setContentDescription("Search apps");pickerSearch.setText(pickerQuery);search.addView(pickerSearch,new LinearLayout.LayoutParams(0,dp(40),1));
        pickerSearchButton=compactButton(button(search,"Search",()->{pickerSearch.requestFocus();getSystemService(android.view.inputmethod.InputMethodManager.class).showSoftInput(pickerSearch,android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);}));
        compactButton(button(search,protection?"Unlock all":"Clear Dock",()->{if(protection)prefs().edit().putStringSet("protectedApps",new HashSet<>()).apply();else QuickAccess.save(prefs(),new ArrayList<>());fillPicker();}));
        LinearLayout body=row(root);body.setLayoutParams(new LinearLayout.LayoutParams(-1,0,1));pickerRows=new LinearLayout(this);pickerRows.setOrientation(LinearLayout.VERTICAL);body.addView(pickerRows,new LinearLayout.LayoutParams(0,-1,1));pickerRail=new LinearLayout(this);pickerRail.setOrientation(LinearLayout.VERTICAL);body.addView(pickerRail,new LinearLayout.LayoutParams(dp(40),-1));pickerRail.setContentDescription(protection?"Locked apps":"Apps in Dock");
        if(protection){pickerPrevious=new Button(this);pickerNext=new Button(this);pickerPrevious.setOnClickListener(v->pickerTurn(-1));pickerNext.setOnClickListener(v->pickerTurn(1));protectionEdge=new TextView(this);protectionEdge.setTextSize(28);protectionEdge.setTextColor(theme.accent);protectionEdge.setGravity(Gravity.CENTER);protectionEdge.setContentDescription("Swipe apps or move the D-pad at an edge to change page");body.addView(protectionEdge,new LinearLayout.LayoutParams(dp(24),-1));}
        else{LinearLayout nav=row(root);pickerPrevious=pagingButton(nav,"page_previous","Previous",()->pickerTurn(-1));View spacer=new View(this);nav.addView(spacer,new LinearLayout.LayoutParams(0,1,1));pickerNext=pagingButton(nav,"page_next","Next",()->pickerTurn(1));}pagePrevious=pickerPrevious;pageNext=pickerNext;
        pickerOptions.clear();fillPicker();pickerSearch.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence v,int a,int c,int f){}public void onTextChanged(CharSequence v,int a,int b,int c){pickerQuery=v.toString();pickerPage=0;fillPicker();}public void afterTextChanged(android.text.Editable e){}});
    }
    private void pickerTurn(int direction){if(direction<0&&pickerPage==0||direction>0&&(pickerNext==null||!pickerNext.isEnabled()))return;pickerPage+=direction;controlIndex=-1;if(pickerActive()){pickerRows.animate().cancel();pickerRows.setTranslationX(0);pickerRows.setAlpha(1);fillPicker();if(android.animation.ValueAnimator.areAnimatorsEnabled()){final LinearLayout rows=pickerRows;final int serial=++pickerAnimationSerial;rows.getViewTreeObserver().addOnPreDrawListener(new android.view.ViewTreeObserver.OnPreDrawListener(){public boolean onPreDraw(){if(rows.getViewTreeObserver().isAlive())rows.getViewTreeObserver().removeOnPreDrawListener(this);if(serial==pickerAnimationSerial&&rows==pickerRows&&pickerActive()){rows.setTranslationX(direction*dp(12));rows.setAlpha(.65f);rows.animate().translationX(0).alpha(1).setDuration(360).setInterpolator(new android.view.animation.DecelerateInterpolator(1.4f)).start();}return true;}});}}else show(Page.PICKER);}
    private void updatePickerRail(){
        if(pickerRail==null||!pickerActive())return;pickerRail.removeAllViews();List<String> names=setupProtection?new ArrayList<>(protectedApps()):QuickAccess.displayOrder(prefs(),QuickAccess.read(prefs()));
        for(String pkg:names){ImageView icon=new ImageView(this);icon.setImageDrawable(appIcon(pkg));icon.setScaleType(ImageView.ScaleType.FIT_CENTER);icon.setPadding(dp(4),dp(2),dp(4),dp(2));icon.setContentDescription(launchLabel(pkg));icon.setTooltipText(launchLabel(pkg));pickerRail.addView(icon,new LinearLayout.LayoutParams(-1,0,1));}
    }
    private void fillPicker(){
        if(pickerRows==null||!pickerActive())return;boolean editing=pickerSearch.hasFocus();controls.removeAll(pickerOptions);pickerOptions.clear();pickerRows.removeAllViews();List<JSONObject> filtered=new ArrayList<>();if(pickerApps!=null)for(JSONObject item:pickerApps)if(item.optString("label").toLowerCase(Locale.ROOT).contains(pickerQuery.toLowerCase(Locale.ROOT)))filtered.add(item);
        filtered.sort((a,b)->String.CASE_INSENSITIVE_ORDER.compare(a.optString("label"),b.optString("label")));int count=filtered.size();pickerPage=Math.max(0,Math.min(pickerPage,Math.max(0,(count-1)/8)));int insert=setupProtection?(setupPrevious!=null&&controls.contains(setupPrevious)?controls.indexOf(setupPrevious):controls.size()):controls.indexOf(pickerPrevious);if(count==0)note(pickerRows,pickerApps==null?"Loading apps…":"No apps found");LinearLayout line=null;
        for(int slot=0;slot<8;slot++){
            if(slot%4==0){line=new LinearLayout(this);pickerRows.addView(line,new LinearLayout.LayoutParams(-1,0,1));}
            int index=pickerPage*8+slot;if(index>=count){line.addView(new View(this),new LinearLayout.LayoutParams(0,-1,1));continue;}JSONObject item=filtered.get(index);String pkg=item.optString("package"),name=item.optString("label");
            Button option=button(line,name,()->{if(setupProtection){Set<String> locks=protectedApps();if(!locks.remove(pkg))locks.add(pkg);prefs().edit().putStringSet("protectedApps",locks).apply();}else pin(pkg);for(Button row:pickerOptions){boolean selected=setupProtection?protectedApps().contains((String)row.getTag()):QuickAccess.read(prefs()).contains((String)row.getTag());row.setSelected(selected);row.setContentDescription(row.getText()+(selected?", selected":", not selected"));}updatePickerRail();});line.removeView(option);option.setCompoundDrawables(null,null,null,null);option.setTag(pkg);option.setTooltipText(name);option.setEllipsize(android.text.TextUtils.TruncateAt.END);AppPickerTile tile=new AppPickerTile(this,option,appIcon(pkg));LinearLayout.LayoutParams size=new LinearLayout.LayoutParams(0,-1,1);size.setMargins(dp(3),dp(3),dp(3),dp(3));line.addView(tile,size);option.setSelected(setupProtection?protectedApps().contains(pkg):QuickAccess.read(prefs()).contains(pkg));controls.remove(option);controls.add(insert++,option);pickerOptions.add(option);
        }
        pickerPrevious.setEnabled(pickerPage>0);pickerNext.setEnabled((pickerPage+1)*8<count);if(setupProtection&&protectionEdge!=null)protectionEdge.setText(pickerNext.isEnabled()?"›":pickerPrevious.isEnabled()?"‹":"");updatePickerRail();if(editing){pickerSearch.requestFocus();controlIndex=-1;}else if(controllerMode&&!pickerOptions.isEmpty())focusControl(controls.indexOf(pickerOptions.get(0)));
    }
    private void dockPreview(LinearLayout parent){
        previewHost=new LinearLayout(this); previewHost.setOrientation(LinearLayout.VERTICAL); previewHost.setContentDescription("Live dock preview"); parent.addView(previewHost,new LinearLayout.LayoutParams(-1,-2));
        if(previewApps!=null){ renderPreview(); return; }
        for(Button sample:new Button[]{previewMinus,previewPlus,previewRightMinus,previewRightPlus})if(sample!=null){sample.setEnabled(false);sample.setAlpha(.4f);}
        app.requestMemory("Preview apps",5000,()->{ Map<String,String> installed=new TreeMap<>(); Intent intent=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER); for(android.content.pm.ResolveInfo info:getPackageManager().queryIntentActivities(intent,0)) if(info.activityInfo!=null) installed.put(info.activityInfo.packageName,info.loadLabel(getPackageManager()).toString()); List<String> keys=new ArrayList<>(installed.keySet()); keys.sort((a,b)->String.CASE_INSENSITIVE_ORDER.compare(installed.get(a),installed.get(b)));List<String> ordered=new ArrayList<>(QuickAccess.read(prefs()));for(String key:keys)if(!ordered.contains(key))ordered.add(key);return new JSONObject().put("packages",new JSONArray(ordered)); },result->{
            if(isDestroyed()) return; previewApps=new ArrayList<>(); JSONArray values=result.optJSONArray("packages"); if(values!=null) for(int i=0;i<values.length();i++) previewApps.add(values.optString(i)); if(dockConfig(page)) renderPreview();
        });
    }
    private void changePreview(int delta,boolean left){if(previewApps==null)return;if(delta>0&&previewCount>=previewApps.size()||delta<0&&previewCount<=1)return;if("thumb".equals(prefs().getString("dockStyle","thumb"))){if(left)previewLeft=Math.max(0,previewLeft+delta);else previewRight=Math.max(0,previewRight+delta);previewCount=previewLeft+previewRight;}else previewCount+=delta;renderPreview();}
    private List<String> previewPackages(){
        if(!"thumb".equals(prefs().getString("dockStyle","thumb")))return new ArrayList<>(previewApps.subList(0,previewCount));
        List<String> saved=QuickAccess.displayOrder(prefs(),QuickAccess.read(prefs()));int split=QuickAccess.leftCount(prefs(),saved.size());List<String> left=new ArrayList<>(saved.subList(0,Math.min(split,previewLeft))),right=new ArrayList<>(saved.subList(split,split+Math.min(saved.size()-split,previewRight)));List<String> pool=new ArrayList<>();for(String pkg:previewApps)if(!saved.contains(pkg))pool.add(pkg);for(String pkg:saved)if(!left.contains(pkg)&&!right.contains(pkg))pool.add(pkg);int index=0;while(left.size()<previewLeft&&index<pool.size())left.add(pool.get(index++));while(right.size()<previewRight&&index<pool.size())right.add(pool.get(index++));left.addAll(right);return QuickAccess.storageOrder(prefs(),left);
    }
    private void renderPreview(){
        if(!dockConfig(page)||previewHost==null||previewApps==null) return;
        refreshVisualSamples();previewHost.removeAllViews(); if(previewCount<0) previewCount=Math.max(1,QuickAccess.read(prefs()).size()); previewCount=Math.max(1,Math.min(previewCount,previewApps.size()));
        if(previewLeft<0||previewLeft+previewRight!=previewCount){previewLeft=QuickAccess.leftCount(prefs(),previewCount);previewRight=previewCount-previewLeft;}
        previewMinus.setEnabled(previewCount>1&&(previewRightMinus==null||previewLeft>0));if(previewRightMinus!=null)previewRightMinus.setEnabled(previewCount>1&&previewRight>0);previewPlus.setEnabled(previewCount<previewApps.size());if(previewRightPlus!=null)previewRightPlus.setEnabled(previewCount<previewApps.size());
        for(Button sample:new Button[]{previewMinus,previewPlus,previewRightMinus,previewRightPlus})if(sample!=null)sample.setAlpha(sample.isEnabled()?1:.4f);
        if(controllerMode&&controlIndex>=0&&controlIndex<controls.size()&&!controls.get(controlIndex).isEnabled())focusControl(controlIndex);
        if(previewApps.isEmpty()){ note(previewHost,"No launchable apps available for preview"); return; }
        previewDock=new QuickAccessDock(this,prefs(),new QuickAccessDock.Host(){
            public Drawable icon(String pkg){ return appIcon(pkg); } public String label(String pkg){ return launchLabel(pkg); } public boolean running(String pkg){ return false; } public boolean current(String pkg){ return false; }
            public void launch(String pkg){} public void options(String pkg){} public void add(){} public void changed(){} public void touch(){} public boolean controller(){ return false; }
        }); previewDock.preview(previewPackages());previewDock.previewSplit(previewLeft); previewHost.addView(previewDock,new LinearLayout.LayoutParams(-1,-2)); previewDock.update(theme); previewDock.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
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
        Drawable value=null; try { value=getPackageManager().getApplicationIcon(name); if(value!=null)value=value.mutate(); } catch(PackageManager.NameNotFoundException ignored){}
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
        View gap=new View(this); gap.setBackgroundColor(theme.muted);gap.setAlpha(.25f);LinearLayout.LayoutParams dividerSize=new LinearLayout.LayoutParams(dp(1),dp(20));dividerSize.leftMargin=dp(8);dividerSize.rightMargin=dp(8);header.addView(gap,dividerSize);gap.setContentDescription("Controller actions");
        HorizontalScrollView hints=new HorizontalScrollView(this); hints.setHorizontalScrollBarEnabled(false); hints.setFillViewport(true); header.addView(hints,new LinearLayout.LayoutParams(0,-2,1));
        LinearLayout actions=new LinearLayout(this); actions.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT); hints.addView(actions);
        controllerButton(actions,"A","Open","open",this::openSelected); controllerButton(actions,"B","Exit","exit",this::finish); controllerButton(actions,"X","Close","close",this::closeSelected);
        controllerButton(actions,"Y","Close all","all",this::closeAll); controllerButton(actions,"R1","Close all but current","others",this::closeOthers); controllerButton(actions,"Select","Options","options",this::protectSelected);
        Button settings=compactButton(button(header,"Settings",()->show(Page.SETTINGS)));settings.setText("");settings.setContentDescription("Settings");settings.setTooltipText("Start: Settings");settings.setCompoundDrawables(null,null,null,null);settings.setForeground(new SettingsHint(theme.text,getResources().getDisplayMetrics().density));settings.setPadding(0,0,0,0);((LinearLayout.LayoutParams)settings.getLayoutParams()).width=dp(58);tools.add(settings);
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
        if("fit".equals(viewMode())&&!cards.isEmpty()&&appContent.isLaidOut()&&android.animation.ValueAnimator.areAnimatorsEnabled()){ android.transition.AutoTransition transition=new android.transition.AutoTransition();transition.setDuration(240);android.transition.TransitionManager.beginDelayedTransition(appContent,transition); } appContent.removeAllViews(); cards.clear(); rail=null; appScroll=null; appGrid=null;
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
            LinearLayout card=new LinearLayout(this); card.setTransitionName("task:"+task.optInt("id")+":"+task.optString("package")); card.setOrientation(LinearLayout.VERTICAL); card.setGravity(list?Gravity.CENTER_VERTICAL:Gravity.CENTER); card.setPadding(dp(horizontal?12:5),dp(horizontal?(compact?6:14):2),dp(horizontal?12:5),dp(horizontal?(compact?6:14):2)); card.setFocusableInTouchMode(controllerMode); card.setDefaultFocusHighlightEnabled(false);
            if(horizontal){ LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(width,-1); p.setMargins(0,0,cards.size()<tasks.size()-1?dp(DECK_GAP):0,0); cardRow.addView(card,p); }
            else { GridLayout.LayoutParams p=new GridLayout.LayoutParams(); p.width=width; p.height=cellHeight; p.setMargins(0,0,cards.size()%gridColumns<gridColumns-1?dp(DECK_GAP):0,cards.size()/gridColumns<(tasks.size()-1)/gridColumns?dp(DECK_GAP):0); appGrid.addView(card,p); }
            LinearLayout stack=new LinearLayout(this); stack.setOrientation(LinearLayout.VERTICAL); stack.setGravity(Gravity.CENTER); card.addView(stack,new LinearLayout.LayoutParams(-1,-2));
            if(isCurrent(task.optString("package"))){ TextView current=text(stack,"Current",9,false); current.setTag("currentBadge"); current.setTextColor(successColor()); GradientDrawable chip=rounded(theme.surface); chip.setStroke(dp(1),successColor()); current.setBackground(chip); current.setGravity(Gravity.CENTER); current.setPadding(dp(6),dp(1),dp(6),dp(1)); LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-2,dp(18)); cp.gravity=Gravity.CENTER_HORIZONTAL; cp.bottomMargin=dp(6); current.setLayoutParams(cp); if(locked(task)){Drawable lock=getDrawable(R.drawable.ic_lock).mutate();lock.setTint(successColor());lock.setBounds(0,0,dp(10),dp(10));current.setCompoundDrawables(lock,null,null,null);current.setCompoundDrawablePadding(dp(3));}current.setContentDescription("Current app: "+task.optString("label")+(locked(task)?", Protected":"")); }
            if(locked(task)&&!isCurrent(task.optString("package"))){TextView lockBadge=text(stack,tiny?"":"Protected",9,false);lockBadge.setTag("protectedBadge");Drawable lock=getDrawable(R.drawable.ic_lock).mutate();lock.setTint(theme.accent);lock.setBounds(0,0,dp(12),dp(12));lockBadge.setCompoundDrawables(lock,null,null,null);lockBadge.setCompoundDrawablePadding(dp(4));lockBadge.setTextColor(theme.accent);GradientDrawable border=rounded(theme.surface);border.setStroke(dp(1),theme.accent);lockBadge.setBackground(border);lockBadge.setPadding(dp(6),dp(1),dp(6),dp(1));LinearLayout.LayoutParams badgeSize=new LinearLayout.LayoutParams(-2,dp(18));badgeSize.gravity=Gravity.CENTER_HORIZONTAL;badgeSize.bottomMargin=dp(6);lockBadge.setLayoutParams(badgeSize);}
            LinearLayout body=new LinearLayout(this); body.setOrientation(list?LinearLayout.HORIZONTAL:LinearLayout.VERTICAL); body.setGravity(Gravity.CENTER); stack.addView(body,new LinearLayout.LayoutParams(-1,-2));
            ImageView icon=new ImageView(this); icon.setImageDrawable(new IconArtwork(appIcon(task.optString("package")),prefs().getString("taskIconLook","original"),false,theme.accent)); icon.setContentDescription(task.optString("label")); int iconSize=horizontal?(compact?40:60):list?30:24; if(tiny) icon.setVisibility(View.GONE); body.addView(icon,new LinearLayout.LayoutParams(dp(iconSize),dp(iconSize)));
            LinearLayout labels=body; if(list){ labels=new LinearLayout(this); labels.setOrientation(LinearLayout.VERTICAL); LinearLayout.LayoutParams labelSize=new LinearLayout.LayoutParams(0,-2,1); labelSize.leftMargin=dp(10); body.addView(labels,labelSize); }
            TextView name=text(labels,task.optString("label")+(task.optInt("windowCount",1)>1?" · "+task.optInt("windowCount")+" windows":""),Math.max(minLabel,horizontal?(compact?16:18):micro?9:tiny?11:14),false); name.setGravity(list?Gravity.START:Gravity.CENTER); name.setMaxLines(1); name.setEllipsize(android.text.TextUtils.TruncateAt.END); name.setTypeface(null,Typeface.BOLD);
            long kb=task.optLong("pssKb",-1); String state=task.optString("memoryState","unavailable");
            String ram=kb>=0?"~"+MemoryUnits.pss(kb)+" MB app RAM"+(state.equals("shared")?" · shared":""):state.equals("loading")?"RAM loading…":state.equals("not-reported")?"No process in snapshot":"RAM unavailable";
            TextView usage=text(labels,ram,horizontal?12:10,true); usage.setMaxLines(1); usage.setEllipsize(android.text.TextUtils.TruncateAt.END); if(micro) usage.setVisibility(View.GONE); usage.setGravity(list?Gravity.START:Gravity.CENTER);
            boolean locked=locked(task);
            TextView badge=text(labels,task.optBoolean("launcher")?(locked?"Launcher · protected":launcherIdle(task)?"Launcher · not running":"Launcher · unlocked"):locked?"Protected":"X to close",12,true); badge.setTextColor(locked?theme.muted:theme.accent); badge.setGravity(Gravity.CENTER);
            if(!horizontal){ name.setPadding(0,0,0,0); usage.setPadding(0,0,0,0); badge.setPadding(0,0,0,0); badge.setVisibility(list||tiny||!locked||cellHeight<dp(90+Math.max(0,minLabel-14)*2)?View.GONE:View.VISIBLE); }

            if(horizontal&&compact){ name.setPadding(0,dp(5),0,dp(2)); usage.setPadding(0,dp(2),0,dp(2)); badge.setPadding(0,dp(2),0,dp(2)); }

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
    private void closeVibration(LinearLayout parent){Page destination=page;toggle(parent,"Vibrate on close",prefs().getBoolean("vibrateClose",false),()->{prefs().edit().putBoolean("vibrateClose",!prefs().getBoolean("vibrateClose",false)).apply();show(destination);});Button option=(Button)parent.getChildAt(parent.getChildCount()-1);option.setPadding(dp(16),dp(6),dp(16),dp(6));}
    private CheckBox checkbox(LinearLayout parent,String label,boolean checked,android.widget.CompoundButton.OnCheckedChangeListener listener){
        CheckBox box=new CheckBox(this);box.setText(label);box.setTextSize(11);box.setTextColor(theme.text);box.setButtonTintList(android.content.res.ColorStateList.valueOf(theme.accent));box.setChecked(checked);box.setMinHeight(dp(44));box.setDefaultFocusHighlightEnabled(false);StateListDrawable focus=new StateListDrawable();GradientDrawable active=rounded(theme.selected);active.setStroke(dp(2),theme.accent);focus.addState(new int[]{android.R.attr.state_activated},active);focus.addState(new int[]{android.R.attr.state_focused},active);focus.addState(new int[]{},rounded(Color.TRANSPARENT));box.setBackground(focus);box.setFocusableInTouchMode(controllerMode);box.setOnCheckedChangeListener(listener);parent.addView(box,new LinearLayout.LayoutParams(-2,dp(44)));controls.add(box);return box;
    }
    private Button permissionRow(LinearLayout parent,String label,String reason,Runnable action){LinearLayout line=row(parent);Button option=button(line,label,action);option.setTextSize(12);option.setMaxLines(2);LinearLayout.LayoutParams actionSize=new LinearLayout.LayoutParams(0,dp(48),1);actionSize.rightMargin=dp(6);option.setLayoutParams(actionSize);TextView why=note(line,reason);why.setTextSize(11);why.setPadding(dp(4),0,0,0);why.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1));return option;}
    private void setupPage(){
        setupStep=Math.max(0,Math.min(4,setupStep));if(setupStep!=2)inlineCapture=false;if(setupStep!=1)inlineConfirm=false;String[] names={"Permissions","Controller","Shortcuts","Appearance","App protection"};
        if(setupStep==4){text(root,"Setup · 5 / 5 · Protect apps from closing",18,false);buildPicker(true);if(pickerApps==null)loadPickerApps();}
        else{
            formPage("Setup · "+(setupStep+1)+" / 5 · "+names[setupStep]);
            if(setupStep==0){
                permissionRow(formMain,app.backend==null?"Connect Shizuku":"✓ Shizuku connected","Required for Android tasks.",()->{if(app.backend==null)app.connect(true);});
                button(formMain,"Open Shizuku",()->{Intent intent=getPackageManager().getLaunchIntentForPackage("moe.shizuku.privileged.api");if(intent!=null)startActivity(intent);else openLink("https://shizuku.rikka.app/download/");});
                permissionRow(formMain,shortcutServiceEnabled()?"✓ Shortcut access enabled":"Allow shortcut access","Optional. Needed for hotkeys.",()->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
                permissionRow(formMain,notificationsAllowed()?"✓ Notifications allowed":"Allow notifications","Optional. RAM/shortcut alerts.",()->{if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)notificationPermission();else startActivity(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,getPackageName()));});
                note(formInfo,"Shizuku gives TaskDeck access to Android tasks using debugging privileges. Install and start it, then authorize TaskDeck. Pairing alone does not start it.");
                button(formInfo,"Official Shizuku guide",()->openLink("https://shizuku.rikka.app/guide/setup/"));LinearLayout downloads=row(formInfo);Button play=button(downloads,"Google Play",()->openLink("https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api"));Button github=button(downloads,"GitHub releases",()->openLink("https://github.com/RikkaApps/Shizuku/releases"));for(Button download:new Button[]{play,github}){download.setTextSize(12);LinearLayout.LayoutParams cell=new LinearLayout.LayoutParams(0,dp(48),1);cell.rightMargin=download==play?dp(6):0;download.setLayoutParams(cell);}
                smallNote(formInfo,"Without root, restart Shizuku after reboot. On-screen task controls work without shortcut access.");
            }else if(setupStep==1){
                LinearLayout labels=row(formMain);for(String style:new String[]{"abxy","playstation"}){Button choice=button(labels,style.equals("abxy")?"A/B/X/Y":"PlayStation",()->{ControllerBindings.selectProfile(prefs(),style);inlineConfirm=false;show(Page.SETUP);});choice.setSelected(style.equals(prefs().getBoolean("psLabels",false)?"playstation":"abxy"));setupThemes.add(choice);}
                button(formMain,inlineConfirm?"Press printed A / Cross…":"Calibrate confirm button",()->{inlineConfirm=true;TaskDeckApp.recordingShortcut=true;show(Page.SETUP);});
                note(formMain,prefs().getBoolean("confirmCalibrated",false)?"✓ Confirm calibrated":"Optional: keep the default mapping");note(formInfo,"Choose printed labels, then calibrate the printed A or Cross button. D-pad and left stick stay available. Individual actions can be remapped later in Settings.");
            }else if(setupStep==2){
                Button deck=button(formMain,inlineCapture&&!recordingQuickSwitch?"Recording deck shortcut…":"Record deck shortcut",()->startRecording(false));deck.setEnabled(shortcutServiceEnabled());
                smallNote(formMain,ShortcutKeys.decode(prefs().getString("shortcutChord","")).isEmpty()?"Not recorded":"✓ Deck shortcut calibrated");
                Button swap=button(formMain,inlineCapture&&recordingQuickSwitch?"Recording Quick Swap Apps…":"Record Quick Swap Apps",()->startRecording(true));swap.setEnabled(shortcutServiceEnabled());
                smallNote(formMain,ShortcutKeys.decode(prefs().getString("quickSwitchChord","")).isEmpty()?"Not recorded":"✓ Quick Swap Apps calibrated");
                recordLive=note(formInfo,inlineCapture?"Hold your combination, then release to save":"Hotkeys open TaskDeck or swap the last two apps.");recordSaved=note(formInfo,inlineCapture?"Recording…":"Deck: "+ShortcutKeys.labels(ShortcutKeys.decode(prefs().getString("shortcutChord","")))+"\nQuick Swap Apps: "+ShortcutKeys.labels(ShortcutKeys.decode(prefs().getString("quickSwitchChord",""))));
                note(formInfo,shortcutServiceEnabled()?"Hold up to four buttons and release. Saves automatically. Confirm, Back, Start, D-pad and L1/R1 remain available for setup navigation.":"Enable shortcut access on the Permissions page before recording. You can skip hotkeys.");
            }else{
                LinearLayout options=null;String[] values={"light","dark","oled","auto"};for(int i=0;i<values.length;i++){if(i%2==0)options=row(formMain);String value=values[i];Button option=button(options,value.equals("auto")?"Auto":value.equals("oled")?"OLED":value.equals("light")?"Light":"Dark",()->{prefs().edit().putString("theme",value).apply();show(Page.SETUP);});option.setSelected(value.equals(prefs().getString("theme","dark")));setupThemes.add(option);}
                toggle(formMain,"Stay open after clearing",!prefs().getBoolean("exitAfterBulk",true),()->{prefs().edit().putBoolean("exitAfterBulk",!prefs().getBoolean("exitAfterBulk",true)).apply();show(Page.SETUP);});closeVibration(formMain);accentColors(formInfo);
            }
        }
        LinearLayout nav=row(root);setupPrevious=button(nav,"Previous",()->{inlineCapture=false;inlineConfirm=false;setupStep--;controlIndex=-1;show(Page.SETUP);});setupPrevious.setEnabled(setupStep>0);Drawable l1=new ControllerBadge(setupStep==4?ControllerBindings.badge(prefs(),"exit"):"L1",theme.text);l1.setBounds(0,0,dp(setupStep==4?22:40),dp(setupStep==4?22:28));setupPrevious.setCompoundDrawables(l1,null,null,null);if(setupStep==4){View gap=new View(this);nav.addView(gap,new LinearLayout.LayoutParams(0,1,1));for(Button paging:new Button[]{pickerPrevious,pickerNext}){boolean previous=paging==pickerPrevious;Drawable badge=new ControllerBadge(previous?"L1":"R1",theme.text);badge.setBounds(0,0,dp(30),dp(20));paging.setText(previous?"Previous page":"Next page");paging.setAllCaps(false);paging.setTextSize(10);paging.setTextColor(theme.text);paging.setSingleLine(true);paging.setCompoundDrawablePadding(dp(6));StateListDrawable frame=new StateListDrawable();GradientDrawable selected=rounded(theme.selected);selected.setStroke(dp(2),theme.accent);frame.addState(new int[]{android.R.attr.state_activated},selected);frame.addState(new int[]{android.R.attr.state_focused},selected);frame.addState(new int[]{},ramSurface());paging.setBackground(frame);paging.setBackgroundTintList(null);paging.setDefaultFocusHighlightEnabled(false);paging.setStateListAnimator(null);paging.setCompoundDrawables(badge,null,null,null);paging.setPadding(dp(8),dp(4),dp(8),dp(4));paging.setMinWidth(0);paging.setMinimumWidth(0);paging.setMinHeight(dp(44));paging.setContentDescription(previous?"L1: Previous apps page":"R1: Next apps page");paging.setFocusableInTouchMode(controllerMode);nav.addView(paging,new LinearLayout.LayoutParams(-2,dp(44)));controls.add(paging);}View endGap=new View(this);nav.addView(endGap,new LinearLayout.LayoutParams(0,1,1));}else{View spacer=new View(this);nav.addView(spacer,new LinearLayout.LayoutParams(0,1,1));}
        setupNext=button(nav,setupStep==4?"Finish setup":"Next step",()->{inlineCapture=false;inlineConfirm=false;controlIndex=-1;if(setupStep<4){setupStep++;show(Page.SETUP);}else{prefs().edit().putBoolean("setupComplete",true).apply();show(Page.COMPLETE);}});Drawable r1=new ControllerBadge(setupStep==4?(prefs().getBoolean("psLabels",false)?"Options":"Start"):"R1",theme.text,setupStep==4);r1.setBounds(0,0,dp(setupStep==4?38:40),dp(setupStep==4?22:28));setupNext.setCompoundDrawables(r1,null,null,null);((LinearLayout.LayoutParams)setupNext.getLayoutParams()).rightMargin=0;setupNext.setEnabled(setupStep>0||app.backend!=null);for(Button navigation:new Button[]{setupPrevious,setupNext}){navigation.setPadding(dp(setupStep==4?10:16),dp(setupStep==4?4:6),dp(setupStep==4?10:16),dp(setupStep==4?4:6));navigation.setMinHeight(dp(setupStep==4?44:52));navigation.setMinimumHeight(dp(setupStep==4?44:52));if(setupStep==4){navigation.setTextSize(11);navigation.getLayoutParams().height=dp(44);}if(navigation.getBackground() instanceof InsetDrawable)navigation.setBackground(((InsetDrawable)navigation.getBackground()).getDrawable());}setupPrevious.setContentDescription((setupStep==4?ControllerBindings.badge(prefs(),"exit"):"L1")+": Previous");setupNext.setContentDescription((setupStep==4?(prefs().getBoolean("psLabels",false)?"Options":"Start"):"R1")+": "+setupNext.getText());
        if(setupStep==4&&getResources().getConfiguration().screenWidthDp<480){setupPrevious.setTooltipText("Previous setup step");setupNext.setTooltipText("Finish setup");setupPrevious.setText("");setupNext.setText("");}
        TaskDeckApp.recordingShortcut=inlineCapture||inlineConfirm;
    }
    private boolean shortcutConflict(Set<String> keys){ return ShortcutKeys.conflicts(keys,ShortcutKeys.decode(prefs().getString(recordingQuickSwitch?"shortcutChord":"quickSwitchChord",""))); }
    private void startRecording(boolean direct){if(!shortcutServiceEnabled()){feedback="Allow shortcut access in Android before recording";updateStatus();return;}recordingQuickSwitch=direct;heldKeys.clear();capturedKeys.clear();captureDevice=null;if(page==Page.SETUP){inlineCapture=true;inlineConfirm=false;show(Page.SETUP);}else show(Page.CHORD);}
    private TextView smallNote(LinearLayout parent,String value){ TextView text=note(parent,value); text.setTextSize(12); text.setMaxLines(2); text.setEllipsize(android.text.TextUtils.TruncateAt.END); return text; }
    private void shortcutsPage(){
        formPage("Controller shortcuts"); text(formInfo,"Two independent actions",20,false);
        button(formMain,"Record deck shortcut",()->startRecording(false));
        button(formMain,"Record Quick Swap Apps",()->startRecording(true));
        for(boolean direct:new boolean[]{false,true}){
            String enabled=direct?"quickSwitchEnabled":"shortcutEnabled",chord=direct?"quickSwitchChord":"shortcutChord";
            toggle(formMain,direct?"Quick Swap Apps":"Open-menu shortcut",prefs().getBoolean(enabled,false),()->{
                if(ShortcutKeys.decode(prefs().getString(chord,"")).isEmpty()){ startRecording(direct); return; }
                prefs().edit().putBoolean(enabled,!prefs().getBoolean(enabled,false)).apply(); show(Page.SHORTCUTS);
            });
        }
        button(formMain,"Shortcut service settings",()->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        smallNote(formInfo,"Deck: "+ShortcutKeys.labels(ShortcutKeys.decode(prefs().getString("shortcutChord",""))));
        smallNote(formInfo,"Quick Swap Apps: "+ShortcutKeys.labels(ShortcutKeys.decode(prefs().getString("quickSwitchChord",""))));
        smallNote(formInfo,"Service: "+(shortcutServiceEnabled()?"enabled":"access required"));
        smallNote(formInfo,"Quick Swap Apps switches the last two distinct apps without opening this menu. Nested/identical chords are rejected to prevent accidental triggers.");
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
        main.postDelayed(()->{ if(page==Page.COMPLETE&&!isFinishing()&&!isDestroyed()) finishSetupAnimation(); },950);
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
        option.setTextColor(enabled?ink:theme.text);if(label.equals("Swap sides")){Drawable swap=new ActionGlyph("switch",enabled?ink:theme.text);swap.setBounds(0,0,dp(18),dp(18));option.setCompoundDrawables(swap,null,null,null);}
        option.setLayoutParams(new LinearLayout.LayoutParams(label.equals("Swap sides")?-2:-1,dp(48)));
    }
    private void testFeedback(){
        if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){ prefs().edit().putBoolean("pendingRamFeedbackTest",true).apply(); notificationPermission(); }
        else { app.testMemoryFeedback(); if(page==Page.SETTINGS) show(Page.SETTINGS); }
    }
    private void settingsPage(){
        formPage("Settings"); String[] names={"Connection","Controller","Appearance","Dock","Notifications","About"};
        settingsCategory=Math.max(0,Math.min(names.length-1,settingsCategory));
        if(landscape()){ ((LinearLayout.LayoutParams)formMain.getLayoutParams()).weight=.7f; ((LinearLayout.LayoutParams)formInfo.getLayoutParams()).weight=1.7f; }
        for(int i=0;i<names.length;i++){ final int category=i; Button item=button(formMain,names[i],()->{ settingsCategory=category; controlIndex=-1; show(Page.SETTINGS); if(controllerMode&&controls.size()>1+names.length) focusControl(1+names.length); }); item.setSelected(i==settingsCategory); if(i==0&&app.backend!=null){ Drawable check=new ActionGlyph("check",successColor()); check.setBounds(0,0,dp(18),dp(18)); item.setCompoundDrawables(item.getCompoundDrawables()[0],null,check,null); item.setContentDescription("Connection: connected"); } categories.add(item); item.setMinHeight(dp(44)); item.setMinimumHeight(dp(44)); item.setLayoutParams(new LinearLayout.LayoutParams(-1,dp(44))); }
        TextView sectionTitle=text(formInfo,names[settingsCategory],18,false); sectionTitle.setPadding(0,0,0,0); sectionTitle.setTypeface(null,Typeface.BOLD);sectionTitle.setIncludeFontPadding(false);sectionTitle.setLayoutParams(new LinearLayout.LayoutParams(-1,dp((int)Math.ceil(24*getResources().getConfiguration().fontScale))));
        if(settingsCategory==5&&landscape()&&getResources().getConfiguration().screenHeightDp<440)formInfo.setPadding(dp(12),dp(5),dp(12),dp(5));
        if(settingsCategory==0){
            TextView connection=smallNote(formInfo,app.backend==null?"Shizuku disconnected":"✓ Shizuku connected"); if(app.backend!=null) connection.setTextColor(successColor());
            button(formInfo,app.backend==null?"Connect Shizuku":"Connection settings",()->show(Page.CONNECT));
            button(formInfo,"Shizuku guide",()->openLink("https://shizuku.rikka.app/guide/setup/"));
            smallNote(formInfo,"Shizuku grants debugging access to Android tasks. Start it again after reboot.");
        } else if(settingsCategory==1){
            button(formInfo,"Button mapping",()->show(Page.MAPPING)); button(formInfo,"Configure shortcuts",()->show(Page.SHORTCUTS));
            smallNote(formInfo,"Service: "+(shortcutServiceEnabled()?"enabled":"access required"));
            smallNote(formInfo,"Choose letter or PlayStation labels, calibrate Confirm or remap individual deck actions.");
        } else if(settingsCategory==2){
            LinearLayout themes=row(formInfo);for(String value:new String[]{"light","dark","oled","auto"}){Button option=button(themes,value.equals("auto")?"Auto":value.equals("oled")?"OLED":value.equals("light")?"Light":"Dark",()->{prefs().edit().putString("theme",value).apply();show(Page.SETTINGS);});option.setSelected(value.equals(prefs().getString("theme","dark")));option.setCompoundDrawables(null,null,null,null);option.setTextSize(12);option.setPadding(dp(16),dp(4),dp(16),dp(4));option.setMinHeight(dp(40));option.setMinimumHeight(dp(40));option.getLayoutParams().height=dp(40);}
            AppearanceOptions behavior=new AppearanceOptions(this);formInfo.addView(behavior,new LinearLayout.LayoutParams(-1,-2));toggle(behavior,"Group apps",prefs().getBoolean("groupApps",true),()->{prefs().edit().putBoolean("groupApps",!prefs().getBoolean("groupApps",true)).apply();rebuildTasks();show(Page.SETTINGS);});toggle(behavior,"Stay open after clearing",!prefs().getBoolean("exitAfterBulk",true),()->{prefs().edit().putBoolean("exitAfterBulk",!prefs().getBoolean("exitAfterBulk",true)).apply();show(Page.SETTINGS);});closeVibration(behavior);for(int i=0;i<behavior.getChildCount();i++)((Button)behavior.getChildAt(i)).setTextSize(12);accentColors(formInfo);iconLookControls(formInfo,false);

        } else if(settingsCategory==3){
            button(formInfo,"Add apps to Dock",this::showPicker); button(formInfo,"Dock / Background",()->show(Page.DOCKSTYLE)); button(formInfo,"Icons",()->show(Page.DOCKLOOK)); button(formInfo,"Dock behavior",()->show(Page.DOCKBEHAVIOR));
            smallNote(formInfo,QuickAccess.read(prefs()).size()+" Dock apps · hold and drag dock icons to reorder, or use Select.");
        } else if(settingsCategory==4){
            toggle(formInfo,"RAM feedback",prefs().getBoolean("ramNotifications",true),()->{ boolean enabled=!prefs().getBoolean("ramNotifications",true); prefs().edit().putBoolean("ramNotifications",enabled).apply(); if(!enabled) getSystemService(NotificationManager.class).cancel(2); show(Page.SETTINGS); });
            toggle(formInfo,"Notification shortcut",shortcutVisible(),()->{ if(shortcutVisible()){ getSystemService(NotificationManager.class).cancel(1); prefs().edit().putBoolean("pendingNotificationShortcut",false).apply(); } else shortcutNotification(); show(Page.SETTINGS); });
            LinearLayout actions=row(formInfo); button(actions,"Test feedback",this::testFeedback); button(actions,"Permissions",()->startActivity(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,getPackageName())));
            smallNote(formInfo,"Notifications "+(notificationsAllowed("ram_results")?"allowed":"blocked")+". Android controls banner visibility.");
        } else {
            TextView about=smallNote(formInfo,"TaskDeck "+versionName()+" · SilasDV · MIT\nVibecoded with OpenAI Codex.");about.setPadding(0,0,0,0);
            aboutLink(formInfo,"Ko-fi","ko-fi.com/silasdv","https://ko-fi.com/silasdv");aboutLink(formInfo,"GitHub","github.com/silasdv/TaskDeck","https://github.com/silasdv/TaskDeck"); button(formInfo,"Redo Setup",()->{prefs().edit().putBoolean("setupComplete",false).apply();setupStep=0;inlineCapture=false;inlineConfirm=false;show(Page.SETUP);});
        }
    }
    private void aboutLink(LinearLayout parent,String label,String display,String url){LinearLayout line=row(parent);Button action=button(line,label,()->openLink(url));action.setPadding(dp(12),dp(4),dp(12),dp(4));TextView target=note(line,display);target.setTextSize(12);target.setPadding(dp(10),0,0,0);target.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1));}
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
        note(formInfo,"Press a controller button. Assigning an occupied button swaps its previous action. Start opens Settings and cannot be reassigned. D-pad, Android Back and the on-screen Back button remain available.");
    }
    private Button pagingButton(LinearLayout parent,String action,String label,Runnable callback){ Button b=button(parent,label,callback); if(action.equals("page_next")) ((LinearLayout.LayoutParams)b.getLayoutParams()).rightMargin=0; String hint=ControllerBindings.badge(prefs(),action); Drawable badge=new ControllerBadge(hint,theme.text); badge.setBounds(0,0,dp(hint.length()>1&&!ControllerBadge.symbol(hint)?28:20),dp(20)); b.setCompoundDrawables(badge,null,null,null); b.setContentDescription(label+": "+hint); return b; }
    private void pagingMappingPage(){ formPage("Page navigation"); pagingButton(formMain,"page_previous","Previous page",()->{ bindingAction="page_previous"; show(Page.BIND); }); pagingButton(formMain,"page_next","Next page",()->{ bindingAction="page_next"; show(Page.BIND); }); note(formInfo,"Applies to Dock app selection, RAM details and window pages. Main-screen actions are independent. Confirm and Back stay reserved."); }
    private void accentColors(LinearLayout parent){
        Page destination=page;
        colorColumns=8; TextView title=text(parent,"Accent Colors",12,false); title.setPadding(0,dp(2),0,dp(2));title.setLayoutParams(new LinearLayout.LayoutParams(-1,dp(18)));
        AccentPalette palette=new AccentPalette(this); FrameLayout paletteArea=page==Page.SETTINGS?new BoundedPaletteFrame(this):new FrameLayout(this); parent.addView(paletteArea,new LinearLayout.LayoutParams(-1,landscape()?0:-2,landscape()?1:0)); paletteArea.addView(palette,new FrameLayout.LayoutParams(-1,-2,Gravity.TOP));
        for(int i=0;i<DeckTheme.ACCENT_KEYS.length;i++){ String key=DeckTheme.ACCENT_KEYS[i],name=DeckTheme.ACCENT_NAMES[i]; Button dot=button(parent,"",()->{ prefs().edit().putString("accentColor",key).apply(); show(destination); }); parent.removeView(dot); dot.setTag("accent:"+key); dot.setContentDescription(name+" accent"); dot.setTooltipText(name); dot.setPadding(0,0,0,0); dot.setMinHeight(0); dot.setMinimumHeight(0); dot.setCompoundDrawables(null,null,null,null); int color=key.equals("theme")?DeckTheme.resolve(prefs().getString("theme","dark"),getResources().getConfiguration().uiMode).accent:DeckTheme.accentColor(key); dot.setBackground(new AccentDot(color,theme.accent,theme.surface,theme.selected,getResources().getDisplayMetrics().density)); dot.setSelected(key.equals(prefs().getString("accentColor","theme"))); colorDots.add(dot); GridLayout.LayoutParams size=new GridLayout.LayoutParams(GridLayout.spec(i/8),GridLayout.spec(i%8)); size.width=dp(48); size.height=dp(48); palette.addView(dot,size); }
    }
    private void boundMenuButtons(LinearLayout pane){
        if(pane==null||!inside(pane,root)||dockConfig(page)||page==Page.APPS||page==Page.PICKER||page==Page.SETTINGS&&pane==formMain) return;
        Runnable fit=()->{ if(pane.getWidth()<=0) return; List<Button> group=new ArrayList<>(); int width=0,padding=dp(16); for(int i=0;i<pane.getChildCount();i++) if(pane.getChildAt(i) instanceof Button){ Button button=(Button)pane.getChildAt(i); width=Math.max(width,ReadableButtons.naturalWidth(button,padding)); group.add(button); } width=Math.min(width,Math.max(1,pane.getWidth()-pane.getPaddingLeft()-pane.getPaddingRight())); for(Button button:group){if(page==Page.DOCKSTYLE&&pane==formMain){button.getLayoutParams().width=width;button.requestLayout();}else ReadableButtons.fit(button,width,padding,page==Page.MAPPING?dp(40):dp(48));} if(page==Page.MAPPING&&pane==formMain&&pane.getHeight()>0){ int needed=0; for(Button button:group){ LinearLayout.LayoutParams size=(LinearLayout.LayoutParams)button.getLayoutParams(); needed+=size.height+size.topMargin+size.bottomMargin; } int vertical=Math.min(dp(7),Math.max(0,(pane.getHeight()-needed)/2)); if(pane.getPaddingTop()!=vertical) pane.setPadding(pane.getPaddingLeft(),vertical,pane.getPaddingRight(),vertical); } };

        pane.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{ if(r-l!=or-ol) fit.run(); }); pane.post(fit);
    }
    private void profileMenu(){String[] labels={"A/B/X/Y","A/B/X/Y (swapped)","PlayStation symbols","PlayStation symbols (swapped)"},values={"abxy","abxy_swapped","playstation","playstation_swapped"};choice("Button labels",labels,Arrays.asList(values).indexOf(ControllerBindings.profile(prefs())),index->{ControllerBindings.selectProfile(prefs(),values[index]);show(Page.MAPPING);});}
    private int successColor(){ return theme.currentColor(); }
    private String versionName(){ try { return getPackageManager().getPackageInfo(getPackageName(),0).versionName; } catch(Exception absent){ return "Unknown"; } }
    private void openLink(String url){ try { startActivity(new Intent(Intent.ACTION_VIEW,android.net.Uri.parse(url))); } catch(ActivityNotFoundException unavailable){ feedback="No browser available"; updateStatus(); } }
    private void connectPage(){
        formPage("Shizuku connection"); status=text(formMain,app.status,15,false);
        button(formMain,"Open Shizuku",()->{ Intent intent=getPackageManager().getLaunchIntentForPackage("moe.shizuku.privileged.api"); if(intent!=null) startActivity(intent); else { feedback="Install Shizuku from shizuku.rikka.app"; updateStatus(); } });
        button(formMain,app.backend==null?"Authorize / Connect":"Reconnect Shizuku",()->{ if(app.backend!=null) app.recoverBackend("Manual reconnect"); else app.connect(true); });
        button(formMain,prefs().getBoolean("setupComplete",false)?"Done":"Continue setup",()->{ show(prefs().getBoolean("setupComplete",false)?Page.SETTINGS:Page.SETUP); if(app.backend!=null) refresh(); });
        button(formInfo,"Official Shizuku guide",()->openLink("https://shizuku.rikka.app/guide/setup/")); note(formInfo,"Shizuku grants debugging-level access to Android tasks without root. "); note(formInfo,"1. Start Shizuku.\n2. Return and choose Authorize / Connect.\n3. Approve TaskDeck access.\n\nWithout root, restart Shizuku after reboot. TaskDeck uses it to access Android tasks.");
    }
    private void recordPage(){
        heldKeys.clear(); capturedKeys.clear(); captureDevice=null; formPage(page==Page.CHORD?(recordingQuickSwitch?"Record Quick Swap Apps":"Record deck shortcut"):"Calibrate confirm");
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
        if(page==Page.CONFIRM||page==Page.SETUP&&inlineConfirm){
            if(event.getAction()==KeyEvent.ACTION_DOWN && event.getRepeatCount()==0 && (event.getKeyCode()==96 || event.getKeyCode()==97)){
                ControllerBindings.calibrate(prefs(),event.getKeyCode()); inlineConfirm=false; feedback="Confirm button calibrated"; show(prefs().getBoolean("setupComplete",false)?Page.MAPPING:Page.SETUP);
            } return;
        }
        if(event.getAction()==KeyEvent.ACTION_DOWN && event.getRepeatCount()==0){
            if(heldKeys.isEmpty()){captureDevice=event.getDeviceId();capturedKeys.clear();}
            if(!Objects.equals(captureDevice,event.getDeviceId())){ recordLive.setText("Use buttons from one controller"); return; }
            heldKeys.add(key);
            if(heldKeys.size()>=capturedKeys.size()){ capturedKeys.clear(); capturedKeys.addAll(heldKeys); }
        } else if(event.getAction()==KeyEvent.ACTION_UP && Objects.equals(captureDevice,event.getDeviceId())) heldKeys.remove(key);
        recordLive.setText("Held now: "+(heldKeys.isEmpty()?"none":ShortcutKeys.labels(heldKeys)));
        recordSaved.setText("Recorded: "+ShortcutKeys.labels(capturedKeys)+(shortcutConflict(capturedKeys)?" · conflicts with other shortcut":""));
        if(page==Page.SETUP){if(heldKeys.isEmpty()&&!capturedKeys.isEmpty()&&capturedKeys.size()<=4&&!shortcutConflict(capturedKeys)&&shortcutServiceEnabled()){prefs().edit().putString(recordingQuickSwitch?"quickSwitchChord":"shortcutChord",ShortcutKeys.encode(capturedKeys)).putBoolean(recordingQuickSwitch?"quickSwitchEnabled":"shortcutEnabled",true).apply();inlineCapture=false;show(Page.SETUP);}return;}
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
        busy=true;refreshingTasks=true; int generation=++catalogGeneration; memoryStatus="Loading task list"; updateStatus();
        app.request("Task list",8000,()->{ configureBackend(backend); return new JSONObject(backend.readTasks()); },result->{
            if(generation!=catalogGeneration||isDestroyed()) return; busy=false;refreshingTasks=false;
            if(result.has("error")){ readError=true; pendingRefresh=false;pendingBulk=0; feedback=result.optString("error"); updateStatus(); return; }
            if(app.backend!=backend) return;
            if(readError){ feedback=""; readError=false; } app.backendHealthy();
            app.status="Connected · backend "+result.optInt("backendVersion",9)+" · UID "+result.optInt("uid")+" · Android API "+result.optInt("sdk");
            if(!result.optString("protectionWarning").isEmpty()) feedback=result.optString("protectionWarning")+" · protection cannot be checked";
            rawTasks.clear(); backgroundApps.clear(); JSONArray rows=result.optJSONArray("tasks");
            if(rows!=null) for(int i=0;i<rows.length();i++){ JSONObject task=rows.optJSONObject(i); if(task!=null){ try { task.put("recentRank",i); } catch(JSONException ignored){} rawTasks.add(task); } }
            if(captureEntry){ entryTask=result.optJSONObject("previousTask"); captureEntry=false; }
            rebuildTasks(); memoryStatus="RAM loading; tasks ready";if(pendingBulk!=0&&page==Page.APPS){int action=pendingBulk;pendingBulk=0;pendingRefresh=false;if(action==1)closeAll();else closeOthers();return;}
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
    private boolean locked(JSONObject task){ return TaskOrder.mandatory(task)||protectedApps().contains(task.optString("package")) ; }
    private boolean launcherIdle(JSONObject task){ return task.optBoolean("launcher")&&task.optBoolean("synthetic")&&"not-reported".equals(task.optString("memoryState")); }
    private boolean closable(JSONObject task){ return !locked(task)&&!launcherIdle(task); }
    private void closeSelected(){ if(dockZone){ feedback="Select an open app card to close it"; updateStatus(); return; } JSONObject task=selected(); if(task==null) return; if(!closable(task)){ feedback=locked(task)?"This app is protected":"Launcher is not running · Select to restart"; updateStatus(); return; } remove(TaskGroups.expand(task),0); }
    private void closeAll(){
        if(busy){if(refreshingTasks)pendingBulk=1;return;}
        List<JSONObject> eligible=new ArrayList<>(); for(JSONObject task:tasks) if(closable(task)) eligible.addAll(TaskGroups.expand(task));
        remove(eligible,prefs().getBoolean("exitAfterBulk",true)?1:0);
    }
    private void closeOthers(){
        if(busy){if(refreshingTasks)pendingBulk=2;return;}
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
        note(panel,launcher?"Choose whether close actions skip this launcher. Android may reopen its default HOME launcher.":mandatory?"Required keyboard or system-service protection.":"Protected apps are skipped by close actions. Your choice is saved.");
        int previous=controls.size();
        Button toggle=button(panel,mandatory?"Always protected · Close":locked(task)?"Unlock app":"Protect app",()->{
            if(!mandatory){
                String name=task.optString("package"); Set<String> manual=protectedApps();
                if(manual.contains(name)) manual.remove(name); else manual.add(name); prefs().edit().putStringSet("protectedApps",manual).apply();
            }
            protectionPopup.dismiss(); reorder(); refresh();
        }); options.add(toggle);
        Drawable lock=getDrawable(R.drawable.ic_lock); if(lock!=null){ lock.setTint(theme.text); lock.setBounds(0,0,dp(22),dp(22)); toggle.setCompoundDrawables(lock,null,null,null); }
        if(task.optInt("windowCount",1)>1) options.add(button(panel,"Choose window",()->{ protectionPopup.dismiss(); windowGroup=task; detailsPage=0; show(Page.WINDOWS); }));
        if(launcher&&!mandatory) options.add(button(panel,"Restart launcher",()->{ protectionPopup.dismiss(); restartLauncher(task); }));
        options.add(button(panel,QuickAccess.read(prefs()).contains(task.optString("package"))?"Remove from Dock":"Pin to Dock",()->{ protectionPopup.dismiss(); pin(task.optString("package")); }));
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
    private void updateOperationInfo(){ if(operationInfo!=null) operationInfo.setVisibility(View.GONE); }

    private void remove(List<JSONObject> snapshot,int destination){
        ITaskBackend backend=app.backend; if(busy||backend==null&&!snapshot.isEmpty()) return; busy=true; catalogGeneration++; long before=available();
        feedback="Closing "+snapshot.size()+" window(s)…"; updateStatus(); JSONObject previous=entryTask;
        List<JSONObject> progress=Collections.synchronizedList(new ArrayList<>());
        long timeout=Math.min(60000,8000+snapshot.size()*1600L),deadline=SystemClock.uptimeMillis()+timeout;
        app.request("Close apps",timeout,()->{
            JSONArray items=new JSONArray(); Set<String> stopped=new HashSet<>(),attempted=new HashSet<>();
            for(JSONObject task:snapshot){
                if(Thread.currentThread().isInterrupted()||SystemClock.uptimeMillis()>=deadline) break;
                if(task.optBoolean("launcher")&&!stopped.add(task.optString("package"))) continue;
                attempted.add(task.optInt("id")+":"+task.optString("package"));JSONObject result;
                try { configureBackend(backend); result=new JSONObject(task.optBoolean("launcher")?backend.stopLauncher(task.optString("package"),false):backend.removeTask(task.optInt("id"),task.optString("package"))); }
                catch(Exception failure){ result=BackendRequests.error(failure.getClass().getSimpleName()+": "+String.valueOf(failure.getMessage())).put("uncertain",true); }
                result=CloseRetry.retry(backend,task,result,deadline,()->{configureBackend(backend);return null;});
                JSONObject item=OperationResults.item(task,task.optBoolean("launcher")?"Stop launcher":"Close window",result); items.put(item); progress.add(item);
            }for(JSONObject task:snapshot)if(!attempted.contains(task.optInt("id")+":"+task.optString("package"))&&!(task.optBoolean("launcher")&&stopped.contains(task.optString("package")))){JSONObject item=OperationResults.item(task,"Not attempted",new JSONObject().put("uncertain",true).put("warning","Batch stopped before this window"));items.put(item);progress.add(item);} return OperationResults.report(items);
        },report->{
            busy=false;
            if(report.has("error")){
                JSONArray completed; synchronized(progress){ completed=new JSONArray(new ArrayList<>(progress)); }
                Set<String> known=new HashSet<>(),launchers=new HashSet<>(); for(int i=0;i<completed.length();i++){ JSONObject item=completed.optJSONObject(i); if(item!=null){ known.add(item.optInt("id")+":"+item.optString("package")); if(item.optString("action").equals("Stop launcher")) launchers.add(item.optString("package")); } }
                int unknown=0; for(JSONObject task:snapshot) if(!known.contains(task.optInt("id")+":"+task.optString("package"))&&!launchers.contains(task.optString("package"))){ completed.put(OperationResults.item(task,"Outcome unknown",report)); unknown++; }
                if(unknown==0) completed.put(OperationResults.item(new JSONObject(Collections.singletonMap("label","Connection outcome unknown")),"Close apps",report)); report=OperationResults.report(completed);
            }
            app.lastOperation=report; int done=report.optInt("confirmed"); if(done>0){ app.reportMemoryResult(done,before); if(prefs().getBoolean("vibrateClose",false)){android.os.Vibrator vibrator=(android.os.Vibrator)getSystemService(VIBRATOR_SERVICE);if(vibrator!=null&&vibrator.hasVibrator())vibrator.vibrate(android.os.VibrationEffect.createOneShot(20,35));} }
            if(isDestroyed()) return; feedback=report.optInt("failed")>0||report.optInt("unverified")>0?"Some apps could not be closed":""; updateStatus(); updateOperationInfo();
            if(destination!=0){
                if(report.optInt("failed")>0||report.optInt("unverified")>0)Toast.makeText(this,"Some apps could not be closed",Toast.LENGTH_LONG).show();
                app.flushMemoryFeedback();
                if(destination==2&&previous!=null&&backend!=null&&!previous.optBoolean("home")){ returnToPrevious(backend,previous); return; }
                startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); finish();
            } else refresh();
        });
    }
    private void returnToPrevious(ITaskBackend backend,JSONObject previous){
        busy=true; app.request("Return to previous app",5000,()->new JSONObject(backend.openTask(previous.optInt("id"),previous.optString("package"))),result->{
            busy=false; if(isDestroyed()) return; if(!result.has("error")) finish(); else {Toast.makeText(this,"Could not return to previous app",Toast.LENGTH_SHORT).show();startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));finish();}
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
        else if(dockConfig(page)) show(Page.SETTINGS);
        else if(page==Page.ACCENT){ settingsCategory=2; show(Page.SETTINGS); }
        else if(page==Page.CHORD) show(prefs().getBoolean("setupComplete",false)?Page.SHORTCUTS:Page.SETUP);
        else if(page==Page.SETUP){ if(inlineCapture||inlineConfirm){inlineCapture=false;inlineConfirm=false;show(Page.SETUP);return;} if(setupStep>0){ setupStep--; show(Page.SETUP); } else finish(); }
        else show(prefs().getBoolean("setupComplete",false)?Page.SETTINGS:Page.SETUP);
    }
    private void focusMode(View view,boolean controller){
        if(view instanceof Button||cards.contains(view)||dock!=null&&dock.items.contains(view)){ view.setFocusableInTouchMode(controller); if(!controller) view.setActivated(false); }
        if(view instanceof ViewGroup) for(int i=0;i<((ViewGroup)view).getChildCount();i++) focusMode(((ViewGroup)view).getChildAt(i),controller);
    }
    private void setInputMode(boolean controller){
        if(controllerMode==controller) return; boolean searching=pickerActive()&&pickerSearch!=null&&pickerSearch.hasFocus(); controllerMode=controller;
        if(root!=null){View hint=root.findViewWithTag("dockPreviewHint");if(hint instanceof TextView)((TextView)hint).setText(previewHint());}
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
        if(page==Page.SETUP&&setupStep==4&&pickerRows!=null){if(event.getActionMasked()==MotionEvent.ACTION_DOWN){android.graphics.Rect bounds=new android.graphics.Rect();pickerRows.getGlobalVisibleRect(bounds);protectionDrag=bounds.contains((int)event.getRawX(),(int)event.getRawY());protectionTouchX=event.getRawX();protectionTouchY=event.getRawY();}if(event.getActionMasked()==MotionEvent.ACTION_UP&&protectionDrag){float dx=event.getRawX()-protectionTouchX,dy=event.getRawY()-protectionTouchY;protectionDrag=false;if(Math.abs(dx)>dp(48)&&Math.abs(dx)>Math.abs(dy)*1.4f){MotionEvent cancel=MotionEvent.obtain(event);cancel.setAction(MotionEvent.ACTION_CANCEL);super.dispatchTouchEvent(cancel);cancel.recycle();pickerTurn(dx<0?1:-1);return true;}}}
        boolean handled=super.dispatchTouchEvent(event); if(outside&&dock==before){ dockZone=false; before.expand(false); } return handled;
    }

    @Override public void onBackPressed(){ back(); }
    @Override public boolean dispatchKeyEvent(KeyEvent event){
        int key=event.getKeyCode();
        if(ShortcutKeys.eligible(event)||key>=19&&key<=23) setInputMode(true);
        int a=ControllerBindings.code(prefs(),"open"),b=ControllerBindings.code(prefs(),"exit"),x=ControllerBindings.code(prefs(),"close"),y=ControllerBindings.code(prefs(),"clear");
        if(page==Page.SETUP&&inlineConfirm&&(key==96||key==97)){record(event);return true;}
        if(page==Page.SETUP&&inlineCapture&&shortcutServiceEnabled()){boolean reservedSetup=key>=19&&key<=23||key==a||key==b||key==KeyEvent.KEYCODE_BACK||key==KeyEvent.KEYCODE_BUTTON_START||key==KeyEvent.KEYCODE_BUTTON_L1||key==KeyEvent.KEYCODE_BUTTON_R1;if(!reservedSetup&&ShortcutKeys.eligible(event)){record(event);return true;}}
        if(page==Page.COMPLETE&&(key==a||key==KeyEvent.KEYCODE_DPAD_CENTER)){ if(event.getAction()==0&&event.getRepeatCount()==0) finishSetupAnimation(); return true; }
        if(page==Page.BIND&&ControllerBindings.paging(bindingAction)&&(key==a||key==b)){ if(event.getAction()==0&&event.getRepeatCount()==0) show(Page.PAGING); return true; }
        if(page==Page.BIND&&key==KeyEvent.KEYCODE_BUTTON_START){feedback="Start is reserved for Settings";updateStatus();return true;}
        if(page==Page.BIND&&ControllerBindings.BUTTONS.contains(key)){ if(event.getAction()==0&&event.getRepeatCount()==0){ if(ControllerBindings.paging(bindingAction)&&(key==a||key==b)){ feedback="Confirm and Back remain reserved"; updateStatus(); return true; } ControllerBindings.assign(prefs(),bindingAction,key); show(ControllerBindings.paging(bindingAction)?Page.PAGING:Page.MAPPING); } return true; }
        if(page==Page.CHORD && key==KeyEvent.KEYCODE_BUTTON_START && heldKeys.isEmpty() && !capturedKeys.isEmpty()){
            if(event.getAction()==KeyEvent.ACTION_DOWN && event.getRepeatCount()==0 && saveChord.isEnabled()) saveChord.performClick(); return true;
        }
        boolean reserved=key>=19&&key<=23 || key==a || key==b || key==KeyEvent.KEYCODE_BACK || key==KeyEvent.KEYCODE_BUTTON_START;
        if((page==Page.CONFIRM&&(key==96||key==97) || page==Page.CHORD&&!reserved) && ShortcutKeys.eligible(event)){ record(event); return true; }
        if(page==Page.SETUP&&setupStep==4&&key==KeyEvent.KEYCODE_BUTTON_START){if(event.getAction()==0&&event.getRepeatCount()==0)setupNext.performClick();return true;}
        if(page==Page.SETUP&&(key==KeyEvent.KEYCODE_BUTTON_L1||key==KeyEvent.KEYCODE_BUTTON_R1)){
            if(event.getAction()==0&&event.getRepeatCount()==0){ Button target=setupStep==4?(key==KeyEvent.KEYCODE_BUTTON_L1?pickerPrevious:pickerNext):(key==KeyEvent.KEYCODE_BUTTON_L1?setupPrevious:setupNext); if(target!=null&&target.isEnabled()) target.performClick(); } return true;
        }
        if((page==Page.PICKER||page==Page.RESULTS||page==Page.WINDOWS||page==Page.MEMORY)&&key!=a&&key!=b&&(key==ControllerBindings.code(prefs(),"page_previous")||key==ControllerBindings.code(prefs(),"page_next"))){ if(event.getAction()==0&&event.getRepeatCount()==0){ Button target=key==ControllerBindings.code(prefs(),"page_previous")?pagePrevious:pageNext; if(target!=null&&target.isEnabled()) target.performClick(); } return true; }
        if(pickerActive()&&pickerSearch!=null&&pickerSearch.hasFocus()){
            if(key==KeyEvent.KEYCODE_DPAD_DOWN){ if(event.getAction()==0){ getSystemService(android.view.inputmethod.InputMethodManager.class).hideSoftInputFromWindow(pickerSearch.getWindowToken(),0); pickerSearch.clearFocus(); if(!pickerOptions.isEmpty()) focusControl(controls.indexOf(pickerOptions.get(0))); } return true; }
            if(key!=b&&key!=KeyEvent.KEYCODE_BACK) return super.dispatchKeyEvent(event);
        }
        if(key==KeyEvent.KEYCODE_BUTTON_START&&page!=Page.SETUP&&page!=Page.CHORD&&page!=Page.BIND&&page!=Page.CONFIRM){if(event.getAction()==0&&event.getRepeatCount()==0&&page!=Page.SETTINGS)show(Page.SETTINGS);return true;}
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
        if(index<0||index>=controls.size()) return;
        if(dockConfig(page)&&!controls.get(index).isEnabled()){android.view.ViewParent group=controls.get(index).getParent();int nearest=-1,distance=Integer.MAX_VALUE;for(int i=0;i<controls.size();i++)if(controls.get(i).isEnabled()&&controls.get(i).getParent()==group&&Math.abs(i-index)<distance){nearest=i;distance=Math.abs(i-index);}if(nearest<0)return;index=nearest;}controlIndex=index;
        for(int i=0;i<controls.size();i++) controls.get(i).setActivated(controllerMode&&i==index);
        Button target=controls.get(index); target.requestFocus(); target.requestRectangleOnScreen(new android.graphics.Rect(0,0,target.getWidth(),target.getHeight()),false);
    }
    private void navigateDockSettings(int direction){
        if(controls.isEmpty())return;if(controlIndex<0||controlIndex>=controls.size()){focusControl(0);return;}View from=controls.get(controlIndex);int[] xy=new int[2];from.getLocationOnScreen(xy);float x=xy[0]+from.getWidth()/2f,y=xy[1]+from.getHeight()/2f;int best=-1;double score=Double.MAX_VALUE;
        for(int i=0;i<controls.size();i++){View to=controls.get(i);if(i==controlIndex||!to.isEnabled()||!to.isShown())continue;int[] pos=new int[2];to.getLocationOnScreen(pos);float dx=pos[0]+to.getWidth()/2f-x,dy=pos[1]+to.getHeight()/2f-y,major=(direction==1?dx:direction==-1?-dx:direction==2?dy:-dy),minor=Math.abs(Math.abs(direction)==1?dy:dx);if(major<4)continue;boolean overlap=Math.abs(direction)==1?pos[1]<xy[1]+from.getHeight()&&pos[1]+to.getHeight()>xy[1]:pos[0]<xy[0]+from.getWidth()&&pos[0]+to.getWidth()>xy[0];double value=(overlap?0:10000)+major+minor*.2;if(value<score){score=value;best=i;}}
        if(best>=0){focusControl(best);View target=controls.get(best);target.requestRectangleOnScreen(new android.graphics.Rect(0,0,target.getWidth(),target.getHeight()),false);}
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
            if(pickerActive()&&controlIndex>=0&&controlIndex<controls.size()&&pickerOptions.contains(controls.get(controlIndex))){int index=pickerOptions.indexOf(controls.get(controlIndex)),next=index;if(setupProtection&&direction==1&&(index%4==3||index==pickerOptions.size()-1)&&pickerNext.isEnabled()){int row=index/4;pickerTurn(1);focusControl(controls.indexOf(pickerOptions.get(Math.min(row*4,pickerOptions.size()-1))));return;}if(setupProtection&&direction==-1&&index%4==0&&pickerPrevious.isEnabled()){int row=index/4;pickerTurn(-1);focusControl(controls.indexOf(pickerOptions.get(Math.min(row*4+3,pickerOptions.size()-1))));return;}if(direction==-2){if(index<4){if(setupProtection){focusControl(controls.indexOf(pickerSearchButton));}else{pickerSearch.requestFocus();controlIndex=-1;}return;}next-=4;}else if(direction==2){if(index+4>=pickerOptions.size()){focusControl(controls.indexOf(setupProtection?setupNext:pickerPrevious));return;}next+=4;}else if(direction==-1&&index%4>0)next--;else if(direction==1&&index%4<3&&index+1<pickerOptions.size())next++;focusControl(controls.indexOf(pickerOptions.get(next)));return;}
            if(dockConfig(page)){navigateDockSettings(direction);return;}
            if(page==Page.SETUP&&setupStep==3&&direction==1&&controlIndex>=0&&controlIndex<controls.size()&&inside(controls.get(controlIndex),formMain)&&!colorDots.isEmpty()){View current=controls.get(controlIndex);int adjacent=controlIndex+1;if(current.getParent() instanceof LinearLayout&&((LinearLayout)current.getParent()).getOrientation()==LinearLayout.HORIZONTAL&&adjacent<controls.size()&&controls.get(adjacent).getParent()==current.getParent()){focusControl(adjacent);return;}setupAccentReturn=controlIndex;focusControl(controls.indexOf(colorDots.get(0)));return;}
            if(page==Page.SETUP&&(setupStep==1||setupStep==3)&&controlIndex>=0&&controlIndex<controls.size()&&setupThemes.contains(controls.get(controlIndex))){ int index=setupThemes.indexOf(controls.get(controlIndex)),next=index; if(direction==-2){ if(index<2){ focusControl(0); return; } next-=2; } else if(direction==2){ if(index+2>=setupThemes.size()){ focusControl(controls.indexOf(setupThemes.get(setupThemes.size()-1))+1); return; } next+=2; } else if(direction==-1&&index%2==1) next--; else if(direction==1&&index%2==0) next++; focusControl(controls.indexOf(setupThemes.get(next))); return; }
            if(pickerActive()&&pickerSearch!=null&&pickerSearch.hasFocus()){ if(direction==2&&!pickerOptions.isEmpty()){ pickerSearch.clearFocus(); getSystemService(android.view.inputmethod.InputMethodManager.class).hideSoftInputFromWindow(pickerSearch.getWindowToken(),0); focusControl(controls.indexOf(pickerOptions.get(0))); } return; }
            if((page==Page.SETTINGS||page==Page.SETUP)&&controlIndex>=0&&controlIndex<controls.size()&&colorDots.contains(controls.get(controlIndex))){ colorColumns=((GridLayout)colorDots.get(0).getParent()).getColumnCount(); int index=colorDots.indexOf(controls.get(controlIndex)),next=index; if(direction==-1){ if(index%colorColumns==0){ focusControl(page==Page.SETUP?setupAccentReturn:1+settingsCategory); return; } next--; } else if(direction==1&&index%colorColumns<colorColumns-1) next++; else if(direction==-2) next-=colorColumns; else if(direction==2) next+=colorColumns; if(next<0) focusControl(controls.indexOf(colorDots.get(0))-1); else if(next<colorDots.size()) focusControl(controls.indexOf(colorDots.get(next)));else if(page==Page.SETUP&&direction==2)focusControl(controls.indexOf(setupNext));else if(page==Page.SETTINGS&&direction==2){int following=controls.indexOf(colorDots.get(colorDots.size()-1))+1;if(following<controls.size())focusControl(following);} return; }
            if(page==Page.SETTINGS&&controlIndex>=0&&controlIndex<controls.size()&&controls.get(controlIndex).getParent() instanceof OptionGrid){OptionGrid choices=(OptionGrid)controls.get(controlIndex).getParent();int index=choices.indexOfChild(controls.get(controlIndex)),columns=choices.getColumnCount(),next=index+(direction==1?1:direction==-1?-1:direction==2?columns:-columns);if(direction==-1&&index%columns==0){focusControl(1+settingsCategory);return;}if(direction==-2&&index<columns){focusControl(controls.indexOf((Button)choices.getChildAt(0))-1);return;}if(next>=0&&next<choices.getChildCount())focusControl(controls.indexOf((Button)choices.getChildAt(next)));return;}
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
            if(direction==-2){ boolean rightCorner=landscape()&&"thumb".equals(prefs().getString("dockStyle","thumb"))&&dock.items.size()>0&&dockIndex>=QuickAccess.leftCount(prefs(),dock.items.size()); dockZone=false; toolZone=rightCorner||cards.isEmpty(); if(rightCorner) toolIndex=tools.indexOf(ramButton); if("focus".equals(prefs().getString("dockBehavior","always"))) dock.expand(false); markSelection(); }
            else if(direction==1||direction==-1){ dockIndex=Math.max(0,Math.min(dock.items.size()-1,dockIndex+direction)); dock.focusItem(dockIndex); } return;
        }
        if(toolZone&&toolIndex==tools.indexOf(ramButton)){
            if(direction==-2){ toolIndex=tools.indexOf(findSettingsTool()); markSelection(); return; }
            if((direction==1||direction==2)&&dock!=null){ dockZone=true; toolZone=false; dockIndex="thumb".equals(prefs().getString("dockStyle","thumb"))?Math.min(Math.max(0,dock.items.size()-1),QuickAccess.leftCount(prefs(),dock.items.size())):Math.max(0,dock.items.size()-1); dock.expand(true); markSelection(); return; }
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
