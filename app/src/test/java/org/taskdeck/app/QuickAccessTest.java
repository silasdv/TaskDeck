package org.taskdeck.app;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import java.util.*;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=33,application=SwitcherActivityTest.TestApp.class)
public class QuickAccessTest {
    private SharedPreferences prefs(){ return RuntimeEnvironment.getApplication().getSharedPreferences("quick-test",0); }
    @Test public void orderPersistsAndPinningDoesNotAlterProtection(){ var p=prefs(); p.edit().putStringSet("protectedApps",Set.of("locked.app")).commit(); QuickAccess.toggle(p,"one.app"); QuickAccess.toggle(p,"two.app"); QuickAccess.toggle(p,"one.app"); QuickAccess.toggle(p,"one.app"); assertEquals(List.of("two.app","one.app"),QuickAccess.read(p)); assertEquals(Set.of("locked.app"),p.getStringSet("protectedApps",Set.of())); }
    @Test public void movingPinsInEitherDirectionIsBoundedAndKeepsEveryEntry(){ var p=prefs(); QuickAccess.save(p,List.of("a.app","b.app","c.app")); QuickAccess.save(p,QuickAccess.move(QuickAccess.read(p),"a.app",50)); assertEquals(List.of("b.app","c.app","a.app"),QuickAccess.read(p)); assertEquals(List.of("a.app","b.app","c.app"),QuickAccess.move(QuickAccess.read(p),"a.app",-20)); assertEquals(QuickAccess.read(p),QuickAccess.move(QuickAccess.read(p),"missing.app",1)); }
    @Test public void malformedDataAndDuplicatesDoNotCreateBrokenDockItems(){ var p=prefs(); p.edit().putString("quickAccess","bad json").commit(); assertTrue(QuickAccess.read(p).isEmpty()); p.edit().putString("quickAccess","[\"a.app\",\"a.app\",\"../private\",\"b.app\"]").commit(); assertEquals(List.of("a.app","b.app"),QuickAccess.read(p)); }
    @Test public void sizeIsClampedAndAutomaticThemeFollowsAndroidNightMode(){ var p=prefs(); assertEquals(100,QuickAccess.size(p)); p.edit().putInt("dockSize",900).commit(); assertEquals(160,QuickAccess.size(p)); p.edit().putInt("dockSize",0).commit(); assertEquals(60,QuickAccess.size(p)); assertEquals(DeckTheme.named("dark").background,DeckTheme.resolve("auto",Configuration.UI_MODE_NIGHT_YES).background); assertEquals(DeckTheme.named("light").background,DeckTheme.resolve("auto",Configuration.UI_MODE_NIGHT_NO).background); assertEquals(DeckTheme.named("oled").background,DeckTheme.resolve("oled",Configuration.UI_MODE_NIGHT_NO).background); }
}
