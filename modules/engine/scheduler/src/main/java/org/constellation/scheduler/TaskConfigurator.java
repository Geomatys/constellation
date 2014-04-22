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

import java.util.List;
import java.util.logging.Logger;
import org.apache.sis.util.logging.Logging;
import org.constellation.configuration.ConfigurationException;

/**
 * Responsible for saving tasks definitions.
 * 
 * @author Johann Sorel (Geomatys)
 */
public interface TaskConfigurator {
    
    public static final Logger LOGGER = Logging.getLogger(TaskConfigurator.class);
    
    /**
     * Get a list of all tasks.
     * @return List of Task
     * @throws ConfigurationException 
     */
    List<Task> getTasksConfiguration() throws ConfigurationException;
    
    /**
     * Get configuration for one task.
     * @param taskId 
     * @return Configuration or null
     */
    Task getTaskConfiguration(String taskId) throws ConfigurationException;
    
    /**
     * Store a new task configuration.
     * @param task
     */
    void addTaskConfiguration(Task task) throws ConfigurationException;
    
    /**
     * Save an existing task updated configuration.
     * @param task
     * @throws ConfigurationException 
     */
    void updateTaskConfiguration(Task task) throws ConfigurationException;
    
    /**
     * Remove a task configuration.
     * @param task
     * @throws ConfigurationException 
     */
    void removeTaskConfiguration(Task task) throws ConfigurationException;
    
    
    
}
