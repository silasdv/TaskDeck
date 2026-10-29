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
    private Set<String> userProtected=Collections.emptySet();
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
    private Set<String> protectedPackages(){
        Set<String> result=inputMethods();
        if(result.isEmpty()) result.add("!input-method-discovery-unavailable");
        for(ResolveInfo info:context.getPackageManager().queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),0)) if(info.activityInfo!=null) result.add(info.activityInfo.packageName);
        // Wave can be used as a frontend without holding Android's default HOME role.
        for(ResolveInfo info:context.getPackageManager().queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),0)){
            if(info.activityInfo!=null){
                String label=info.loadLabel(context.getPackageManager()).toString().replace(" ","");
                if(label.equalsIgnoreCase("WaveLauncher")) result.add(info.activityInfo.packageName);
            }
        }
        return result;
    }
    private String packageOf(ActivityManager.RecentTaskInfo task) {
        ComponentName component=task.baseActivity!=null ? task.baseActivity : task.baseIntent==null ? null : task.baseIntent.getComponent();
        return component==null ? "" : component.getPackageName();
    }
    private ApplicationInfo app(String name) throws Exception { return context.getPackageManager().getApplicationInfo(name,0); }
    private boolean removable(ActivityManager.RecentTaskInfo task,String home,Set<String> protectedPackages) throws Exception {
        String name=packageOf(task); ApplicationInfo app=app(name);
        int taskUser=task.getClass().getField("userId").getInt(task);
        // Protect every discovered IME; avoid ContentResolver, which is unsupported in UserService.
        return !protectedPackages.contains("!input-method-discovery-unavailable") && !home.isEmpty() && TaskPolicy.canRemove(name,(app.flags&ApplicationInfo.FLAG_SYSTEM)!=0,taskUser,user(),home,"",protectedPackages);
    }
    private ActivityManager.RecentTaskInfo find(int id,String expected) throws Exception {
        for(ActivityManager.RecentTaskInfo task:tasks()) if(task.taskId==id && packageOf(task).equals(expected)) return task;
        throw new IllegalStateException("Task changed or is no longer available. Refresh and try again.");
    }
    private String error(Exception exception) {
        Throwable cause=exception; while(cause.getCause()!=null) cause=cause.getCause();
        return new JSONObject(Collections.singletonMap("error",cause.getClass().getSimpleName()+": "+String.valueOf(cause.getMessage()))).toString();
    }
    private static final class Ram {
        long kb; int processes,missing; boolean shared;
    }
    @SuppressWarnings("unchecked") private Map<String,Ram> appMemory(Set<String> names) throws Exception {
        Object am=Class.forName("android.app.ActivityManager").getDeclaredMethod("getService").invoke(null);
        List<ActivityManager.RunningAppProcessInfo> running=(List<ActivityManager.RunningAppProcessInfo>)call(am,"getRunningAppProcesses",new Class[]{});
        if(running==null) throw new IllegalStateException("Process information unavailable");
        List<ActivityManager.RunningAppProcessInfo> relevant=new ArrayList<>(); Set<Integer> seen=new HashSet<>();
        for(ActivityManager.RunningAppProcessInfo process:running){
            if(process.pkgList==null || process.uid/100000!=user()) continue;
            for(String name:process.pkgList) if(names.contains(name) && seen.add(process.pid)){ relevant.add(process); break; }
        }
        Map<String,Ram> result=new HashMap<>(); for(String name:names) result.put(name,new Ram());
        if(relevant.isEmpty()) return result;
        int[] pids=new int[relevant.size()]; for(int i=0;i<pids.length;i++) pids[i]=relevant.get(i).pid;
        Debug.MemoryInfo[] samples=(Debug.MemoryInfo[])call(am,"getProcessMemoryInfo",new Class[]{int[].class},(Object)pids);
        for(int i=0;i<relevant.size();i++){
            ActivityManager.RunningAppProcessInfo process=relevant.get(i);
            int pss=samples!=null && i<samples.length && samples[i]!=null?samples[i].getTotalPss():0;
            for(String name:new HashSet<>(Arrays.asList(process.pkgList))){
                Ram ram=result.get(name); if(ram==null) continue;
                ram.processes++; ram.shared|=process.pkgList.length>1;
                if(pss>0) ram.kb+=pss; else ram.missing++;
            }
        }
        return result;
    }
    private JSONObject previousTask(List<ActivityManager.RecentTaskInfo> recent,String home){
        try {
            Object manager=manager(),value;
            try { value=call(manager,"getTasks",new Class[]{int.class,boolean.class,boolean.class},100,false,false); }
            catch(NoSuchMethodException newer){ value=call(manager,"getTasks",new Class[]{int.class,boolean.class,boolean.class,int.class},100,false,false,-1); }
            for(Object item:(List<?>)value){
                ActivityManager.RunningTaskInfo task=(ActivityManager.RunningTaskInfo)item;
                ComponentName component=task.baseActivity; if(component==null||component.getPackageName().equals(context.getPackageName())) continue;
                if(task.getClass().getField("userId").getInt(task)!=user()) continue;
                String name=component.getPackageName();
                // Only return an identifiable recent task or HOME. Other surfaces are not guessed.
                if(name.equals(home)) return new JSONObject().put("home",true).put("package",name).put("id",task.taskId);
                for(ActivityManager.RecentTaskInfo candidate:recent) if(candidate.taskId==task.taskId) return new JSONObject().put("id",task.taskId).put("package",name);
            }
        } catch(Exception unavailable){ /* Firmware variation: fall back to recent order. */ }
        for(ActivityManager.RecentTaskInfo task:recent) if(!packageOf(task).equals(context.getPackageName())&&!packageOf(task).isEmpty()){
            try { return new JSONObject().put("id",task.taskId).put("package",packageOf(task)).put("fallback",true); } catch(JSONException ignored){}
        } return null;
    }
    @Override public synchronized String readTasks() {
        try {
            JSONArray rows=new JSONArray();
            List<ActivityManager.RecentTaskInfo> recent=tasks();
            String home=home(); Set<String> protectedApps=protectedPackages();
            Set<String> names=new HashSet<>(); for(ActivityManager.RecentTaskInfo task:recent) names.add(packageOf(task));
            Map<String,Ram> ram; try { ram=appMemory(names); } catch(Exception unavailable){ ram=Collections.emptyMap(); }
            int rank=0;
            for(ActivityManager.RecentTaskInfo task:recent) {
                String name=packageOf(task); if(name.isEmpty() || name.equals(context.getPackageName())) continue;
                ApplicationInfo info; try { info=app(name); } catch(PackageManager.NameNotFoundException ignored) { continue; }
                Ram sample=ram.get(name);
                String memoryState=sample==null?"unavailable":sample.processes==0?"not-reported":sample.missing>0?"partial":sample.shared?"shared":"sampled";
                long pss=sample==null || sample.processes==0 || sample.missing>0?-1:sample.kb;
                boolean mandatory=!removable(task,home,protectedApps),manual=userProtected.contains(name);
                rows.put(new JSONObject().put("id",task.taskId).put("package",name).put("label",context.getPackageManager().getApplicationLabel(info).toString())
                    .put("recentRank",rank++).put("removable",!mandatory && !manual).put("mandatoryProtected",mandatory).put("userProtected",manual).put("pssKb",pss).put("memoryState",memoryState));
            }
            return new JSONObject().put("uid",android.os.Process.myUid()).put("sdk",Build.VERSION.SDK_INT).put("tasks",rows).put("previousTask",previousTask(recent,home)).toString();
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
            if(userProtected.contains(expected) || !removable(task,home(),protectedPackages())) throw new SecurityException("Protected task: removal is disabled.");
            boolean removed=(Boolean)call(manager(),"removeTask",new Class[]{int.class},id);
            if(!removed) throw new IllegalStateException("Android did not remove this task.");
            for(ActivityManager.RecentTaskInfo remaining:tasks()) if(remaining.taskId==id) throw new IllegalStateException("Task is still listed after removal. Refresh to check its state.");
            return "{}";
        } catch(Exception e) { return error(e); }
    }
    @Override public synchronized void setProtectedPackages(String[] packages){
        Set<String> next=new HashSet<>(); if(packages!=null) for(String name:packages) if(name!=null && name.matches("[A-Za-z0-9_.]+")) next.add(name);
        userProtected=next;
    }
    @Override public void destroy() { System.exit(0); }
}
