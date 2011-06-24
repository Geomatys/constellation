/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import org.constellation.configuration.ConfigDirectory;
import org.geotoolkit.util.logging.Logging;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Constellation can run tasks at regular intervals. 
 * The scheduler used is Quartz, added tasks are stored in a configuration
 * file and are restarted when the server starts.
 * 
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class CstlScheduler {
    
    private static final Logger LOGGER = Logging.getLogger(CstlScheduler.class);
    private static final String TASK_FILE = "scheduler-tasks.xml";    
    private static CstlScheduler INSTANCE = null;
        
    private final List<Task> tasks = new ArrayList<Task>();
    private Scheduler quartzScheduler;
    
    private CstlScheduler(){
        
        try {
            loadTasks();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        } catch (XMLStreamException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
        
        LOGGER.log(Level.WARNING, "=== Starting Constellation Scheduler ===");
        final SchedulerFactory schedFact = new StdSchedulerFactory();
        try {
            quartzScheduler = schedFact.getScheduler();
            quartzScheduler.start();
        } catch (SchedulerException ex) {
            LOGGER.log(Level.SEVERE, "=== Failed to start quartz scheduler ===\n"+ex.getLocalizedMessage(), ex);            
            return;
        }
        LOGGER.log(Level.SEVERE, "=== Constellation Scheduler sucessfully started ===");    
        
        for(Task t : tasks){
            try {
                registerTask(t);
            } catch (SchedulerException ex) {
                LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }
    
    /**
     * @return unmodifiable list of all tasks.
     */
    public List<Task> listTasks(){
        return Collections.unmodifiableList(tasks);
    }
    
    /**
     * Load tasks defined in the configuration file.
     */
    private void loadTasks() throws IOException, XMLStreamException{
        //open configuration file
        final File configDir = ConfigDirectory.getConfigDirectory();
        final File taskFile = new File(configDir, TASK_FILE);
        if(!taskFile.exists()){
            return;
        }
        
        final TasksReader reader = new TasksReader();
        reader.setInput(taskFile);
        final List<Task> candidates = reader.read();
        if(candidates != null){
            tasks.addAll(candidates);
        }
    }
    
    /**
     * Add the given task in the scheduler.
     */
    private void registerTask(final Task task) throws SchedulerException{
        LOGGER.log(Level.INFO, "Schedule task id :{0}   type : {1}.{2}", new Object[]{
            task.getId(), 
            task.getDetail().getFactoryIdentifier(), 
            task.getDetail().getProcessIdentifier()});
        quartzScheduler.scheduleJob(task.getDetail(), task.getTrigger());
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
    
}
