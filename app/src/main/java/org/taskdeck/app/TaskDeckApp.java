package org.taskdeck.app;

import android.app.Application;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import rikka.shizuku.Shizuku;
import java.util.concurrent.*;
import org.json.*;
import java.util.function.Consumer;

public class TaskDeckApp extends Application {
    public static volatile boolean recordingShortcut;
    public volatile ITaskBackend backend;
    public volatile String status="Start Shizuku, then connect.";
    public Runnable changed;
    public volatile ExecutorService worker=Executors.newSingleThreadExecutor();
    public final ExecutorService memoryWorker=new ThreadPoolExecutor(0,2,30,TimeUnit.SECONDS,new SynchronousQueue<>());
    public volatile JSONObject lastOperation;
    public volatile String lastFeedbackDelivery="Not tested";
    private int recoveryAttempts;
    private final java.util.concurrent.atomic.AtomicInteger pendingRequests=new java.util.concurrent.atomic.AtomicInteger();
    private final Handler main=new Handler(Looper.getMainLooper());
    private boolean binding,pendingPermissionRequest;
    private int connectionGeneration;
    private final Shizuku.UserServiceArgs args=new Shizuku.UserServiceArgs(new ComponentName("org.taskdeck.app",TaskBackend.class.getName()))
        .daemon(false).processNameSuffix("tasks").debuggable(false).version(8);
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
        lastFeedbackDelivery="Toast requested; notification permission/channel blocked";
        android.app.NotificationManager manager=getSystemService(android.app.NotificationManager.class);
        boolean permission=Build.VERSION.SDK_INT<33||checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED;
        android.app.NotificationChannel old=manager.getNotificationChannel("ram"),channel=manager.getNotificationChannel("ram_results");
        if(permission&&manager.areNotificationsEnabled()&&(old==null||old.getImportance()!=android.app.NotificationManager.IMPORTANCE_NONE)&&(channel==null||channel.getImportance()!=android.app.NotificationManager.IMPORTANCE_NONE)){
            android.app.NotificationChannel results=new android.app.NotificationChannel("ram_results","RAM result popups",android.app.NotificationManager.IMPORTANCE_HIGH); results.enableVibration(false); results.setSound(null,null); manager.createNotificationChannel(results);
            try { manager.notify(2,new android.app.Notification.Builder(this,"ram_results").setSmallIcon(R.drawable.ic_tasks).setContentTitle(count>0?count+" item(s) closed":"RAM feedback test").setContentText(message.replace('\n',' ')).setStyle(new android.app.Notification.BigTextStyle().bigText(message)).setTimeoutAfter(20000).setAutoCancel(true).build()); lastFeedbackDelivery="Toast and notification requested; banner visibility needs device check"; }
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
    protected ExecutorService memoryExecutor(){ return memoryWorker; }
    public void backendHealthy(){ recoveryAttempts=0; }
    public void request(String label,long timeout,Callable<JSONObject> operation,Consumer<JSONObject> complete){
        pendingRequests.incrementAndGet();
        BackendRequests.submit(main,worker,timeout,operation,result->{ pendingRequests.decrementAndGet(); complete.accept(result); },()->{ pendingRequests.decrementAndGet(); recoverBackend(label); complete.accept(BackendRequests.uncertain(label+" timed out; outcome may be unknown. Refresh after reconnect.")); });
    }
    public void requestMemory(String label,long timeout,Callable<JSONObject> operation,Consumer<JSONObject> complete){
        BackendRequests.submit(main,memoryExecutor(),timeout,operation,complete,()->complete.accept(BackendRequests.error(label+" timed out; task controls remain available")));
    }
    public void recoverBackend(String reason){
        boolean manual=reason.equals("Manual reconnect"); if(manual) recoveryAttempts=0;
        ExecutorService old=worker; worker=Executors.newSingleThreadExecutor(); old.shutdownNow();
        ServiceConnection previous=connection; connection=null; connectionGeneration++; backend=null; binding=false;
        if(previous!=null) worker.execute(()->{ try { Shizuku.unbindUserService(args,previous,true); } catch(Exception ignored){} });
        status=manual?"Reconnecting Shizuku…":reason+" timed out. Reconnecting without repeating the action."; notifyChanged();
        if(recoveryAttempts++<1) main.postDelayed(()->connect(false),500); else { status="Repeated timeout. Open Connect to retry manually."; notifyChanged(); }
    }
    public void quickSwitch(){
        if(pendingRequests.get()>0){ android.widget.Toast.makeText(this,"Wait for the current action to finish",android.widget.Toast.LENGTH_SHORT).show(); return; }
        ITaskBackend service=backend;
        if(service==null){ android.widget.Toast.makeText(this,"Connect TaskDeck to Shizuku first",android.widget.Toast.LENGTH_LONG).show(); connect(false); return; }
        request("Direct app switch",5000,()->new JSONObject(service.switchPreviousTask()),result->{
            if(result.has("error")){ android.widget.Toast.makeText(this,result.optString("error"),android.widget.Toast.LENGTH_LONG).show(); return; }
            if(result.optBoolean("home")) try { startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).setPackage(result.optString("package")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); }
            catch(RuntimeException denied){ android.widget.Toast.makeText(this,"Android blocked the HOME switch",android.widget.Toast.LENGTH_LONG).show(); }
        });
    }
    public void verifyRestart(ITaskBackend service,JSONObject task,int[] previous,long before){
        requestMemory("Launcher verification",4000,()->new JSONObject(service.verifyLauncherRestart(task.optString("package"),previous)),result->{
            if(result.has("error")) result=BackendRequests.uncertain("Restart requested; verification unavailable: "+result.optString("error"));
            lastOperation=OperationResults.report(new JSONArray().put(OperationResults.item(task,"Restart launcher",result)));
            android.widget.Toast.makeText(this,lastOperation.optString("summary"),android.widget.Toast.LENGTH_LONG).show();
            if(result.optBoolean("verified",false)&&!result.has("error")){ reportMemoryResult(1,before); flushMemoryFeedback(); }
        });
    }
    private ServiceConnection connection;
    private ServiceConnection newConnection(int generation){ return new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName name,IBinder binder) {
            main.post(()->{
                if(generation!=connectionGeneration) return; binding=false; pendingPermissionRequest=false;
                if(binder==null||!binder.isBinderAlive()){ disconnected("Task backend did not respond."); return; }
                backend=ITaskBackend.Stub.asInterface(binder); status="Connected. Reading tasks…"; notifyChanged();
            });
        }
        @Override public void onServiceDisconnected(ComponentName name) { main.post(()->{ if(generation==connectionGeneration) disconnected("Task backend disconnected. Reconnect to continue."); }); }
        @Override public void onBindingDied(ComponentName name) { main.post(()->{ if(generation==connectionGeneration) disconnected("Task backend stopped. Reconnect to continue."); }); }
    }; }
    private final Shizuku.OnBinderReceivedListener received=()->main.post(()->{
        status="Shizuku available. Connect to grant access.";
        connect(false);
    });
    private final Shizuku.OnBinderDeadListener dead=()->main.post(()->disconnected("Shizuku stopped. Start it again, then reconnect."));
    private final Shizuku.OnRequestPermissionResultListener permission=(code,result)->main.post(()->{
        if(result==PackageManager.PERMISSION_GRANTED){ binding=false; connect(false); } else disconnected("Shizuku access denied. Enable it in Shizuku to continue.");
    });
    @Override public void onCreate() {
        super.onCreate();
        android.content.SharedPreferences preferences=getSharedPreferences("settings",MODE_PRIVATE);
        if(!preferences.contains("shortcutChord")) preferences.edit().putBoolean("shortcutEnabled",false).apply();
        Shizuku.addBinderReceivedListenerSticky(received); Shizuku.addBinderDeadListener(dead); Shizuku.addRequestPermissionResultListener(permission);
    }
    private void disconnected(String message) { connectionGeneration++; backend=null; binding=false; pendingPermissionRequest=false; status=message; notifyChanged(); }
    public void notifyChanged() { main.post(()->{ if(changed!=null) changed.run(); }); }
    protected JSONObject beginUserServiceBinding(ServiceConnection target,boolean request) throws Exception {
        if(!Shizuku.pingBinder()) return BackendRequests.error("Shizuku is not running. Open Shizuku and start it first.");
        if(Shizuku.getVersion()<13) return BackendRequests.error("Shizuku 13 or newer is required.");
        if(Shizuku.checkSelfPermission()!=PackageManager.PERMISSION_GRANTED){
            if(request) Shizuku.requestPermission(1);
            return new JSONObject().put("permissionRequired",true);
        }
        if(Thread.currentThread().isInterrupted()) throw new InterruptedException("Binding cancelled");
        Shizuku.bindUserService(args,target); return new JSONObject().put("binding",true);
    }
    public void connect(boolean request){
        if(request){ recoveryAttempts=0; if(binding){ pendingPermissionRequest=true; return; } }
        if(backend!=null){ notifyChanged(); return; } if(binding) return;
        binding=true; int generation=++connectionGeneration; status="Connecting to the task backend…"; notifyChanged();
        ServiceConnection target=newConnection(generation); connection=target;
        BackendRequests.submit(main,worker,12000,()->beginUserServiceBinding(target,request),result->{
            if(generation!=connectionGeneration) return;
            if(result.has("error")){ disconnected(result.optString("error")); return; }
            if(result.optBoolean("permissionRequired")){
                binding=false;
                if(pendingPermissionRequest&&!request){ pendingPermissionRequest=false; connect(true); return; }
                status="Approve TaskDeck in Shizuku, then connect."; notifyChanged();
            }
        },()->{ if(generation==connectionGeneration&&binding) recoverBackend("Connection"); });
        main.postDelayed(()->{ if(generation==connectionGeneration&&binding) recoverBackend("Connection"); },12000);
    }
}
