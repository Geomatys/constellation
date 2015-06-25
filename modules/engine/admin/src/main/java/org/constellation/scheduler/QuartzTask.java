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

import org.quartz.Trigger;

import java.util.Date;
import java.util.UUID;
import org.geotoolkit.processing.quartz.ProcessJobDetail;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @author Quentin Boileau (Geomatys)
 */
public class QuartzTask {
        
    private String id;
    private String title;
    private ProcessJobDetail detail = null;
    private Trigger trigger = null;
    private Date lastExecutionDate = null;
    private Exception lastFailedException = null;
    private Integer userId = null;
    private Integer taskParameterId = null;

    public QuartzTask(){
        this((String)null);
    }
    
    public QuartzTask(final String id){
        this.id = id;
        if(this.id == null){
            //generate one
            this.id = UUID.randomUUID().toString();
        }
    }
    
    public QuartzTask(final QuartzTask t){
        this.id = t.id;
        this.title = t.title;
        this.detail = t.detail;
        this.trigger = t.trigger;
        this.lastExecutionDate = (t.lastExecutionDate!=null) ? (Date)t.lastExecutionDate.clone() : null;
        this.lastFailedException = t.lastFailedException;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    public ProcessJobDetail getDetail() {
        return detail;
    }

    public void setDetail(final ProcessJobDetail detail) {
        this.detail = detail;
    }
    
    public Date getLastExecutionDate() {
        return lastExecutionDate;
    }

    public void setLastExecutionDate(final Date lastExecutionDate) {
        this.lastExecutionDate = lastExecutionDate;
    }
    
    public Exception getLastFailedException() {
        return lastFailedException;
    }

    public void setLastFailedException(final Exception lastFailedException) {
        this.lastFailedException = lastFailedException;
    }
    
    public Trigger getTrigger() {
        return trigger;
    }

    public void setTrigger(final Trigger trigger) {
        this.trigger = trigger;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getTaskParameterId() {
        return taskParameterId;
    }

    public void setTaskParameterId(Integer taskParameterId) {
        this.taskParameterId = taskParameterId;
    }

    @Override
    public String toString() {
        return super.toString();
    }
    
}
