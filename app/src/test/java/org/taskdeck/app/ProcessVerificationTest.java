package org.taskdeck.app;
import org.junit.Test;
import java.util.Set;
import static org.junit.Assert.*;
public class ProcessVerificationTest {
    @Test public void requiresOldPidsGoneAndNewProcessPresentForRestart(){
        assertFalse(ProcessVerification.restarted(Set.of(10),Set.of(10,20)));
        assertFalse(ProcessVerification.restarted(Set.of(10),Set.of()));
        assertTrue(ProcessVerification.stopped(Set.of(10),Set.of()));
        assertTrue(ProcessVerification.restarted(Set.of(10,11),Set.of(20,21)));
        assertFalse(ProcessVerification.stopped(Set.of(10,11),Set.of(11)));
        assertFalse(ProcessVerification.launcherClosed(Set.of(10),Set.of(),true));
        assertTrue(ProcessVerification.launcherClosed(Set.of(10),Set.of(),false));
        assertTrue(ProcessVerification.launcherClosed(Set.of(10),Set.of(20),true));
        assertFalse(ProcessVerification.launcherClosed(Set.of(10),Set.of(10),false));
    }
}
