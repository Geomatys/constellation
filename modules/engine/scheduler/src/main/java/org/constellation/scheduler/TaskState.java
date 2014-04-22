/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011-2014, Geomatys
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

package org.constellation.scheduler;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class TaskState {
    
    public static enum Status{
        NONE,
        RUN,
        PAUSE,
        FINISH,
        FAIL
    };
    
    private final Task task;

    private Status status = Status.NONE;
    private String message;
    private float percent;
    private Exception lastException;
    
    public TaskState(Task task) {
        this.task = task;
    }

    public Task getTask() {
        return task;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public float getPercent() {
        return percent;
    }

    public void setPercent(float percent) {
        this.percent = percent;
    }
    
    public Exception getLastException() {
        return lastException;
    }

    public void setLastException(Exception lastException) {
        this.lastException = lastException;
    }
    
}
