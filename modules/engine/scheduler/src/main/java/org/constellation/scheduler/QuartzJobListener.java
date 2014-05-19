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

import org.geotoolkit.process.ProcessEvent;
import org.geotoolkit.process.ProcessListener;
import org.geotoolkit.process.quartz.ProcessJob;
import org.geotoolkit.process.quartz.ProcessJobDetail;
import org.opengis.util.InternationalString;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

/**
 * Quartz Job listener attaching a listener on geotoolkit processes to track their state.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class QuartzJobListener implements JobListener {

    public static final String PROPERTY_TASK = "task";
    
    
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
        final TaskState state = CstlScheduler.getInstance().getaskState(taskId);
        
        final ProcessListener listener = new StateListener(state);
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
    
        private final TaskState state;

        public StateListener(TaskState state) {
            this.state = state;
        }
        
        @Override
        public void started(ProcessEvent event) {
            state.setStatus(TaskState.Status.RUN);
            state.setMessage(toString(event.getTask()));
            state.setPercent(event.getProgress());
            CstlScheduler.getInstance().fireTaskUpdate(state);
        }

        @Override
        public void progressing(ProcessEvent event) {
            state.setStatus(TaskState.Status.RUN);
            state.setMessage(toString(event.getTask()));
            state.setPercent(event.getProgress());
            CstlScheduler.getInstance().fireTaskUpdate(state);
        }

        @Override
        public void paused(ProcessEvent event) {
            state.setStatus(TaskState.Status.PAUSE);
            state.setMessage(toString(event.getTask()));
            state.setPercent(event.getProgress());
            CstlScheduler.getInstance().fireTaskUpdate(state);
        }

        @Override
        public void resumed(ProcessEvent event) {
            state.setStatus(TaskState.Status.RUN);
            state.setMessage(toString(event.getTask()));
            state.setPercent(event.getProgress());
            CstlScheduler.getInstance().fireTaskUpdate(state);
        }

        @Override
        public void completed(ProcessEvent event) {
            state.setStatus(TaskState.Status.FINISH);
            state.setMessage(toString(event.getTask()));
            state.setPercent(event.getProgress());
            CstlScheduler.getInstance().fireTaskUpdate(state);
        }

        @Override
        public void failed(ProcessEvent event) {
            state.setStatus(TaskState.Status.FAIL);
            state.setMessage(toString(event.getTask()));
            state.setPercent(event.getProgress());
            state.setLastException(event.getException());
            CstlScheduler.getInstance().fireTaskUpdate(state);
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
