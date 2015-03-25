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
package org.constellation.business;

import java.util.List;

import org.constellation.admin.exception.ConstellationException;
import org.constellation.configuration.ConfigurationException;
import org.constellation.engine.register.jooq.tables.pojos.ChainProcess;
import org.constellation.engine.register.jooq.tables.pojos.Task;
import org.constellation.engine.register.jooq.tables.pojos.TaskParameter;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.process.Process;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.chain.model.Chain;
import org.quartz.JobListener;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @author Cédric Briançon (Geomatys)
 * @author Christophe Mourette (Geomatys)
 * @author Quentin Boileau (Geomatys)
 */
public interface IProcessBusiness {

    /**
     * The returned list is a subset of what can be found with ProcessFinder.
     * But only process with simple types arguments are preserved.
     *
     * @return List of all available process.
     */
    List<Name> listProcess();

    /**
     * The returned list is a subset of what can be found with ProcessFinder.
     * But only process with simple types arguments are preserved.
     *
     * @param authorityCode
     * @return List of all available process.
     */
    List<String> listProcessForFactory(final String authorityCode);

    /**
     * The returned list of all found ProcessingRegistry with ProcessFinder.
     *
     * @return List of all available process registry.
     */
    List<String> listProcessFactory();

    void registerQuartzListener(JobListener jobListener) throws ConstellationException;

    List<ProcessDescriptor> getChainDescriptors() throws ConstellationException;

    void createChainProcess(final Chain chain) throws ConstellationException;

    boolean deleteChainProcess(final String auth, final String code);

    ChainProcess getChainProcess(final String auth, final String code);

    Task getTask(String uuid);

    Task addTask(Task task) throws ConstellationException;

    void updateTask(Task task) throws ConstellationException;

    List<Task> listRunningTasks();

    /**
     * List all task for a TaskParameter id
     * @param id TaskParameter
     * @return
     */
    List<Task> listRunningTasks(Integer id, Integer offset, Integer limit);

    /**
     * List all task for a TaskParameter id
     * @param id TaskParameter
     * @return
     */
    List<Task> listTaskHistory(Integer id, Integer offset, Integer limit);

    /**
     * Run an instantiated geotk process on scheduler only once.
     *
     * @param title
     * @param process
     * @param taskParameterId
     * @param userId
     * @throws ConstellationException
     */
    void runProcess(String title, Process process, Integer taskParameterId, Integer userId) throws ConstellationException;

    /**
     * Instantiate and run once a TaskParameter
     *
     * @param task
     * @param title
     * @param userId
     * @throws ConstellationException
     * @throws ConfigurationException
     */
    void executeTaskParameter (TaskParameter task, String title, Integer userId) throws ConstellationException, ConfigurationException;

    /**
     * Add a TaskParameter with trigger on scheduler.
     *
     * @param task
     * @param title
     * @param userId
     * @param checkEndDate flag that used to throw exception if CRON endDate is invalid.
     * @throws ConstellationException
     * @throws ConfigurationException
     */
    void scheduleTaskParameter (TaskParameter task, String title, Integer userId, boolean checkEndDate) throws ConstellationException, ConfigurationException;

    /**
     * Remove TaskParameter from scheduler.
     * @param task
     * @param userId
     * @throws ConstellationException
     * @throws ConfigurationException
     */
    void stopScheduleTaskParameter (TaskParameter task, Integer userId) throws ConstellationException, ConfigurationException;

    TaskParameter getTaskParameterById(Integer id);

    TaskParameter addTaskParameter(TaskParameter taskParameter);

    void updateTaskParameter(TaskParameter taskParameter);

    void deleteTaskParameter(TaskParameter taskParameter);

    List<TaskParameter> findTaskParameterByNameAndProcess(String name, String authority, String code);

    /**
     * Test TaskParameter inputs
     * @param taskParameter
     * @return
     */
    void testTaskParameter(TaskParameter taskParameter) throws ConfigurationException;
}
