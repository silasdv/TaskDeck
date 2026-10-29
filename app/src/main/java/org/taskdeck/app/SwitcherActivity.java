package org.taskdeck.app;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.*;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.*;

public class SwitcherActivity extends Activity {
    private class ControllerDialogBuilder extends AlertDialog.Builder {
        ControllerDialogBuilder(Context context){ super(context); }
        @Override public AlertDialog show(){
            AlertDialog dialog=create();
            dialog.setOnKeyListener((d,key,event)->{
                boolean swapped=prefs().getBoolean("swapped",false);
                if(key==(swapped?96:97)){ if(event.getAction()==KeyEvent.ACTION_DOWN && event.getRepeatCount()==0) d.cancel(); return true; }
                if(key==(swapped?97:96)){
                    dialog.dispatchKeyEvent(new KeyEvent(event.getDownTime(),event.getEventTime(),event.getAction(),KeyEvent.KEYCODE_DPAD_CENTER,event.getRepeatCount())); return true;
                }
                return false;
            });
            dialog.show(); return dialog;
        }
    }
    private TaskDeckApp app;
    private TextView status;
    private ListView list;
    private final List<JSONObject> tasks=new ArrayList<>();
    private ArrayAdapter<JSONObject> adapter;
    private boolean busy;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private int stick;
    private final Runnable repeat=new Runnable(){ public void run(){ if(stick!=0){ move(stick); handler.postDelayed(this,130); } } };
    private TextView memory;
    private int dp(int value){ return Math.round(value*getResources().getDisplayMetrics().density); }
    private android.content.SharedPreferences prefs(){ return getSharedPreferences("settings",MODE_PRIVATE); }
    private GradientDrawable rounded(int color){ GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(16)); return d; }
    private Button button(LinearLayout parent,String label,Runnable action){
        Button b=new Button(this); b.setText(label); b.setAllCaps(false); b.setTextColor(Color.WHITE);
        StateListDrawable background=new StateListDrawable();
        GradientDrawable focus=rounded(Color.rgb(44,87,85)); focus.setStroke(dp(2),Color.rgb(104,224,191));
        background.addState(new int[]{android.R.attr.state_focused},focus);
        background.addState(new int[]{android.R.attr.state_pressed},rounded(Color.rgb(44,87,85)));
        background.addState(new int[]{},rounded(Color.rgb(34,46,63))); b.setBackground(background); b.setBackgroundTintList(null); b.setDefaultFocusHighlightEnabled(false);
        b.setPadding(dp(16),dp(8),dp(16),dp(8)); b.setOnClickListener(v->action.run());
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,-2); p.setMargins(0,dp(5),dp(8),dp(5)); parent.addView(b,p); return b;
    }
    private TextView text(LinearLayout parent,String value,int size){ TextView t=new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(Color.WHITE); t.setPadding(0,dp(6),0,dp(6)); parent.addView(t); return t; }
    @Override public void onCreate(Bundle state){
        super.onCreate(state); app=(TaskDeckApp)getApplication();
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(20),dp(16),dp(20),dp(12)); root.setBackgroundColor(Color.rgb(16,23,34));
        root.setOnApplyWindowInsetsListener((v,insets)->{
            if(Build.VERSION.SDK_INT>=30){ android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars()); v.setPadding(dp(20)+bars.left,dp(12)+bars.top,dp(20)+bars.right,dp(12)+bars.bottom); }
            else v.setPadding(dp(20)+insets.getSystemWindowInsetLeft(),dp(12)+insets.getSystemWindowInsetTop(),dp(20)+insets.getSystemWindowInsetRight(),dp(12)+insets.getSystemWindowInsetBottom());
            return insets;
        });
        setContentView(root); text(root,"TaskDeck",26); status=text(root,app.status,14); memory=text(root,"",12);
        LinearLayout tools=new LinearLayout(this); root.addView(tools);
        button(tools,"Connect",()->app.connect(true)); button(tools,"Refresh",this::refresh); button(tools,"Setup",this::setup);
        list=new ListView(this); list.setChoiceMode(ListView.CHOICE_MODE_SINGLE); list.setDividerHeight(dp(6)); list.setSelector(rounded(Color.rgb(36,84,79)));
        adapter=new ArrayAdapter<JSONObject>(this,android.R.layout.simple_list_item_1,tasks){
            @Override public View getView(int position,View recycled,ViewGroup parent){
                TextView row=(TextView)super.getView(position,recycled,parent); JSONObject task=getItem(position);
                row.setText(task.optString("label")+(task.optBoolean("removable")?"":"  · protected")); row.setTextColor(Color.WHITE); row.setTextSize(20); row.setPadding(dp(14),dp(14),dp(14),dp(14));
                row.setCompoundDrawablePadding(dp(14));
                Drawable icon=null;
                try { icon=getPackageManager().getApplicationIcon(task.optString("package")); }
                catch(PackageManager.NameNotFoundException ignored) {}
                if(icon==null) icon=getDrawable(org.taskdeck.app.R.drawable.ic_tasks);
                if(icon!=null) icon.setBounds(0,0,dp(36),dp(36));
                row.setCompoundDrawables(icon,null,null,null);
                return row;
            }
        };
        list.setAdapter(adapter); root.addView(list,new LinearLayout.LayoutParams(-1,0,1));
        list.setOnItemClickListener((p,v,index,id)->{ list.setItemChecked(index,true); list.setSelection(index); list.requestFocus(); });
        LinearLayout actionBox=new LinearLayout(this); actionBox.setOrientation(LinearLayout.VERTICAL); root.addView(actionBox);
        LinearLayout actions=new LinearLayout(this); actionBox.addView(actions);
        button(actions,"A · Open",this::open); button(actions,"X · Close",this::closeSelected);
        if(getResources().getConfiguration().screenWidthDp<600){ actions=new LinearLayout(this); actionBox.addView(actions); }
        button(actions,"Y · Close all…",this::closeAll); button(actions,"B · Back",this::finish);
        text(root,"Close removes a recent task; it does not force-stop every background process.",12);
    }
    @Override protected void onResume(){ super.onResume(); app.changed=()->{ if(!isFinishing()){ status.setText(app.status); if(app.backend!=null) refresh(); else { tasks.clear(); adapter.notifyDataSetChanged(); } } }; status.setText(app.status); if(app.backend!=null) refresh(); else app.connect(false); }
    @Override protected void onPause(){ stick=0; handler.removeCallbacks(repeat); app.changed=null; super.onPause(); }
    @Override protected void onNewIntent(Intent intent){ super.onNewIntent(intent); setIntent(intent); if(app.backend!=null) refresh(); }
    private void run(java.util.concurrent.Callable<String> operation,Runnable success){
        if(busy) return;
        busy=true;
        app.worker.execute(()->{
            String error=null;
            try { JSONObject result=new JSONObject(operation.call()); if(result.has("error")) error=result.getString("error"); }
            catch(Exception e){ error="Backend unavailable: "+e.getClass().getSimpleName(); }
            final String problem=error;
            handler.post(()->{ busy=false; if(isDestroyed()) return; if(problem!=null) status.setText(problem); else success.run(); });
        });
    }
    private void refresh(){
        ActivityManager.MemoryInfo info=new ActivityManager.MemoryInfo(); getSystemService(ActivityManager.class).getMemoryInfo(info);
        memory.setText("Available RAM: "+(info.availMem/(1024*1024))+" / "+(info.totalMem/(1024*1024))+" MiB · Android manages cached memory");
        ITaskBackend backend=app.backend; if(backend==null){ status.setText(app.status); return; }
        int previous=selected()==null?-1:selected().optInt("id");
        run(()->{
            String raw=backend.readTasks(); JSONObject data=new JSONObject(raw);
            if(!data.has("error")) {
                JSONArray rows=data.getJSONArray("tasks"); List<JSONObject> fresh=new ArrayList<>(); for(int i=0;i<rows.length();i++) fresh.add(rows.getJSONObject(i));
                handler.post(()->{
                    if(isDestroyed() || app.backend!=backend) return;
                    tasks.clear(); tasks.addAll(fresh); adapter.notifyDataSetChanged();
                    int index=0; for(int i=0;i<tasks.size();i++) if(tasks.get(i).optInt("id")==previous) index=i;
                    if(!tasks.isEmpty()){ list.setItemChecked(index,true); list.setSelection(index); list.requestFocus(); }
                    status.setText(tasks.size()+" recent tasks · backend UID "+data.optInt("uid")+" · Android API "+data.optInt("sdk")+(tasks.isEmpty()?" · No tasks returned; compatibility is not established.":""));
                });
            }
            return raw;
        },()->{});
    }
    private JSONObject selected(){ int index=list.getSelectedItemPosition(); if(index<0) index=list.getCheckedItemPosition(); return index>=0 && index<tasks.size()?tasks.get(index):null; }
    private void open(){ JSONObject task=selected(); ITaskBackend backend=app.backend; if(task==null || backend==null || busy) return; run(()->backend.openTask(task.optInt("id"),task.optString("package")),this::finish); }
    private void closeSelected(){
        JSONObject task=selected(); if(task==null || app.backend==null || busy) return;
        if(!task.optBoolean("removable")){ status.setText("This task is protected. Closing is disabled."); return; }
        new ControllerDialogBuilder(this).setTitle("Close "+task.optString("label")+"?").setMessage("This removes its task. Unsaved work or game progress may be lost. It is not a force-stop.")
            .setNegativeButton("Cancel",null).setPositiveButton("Close task",(d,w)->{ ITaskBackend backend=app.backend; if(backend!=null) run(()->backend.removeTask(task.optInt("id"),task.optString("package")),this::refresh); }).show();
    }
    private void closeAll(){
        if(busy || app.backend==null) return;
        List<JSONObject> snapshot=new ArrayList<>(); for(JSONObject task:tasks) if(task.optBoolean("removable")) snapshot.add(task);
        if(snapshot.isEmpty()){ status.setText("No eligible tasks to close."); return; }
        new ControllerDialogBuilder(this).setTitle("Close "+snapshot.size()+" tasks?").setMessage("Protected and system apps are excluded. Unsaved work or game progress may be lost. Background services may remain active.")
            .setNegativeButton("Cancel",null).setPositiveButton("Close tasks",(d,w)->{
                ITaskBackend backend=app.backend; if(backend==null) return;
                final int[] count={0,0};
                run(()->{
                    for(JSONObject task:snapshot){ JSONObject result=new JSONObject(backend.removeTask(task.optInt("id"),task.optString("package"))); if(result.has("error")) count[1]++; else count[0]++; }
                    return "{}";
                },()->{ Toast.makeText(this,count[0]+" closed; "+count[1]+" failed or changed",Toast.LENGTH_LONG).show(); refresh(); });
            }).show();
    }
    private void setup(){
        new ControllerDialogBuilder(this).setTitle("TaskDeck setup · prototype")
            .setItems(new String[]{"1. Shizuku instructions","2. Calibrate confirm button","3. Learn controller shortcut","4. Enable shortcut accessibility service","5. Disable controller shortcut","6. Show notification shortcut"},(d,index)->{
                if(index==0) new ControllerDialogBuilder(this).setTitle("Connect Shizuku").setMessage("Install Shizuku from shizuku.rikka.app. Start it using wireless debugging, then return and choose Connect. Shizuku 13+ is required. Without root, restart Shizuku after each device reboot. TaskDeck does not replace Android's native gesture navigation.").setPositiveButton("OK",null).show();
                if(index==1) learn(false);
                if(index==2) learn(true);
                if(index==3) new ControllerDialogBuilder(this).setTitle("Controller shortcut access").setMessage("TaskDeck uses Accessibility only to intercept the controller key you explicitly choose and open this menu. That key will not reach games while the shortcut is enabled. No screen content or typed text is read, saved or sent. This is optional; the Quick Settings tile also opens the menu.").setNegativeButton("Cancel",null).setPositiveButton("Continue",(x,y)->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))).show();
                if(index==4){ prefs().edit().putBoolean("shortcutEnabled",false).apply(); Toast.makeText(this,"Controller shortcut disabled",Toast.LENGTH_SHORT).show(); }
                if(index==5) notification();
            }).setNegativeButton("Back",null).show();
    }
    private void learn(boolean shortcut){
        AlertDialog dialog=new ControllerDialogBuilder(this).setTitle(shortcut?"Press your dedicated shortcut button":"Press the button printed A")
            .setMessage(shortcut?"Use a spare controller key or macro mapped to F1–F12. Do not choose a button needed in games. Android may reserve the Guide/Home button.":"This detects standard or swapped A/B and X/Y reporting for the menu.")
            .setNegativeButton("Cancel",null).create();
        dialog.setOnKeyListener((d,key,event)->{
            boolean allowed=shortcut?(KeyEvent.isGamepadButton(key) || key>=KeyEvent.KEYCODE_F1 && key<=KeyEvent.KEYCODE_F12):(key==96 || key==97);
            if(!allowed) return false;
            if(event.getAction()==KeyEvent.ACTION_DOWN && event.getRepeatCount()==0){
                if(shortcut) prefs().edit().putInt("shortcut",key).putBoolean("shortcutEnabled",true).apply();
                else prefs().edit().putBoolean("swapped",key==97).apply();
                dialog.dismiss(); Toast.makeText(this,shortcut?"Shortcut saved. Enable the accessibility service to use it globally.":"Confirm button calibrated",Toast.LENGTH_LONG).show();
            }
            return true;
        }); dialog.show();
    }
    private void notification(){
        if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){ requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},1); return; }
        NotificationManager manager=getSystemService(NotificationManager.class); manager.createNotificationChannel(new NotificationChannel("switcher","TaskDeck shortcut",NotificationManager.IMPORTANCE_LOW));
        PendingIntent open=PendingIntent.getActivity(this,0,new Intent(this,SwitcherActivity.class),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        manager.notify(1,new Notification.Builder(this,"switcher").setSmallIcon(org.taskdeck.app.R.drawable.ic_tasks).setContentTitle("TaskDeck").setContentText("Open the controller app switcher").setContentIntent(open).setOngoing(true).build());
    }
    @Override public void onRequestPermissionsResult(int code,String[] permissions,int[] grants){ super.onRequestPermissionsResult(code,permissions,grants); if(code==1 && grants.length>0 && grants[0]==PackageManager.PERMISSION_GRANTED) notification(); }
    @Override public boolean dispatchKeyEvent(KeyEvent event){
        int code=event.getKeyCode(); boolean swapped=prefs().getBoolean("swapped",false);
        int a=swapped?97:96,b=swapped?96:97,x=swapped?100:99,y=swapped?99:100;
        if(code!=a && code!=b && code!=x && code!=y) return super.dispatchKeyEvent(event);
        if(event.getAction()==KeyEvent.ACTION_DOWN && event.getRepeatCount()==0){
            if(code==b) finish();
            else if(code==a){ if(list.hasFocus()) open(); else { View focus=getCurrentFocus(); if(focus!=null) focus.performClick(); } }
            else if(list.hasFocus()){ if(code==x) closeSelected(); else closeAll(); }
        }
        return true;
    }
    private void move(int delta){ if(list.hasFocus() && !tasks.isEmpty()) list.setSelection(Math.max(0,Math.min(tasks.size()-1,list.getSelectedItemPosition()+delta))); }
    @Override public boolean onGenericMotionEvent(MotionEvent event){
        if(!event.isFromSource(InputDevice.SOURCE_JOYSTICK) || !list.hasFocus()) return super.onGenericMotionEvent(event);
        float y=event.getAxisValue(MotionEvent.AXIS_HAT_Y); if(Math.abs(y)<.55f) y=event.getAxisValue(MotionEvent.AXIS_Y);
        int next=y>.55f?1:y<-.55f?-1:0;
        if(next!=stick){ stick=next; handler.removeCallbacks(repeat); if(next!=0){ move(next); handler.postDelayed(repeat,350); } }
        return true;
    }
    @Override public void onWindowFocusChanged(boolean focused){ super.onWindowFocusChanged(focused); if(!focused){ stick=0; handler.removeCallbacks(repeat); } }
}
