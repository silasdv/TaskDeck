package org.taskdeck.app;
import android.content.SharedPreferences;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import java.util.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=33)
public class ControllerBindingsTest {
    private SharedPreferences prefs(){ return RuntimeEnvironment.getApplication().getSharedPreferences("mapping-test",0); }
    @Test public void occupiedButtonSwapsInsteadOfMakingAnotherActionUnreachable(){
        var p=prefs(); ControllerBindings.assign(p,"open",99); assertEquals(99,ControllerBindings.code(p,"open")); assertEquals(96,ControllerBindings.code(p,"close")); assertEquals(6,new HashSet<>(ControllerBindings.read(p).values()).size());
    }
    @Test public void swappedPlaystationProfileTranslatesCustomFaceBindingsAndLabels(){
        var p=prefs(); ControllerBindings.assign(p,"others",96); ControllerBindings.selectProfile(p,"playstation_swapped");
        assertEquals(97,ControllerBindings.code(p,"others")); assertEquals("Cross",ControllerBindings.badge(p,"others")); assertEquals("R1",ControllerBindings.badge(p,"open"));
        ControllerBindings.selectProfile(p,"abxy"); assertEquals(96,ControllerBindings.code(p,"others")); assertEquals("A",ControllerBindings.badge(p,"others"));
    }
    @Test public void calibrationRestoresCommonFaceActionsAndPreservesDistinctOtherActions(){
        var p=prefs(); ControllerBindings.assign(p,"others",96); ControllerBindings.selectProfile(p,"playstation"); ControllerBindings.calibrate(p,97);
        assertEquals(97,ControllerBindings.code(p,"open")); assertEquals(96,ControllerBindings.code(p,"exit")); assertEquals(100,ControllerBindings.code(p,"close")); assertEquals(99,ControllerBindings.code(p,"clear")); assertEquals("Cross",ControllerBindings.badge(p,"open")); assertEquals(6,new HashSet<>(ControllerBindings.read(p).values()).size()); assertTrue(p.getBoolean("confirmCalibrated",false));
    }
    @Test public void resettingBindingsKeepsChosenLabelProfile(){
        var p=prefs(); ControllerBindings.selectProfile(p,"playstation_swapped"); ControllerBindings.assign(p,"open",105); ControllerBindings.reset(p);
        assertEquals("playstation_swapped",ControllerBindings.profile(p)); assertEquals(97,ControllerBindings.code(p,"open")); assertEquals("Cross",ControllerBindings.badge(p,"open"));
    }
    @Test public void startIsReservedAndLegacyMappingMovesToAnAvailableButton(){var p=prefs();ControllerBindings.assign(p,"open",97);p.edit().putInt("map_exit",108).putInt("map_page_next",108).commit();ControllerBindings.reserveStart(p);assertFalse(ControllerBindings.BUTTONS.contains(108));assertFalse(ControllerBindings.read(p).containsValue(108));assertEquals(6,new HashSet<>(ControllerBindings.read(p).values()).size());assertNotEquals(108,ControllerBindings.code(p,"page_next"));ControllerBindings.assign(p,"close",108);assertNotEquals(108,ControllerBindings.code(p,"close"));}
}
