package org.taskdeck.app;

import android.app.ActivityManager;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import org.json.*;
import java.lang.reflect.*;
import java.util.*;

/** Runs only as a Shizuku UserService, with shell/root identity. No arbitrary command endpoint. */
// Shizuku documents unrestricted non-SDK access in UserService, not in the normal app process.
// Firmware API availability remains runtime-checked and errors are returned to the UI.
@android.annotation.SuppressLint({"BlockedPrivateApi","PrivateApi"})
public class TaskBackend extends ITaskBackend.Stub {
    private final Context context;
    public TaskBackend(Context context) { this.context=context; }
    private Object manager() throws Exception { return Class.forName("android.app.ActivityTaskManager").getDeclaredMethod("getService").invoke(null); }
    private Object call(Object receiver,String name,Class<?>[] types,Object... args) throws Exception {
        Method method=receiver.getClass().getMethod(name,types); method.setAccessible(true); return method.invoke(receiver,args);
    }
    @SuppressWarnings("unchecked") private List<ActivityManager.RecentTaskInfo> tasks() throws Exception {
        if(android.os.Process.myUid()!=0 && android.os.Process.myUid()!=2000) throw new SecurityException("Task backend must run as Shizuku shell/root, not the normal app process.");
        Object slice=call(manager(),"getRecentTasks",new Class[]{int.class,int.class,int.class},100,0,user());
        return (List<ActivityManager.RecentTaskInfo>)call(slice,"getList",new Class[]{});
    }
    private int user() throws Exception { return (Integer)call(UserHandle.getUserHandleForUid(context.getApplicationInfo().uid),"getIdentifier",new Class[]{}); }
    private String home() {
        ResolveInfo info=context.getPackageManager().resolveActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),PackageManager.MATCH_DEFAULT_ONLY);
        return info==null || info.activityInfo==null ? "" : info.activityInfo.packageName;
    }
    private Set<String> inputMethods() {
        Set<String> packages=new HashSet<>();
        for(ResolveInfo info:context.getPackageManager().queryIntentServices(new Intent("android.view.InputMethod"),0)) if(info.serviceInfo!=null) packages.add(info.serviceInfo.packageName);
        return packages;
    }
    private String packageOf(ActivityManager.RecentTaskInfo task) {
        ComponentName component=task.baseActivity!=null ? task.baseActivity : task.baseIntent==null ? null : task.baseIntent.getComponent();
        return component==null ? "" : component.getPackageName();
    }
    private ApplicationInfo app(String name) throws Exception { return context.getPackageManager().getApplicationInfo(name,0); }
    private boolean removable(ActivityManager.RecentTaskInfo task,String home,Set<String> inputMethods) throws Exception {
        String name=packageOf(task); ApplicationInfo app=app(name);
        // An app containing a foreign top activity is kept out of destructive operations.
        boolean foreign=task.topActivity!=null && !name.equals(task.topActivity.getPackageName());
        int taskUser=task.getClass().getField("userId").getInt(task);
        // Protect every discovered IME; avoid ContentResolver, which is unsupported in UserService.
        return !inputMethods.isEmpty() && !foreign && TaskPolicy.canRemove(name,(app.flags&ApplicationInfo.FLAG_SYSTEM)!=0,taskUser,user(),home,"",inputMethods);
    }
    private ActivityManager.RecentTaskInfo find(int id,String expected) throws Exception {
        for(ActivityManager.RecentTaskInfo task:tasks()) if(task.taskId==id && packageOf(task).equals(expected)) return task;
        throw new IllegalStateException("Task changed or is no longer available. Refresh and try again.");
    }
    private String error(Exception exception) {
        Throwable cause=exception; while(cause.getCause()!=null) cause=cause.getCause();
        return new JSONObject(Collections.singletonMap("error",cause.getClass().getSimpleName()+": "+String.valueOf(cause.getMessage()))).toString();
    }
    @Override public synchronized String readTasks() {
        try {
            JSONArray rows=new JSONArray();
            String home=home(); Set<String> inputMethods=inputMethods();
            for(ActivityManager.RecentTaskInfo task:tasks()) {
                String name=packageOf(task); if(name.isEmpty() || name.equals(context.getPackageName())) continue;
                ApplicationInfo info; try { info=app(name); } catch(PackageManager.NameNotFoundException ignored) { continue; }
                rows.put(new JSONObject().put("id",task.taskId).put("package",name).put("label",context.getPackageManager().getApplicationLabel(info).toString()).put("removable",removable(task,home,inputMethods)));
            }
            return new JSONObject().put("uid",android.os.Process.myUid()).put("sdk",Build.VERSION.SDK_INT).put("tasks",rows).toString();
        } catch(Exception e) { return error(e); }
    }
    @Override public synchronized String openTask(int id,String expected) {
        try {
            find(id,expected);
            Object result=call(manager(),"startActivityFromRecents",new Class[]{int.class,Bundle.class},id,null);
            if(!(result instanceof Integer) || (((Integer)result)!=0 && ((Integer)result)!=2 && ((Integer)result)!=3)) throw new IllegalStateException("Android did not confirm task start: "+result);
            return "{}";
        } catch(Exception e) { return error(e); }
    }
    @Override public synchronized String removeTask(int id,String expected) {
        try {
            ActivityManager.RecentTaskInfo task=find(id,expected);
            if(!removable(task,home(),inputMethods())) throw new SecurityException("Protected task: removal is disabled (including when input-method discovery is unavailable).");
            boolean removed=(Boolean)call(manager(),"removeTask",new Class[]{int.class},id);
            if(!removed) throw new IllegalStateException("Android did not remove this task.");
            for(ActivityManager.RecentTaskInfo remaining:tasks()) if(remaining.taskId==id) throw new IllegalStateException("Task is still listed after removal. Refresh to check its state.");
            return "{}";
        } catch(Exception e) { return error(e); }
    }
    @Override public void destroy() { System.exit(0); }
}
