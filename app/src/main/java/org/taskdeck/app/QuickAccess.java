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
    public static int limit(SharedPreferences prefs){return "thumb".equals(prefs.getString("dockStyle","thumb"))?6:8;}
    public static void save(SharedPreferences prefs,List<String> values){ prefs.edit().putString("quickAccess",new JSONArray(new LinkedHashSet<>(values)).toString()).apply(); }
    public static void toggle(SharedPreferences prefs,String name){ List<String> values=read(prefs); if(!values.remove(name)) values.add(name); save(prefs,values); }
    public static List<String> move(List<String> source,String name,int target){ List<String> values=new ArrayList<>(source); if(values.remove(name)) values.add(Math.max(0,Math.min(target,values.size())),name); return values; }
    public static int leftCount(SharedPreferences prefs,int count){ return prefs.getBoolean("dockSwapSides",false)?count/2:(count+1)/2; }
    public static List<String> displayOrder(SharedPreferences prefs,List<String> source){ List<String> values=new ArrayList<>(source);if("thumb".equals(prefs.getString("dockStyle","thumb"))&&prefs.getBoolean("dockSwapSides",false)) Collections.rotate(values,-(values.size()+1)/2);return values; }
    public static List<String> storageOrder(SharedPreferences prefs,List<String> order){List<String> values=new ArrayList<>(order);if("thumb".equals(prefs.getString("dockStyle","thumb"))&&prefs.getBoolean("dockSwapSides",false)) Collections.rotate(values,(values.size()+1)/2);return values;}
    public static void saveDisplayOrder(SharedPreferences prefs,List<String> order){save(prefs,storageOrder(prefs,order));}
    public static int size(SharedPreferences prefs){ return Math.max(50,Math.min(160,prefs.getInt("dockSize",100))); }
    public static int iconSize(SharedPreferences prefs){ return Math.max(50,Math.min(160,prefs.getInt("dockIconSize",100))); }
    public static int foregroundColor(int background){return android.graphics.Color.luminance(background)>.179?android.graphics.Color.BLACK:android.graphics.Color.WHITE;}
    public static final String[] BACKGROUND_KEYS={"none","theme","black","graphite","white","blue","purple","green","red","amber"};
    public static int backgroundColor(SharedPreferences prefs,DeckTheme theme){
        String key=prefs.getString("dockBackground","theme");
        if("none".equals(key))return android.graphics.Color.TRANSPARENT;
        if("theme".equals(key))return theme.surface;
        if("black".equals(key))return android.graphics.Color.BLACK;
        if("graphite".equals(key))return android.graphics.Color.rgb(104,111,124);
        return DeckTheme.accentColor(key)==0?theme.surface:DeckTheme.accentColor(key);
    }
}
