package org.taskdeck.app;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class DeckSelectionTest {
    @Test public void navigationRetainsIdentityAcrossRefreshAndSelectsNeighborAfterRemoval(){
        DeckSelection s=new DeckSelection(); s.update(List.of(10,20,30)); s.move(1); assertEquals(Integer.valueOf(20),s.id());
        s.update(List.of(30,20,10)); assertEquals(Integer.valueOf(20),s.id());
        s.update(List.of(30,10)); assertEquals(Integer.valueOf(10),s.id());
        s.move(100); assertEquals(Integer.valueOf(10),s.id()); s.move(-100); assertEquals(Integer.valueOf(30),s.id());
        s.update(List.of()); assertNull(s.id());
    }
}
