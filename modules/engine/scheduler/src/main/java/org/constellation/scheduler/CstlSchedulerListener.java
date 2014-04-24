package org.constellation.scheduler;

import java.util.EventListener;

public interface CstlSchedulerListener extends EventListener {

    void taskUpdated(TaskState taskState);
    
}
