package org.taskdeck.app;
interface ITaskBackend {
    String readTasks() = 0;
    String openTask(int taskId, String expectedPackage) = 1;
    String removeTask(int taskId, String expectedPackage) = 2;
    void setProtectedPackages(in String[] packages) = 3;
    void setUnlockedLaunchers(in String[] packages) = 4;
    String stopLauncher(String expectedPackage, boolean forRestart) = 5;
    String readMemory() = 6;
    String switchPreviousTask() = 7;
    String verifyLauncherRestart(String expectedPackage, in int[] previousPids) = 8;
    void destroy() = 16777114;
}
