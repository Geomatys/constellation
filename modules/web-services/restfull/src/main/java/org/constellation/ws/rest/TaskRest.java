/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013 - 2014, Geomatys
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
