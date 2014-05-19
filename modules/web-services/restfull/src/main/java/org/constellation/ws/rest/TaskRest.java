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
package org.constellation.ws.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.constellation.dto.TaskStatus;
import org.constellation.scheduler.CstlScheduler;
import org.constellation.scheduler.Task;
import org.constellation.scheduler.TaskState;

/**
 * RestFull API for task management/operations.
 * 
 * @author Johann Sorel (Geomatys)
 */
@Path("/1/task")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public final class TaskRest {
    
    /**
     * List running tasks.
     */
    @GET
    @Path("listTasks")
    public Response listTasks() {
        
        final CstlScheduler scheduler = CstlScheduler.getInstance();
        final List<Task> tasks = scheduler.listTasks();
        
        final Map<String, TaskStatus> lst = new HashMap<String, TaskStatus>();
        
        for(Task t : tasks){
            final TaskState state = scheduler.getaskState(t.getId());
            final TaskStatus status = new TaskStatus();
            status.setId(t.getId());
            status.setMessage(state.getMessage());
            status.setPercent(state.getPercent());
            status.setStatus(state.getStatus().name());
            status.setTitle(t.getTitle());
            lst.put(status.getId(), status);
        }
                
        return Response.ok(lst).build();
    }
    
    
}
