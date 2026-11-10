package org.taskdeck.app;
import android.content.Context;
import android.view.KeyEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import java.util.Set;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=33,application=DirectShortcutTest.TestApp.class)
public class DirectShortcutTest {
    public static class TestApp extends TaskDeckApp { int switches; @Override public void onCreate(){} @Override void quickSwitchFromShortcut(android.os.ResultReceiver reply){ assertNotNull(reply); switches++; } }
    @Test public void directChordSwitchesOnceWithoutOpeningDeckAndMenuChordStillWorks(){
        TestApp app=(TestApp)RuntimeEnvironment.getApplication(); HotkeyService service=Robolectric.buildService(HotkeyService.class).create().get();
        service.getSharedPreferences("settings",Context.MODE_PRIVATE).edit().putString("shortcutChord","138:0,139:0").putBoolean("shortcutEnabled",true).putString("quickSwitchChord","140:0,141:0").putBoolean("quickSwitchEnabled",true).commit();
        assertFalse(service.onKeyEvent(new KeyEvent(0,140))); assertTrue(service.onKeyEvent(new KeyEvent(0,141))); Shadows.shadowOf(android.os.Looper.getMainLooper()).idle(); assertEquals(1,app.switches); assertNull(Shadows.shadowOf(service).getNextStartedActivity());
        assertTrue(service.onKeyEvent(new KeyEvent(0,1,0,141,1))); assertEquals(1,app.switches);
        assertTrue(service.onKeyEvent(new KeyEvent(1,141))); assertFalse(service.onKeyEvent(new KeyEvent(1,140)));
        service.onKeyEvent(new KeyEvent(0,138)); service.onKeyEvent(new KeyEvent(0,139)); assertNotNull(Shadows.shadowOf(service).getNextStartedActivity()); assertEquals(1,app.switches); service.onDestroy(); app.worker.shutdownNow(); app.memoryWorker.shutdownNow();
    }
    @Test public void nestedChordsAreRejectedButSharedPartialKeysAreAllowed(){
        assertTrue(ShortcutKeys.conflicts(Set.of("138:0"),Set.of("138:0","139:0")));
        assertTrue(ShortcutKeys.conflicts(Set.of("138:0","139:0"),Set.of("138:0","139:0")));
        assertFalse(ShortcutKeys.conflicts(Set.of("138:0","140:0"),Set.of("138:0","139:0")));
        assertFalse(ShortcutKeys.conflicts(Set.of(),Set.of("138:0")));
    }
}
