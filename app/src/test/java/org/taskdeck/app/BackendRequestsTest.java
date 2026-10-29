package org.taskdeck.app;
import android.app.Application;
import android.os.*;
import org.json.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=33,application=Application.class)
public class BackendRequestsTest {
    @Test public void timeoutDeliversOnceAndLateUninterruptibleResultIsIgnored() throws Exception {
        ExecutorService worker=Executors.newSingleThreadExecutor(); CountDownLatch started=new CountDownLatch(1),release=new CountDownLatch(1); AtomicInteger complete=new AtomicInteger(),expired=new AtomicInteger();
        try {
            BackendRequests.submit(new Handler(Looper.getMainLooper()),worker,100,()->{
                started.countDown(); while(release.getCount()>0) try { release.await(); } catch(InterruptedException ignored){} return new JSONObject();
            },result->complete.incrementAndGet(),expired::incrementAndGet);
            assertTrue(started.await(2,TimeUnit.SECONDS)); Shadows.shadowOf(Looper.getMainLooper()).idleFor(101,TimeUnit.MILLISECONDS);
            assertEquals(1,expired.get()); assertEquals(0,complete.get()); release.countDown(); worker.submit(()->{}).get(2,TimeUnit.SECONDS); Shadows.shadowOf(Looper.getMainLooper()).idle(); assertEquals(0,complete.get());
        } finally { release.countDown(); worker.shutdownNow(); }
    }
    @Test public void successCancelsDeadlineAndRejectedExecutorReportsAnError() throws Exception {
        ExecutorService worker=Executors.newSingleThreadExecutor(); AtomicInteger complete=new AtomicInteger(),expired=new AtomicInteger();
        BackendRequests.submit(new Handler(Looper.getMainLooper()),worker,100,JSONObject::new,result->complete.incrementAndGet(),expired::incrementAndGet);
        worker.submit(()->{}).get(2,TimeUnit.SECONDS); Shadows.shadowOf(Looper.getMainLooper()).idleFor(200,TimeUnit.MILLISECONDS); assertEquals(1,complete.get()); assertEquals(0,expired.get()); worker.shutdownNow();
        AtomicReference<JSONObject> rejected=new AtomicReference<>(); BackendRequests.submit(new Handler(Looper.getMainLooper()),worker,100,JSONObject::new,rejected::set,expired::incrementAndGet); Shadows.shadowOf(Looper.getMainLooper()).idle(); assertTrue(rejected.get().has("error"));
    }
}
