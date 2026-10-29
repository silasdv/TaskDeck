package org.taskdeck.app;
interface ITaskBackend {
    String readTasks() = 0;
    String openTask(int taskId, String expectedPackage) = 1;
    String removeTask(int taskId, String expectedPackage) = 2;
    void destroy() = 16777114;
}
