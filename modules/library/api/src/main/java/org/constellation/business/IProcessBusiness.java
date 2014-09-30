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

import org.constellation.admin.exception.ConstellationException;
import org.constellation.configuration.ConfigurationException;
import org.constellation.engine.register.ChainProcess;
import org.constellation.engine.register.Task;
import org.constellation.engine.register.TaskParameter;
import org.constellation.scheduler.CstlSchedulerListener;
import org.constellation.scheduler.TaskState;
import org.geotoolkit.process.*;
import org.geotoolkit.process.Process;
import org.geotoolkit.process.chain.model.Chain;
import org.quartz.SchedulerException;

import java.util.List;

/**
 * @author Cédric Briançon (Geomatys)
 */
public interface IProcessBusiness {
    List<ProcessDescriptor> getChainDescriptors() throws ConstellationException;

    void createChainProcess(final Chain chain) throws ConstellationException;

    boolean deleteChainProcess(final String auth, final String code);

    ChainProcess getChainProcess(final String auth, final String code);

//    void writeTask(String uuidTask, String pyramid, Integer userId, final long start);

    void update(Task task);

    Task getTask(String uuid);

    List<Task> listRunningTasks();

    void runOnce(String title, Process process, Integer taskParameterId, Integer userId) throws ConstellationException;

    void removeTask(String id) throws ConstellationException;

    TaskState geTaskState(String id);

    org.constellation.scheduler.Task getProcessTask(String id);

    void addListenerOnRunningTasks(CstlSchedulerListener cstlSchedulerListener);

    void addTask(org.constellation.scheduler.Task task) throws ConstellationException;

    boolean updateTask(org.constellation.scheduler.Task task) throws ConstellationException;

    TaskParameter addTaskParameter(TaskParameter taskParameter);
}
