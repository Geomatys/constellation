/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.services.web.controller.admin;

import org.constellation.dto.TaskStatus;
import org.constellation.scheduler.CstlScheduler;
import org.constellation.scheduler.CstlSchedulerListener;
import org.constellation.scheduler.TaskState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

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
