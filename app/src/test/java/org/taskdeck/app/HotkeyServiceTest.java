package org.taskdeck.app;

import android.app.Application;
import android.content.Context;
import android.view.KeyEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=33,application=Application.class)
public class HotkeyServiceTest {
    @Test public void persistedShortcutWorksWithoutInitializingTheMainApplication(){
        HotkeyService service=Robolectric.buildService(HotkeyService.class).create().get();
        android.os.Bundle configuration=new android.os.Bundle();
        configuration.putString(HotkeyStateProvider.DECK_CHORD,KeyEvent.KEYCODE_F8+":0");
        configuration.putBoolean(HotkeyStateProvider.DECK_ENABLED,true);
        HotkeyStateProvider.publish(service,"configuration",configuration);
        assertTrue(service.onKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_F8)));
        assertNotNull(Shadows.shadowOf(service).getNextStartedActivity());
        assertTrue(service.onKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_F8)));

        configuration.putBoolean(HotkeyStateProvider.DECK_ENABLED,false);
        HotkeyStateProvider.publish(service,"configuration",configuration);
        assertFalse(service.onKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_F8)));
    }
    @Test public void enablingWhileAKeyIsAlreadyHeldDoesNotSwallowItsRelease(){
        HotkeyService service=Robolectric.buildService(HotkeyService.class).create().get();
        service.getSharedPreferences("settings",Context.MODE_PRIVATE).edit().putBoolean("shortcutEnabled",true).putString("shortcutChord",KeyEvent.KEYCODE_F8+":0").commit();
        assertFalse(service.onKeyEvent(new KeyEvent(0,1,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_F8,1)));
        assertFalse(service.onKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_F8)));
        assertNull(Shadows.shadowOf(service).getNextStartedActivity());
    }
    @Test public void disabledShortcutAndNormalGameButtonsPassThrough(){
        HotkeyService service=Robolectric.buildService(HotkeyService.class).create().get();
        assertFalse(service.onKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_BUTTON_A)));
        service.getSharedPreferences("settings",Context.MODE_PRIVATE).edit().putBoolean("shortcutEnabled",true).putString("shortcutChord",KeyEvent.KEYCODE_F8+":0").commit();
        assertFalse(service.onKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_BUTTON_A)));
        assertNull(Shadows.shadowOf(service).getNextStartedActivity());
    }
    @Test public void shortcutOpensOnceAndConsumesItsReleaseEvenAfterDisabling(){
        HotkeyService service=Robolectric.buildService(HotkeyService.class).create().get();
        service.getSharedPreferences("settings",Context.MODE_PRIVATE).edit().putBoolean("shortcutEnabled",true).putString("shortcutChord",KeyEvent.KEYCODE_F8+":0").commit();
        assertTrue(service.onKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_F8)));
        assertNotNull(Shadows.shadowOf(service).getNextStartedActivity());
        assertTrue(service.onKeyEvent(new KeyEvent(0,1,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_F8,1)));
        assertNull(Shadows.shadowOf(service).getNextStartedActivity());
        service.getSharedPreferences("settings",Context.MODE_PRIVATE).edit().putBoolean("shortcutEnabled",false).commit();
        assertTrue(service.onKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_F8)));
        assertFalse(service.onKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_F8)));
    }
}
