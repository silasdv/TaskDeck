package org.taskdeck.app;

import java.util.*;

/** Controller selection is independent of Android touch/focus mode. */
public final class DeckSelection {
    private List<Integer> ids=Collections.emptyList();
    private int index;
    public void update(List<Integer> next){
        Integer previous=id(); ids=new ArrayList<>(next);
        int retained=previous==null?-1:ids.indexOf(previous);
        index=retained>=0?retained:Math.max(0,Math.min(index,ids.size()-1));
    }
    public void move(int delta){ index=Math.max(0,Math.min(index+delta,ids.size()-1)); }
    public void select(int id){ int next=ids.indexOf(id); if(next>=0) index=next; }
    public Integer id(){ return ids.isEmpty()?null:ids.get(index); }
    public int index(){ return index; }
}
