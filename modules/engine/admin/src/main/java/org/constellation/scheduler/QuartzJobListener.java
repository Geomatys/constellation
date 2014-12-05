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


import org.apache.sis.util.logging.Logging;
import org.constellation.admin.SpringHelper;
import org.constellation.admin.dto.TaskStatusDTO;
import org.constellation.api.TaskState;
import org.constellation.business.IProcessBusiness;
import org.constellation.engine.register.Task;
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
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Quartz Job listener attaching a listener on geotoolkit processes to track their state.
 * 
 * @author Johann Sorel (Geomatys)
 * @author Christophe Mourette (Geomatys)
 */
public class QuartzJobListener implements JobListener {

    private static final Logger LOGGER = Logging.getLogger(QuartzJobListener.class);
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
        final QuartzTask quartzTask = (QuartzTask) detail.getJobDataMap().get(QuartzJobListener.PROPERTY_TASK);
        final String quartzTaskId = quartzTask.getId();

        final org.constellation.engine.register.Task taskEntity = new org.constellation.engine.register.Task();
        taskEntity.setIdentifier(UUID.randomUUID().toString());
        taskEntity.setState(TaskState.PENDING.name());
        taskEntity.setTaskParameterId(quartzTask.getTaskParameterId());
        taskEntity.setOwner(quartzTask.getUserId());
        taskEntity.setType(""); // TODO
        processBusiness.addTask(taskEntity);

        final ProcessListener listener = new StateListener(taskEntity.getIdentifier(), processBusiness, quartzTask.getTitle() );
        pj.addListener(listener);
        LOGGER.log(Level.INFO, "Run task "+taskEntity.getIdentifier());
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
        private final String title;
        private final IProcessBusiness processBusiness;
        private final org.constellation.engine.register.Task taskEntity;

        public StateListener(String taskId, IProcessBusiness processBusiness, String title) {
            this.taskId = taskId;
            this.processBusiness = processBusiness;
            this.taskEntity = processBusiness.getTask(taskId);
            this.title = title;
        }
        
        @Override
        public void started(ProcessEvent event) {
            taskEntity.setState(TaskState.RUNNING.name());
            taskEntity.setDateStart(System.currentTimeMillis());
            taskEntity.setMessage(toString(event.getTask()));
            taskEntity.setProgress((double) event.getProgress());
            updateTask(taskEntity);
        }

        @Override
        public void progressing(ProcessEvent event) {
            taskEntity.setState(TaskState.RUNNING.name());
            taskEntity.setMessage(toString(event.getTask()));
            taskEntity.setProgress((double) event.getProgress());
            updateTask(taskEntity);
        }

        @Override
        public void paused(ProcessEvent event) {
            taskEntity.setState(TaskState.PAUSED.name());
            taskEntity.setMessage(toString(event.getTask()));
            taskEntity.setProgress((double) event.getProgress());
            updateTask(taskEntity);
        }

        @Override
        public void resumed(ProcessEvent event) {
            taskEntity.setState(TaskState.RUNNING.name());
            taskEntity.setMessage(toString(event.getTask()));
            taskEntity.setProgress((double) event.getProgress());
            updateTask(taskEntity);
        }

        @Override
        public void completed(ProcessEvent event) {
            taskEntity.setState(TaskState.SUCCEED.name());
            taskEntity.setDateEnd(System.currentTimeMillis());
            taskEntity.setMessage(toString(event.getTask()));
            taskEntity.setProgress((double) event.getProgress());
            updateTask(taskEntity);
        }

        @Override
        public void failed(ProcessEvent event) {
            taskEntity.setState(TaskState.FAILED.name());
            taskEntity.setDateEnd(System.currentTimeMillis());
            StringWriter errors = new StringWriter();
            if (event.getException() != null) {
                event.getException().printStackTrace(new PrintWriter(errors));
            }
            taskEntity.setMessage(toString(event.getTask()) + " cause : " + errors.toString());
            //taskEntity.setProgress((double) event.getProgress());
            updateTask(taskEntity);
        }

        private void updateTask(Task taskEntity) {

            //update in database
            processBusiness.updateTask(taskEntity);

            //send event
            final TaskStatusDTO taskStatus = new TaskStatusDTO();
            taskStatus.setId(taskEntity.getIdentifier());
            taskStatus.setTaskId(taskEntity.getTaskParameterId());
            taskStatus.setTitle(title);
            taskStatus.setStatus(taskEntity.getState());
            taskStatus.setMessage(taskEntity.getMessage());
            taskStatus.setPercent(taskEntity.getProgress().floatValue());
            taskStatus.setStart(taskEntity.getDateStart());
            taskStatus.setEnd(taskEntity.getDateEnd());
            SpringHelper.sendEvent(taskStatus);
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
