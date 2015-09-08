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
package org.constellation.rest.api;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.constellation.admin.dto.ServiceDTO;
import org.constellation.admin.dto.TaskStatusDTO;
import org.constellation.admin.exception.ConstellationException;
import org.constellation.business.IDatasetBusiness;
import org.constellation.business.IProcessBusiness;
import org.constellation.business.IServiceBusiness;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.StringList;
import org.constellation.configuration.StringMap;
import org.constellation.database.api.TaskParameterWithOwnerName;
import org.constellation.database.api.jooq.tables.pojos.CstlUser;
import org.constellation.database.api.jooq.tables.pojos.Dataset;
import org.constellation.database.api.jooq.tables.pojos.Style;
import org.constellation.database.api.jooq.tables.pojos.Task;
import org.constellation.database.api.jooq.tables.pojos.TaskParameter;
import org.constellation.database.api.repository.StyleRepository;
import org.constellation.database.api.repository.TaskParameterRepository;
import org.constellation.database.api.repository.UserRepository;
import org.constellation.process.DatasetProcessReference;
import org.constellation.process.ServiceProcessReference;
import org.constellation.process.StyleProcessReference;
import org.constellation.util.ParamUtilities;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessFinder;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.util.NoSuchIdentifierException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Optional;
import org.constellation.process.UserProcessReference;
import org.geotoolkit.processing.chain.model.Chain;

/**
 * RestFull API for task management/operations.
 * 
 * @author Johann Sorel (Geomatys)
 */
@Component
@Path("/1/task")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class TaskRest {
    private static final DateFormat TASK_DATE = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    @Inject
    private IProcessBusiness processBusiness;

    @Inject
    private TaskParameterRepository taskParameterRepository;

    @Inject
    private UserRepository userRepository;

    /**
     * DatasetBusiness used for provider GUI editors data
     */
    @Inject
    private IDatasetBusiness datasetBusiness;

    /**
     * ServiceBusiness used for provider GUI editors data
     */
    @Inject
    private IServiceBusiness serviceBusiness;

    /**
     * StyleRepository used for provider GUI editors data
     */
    @Inject
    private StyleRepository styleRepository;
    
    // <editor-fold defaultstate="collapsed" desc="GeotkProcessAPI">
    /**
     * Returns a list of all process available in the current factories.
     */
    @GET
    @Path("listProcesses")
    public Response listProcess(){

        final Map<String, Set<String>> registryMap = processBusiness.listProcess();

        final List<String> result = new ArrayList<>();

        for (Map.Entry<String, Set<String>> registry : registryMap.entrySet()) {
            final Set<String> processes = registry.getValue();
            for (String process : processes) {
                result.add(registry.getKey() +":"+ process);
            }
        }

        final Map<String, List<String>> wrapper = Collections.singletonMap("list", result);
        return Response.ok(wrapper).build();
    }

    /**
     * Returns a list of all process available in the current factories.
     */
    @GET
    @Path("countProcesses")
    public Response countAvailableProcess(){
        final Map<String, Set<String>> registryMap = processBusiness.listProcess();
        int count = 0;
        for (Set<String> strings : registryMap.values()) {
            count += strings.size();
        }

        StringMap stringMap = new StringMap();
        stringMap.getMap().put("value",""+count);
        return Response.ok(stringMap).build();
    }
    
    /**
     * Returns a list of all process available for the specified factory.
     * 
     * @param authorityCode
     */
    @GET
    @Path("process/factory/{authorityCode}")
    public Response listProcessForFactory(final @PathParam("authorityCode") String authorityCode) {
        return Response.ok(new StringList(processBusiness.listProcessForFactory(authorityCode))).build();
    }
    
    /**
     * Returns a list of all process available in the current factories.
     */
    @Path("listProcessFactories")
    public Response listProcessFactories(){
        final List<String> names = processBusiness.listProcessFactory();
        return Response.ok(new StringList(names)).build();
    }
    
    /**
     * Returns a description of the process parameters.
     */
    @GET
    @Path("process/descriptor/{authority}/{code}")
    public Response getProcessDescriptor(final @PathParam("authority") String authority, final @PathParam("code") String code)
            throws ConfigurationException {

        final ParameterDescriptorGroup idesc;
        try {
            idesc = getDescriptor(authority, code);
        } catch (ConfigurationException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new AcknowlegementType("Failure", "Could not find process description for given authority/code.")).build();
        }

        try {
            final String jsonString = ParamUtilities.writeParameterDescriptorJSON(idesc);
            return Response.ok(jsonString, MediaType.APPLICATION_JSON_TYPE).build();
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new AcknowlegementType("Failure", "Could not find chain for given authority/code.")).build();
        }
    }

    private ParameterDescriptorGroup getDescriptor(final String authority, final String code) throws ConfigurationException {
        final ProcessDescriptor desc;
        try {
            desc = ProcessFinder.getProcessDescriptor(authority,code);
        } catch (NoSuchIdentifierException ex) {
            throw new ConfigurationException("No Process for id : {" + authority + "}"+code+" has been found");
        } catch (InvalidParameterValueException ex) {
            throw new ConfigurationException(ex);
        }
        if(desc == null){
            throw new ConfigurationException("No Process for id : {" + authority + "}"+code+" has been found");
        }

        //change the description, always encapsulate in the same namespace and name
        //jaxb object factory can not reconize changing names without a namespace
        ParameterDescriptorGroup idesc = desc.getInputDescriptor();
        idesc = new DefaultParameterDescriptorGroup("input", idesc.descriptors().toArray(new GeneralParameterDescriptor[0]));
        return idesc;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ChainAPI">
    @POST
    @Path("chain")
    public Response createChain(final Chain chain) throws ConfigurationException {
        processBusiness.createChainProcess(chain);
        return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
    }
    
    @DELETE
    @Path("chain/{authority}/{code}")
    public Response deleteChain(final @PathParam("authority") String authority, final @PathParam("code") String code) {
        if (processBusiness.deleteChainProcess(authority, code)) {
            return Response.ok(new AcknowlegementType("Success", "The chain has been deleted")).build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new AcknowlegementType("Failure", "Could not find chain for given authority/code.")).build();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="TaskParameterAPI">
    @POST
    @Path("params/create")
    public Response createParamsTask(final TaskParameter taskParameter, @Context HttpServletRequest req) throws ConfigurationException {
        final Optional<CstlUser> cstlUser = userRepository.findOne(req.getUserPrincipal().getName());

        try {
            processBusiness.testTaskParameter(taskParameter);
            if (cstlUser.isPresent()) {
                taskParameter.setOwner(cstlUser.get().getId());
                taskParameter.setDate(System.currentTimeMillis());
                processBusiness.addTaskParameter(taskParameter);
                return Response.ok().status(Response.Status.CREATED).type(MediaType.TEXT_PLAIN_TYPE).build();
            } else {
                return Response.status(Response.Status.EXPECTATION_FAILED).build();
            }
        } catch (ConfigurationException ex) {
            final AcknowlegementType failure = new AcknowlegementType("Failure", "Failed to create task : " + ex.getMessage());
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(failure).build();
        }
    }

    @POST
    @Path("params/update")
    public Response updateParamsTask(final TaskParameter taskParameter) throws ConfigurationException {
        try {
            processBusiness.testTaskParameter(taskParameter);
            processBusiness.updateTaskParameter(taskParameter);
            return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
        } catch (ConfigurationException ex) {
            final AcknowlegementType failure = new AcknowlegementType("Failure", "Failed to create task : " + ex.getMessage());
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(failure).build();
        }
    }

    @GET
    @Path("params/get/{id}")
    public Response getParamsTask(final @PathParam("id") Integer id) {
        final TaskParameter taskParameter = processBusiness.getTaskParameterById(id);
        if (taskParameter != null) {
            return Response.ok(taskParameter).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @GET
    @Path("params/delete/{id}")
    public Response deleteParamsTask(final @PathParam("id") Integer id) {
        final TaskParameter taskParameter = processBusiness.getTaskParameterById(id);
        if (taskParameter != null) {
            processBusiness.deleteTaskParameter(taskParameter);
            return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @GET
    @Path("params/duplicate/{id}")
    @Transactional
    public Response duplicateParamsTask(final @PathParam("id") Integer taskParameterIdForTemplate, @Context HttpServletRequest req) throws ConfigurationException {
        final Optional<CstlUser> cstlUser = userRepository.findOne(req.getUserPrincipal().getName());

        if (cstlUser.isPresent()) {
            final TaskParameter taskParameter = processBusiness.getTaskParameterById(taskParameterIdForTemplate);
            taskParameter.setId(null);
            taskParameter.setName(taskParameter.getName()+"(COPY)");
            taskParameter.setOwner(cstlUser.get().getId());
            taskParameterRepository.create(taskParameter);
            return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
        } else {
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }
    }

    @GET
    @Path("params/execute/{id}")
    public Response executeParamsTask(final @PathParam("id") Integer id, @Context HttpServletRequest req)
            throws ConfigurationException {

        final Optional<CstlUser> cstlUser = userRepository.findOne(req.getUserPrincipal().getName());

        if (!cstlUser.isPresent()) {
            throw new ConstellationException("operation not allowed without login");
        }
        final TaskParameter taskParameter = processBusiness.getTaskParameterById(id);
        final String title = taskParameter.getName()+" "+TASK_DATE.format(new Date());

        try {
            processBusiness.executeTaskParameter(taskParameter, title, cstlUser.get().getId());
        } catch (ConstellationException | ConfigurationException ex) {
            final AcknowlegementType failure = new AcknowlegementType("Failure", "Failed to run task : " + ex.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(failure).build();
        }

        return Response.ok("The task has been created").build();
    }

    @GET
    @Path("params/schedule/start/{id}")
    public Response startScheduleParamsTask(final @PathParam("id") Integer id, @Context HttpServletRequest req) throws ConfigurationException {
        final Optional<CstlUser> cstlUser = userRepository.findOne(req.getUserPrincipal().getName());

        if (!cstlUser.isPresent()) {
            throw new ConstellationException("operation not allowed without login");
        }
        final Date now = new Date();
        final TaskParameter taskParameter = processBusiness.getTaskParameterById(id);
        final String title = taskParameter.getName()+" "+TASK_DATE.format(now);

        try {
            processBusiness.scheduleTaskParameter(taskParameter, title, cstlUser.get().getId(), true);
        } catch (ConstellationException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new AcknowlegementType("Failure", "Failed to schedule task : "+ex.getMessage())).build();
        }
        return Response.ok(new AcknowlegementType("Success", "The task has been schedule")).build();
    }

    @GET
    @Path("params/schedule/stop/{id}")
    public Response stopScheduleParamsTask(final @PathParam("id") Integer id, @Context HttpServletRequest req) throws ConfigurationException {
        final Optional<CstlUser> cstlUser = userRepository.findOne(req.getUserPrincipal().getName());

        if (!cstlUser.isPresent()) {
            throw new ConstellationException("operation not allowed without login");
        }
        final TaskParameter taskParameter = processBusiness.getTaskParameterById(id);

        try {
            processBusiness.stopScheduleTaskParameter(taskParameter, cstlUser.get().getId());
        } catch (ConstellationException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new AcknowlegementType("Failure", "Failed to un-schedule  task: "+ex.getMessage())).build();
        }

        return Response.ok(new AcknowlegementType("Success", "The task has been un-schedule")).build();
    }

    @GET
    @Path("params/list")
    public Response listParamsTask() throws ConfigurationException {

        final List<? extends TaskParameter> all = taskParameterRepository.findAll();
        final List<TaskParameterWithOwnerName> ltpwon = convertTaskParameter(all);
        return Response.ok(ltpwon).build();
    }

    @GET
    @Path("params/list/{type}")
    public Response listParamsTaskByType(final @PathParam("type") String type) throws ConfigurationException {

        final List<? extends TaskParameter> all = taskParameterRepository.findAllByType(type);
        final List<TaskParameterWithOwnerName> ltpwon = convertTaskParameter(all);
        return Response.ok(ltpwon).build();
    }

    private List<TaskParameterWithOwnerName> convertTaskParameter(List<? extends TaskParameter> all) {
        final List<TaskParameterWithOwnerName> ltpwon = new ArrayList<>();
        for (TaskParameter tp : all){
            TaskParameterWithOwnerName tpwon = new TaskParameterWithOwnerName();

            tpwon.setId(tp.getId());
            tpwon.setName(tp.getName());
            tpwon.setDate(tp.getDate());
            tpwon.setOwner(tp.getOwner());
            tpwon.setProcessAuthority(tp.getProcessAuthority());
            tpwon.setProcessCode(tp.getProcessCode());
            tpwon.setInputs(tp.getInputs());
            tpwon.setType(tp.getType());

            final Optional<CstlUser> byId = userRepository.findById(tp.getOwner());
            if (byId.isPresent()) {
                tpwon.setOwnerName(byId.get().getFirstname()+" "+byId.get().getLastname());
            }

            ltpwon.add(tpwon);
        }
        return ltpwon;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="EditorsDataProvidersAPI">
    /**
     * List all Datasets as DatasetProcessReference to provider GUI editors.
     * @return list of DatasetProcessReference
     */
    @GET
    @Path("/list/datasetRef")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getDatasetProcessReferenceList() {
        final List<DatasetProcessReference> datasetPRef = new ArrayList<>();
        final List<Dataset> datasets = datasetBusiness.getAllDataset();
        if(datasets!=null){
            for(final Dataset ds : datasets){
                final DatasetProcessReference ref = new DatasetProcessReference();
                ref.setId(ds.getId());
                ref.setIdentifier(ds.getIdentifier());
                datasetPRef.add(ref);
            }
        }
        return Response.ok(datasetPRef).build();
    }

    /**
     * List all Services as ServiceProcessReference to provider GUI editors.
     * @param domainId
     * @return list of ServiceProcessReference
     */
    @GET
    @Path("/list/serviceRef/domain/{domainId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getServiceProcessReferenceList(@PathParam("domainId") Integer domainId) throws ConfigurationException {
        final List<ServiceProcessReference> servicePRef = new ArrayList<>();
        final List<ServiceDTO> services = serviceBusiness.getAllServices(null);
        if(services!=null){
            for(final ServiceDTO service : services){
                final ServiceProcessReference ref = new ServiceProcessReference();
                ref.setId(service.getId());
                ref.setType(service.getType());
                ref.setName(service.getTitle());
                servicePRef.add(ref);
            }
        }
        return Response.ok(servicePRef).build();
    }

    /**
     * List all Style as StyleProcessReference to provider GUI editors.
     * @return list of ServiceProcessReference
     */
    @GET
    @Path("/list/styleRef")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getStyleProcessReferenceList() throws ConfigurationException {
        final List<StyleProcessReference> servicePRef = new ArrayList<>();
        final List<Style> styles = styleRepository.findAll();
        if(styles!=null){
            for(final Style style : styles){
                final StyleProcessReference ref = new StyleProcessReference();
                ref.setId(style.getId());
                ref.setType(style.getType());
                ref.setName(style.getName());
                ref.setProvider(style.getProvider());
                servicePRef.add(ref);
            }
        }
        return Response.ok(servicePRef).build();
    }
    
    /**
     * List all User as UserProcessReference to provider GUI editors.
     * @return list of UserProcessReference
     * @throws org.constellation.configuration.ConfigurationException
     */
    @GET
    @Path("/list/userRef")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getUserProcessReferenceList() throws ConfigurationException {
        final List<UserProcessReference> userPRef = new ArrayList<>();
        final List<CstlUser> users = userRepository.findAll();
        if(users!=null){
            for(final CstlUser user : users){
                final UserProcessReference ref = new UserProcessReference();
                ref.setId(user.getId());
                ref.setIdentifier(user.getLogin());
                userPRef.add(ref);
            }
        }
        return Response.ok(userPRef).build();
    }
    // </editor-fold>

    /**
     * List running tasks.
     */
    @GET
    @Path("listTasks")
    public Response listTasks() {
        final List<Task> tasks = processBusiness.listRunningTasks();
        Map<Integer, List<TaskStatusDTO>> map = new HashMap<>();

        for (Task task : tasks) {
            Integer taskParameterId = task.getTaskParameterId();

            if (!map.containsKey(taskParameterId)) {
                map.put(taskParameterId, new ArrayList<TaskStatusDTO>());
            }
            map.get(taskParameterId).add(toTaskStatus(task));
        }

        return Response.ok(map).build();
    }

    /**
     * List running tasks.
     */
    @GET
    @Path("listRunningTasks/{id}/{limit}")
    public Response listRunningTaskForTaskParameter(final @PathParam("id") Integer id, final @PathParam("limit") Integer limit) {
        final List<Task> tasks = processBusiness.listRunningTasks(id, 0, limit);

        List<TaskStatusDTO> lst = new ArrayList<>();
        for(Task task : tasks) {
            lst.add(toTaskStatus(task));
        }
        return Response.ok(lst).build();
    }

    /**
     * List running tasks.
     */
    @GET
    @Path("taskHistory/{id}/{limit}")
    public Response listHistoryForTaskParameter(final @PathParam("id") Integer id, final @PathParam("limit") Integer limit) {
        final List<Task> tasks = processBusiness.listTaskHistory(id, 0, limit);

        List<TaskStatusDTO> lst = new ArrayList<>();
        for(Task task : tasks) {
            lst.add(toTaskStatus(task));
        }
        return Response.ok(lst).build();
    }

    private TaskStatusDTO toTaskStatus(Task task) {
        final TaskStatusDTO status = new TaskStatusDTO();
        status.setId(task.getIdentifier());
        status.setTaskId(task.getTaskParameterId());
        status.setMessage(task.getMessage());
        status.setPercent(task.getProgress() != null ? task.getProgress().floatValue() : 0f);
        status.setStatus(task.getState());
        status.setStart(task.getDateStart());
        status.setEnd(task.getDateEnd());
        status.setOutput(task.getTaskOutput());

        final TaskParameter taskParameter = processBusiness.getTaskParameterById(task.getTaskParameterId());
        status.setTitle(taskParameter.getName());
        return status;
    }
}
