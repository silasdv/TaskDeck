package org.taskdeck.app;
import android.os.Handler;
import org.json.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/** Bounded deadlines and exactly one completion; late Binder results cannot overwrite newer UI. */
public final class BackendRequests {
    private BackendRequests(){}
    public static JSONObject error(String message){ try { return new JSONObject().put("error",message); } catch(JSONException impossible){ throw new IllegalArgumentException(impossible); } }
    public static JSONObject uncertain(String message){ try { return error(message).put("uncertain",true); } catch(JSONException impossible){ throw new IllegalArgumentException(impossible); } }
    public static void submit(Handler main,Executor executor,long timeoutMs,Callable<JSONObject> operation,Consumer<JSONObject> complete,Runnable expired){
        AtomicBoolean delivered=new AtomicBoolean(); Runnable[] deadline=new Runnable[1];
        FutureTask<Void> job=new FutureTask<>(()->{
            JSONObject result; try { result=operation.call(); } catch(Exception failure){ result=error(failure.getClass().getSimpleName()+": "+String.valueOf(failure.getMessage())); }
            JSONObject value=result; main.post(()->{ if(delivered.compareAndSet(false,true)){ main.removeCallbacks(deadline[0]); complete.accept(value); } }); return null;
        });
        deadline[0]=()->{ if(delivered.compareAndSet(false,true)){ job.cancel(true); expired.run(); } };
        main.postDelayed(deadline[0],timeoutMs);
        try { executor.execute(job); } catch(RejectedExecutionException busy){ if(delivered.compareAndSet(false,true)){ main.removeCallbacks(deadline[0]); main.post(()->complete.accept(error("Service is busy; try again shortly"))); } }
    }
}
