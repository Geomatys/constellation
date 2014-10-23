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

import org.apache.sis.util.logging.Logging;
import org.constellation.admin.ProcessBusiness;
import org.constellation.admin.dto.TaskStatusDTO;
import org.constellation.admin.exception.ConstellationException;
import org.constellation.api.TaskState;
import org.constellation.business.IProcessBusiness;
import org.constellation.scheduler.QuartzJobListener;
import org.constellation.scheduler.QuartzTask;
import org.geotoolkit.process.ProcessEvent;
import org.geotoolkit.process.ProcessListener;
import org.geotoolkit.process.quartz.ProcessJob;
import org.geotoolkit.process.quartz.ProcessJobDetail;
import org.opengis.util.InternationalString;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.springframework.context.annotation.DependsOn;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Quartz job listener that register a geotk process listener each time the job is executed.
 * And send messages on websocket "/topic/taskevents*" topic.
 *
 * TODO for a clustering support this mechanism should be changed to directly listen to database updates.
 *
 * @author Quentin Boileau (Geomatys)
 */
@Named
@Singleton
@DependsOn(ProcessBusiness.BEAN_NAME)
public class MessagingJobListener implements JobListener {

    private static final Logger LOGGER = Logging.getLogger(MessagingJobListener.class);

    @Inject
    private SimpMessagingTemplate template;

    @Inject
    private IProcessBusiness processBusiness;

    @PostConstruct
    private void init() {
        try {
            processBusiness.registerQuartzListener(this);
        } catch (ConstellationException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }

    @Override
    public String getName() {
        return MessagingJobListener.class.getName();
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        final Job job = context.getJobInstance();
        if(!(job instanceof ProcessJob)) return;

        final ProcessJob pj = (ProcessJob) job;
        final ProcessJobDetail detail = (ProcessJobDetail) context.getJobDetail();
        final QuartzTask quartzTask = (QuartzTask) detail.getJobDataMap().get(QuartzJobListener.PROPERTY_TASK);

        pj.addListener(new MessageProcessListener(quartzTask.getTaskParameterId()));
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        //nothing to do
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        //nothing to do
    }

    /**
     * Send two message to websocket on topics
     * /topic/taskevents and /topic/taskevents/{taskId}
     *
     * @param taskStatus
     */
    private void sendMessageEvent(TaskStatusDTO taskStatus) {
        template.convertAndSend("/topic/taskevents", taskStatus);
        template.convertAndSend("/topic/taskevents/"+taskStatus.getId(), taskStatus);
    }

    /**
     * Geotk process listener that send event on each process life cycle
     */
    private class MessageProcessListener implements ProcessListener {

        private float lastProgress = 0f;
        final int taskId;

        private MessageProcessListener(int taskId) {
            this.taskId = taskId;
        }

        @Override
        public void started(ProcessEvent event) {
            sendStatus(event, TaskState.RUNNING);
        }

        @Override
        public void progressing(ProcessEvent event) {
            sendStatus(event, TaskState.RUNNING);
        }

        @Override
        public void paused(ProcessEvent event) {
            sendStatus(event, TaskState.PAUSED);
        }

        @Override
        public void resumed(ProcessEvent event) {
            sendStatus(event, TaskState.RUNNING);
        }

        @Override
        public void completed(ProcessEvent event) {
            sendStatus(event, TaskState.SUCCEED);
        }

        @Override
        public void failed(ProcessEvent event) {
            sendStatus(event, TaskState.FAILED);
        }

        private void sendStatus(ProcessEvent event, TaskState state) {

            final TaskStatusDTO taskStatus = new TaskStatusDTO();
            taskStatus.setTaskId(taskId);
            taskStatus.setStatus(state.name());

            //message
            if (TaskState.FAILED.equals(state)) {
                StringWriter errors = new StringWriter();
                event.getException().printStackTrace(new PrintWriter(errors));
                taskStatus.setMessage(toString(event.getTask()) + " cause : " + errors.toString());
            } else {
                taskStatus.setMessage(toString(event.getTask()));
            }

            //progress
            if (!Float.isNaN(event.getProgress())) {
                this.lastProgress =  event.getProgress();
            }
            taskStatus.setPercent(this.lastProgress);

            //send
            sendMessageEvent(taskStatus);
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
