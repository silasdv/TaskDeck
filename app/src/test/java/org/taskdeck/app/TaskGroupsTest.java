package org.taskdeck.app;
import org.json.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import java.util.*;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=33)
public class TaskGroupsTest {
    private JSONObject window(int id,String pkg,long ram) throws Exception { return new JSONObject().put("id",id).put("package",pkg).put("label",pkg).put("pssKb",ram).put("mandatoryProtected",false); }
    @Test public void groupsWindowsKeepsRecentRepresentativeAndDoesNotDoubleCountRam() throws Exception {
        var a=window(1,"browser",64000); var b=window(2,"browser",64000); var c=window(3,"game",80000);
        var groups=TaskGroups.group(List.of(a,b,c),true); assertEquals(2,groups.size()); assertEquals(1,groups.get(0).getInt("id")); assertEquals(2,groups.get(0).getInt("windowCount"));
        assertEquals(64000,groups.get(0).getLong("pssKb")); assertEquals(List.of(a,b),TaskGroups.expand(groups.get(0))); assertFalse(a.has("members"));
    }
    @Test public void separateModePreservesEachWindowAndHardProtectionCannotBeLost() throws Exception {
        var a=window(1,"browser",0); var b=window(2,"browser",0).put("mandatoryProtected",true);
        assertEquals(2,TaskGroups.group(List.of(a,b),false).size()); assertTrue(TaskOrder.mandatory(TaskGroups.group(List.of(a,b),true).get(0)));
    }
}
