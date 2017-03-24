package com.vmware.vmcreation.samples;
import com.svm.vim25.mo.Task;

public class TaskExecutor {

    public static void exec(Task task, String desc) throws Exception {
        String result = task.waitForTask(200, 100); 
        if (Task.SUCCESS.equals(result)) {
            System.out.println("Task: " + desc + " sucessfully!");
        } else {
            throw new Exception("Task: " + desc + " failed, error: " + task.getTaskInfo().getError().getLocalizedMessage());
        }
    }

}
