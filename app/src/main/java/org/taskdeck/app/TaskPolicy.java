package org.taskdeck.app;

import java.util.Set;

/** Rechecked in the privileged process, never trusted solely to UI state. */
public final class TaskPolicy {
    private TaskPolicy() {}
    public static boolean canRemove(String packageName, boolean systemApp, int taskUser, int activeUser,
                                    String home, String inputMethod, Set<String> protectedApps) {
        return packageName!=null && !packageName.isEmpty() && !systemApp && taskUser==activeUser
            && !packageName.equals("android") && !packageName.equals("com.android.systemui")
            && !packageName.equals("org.taskdeck.app") && !packageName.equals("org.padboard.keyboard")
            && !packageName.equals("moe.shizuku.privileged.api") && !packageName.equals(home)
            && !packageName.equals(inputMethod) && !protectedApps.contains(packageName);
    }
}
