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

import org.constellation.api.*;
import org.constellation.api.TaskState;
import org.constellation.business.IProcessBusiness;
import org.geotoolkit.process.ProcessEvent;
import org.geotoolkit.process.ProcessListener;
import org.geotoolkit.process.quartz.ProcessJob;
import org.geotoolkit.process.quartz.ProcessJobDetail;
import org.opengis.util.InternationalString;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Quartz Job listener attaching a listener on geotoolkit processes to track their state.
 * 
 * @author Johann Sorel (Geomatys)
 * @author Christophe Mourette (Geomatys)
 */
public class QuartzJobListener implements JobListener {

    public static final String PROPERTY_TASK = "task";
    private final IProcessBusiness processBusiness;

    public QuartzJobListener(IProcessBusiness processBusiness) {
        this.processBusiness = processBusiness;
     }


    ////////////////////////////////////////////////////////////////////////////
    // Quartz listener /////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    
    @Override
    public String getName() {
        return "ConstellationJobTracker";
    }

    @Override
    public synchronized void jobToBeExecuted(JobExecutionContext jec) {
        final Job job = jec.getJobInstance();
        if(!(job instanceof ProcessJob)) return;
        
        //attach a listener on the process
        final ProcessJob pj = (ProcessJob) job;
        final ProcessJobDetail detail = (ProcessJobDetail) jec.getJobDetail();
        final Task task = (Task) detail.getJobDataMap().get(QuartzJobListener.PROPERTY_TASK);
        final String taskId = task.getId();

        
        final ProcessListener listener = new StateListener(taskId,processBusiness);
        pj.addListener(listener);
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext jec) {
    }

    @Override
    public void jobWasExecuted(JobExecutionContext jec, JobExecutionException jee) {
    }

    
    ////////////////////////////////////////////////////////////////////////////
    // Geotk process listener //////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
      
    /**
     * Catch process events and set them in the TaskState.
     */
    private static class StateListener implements ProcessListener{
    
        private final String taskId;
        private final IProcessBusiness processBusiness;
        private final org.constellation.engine.register.Task taskEntity;

        public StateListener(String taskId, IProcessBusiness processBusiness) {
            this.taskId = taskId;
            this.processBusiness = processBusiness;
            taskEntity = processBusiness.getTask(taskId);
        }
        
        @Override
        public void started(ProcessEvent event) {
            taskEntity.setState(org.constellation.api.TaskState.RUNNING.name());
            taskEntity.setStart(System.currentTimeMillis());
            taskEntity.setMessage(toString(event.getTask()));
            processBusiness.update(taskEntity);
        }

        @Override
        public void progressing(ProcessEvent event) {
            //
        }

        @Override
        public void paused(ProcessEvent event) {
            //Not yet implemented
        }

        @Override
        public void resumed(ProcessEvent event) {
            //Not yet implemented
        }

        @Override
        public void completed(ProcessEvent event) {
            taskEntity.setState(TaskState.SUCCEED.name());
            taskEntity.setMessage(toString(event.getTask()));
            taskEntity.setEnd(System.currentTimeMillis());
            processBusiness.update(taskEntity);
        }

        @Override
        public void failed(ProcessEvent event) {
            taskEntity.setState(TaskState.FAILED.name());
            taskEntity.setEnd(System.currentTimeMillis());
            StringWriter errors = new StringWriter();
            event.getException().printStackTrace(new PrintWriter(errors));
            taskEntity.setMessage(toString(event.getTask())+ " cause : "+errors.toString());
            processBusiness.update(taskEntity);
        }
        
        private String toString(InternationalString str){
            if(str==null){
                return "";
            }else{
                return str.toString();
            }
        }

    }

}
