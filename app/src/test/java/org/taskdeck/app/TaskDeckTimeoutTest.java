package org.taskdeck.app;
import android.os.*;
import org.json.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=33,application=TaskDeckTimeoutTest.TestApp.class)
public class TaskDeckTimeoutTest {
    public static class TestApp extends TaskDeckApp { @Override public void onCreate(){} @Override public void connect(boolean request){} }
    @Test public void recoveryRotatesBlockedWorkerDoesNotReplayAndRejectsLateCompletion() throws Exception {
        TestApp app=(TestApp)RuntimeEnvironment.getApplication(); ExecutorService old=app.worker; CountDownLatch started=new CountDownLatch(1),release=new CountDownLatch(1); AtomicInteger invoked=new AtomicInteger(),delivered=new AtomicInteger(); AtomicReference<JSONObject> last=new AtomicReference<>();
        try {
            app.request("Close test",100,()->{ invoked.incrementAndGet(); started.countDown(); while(release.getCount()>0) try { release.await(); } catch(InterruptedException ignored){} return new JSONObject().put("old",true); },result->{ last.set(result); delivered.incrementAndGet(); });
            assertTrue(started.await(2,TimeUnit.SECONDS)); Shadows.shadowOf(Looper.getMainLooper()).idleFor(101,TimeUnit.MILLISECONDS);
            assertNotSame(old,app.worker); assertTrue(old.isShutdown()); assertTrue(last.get().optBoolean("uncertain")); assertEquals(1,invoked.get());
            app.request("Read again",1000,()->new JSONObject().put("fresh",true),result->{ last.set(result); delivered.incrementAndGet(); });
            app.worker.submit(()->{}).get(2,TimeUnit.SECONDS); Shadows.shadowOf(Looper.getMainLooper()).idle(); assertTrue(last.get().getBoolean("fresh"));
            release.countDown(); old.awaitTermination(2,TimeUnit.SECONDS); Shadows.shadowOf(Looper.getMainLooper()).idle(); assertEquals(2,delivered.get()); assertEquals(1,invoked.get()); assertFalse(last.get().has("old"));
        } finally { release.countDown(); old.shutdownNow(); app.worker.shutdownNow(); app.memoryWorker.shutdownNow(); }
    }
}
