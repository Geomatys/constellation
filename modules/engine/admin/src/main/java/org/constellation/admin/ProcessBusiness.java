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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.sis.metadata.iso.DefaultIdentifier;
import org.apache.sis.metadata.iso.citation.DefaultCitation;
import org.apache.sis.metadata.iso.identification.DefaultServiceIdentification;
import org.constellation.admin.exception.ConstellationException;
import org.constellation.api.TaskState;
import org.constellation.business.IProcessBusiness;
import org.constellation.configuration.ConfigurationException;
import org.constellation.engine.register.ChainProcess;
import org.constellation.engine.register.Task;
import org.constellation.engine.register.TaskParameter;
import org.constellation.engine.register.repository.ChainProcessRepository;
import org.constellation.engine.register.repository.TaskParameterRepository;
import org.constellation.engine.register.repository.TaskRepository;
import org.constellation.scheduler.QuartzJobListener;
import org.constellation.scheduler.QuartzTask;
import org.geotoolkit.feature.type.DefaultName;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.io.DirectoryWatcher;
import org.geotoolkit.io.PathChangeListener;
import org.geotoolkit.io.PathChangedEvent;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.geotoolkit.parameter.Parameters;
import org.geotoolkit.parameter.ParametersExt;
import org.geotoolkit.process.Process;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.process.ProcessingRegistry;
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
import org.quartz.CronScheduleBuilder;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.quartz.impl.matchers.EverythingMatcher.allJobs;

/**
 *
 * @author Cédric Briançon (Geomatys)
 * @author Christophe Mourette (Geomatys)
 * @author Quentin Boileau (Geomatys)
 * @author Guilhem Legal (Geomatys)
 */
@Component(ProcessBusiness.BEAN_NAME)
@Primary
@DependsOn("database-initer")
public class ProcessBusiness implements IProcessBusiness {

    public static final String BEAN_NAME = "processBusiness";

    private static final DateFormat TASK_DATE = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessBusiness.class);

    @Inject
    private TaskParameterRepository taskParameterRepository;

    @Inject
    private TaskRepository taskRepository;

    @Inject
    private ChainProcessRepository chainRepository;

    private Scheduler quartzScheduler;

    private DirectoryWatcher directoryWatcher;

    private Map<Integer, Object> scheduledTasks = new HashMap<>();

    @PostConstruct
    public void init(){
        cleanTasksStates();

        LOGGER.info("=== Starting Constellation Scheduler ===");
        /*
            Quartz scheduler
         */
        Properties properties;
        try {
            properties = new Properties();
            properties.load(ProcessBusiness.class.getResourceAsStream("/org/constellation/scheduler/tasks-quartz.properties"));
        } catch (IOException e) {
            LOGGER.warn("Failed to load quartz properties", e);
            //use default quartz configuration
            properties = null;
        }

        try {
            final StdSchedulerFactory schedFact = new StdSchedulerFactory();
            if (properties != null) {
                schedFact.initialize(properties);
            }
            quartzScheduler = schedFact.getScheduler();
            quartzScheduler.start();

            //listen and attach a process on all geotk process tasks
            quartzScheduler.getListenerManager().addJobListener(new QuartzJobListener(this), allJobs());

        } catch (SchedulerException ex) {
            LOGGER.error("=== Failed to start quartz scheduler ===\n"+ex.getLocalizedMessage(), ex);
            return;
        }
        LOGGER.info("=== Constellation Scheduler successfully started ===");

        /*
            DirectoryWatcher
         */
        LOGGER.info("=== Starting directory watcher ===");
        try {
            directoryWatcher = new DirectoryWatcher(true);
            directoryWatcher.start();

            final PathChangeListener pathListener = new PathChangeListener() {
                @Override
                public void pathChanged(PathChangedEvent event) {
                    Path target = event.target;

                    for (Map.Entry<Integer, Object> sTask : scheduledTasks.entrySet()) {
                        if (sTask.getValue() instanceof Path && target.startsWith((Path) sTask.getValue())) {
                            final Integer taskId = sTask.getKey();
                            final TaskParameter taskParameter = taskParameterRepository.get(taskId);
                            try {
                                executeTaskParameter(taskParameter, null, taskParameter.getOwner());
                            } catch (ConfigurationException ex) {
                                LOGGER.warn(ex.getMessage(), ex);
                            }
                        }
                    }
                }
            };
            directoryWatcher.addPathChangeListener(pathListener);

        } catch (IOException ex) {
            LOGGER.error("=== Failed to start directory watcher ===\n"+ex.getLocalizedMessage(), ex);
            return;
        }
        LOGGER.info("=== Directory watcher successfully started ===");

        /*
          Re-programme taskParameters with trigger in scheduler.
         */
        List<? extends TaskParameter> programmedTasks = taskParameterRepository.findProgrammedTasks();
        for (TaskParameter taskParameter : programmedTasks) {
            try {
                scheduleTaskParameter(taskParameter, taskParameter.getName(), taskParameter.getOwner(), false);
            } catch (ConstellationException ex) {
                LOGGER.warn(ex.getMessage(), ex);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Name> listProcess(){
        final List<Name> names = new ArrayList<>();

        final Iterator<ProcessingRegistry> ite = ProcessFinder.getProcessFactories();
        while(ite.hasNext()){
            final ProcessingRegistry factory = ite.next();
            final String authorityCode = factory.getIdentification().getCitation()
                    .getIdentifiers().iterator().next().getCode();

            for(String processCode : factory.getNames()){
                names.add(new DefaultName(authorityCode, processCode));
            }
        }

        return names;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> listProcessForFactory(final String authorityCode){
        final List<String> names = new ArrayList<>();

        final Iterator<ProcessingRegistry> ite = ProcessFinder.getProcessFactories();
        while(ite.hasNext()){
            final ProcessingRegistry factory = ite.next();
            final String currentAuthorityCode = factory.getIdentification().getCitation()
                    .getIdentifiers().iterator().next().getCode();
            if (currentAuthorityCode.equals(authorityCode)) {
                for(String processCode : factory.getNames()){
                    names.add(processCode);
                }
            }
        }
        return names;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> listProcessFactory(){
        final List<String> names = new ArrayList<>();

        final Iterator<ProcessingRegistry> ite = ProcessFinder.getProcessFactories();
        while(ite.hasNext()){
            final ProcessingRegistry factory = ite.next();
            names.add(factory.getIdentification().getCitation()
                    .getIdentifiers().iterator().next().getCode());
        }
        return names;
    }

    @Override
    public TaskParameter getTaskParameterById(Integer id) {
        return taskParameterRepository.get(id);
    }

    @Override
    public TaskParameter addTaskParameter(TaskParameter taskParameter) {
        return taskParameterRepository.create(taskParameter);
    }

    @Override
    public void updateTaskParameter(TaskParameter taskParameter) {
        taskParameterRepository.update(taskParameter);
    }

    @Override
    public void deleteTaskParameter(TaskParameter taskParameter) {
        taskParameterRepository.delete(taskParameter);
    }

    @Override
    public List<TaskParameter> findTaskParameterByNameAndProcess(String name, String authority, String code) {
        return (List<TaskParameter>) taskParameterRepository.findAllByNameAndProcess(name, authority, code);
    }

    @Override
    public void registerQuartzListener(JobListener jobListener) throws ConstellationException {
        try {
            quartzScheduler.getListenerManager().addJobListener(jobListener, allJobs());
        } catch (SchedulerException e) {
            throw new ConstellationException("Unable to attach listener to quartz scheduler.", e);
        }
    }

    private ProcessDescriptor getDescriptor(final String authority, final String code) {
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
        return desc;
    }

    private ParameterValueGroup readTaskParametersFromXML(final TaskParameter taskParameter, final ProcessDescriptor processDesc) {

        //change the description, always encapsulate in the same namespace and name
        //jaxb object factory can not reconize changing names without a namespace
        final ParameterDescriptorGroup idesc = processDesc.getInputDescriptor();
        final GeneralParameterDescriptor retypedDesc =
                new DefaultParameterDescriptorGroup("input", idesc.descriptors().toArray(new GeneralParameterDescriptor[0]));

        final ParameterValueGroup params;
        final ParameterValueReader reader = new ParameterValueReader(retypedDesc);
        try {
            reader.setInput(taskParameter.getInputs());
            params = (ParameterValueGroup) reader.read();
            reader.dispose();
        } catch (XMLStreamException | IOException ex) {
            throw new ConstellationException(ex);
        }
        return params;
    }

    private ParameterValueGroup readTaskParametersFromJSON(final TaskParameter taskParameter, final ProcessDescriptor processDesc)
            throws ConfigurationException {

        final ParameterDescriptorGroup idesc = processDesc.getInputDescriptor();

        ParameterValueGroup params;
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final Map valueMap = mapper.readValue(taskParameter.getInputs(), Map.class);
            params = Parameters.toParameter(valueMap, idesc);
        } catch (IOException e) {
            throw new ConfigurationException("Fail to parse input parameter as JSON for task : "+taskParameter.getId(), e);
        }

        return params;
    }


    private void registerJobInScheduler(String title, Integer taskParameterId, Integer userId, Trigger trigger, ProcessJobDetail detail) {
        final QuartzTask quartzTask = new QuartzTask(UUID.randomUUID().toString());
        quartzTask.setDetail(detail);
        quartzTask.setTitle(title);
        quartzTask.setTrigger(trigger);
        quartzTask.setTaskParameterId(taskParameterId);
        quartzTask.setUserId(userId);

        registerTaskInScheduler(quartzTask);
    }

    /**
     * Read TaskParameter process description and inputs to create a ProcessJobDetail for quartz scheduler.
     *
     * @param task TaskParameter
     * @param createProcess flag that specified if the process is instantiated in ProcessJobDetails or
     *                      ProcessJobDetails create it-self a new instance each time is executed.
     * @return ProcessJobDetails
     */
    private ProcessJobDetail createJobDetailFromTaskParameter(final TaskParameter task, final boolean createProcess)
            throws ConfigurationException {

        final ProcessDescriptor processDesc = getDescriptor(task.getProcessAuthority(), task.getProcessCode());
        final ParameterValueGroup params = readTaskParametersFromJSON(task, processDesc);

        if (createProcess) {
            final ParameterDescriptorGroup originalDesc = processDesc.getInputDescriptor();
            final ParameterValueGroup orig = originalDesc.createValue();
            ParametersExt.deepCopy(params, orig);
            final Process process = processDesc.createProcess(orig);
            return new ProcessJobDetail(process);
        } else {
            return new ProcessJobDetail(task.getProcessAuthority(), task.getProcessCode(), params);
        }
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
        LOGGER.info("Scheduler task added : "+quartzTask.getId()+", "+quartzTask.getTitle()
                        +"   type : "+quartzTask.getDetail().getFactoryIdentifier()+"."+quartzTask.getDetail().getProcessIdentifier());
    }

    /**
     * unregister the given task in the scheduler.
     */
    private void unregisterTaskInScheduler(final JobKey key) throws SchedulerException{
        quartzScheduler.interrupt(key);
        final boolean removed = quartzScheduler.deleteJob(key);

        if(removed){
            LOGGER.info("Scheduler task removed : "+key);
        }else{
            LOGGER.warn("Scheduler failed to remove task : "+key);
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
    public List<org.constellation.engine.register.Task> listRunningTasks(Integer id, Integer offset, Integer limit) {
        return taskRepository.findRunningTasks(id, offset, limit);
    }

    @Override
    public List<org.constellation.engine.register.Task> listTaskHistory(Integer id, Integer offset, Integer limit) {
        return taskRepository.taskHistory(id, offset, limit);
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
    public void runProcess(final String title, final Process process, final Integer taskParameterId, final Integer userId)
            throws ConstellationException {

        final TriggerBuilder tb = TriggerBuilder.newTrigger();
        final Trigger trigger = tb.startNow().build();
        final ProcessJobDetail detail = new ProcessJobDetail(process);

        registerJobInScheduler(title, taskParameterId, userId, trigger, detail);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeTaskParameter (final TaskParameter taskParameter, String title, final Integer userId)
            throws ConstellationException, ConfigurationException {

        final TriggerBuilder tb = TriggerBuilder.newTrigger();
        final Trigger trigger = tb.startNow().build();
        final ProcessJobDetail jobDetail = createJobDetailFromTaskParameter(taskParameter, true);

        if (title == null) {
            title = taskParameter.getName()+TASK_DATE.format(new Date());
        }

        registerJobInScheduler(title, taskParameter.getId(), userId, trigger, jobDetail);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testTaskParameter(TaskParameter taskParameter) throws ConfigurationException {

        if (taskParameter.getInputs() != null && !taskParameter.getInputs().isEmpty()) {
            final ProcessDescriptor processDesc = getDescriptor(taskParameter.getProcessAuthority(), taskParameter.getProcessCode());
            readTaskParametersFromJSON(taskParameter, processDesc);
        } else {
            throw new ConfigurationException("No input for task : " + taskParameter.getName());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void scheduleTaskParameter (final TaskParameter task, final String title, final Integer userId, boolean checkEndDate)
            throws ConstellationException {

        // Stop previous scheduling first.
        if (scheduledTasks.containsKey(task.getId())) {
            try {
                stopScheduleTaskParameter(task, userId);
            } catch (ConfigurationException e) {
                throw new ConstellationException("Unable to re-schedule task.", e);
            }
        }

        String trigger = task.getTrigger();
        if (task.getTriggerType() != null && trigger != null && !trigger.isEmpty()) {

            if ("CRON".equalsIgnoreCase(task.getTriggerType())) {

                try {
                    String cronExp = null;
                    Date endDate = null;
                    if (trigger.contains("{")) {
                        ObjectMapper jsonMapper = new ObjectMapper();
                        jsonMapper.configure(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS, true);

                        Map map = jsonMapper.readValue(trigger, Map.class);

                        cronExp = (String) map.get("cron");
                        long endDateMs =  ((BigInteger)map.get("endDate")).longValue();
                        if (endDateMs > 0) {
                            endDate = new Date(endDateMs);
                        }
                    } else {
                        cronExp = trigger;
                    }

                    if (cronExp == null) {
                        throw new ConstellationException("Invalid cron expression. Can't be empty.");
                    }

                    if (endDate != null && endDate.before(new Date())) {
                        String message = "Task " + task.getName() + " can't be scheduled : end date in the past.";
                        if (checkEndDate) {
                            throw new ConstellationException(message);
                        } else {
                            LOGGER.info(message);
                            return;
                        }
                    }

                    // HACK for Quartz to prevent ParseException :
                    // "Support for specifying both a day-of-week AND a day-of-month parameter is not implemented."
                    // in this case replace the last '*' by '?'
                    if (cronExp.matches("([0-9]\\d{0,1}|\\*) ([0-9]\\d{0,1}|\\*) ([0-9]\\d{0,1}|\\*) \\* ([0-9]\\d{0,1}|\\*) \\*")) {
                        cronExp = cronExp.substring(0, cronExp.length()-1)+ "?";
                    }

                    final ProcessJobDetail jobDetail = createJobDetailFromTaskParameter(task, false);
                    final JobKey key = jobDetail.getKey();

                    final TriggerBuilder tb = TriggerBuilder.newTrigger();
                    final CronScheduleBuilder cronSchedule = CronScheduleBuilder.cronSchedule(cronExp);
                    final Trigger cronTrigger;
                    if (endDate != null) {
                        cronTrigger = tb.withSchedule(cronSchedule).forJob(key).endAt(endDate).build();
                    } else {
                        cronTrigger = tb.withSchedule(cronSchedule).forJob(key).build();
                    }

                    registerJobInScheduler(task.getName(), task.getId(), userId, cronTrigger, jobDetail);
                    scheduledTasks.put(task.getId(), key);

                } catch (ParseException | ConfigurationException | IOException e) {
                    throw new ConstellationException(e.getMessage(), e);
                }

            } else if ("FOLDER".equalsIgnoreCase(task.getTriggerType())) {
                try {
                    File folder = new File(trigger);
                    if (folder.exists() && folder.isDirectory()) {
                        final Path path = folder.toPath();
                        directoryWatcher.register(path);
                        scheduledTasks.put(task.getId(), path);
                    } else {
                        throw new ConstellationException("Invalid folder trigger : " + trigger);
                    }
                } catch (IOException e) {
                    throw new ConstellationException(e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void stopScheduleTaskParameter(final TaskParameter task, final Integer userId)
            throws ConstellationException, ConfigurationException {

        if (!scheduledTasks.containsKey(task.getId())) {
            throw new ConstellationException("Task "+task.getName()+" wasn't scheduled.");
        }

        final Object obj = scheduledTasks.get(task.getId());

        //scheduled task
        if (obj instanceof JobKey) {
            try {
                unregisterTaskInScheduler((JobKey) obj);
                scheduledTasks.remove(task.getId());
            } catch (SchedulerException e) {
                throw new ConstellationException(e.getMessage(), e);
            }

        } else if (obj instanceof Path) {
            //directory watched task
            directoryWatcher.unregister((Path) obj);
            scheduledTasks.remove(task.getId());
        } else {
            throw new ConstellationException("Unable to stop scheduled task " + task.getName());
        }
    }

    @PreDestroy
    public void stop() {
        LOGGER.info("=== Stopping Scheduler ===");
        try {
            LOGGER.info("=== Wait for job to stop ===");
            quartzScheduler.shutdown(false);
            quartzScheduler = null;
        } catch (SchedulerException ex) {
            LOGGER.error("=== Failed to stop quartz scheduler ===", ex);
        }
        LOGGER.info("=== Scheduler successfully stopped ===");

        LOGGER.info("=== Stopping directory watcher ===");
        try {
            directoryWatcher.close();
        } catch (IOException ex) {
            LOGGER.error("=== Failed to stop directory watcher ===", ex);
        }
        LOGGER.info("=== Directory watcher successfully stopped ===");
        cleanTasksStates();
    }

    /**
     * Clear remaining running tasks before server shutdown or after server startup
     */
    private void cleanTasksStates() {
        List<Task> runningTasks = taskRepository.findRunningTasks();

        if (!runningTasks.isEmpty()) {
            LOGGER.info("=== Clear remaining running tasks ===");
        }

        long now = System.currentTimeMillis();
        String msg = "Stopped by server shutdown";
        for (Task runningTask : runningTasks) {
            if (runningTask.getEnd() == null) {
                runningTask.setEnd(now);
                runningTask.setState(TaskState.CANCELLED.name());
                runningTask.setMessage(msg);
                taskRepository.update(runningTask);
            }
        }
    }
}
