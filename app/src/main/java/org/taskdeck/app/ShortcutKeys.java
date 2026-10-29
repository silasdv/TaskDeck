package org.taskdeck.app;

import android.view.*;
import java.util.*;

public final class ShortcutKeys {
    private ShortcutKeys(){}
    public static String id(KeyEvent event){ return event.getKeyCode()+":"+event.getScanCode(); }
    public static boolean eligible(KeyEvent event){
        int key=event.getKeyCode();
        return KeyEvent.isGamepadButton(key) || key>=KeyEvent.KEYCODE_F1 && key<=KeyEvent.KEYCODE_F12 || event.isFromSource(InputDevice.SOURCE_GAMEPAD) || event.isFromSource(InputDevice.SOURCE_JOYSTICK);
    }
    public static Set<String> decode(String value){
        Set<String> keys=new LinkedHashSet<>();
        if(value!=null) for(String key:value.split(",")) if(key.matches("\\d+:\\d+")) keys.add(key);
        return keys;
    }
    public static String encode(Collection<String> keys){ return String.join(",",keys); }
    public static boolean conflicts(Set<String> a,Set<String> b){ return !a.isEmpty()&&!b.isEmpty()&&(a.containsAll(b)||b.containsAll(a)); }
    public static String label(String key){
        try {
            String[] parts=key.split(":"); int code=Integer.parseInt(parts[0]),scan=Integer.parseInt(parts[1]);
            String name=KeyEvent.keyCodeToString(code).replace("KEYCODE_","").replace("BUTTON_","Button ").replace('_',' ');
            return name+(scan==0?"":" [scan "+scan+"]");
        } catch(RuntimeException e){ return "Unknown button"; }
    }
    public static String labels(Collection<String> keys){
        List<String> names=new ArrayList<>(); for(String key:keys) names.add(label(key)); return names.isEmpty()?"Not configured":String.join(" + ",names);
    }
}
