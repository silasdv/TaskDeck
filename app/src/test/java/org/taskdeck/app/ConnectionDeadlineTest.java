package org.taskdeck.app;
import android.content.ServiceConnection;
import android.os.*;
import org.json.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=33,application=ConnectionDeadlineTest.TestApp.class)
public class ConnectionDeadlineTest {
    public static class TestApp extends TaskDeckApp {
        CountDownLatch started=new CountDownLatch(1),release=new CountDownLatch(1); int recoveries; boolean allow=true;
        @Override public void onCreate(){}
        @Override public void connect(boolean request){ if(allow) super.connect(request); }
        @Override protected JSONObject beginUserServiceBinding(ServiceConnection target,boolean request) throws Exception {
            started.countDown(); while(release.getCount()>0) try { release.await(); } catch(InterruptedException ignored){} return new JSONObject().put("binding",true);
        }
        @Override public void recoverBackend(String reason){ recoveries++; super.recoverBackend(reason); }
    }
    @Test public void blockedBindingLeavesMainThreadResponsiveAndExpiresOnce() throws Exception {
        TestApp app=(TestApp)RuntimeEnvironment.getApplication(); ExecutorService original=app.worker;
        try {
            app.connect(false); assertTrue(app.started.await(2,TimeUnit.SECONDS)); app.allow=false;
            AtomicBoolean responsive=new AtomicBoolean(); new Handler(Looper.getMainLooper()).post(()->responsive.set(true)); Shadows.shadowOf(Looper.getMainLooper()).idle(); assertTrue(responsive.get());
            Shadows.shadowOf(Looper.getMainLooper()).idleFor(12100,TimeUnit.MILLISECONDS); assertEquals(1,app.recoveries); assertNotSame(original,app.worker);
            app.release.countDown(); original.awaitTermination(2,TimeUnit.SECONDS); Shadows.shadowOf(Looper.getMainLooper()).idle(); assertEquals(1,app.recoveries);
        } finally { app.release.countDown(); original.shutdownNow(); app.worker.shutdownNow(); app.memoryWorker.shutdownNow(); }
    }
}
