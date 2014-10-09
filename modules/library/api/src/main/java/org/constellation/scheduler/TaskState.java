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

package org.constellation.scheduler;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class TaskState {

    public static enum Status{
        PENDING,
        RUNNING,
        SUCCEED,
        FAILED,
        CANCELLED,
        PAUSED
    };

    private final QuartzTask quartzTask;

    private Status status = Status.CANCELLED;
    private String message;
    private float percent;
    private Exception lastException;
    private String title;

    public TaskState(){
        this.quartzTask = null;
    }
    
    public TaskState(QuartzTask quartzTask) {
        this.quartzTask = quartzTask;
    }

    public QuartzTask getQuartzTask() {
        return quartzTask;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
}
