package org.taskdeck.app;
import java.util.*;
public final class ProcessVerification {
    private ProcessVerification(){}
    public static boolean stopped(Set<Integer> before,Set<Integer> after){ return Collections.disjoint(before,after); }
    public static boolean restarted(Set<Integer> before,Set<Integer> after){ return !after.isEmpty()&&stopped(before,after); }
    public static boolean launcherClosed(Set<Integer> before,Set<Integer> after,boolean originalTasksRemain){ return stopped(before,after)&&(!originalTasksRemain||!after.isEmpty()); }
}
