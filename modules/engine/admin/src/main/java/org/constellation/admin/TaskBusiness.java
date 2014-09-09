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

package org.constellation.admin;

import org.constellation.business.ITaskBusiness;
import org.constellation.engine.register.Task;
import org.constellation.engine.register.repository.TaskRepository;
import org.springframework.stereotype.Component;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Component
public class TaskBusiness implements ITaskBusiness{
    
    private TaskRepository taskRepository;

    public void writeTask(String uuidTask, String pyramid, Integer userId, final long start) {
        final Task t = new Task(uuidTask, "PENDING", "TODO-TYPE", start, null, userId);
        taskRepository.create(t);
    }
    
    public Task getTask(String uuid) {
        return taskRepository.get(uuid);
    }
    
    public void update(Task task) {
        taskRepository.update(task);
    }
}
