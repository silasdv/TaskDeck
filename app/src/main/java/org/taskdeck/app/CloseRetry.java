package org.taskdeck.app;
import android.os.SystemClock;
import org.json.*;
import java.util.Locale;
import java.util.concurrent.Callable;
/** One explicit-user-authorized retry, only after rechecking the original task identity. */
final class CloseRetry {
    static JSONObject retry(ITaskBackend backend,JSONObject task,JSONObject result,long deadline,Callable<Void> prepare){
        if(result.optBoolean("uncertain")||!result.has("error")&&result.optBoolean("verified",true)||Thread.currentThread().isInterrupted()||SystemClock.uptimeMillis()+100>=deadline)return result;
        if(result.optBoolean("requested")&&result.optString("warning").toLowerCase(Locale.ROOT).contains("unavailable"))return result;
        String error=result.optString("error").toLowerCase(Locale.ROOT);if(error.contains("protect")||error.contains("permission")||error.contains("security"))return result;
        try {Thread.sleep(50);if(Thread.currentThread().isInterrupted()||SystemClock.uptimeMillis()>=deadline)return result;prepare.call();JSONObject snapshot=new JSONObject(backend.readTasks());if(snapshot.has("error")||!snapshot.has("tasks"))return result;JSONArray tasks=snapshot.optJSONArray("tasks");if(tasks==null)return result;JSONObject match=null;for(int i=0;i<tasks.length();i++){JSONObject row=tasks.optJSONObject(i);if(row!=null&&row.optInt("id",-1)==task.optInt("id",-2)&&row.optString("package").equals(task.optString("package")))match=row;}
            if(match==null)return task.optBoolean("launcher")?result:new JSONObject().put("verified",true);
            if(match.optBoolean("mandatoryProtected")||match.optBoolean("userProtected")||!match.optBoolean("removable",true))return result;
            if(SystemClock.uptimeMillis()>=deadline)return result;return new JSONObject(task.optBoolean("launcher")?backend.stopLauncher(task.optString("package"),false):backend.removeTask(task.optInt("id"),task.optString("package")));
        }catch(InterruptedException interrupted){Thread.currentThread().interrupt();return result;}catch(Exception unknown){return result;}
    }
}
