package org.taskdeck.app;

import android.app.Application;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import rikka.shizuku.Shizuku;
import java.util.concurrent.*;

public class TaskDeckApp extends Application {
    public static volatile boolean recordingShortcut;
    public volatile ITaskBackend backend;
    public volatile String status="Start Shizuku, then connect.";
    public Runnable changed;
    public final ExecutorService worker=Executors.newSingleThreadExecutor();
    private final Handler main=new Handler(Looper.getMainLooper());
    private boolean binding;
    private int connectionGeneration;
    private final Shizuku.UserServiceArgs args=new Shizuku.UserServiceArgs(new ComponentName("org.taskdeck.app",TaskBackend.class.getName()))
        .daemon(false).processNameSuffix("tasks").debuggable(false).version(7);
    private int pendingRamCount;
    private long ramBefore;
    public volatile String lastMemoryMessage="";
    private final Runnable memoryReport=()->{
        int count=pendingRamCount; pendingRamCount=0;
        if(count<=0||!getSharedPreferences("settings",MODE_PRIVATE).getBoolean("ramNotifications",true)) return;
        android.app.ActivityManager.MemoryInfo info=new android.app.ActivityManager.MemoryInfo(); getSystemService(android.app.ActivityManager.class).getMemoryInfo(info);
        long delta=(info.availMem-ramBefore)/(1024*1024);
        String message="RAM change: "+(delta>=0?"+":"")+delta+" MiB\nAvailable: "+info.availMem/(1024*1024)+" MiB";
        showMemoryFeedback(message,count);
    };
    private void showMemoryFeedback(String message,int count){
        lastMemoryMessage=message;
        android.widget.Toast.makeText(this,message,android.widget.Toast.LENGTH_LONG).show();
        android.app.NotificationManager manager=getSystemService(android.app.NotificationManager.class);
        boolean permission=Build.VERSION.SDK_INT<33||checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED;
        android.app.NotificationChannel old=manager.getNotificationChannel("ram"),channel=manager.getNotificationChannel("ram_results");
        if(permission&&manager.areNotificationsEnabled()&&(old==null||old.getImportance()!=android.app.NotificationManager.IMPORTANCE_NONE)&&(channel==null||channel.getImportance()!=android.app.NotificationManager.IMPORTANCE_NONE)){
            android.app.NotificationChannel results=new android.app.NotificationChannel("ram_results","RAM result popups",android.app.NotificationManager.IMPORTANCE_HIGH); results.enableVibration(false); results.setSound(null,null); manager.createNotificationChannel(results);
            try { manager.notify(2,new android.app.Notification.Builder(this,"ram_results").setSmallIcon(R.drawable.ic_tasks).setContentTitle(count>0?count+" item(s) closed":"RAM feedback test").setContentText(message.replace('\n',' ')).setStyle(new android.app.Notification.BigTextStyle().bigText(message)).setTimeoutAfter(20000).setAutoCancel(true).build()); }
            catch(SecurityException revoked){ /* Permission can change while the result is pending; the toast still works. */ }
        }
    }
    /** Application-owned and coalesced; survives Activity finish/destroy without notification permission. */
    public void reportMemoryResult(int count,long before){
        if(count<=0) return;
        Runnable queue=()->{ if(pendingRamCount==0) ramBefore=before; pendingRamCount+=count; main.removeCallbacks(memoryReport); main.postDelayed(memoryReport,1200); };
        if(Looper.myLooper()==Looper.getMainLooper()) queue.run(); else main.post(queue);
    }
    /** Publish while the Activity is still foreground, then let the system retain it after finish. */
    public void flushMemoryFeedback(){ if(Looper.myLooper()!=Looper.getMainLooper()){ main.post(this::flushMemoryFeedback); return; } main.removeCallbacks(memoryReport); memoryReport.run(); }
    public void testMemoryFeedback(){ android.app.ActivityManager.MemoryInfo info=new android.app.ActivityManager.MemoryInfo(); getSystemService(android.app.ActivityManager.class).getMemoryInfo(info); showMemoryFeedback("RAM feedback test\nAvailable: "+info.availMem/(1024*1024)+" MiB",0); }
    private final ServiceConnection connection=new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName name,IBinder binder) {
            binding=false;
            if(binder==null || !binder.pingBinder()) { disconnected("Task backend did not respond."); return; }
            backend=ITaskBackend.Stub.asInterface(binder); status="Connected. Reading tasks…"; notifyChanged();
        }
        @Override public void onServiceDisconnected(ComponentName name) { disconnected("Task backend disconnected. Reconnect to continue."); }
        @Override public void onBindingDied(ComponentName name) { disconnected("Task backend stopped. Reconnect to continue."); }
    };
    private final Shizuku.OnBinderReceivedListener received=()->main.post(()->{
        status="Shizuku available. Connect to grant access.";
        try { if(Shizuku.checkSelfPermission()==PackageManager.PERMISSION_GRANTED) connect(false); else notifyChanged(); }
        catch(Exception e) { disconnected("Shizuku permission unavailable."); }
    });
    private final Shizuku.OnBinderDeadListener dead=()->main.post(()->disconnected("Shizuku stopped. Start it again, then reconnect."));
    private final Shizuku.OnRequestPermissionResultListener permission=(code,result)->main.post(()->{
        if(result==PackageManager.PERMISSION_GRANTED) connect(false); else disconnected("Shizuku access denied. Enable it in Shizuku to continue.");
    });
    @Override public void onCreate() {
        super.onCreate();
        android.content.SharedPreferences preferences=getSharedPreferences("settings",MODE_PRIVATE);
        if(!preferences.contains("shortcutChord")) preferences.edit().putBoolean("shortcutEnabled",false).apply();
        Shizuku.addBinderReceivedListenerSticky(received); Shizuku.addBinderDeadListener(dead); Shizuku.addRequestPermissionResultListener(permission);
    }
    private void disconnected(String message) { backend=null; binding=false; status=message; notifyChanged(); }
    public void notifyChanged() { main.post(()->{ if(changed!=null) changed.run(); }); }
    public void connect(boolean request) {
        try {
            if(!Shizuku.pingBinder()) { disconnected("Shizuku is not running. Open Shizuku and start it first."); return; }
            if(Shizuku.getVersion()<13) { disconnected("Shizuku 13 or newer is required."); return; }
            if(Shizuku.checkSelfPermission()!=PackageManager.PERMISSION_GRANTED) {
                if(request) Shizuku.requestPermission(1); else disconnected("Connect to grant Shizuku access.");
                return;
            }
            if(backend!=null) { notifyChanged(); return; }
            if(binding) return;
            binding=true; final int generation=++connectionGeneration; status="Connecting to the task backend…"; notifyChanged();
            Shizuku.bindUserService(args,connection);
            main.postDelayed(()->{ if(binding && generation==connectionGeneration) disconnected("Connection timed out. Check Shizuku, then reconnect."); },12000);
        } catch(Exception e) { disconnected("Cannot connect: "+e.getClass().getSimpleName()); }
    }
}
