package org.taskdeck.app;

import android.content.ComponentName;
import android.os.Looper;
import android.view.KeyEvent;
import android.widget.ListView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlertDialog;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;

/** UI simulation only. Fake IPC here does not establish firmware/backend compatibility. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=33,application=SwitcherActivityTest.TestApp.class)
public class SwitcherActivityTest {
    public static class TestApp extends TaskDeckApp {
        @Override public void onCreate() {}
        @Override public void connect(boolean request) {}
    }
    static class FakeBackend extends ITaskBackend.Stub {
        int removals;
        public String readTasks(){ return "{\"uid\":2000,\"sdk\":33,\"tasks\":[{\"id\":7,\"package\":\"test.game\",\"label\":\"Test game\",\"removable\":true}]}"; }
        public String openTask(int id,String name){ return "{}"; }
        public String removeTask(int id,String name){ removals++; return "{}"; }
        public void destroy() {}
    }
    @Test public void closeAllRequiresConfirmationAndControllerBCancels() throws Exception {
        TestApp app=(TestApp)RuntimeEnvironment.getApplication(); FakeBackend backend=new FakeBackend(); app.backend=backend;
        var controller=Robolectric.buildActivity(SwitcherActivity.class).setup().visible(); controller.windowFocusChanged(true);
        app.worker.submit(()->{}).get(5,TimeUnit.SECONDS); Shadows.shadowOf(Looper.getMainLooper()).idle();
        SwitcherActivity activity=controller.get();
        var field=SwitcherActivity.class.getDeclaredField("list"); field.setAccessible(true); ListView list=(ListView)field.get(activity);
        assertEquals(1,list.getCount()); list.setSelection(0); list.requestFocus();
        activity.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_BUTTON_Y));
        var dialog=ShadowAlertDialog.getLatestAlertDialog(); assertNotNull(dialog); assertTrue(dialog.isShowing()); assertEquals(0,backend.removals);
        dialog.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_BUTTON_B));
        assertFalse(dialog.isShowing()); assertEquals(0,backend.removals);
        controller.pause().stop().destroy(); app.worker.shutdownNow();
    }
}
