/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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
        }

        @Override
        public void progressing(ProcessEvent event) {
            state.setStatus(TaskState.Status.RUN);
            state.setMessage(toString(event.getTask()));
            state.setPercent(event.getProgress());
        }

        @Override
        public void paused(ProcessEvent event) {
            state.setStatus(TaskState.Status.PAUSE);
            state.setMessage(toString(event.getTask()));
            state.setPercent(event.getProgress());
        }

        @Override
        public void resumed(ProcessEvent event) {
            state.setStatus(TaskState.Status.RUN);
            state.setMessage(toString(event.getTask()));
            state.setPercent(event.getProgress());
        }

        @Override
        public void completed(ProcessEvent event) {
            state.setStatus(TaskState.Status.FINISH);
            state.setMessage(toString(event.getTask()));
            state.setPercent(event.getProgress());
        }

        @Override
        public void failed(ProcessEvent event) {
            state.setStatus(TaskState.Status.FAIL);
            state.setMessage(toString(event.getTask()));
            state.setPercent(event.getProgress());
            state.setLastException(event.getException());
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
