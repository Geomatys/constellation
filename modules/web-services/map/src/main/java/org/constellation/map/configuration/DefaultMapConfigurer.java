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

package org.constellation.map.configuration;

import java.io.InputStream;
import java.util.List;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.constellation.provider.*;
import org.quartz.TriggerBuilder;
import org.quartz.SimpleScheduleBuilder;

import org.constellation.configuration.AbstractConfigurer;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.StringList;
import org.constellation.configuration.StringTreeNode;
import org.constellation.provider.configuration.ProviderParameters;
import org.constellation.scheduler.CstlScheduler;
import org.constellation.scheduler.Task;
import org.constellation.ws.CstlServiceException;

import org.geotoolkit.feature.type.DefaultName;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.process.quartz.ProcessJobDetail;
import org.geotoolkit.sld.xml.Specification.SymbologyEncoding;
import org.geotoolkit.sld.xml.StyleXmlIO;
import org.geotoolkit.style.MutableStyle;
import org.apache.sis.util.Classes;
import org.geotoolkit.xml.parameter.ParameterValueReader;

import org.geotoolkit.feature.type.Name;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.FactoryException;
import org.opengis.util.NoSuchIdentifierException;

import static org.constellation.ws.ExceptionCode.*;
import static org.constellation.api.QueryConstants.*;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.process.provider.style.SetStyleToStyleProviderDescriptor;
import org.constellation.process.provider.style.DeleteStyleToStyleProviderDescriptor;
import org.constellation.configuration.ConfigurationException;
import org.constellation.scheduler.Tasks;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.InvalidParameterValueException;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @author Johann Sorel (Geomatys)
 */
public class DefaultMapConfigurer extends AbstractConfigurer {

    private final Map<String, ProviderFactory> services = new HashMap<>();

    private ProviderOperationListener providerListener;
    
    public DefaultMapConfigurer() {
        this(new DefaultProviderOperationListener());
    }
    
    public DefaultMapConfigurer(final ProviderOperationListener providerListener) {
        final Collection<DataProviderFactory> availableLayerServices = DataProviders.getInstance().getFactories();
        for (DataProviderFactory service: availableLayerServices) {
            this.services.put(service.getName(), service);
        }
        final Collection<StyleProviderFactory> availableStyleServices = StyleProviders.getInstance().getFactories();
        for (StyleProviderFactory service: availableStyleServices) {
            this.services.put(service.getName(), service);
        }
        this.providerListener = providerListener;
    }

    @Override
    public boolean needCustomUnmarshall(final String request, MultivaluedMap<String, String> parameters) {

        if ( REQUEST_CREATE_STYLE.equalsIgnoreCase(request)
          || REQUEST_UPDATE_STYLE.equalsIgnoreCase(request)) {
            return true;
        }

        return super.needCustomUnmarshall(request,parameters);
    }

    @Override
    public Object unmarshall(final String request, final MultivaluedMap<String, String> parameters,
            final InputStream stream) throws JAXBException, CstlServiceException {

        if ( REQUEST_CREATE_STYLE.equalsIgnoreCase(request)
          || REQUEST_UPDATE_STYLE.equalsIgnoreCase(request)) {

            final StyleXmlIO util = new StyleXmlIO();
            try {
                return util.readStyle(stream, SymbologyEncoding.V_1_1_0);
            } catch (FactoryException ex) {
                throw new JAXBException(ex.getMessage(),ex);
            }
        }

        return super.unmarshall(request, parameters, stream);
    }

    @Override
    public Object treatRequest(final String request, final MultivaluedMap<String, String> parameters, final Object objectRequest) throws CstlServiceException {

        //Layer operations
        if (REQUEST_CREATE_LAYER.equalsIgnoreCase(request)) {
            final String sourceId = getParameter("id", true, parameters);
            return createLayer(sourceId, objectRequest);
        } else if (REQUEST_UPDATE_LAYER.equalsIgnoreCase(request)) {
            final String sourceId = getParameter("id", true, parameters);
            final String layerName = getParameter("layerName", true, parameters);
            return updateLayer(sourceId, layerName, objectRequest);
        } else if (REQUEST_DELETE_LAYER.equalsIgnoreCase(request)) {
            final String sourceId = getParameter("id", true, parameters);
            final String layerName = getParameter("layerName", true, parameters);
            return deleteLayer(sourceId, layerName);
        }

        //Style operations
        else if (REQUEST_DOWNLOAD_STYLE.equalsIgnoreCase(request)) {
            final String id = getParameter("id", true, parameters);
            final String styleId = getParameter("styleName", true, parameters);
            return downloadStyle(id, styleId);
        } else if (REQUEST_CREATE_STYLE.equalsIgnoreCase(request)) {
            final String sourceId = getParameter("id", true, parameters);
            final String styleId = getParameter("styleName", true, parameters);
            return createStyle(sourceId, styleId, objectRequest);
        } else if (REQUEST_UPDATE_STYLE.equalsIgnoreCase(request)) {
            final String sourceId = getParameter("id", true, parameters);
            final String styleId = getParameter("styleName", true, parameters);
            return updateStyle(sourceId, styleId, objectRequest);
        } else if (REQUEST_DELETE_STYLE.equalsIgnoreCase(request)) {
            final String sourceId = getParameter("id", true, parameters);
            final String styleId = getParameter("styleName", true, parameters);
            return deleteStyle(sourceId, styleId);
        }

        //Tasks operations
        else if (REQUEST_LIST_PROCESS.equalsIgnoreCase(request)) {
            return listProcess();
        } else if (REQUEST_LIST_PROCESS_FOR_FACTO.equalsIgnoreCase(request)) {
            final String authorityCode = getParameter("authorityCode", true, parameters);
            return listProcessForFactory(authorityCode);
        } else if (REQUEST_LIST_PROCESS_FACTORIES.equalsIgnoreCase(request)) {
            return listProcessFactories();
        } else if (REQUEST_LIST_TASKS.equalsIgnoreCase(request)) {
            return listTasks();
        } else if (REQUEST_GET_PROCESS_DESC.equalsIgnoreCase(request)) {
            final String authority = getParameter("authority", true, parameters);
            final String code = getParameter("code", true, parameters);
            return getProcessDescriptor(authority, code);
        } else if (REQUEST_GET_TASK_PARAMS.equalsIgnoreCase(request)) {
            final String id = getParameter("id", true, parameters);
            return getTaskParameters(id);
        } else if (REQUEST_CREATE_TASK.equalsIgnoreCase(request)) {
            final String authority = getParameter("authority", true, parameters);
            final String code = getParameter("code", true, parameters);
            String title = getParameter("title", false, parameters);
            final int step = Integer.valueOf(getParameter("step", true, parameters));
            final String id = getParameter("id", true, parameters);
            return createTask(authority, code, title, step, id, objectRequest);
        } else if (REQUEST_UPDATE_TASK.equalsIgnoreCase(request)) {
            final String authority = getParameter("authority", true, parameters);
            final String code      = getParameter("code", true, parameters);
            final String title     = getParameter("title", false, parameters);
            final int step         = Integer.valueOf(getParameter("step", true, parameters));
            final String id        = getParameter("id", true, parameters);
            return updateTask(authority, code, title, step, id, objectRequest);
        } else if (REQUEST_DELETE_TASK.equalsIgnoreCase(request)) {
            final String id = getParameter("id", true, parameters);
            return deleteTask(id);
        }

        return null;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void beforeRestart() {
        StyleProviders.getInstance().dispose();
        DataProviders.getInstance().dispose();
    }

    /**
     * Add a layer to the specified provider.
     *
     * @param parameters The GET KVP parameters send in the request.
     * @param objectRequest The POST parameters send in the request.
     *
     * @return An acknowledgment informing if the request have been successfully treated or not.
     * @throws CstlServiceException
     */
    private AcknowlegementType createLayer(final String sourceId,
            final Object objectRequest) throws CstlServiceException{

        final ParameterValueReader reader = new ParameterValueReader(ProviderParameters.LAYER_DESCRIPTOR);

        try {
            // we read the soruce parameter to add
            reader.setInput(objectRequest);
            final ParameterValueGroup newLayer = (ParameterValueGroup) reader.read();
            reader.dispose();

            final Collection<DataProvider> providers = DataProviders.getInstance().getProviders();
            for (DataProvider p : providers) {
                if (p.getId().equals(sourceId)) {
                    p.getSource().values().add(newLayer);
                    p.updateSource(p.getSource());
                    return new AcknowlegementType("Success", "The layer has been added");
                }
            }
            return new AcknowlegementType("Failure", "Unable to find a source named:" + sourceId);


        } catch (XMLStreamException | IOException ex) {
            throw new CstlServiceException(ex);
        }

//        try {
//            // we read the soruce parameter to add
//            reader.setInput(objectRequest);
//            final ParameterValueGroup newLayer = (ParameterValueGroup) reader.read();
//
//
//            final ProcessDescriptor procDesc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, CreateProviderLayerStyleDescriptor.NAME);
//            final ParameterValueGroup inputs = procDesc.getInputDescriptor().createValue();
//            inputs.parameter(CreateProviderLayerStyleDescriptor.PROVIDER_ID_NAME).setValue(sourceId);
//            inputs.parameter(CreateProviderLayerStyleDescriptor.LAYER_NAME).setValue(newLayer);
//
//            try {
//                final org.geotoolkit.process.Process process = procDesc.createProcess(inputs);
//                process.call();
//
//            } catch (ProcessException ex) {
//                return new AcknowlegementType("Failure", ex.getLocalizedMessage());
//            }
//
//            reader.dispose();
//            return new AcknowlegementType("Success", "The layer has been added");
//
//        } catch (NoSuchIdentifierException ex) {
//           throw new CstlServiceException(ex);
//        } catch (XMLStreamException ex) {
//            throw new CstlServiceException(ex);
//        } catch (IOException ex) {
//            throw new CstlServiceException(ex);
//        }
    }

    /**
     * Remove a layer in the specified provider.
     *
     * @param parameters The GET KVP parameters send in the request.
     *
     * @return An acknowledgment informing if the request have been successfully treated or not.
     * @throws CstlServiceException
     */
    private AcknowlegementType deleteLayer(final String sourceId, final String layerName) throws CstlServiceException{
        final Collection<DataProvider> providers = DataProviders.getInstance().getProviders();
        for (DataProvider p : providers) {
            if (p.getId().equals(sourceId)) {
                for (GeneralParameterValue param : p.getSource().values()) {
                    if (param instanceof ParameterValueGroup) {
                        final ParameterValueGroup pvg = (ParameterValueGroup)param;
                        if (param.getDescriptor().equals(ProviderParameters.LAYER_DESCRIPTOR)) {
                            final ParameterValue value = pvg.parameter("name");
                            if (value.stringValue().equals(layerName)) {
                                p.getSource().values().remove(pvg);
                                break;
                            }
                        }
                    }
                }
                p.updateSource(p.getSource());
                return new AcknowlegementType("Success", "The layer has been removed");
            }
        }
        return new AcknowlegementType("Failure", "Unable to find a source named:" + sourceId);
//        try {
//
//            final ProcessDescriptor procDesc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, DeleteProviderLayerStyleDescriptor.NAME);
//            final ParameterValueGroup inputs = procDesc.getInputDescriptor().createValue();
//            inputs.parameter(DeleteProviderLayerStyleDescriptor.PROVIDER_ID_NAME).setValue(sourceId);
//            inputs.parameter(DeleteProviderLayerStyleDescriptor.LAYER_NAME_NAME).setValue(layerName);
//
//            try {
//                final org.geotoolkit.process.Process process = procDesc.createProcess(inputs);
//                process.call();
//
//            } catch (ProcessException ex) {
//                return new AcknowlegementType("Failure", ex.getLocalizedMessage());
//            }
//
//            return new AcknowlegementType("Success", "The source has been deleted");
//
//        } catch (NoSuchIdentifierException ex) {
//           throw new CstlServiceException(ex);
//        }
    }

    /**
     * Modify a layer to the specified provider.
     *
     * @param parameters The GET KVP parameters send in the request.
     * @param objectRequest The POST parameters send in the request.
     *
     * @return An acknowledgment informing if the request have been successfully treated or not.
     * @throws CstlServiceException
     */
    private AcknowlegementType updateLayer(final String sourceId, final String layerName,
            final Object objectRequest) throws CstlServiceException{

        final ParameterValueReader reader = new ParameterValueReader(ProviderParameters.LAYER_DESCRIPTOR);

        try {
            // we read the source parameter to add
            reader.setInput(objectRequest);
            ParameterValueGroup newLayer = (ParameterValueGroup) reader.read();
            reader.dispose();

            Collection<DataProvider> providers = DataProviders.getInstance().getProviders();
            for (DataProvider p : providers) {
                if (p.getId().equals(sourceId)) {
                    for (GeneralParameterValue param : p.getSource().values()) {
                        if (param instanceof ParameterValueGroup) {
                            ParameterValueGroup pvg = (ParameterValueGroup)param;
                            if (param.getDescriptor().equals(ProviderParameters.LAYER_DESCRIPTOR)) {
                                ParameterValue value = pvg.parameter("name");
                                if (value.stringValue().equals(layerName)) {
                                    p.getSource().values().remove(pvg);
                                    p.getSource().values().add(newLayer);
                                    break;
                                }
                            }
                        }
                    }
                    p.updateSource(p.getSource());
                    return new AcknowlegementType("Success", "The layer has been modified");
                }
            }
        } catch (XMLStreamException | IOException ex) {
            throw new CstlServiceException(ex);
        }
        return new AcknowlegementType("Failure", "Unable to find a source named:" + sourceId);

//        try {
//            // we read the soruce parameter to add
//            reader.setInput(objectRequest);
//            final ParameterValueGroup newLayer = (ParameterValueGroup) reader.read();
//
//            final ProcessDescriptor procDesc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, UpdateProviderLayerStyleDescriptor.NAME);
//            final ParameterValueGroup inputs = procDesc.getInputDescriptor().createValue();
//            inputs.parameter(UpdateProviderLayerStyleDescriptor.PROVIDER_ID_NAME).setValue(sourceId);
//            inputs.parameter(UpdateProviderLayerStyleDescriptor.LAYER_NAME_NAME).setValue(layerName);
//            inputs.parameter(UpdateProviderLayerStyleDescriptor.UPDATE_LAYER_NAME).setValue(newLayer);
//
//            try {
//                final org.geotoolkit.process.Process process = procDesc.createProcess(inputs);
//                process.call();
//
//            } catch (ProcessException ex) {
//                return new AcknowlegementType("Failure", ex.getLocalizedMessage());
//            }
//
//            reader.dispose();
//            return new AcknowlegementType("Success", "The layer has been modified");
//
//        } catch (NoSuchIdentifierException ex) {
//            throw new CstlServiceException(ex);
//        } catch (XMLStreamException ex) {
//            throw new CstlServiceException(ex);
//        } catch (IOException ex) {
//            throw new CstlServiceException(ex);
//        }
    }

    /**
     * Download a complete style definition.
     *
     * @param parameters
     * @return
     * @throws CstlServiceException
     */
    private Object downloadStyle(final String id, final String styleId) throws CstlServiceException{
        final Collection<StyleProvider> providers = StyleProviders.getInstance().getProviders();
        for (Provider p : providers) {
            if (p.getId().equals(id)) {
                Object style = p.get(styleId);
                if(style != null){
                    return style;
                }
                return new AcknowlegementType("Failure", "Unable to find a style named : " + id);
            }
        }

        return new AcknowlegementType("Failure", "Unable to find a provider named : " + id);
    }

    /**
     * Add a style to the specified provider.
     *
     * @param parameters The GET KVP parameters send in the request.
     * @param objectRequest The POST parameters send in the request.
     *
     * @return An acknowledgment informing if the request have been successfully treated or not.
     * @throws CstlServiceException
     */
    private AcknowlegementType createStyle(final String sourceId, final String styleId,
            final Object objectRequest) throws CstlServiceException{

        if(objectRequest instanceof MutableStyle){
            // we read the style to add
            final MutableStyle style = (MutableStyle) objectRequest;

            try {
                // we read the soruce parameter to add

                final ProcessDescriptor procDesc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, SetStyleToStyleProviderDescriptor.NAME);
                final ParameterValueGroup inputs = procDesc.getInputDescriptor().createValue();
                inputs.parameter(SetStyleToStyleProviderDescriptor.PROVIDER_ID_NAME).setValue(sourceId);
                inputs.parameter(SetStyleToStyleProviderDescriptor.STYLE_ID_NAME).setValue(styleId);
                inputs.parameter(SetStyleToStyleProviderDescriptor.STYLE_NAME).setValue(style);

                try {
                    final org.geotoolkit.process.Process process = procDesc.createProcess(inputs);
                    process.call();

                } catch (ProcessException ex) {
                    return new AcknowlegementType("Failure", ex.getLocalizedMessage());
                }

                return new AcknowlegementType("Success", "The layer has been added");

            } catch (NoSuchIdentifierException ex) {
                throw new CstlServiceException(ex);
            } catch (InvalidParameterValueException ex) {
                throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE);
            }
        }
        return new AcknowlegementType("Failure", "Passed object is not a style:" + Classes.getShortClassName(objectRequest));

    }

    /**
     * Remove a style in the specified provider.
     *
     * @param parameters The GET KVP parameters send in the request.
     *
     * @return An acknowledgment informing if the request have been successfully treated or not.
     * @throws CstlServiceException
     */
    private AcknowlegementType deleteStyle(final String sourceId, final String styleId) throws CstlServiceException{
        try {
            // we read the soruce parameter to add

            final ProcessDescriptor procDesc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, DeleteStyleToStyleProviderDescriptor.NAME);
            final ParameterValueGroup inputs = procDesc.getInputDescriptor().createValue();
            inputs.parameter(DeleteStyleToStyleProviderDescriptor.PROVIDER_ID_NAME).setValue(sourceId);
            inputs.parameter(DeleteStyleToStyleProviderDescriptor.STYLE_ID_NAME).setValue(styleId);

            try {
                final org.geotoolkit.process.Process process = procDesc.createProcess(inputs);
                process.call();

            } catch (ProcessException ex) {
                return new AcknowlegementType("Failure", ex.getLocalizedMessage());
            }

            return new AcknowlegementType("Success", "The layer has been removed");

        } catch (NoSuchIdentifierException ex) {
            throw new CstlServiceException(ex);
        } catch (InvalidParameterValueException ex) {
                throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE);
        }
    }

    /**
     * Modify a Style to the specified provider.
     *
     * @param parameters The GET KVP parameters send in the request.
     * @param objectRequest The POST parameters send in the request.
     *
     * @return An acknowledgment informing if the request have been successfully treated or not.
     * @throws CstlServiceException
     */
    private AcknowlegementType updateStyle(final String sourceId, final String styleId,
            final Object objectRequest) throws CstlServiceException{

        if(objectRequest instanceof MutableStyle){
            // we read the style to add
            final MutableStyle style = (MutableStyle) objectRequest;

            try {
                // we read the soruce parameter to add

                final ProcessDescriptor procDesc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, SetStyleToStyleProviderDescriptor.NAME);
                final ParameterValueGroup inputs = procDesc.getInputDescriptor().createValue();
                inputs.parameter(SetStyleToStyleProviderDescriptor.PROVIDER_ID_NAME).setValue(sourceId);
                inputs.parameter(SetStyleToStyleProviderDescriptor.STYLE_ID_NAME).setValue(styleId);
                inputs.parameter(SetStyleToStyleProviderDescriptor.STYLE_NAME).setValue(style);

                try {
                    final org.geotoolkit.process.Process process = procDesc.createProcess(inputs);
                    process.call();

                } catch (ProcessException ex) {
                    return new AcknowlegementType("Failure", ex.getLocalizedMessage());
                }

                return new AcknowlegementType("Success", "The layer has been added");

            } catch (NoSuchIdentifierException ex) {
                throw new CstlServiceException(ex);
            } catch (InvalidParameterValueException ex) {
                throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE);
            }
        }

        return new AcknowlegementType("Failure", "Passed object is not a style:" + Classes.getShortClassName(objectRequest));
    }


    /**
     * Returns a list of all process available in the current factories.
     */
    private StringList listProcess(){
        final List<Name> names = Tasks.listProcess();
        final StringList lst = new StringList();
        for(Name n : names){
            lst.getList().add(DefaultName.toJCRExtendedForm(n));
        }
        return lst;
    }

    /**
     * Returns a list of all process available for the specified factory.
     */
    private StringList listProcessForFactory(final String authorityCode) throws CstlServiceException{
        return new StringList(Tasks.listProcessForFactory(authorityCode));
    }

    /**
     * Returns a list of all process available in the current factories.
     */
    private StringList listProcessFactories(){
        final List<String> names = Tasks.listProcessFactory();
        return new StringList(names);
    }

    /**
     * Returns a list of all tasks.
     */
    private StringTreeNode listTasks(){
        final List<Task> tasks = CstlScheduler.getInstance().listTasks();
        final StringTreeNode node = new StringTreeNode();
        for(Task t : tasks){
            final StringTreeNode n = new StringTreeNode();
            n.getProperties().put("id", t.getId());
            n.getProperties().put("title", t.getTitle());
            if (t.getLastExecutionDate() != null) {
                n.getProperties().put("lastRun", String.valueOf(t.getLastExecutionDate()));
            } else {
                n.getProperties().put("lastRun", "");
            }
            if (t.getLastFailedException() != null) {
                n.getProperties().put("lastError", t.getLastFailedException().getMessage());
            } else {
                n.getProperties().put("lastError", "");
            }

            //return value in minutes
            n.getProperties().put("step", String.valueOf(t.getTrigger().getRepeatInterval()/60000));
            n.getProperties().put("authority", t.getDetail().getFactoryIdentifier());
            n.getProperties().put("code", t.getDetail().getProcessIdentifier());

            node.getChildren().add(n);
        }
        return node;
    }

    /**
     * Returns a description of the process parameters.
     */
    private GeneralParameterDescriptor getProcessDescriptor(final String authority, final String code) throws CstlServiceException{
        final ProcessDescriptor desc;
        try {
            desc = ProcessFinder.getProcessDescriptor(authority,code);
        } catch (NoSuchIdentifierException ex) {
            throw new CstlServiceException("No Process for id : {" + authority + "}"+code+" has been found", INVALID_PARAMETER_VALUE);
        } catch (InvalidParameterValueException ex) {
            throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE);
        }
        if(desc == null){
            throw new CstlServiceException("No Process for id : {" + authority + "}"+code+" has been found", INVALID_PARAMETER_VALUE);
        }

        //change the description, always encapsulate in the same namespace and name
        //jaxb object factory can not reconize changing names without a namespace
        ParameterDescriptorGroup idesc = desc.getInputDescriptor();
        idesc = new DefaultParameterDescriptorGroup("input", idesc.descriptors().toArray(new GeneralParameterDescriptor[0]));
        return idesc;
    }

    /**
     * Returns task parameters.
     */
    private Object getTaskParameters(final String id) throws CstlServiceException{
        final Task task = CstlScheduler.getInstance().getTask(id);

        if(task == null){
            return new AcknowlegementType("Failure", "Could not find task for given id.");
        }

        final ParameterValueGroup origParam = task.getDetail().getParameters();

        //change the description, always encapsulate in the same namespace and name
        //jaxb object factory can not reconize changing names without a namespace
        ParameterDescriptorGroup idesc = origParam.getDescriptor();
        idesc = new DefaultParameterDescriptorGroup("input", idesc.descriptors().toArray(new GeneralParameterDescriptor[0]));
        final ParameterValueGroup iparams = idesc.createValue();
        iparams.values().addAll(origParam.values());

        return iparams;
    }

    /**
     * Create a new task.
     */
    private AcknowlegementType createTask(final String authority, final String code, String title,
            final int step, final String id, final Object objectRequest) throws CstlServiceException{
        if(title == null || title.trim().isEmpty()){
            title = id;
        }

        final GeneralParameterDescriptor retypedDesc = getProcessDescriptor(authority, code);


        final ParameterValueGroup params;
        final ParameterValueReader reader = new ParameterValueReader(retypedDesc);
        try {
            reader.setInput(objectRequest);
            params = (ParameterValueGroup) reader.read();
            reader.dispose();
        } catch (XMLStreamException | IOException ex) {
            throw new CstlServiceException(ex);
        }

        //rebuild original values since we have changed the namespace
        final ParameterDescriptorGroup originalDesc;
        try {
            originalDesc = ProcessFinder.getProcessDescriptor(authority,code).getInputDescriptor();
        } catch (NoSuchIdentifierException ex) {
            return new AcknowlegementType("Failure", "No process for given id.");
        }  catch (InvalidParameterValueException ex) {
            throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE);
        }
        final ParameterValueGroup orig = originalDesc.createValue();
        orig.values().addAll(params.values());


        final Task task = new Task(id);
        task.setTitle(title);
        task.setTrigger(TriggerBuilder.newTrigger()
                .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(step*60))
                .build());
        ProcessJobDetail detail = new ProcessJobDetail(authority, code, orig);
        task.setDetail(detail);
        
        try{
            CstlScheduler.getInstance().addTask(task);
        }catch(ConfigurationException ex){
            LOGGER.log(Level.WARNING, ex.getMessage(),ex);
            return new AcknowlegementType("Failure", "Failed to create task : "+ex.getMessage());
        }

        return new AcknowlegementType("Success", "The task has been created");
    }

    /**
     * Update a task.
     */
    private Object updateTask(final String authority, final String code, String title, 
            final int step, final String id, final Object objectRequest) throws CstlServiceException{

        if(title == null || title.trim().isEmpty()){
            title = id;
        }

        final GeneralParameterDescriptor retypedDesc = getProcessDescriptor(authority, code);


        final ParameterValueGroup params;
        final ParameterValueReader reader = new ParameterValueReader(retypedDesc);
        try {
            reader.setInput(objectRequest);
            params = (ParameterValueGroup) reader.read();
            reader.dispose();
        } catch (XMLStreamException | IOException ex) {
            throw new CstlServiceException(ex);
        }

        //rebuild original values since we have changed the namespace
        final ParameterDescriptorGroup originalDesc;
        try {
            originalDesc = ProcessFinder.getProcessDescriptor(authority,code).getInputDescriptor();
        } catch (NoSuchIdentifierException ex) {
            return new AcknowlegementType("Failure", "No process for given id.");
        } catch (InvalidParameterValueException ex) {
            throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE);
        }
        final ParameterValueGroup orig = originalDesc.createValue();
        orig.values().addAll(params.values());


        final Task task = new Task(id);
        task.setTitle(title);
        task.setTrigger(TriggerBuilder.newTrigger()
                .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(step*60))
                .build());
        ProcessJobDetail detail = new ProcessJobDetail(authority, code, orig);
        task.setDetail(detail);

        try{
            if(CstlScheduler.getInstance().updateTask(task)){
                return new AcknowlegementType("Success", "The task has been updated.");
            }else{
                return new AcknowlegementType("Failure", "Could not find task for given id.");
            }
        }catch(ConfigurationException ex){
            LOGGER.log(Level.WARNING, ex.getMessage(),ex);
            return new AcknowlegementType("Failure", "Could not find task for given id : "+ex.getMessage());
        }
    }

    /**
     * Delete a task;
     */
    private Object deleteTask(final String id) throws CstlServiceException{
        try{
            if( CstlScheduler.getInstance().removeTask(id)){
                return new AcknowlegementType("Success", "The task has been deleted");
            }else{
                return new AcknowlegementType("Failure", "Could not find task for given id.");
            }
        }catch(ConfigurationException ex){
            LOGGER.log(Level.WARNING, ex.getMessage(),ex);
            return new AcknowlegementType("Failure", "Could not find task for given id : "+ex.getMessage());
        }
    }

}
