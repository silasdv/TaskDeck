package org.taskdeck.app;

import android.content.SharedPreferences;
import java.util.*;
import org.json.*;

/** Versioned, allow-listed local configuration. No runtime status or Android grants. */
final class SettingsBackup {
    static final int MAX_BYTES=65536;
    private static final Set<String> BOOL=Set.of("dockIndicators","dockMagnify","dockSwapSides","exitAfterBulk","groupApps","psLabels","swapped","quickSwitchEnabled","ramNotifications","shortcutEnabled","vibrateClose","confirmCalibrated");
    private static final Set<String> SETS=Set.of("protectedApps","unlockedLaunchers");
    private static final Map<String,List<String>> ENUMS=Map.ofEntries(
        Map.entry("theme",List.of("light","dark","oled","auto")),Map.entry("accentColor",accentKeys()),
        Map.entry("dockBackground",Arrays.asList(QuickAccess.BACKGROUND_KEYS)),Map.entry("dockStyle",List.of("thumb","floating","shelf")),
        Map.entry("dockBehavior",List.of("always","focus")),Map.entry("dockBackgroundShape",List.of("rounded","round","square","squircle")),
        Map.entry("dockIconShape",List.of("round","square","squircle")),Map.entry("dockIconLook",List.of("original","mono","accent","glass")),
        Map.entry("taskIconLook",List.of("original","mono","accent","glass")),Map.entry("view",List.of("fit","grid","list","cards")),Map.entry("sort",List.of("recent","ram","alphabetical")));
    private static List<String> accentKeys(){List<String> keys=new ArrayList<>(Arrays.asList(DeckTheme.ACCENT_KEYS));keys.add("theme");return keys;}
    static JSONObject export(SharedPreferences prefs) throws JSONException {
        JSONObject values=new JSONObject();for(var entry:prefs.getAll().entrySet())if(known(entry.getKey()))values.put(entry.getKey(),entry.getValue() instanceof Set?new JSONArray(new TreeSet<>((Set<String>)entry.getValue())):entry.getValue());
        JSONObject root=new JSONObject().put("format","TaskDeck settings").put("version",1).put("settings",values);validate(root.toString());return root;
    }
    private static boolean known(String k){return BOOL.contains(k)||SETS.contains(k)||ENUMS.containsKey(k)||Set.of("dockSize","dockIconSize","minLabelSp","quickAccess","shortcutChord","quickSwitchChord").contains(k)||k.startsWith("map_")&&(Arrays.asList(ControllerBindings.ACTIONS).contains(k.substring(4))||Arrays.asList(ControllerBindings.PAGE_ACTIONS).contains(k.substring(4)));}
    static JSONObject validate(String json) throws JSONException {
        if(json.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>MAX_BYTES)throw new JSONException("Backup exceeds 64 KB");
        JSONObject root=new JSONObject(json);if(!"TaskDeck settings".equals(root.optString("format"))||!(root.opt("version") instanceof Integer)||root.getInt("version")!=1)throw new JSONException("Unsupported settings format/version");
        JSONObject values=root.getJSONObject("settings"),clean=new JSONObject();if(values.length()==0)throw new JSONException("Backup contains no settings");Iterator<String> keys=values.keys();while(keys.hasNext()){
            String k=keys.next();Object v=values.get(k);if(!known(k))throw new JSONException("Unknown setting: "+k);
            if(BOOL.contains(k)){if(!(v instanceof Boolean))throw new JSONException("Invalid boolean: "+k);}
            else if(SETS.contains(k)){packages(values.getJSONArray(k),256);}
            else if(ENUMS.containsKey(k)){if(!(v instanceof String)||!ENUMS.get(k).contains(v))throw new JSONException("Invalid value: "+k);}
            else if(k.equals("quickAccess")){if(!(v instanceof String))throw new JSONException("Invalid Dock order");packages(new JSONArray((String)v),8);}
            else if(k.endsWith("Chord")){if(!(v instanceof String)||((String)v).length()>128||!((String)v).isEmpty()&&(!((String)v).matches("\\d{1,5}:\\d{1,5}(,\\d{1,5}:\\d{1,5}){0,3}")||ShortcutKeys.decode((String)v).size()>4))throw new JSONException("Invalid shortcut");}
            else {if(!(v instanceof Integer))throw new JSONException("Invalid integer: "+k);int n=(Integer)v;if(k.startsWith("map_")){if(!ControllerBindings.BUTTONS.contains(n))throw new JSONException("Invalid button");}else if(n<(k.equals("minLabelSp")?10:50)||n>(k.equals("minLabelSp")?24:160))throw new JSONException("Invalid size");}
            clean.put(k,v);
        }
        if(clean.has("quickAccess")&&new JSONArray(clean.getString("quickAccess")).length()>(clean.optString("dockStyle","thumb").equals("thumb")?6:8))throw new JSONException("Dock exceeds layout limit");
        if(ShortcutKeys.conflicts(ShortcutKeys.decode(clean.optString("shortcutChord")),ShortcutKeys.decode(clean.optString("quickSwitchChord"))))throw new JSONException("Conflicting shortcuts");
        Set<Integer> used=new HashSet<>();for(String a:ControllerBindings.ACTIONS){int code=clean.optInt("map_"+a,clean.optBoolean("swapped")?ControllerBindings.swapped(ControllerBindings.DEFAULTS[Arrays.asList(ControllerBindings.ACTIONS).indexOf(a)]):ControllerBindings.DEFAULTS[Arrays.asList(ControllerBindings.ACTIONS).indexOf(a)]);if(!used.add(code))throw new JSONException("Duplicate action mapping");}
        return clean;
    }
    private static void packages(JSONArray array,int limit) throws JSONException {if(array.length()>limit)throw new JSONException("Too many apps");Set<String> seen=new HashSet<>();for(int i=0;i<array.length();i++){Object p=array.get(i);if(!(p instanceof String)||!((String)p).matches("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+")||!seen.add((String)p))throw new JSONException("Invalid/duplicate app package");}}
    static void apply(SharedPreferences prefs,JSONObject clean) throws JSONException {
        // Validate again before one atomic edit; preserve setup completion and Android permission state.
        JSONObject checked=validate(new JSONObject().put("format","TaskDeck settings").put("version",1).put("settings",clean).toString());SharedPreferences.Editor edit=prefs.edit();for(String k:prefs.getAll().keySet())if(known(k))edit.remove(k);
        Iterator<String> keys=checked.keys();while(keys.hasNext()){String k=keys.next();Object v=checked.get(k);if(v instanceof Boolean)edit.putBoolean(k,(Boolean)v);else if(v instanceof Integer)edit.putInt(k,(Integer)v);else if(v instanceof JSONArray){Set<String> set=new HashSet<>();for(int i=0;i<((JSONArray)v).length();i++)set.add(((JSONArray)v).getString(i));edit.putStringSet(k,set);}else edit.putString(k,(String)v);}if(!edit.commit())throw new JSONException("Could not save settings");
    }
}
