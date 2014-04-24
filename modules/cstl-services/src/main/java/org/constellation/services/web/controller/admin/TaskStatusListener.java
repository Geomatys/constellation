package org.constellation.services.web.controller.admin;

import javax.annotation.PostConstruct;

import org.constellation.dto.TaskStatus;
import org.constellation.scheduler.CstlScheduler;
import org.constellation.scheduler.CstlSchedulerListener;
import org.constellation.scheduler.TaskState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TaskStatusListener {

    @Autowired
    private SimpMessagingTemplate template;

    @PostConstruct
    public void init() {
    
        CstlScheduler.getInstance().addListener(new CstlSchedulerListener() {
            
            @Override
            public void taskUpdated(TaskState taskState) {
                TaskStatus taskStatus = new TaskStatus();
                taskStatus.setId(taskState.getTask().getId());
                taskStatus.setMessage(taskState.getMessage());
                taskStatus.setPercent(taskState.getPercent());
                taskStatus.setStatus(taskState.getStatus().name());
                template.convertAndSend("/topic/taskevents", taskStatus);                
            }
        });
        
    }
    

//    @Scheduled(fixedDelay=3000)
//    public void greet() {
//        TaskStatus taskStatus = new TaskStatus();
//        taskStatus.setId("task_id");
//        taskStatus.setMessage("task_message");
//        taskStatus.setPercent(System.currentTimeMillis()%100);
//        taskStatus.setStatus("task_status");
//        template.convertAndSend("/topic/taskevents", taskStatus);                
//    }
    
}
