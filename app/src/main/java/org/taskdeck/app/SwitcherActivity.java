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
    private boolean pendingRefresh,controllerMode=true,resetSelection=true,captureEntry=true;
    private int setupStep,controlIndex=-1;
    private JSONObject entryTask;
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
        if(initial==Page.CHORD || initial==Page.CONFIRM) initial=Page.SETUP;
        show(initial);
    }
    @Override public void onSaveInstanceState(Bundle out){ out.putString("page",page.name()); out.putInt("setupStep",setupStep); super.onSaveInstanceState(out); }
    @Override protected void onResume(){
        super.onResume(); TaskDeckApp.recordingShortcut=page==Page.CHORD || page==Page.CONFIRM;
        app.changed=this::connectionChanged; if(page==Page.SETUP) show(Page.SETUP); updateStatus();
        if(app.backend!=null) refresh(); else app.connect(false);
    }
    @Override protected void onPause(){ app.changed=null; TaskDeckApp.recordingShortcut=false; stopStick(); super.onPause(); }
    @Override protected void onNewIntent(Intent intent){ super.onNewIntent(intent); setIntent(intent); resetSelection=true; captureEntry=true; entryTask=null; show(prefs().getBoolean("setupComplete",false)?Page.APPS:Page.SETUP); if(app.backend!=null) refresh(); }
    private void connectionChanged(){
        if(isFinishing() || isDestroyed()) return;
        if(app.backend==null){ tasks.clear(); selection.update(Collections.emptyList()); if(page==Page.APPS) renderCards(); updateStatus(); }
        else { updateStatus(); refresh(); }
    }
    private void show(Page next){
        int restoreY=page==Page.SETUP && next==Page.SETUP && pageScroll!=null?pageScroll.getScrollY():0;
        int restoreFocus=page==Page.SETUP && next==Page.SETUP?controlIndex:-1;
        if(next==Page.APPS && page!=Page.APPS) resetSelection=true;
        if(protectionPopup!=null) protectionPopup.dismiss(); pageScroll=null;
        stopStick(); feedback=""; TaskDeckApp.recordingShortcut=next==Page.CHORD || next==Page.CONFIRM; page=next; toolZone=false;
        controls.clear(); controlIndex=-1; tools.clear(); cards.clear(); cardRow=null; rail=null; status=null; memory=null;
        theme=DeckTheme.named(prefs().getString("theme","dark"));
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(theme.background); root.setPadding(dp(20),dp(12),dp(20),dp(12));
        root.setOnApplyWindowInsetsListener((view,insets)->{
            if(Build.VERSION.SDK_INT>=30){ android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars()); view.setPadding(dp(20)+bars.left,dp(10)+bars.top,dp(20)+bars.right,dp(10)+bars.bottom); }
            else view.setPadding(dp(20)+insets.getSystemWindowInsetLeft(),dp(10)+insets.getSystemWindowInsetTop(),dp(20)+insets.getSystemWindowInsetRight(),dp(10)+insets.getSystemWindowInsetBottom());
            return insets;
        }); root.setFocusableInTouchMode(true); setContentView(root); if(!controllerMode) root.requestFocus();
        if(page==Page.APPS) appsPage(); else if(page==Page.SETUP) setupPage(); else if(page==Page.CONNECT) connectPage(); else recordPage();
        if(restoreY>0 && pageScroll!=null){ ScrollView scroll=pageScroll; main.post(()->scroll.scrollTo(0,restoreY)); }
        if(controllerMode && restoreFocus>=0 && restoreFocus<controls.size()){ int index=restoreFocus; main.post(()->focusControl(index)); }
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
        tools.add(button(toolRow,"Refresh",this::refresh)); tools.add(button(toolRow,"Sort",this::sortMenu));
        if(getResources().getConfiguration().screenWidthDp<600) toolRow=row(root);
        tools.add(button(toolRow,"Connect",()->show(Page.CONNECT))); tools.add(button(toolRow,"Setup",()->{ setupStep=0; show(Page.SETUP); }));
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
        button(actions,prefs().getBoolean("exitAfterBulk",true)?"Y · All + exit":"Y · Close all",this::closeAll); button(actions,"R1 · Others",this::closeOthers);
        if(getResources().getConfiguration().screenWidthDp<600) actions=row(root); button(actions,"B · Exit",this::finish);
        text(root,"← / → or stick: apps    ↑: tools    Select / long press: protect",12,true);
    }
    private void renderCards(){
        if(cardRow==null) return;
        cardRow.removeAllViews(); cards.clear();
        if(tasks.isEmpty()){ text(cardRow,app.backend==null?"Connect Shizuku to load your apps.":"Nothing to close",20,true); return; }
        int width=dp(Math.max(156,Math.min(208,(getResources().getConfiguration().screenWidthDp-64)/4)));
        boolean compact=getResources().getConfiguration().screenHeightDp<440;
        for(JSONObject task:tasks){
            LinearLayout card=new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setGravity(Gravity.CENTER); card.setPadding(dp(12),dp(compact?6:14),dp(12),dp(compact?6:14)); card.setFocusableInTouchMode(controllerMode); card.setDefaultFocusHighlightEnabled(false);
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
            View card=cards.get(selection.index()); if(controllerMode) card.requestFocus();
            main.post(()->{ if(page==Page.APPS && rail!=null && card.getParent()==cardRow) rail.smoothScrollTo(Math.max(0,card.getLeft()-(rail.getWidth()-card.getWidth())/2),0); });
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
        LinearLayout body=scrollBody(prefs().getBoolean("setupComplete",false)?"TaskDeck settings":"Welcome to TaskDeck");
        text(body,"Step "+(setupStep+1)+" of 4 · "+new String[]{"Connection","Controller","Appearance","Notifications & closing"}[setupStep],16,false);
        if(setupStep==0){
            LinearLayout panel=section(body,"Connection", "Start Shizuku, then connect TaskDeck. Without root, Shizuku must be started again after a reboot.");
            status=text(panel,app.status,15,true); text(panel,app.backend!=null?"✓ Connected":"Not connected yet",17,false);
            button(panel,app.backend==null?"Connect Shizuku":"Connection details",()->show(Page.CONNECT));
        } else if(setupStep==1){
            LinearLayout panel=section(body,"Controller", "D-pad or left stick selects apps. Confirm opens, X closes, Y closes all, R1 keeps the previous app, B exits. Closing is immediate.");
            text(panel,prefs().getBoolean("confirmCalibrated",prefs().contains("swapped"))?"✓ Confirm calibrated":"Confirm uses the default mapping",15,false);
            button(panel,"Calibrate printed A",()->show(Page.CONFIRM));
            Set<String> chord=ShortcutKeys.decode(prefs().getString("shortcutChord",""));
            text(panel,(chord.isEmpty()?"No shortcut saved":"✓ Saved shortcut: "+ShortcutKeys.labels(chord)),15,false);
            button(panel,"Record button combination",()->show(Page.CHORD));
            text(panel,"Global shortcut: "+(!prefs().getBoolean("shortcutEnabled",false)?"off":shortcutServiceEnabled()?"✓ enabled":"service access required"),15,false);
            button(panel,prefs().getBoolean("shortcutEnabled",false)?"Disable shortcut":"Enable saved shortcut",()->{
                if(ShortcutKeys.decode(prefs().getString("shortcutChord","")).isEmpty()){ show(Page.CHORD); return; }
                prefs().edit().putBoolean("shortcutEnabled",!prefs().getBoolean("shortcutEnabled",false)).apply(); show(Page.SETUP);
            });
            button(panel,shortcutServiceEnabled()?"Shortcut service enabled · Settings":"Enable controller shortcut service",()->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
            text(panel,"Optional Accessibility access opens TaskDeck on your chosen combination. Partial presses pass through. No screen content or typed text is read or stored.",14,true);
        } else if(setupStep==2){
            LinearLayout panel=section(body,"Appearance", "Your choice is saved immediately. Touch activates an option on the first tap; controller focus shows what Confirm will activate.");
            text(panel,"Selected: "+prefs().getString("theme","dark").toUpperCase(Locale.ROOT),15,false); LinearLayout themes=row(panel);
            if(getResources().getConfiguration().screenWidthDp<420) themes.setOrientation(LinearLayout.VERTICAL);
            for(String value:new String[]{"light","dark","oled"}){
                Button option=button(themes,value.equals("oled")?"OLED":value.equals("light")?"Light":"Dark",()->{ prefs().edit().putString("theme",value).apply(); show(Page.SETUP); });
                option.setSelected(value.equals(prefs().getString("theme","dark")));
            }
        } else {
            LinearLayout panel=section(body,"Notifications & closing", "RAM is an approximate PSS snapshot, not guaranteed savings. The result notification reports the measured change in total available RAM.");
            boolean ram=prefs().getBoolean("ramNotifications",true);
            text(panel,"RAM notifications: "+(!ram?"off":notificationsAllowed("ram")?"enabled":"permission or channel blocked"),15,false);
            button(panel,ram?"Disable RAM notifications":"Enable RAM notifications",()->{
                boolean enable=!prefs().getBoolean("ramNotifications",true); prefs().edit().putBoolean("ramNotifications",enable).apply();
                if(enable) notificationPermission(); else getSystemService(NotificationManager.class).cancel(2); show(Page.SETUP);
            });
            boolean visible=shortcutVisible(); text(panel,"Notification shortcut: "+(visible?"visible":notificationsAllowed("switcher")?"hidden":"permission or channel blocked"),15,false);
            button(panel,visible?"Hide notification shortcut":"Show notification shortcut",()->{
                if(shortcutVisible()){ getSystemService(NotificationManager.class).cancel(1); prefs().edit().putBoolean("pendingNotificationShortcut",false).apply(); }
                else shortcutNotification(); show(Page.SETUP);
            });
            button(panel,"Android notification settings",()->startActivity(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,getPackageName())));
            boolean exit=prefs().getBoolean("exitAfterBulk",true);
            text(panel,"After bulk closing: "+(exit?"exit TaskDeck":"stay in TaskDeck"),15,false);
            button(panel,exit?"Stay open after close actions":"Exit after close actions",()->{ prefs().edit().putBoolean("exitAfterBulk",!exit).apply(); show(Page.SETUP); });
            text(panel,"Close all returns to HOME. Close others keeps the app you came from and returns to it. Protected apps are skipped. Launchers and essential services are hidden; apps you lock appear last.",14,true);
        }
        LinearLayout navigation=row(root);
        Button previous=button(navigation,"Previous",()->{ setupStep--; show(Page.SETUP); }); previous.setEnabled(setupStep>0);
        button(navigation,setupStep==3?"Finish setup":"Next step",()->{
            if(setupStep<3){ setupStep++; show(Page.SETUP); }
            else { prefs().edit().putBoolean("setupComplete",true).apply(); show(Page.APPS); if(app.backend!=null) refresh(); }
        });
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
        text(body,page==Page.CHORD?"Hold the buttons together, then release them. TaskDeck records the largest simultaneous combination. Save when the displayed buttons are correct. Up to four digital buttons are supported. D-pad, Confirm, Back and Start stay available for navigation and saving.":"Press the button printed A. This calibrates A/B and X/Y for the app deck.",17,false);
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
        recordSaved.setText("Recorded: "+ShortcutKeys.labels(capturedKeys));
        saveChord.setEnabled(!capturedKeys.isEmpty() && heldKeys.isEmpty() && capturedKeys.size()<=4);
        if(controllerMode && saveChord.isEnabled()) focusControl(controls.indexOf(saveChord));
    }
    private void updateStatus(){
        if(status==null) return;
        if(!feedback.isEmpty()) status.setText(feedback);
        else if(page==Page.APPS) status.setText(app.backend==null?"Not connected · open Connect":tasks.size()+" apps · "+prefs().getString("sort","recent")+(tasks.stream().noneMatch(t->!locked(t))?" · Nothing to close":"")+(busy?" · updating…":""));
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
                List<JSONObject> raw=new ArrayList<>(); JSONArray rows=result.optJSONArray("tasks");
                if(rows!=null) for(int i=0;i<rows.length();i++){ JSONObject task=rows.optJSONObject(i); if(task!=null){ try { task.put("recentRank",i); } catch(JSONException ignored){} raw.add(task); } }
                if(captureEntry){ entryTask=result.optJSONObject("previousTask"); if(entryTask==null) for(JSONObject task:raw) if(!TaskOrder.mandatory(task)){ entryTask=task; break; } captureEntry=false; }
                tasks.clear(); tasks.addAll(TaskOrder.arrange(raw,prefs().getString("sort","recent"),this::locked));
                List<Integer> ids=new ArrayList<>(); for(JSONObject task:tasks) ids.add(task.optInt("id"));
                selection.update(ids); if(resetSelection){ selection.first(); resetSelection=false; } if(page==Page.APPS) renderCards(); updateStatus();
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
    private void closeSelected(){ JSONObject task=selected(); if(task==null) return; if(locked(task)){ feedback="This app is protected"; updateStatus(); return; } remove(Collections.singletonList(task),0); }
    private void closeAll(){
        List<JSONObject> eligible=new ArrayList<>(); for(JSONObject task:tasks) if(!locked(task)) eligible.add(task);
        remove(eligible,prefs().getBoolean("exitAfterBulk",true)?1:0);
    }
    private void closeOthers(){
        if(entryTask==null && !tasks.isEmpty()){ feedback="Previous app could not be identified. Reopen TaskDeck from that app."; updateStatus(); return; }
        String keep=entryTask==null?"":entryTask.optString("package"); List<JSONObject> eligible=new ArrayList<>();
        for(JSONObject task:tasks) if(!locked(task)&&!keep.equals(task.optString("package"))) eligible.add(task);
        remove(eligible,prefs().getBoolean("exitAfterBulk",true)?2:0);
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
    private void reorder(){ List<JSONObject> sorted=TaskOrder.arrange(tasks,prefs().getString("sort","recent"),this::locked); tasks.clear(); tasks.addAll(sorted); List<Integer> ids=new ArrayList<>(); for(JSONObject task:tasks) ids.add(task.optInt("id")); selection.update(ids); renderCards(); updateStatus(); }

    private void protectSelected(){
        JSONObject task=selected(); if(task==null) return;
        boolean mandatory=task.optBoolean("mandatoryProtected",!task.optBoolean("removable") && !task.optBoolean("userProtected"));
        final Button[] option=new Button[1];
        LinearLayout panel=new LinearLayout(this){
            @Override public boolean dispatchKeyEvent(KeyEvent event){
                setInputMode(true); int a=prefs().getBoolean("swapped",false)?97:96,b=prefs().getBoolean("swapped",false)?96:97;
                if(event.getKeyCode()==b || event.getKeyCode()==KeyEvent.KEYCODE_BACK){ if(event.getAction()==KeyEvent.ACTION_DOWN) protectionPopup.dismiss(); return true; }
                if(event.getKeyCode()==a || event.getKeyCode()==KeyEvent.KEYCODE_DPAD_CENTER){ if(event.getAction()==KeyEvent.ACTION_DOWN && event.getRepeatCount()==0) option[0].performClick(); return true; }
                return super.dispatchKeyEvent(event);
            }
            @Override public boolean dispatchTouchEvent(MotionEvent event){ if(event.getActionMasked()==0) setInputMode(false); return super.dispatchTouchEvent(event); }
        };
        panel.setOrientation(LinearLayout.VERTICAL); panel.setPadding(dp(24),dp(18),dp(24),dp(18)); panel.setBackground(rounded(theme.surface));
        text(panel,task.optString("label"),21,false); text(panel,mandatory?"Required protection for launcher, keyboard or system service.":"Protected apps are skipped by X and Y. Your choice is saved.",14,true);
        boolean manual=protectedApps().contains(task.optString("package"));
        int previousControls=controls.size();
        option[0]=button(panel,mandatory?"Always protected · Close":manual?"Unlock app":"Protect app",()->{
            if(!mandatory){ Set<String> names=protectedApps(); String name=task.optString("package"); if(names.contains(name)) names.remove(name); else names.add(name); prefs().edit().putStringSet("protectedApps",names).apply(); }
            protectionPopup.dismiss(); reorder(); refresh();
        });
        while(controls.size()>previousControls) controls.remove(controls.size()-1);
        Drawable lock=getDrawable(R.drawable.ic_lock); if(lock!=null){ lock.setTint(theme.accent); lock.setBounds(0,0,dp(22),dp(22)); option[0].setCompoundDrawables(lock,null,null,null); option[0].setCompoundDrawablePadding(dp(8)); }
        protectionPopup=new PopupWindow(panel,Math.min(dp(360),getResources().getDisplayMetrics().widthPixels-dp(32)),-2,true);
        protectionPopup.setBackgroundDrawable(rounded(theme.surface)); protectionPopup.setOutsideTouchable(true); protectionPopup.setElevation(dp(16)); protectionPopup.setOnDismissListener(this::markSelection);
        protectionPopup.showAtLocation(root,Gravity.CENTER,0,0); if(controllerMode) option[0].requestFocus();
    }
    // 0: keep menu, 1: HOME, 2: previous app. Finish only the UI; the shortcut service remains available.
    private void remove(List<JSONObject> snapshot,int destination){
        ITaskBackend backend=app.backend; if(busy || backend==null&&!snapshot.isEmpty()) return;
        busy=true; long before=available(); feedback="Closing "+snapshot.size()+" task(s)…"; updateStatus();
        final JSONObject previous=entryTask;
        app.worker.execute(()->{
            int closed=0,failed=0; String lastError="";
            for(JSONObject task:snapshot){
                try { backend.setProtectedPackages(protectedArray()); JSONObject result=new JSONObject(backend.removeTask(task.optInt("id"),task.optString("package"))); if(result.has("error")){ failed++; lastError=result.optString("error"); } else closed++; }
                catch(Exception e){ failed++; lastError=e.getClass().getSimpleName(); }
            }
            if(failed==0 && destination==2 && previous!=null && backend!=null && !previous.optBoolean("home")){
                try { JSONObject result=new JSONObject(backend.openTask(previous.optInt("id"),previous.optString("package"))); if(result.has("error")){ failed++; lastError=result.optString("error"); } }
                catch(Exception e){ failed++; lastError="Return failed: "+e.getClass().getSimpleName(); }
            }
            final int done=closed,errors=failed; final String problem=lastError;
            main.post(()->{
                busy=false; if(isDestroyed()) return;
                feedback=done+" task(s) closed"+(errors>0?" · "+errors+" failed: "+problem:""); updateStatus();
                if(errors==0 && destination!=0){
                    if(destination==1 || previous==null || previous.optBoolean("home")) startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    finish();
                } else refresh();
                if(done>0) main.postDelayed(()->{
                    long after=available(),delta=(after-before)/(1024*1024);
                    if(!isDestroyed()&&!isFinishing()){ feedback=done+" closed · RAM available "+(after/(1024*1024))+" MiB · change "+(delta>=0?"+":"")+delta+" MiB"+(errors>0?" · "+errors+" failed":""); updateStatus(); updateMemory(); }
                    ramNotification(done,after,delta);
                },700);
            });
        });
    }

    private void notificationPermission(){
        if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},1);
        else { feedback=notificationsAllowed()?"Notifications enabled":"Notifications blocked in Android settings"; updateStatus(); }
    }
    private boolean notificationsAllowed(){ return (Build.VERSION.SDK_INT<33 || checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED)&&getSystemService(NotificationManager.class).areNotificationsEnabled(); }
    private boolean notificationsAllowed(String channel){ NotificationChannel value=getSystemService(NotificationManager.class).getNotificationChannel(channel); return notificationsAllowed()&&(value==null||value.getImportance()!=NotificationManager.IMPORTANCE_NONE); }
    private boolean shortcutVisible(){ for(android.service.notification.StatusBarNotification item:getSystemService(NotificationManager.class).getActiveNotifications()) if(item.getId()==1) return true; return false; }
    private void ramNotification(int count,long after,long delta){
        if(!prefs().getBoolean("ramNotifications",true) || !notificationsAllowed("ram")) return;
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
    @Override public void onRequestPermissionsResult(int code,String[] permissions,int[] grants){
        super.onRequestPermissionsResult(code,permissions,grants); if(code!=1) return;
        boolean granted=grants.length>0&&grants[0]==PackageManager.PERMISSION_GRANTED;
        boolean shortcut=prefs().getBoolean("pendingNotificationShortcut",false); prefs().edit().remove("pendingNotificationShortcut").apply();
        if(granted&&shortcut) shortcutNotification();
        if(page==Page.SETUP) show(Page.SETUP); feedback=granted?"Notification permission granted":"Notification permission denied"; updateStatus();
    }
    private void back(){ if(page==Page.APPS) finish(); else if(page==Page.CHORD || page==Page.CONFIRM || page==Page.CONNECT) show(Page.SETUP); else if(setupStep>0){ setupStep--; show(Page.SETUP); } else show(Page.APPS); }
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
        if(page==Page.APPS){
            if(key==KeyEvent.KEYCODE_BUTTON_R1){ if(event.getAction()==0&&event.getRepeatCount()==0) closeOthers(); return true; }
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
            if(key==a || key==KeyEvent.KEYCODE_DPAD_CENTER){
                if(event.getAction()==0&&event.getRepeatCount()==0){ if(controlIndex<0) pageNavigate(1); if(controlIndex>=0&&controlIndex<controls.size()) controls.get(controlIndex).performClick(); } return true;
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
        if(direction==-2){ toolZone=true; if(!tools.isEmpty()) tools.get(toolIndex).requestFocus(); }
        else if(direction==2){ toolZone=false; }
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
