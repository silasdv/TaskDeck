package org.taskdeck.app;

import android.app.Application;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=33,application=Application.class)
public class BackendBoundaryTest {
    @Test public void normalAppIdentityCannotReadOrRemoveForeignTasks() throws Exception {
        TaskBackend backend=new TaskBackend(RuntimeEnvironment.getApplication());
        assertTrue(new JSONObject(backend.readTasks()).getString("error").contains("Shizuku shell/root"));
        assertTrue(new JSONObject(backend.removeTask(123,"test.game")).has("error"));
        assertTrue(new JSONObject(backend.openTask(123,"test.game")).has("error"));
        assertTrue(new JSONObject(backend.stopLauncher("test.game",true)).getString("error").contains("Shizuku shell/root"));
        assertTrue(new JSONObject(backend.readMemory()).has("error"));
        assertTrue(new JSONObject(backend.switchPreviousTask()).has("error"));
        assertTrue(new JSONObject(backend.verifyLauncherRestart("test.game",new int[]{1})).has("error"));
    }
}
