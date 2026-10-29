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
    enum Page { APPS, SETUP, CONNECT, CHORD, CONFIRM }
    private TaskDeckApp app;
    private Page page;
    private DeckTheme theme;
    private LinearLayout root,cardRow;
    private HorizontalScrollView rail;
    private TextView status,memory,recordLive,recordSaved;
    private Button saveChord;
    private PopupWindow protectionPopup;
    private ScrollView pageScroll;
    private boolean pendingRefresh;
    private final List<JSONObject> tasks=new ArrayList<>();
    private final List<View> cards=new ArrayList<>();
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
        Button b=new Button(this); b.setText(value); b.setAllCaps(false); b.setTextSize(14); b.setTextColor(theme.text); b.setFocusableInTouchMode(true); b.setDefaultFocusHighlightEnabled(false);
        StateListDrawable bg=new StateListDrawable(); GradientDrawable focused=rounded(theme.selected); focused.setStroke(dp(3),theme.accent);
        GradientDrawable selected=rounded(theme.selected); selected.setStroke(dp(1),theme.accent);
        bg.addState(new int[]{android.R.attr.state_focused},focused); bg.addState(new int[]{android.R.attr.state_pressed},rounded(theme.selected)); bg.addState(new int[]{android.R.attr.state_selected},selected); bg.addState(new int[]{},rounded(theme.raised));
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
        super.onCreate(state); app=(TaskDeckApp)getApplication();
        if(Build.VERSION.SDK_INT>=30) getWindow().setDecorFitsSystemWindows(false);
        else getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        Page initial=prefs().getBoolean("setupComplete",false)?Page.APPS:Page.SETUP;
        if(state!=null) try { initial=Page.valueOf(state.getString("page",initial.name())); } catch(IllegalArgumentException ignored){}
        if(initial==Page.CHORD || initial==Page.CONFIRM) initial=Page.SETUP;
        show(initial);
    }
    @Override public void onSaveInstanceState(Bundle out){ out.putString("page",page.name()); super.onSaveInstanceState(out); }
    @Override protected void onResume(){
        super.onResume(); TaskDeckApp.recordingShortcut=page==Page.CHORD || page==Page.CONFIRM;
        app.changed=this::connectionChanged; updateStatus();
        if(app.backend!=null) refresh(); else app.connect(false);
    }
    @Override protected void onPause(){ app.changed=null; TaskDeckApp.recordingShortcut=false; stopStick(); super.onPause(); }
    @Override protected void onNewIntent(Intent intent){ super.onNewIntent(intent); setIntent(intent); show(prefs().getBoolean("setupComplete",false)?Page.APPS:Page.SETUP); if(app.backend!=null) refresh(); }
    private void connectionChanged(){
        if(isFinishing() || isDestroyed()) return;
        if(app.backend==null){ tasks.clear(); selection.update(Collections.emptyList()); if(page==Page.APPS) renderCards(); updateStatus(); }
        else { updateStatus(); refresh(); }
    }
    private void show(Page next){
        int restoreY=page==Page.SETUP && next==Page.SETUP && pageScroll!=null?pageScroll.getScrollY():0;
        int restoreFocus=page==Page.SETUP && next==Page.SETUP?controls.indexOf(getCurrentFocus()):-1;
        if(protectionPopup!=null) protectionPopup.dismiss(); pageScroll=null;
        stopStick(); feedback=""; TaskDeckApp.recordingShortcut=next==Page.CHORD || next==Page.CONFIRM; page=next; toolZone=false;
        controls.clear(); tools.clear(); cards.clear(); cardRow=null; rail=null; status=null; memory=null;
        theme=DeckTheme.named(prefs().getString("theme","dark"));
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(theme.background); root.setPadding(dp(20),dp(12),dp(20),dp(12));
        root.setOnApplyWindowInsetsListener((view,insets)->{
            if(Build.VERSION.SDK_INT>=30){ android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars()); view.setPadding(dp(20)+bars.left,dp(10)+bars.top,dp(20)+bars.right,dp(10)+bars.bottom); }
            else view.setPadding(dp(20)+insets.getSystemWindowInsetLeft(),dp(10)+insets.getSystemWindowInsetTop(),dp(20)+insets.getSystemWindowInsetRight(),dp(10)+insets.getSystemWindowInsetBottom());
            return insets;
        }); setContentView(root);
        if(page==Page.APPS) appsPage(); else if(page==Page.SETUP) setupPage(); else if(page==Page.CONNECT) connectPage(); else recordPage();
        if(restoreY>0 && pageScroll!=null){ ScrollView scroll=pageScroll; main.post(()->scroll.scrollTo(0,restoreY)); }
        if(restoreFocus>=0 && restoreFocus<controls.size()){ Button previous=controls.get(restoreFocus); main.post(previous::requestFocus); }
        root.requestApplyInsets();
    }
    private LinearLayout scrollBody(String title){
        LinearLayout heading=row(root); text(heading,title,25,false).setTypeface(null,Typeface.BOLD); button(heading,"Back",this::back);
        ScrollView scroll=new ScrollView(this); pageScroll=scroll; root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout body=new LinearLayout(this); body.setOrientation(LinearLayout.VERTICAL); scroll.addView(body); return body;
    }
    private void appsPage(){
        LinearLayout header=row(root); TextView title=text(header,"TaskDeck",24,false); title.setTypeface(null,Typeface.BOLD); title.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1));
        LinearLayout toolRow=getResources().getConfiguration().screenWidthDp<600?row(root):header;
        tools.add(button(toolRow,"Refresh",this::refresh)); tools.add(button(toolRow,"Connect",()->show(Page.CONNECT))); tools.add(button(toolRow,"Setup",()->show(Page.SETUP)));
        LinearLayout metrics=row(root); if(getResources().getConfiguration().screenWidthDp<600) metrics.setOrientation(LinearLayout.VERTICAL);
        status=text(metrics,"",13,true); memory=text(metrics,"",13,true);
        if(metrics.getOrientation()==LinearLayout.HORIZONTAL){ status.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1)); status.setMaxLines(1); status.setEllipsize(android.text.TextUtils.TruncateAt.END); }
        updateStatus(); updateMemory();
        rail=new HorizontalScrollView(this); rail.setFillViewport(false); rail.setClipToPadding(false); rail.setHorizontalScrollBarEnabled(false);
        root.addView(rail,new LinearLayout.LayoutParams(-1,0,1)); cardRow=new LinearLayout(this); cardRow.setGravity(Gravity.CENTER_VERTICAL); rail.addView(cardRow,new HorizontalScrollView.LayoutParams(-2,-1));
        renderCards();
        LinearLayout actions=row(root);
        if(getResources().getConfiguration().screenWidthDp<600){
            button(actions,"A · Open",this::openSelected); button(actions,"X · Close",this::closeSelected); actions=row(root);
        } else { button(actions,"A · Open",this::openSelected); button(actions,"X · Close",this::closeSelected); }
        button(actions,"Y · Close all",this::closeAll); button(actions,"B · Exit",this::finish);
        text(root,"← / → or stick: apps    ↑: tools    Select / long press: protect",12,true);
    }
    private void renderCards(){
        if(cardRow==null) return;
        cardRow.removeAllViews(); cards.clear();
        if(tasks.isEmpty()){ text(cardRow,app.backend==null?"Connect Shizuku to load your apps.":"No recent apps returned.",20,true); return; }
        int width=dp(Math.max(156,Math.min(208,(getResources().getConfiguration().screenWidthDp-64)/4)));
        boolean compact=getResources().getConfiguration().screenHeightDp<440;
        for(JSONObject task:tasks){
            LinearLayout card=new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setGravity(Gravity.CENTER); card.setPadding(dp(12),dp(compact?6:14),dp(12),dp(compact?6:14)); card.setFocusableInTouchMode(true); card.setDefaultFocusHighlightEnabled(false);
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(width,-1); p.setMargins(dp(2),dp(8),dp(12),dp(8)); cardRow.addView(card,p);
            ImageView icon=new ImageView(this); Drawable drawable=null;
            try { drawable=getPackageManager().getApplicationIcon(task.optString("package")); } catch(PackageManager.NameNotFoundException ignored){}
            if(drawable==null){ drawable=getDrawable(R.drawable.ic_tasks); if(drawable!=null) drawable.setTint(theme.accent); } icon.setImageDrawable(drawable); icon.setContentDescription(task.optString("label")); card.addView(icon,new LinearLayout.LayoutParams(dp(compact?40:60),dp(compact?40:60)));
            TextView name=text(card,task.optString("label"),compact?16:18,false); name.setGravity(Gravity.CENTER); name.setMaxLines(compact?1:2); name.setEllipsize(android.text.TextUtils.TruncateAt.END); name.setTypeface(null,Typeface.BOLD);
            long kb=task.optLong("pssKb",-1); String state=task.optString("memoryState","unavailable");
            String ram=kb>=0?"~"+Math.round(kb/1024d)+" MiB app RAM"+(state.equals("shared")?" · shared":""):state.equals("not-reported")?"No process in snapshot":"RAM unavailable";
            TextView usage=text(card,ram,12,true); usage.setGravity(Gravity.CENTER);
            boolean locked=locked(task);
            TextView badge=text(card,locked?"Protected":"X to close",12,true); badge.setTextColor(locked?theme.muted:theme.accent); badge.setGravity(Gravity.CENTER);
            if(compact){ name.setPadding(0,dp(5),0,dp(2)); usage.setPadding(0,dp(2),0,dp(2)); badge.setPadding(0,dp(2),0,dp(2)); }
            if(locked){ Drawable lock=getDrawable(R.drawable.ic_lock); if(lock!=null){ lock.setTint(theme.accent); lock.setBounds(0,0,dp(18),dp(18)); badge.setCompoundDrawables(lock,null,null,null); badge.setCompoundDrawablePadding(dp(5)); } }
            card.setOnClickListener(v->{ selection.select(task.optInt("id")); toolZone=false; markSelection(); openSelected(); });
            card.setOnLongClickListener(v->{ selection.select(task.optInt("id")); toolZone=false; markSelection(); protectSelected(); return true; });
            cards.add(card);
        }
        markSelection();
    }
    private void markSelection(){
        if(page!=Page.APPS) return;
        for(int i=0;i<cards.size();i++){
            boolean chosen=i==selection.index(); GradientDrawable bg=rounded(chosen?theme.selected:theme.surface);
            bg.setStroke(dp(chosen&&!toolZone?4:1),chosen?theme.accent:theme.raised); cards.get(i).setBackground(bg); cards.get(i).setSelected(chosen);
        }
        if(!toolZone && !cards.isEmpty()){
            View card=cards.get(selection.index()); card.requestFocus();
            main.post(()->{ if(page==Page.APPS && rail!=null && card.getParent()==cardRow) rail.smoothScrollTo(Math.max(0,card.getLeft()-(rail.getWidth()-card.getWidth())/2),0); });
        }
    }
    private void setupPage(){
        LinearLayout body=scrollBody(prefs().getBoolean("setupComplete",false)?"TaskDeck settings":"Welcome to TaskDeck");
        text(body,"Set up once. Your controller opens the app deck directly afterwards.",15,true);
        LinearLayout connection=section(body,"1 · Connection", "Shizuku provides access to Android's recent tasks. Root is not required.");
        status=text(connection,app.status,14,true); button(connection,app.backend==null?"Connect Shizuku":"Connection details",()->show(Page.CONNECT));
        LinearLayout macro=section(body,"2 · Controller", "The selected app has a colored border. Use D-pad left/right or the left stick; A opens, X closes, Y closes all eligible tasks, B exits. Closing happens immediately.");
        button(macro,"Calibrate printed A",()->show(Page.CONFIRM));
        text(macro,"Shortcut: "+ShortcutKeys.labels(ShortcutKeys.decode(prefs().getString("shortcutChord",""))),15,false);
        button(macro,"Record button combination",()->show(Page.CHORD));
        button(macro,prefs().getBoolean("shortcutEnabled",false)?"Disable shortcut":"Enable saved shortcut",()->{
            if(ShortcutKeys.decode(prefs().getString("shortcutChord","")).isEmpty()){ show(Page.CHORD); return; }
            prefs().edit().putBoolean("shortcutEnabled",!prefs().getBoolean("shortcutEnabled",false)).apply(); show(Page.SETUP);
        });
        text(macro,"Optional Accessibility access intercepts the completing button of your chosen combination to open TaskDeck. Partial combinations pass through. No screen content or typed text is read, stored or sent. Enable this only if you want a global controller shortcut.",14,true);
        button(macro,"Enable controller shortcut service",()->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        LinearLayout appearance=section(body,"3 · Appearance", "Choose your preferred contrast and background."); LinearLayout themes=row(appearance);
        for(String value:new String[]{"light","dark","oled"}){
            Button option=button(themes,value.equals("oled")?"OLED":value.equals("light")?"Light":"Dark",()->{ prefs().edit().putString("theme",value).apply(); show(Page.SETUP); });
            if(value.equals(prefs().getString("theme","dark"))){ option.setSelected(true); option.setTextColor(theme.accent); option.setTypeface(null,Typeface.BOLD); }
        }
        LinearLayout ram=section(body,"4 · RAM and shortcuts", "Per-app RAM is an approximate PSS sample across its processes, not the amount a task will release. Multiple cards for one app share that value. Missing samples are marked unavailable. After closing, TaskDeck reports the measured change in total available RAM; Android and other apps can affect it.");
        button(ram,"Enable RAM notifications",()->{ prefs().edit().putBoolean("ramNotifications",true).apply(); notificationPermission(); });
        button(ram,"Disable RAM notifications",()->{ prefs().edit().putBoolean("ramNotifications",false).apply(); feedback="RAM notifications disabled"; updateStatus(); });
        button(ram,"Show notification shortcut",this::shortcutNotification);
        text(ram,"You can also add the TaskDeck tile in Android Quick Settings. Long-press an app card or press Select to protect it. Wave, HOME launchers, keyboards and essential services stay protected. Settings can be closed unless you lock it.",14,true);
        button(body,"Finish setup · Open app deck",()->{ prefs().edit().putBoolean("setupComplete",true).apply(); show(Page.APPS); if(app.backend!=null) refresh(); });
        text(body,"TaskDeck 0.2.0 · SilasDV · vibecoded",12,true);
    }
    private void connectPage(){
        LinearLayout body=scrollBody("Connection"); LinearLayout card=section(body,app.backend==null?"Connect to Shizuku":"Shizuku connected", "TaskDeck uses a separate privileged service to read and close recent tasks.");
        status=text(card,app.status,16,false);
        text(card,"1. Install and start Shizuku.\n2. Return here and choose Authorize / Connect.\n3. Approve TaskDeck in Shizuku.\n\nWithout root, restart Shizuku after a device reboot. Pairing alone does not start the service.",16,true);
        button(card,"Open Shizuku",()->{ Intent intent=getPackageManager().getLaunchIntentForPackage("moe.shizuku.privileged.api"); if(intent!=null) startActivity(intent); else { feedback="Install Shizuku from shizuku.rikka.app"; updateStatus(); } });
        button(card,"Authorize / Connect",()->app.connect(true));
        button(body,prefs().getBoolean("setupComplete",false)?"Open app deck":"Continue setup",()->{ show(prefs().getBoolean("setupComplete",false)?Page.APPS:Page.SETUP); if(app.backend!=null) refresh(); });
    }
    private void recordPage(){
        heldKeys.clear(); capturedKeys.clear(); captureDevice=null;
        LinearLayout body=scrollBody(page==Page.CHORD?"Record controller combination":"Calibrate confirm");
        text(body,page==Page.CHORD?"Hold the buttons together, then release them. TaskDeck records the largest simultaneous combination. Save when the displayed buttons are correct. Up to four digital buttons are supported.":"Press the button printed A. This calibrates A/B and X/Y for the app deck.",17,false);
        LinearLayout panel=section(body,"Detected buttons", "Names and scan codes are what Android actually reports; rear-button labels may differ. Buttons must come from the same input device.");
        recordLive=text(panel,"Held now: none",18,false); recordSaved=text(panel,"Recorded: none",18,false);
        if(page==Page.CHORD){
            text(body,"Choose a combination unused by your game or other macro. Partial button presses remain available to other apps. The first button can already reach the game before the combination completes. If both rear buttons report the same Android key and scan code, assign different outputs in Retroid's button settings.",14,true);
            saveChord=button(body,"Save combination · Start",()->{
                if(capturedKeys.isEmpty() || !heldKeys.isEmpty() || capturedKeys.size()>4) return;
                prefs().edit().putString("shortcutChord",ShortcutKeys.encode(capturedKeys)).putBoolean("shortcutEnabled",true).apply(); show(Page.SETUP);
            }); saveChord.setEnabled(false);
            button(body,"Record again",()->show(Page.CHORD));
        }
    }
    private void record(KeyEvent event){
        String key=ShortcutKeys.id(event);
        if(page==Page.CONFIRM){
            if(event.getAction()==KeyEvent.ACTION_DOWN && event.getRepeatCount()==0 && (event.getKeyCode()==96 || event.getKeyCode()==97)){
                prefs().edit().putBoolean("swapped",event.getKeyCode()==97).apply(); feedback="Confirm button calibrated"; show(Page.SETUP);
            } return;
        }
        if(event.getAction()==KeyEvent.ACTION_DOWN && event.getRepeatCount()==0){
            if(heldKeys.isEmpty()) captureDevice=event.getDeviceId();
            if(!Objects.equals(captureDevice,event.getDeviceId())){ recordLive.setText("Use buttons from one controller"); return; }
            heldKeys.add(key);
            if(heldKeys.size()>=capturedKeys.size()){ capturedKeys.clear(); capturedKeys.addAll(heldKeys); }
        } else if(event.getAction()==KeyEvent.ACTION_UP && Objects.equals(captureDevice,event.getDeviceId())) heldKeys.remove(key);
        recordLive.setText("Held now: "+(heldKeys.isEmpty()?"none":ShortcutKeys.labels(heldKeys)));
        recordSaved.setText("Recorded: "+ShortcutKeys.labels(capturedKeys));
        saveChord.setEnabled(!capturedKeys.isEmpty() && heldKeys.isEmpty() && capturedKeys.size()<=4);
    }
    private void updateStatus(){
        if(status==null) return;
        if(!feedback.isEmpty()) status.setText(feedback);
        else if(page==Page.APPS) status.setText(app.backend==null?"Not connected · open Connect":tasks.size()+" recent tasks"+(busy?" · updating…":""));
        else status.setText(app.status);
    }
    private long available(){ ActivityManager.MemoryInfo info=new ActivityManager.MemoryInfo(); getSystemService(ActivityManager.class).getMemoryInfo(info); return info.availMem; }
    private void updateMemory(){
        if(memory==null) return;
        ActivityManager.MemoryInfo info=new ActivityManager.MemoryInfo(); getSystemService(ActivityManager.class).getMemoryInfo(info);
        memory.setText("Available RAM  "+(info.availMem/(1024*1024))+" / "+(info.totalMem/(1024*1024))+" MiB");
    }
    private void refresh(){
        updateMemory(); ITaskBackend backend=app.backend; if(backend==null) return; if(busy){ pendingRefresh=true; return; }
        busy=true; updateStatus();
        app.worker.execute(()->{
            JSONObject data; try { backend.setProtectedPackages(protectedArray()); data=new JSONObject(backend.readTasks()); } catch(Exception e){ data=new JSONObject(); try { data.put("error","Backend unavailable: "+e.getClass().getSimpleName()); } catch(JSONException ignored){} }
            final JSONObject result=data;
            main.post(()->{
                busy=false; if(isDestroyed() || app.backend!=backend) return;
                if(result.has("error")){ feedback=result.optString("error"); updateStatus(); return; }
                app.status="Connected · backend UID "+result.optInt("uid")+" · Android API "+result.optInt("sdk");
                tasks.clear(); List<Integer> ids=new ArrayList<>(); JSONArray rows=result.optJSONArray("tasks");
                if(rows!=null) for(int i=0;i<rows.length();i++){ JSONObject task=rows.optJSONObject(i); if(task!=null){ tasks.add(task); ids.add(task.optInt("id")); } }
                selection.update(ids); if(page==Page.APPS) renderCards(); updateStatus();
                if(pendingRefresh){ pendingRefresh=false; refresh(); }
            });
        });
    }
    private JSONObject selected(){ Integer id=selection.id(); if(id!=null) for(JSONObject task:tasks) if(task.optInt("id")==id) return task; return null; }
    private void openSelected(){
        JSONObject task=selected(); ITaskBackend backend=app.backend; if(task==null || backend==null || busy) return;
        busy=true;
        app.worker.execute(()->{
            String error=null; try { error=new JSONObject(backend.openTask(task.optInt("id"),task.optString("package"))).optString("error",null); } catch(Exception e){ error="Cannot open task: "+e.getClass().getSimpleName(); }
            final String failure=error; main.post(()->{ busy=false; if(isDestroyed()) return; if(failure==null) finish(); else { feedback=failure; updateStatus(); } });
        });
    }
    private Set<String> protectedApps(){ return new HashSet<>(prefs().getStringSet("protectedApps",Collections.emptySet())); }
    private String[] protectedArray(){ return protectedApps().toArray(new String[0]); }
    private boolean locked(JSONObject task){ return task.optBoolean("mandatoryProtected",!task.optBoolean("removable") && !task.optBoolean("userProtected")) || protectedApps().contains(task.optString("package")); }
    private void closeSelected(){ JSONObject task=selected(); if(task==null) return; if(locked(task)){ feedback="This app is protected"; updateStatus(); return; } remove(Collections.singletonList(task)); }
    private void closeAll(){ List<JSONObject> eligible=new ArrayList<>(); for(JSONObject task:tasks) if(!locked(task)) eligible.add(task); if(!eligible.isEmpty()) remove(eligible); }
    private void protectSelected(){
        JSONObject task=selected(); if(task==null) return;
        boolean mandatory=task.optBoolean("mandatoryProtected",!task.optBoolean("removable") && !task.optBoolean("userProtected"));
        final Button[] option=new Button[1];
        LinearLayout panel=new LinearLayout(this){
            @Override public boolean dispatchKeyEvent(KeyEvent event){
                int a=prefs().getBoolean("swapped",false)?97:96,b=prefs().getBoolean("swapped",false)?96:97;
                if(event.getKeyCode()==b || event.getKeyCode()==KeyEvent.KEYCODE_BACK){ if(event.getAction()==KeyEvent.ACTION_DOWN) protectionPopup.dismiss(); return true; }
                if(event.getKeyCode()==a || event.getKeyCode()==KeyEvent.KEYCODE_DPAD_CENTER){ if(event.getAction()==KeyEvent.ACTION_DOWN && event.getRepeatCount()==0) option[0].performClick(); return true; }
                return super.dispatchKeyEvent(event);
            }
        };
        panel.setOrientation(LinearLayout.VERTICAL); panel.setPadding(dp(24),dp(18),dp(24),dp(18)); panel.setBackground(rounded(theme.surface));
        text(panel,task.optString("label"),21,false); text(panel,mandatory?"Required protection for launcher, keyboard or system service.":"Protected apps are skipped by X and Y. Your choice is saved.",14,true);
        boolean manual=protectedApps().contains(task.optString("package"));
        int previousControls=controls.size();
        option[0]=button(panel,mandatory?"Always protected · Close":manual?"Unlock app":"Protect app",()->{
            if(!mandatory){ Set<String> names=protectedApps(); String name=task.optString("package"); if(names.contains(name)) names.remove(name); else names.add(name); prefs().edit().putStringSet("protectedApps",names).apply(); }
            protectionPopup.dismiss(); renderCards(); refresh();
        });
        while(controls.size()>previousControls) controls.remove(controls.size()-1);
        Drawable lock=getDrawable(R.drawable.ic_lock); if(lock!=null){ lock.setTint(theme.accent); lock.setBounds(0,0,dp(22),dp(22)); option[0].setCompoundDrawables(lock,null,null,null); option[0].setCompoundDrawablePadding(dp(8)); }
        protectionPopup=new PopupWindow(panel,Math.min(dp(360),getResources().getDisplayMetrics().widthPixels-dp(32)),-2,true);
        protectionPopup.setBackgroundDrawable(rounded(theme.surface)); protectionPopup.setOutsideTouchable(true); protectionPopup.setElevation(dp(16)); protectionPopup.setOnDismissListener(this::markSelection);
        protectionPopup.showAtLocation(root,Gravity.CENTER,0,0); option[0].requestFocus();
    }
    private void remove(List<JSONObject> snapshot){
        ITaskBackend backend=app.backend; if(backend==null || busy) return;
        busy=true; long before=available(); feedback="Closing "+snapshot.size()+" task(s)…"; updateStatus();
        app.worker.execute(()->{
            int closed=0,failed=0; String lastError="";
            for(JSONObject task:snapshot){
                try { backend.setProtectedPackages(protectedArray()); JSONObject result=new JSONObject(backend.removeTask(task.optInt("id"),task.optString("package"))); if(result.has("error")){ failed++; lastError=result.optString("error"); } else closed++; }
                catch(Exception e){ failed++; lastError=e.getClass().getSimpleName(); }
            }
            final int done=closed,errors=failed; final String problem=lastError;
            main.post(()->{
                busy=false; if(isDestroyed()) return;
                feedback=done+" task(s) closed"+(errors>0?" · "+errors+" failed: "+problem:""); updateStatus(); refresh();
                if(done>0) main.postDelayed(()->{
                    if(isDestroyed()) return;
                    long after=available(),delta=(after-before)/(1024*1024);
                    feedback=done+" closed · RAM available "+(after/(1024*1024))+" MiB · change "+(delta>=0?"+":"")+delta+" MiB"+(errors>0?" · "+errors+" failed":""); updateStatus(); updateMemory(); ramNotification(done,after,delta);
                },700);
            });
        });
    }
    private void notificationPermission(){
        if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},1);
        else { feedback="Notifications enabled"; updateStatus(); }
    }
    private boolean notificationsAllowed(){ return Build.VERSION.SDK_INT<33 || checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED; }
    private void ramNotification(int count,long after,long delta){
        if(!prefs().getBoolean("ramNotifications",true) || !notificationsAllowed()) return;
        NotificationManager manager=getSystemService(NotificationManager.class); manager.createNotificationChannel(new NotificationChannel("ram","Task results",NotificationManager.IMPORTANCE_LOW));
        manager.notify(2,new Notification.Builder(this,"ram").setSmallIcon(R.drawable.ic_tasks).setContentTitle(count+" task(s) closed")
            .setContentText("RAM available: "+after/(1024*1024)+" MiB · change "+(delta>=0?"+":"")+delta+" MiB").setTimeoutAfter(7000).setAutoCancel(true).build());
    }
    private void shortcutNotification(){
        if(!notificationsAllowed()){ prefs().edit().putBoolean("pendingNotificationShortcut",true).apply(); notificationPermission(); return; }
        NotificationManager manager=getSystemService(NotificationManager.class); manager.createNotificationChannel(new NotificationChannel("switcher","TaskDeck shortcut",NotificationManager.IMPORTANCE_LOW));
        PendingIntent open=PendingIntent.getActivity(this,0,new Intent(this,SwitcherActivity.class),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        manager.notify(1,new Notification.Builder(this,"switcher").setSmallIcon(R.drawable.ic_tasks).setContentTitle("TaskDeck").setContentText("Open your app deck").setContentIntent(open).setOngoing(true).build());
    }
    @Override public void onRequestPermissionsResult(int code,String[] permissions,int[] grants){ super.onRequestPermissionsResult(code,permissions,grants); if(code==1 && grants.length>0 && grants[0]==PackageManager.PERMISSION_GRANTED){ if(prefs().getBoolean("pendingNotificationShortcut",false)){ prefs().edit().remove("pendingNotificationShortcut").apply(); shortcutNotification(); } feedback="Notifications enabled"; updateStatus(); } }
    private void back(){ if(page==Page.APPS) finish(); else if(page==Page.CHORD || page==Page.CONFIRM || page==Page.CONNECT && !prefs().getBoolean("setupComplete",false)) show(Page.SETUP); else show(Page.APPS); }
    @Override public void onBackPressed(){ back(); }
    @Override public boolean dispatchKeyEvent(KeyEvent event){
        int key=event.getKeyCode();
        if(page==Page.CHORD && key==KeyEvent.KEYCODE_BUTTON_START && heldKeys.isEmpty() && !capturedKeys.isEmpty()){
            if(event.getAction()==KeyEvent.ACTION_DOWN && event.getRepeatCount()==0 && saveChord.isEnabled()) saveChord.performClick(); return true;
        }
        if((page==Page.CHORD || page==Page.CONFIRM) && ShortcutKeys.eligible(event)){ record(event); return true; }
        boolean swapped=prefs().getBoolean("swapped",false); int a=swapped?97:96,b=swapped?96:97,x=swapped?100:99,y=swapped?99:100;
        if(key==b){ if(event.getAction()==KeyEvent.ACTION_DOWN && event.getRepeatCount()==0) back(); return true; }
        if(page==Page.APPS){
            if(key==KeyEvent.KEYCODE_BUTTON_SELECT){ if(event.getAction()==KeyEvent.ACTION_DOWN && event.getRepeatCount()==0 && !toolZone) protectSelected(); return true; }
            if(key>=KeyEvent.KEYCODE_DPAD_UP && key<=KeyEvent.KEYCODE_DPAD_RIGHT){
                if(event.getAction()==KeyEvent.ACTION_DOWN) navigate(key==KeyEvent.KEYCODE_DPAD_LEFT?-1:key==KeyEvent.KEYCODE_DPAD_RIGHT?1:key==KeyEvent.KEYCODE_DPAD_UP?-2:2); return true;
            }
            if(key==a || key==x || key==y || key==KeyEvent.KEYCODE_DPAD_CENTER){
                if(event.getAction()==KeyEvent.ACTION_DOWN && event.getRepeatCount()==0){
                    if(key==a || key==KeyEvent.KEYCODE_DPAD_CENTER){ if(toolZone) tools.get(toolIndex).performClick(); else openSelected(); }
                    else if(!toolZone){ if(key==x) closeSelected(); else closeAll(); }
                } return true;
            }
        } else {
            if(key==a){
                if(event.getRepeatCount()==0) return super.dispatchKeyEvent(new KeyEvent(event.getDownTime(),event.getEventTime(),event.getAction(),KeyEvent.KEYCODE_DPAD_CENTER,0)); return true;
            }
            if(key>=KeyEvent.KEYCODE_DPAD_UP && key<=KeyEvent.KEYCODE_DPAD_RIGHT){
                if(event.getAction()==KeyEvent.ACTION_DOWN) pageNavigate(key==KeyEvent.KEYCODE_DPAD_UP || key==KeyEvent.KEYCODE_DPAD_LEFT?-1:1); return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }
    private void pageNavigate(int delta){ if(controls.isEmpty()) return; int index=controls.indexOf(getCurrentFocus()); int next=index<0?0:Math.max(0,Math.min(controls.size()-1,index+delta)); Button target=controls.get(next); target.requestFocus(); target.requestRectangleOnScreen(new android.graphics.Rect(0,0,target.getWidth(),target.getHeight()),false); }
    private void navigate(int direction){
        if(page!=Page.APPS){ pageNavigate(direction<0?-1:1); return; }
        if(direction==-2){ toolZone=true; if(!tools.isEmpty()) tools.get(toolIndex).requestFocus(); }
        else if(direction==2){ toolZone=false; }
        else if(toolZone){ toolIndex=Math.max(0,Math.min(tools.size()-1,toolIndex+direction)); tools.get(toolIndex).requestFocus(); }
        else selection.move(direction);
        markSelection();
    }
    @Override public boolean onGenericMotionEvent(MotionEvent event){
        if(!event.isFromSource(InputDevice.SOURCE_JOYSTICK)) return super.onGenericMotionEvent(event);
        if(page==Page.CHORD || page==Page.CONFIRM) return true;
        float x=event.getAxisValue(MotionEvent.AXIS_HAT_X),y=event.getAxisValue(MotionEvent.AXIS_HAT_Y);
        if(Math.abs(x)<.55f) x=event.getAxisValue(MotionEvent.AXIS_X); if(Math.abs(y)<.55f) y=event.getAxisValue(MotionEvent.AXIS_Y);
        int next=Math.abs(x)>=Math.abs(y)?x>.55f?1:x<-.55f?-1:0:y>.55f?2:y<-.55f?-2:0;
        if(next!=stickDirection){ stopStick(); stickDirection=next; if(next!=0){ navigate(next); main.postDelayed(repeat,360); } }
        return true;
    }
    private void stopStick(){ stickDirection=0; main.removeCallbacks(repeat); }
    @Override public void onWindowFocusChanged(boolean focused){ super.onWindowFocusChanged(focused); if(!focused) stopStick(); }
}
