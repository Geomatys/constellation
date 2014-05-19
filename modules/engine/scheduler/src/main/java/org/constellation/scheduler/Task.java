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

import java.util.Date;
import java.util.UUID;

import org.geotoolkit.process.quartz.ProcessJobDetail;

import org.quartz.SimpleTrigger;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class Task {
        
    private String id;
    private String title;
    private ProcessJobDetail detail = null;
    private SimpleTrigger trigger = null;
    private Date lastExecutionDate = null;
    private Exception lastFailedException = null;
    
    public Task(){
        this((String)null);
    }
    
    public Task(final String id){
        this.id = id;
        if(this.id == null){
            //generate one
            this.id = UUID.randomUUID().toString();
        }
    }
    
    public Task(final Task t){
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
    
    public SimpleTrigger getTrigger() {
        return trigger;
    }

    public void setTrigger(final SimpleTrigger trigger) {
        this.trigger = trigger;
    }

    @Override
    public String toString() {
        return super.toString();
    }
    
}
