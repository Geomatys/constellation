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
import org.constellation.configuration.ConfigurationException;

import java.util.List;
import java.util.logging.Logger;

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
