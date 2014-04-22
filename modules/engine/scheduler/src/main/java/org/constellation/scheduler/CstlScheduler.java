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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import org.apache.sis.util.logging.Logging;
import org.constellation.configuration.ConfigurationException;
import org.constellation.scheduler.configuration.XMLTaskConfigurator;
import org.geotoolkit.process.Process;
import org.geotoolkit.process.ProcessEvent;
import org.geotoolkit.process.ProcessListenerAdapter;
import org.geotoolkit.process.quartz.ProcessJobDetail;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import static org.quartz.impl.matchers.EverythingMatcher.*;

/**
 * Constellation can run tasks at regular intervals. 
 * The scheduler used is Quartz, added tasks are stored in a configuration
 * file and are restarted when the server starts.
 * 
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public final class CstlScheduler {
        
    private static final Logger LOGGER = Logging.getLogger(CstlScheduler.class);
    private static CstlScheduler INSTANCE = null;
        
    //programmed tasks
    private final List<Task> tasks = new ArrayList<>();
    //execute once tasks
    private final List<Task> once = new ArrayList<>();
    
    private final Map<String,TaskState> statuses = new ConcurrentHashMap<>();
    private final QuartzJobListener quartzListener = new QuartzJobListener();
    private Scheduler quartzScheduler;
    
    public static final TaskConfigurator DEFAULT_CONFIGURATOR = new XMLTaskConfigurator();    
    protected static TaskConfigurator CONFIGURATOR = DEFAULT_CONFIGURATOR;
    
    
    private CstlScheduler(){
        
        LOGGER.log(Level.INFO, "=== Starting Constellation Scheduler ===");
        try {
            loadTasks();
        } catch (IOException | XMLStreamException ex) {
            LOGGER.log(Level.SEVERE, "=== Failed to read tasks ===\n"+ex.getLocalizedMessage(), ex); 
        }

        final SchedulerFactory schedFact = new StdSchedulerFactory();
        try {
            quartzScheduler = schedFact.getScheduler();
            quartzScheduler.start();
            
            //listen and attach a process on all geotk process tasks
            quartzScheduler.getListenerManager().addJobListener(quartzListener, allJobs());
            
        } catch (SchedulerException ex) {
            LOGGER.log(Level.SEVERE, "=== Failed to start quartz scheduler ===\n"+ex.getLocalizedMessage(), ex);            
            return;
        }
        LOGGER.log(Level.INFO, "=== Constellation Scheduler sucessfully started ===");    
        
        for(Task t : tasks){
            try {
                registerTask(t);
            } catch (SchedulerException ex) {
                LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }
    
    public static TaskConfigurator getConfigurator() {
        return CONFIGURATOR;
    }

    public static synchronized void setConfigurator(TaskConfigurator configurator) {
        CONFIGURATOR = configurator;
        //TODO need to clean the quartz task pool and reload task from new configurator
    }
    
    
    /**
     * @return copied list of all tasks.
     */
    public synchronized List<Task> listTasks(){
        //return a copy, since tasks object are mutable
        final List<Task> copy = new ArrayList<>();
        for(Task t : tasks){
            copy.add(new Task(t));
        }
        for(Task t : once){
            copy.add(new Task(t));
        }
        return copy;
    }
    
    public synchronized Task getTask(final String id){
        for(Task t : listTasks()){
            if(t.getId().equals(id)){
                return t;
            }
        }
        return null;
    }
    
    /**
     * Get task status.
     * @param id, must be a valid id. Illegal
     * @return TaskState, null if no task exist for this id.
     */
    public synchronized TaskState getaskState(final String id){
        TaskState state = statuses.get(id);
        if(state==null){
            final Task task = getTask(id);
            if(task==null){
                return null;
            }
            state = new TaskState(task);
            statuses.put(id, state);
        }
        return state;
    }
    
    /**
     * Add a new task.
     * @param task
     */
    public synchronized void addTask(Task task) throws ConfigurationException{
        
        task = new Task(task); //defense copy
        tasks.add(task);
        try {
            registerTask(task);
        } catch (SchedulerException ex) {
            LOGGER.log(Level.WARNING, "Failed to register task :"+task.getId()+","+task.getTitle()+" in scheduler.",ex);
        }
                
        getConfigurator().addTaskConfiguration(task);
    }
    
    public void runOnce(String title, Process process){
        final TriggerBuilder tb = TriggerBuilder.newTrigger();
        final Trigger trigger = tb.startNow().build();
        
        final Task task = new Task(UUID.randomUUID().toString());
        final ProcessJobDetail detail = new ProcessJobDetail(process);
        task.setDetail(detail);
        task.setTitle(title);
        task.setTrigger((SimpleTrigger)trigger);
        
        //add listener on this task to remove it from the list once finished
        process.addListener(new ProcessListenerAdapter(){
            @Override
            public void failed(ProcessEvent event) {
                once.remove(task);
            }
            @Override
            public void completed(ProcessEvent event) {
                once.remove(task);
            }
        });
        
        
        once.add(task);
        
        try {
            registerTask(task);
        } catch (SchedulerException ex) {
            LOGGER.log(Level.WARNING, "Failed to register task :"+task.getId()+","+task.getTitle()+" in scheduler.",ex);
        }
        
    }
    
    /**
     * Update a task.
     * @param task
     */
    public synchronized boolean updateTask(final Task task) throws ConfigurationException{
        if(removeTask(task)){
            addTask(task);
            return true;
        }
        return false;
    }
    
    /**
     * Remove a task.
     * @param task
     */
    public synchronized boolean removeTask(Task task) throws ConfigurationException{
        return removeTask(task.getId());
    }
    
    /**
     * Remove task for the given id.
     * @param id
     */
    public synchronized boolean removeTask(final String id) throws ConfigurationException{
        
        //find task for this id
        Task task = null;
        for(int i=0,n=tasks.size(); i<n; i++){
            final Task t = tasks.get(i);            
            if(t.getId().equals(id)){
                task = tasks.remove(i);
                break;
            }
        }
        
        if(task != null){
            statuses.remove(task.getId());
            
            getConfigurator().removeTaskConfiguration(task);
            try {
                unregisterTask(task);
            } catch (SchedulerException ex) {
                LOGGER.log(Level.WARNING, "Failed to unregister task:"+task.getId()+","+task.getTitle()+" from scheduler",ex);
            }
        }
        
        return task != null;
    }
    
    /**
     * Load tasks defined in the configuration file.
     */
    private synchronized void loadTasks() throws IOException, XMLStreamException{
        try {
            final List<Task> candidates = CONFIGURATOR.getTasksConfiguration();
            if(candidates != null){
                tasks.addAll(candidates);
            }
        } catch (ConfigurationException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
    }
        
    /**
     * Add the given task in the scheduler.
     */
    private void registerTask(final Task task) throws SchedulerException{
        //ensure the job detail contain the task in the datamap, this is used in the
        //job listener to track back the task
        task.getDetail().getJobDataMap().put(QuartzJobListener.PROPERTY_TASK, task);
                
        quartzScheduler.scheduleJob(task.getDetail(), task.getTrigger());
        LOGGER.log(Level.INFO, "Scheduler task added : {0}, {1}   type : {2}.{3}", new Object[]{
            task.getId(),
            task.getTitle(),
            task.getDetail().getFactoryIdentifier(), 
            task.getDetail().getProcessIdentifier()});
    }
    
    /**
     * Add the given task in the scheduler.
     */
    private void unregisterTask(final Task task) throws SchedulerException{
        quartzScheduler.interrupt(task.getDetail().getKey());
        final boolean removed = quartzScheduler.deleteJob(task.getDetail().getKey());
        
        if(removed){
            LOGGER.log(Level.INFO, "Scheduler task removed : {0}, {1}   type : {2}.{3}", new Object[]{
                task.getId(),
                task.getTitle(),
                task.getDetail().getFactoryIdentifier(), 
                task.getDetail().getProcessIdentifier()});
        }else{
            LOGGER.log(Level.WARNING, "Scheduler failed to remove task : {0}, {1}   type : {2}.{3}", new Object[]{
                task.getId(),
                task.getTitle(),
                task.getDetail().getFactoryIdentifier(), 
                task.getDetail().getProcessIdentifier()});
        }
        
    }
    
    
    /**
     * @return Singleton instance of Constellation scheduler.
     */
    public synchronized static CstlScheduler getInstance(){
        if (INSTANCE == null) {
            INSTANCE = new CstlScheduler();
        }
        return INSTANCE;
    }

    public void stop() {        
        LOGGER.log(Level.INFO, "=== Stopping Scheduler ===");
        try {
            quartzScheduler.shutdown();
        } catch (SchedulerException ex) {
            LOGGER.log(Level.SEVERE, "=== Failed to stop quartz scheduler ===");
            return;
        }
        LOGGER.log(Level.INFO, "=== Scheduler sucessfully stopped ===");    
    }
    
}
