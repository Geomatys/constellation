/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
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
