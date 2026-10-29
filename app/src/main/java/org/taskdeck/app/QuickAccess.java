package org.taskdeck.app;
import android.content.SharedPreferences;
import org.json.*;
import java.util.*;

/** Ordered launch shortcuts, independent of tasks and protection policy. */
public final class QuickAccess {
    private QuickAccess(){}
    public static List<String> read(SharedPreferences prefs){
        List<String> values=new ArrayList<>(); try { JSONArray data=new JSONArray(prefs.getString("quickAccess","[]")); for(int i=0;i<data.length();i++){ String name=data.optString(i); if(name.matches("[A-Za-z0-9_.]+")&&!values.contains(name)) values.add(name); } } catch(JSONException ignored){} return values;
    }
    public static void save(SharedPreferences prefs,List<String> values){ prefs.edit().putString("quickAccess",new JSONArray(new LinkedHashSet<>(values)).toString()).apply(); }
    public static void toggle(SharedPreferences prefs,String name){ List<String> values=read(prefs); if(!values.remove(name)) values.add(name); save(prefs,values); }
    public static List<String> move(List<String> source,String name,int target){ List<String> values=new ArrayList<>(source); if(values.remove(name)) values.add(Math.max(0,Math.min(target,values.size())),name); return values; }
    public static int size(SharedPreferences prefs){ return Math.max(60,Math.min(160,prefs.getInt("dockSize",100))); }
}
