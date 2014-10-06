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

import java.io.IOException;
import java.io.PrintWriter;
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
import org.constellation.engine.register.TaskParameter;
import org.constellation.engine.register.repository.ChainProcessRepository;
import org.constellation.engine.register.repository.TaskParameterRepository;
import org.constellation.engine.register.repository.TaskRepository;
import org.constellation.scheduler.CstlSchedulerListener;
import org.constellation.scheduler.QuartzJobListener;
import org.constellation.scheduler.Task;
import org.constellation.scheduler.TaskState;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.geotoolkit.process.Process;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessEvent;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.process.ProcessListenerAdapter;
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
import org.opengis.util.InternationalString;
import org.opengis.util.NoSuchIdentifierException;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import static org.quartz.impl.matchers.EverythingMatcher.allJobs;

/**
 * 
 * @author Guilhem Legal (Geomatys)
 */
@Component
@Primary
@DependsOn("database-initer")
public class ProcessBusiness implements IProcessBusiness {


//    //execute once tasks
//    private final List<Task> once = new ArrayList<>();

    private static final Logger LOGGER = Logging.getLogger(ProcessBusiness.class);

    private final QuartzJobListener quartzListener = new QuartzJobListener(this);

    private Scheduler quartzScheduler;

    @Inject
    private TaskParameterRepository taskParameterRepository;

    @Inject
    private TaskRepository taskRepository;

    @Inject
    private ChainProcessRepository chainRepository;

    @PostConstruct
    public void init(){
        LOGGER.log(Level.INFO, "=== Starting Constellation Scheduler ===");
        List<org.constellation.scheduler.Task> tasks = new ArrayList<>();
        try {
            tasks = loadProgrammedTasks();
        } catch (Exception ex) {
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

        for(org.constellation.scheduler.Task t : tasks){
            try {
                registerTaskInScheduler(t);
            } catch (ConstellationException ex) {
                LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }

    /**
     * Get task status.
     * @param id, must be a valid id. Illegal
     * @return TaskState, null if no task exist for this id.
     */
    public TaskState geTaskState(final String id){
        final org.constellation.engine.register.Task taskEntity = taskRepository.get(id);
        TaskParameter taskParameter = taskParameterRepository.get(taskEntity.getTaskParameterId());
        TaskState taskState = new TaskState();
        taskState.setStatus(TaskState.Status.valueOf(taskEntity.getState()));
        taskState.setTitle(taskParameter.getName());
        taskState.setMessage(taskEntity.getMessage());
        //TODO percent not set cause not know
        taskState.setPercent(0);
        //TODO exception if exist stored in string message atribute from taskEntity
//        taskState.setLastException(");
        return taskState;
    }

    @Override
    public Task getProcessTask(String id) {

        //TODO a faire si utile
        return null;
    }

    @Override
    public void addListenerOnRunningTasks(CstlSchedulerListener cstlSchedulerListener) {
        //TODO a faire si utile

    }

    @Override
    public void addTask(Task task) throws ConstellationException {
        //TODO a faire si utile

    }

    @Override
    public boolean updateTask(Task task) throws ConstellationException {
        //TODO a faire si utile
        return false;
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
    private synchronized List<Task> loadProgrammedTasks() throws Exception{
        List<org.constellation.scheduler.Task> tasks = new ArrayList<>();
        final List<? extends TaskParameter> programmedTasks = taskParameterRepository.findProgrammedTasks();
        for( TaskParameter taskParameter : programmedTasks){
            org.constellation.scheduler.Task task = new org.constellation.scheduler.Task();
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
            task.setDetail(processJobDetails);
            task.setTitle(taskParameter.getName());
            tasks.add(task);
        }

        return tasks;
    }

    /**
     * Add the given task in the scheduler.
     */
    private void registerTaskInScheduler(final org.constellation.scheduler.Task task) throws ConstellationException{
        //ensure the job detail contain the task in the datamap, this is used in the
        //job listener to track back the task
        task.getDetail().getJobDataMap().put(QuartzJobListener.PROPERTY_TASK, task);

        try {
            quartzScheduler.scheduleJob(task.getDetail(), task.getTrigger());
        } catch (SchedulerException e) {
            throw new ConstellationException(e);
        }
        LOGGER.log(Level.INFO, "Scheduler task added : {0}, {1}   type : {2}.{3}", new Object[]{
                task.getId(),
                task.getTitle(),
                task.getDetail().getFactoryIdentifier(),
                task.getDetail().getProcessIdentifier()});
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



    /*public void writeTask(String uuidTask, String pyramid, Integer userId, final long start) {
        final org.constellation.engine.register.Task t = new org.constellation.engine.register.Task(uuidTask, "PENDING", "TODO-TYPE", start, null, userId,null);
        taskRepository.create(t);
    }*/

    /**
     * get specific task from task journal (running or finished)
     * @param uuid task id
     * @return task object
     */
    public org.constellation.engine.register.Task getTask(String uuid) {
        return taskRepository.get(uuid);
    }

    @Override
    public List<org.constellation.engine.register.Task> listRunningTasks() {
        List<org.constellation.engine.register.Task> result = new ArrayList<>();
        result = taskRepository.findRunningTasks();
        return result;
    }



    public void update(org.constellation.engine.register.Task task) {
        taskRepository.update(task);
    }
     
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
    
    public boolean deleteChainProcess(final String auth, final String code) {
        final ChainProcess chain = chainRepository.findOne(auth, code);
        if (chain != null) {
            chainRepository.delete(chain.getId());
            return true;
        }
        return false;
    }
    
    public ChainProcess getChainProcess(final String auth, final String code) {
        return chainRepository.findOne(auth, code);
    }

    public void runOnce(String title, Process process, Integer taskParameterId, Integer userId) throws ConstellationException {
        final TriggerBuilder tb = TriggerBuilder.newTrigger();
        final Trigger trigger = tb.startNow().build();

        final Task task = new Task(UUID.randomUUID().toString());
        final ProcessJobDetail detail = new ProcessJobDetail(process);
        task.setDetail(detail);
        task.setTitle(title);
        task.setTrigger((SimpleTrigger)trigger);
        final org.constellation.engine.register.Task taskEntity = new org.constellation.engine.register.Task();
        taskEntity.setIdentifier(task.getId());
        taskEntity.setState(TaskState.Status.PENDING.name());
        taskEntity.setOwner(userId);

        taskEntity.setTaskParameterId(taskParameterId);
        //TODO ???
        taskEntity.setType("");
        taskRepository.create(taskEntity);

        //add listener on this task to remove it from the list once finished
        process.addListener(new ProcessListenerAdapter(){

            @Override
            public void started(final ProcessEvent event) {
                taskEntity.setState(TaskState.Status.RUNNING.name());
                taskEntity.setMessage(toString(event.getTask()));
                taskEntity.setStart(System.currentTimeMillis());
                taskRepository.update(taskEntity);
            }
            @Override
            public void failed(ProcessEvent event) {
                taskEntity.setState(TaskState.Status.FAILED.name());
                StringWriter errors = new StringWriter();
                event.getException().printStackTrace(new PrintWriter(errors));
                taskEntity.setMessage(toString(event.getTask())+ " cause : " + errors.toString());
                taskEntity.setEnd(System.currentTimeMillis());
                taskRepository.update(taskEntity);
            }
            @Override
            public void completed(ProcessEvent event) {
                taskEntity.setState(TaskState.Status.SUCCEED.name());
                taskEntity.setMessage(toString(event.getTask()));
                taskEntity.setEnd(System.currentTimeMillis());
                taskRepository.update(taskEntity);
            }

            private String toString(InternationalString str){
                if(str==null){
                    return "";
                }else{
                    return str.toString();
                }
            }

        });

        registerTaskInScheduler(task);
    }

    /**
     * remove task from quartz scheduler and update record in database
     * @param uuidTask
     * @return true if
     * @throws ConstellationException
     */

    @Override
    public void removeTask(String uuidTask) throws ConstellationException {
        //TODO a tester
        org.constellation.engine.register.Task task = taskRepository.get(uuidTask);
        final TaskParameter taskParameter = taskParameterRepository.get(task.getTaskParameterId());
        Task scheduledTask = new Task();
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
        scheduledTask.setDetail(processJobDetails);

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
        LOGGER.log(Level.INFO, "=== Scheduler sucessfully stopped ===");
    }
}
