package org.taskdeck.app;
import org.junit.Test;
import java.util.Set;
import static org.junit.Assert.*;

public class ChordGateTest {
    @Test public void freshPressRecoversAfterMissingRelease(){
        ChordGate gate=new ChordGate();
        gate.configure(Set.of("1:10"));
        assertEquals(ChordGate.Result.TRIGGER,gate.update(1,"1:10",true,0,true));
        assertEquals(ChordGate.Result.TRIGGER,gate.update(1,"1:10",true,0,true));
        assertEquals(ChordGate.Result.CONSUME,gate.update(1,"1:10",false,0,true));
    }
    @Test public void windowResetDropsStaleKeysAndKeepsCapturedReleaseBalanced(){
        ChordGate gate=new ChordGate();
        gate.configure(Set.of("1:10","2:20"));
        gate.update(1,"1:10",true,0,true);
        assertEquals(ChordGate.Result.TRIGGER,gate.update(1,"2:20",true,0,true));
        gate.resetHeldKeys();
        assertEquals(ChordGate.Result.CONSUME,gate.update(1,"2:20",false,0,true));
        assertEquals(ChordGate.Result.PASS,gate.update(1,"2:20",true,0,true));
        assertEquals(ChordGate.Result.TRIGGER,gate.update(1,"1:10",true,0,true));
    }
    @Test public void partialChordsPassAndCompletingKeyHasBalancedDownUp(){
        ChordGate g=new ChordGate(); g.configure(Set.of("1:10","2:20"));
        assertEquals(ChordGate.Result.PASS,g.update(1,"1:10",true,0,true));
        assertEquals(ChordGate.Result.TRIGGER,g.update(1,"2:20",true,0,true));
        assertEquals(ChordGate.Result.CONSUME,g.update(1,"2:20",true,1,true));
        assertEquals(ChordGate.Result.PASS,g.update(1,"1:10",false,0,true));
        assertEquals(ChordGate.Result.CONSUME,g.update(1,"2:20",false,0,false));
    }
    @Test public void differentDevicesOrExtraHeldKeysDoNotCompleteTheChord(){
        ChordGate g=new ChordGate(); g.configure(Set.of("1:10","2:20"));
        g.update(1,"1:10",true,0,true);
        assertEquals(ChordGate.Result.PASS,g.update(2,"2:20",true,0,true));
        g.update(1,"3:30",true,0,true);
        assertEquals(ChordGate.Result.PASS,g.update(1,"2:20",true,0,true));
    }
    @Test public void anotherMacroCanUseTheFirstButtonAndChordRearmsAfterRelease(){
        ChordGate g=new ChordGate(); g.configure(Set.of("1:10","2:20"));
        for(int i=0;i<2;i++){
            assertEquals(ChordGate.Result.PASS,g.update(1,"1:10",true,0,true));
            assertEquals(ChordGate.Result.PASS,g.update(1,"1:10",false,0,true));
        }
        g.update(1,"1:10",true,0,true); assertEquals(ChordGate.Result.TRIGGER,g.update(1,"2:20",true,0,true));
        g.update(1,"2:20",false,0,true); g.update(1,"1:10",false,0,true);
        g.update(1,"2:20",true,0,true); assertEquals(ChordGate.Result.TRIGGER,g.update(1,"1:10",true,0,true));
    }
}
