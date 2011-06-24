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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    
    private static final Scheduler SCHEDULER;
    private static CstlScheduler INSTANCE = null;
    
    static {
        
        Scheduler temp = null;
        final SchedulerFactory schedFact = new StdSchedulerFactory();
        try {
            temp = schedFact.getScheduler();
            temp.start();
        } catch (SchedulerException ex) {
            Logger.getLogger(CstlScheduler.class.getName()).log(Level.WARNING, null, ex);
        }
        
        SCHEDULER = temp;
    }
    
    private final List<Task> tasks = new ArrayList<Task>();
    
    private CstlScheduler(){
        loadTasks();
        
        for(Task t : tasks){
            try {
                registerTask(t);
            } catch (SchedulerException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }
    }
    
    public List<Task> listTasks(){
        return Collections.unmodifiableList(tasks);
    }
    
    /**
     * Load tasks defined in the configuration file.
     */
    private void loadTasks(){
        //open configuration file
    }
    
    /**
     * Add the given task in the scheduler.
     */
    private void registerTask(final Task task) throws SchedulerException{
        SCHEDULER.scheduleJob(task.getDetail(), task.getTrigger());
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
