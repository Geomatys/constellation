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
package org.constellation.admin;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;

import org.apache.sis.metadata.iso.DefaultIdentifier;
import org.apache.sis.metadata.iso.citation.DefaultCitation;
import org.apache.sis.metadata.iso.identification.DefaultServiceIdentification;
import org.apache.sis.util.logging.Logging;
import org.constellation.admin.exception.ConstellationException;
import org.constellation.business.IProcessBusiness;
import org.constellation.configuration.ConfigurationException;
import org.constellation.engine.register.ChainProcess;
import org.constellation.engine.register.Task;
import org.constellation.engine.register.TaskParameter;
import org.constellation.engine.register.repository.ChainProcessRepository;
import org.constellation.engine.register.repository.TaskParameterRepository;
import org.constellation.engine.register.repository.TaskRepository;
import org.constellation.scheduler.CstlSchedulerListener;
import org.constellation.scheduler.QuartzJobListener;
import org.constellation.scheduler.QuartzTask;
import org.constellation.scheduler.TaskState;
import org.geotoolkit.io.DirectoryWatcher;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.geotoolkit.process.*;
import org.geotoolkit.process.Process;
import org.geotoolkit.process.chain.ChainProcessDescriptor;
import org.geotoolkit.process.chain.model.Chain;
import org.geotoolkit.process.chain.model.ChainMarshallerPool;
import org.geotoolkit.process.quartz.ProcessJobDetail;
import org.geotoolkit.xml.parameter.ParameterValueReader;
import org.opengis.metadata.Identifier;
import org.opengis.metadata.identification.Identification;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import static org.quartz.impl.matchers.EverythingMatcher.allJobs;

/**
 *
 * @author Cédric Briançon (Geomatys)
 * @author Christophe Mourette (Geomatys)
 * @author Quentin Boileau (Geomatys)
 * @author Guilhem Legal (Geomatys)
 */
@Component
@Primary
@DependsOn("database-initer")
public class ProcessBusiness implements IProcessBusiness {

    private static final Logger LOGGER = Logging.getLogger(ProcessBusiness.class);

    private final QuartzJobListener quartzListener = new QuartzJobListener(this);

    private Scheduler quartzScheduler;

    private DirectoryWatcher directoryWatcher;

    @Inject
    private TaskParameterRepository taskParameterRepository;

    @Inject
    private TaskRepository taskRepository;

    @Inject
    private ChainProcessRepository chainRepository;

    @PostConstruct
    public void init(){
        LOGGER.log(Level.INFO, "=== Starting Constellation Scheduler ===");
        /*
            Quartz scheduler
         */
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
        LOGGER.log(Level.INFO, "=== Constellation Scheduler successfully started ===");

        /*
            DirectoryWatcher
         */
        LOGGER.log(Level.INFO, "=== Starting directory watcher ===");
        try {
            directoryWatcher = new DirectoryWatcher(true);
            directoryWatcher.start();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "=== Failed to start directory watcher ===\n"+ex.getLocalizedMessage(), ex);
            return;
        }
        LOGGER.log(Level.INFO, "=== Directory watcher successfully started ===");

        /*
          Re-programme taskParameters with trigger in scheduler.
         */
        List<? extends TaskParameter> programmedTasks = taskParameterRepository.findProgrammedTasks();
        for (TaskParameter taskParameter : programmedTasks) {
            try {
                scheduleTaskParameter(taskParameter, taskParameter.getName(), taskParameter.getOwner());
            } catch (ConstellationException ex) {
                LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }

    @Override
    public QuartzTask getProcessTask(String id) {
        //TODO a faire si utile
        return null;
    }

    @Override
    public void addListenerOnRunningTasks(CstlSchedulerListener cstlSchedulerListener) {
        //TODO a faire si utile
    }

    @Override
    public TaskParameter addTaskParameter(TaskParameter taskParameter) {
        return taskParameterRepository.create(taskParameter);
    }


    private ParameterDescriptorGroup getDescriptor(final String authority, final String code) throws ConstellationException {
        final ProcessDescriptor desc;
        try {
            desc = ProcessFinder.getProcessDescriptor(authority, code);
        } catch (NoSuchIdentifierException ex) {
            throw new ConstellationException("No Process for id : {" + authority + "}"+code+" has been found");
        } catch (InvalidParameterValueException ex) {
            throw new ConstellationException(ex);
        }
        if(desc == null){
            throw new ConstellationException("No Process for id : {" + authority + "}"+code+" has been found");
        }

        //change the description, always encapsulate in the same namespace and name
        //jaxb object factory can not reconize changing names without a namespace
        ParameterDescriptorGroup idesc = desc.getInputDescriptor();
        idesc = new DefaultParameterDescriptorGroup("input", idesc.descriptors().toArray(new GeneralParameterDescriptor[0]));
        return idesc;
    }


    /**
     * Load tasks defined as programmed in the system
     */
    private synchronized List<QuartzTask> loadProgrammedTasks() throws Exception{
        List<QuartzTask> quartzTasks = new ArrayList<>();
        final List<? extends TaskParameter> programmedTasks = taskParameterRepository.findProgrammedTasks();
        for( TaskParameter taskParameter : programmedTasks){
            final QuartzTask quartzTask = new QuartzTask();
            final GeneralParameterDescriptor retypedDesc = getDescriptor(taskParameter.getProcessAuthority(), taskParameter.getProcessCode());

            final ParameterValueGroup params;
            final ParameterValueReader reader = new ParameterValueReader(retypedDesc);
            try {
                reader.setInput(taskParameter.getInputs());
                params = (ParameterValueGroup) reader.read();
                reader.dispose();
            } catch (XMLStreamException | IOException ex) {
                throw new ConfigurationException(ex);
            }
            ProcessJobDetail processJobDetails = new ProcessJobDetail(taskParameter.getProcessAuthority(), taskParameter.getProcessCode(),params );
            quartzTask.setDetail(processJobDetails);
            quartzTask.setTitle(taskParameter.getName());
            quartzTasks.add(quartzTask);
        }

        return quartzTasks;
    }

    /**
     * Add the given task in the scheduler.
     */
    private void registerTaskInScheduler(final QuartzTask quartzTask) throws ConstellationException{
        //ensure the job detail contain the task in the datamap, this is used in the
        //job listener to track back the task
        quartzTask.getDetail().getJobDataMap().put(QuartzJobListener.PROPERTY_TASK, quartzTask);

        try {
            quartzScheduler.scheduleJob(quartzTask.getDetail(), quartzTask.getTrigger());
        } catch (SchedulerException e) {
            throw new ConstellationException(e);
        }
        LOGGER.log(Level.INFO, "Scheduler task added : {0}, {1}   type : {2}.{3}", new Object[]{
                quartzTask.getId(),
                quartzTask.getTitle(),
                quartzTask.getDetail().getFactoryIdentifier(),
                quartzTask.getDetail().getProcessIdentifier()});
    }

    /**
     * unregister the given task in the scheduler.
     */
    private void unregisterTaskInScheduler(final ProcessJobDetail processJobDetail, final String uuidTask) throws SchedulerException{
        quartzScheduler.interrupt(processJobDetail.getKey());
        final boolean removed = quartzScheduler.deleteJob(processJobDetail.getKey());

        if(removed){
            LOGGER.log(Level.INFO, "Scheduler task removed : "+uuidTask);
        }else{
            LOGGER.log(Level.WARNING, "Scheduler failed to remove task : "+uuidTask);
        }

    }

    /**
     * Get specific task from task journal (running or finished)
     * @param uuid task id
     * @return task object
     */
    @Override
    public org.constellation.engine.register.Task getTask(String uuid) {
        return taskRepository.get(uuid);
    }

    @Override
    public Task addTask(org.constellation.engine.register.Task task) throws ConstellationException {
        return taskRepository.create(task);
    }

    @Override
    public void updateTask(org.constellation.engine.register.Task task) throws ConstellationException {
        taskRepository.update(task);
    }

    @Override
    public List<org.constellation.engine.register.Task> listRunningTasks() {
        return taskRepository.findRunningTasks();
    }

    @Override
    public List<org.constellation.engine.register.Task> listRunningTasks(Integer id) {
        return taskRepository.findRunningTasks(id);
    }

    @Override
    public List<ProcessDescriptor> getChainDescriptors() throws ConstellationException {
        final List<ProcessDescriptor> result = new ArrayList<>();
        final List<ChainProcess> chains = chainRepository.findAll();
        for (ChainProcess cp : chains) {
            try {
                final Unmarshaller u = ChainMarshallerPool.getInstance().acquireUnmarshaller();
                final Chain chain = (Chain) u.unmarshal(new StringReader(cp.getConfig()));
                ChainMarshallerPool.getInstance().recycle(u);
                final ProcessDescriptor desc = new ChainProcessDescriptor(chain, buildIdentification(chain.getName()));
                result.add(desc);
            } catch (JAXBException ex) {
                throw new ConstellationException("Unable to unmarshall chain configuration:" + cp.getId(), ex);
            }
        }
        return result;
    }
    
    public Identification buildIdentification(final String name) {
        final DefaultServiceIdentification ident = new DefaultServiceIdentification();
        final Identifier id = new DefaultIdentifier(name);
        final DefaultCitation citation = new DefaultCitation(name);
        citation.setIdentifiers(Collections.singleton(id));
        ident.setCitation(citation);
        return ident;
    }

    @Override
    public void createChainProcess(final Chain chain) throws ConstellationException {
        final String code = chain.getName();
        String config = null;
        try {
            final Marshaller m = ChainMarshallerPool.getInstance().acquireMarshaller();
            final StringWriter sw = new StringWriter();
            m.marshal(chain, sw);
            ChainMarshallerPool.getInstance().recycle(m);
            config = sw.toString();
        } catch (JAXBException ex) {
            throw new ConstellationException("Unable to marshall chain configuration",ex);
        }
        final ChainProcess process = new ChainProcess("constellation", code, config);
        chainRepository.create(process);
    }

    @Override
    public boolean deleteChainProcess(final String auth, final String code) {
        final ChainProcess chain = chainRepository.findOne(auth, code);
        if (chain != null) {
            chainRepository.delete(chain.getId());
            return true;
        }
        return false;
    }

    @Override
    public ChainProcess getChainProcess(final String auth, final String code) {
        return chainRepository.findOne(auth, code);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void runProcess(String title, Process process, Integer taskParameterId, Integer userId) throws ConstellationException {
        final TriggerBuilder tb = TriggerBuilder.newTrigger();
        final Trigger trigger = tb.startNow().build();
        final ProcessJobDetail detail = new ProcessJobDetail(process);

        final QuartzTask quartzTask = new QuartzTask(UUID.randomUUID().toString());
        quartzTask.setDetail(detail);
        quartzTask.setTitle(title);
        quartzTask.setTrigger((SimpleTrigger) trigger);
        quartzTask.setTaskParameterId(taskParameterId);
        quartzTask.setUserId(userId);

        registerTaskInScheduler(quartzTask);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeTaskParameter (TaskParameter taskParameter, String title, Integer userId)
            throws ConstellationException, ConfigurationException {

        final GeneralParameterDescriptor retypedDesc = getDescriptor(taskParameter.getProcessAuthority(), taskParameter.getProcessCode());

        final ParameterValueGroup params;
        final ParameterValueReader reader = new ParameterValueReader(retypedDesc);
        try {
            reader.setInput(taskParameter.getInputs());
            params = (ParameterValueGroup) reader.read();
            reader.dispose();
        } catch (XMLStreamException | IOException ex) {
            throw new ConfigurationException(ex);
        }

        //rebuild original values since we have changed the namespace
        try {
            final ProcessDescriptor processDesc = ProcessFinder.getProcessDescriptor(taskParameter.getProcessAuthority(), taskParameter.getProcessCode());
            final ParameterDescriptorGroup originalDesc = processDesc.getInputDescriptor();
            final ParameterValueGroup orig = originalDesc.createValue();
            orig.values().addAll(params.values());
            final Process process = processDesc.createProcess(orig);

            runProcess(title, process, taskParameter.getId(), userId);
        } catch (NoSuchIdentifierException ex) {
            throw new ConstellationException("No process for given id.", ex);
        }  catch (InvalidParameterValueException ex) {
            throw new ConfigurationException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void scheduleTaskParameter (TaskParameter task, String title, Integer userId) throws ConstellationException {
        if (task.getTriggerType() != null && task.getTrigger() != null) {

            if ("CRON".equalsIgnoreCase(task.getTriggerType())) {
                // TODO

            } else if ("FOLDER".equalsIgnoreCase(task.getTriggerType())) {
                File folder = new File(task.getTrigger());
                if (folder.exists() && folder.isDirectory()) {
                    // TODO
                } else {
                    throw new ConstellationException("Invalid folder trigger : "+task.getTrigger());
                }
            }
        }
    }

    /**
     * Remove task from quartz scheduler and update record in database
     * @param uuidTask
     * @return true if
     * @throws ConstellationException
     */
    @Override
    public void removeTask(String uuidTask) throws ConstellationException {
        //TODO a tester
        org.constellation.engine.register.Task task = taskRepository.get(uuidTask);
        final TaskParameter taskParameter = taskParameterRepository.get(task.getTaskParameterId());
        QuartzTask scheduledQuartzTask = new QuartzTask();
        final GeneralParameterDescriptor retypedDesc = getDescriptor(taskParameter.getProcessAuthority(), taskParameter.getProcessCode());

        final ParameterValueGroup params;
        final ParameterValueReader reader = new ParameterValueReader(retypedDesc);
        try {
            reader.setInput(taskParameter.getInputs());
            params = (ParameterValueGroup) reader.read();
            reader.dispose();
        } catch (XMLStreamException | IOException ex) {
            throw new ConstellationException(ex);
        }
        ProcessJobDetail processJobDetails = new ProcessJobDetail(taskParameter.getProcessAuthority(), taskParameter.getProcessCode(),params );
        scheduledQuartzTask.setDetail(processJobDetails);

        try {
            unregisterTaskInScheduler(processJobDetails, uuidTask);
        } catch (SchedulerException e) {
            throw new ConstellationException("cannot unregister from scheduler", e);
        }

        task.setState(TaskState.Status.CANCELLED.name());
        taskRepository.update(task);
        //TODO verify why returning false before refactoring

    }

    @PreDestroy
    public void stop() {
        LOGGER.log(Level.INFO, "=== Stopping Scheduler ===");
        try {
            quartzScheduler.shutdown();
        } catch (SchedulerException ex) {
            LOGGER.log(Level.SEVERE, "=== Failed to stop quartz scheduler ===");
            return;
        }
        LOGGER.log(Level.INFO, "=== Scheduler successfully stopped ===");

        LOGGER.log(Level.INFO, "=== Stopping directory watcher ===");
        try {
            directoryWatcher.close();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "=== Failed to stop directory watcher ===");
            return;
        }
        LOGGER.log(Level.INFO, "=== Directory watcher successfully stopped ===");
    }
}
