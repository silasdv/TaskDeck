package org.taskdeck.app;

import java.util.*;

/** Partial chords pass through. Only the completing key's full stream is consumed. */
public final class ChordGate {
    public enum Result { PASS, CONSUME, TRIGGER }
    private Set<String> keys=Collections.emptySet();
    private final Map<Integer,Set<String>> held=new HashMap<>();
    private final Map<Integer,Set<String>> consumed=new HashMap<>();
    private final Set<Integer> latched=new HashSet<>();
    public void configure(Set<String> next){ if(!keys.equals(next)){ keys=new HashSet<>(next); held.clear(); latched.clear(); } }
    public Result update(int device,String key,boolean down,int repeat,boolean enabled){
        Set<String> pressed=held.computeIfAbsent(device,d->new HashSet<>());
        Set<String> swallowed=consumed.computeIfAbsent(device,d->new HashSet<>());
        if(!down){
            pressed.remove(key);
            if(Collections.disjoint(pressed,keys)) latched.remove(device);
            boolean captured=swallowed.remove(key);
            if(pressed.isEmpty()) held.remove(device);
            if(swallowed.isEmpty()) consumed.remove(device);
            return captured?Result.CONSUME:Result.PASS;
        }
        if(swallowed.contains(key)) return Result.CONSUME;
        if(!enabled){ held.clear(); latched.clear(); return Result.PASS; }
        if(repeat>0) return Result.PASS;
        pressed.add(key);
        if(!keys.isEmpty() && pressed.equals(keys) && !latched.contains(device)){
            latched.add(device); swallowed.add(key); return Result.TRIGGER;
        }
        return Result.PASS;
    }
    public void clear(){ held.clear(); consumed.clear(); latched.clear(); }
}
