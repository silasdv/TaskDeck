package org.taskdeck.app;
import org.json.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import java.util.*;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=33)
public class TaskOrderTest {
    private JSONObject task(String name,int rank,long ram,boolean fixed) throws Exception { return new JSONObject().put("label",name).put("recentRank",rank).put("pssKb",ram).put("mandatoryProtected",fixed); }
    @Test public void mandatoryAppsAreHiddenAndManualLocksAlwaysSortLast() throws Exception {
        var a=task("Alpha",1,100,false); var b=task("Beta",0,900,false); var c=task("Core",2,999,true);
        for(String mode:List.of("recent","ram","alphabetical")) assertEquals(List.of(a,b),TaskOrder.arrange(List.of(b,c,a),mode,t->t==b));
    }
    @Test public void recentRamAndAlphabeticalOrdersAreDistinctAndUnknownRamLast() throws Exception {
        var a=task("Alpha",2,-1,false); var b=task("Beta",0,100,false); var c=task("Charlie",1,900,false); var all=List.of(a,b,c);
        assertEquals(List.of(b,c,a),TaskOrder.arrange(all,"recent",t->false));
        assertEquals(List.of(c,b,a),TaskOrder.arrange(all,"ram",t->false));
        assertEquals(List.of(a,b,c),TaskOrder.arrange(all,"alphabetical",t->false));
    }
    @Test public void protectedLauncherIsVisibleButOtherInfrastructureIsHidden() throws Exception {
        var launcher=task("Wave",0,10,true).put("launcher",true); var core=task("Keyboard",1,20,true); var game=task("Game",2,30,false);
        assertEquals(List.of(game,launcher),TaskOrder.arrange(List.of(launcher,core,game),"recent",TaskOrder::mandatory));
    }
}
