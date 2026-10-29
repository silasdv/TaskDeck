package org.taskdeck.app;

import org.junit.Test;
import java.util.Set;
import static org.junit.Assert.*;

public class TaskPolicyTest {
    private boolean allowed(String name,boolean system,int user){ return TaskPolicy.canRemove(name,system,user,0,"launcher.app","keyboard.app",Set.of("music.app")); }
    @Test public void ordinaryCurrentUserTaskCanBeRemoved(){ assertTrue(allowed("test.game",false,0)); }
    @Test public void otherUsersAndSystemAppsAreExcluded(){ assertFalse(allowed("test.game",false,10)); assertFalse(allowed("system.game",true,0)); }
    @Test public void infrastructureAndConfiguredExceptionsAreProtected(){
        for(String name: new String[]{"android","com.android.systemui","org.taskdeck.app","org.padboard.keyboard","moe.shizuku.privileged.api","launcher.app","keyboard.app","music.app",""}) assertFalse(name,allowed(name,false,0));
        assertFalse(allowed(null,false,0));
    }
}
