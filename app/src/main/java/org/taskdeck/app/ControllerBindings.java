package org.taskdeck.app;

import android.content.SharedPreferences;
import java.util.*;

/** Printed-label profiles and conflict-swapping mappings, following PadBoard's convention. */
public final class ControllerBindings {
    public static final String[] ACTIONS={"open","exit","close","clear","others","options"};
    public static final int[] DEFAULTS={96,97,99,100,103,109};
    public static final String[] NAMES={"Open app","Exit menu","Close app","Close all","Close all but current","App options"};
    public static final List<Integer> BUTTONS=List.of(96,97,99,100,102,103,104,105,106,107,108,109);
    private ControllerBindings(){}
    public static int swapped(int code){ switch(code){ case 96:return 97; case 97:return 96; case 99:return 100; case 100:return 99; default:return code; } }
    public static int confirm(SharedPreferences p){ return p.getBoolean("swapped",false)?97:96; }
    public static int back(SharedPreferences p){ return p.getBoolean("swapped",false)?96:97; }
    public static Map<String,Integer> read(SharedPreferences p){
        Map<String,Integer> result=new LinkedHashMap<>(); for(int i=0;i<ACTIONS.length;i++){ int base=p.getBoolean("swapped",false)?swapped(DEFAULTS[i]):DEFAULTS[i]; result.put(ACTIONS[i],p.getInt("map_"+ACTIONS[i],base)); } return result;
    }
    public static int code(SharedPreferences p,String action){ return read(p).get(action); }
    public static Map<String,Integer> assign(Map<String,Integer> original,String action,int code){
        Map<String,Integer> result=new LinkedHashMap<>(original); int old=result.get(action);
        for(String candidate:ACTIONS) if(!candidate.equals(action)&&Objects.equals(result.get(candidate),code)){ result.put(candidate,old); break; }
        result.put(action,code); return result;
    }
    private static void save(SharedPreferences p,Map<String,Integer> values){ SharedPreferences.Editor edit=p.edit(); values.forEach((a,c)->edit.putInt("map_"+a,c)); edit.apply(); }
    public static void assign(SharedPreferences p,String action,int code){ if(!BUTTONS.contains(code)) return; save(p,assign(read(p),action,code)); }
    public static String profile(SharedPreferences p){ return (p.getBoolean("psLabels",false)?"playstation":"abxy")+(p.getBoolean("swapped",false)?"_swapped":""); }
    public static void selectProfile(SharedPreferences p,String profile){
        boolean swap=profile.endsWith("_swapped"); Map<String,Integer> values=read(p);
        if(swap!=p.getBoolean("swapped",false)) values.replaceAll((a,c)->swapped(c));
        p.edit().putBoolean("swapped",swap).putBoolean("psLabels",profile.startsWith("playstation")).apply(); save(p,values);
    }
    public static void calibrate(SharedPreferences p,int received){
        boolean swap=received==97; String style=p.getBoolean("psLabels",false)?"playstation":"abxy"; selectProfile(p,style+(swap?"_swapped":""));
        Map<String,Integer> values=read(p); for(int i=0;i<4;i++) values=assign(values,ACTIONS[i],swap?swapped(DEFAULTS[i]):DEFAULTS[i]); save(p,values); p.edit().putBoolean("confirmCalibrated",true).apply();
    }
    public static void reset(SharedPreferences p){ SharedPreferences.Editor edit=p.edit(); for(String action:ACTIONS) edit.remove("map_"+action); edit.apply(); }
    public static String label(SharedPreferences p,int code){
        int printed=p.getBoolean("swapped",false)?swapped(code):code; boolean ps=p.getBoolean("psLabels",false);
        switch(printed){ case 96:return ps?"Cross":"A"; case 97:return ps?"Circle":"B"; case 99:return ps?"Square":"X"; case 100:return ps?"Triangle":"Y"; case 102:return "L1"; case 103:return "R1"; case 104:return "L2"; case 105:return "R2"; case 106:return "L3"; case 107:return "R3"; case 108:return ps?"Options":"Start"; case 109:return ps?"Share":"Select"; default:return "Button "+code; }
    }
    public static String badge(SharedPreferences p,String action){ return label(p,code(p,action)); }
}
