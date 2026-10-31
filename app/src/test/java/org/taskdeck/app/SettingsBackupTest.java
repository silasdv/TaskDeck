package org.taskdeck.app;
import android.content.SharedPreferences;
import org.json.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import java.util.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=33)
public class SettingsBackupTest {
    private SharedPreferences prefs(){return RuntimeEnvironment.getApplication().getSharedPreferences("backup-test",0);}
    private String document(JSONObject settings) throws Exception{return new JSONObject().put("format","TaskDeck settings").put("version",1).put("settings",settings).toString();}
    @Test public void roundTripRestoresCustomizationButPreservesRuntimeAndPermissionState() throws Exception {
        var p=prefs();p.edit().clear().putString("theme","oled").putString("accentColor","theme").putString("dockStyle","shelf").putInt("dockSize",130).putString("quickAccess","[\"com.one.app\",\"com.two.app\"]").putStringSet("protectedApps",Set.of("com.one.app")).putBoolean("setupComplete",true).putBoolean("pendingRamFeedbackTest",true).commit();
        JSONObject file=SettingsBackup.export(p);assertFalse(file.getJSONObject("settings").has("setupComplete"));assertFalse(file.getJSONObject("settings").has("pendingRamFeedbackTest"));p.edit().putString("theme","light").putInt("dockIconSize",160).putBoolean("pendingRamFeedbackTest",false).commit();SettingsBackup.apply(p,SettingsBackup.validate(file.toString()));assertEquals("oled",p.getString("theme",""));assertEquals(130,p.getInt("dockSize",0));assertFalse(p.contains("dockIconSize"));assertTrue(p.getBoolean("setupComplete",false));assertFalse(p.getBoolean("pendingRamFeedbackTest",true));assertEquals(Set.of("com.one.app"),p.getStringSet("protectedApps",Set.of()));assertEquals(List.of("com.one.app","com.two.app"),QuickAccess.read(p));
    }
    @Test public void rejectsUnknownTypesValuesAndVersions() throws Exception {
        for(JSONObject invalid:List.of(new JSONObject().put("setupComplete",true),new JSONObject().put("theme","garbage"),new JSONObject().put("dockSize","100"),new JSONObject().put("dockSize",200),new JSONObject().put("protectedApps",new JSONArray().put("bad/package"))))assertThrows(JSONException.class,()->SettingsBackup.validate(document(invalid)));
        assertThrows(JSONException.class,()->SettingsBackup.validate("{\"format\":\"TaskDeck settings\",\"version\":\"1\",\"settings\":{}}"));assertThrows(JSONException.class,()->SettingsBackup.validate(" ".repeat(65537)));assertThrows(JSONException.class,()->SettingsBackup.validate("not JSON"));
    }
    @Test public void layoutLimitsRejectOversizedOrDuplicateDock() throws Exception {
        JSONArray apps=new JSONArray();for(int i=0;i<8;i++)apps.put("com.app"+i+".main");JSONObject settings=new JSONObject().put("quickAccess",apps.toString()).put("dockStyle","shelf");assertNotNull(SettingsBackup.validate(document(settings)));settings.put("dockStyle","thumb");assertThrows(JSONException.class,()->SettingsBackup.validate(document(settings)));settings.put("quickAccess","[\"com.same.app\",\"com.same.app\"]");assertThrows(JSONException.class,()->SettingsBackup.validate(document(settings)));
    }
    @Test public void invalidImportNeverPartiallyWritesPreferences() throws Exception {
        var p=prefs();p.edit().clear().putString("theme","oled").putInt("dockSize",110).commit();JSONObject invalid=new JSONObject().put("theme","light").put("dockSize",999);assertThrows(JSONException.class,()->SettingsBackup.apply(p,invalid));assertEquals("oled",p.getString("theme",""));assertEquals(110,p.getInt("dockSize",0));
    }
    @Test public void rejectsConflictingChordsAndDuplicateActionMapping() throws Exception {
        assertThrows(JSONException.class,()->SettingsBackup.validate(document(new JSONObject().put("shortcutChord","99:0").put("quickSwitchChord","99:0,100:0"))));assertThrows(JSONException.class,()->SettingsBackup.validate(document(new JSONObject().put("map_open",97))));assertThrows(JSONException.class,()->SettingsBackup.validate(document(new JSONObject().put("shortcutChord","bad"))));
    }
    @Test public void exportsOnlyExplicitAllowList() throws Exception {
        var p=prefs();p.edit().clear().putString("theme","dark").putString("typedText","do not export").putString("password","never").putString("lastError","runtime").commit();String json=SettingsBackup.export(p).toString();assertTrue(json.contains("dark"));assertFalse(json.contains("password"));assertFalse(json.contains("typedText"));assertFalse(json.contains("runtime"));
    }
}
