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

package org.constellation.map.configuration;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.quartz.TriggerBuilder;
import org.quartz.SimpleScheduleBuilder;

import org.constellation.configuration.ProviderServiceReport;
import org.constellation.configuration.AbstractConfigurer;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ProviderReport;
import org.constellation.configuration.ProvidersReport;
import org.constellation.configuration.StringList;
import org.constellation.configuration.StringTreeNode;
import org.constellation.provider.LayerProvider;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.LayerProviderService;
import org.constellation.provider.StyleProvider;
import org.constellation.provider.StyleProviderProxy;
import org.constellation.provider.StyleProviderService;
import org.constellation.provider.configuration.ProviderParameters;
import org.constellation.provider.Provider;
import org.constellation.provider.ProviderService;
import org.constellation.scheduler.CstlScheduler;
import org.constellation.scheduler.Task;
import org.constellation.ws.CstlServiceException;

import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.process.quartz.ProcessJobDetail;
import org.geotoolkit.sld.xml.Specification.SymbologyEncoding;
import org.geotoolkit.sld.xml.XMLUtilities;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.util.converter.Classes;
import org.geotoolkit.xml.parameter.ParameterValueReader;

import org.opengis.feature.type.Name;
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
import org.constellation.process.provider.CreateProviderDescriptor;
import org.constellation.process.provider.GetConfigProviderDescriptor;
import org.constellation.process.provider.DeleteProviderDescriptor;
import org.constellation.process.provider.RestartProviderDescriptor;
import org.constellation.process.provider.UpdateProviderDescriptor;
import org.constellation.process.provider.style.SetStyleToStyleProviderDescriptor;
import org.constellation.process.provider.style.DeleteStyleToStyleProviderDescriptor;
import org.geotoolkit.process.ProcessException;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @author Johann Sorel (Geomatys)
 */
public class DefaultMapConfigurer extends AbstractConfigurer {

    private final Map<String, ProviderService> services = new HashMap<String, ProviderService>();

    public DefaultMapConfigurer() {
        final Collection<LayerProviderService> availableLayerServices = LayerProviderProxy.getInstance().getServices();
        for (LayerProviderService service: availableLayerServices) {
            this.services.put(service.getName(), service);
        }
        final Collection<StyleProviderService> availableStyleServices = StyleProviderProxy.getInstance().getServices();
        for (StyleProviderService service: availableStyleServices) {
            this.services.put(service.getName(), service);
        }
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

            final XMLUtilities util = new XMLUtilities();
            try {
                return util.readStyle(stream, SymbologyEncoding.V_1_1_0);
            } catch (FactoryException ex) {
                throw new JAXBException(ex.getMessage(),ex);
            }
        }

        return super.unmarshall(request, parameters, stream);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Object treatRequest(final String request, final MultivaluedMap<String, String> parameters, final Object objectRequest) throws CstlServiceException {

        //Provider services operations
        if (REQUEST_LIST_SERVICES.equalsIgnoreCase(request)) {
            return listProviderServices();
        } else if (REQUEST_GET_SERVICE_DESCRIPTOR.equalsIgnoreCase(request)) {
            return getServiceDescriptor(parameters);
        } else if (REQUEST_GET_SOURCE_DESCRIPTOR.equalsIgnoreCase(request)) {
            return getSourceDescriptor(parameters);
        }

        //Provider operations
        else if (REQUEST_RESTART_ALL_LAYER_PROVIDERS.equalsIgnoreCase(request)) {
            return restartLayerProviders();
        } else if (REQUEST_RESTART_ALL_STYLE_PROVIDERS.equalsIgnoreCase(request)) {
            return restartStyleProviders();
        } else if (REQUEST_CREATE_PROVIDER.equalsIgnoreCase(request)) {
            return createProvider(parameters, objectRequest);
        } else if (REQUEST_UPDATE_PROVIDER.equalsIgnoreCase(request)) {
            return updateProvider(parameters, objectRequest);
        } else if (REQUEST_GET_PROVIDER_CONFIG.equalsIgnoreCase(request)) {
            return getProviderConfiguration(parameters);
        } else if (REQUEST_DELETE_PROVIDER.equalsIgnoreCase(request)) {
            return deleteProvider(parameters);
        } else if (REQUEST_RESTART_PROVIDER.equalsIgnoreCase(request)) {
            return restartProvider(parameters);
        }

        //Layer operations
        else if (REQUEST_CREATE_LAYER.equalsIgnoreCase(request)) {
            return createLayer(parameters, objectRequest);
        } else if (REQUEST_UPDATE_LAYER.equalsIgnoreCase(request)) {
            return updateLayer(parameters, objectRequest);
        } else if (REQUEST_DELETE_LAYER.equalsIgnoreCase(request)) {
            return deleteLayer(parameters);
        }

        //Style operations
        else if (REQUEST_DOWNLOAD_STYLE.equalsIgnoreCase(request)) {
            return downloadStyle(parameters);
        } else if (REQUEST_CREATE_STYLE.equalsIgnoreCase(request)) {
            return createStyle(parameters, objectRequest);
        } else if (REQUEST_UPDATE_STYLE.equalsIgnoreCase(request)) {
            return updateStyle(parameters, objectRequest);
        } else if (REQUEST_DELETE_STYLE.equalsIgnoreCase(request)) {
            return deleteStyle(parameters);
        }

        //Tasks operations
        else if (REQUEST_LIST_PROCESS.equalsIgnoreCase(request)) {
            return ListProcess();
        } else if (REQUEST_LIST_TASKS.equalsIgnoreCase(request)) {
            return ListTasks();
        } else if (REQUEST_GET_PROCESS_DESC.equalsIgnoreCase(request)) {
            return getProcessDescriptor(parameters);
        } else if (REQUEST_GET_TASK_PARAMS.equalsIgnoreCase(request)) {
            return getTaskParameters(parameters);
        } else if (REQUEST_CREATE_TASK.equalsIgnoreCase(request)) {
            return createTask(parameters, objectRequest);
        } else if (REQUEST_UPDATE_TASK.equalsIgnoreCase(request)) {
            return updateTask(parameters, objectRequest);
        } else if (REQUEST_DELETE_TASK.equalsIgnoreCase(request)) {
            return deleteTask(parameters);
        }

        return null;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void beforeRestart() {
        StyleProviderProxy.getInstance().dispose();
        LayerProviderProxy.getInstance().dispose();
    }

    private AcknowlegementType restartLayerProviders(){
        LayerProviderProxy.getInstance().reload();
        return new AcknowlegementType("Success", "All layer providers have been restarted.");
    }

    private AcknowlegementType restartStyleProviders(){
        StyleProviderProxy.getInstance().reload();
        return new AcknowlegementType("Success", "All style providers have been restarted.");

    }


    /**
     * Add a new source to the specified provider.
     *
     * @param parameters The GET KVP parameters send in the request.
     * @param objectRequest The POST parameters send in the request.
     *
     * @return An acknowledgment informing if the request have been successfully treated or not.
     * @throws CstlServiceException
     */
    private AcknowlegementType createProvider(final MultivaluedMap<String, String> parameters,
            final Object objectRequest) throws CstlServiceException{
        final String serviceName = getParameter("serviceName", true, parameters);
        final ProviderService service = this.services.get(serviceName);
        if (service != null) {

            final ParameterValueReader reader = new ParameterValueReader(
                    service.getServiceDescriptor().descriptor(ProviderParameters.SOURCE_DESCRIPTOR_NAME));

            try {
                // we read the source parameter to add
                reader.setInput(objectRequest);
                final ParameterValueGroup sourceToAdd = (ParameterValueGroup) reader.read();

                final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, CreateProviderDescriptor.NAME);

                final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
                inputs.parameter(CreateProviderDescriptor.PROVIDER_TYPE_NAME).setValue(serviceName);
                inputs.parameter(CreateProviderDescriptor.SOURCE_NAME).setValue(sourceToAdd);

                try {
                    final org.geotoolkit.process.Process process = desc.createProcess(inputs);
                    process.call();

                } catch (ProcessException ex) {
                    return new AcknowlegementType("Failure", ex.getLocalizedMessage());
                }

                reader.dispose();
                return new AcknowlegementType("Success", "The source has been added");

            } catch (NoSuchIdentifierException ex) {
                throw new CstlServiceException(ex);
            } catch (XMLStreamException ex) {
                throw new CstlServiceException(ex);
            } catch (IOException ex) {
                throw new CstlServiceException(ex);
            }
        } else {
            throw new CstlServiceException("No provider service for: " + serviceName + " has been found", INVALID_PARAMETER_VALUE);
        }
    }

    /**
     * Modify a source in the specified provider.
     *
     * @param parameters The GET KVP parameters send in the request.
     * @param objectRequest The POST parameters send in the request.
     *
     * @return An acknowledgment informing if the request have been successfully treated or not.
     * @throws CstlServiceException
     */
    private AcknowlegementType updateProvider(final MultivaluedMap<String, String> parameters,
            final Object objectRequest) throws CstlServiceException{
        final String serviceName = getParameter("serviceName", true, parameters);
        final String currentId = getParameter("id", true, parameters);
        final ProviderService service = services.get(serviceName);
        if (service != null) {

            ParameterDescriptorGroup desc = service.getServiceDescriptor();
            desc = (ParameterDescriptorGroup) desc.descriptor("source");
            final ParameterValueReader reader = new ParameterValueReader(desc);

            try {
                // we read the source parameter to add
                reader.setInput(objectRequest);
                final ParameterValueGroup sourceToModify = (ParameterValueGroup) reader.read();

                final ProcessDescriptor procDesc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, UpdateProviderDescriptor.NAME);

                final ParameterValueGroup inputs = procDesc.getInputDescriptor().createValue();
                inputs.parameter(UpdateProviderDescriptor.PROVIDER_ID_NAME).setValue(currentId);
                inputs.parameter(UpdateProviderDescriptor.SOURCE_NAME).setValue(sourceToModify);

                try {
                    final org.geotoolkit.process.Process process = procDesc.createProcess(inputs);
                    process.call();

                } catch (ProcessException ex) {
                    return new AcknowlegementType("Failure", ex.getLocalizedMessage());
                }

                reader.dispose();
                return new AcknowlegementType("Success", "The source has been updated");

            } catch (NoSuchIdentifierException ex) {
                throw new CstlServiceException(ex);
            } catch (XMLStreamException ex) {
                throw new CstlServiceException(ex);
            } catch (IOException ex) {
                throw new CstlServiceException(ex);
            }
        } else {
            throw new CstlServiceException("No descriptor for: " + serviceName + " has been found", INVALID_PARAMETER_VALUE);
        }
    }

    /**
     * Return the configuration object  of the specified source.
     *
     * @param parameters The GET KVP parameters send in the request.
     *
     * @return The configuration object  of the specified source.
     * @throws CstlServiceException
     */
    private Object getProviderConfiguration(final MultivaluedMap<String, String> parameters) throws CstlServiceException{
        final String id = getParameter("id", true, parameters);

         try {

            final ProcessDescriptor procDesc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, GetConfigProviderDescriptor.NAME);
            final ParameterValueGroup inputs = procDesc.getInputDescriptor().createValue();
            inputs.parameter(GetConfigProviderDescriptor.PROVIDER_ID_NAME).setValue(id);

            try {

                final org.geotoolkit.process.Process process = procDesc.createProcess(inputs);
                final ParameterValueGroup outputs = process.call();
                return outputs.parameter(GetConfigProviderDescriptor.CONFIG_NAME).getValue();

            } catch (ProcessException ex) {
                return new AcknowlegementType("Failure", ex.getLocalizedMessage());
            }

        } catch (NoSuchIdentifierException ex) {
           throw new CstlServiceException(ex);
        }
    }

    /**
     * Remove a source in the specified provider.
     *
     * @param parameters The GET KVP parameters send in the request.
     *
     * @return An acknowledgment informing if the request have been successfully treated or not.
     * @throws CstlServiceException
     */
    private AcknowlegementType deleteProvider(final MultivaluedMap<String, String> parameters) throws CstlServiceException{
        final String providerId = getParameter("id", true, parameters);

        try {

            final ProcessDescriptor procDesc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, DeleteProviderDescriptor.NAME);
            final ParameterValueGroup inputs = procDesc.getInputDescriptor().createValue();
            inputs.parameter(DeleteProviderDescriptor.PROVIDER_ID_NAME).setValue(providerId);

            try {
                final org.geotoolkit.process.Process process = procDesc.createProcess(inputs);
                process.call();

            } catch (ProcessException ex) {
                return new AcknowlegementType("Failure", ex.getLocalizedMessage());
            }

            return new AcknowlegementType("Success", "The provider has been deleted");

        } catch (NoSuchIdentifierException ex) {
           throw new CstlServiceException(ex);
        }

    }

    /**
     * Restart a provider in the specified service.
     *
     * @param parameters The GET KVP parameters send in the request.
     *
     * @return An acknowledgment informing if the request have been successfully treated or not.
     * @throws CstlServiceException
     */
    private AcknowlegementType restartProvider(final MultivaluedMap<String, String> parameters) throws CstlServiceException{
        final String providerId = getParameter("id", true, parameters);

         try {

            final ProcessDescriptor procDesc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, RestartProviderDescriptor.NAME);
            final ParameterValueGroup inputs = procDesc.getInputDescriptor().createValue();
            inputs.parameter(RestartProviderDescriptor.PROVIDER_ID_NAME).setValue(providerId);

            try {
                final org.geotoolkit.process.Process process = procDesc.createProcess(inputs);
                process.call();

            } catch (ProcessException ex) {
                return new AcknowlegementType("Failure", ex.getLocalizedMessage());
            }

            return new AcknowlegementType("Success", "The source has been deleted");

        } catch (NoSuchIdentifierException ex) {
           throw new CstlServiceException(ex);
        }
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
    private AcknowlegementType createLayer(final MultivaluedMap<String, String> parameters,
            final Object objectRequest) throws CstlServiceException{

        final String sourceId = getParameter("id", true, parameters);
        final ParameterValueReader reader = new ParameterValueReader(ProviderParameters.LAYER_DESCRIPTOR);
        
        try {
            // we read the soruce parameter to add
            reader.setInput(objectRequest);
            final ParameterValueGroup newLayer = (ParameterValueGroup) reader.read();
            reader.dispose();

            final Collection<LayerProvider> providers = LayerProviderProxy.getInstance().getProviders();
            for (LayerProvider p : providers) {
                if (p.getId().equals(sourceId)) {
                    p.getSource().values().add(newLayer);
                    p.updateSource(p.getSource());
                    return new AcknowlegementType("Success", "The layer has been added");
                }
            }
            return new AcknowlegementType("Failure", "Unable to find a source named:" + sourceId);


        } catch (XMLStreamException ex) {
            throw new CstlServiceException(ex);
        } catch (IOException ex) {
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
    private AcknowlegementType deleteLayer(final MultivaluedMap<String, String> parameters) throws CstlServiceException{
        final String sourceId = getParameter("id", true, parameters);
        final String layerName = getParameter("layerName", true, parameters);

        final Collection<LayerProvider> providers = LayerProviderProxy.getInstance().getProviders();
        for (LayerProvider p : providers) {
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
    private AcknowlegementType updateLayer(final MultivaluedMap<String, String> parameters,
            final Object objectRequest) throws CstlServiceException{

        final String sourceId = getParameter("id", true, parameters);
        final String layerName = getParameter("layerName", true, parameters);

        final ParameterValueReader reader = new ParameterValueReader(ProviderParameters.LAYER_DESCRIPTOR);

        try {
            // we read the source parameter to add
            reader.setInput(objectRequest);
            ParameterValueGroup newLayer = (ParameterValueGroup) reader.read();
            reader.dispose();

            Collection<LayerProvider> providers = LayerProviderProxy.getInstance().getProviders();
            for (LayerProvider p : providers) {
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
        } catch (XMLStreamException ex) {
            throw new CstlServiceException(ex);
        } catch (IOException ex) {
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
    private Object downloadStyle(final MultivaluedMap<String, String> parameters) throws CstlServiceException{
        final String id = getParameter("id", true, parameters);
        final String styleId = getParameter("styleName", true, parameters);

        final Collection<StyleProvider> providers = StyleProviderProxy.getInstance().getProviders();
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
    private AcknowlegementType createStyle(final MultivaluedMap<String, String> parameters,
            final Object objectRequest) throws CstlServiceException{

        final String sourceId = getParameter("id", true, parameters);
        final String styleId = getParameter("styleName", true, parameters);

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
    private AcknowlegementType deleteStyle(final MultivaluedMap<String, String> parameters) throws CstlServiceException{
        final String sourceId = getParameter("id", true, parameters);
        final String styleId = getParameter("styleName", true, parameters);

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
    private AcknowlegementType updateStyle(final MultivaluedMap<String, String> parameters,
            final Object objectRequest) throws CstlServiceException{

        final String sourceId = getParameter("id", true, parameters);
        final String styleId = getParameter("styleName", true, parameters);

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
            }
        }

        return new AcknowlegementType("Failure", "Passed object is not a style:" + Classes.getShortClassName(objectRequest));
    }


    /**
     * Return the service descriptor of the specified type.
     *
     * @param parameters The GET KVP parameters send in the request.
     *
     * @return The descriptor of the specified provider type.
     * @throws CstlServiceException
     */
    private ParameterDescriptorGroup getServiceDescriptor(final MultivaluedMap<String, String> parameters) throws CstlServiceException{
        final String serviceName = getParameter("serviceName", true, parameters);
        final ProviderService service = services.get(serviceName);
        if (service != null) {
            return service.getServiceDescriptor();
        }
        throw new CstlServiceException("No provider service for: " + serviceName + " has been found", INVALID_PARAMETER_VALUE);
    }

    /**
     * Return the service source descriptor of the specified type.
     *
     * @param parameters The GET KVP parameters send in the request.
     *
     * @return The descriptor of the specified provider type.
     * @throws CstlServiceException
     */
    private GeneralParameterDescriptor getSourceDescriptor(final MultivaluedMap<String, String> parameters) throws CstlServiceException{
        final String serviceName = getParameter("serviceName", true, parameters);
        final ProviderService service = services.get(serviceName);
        if (service != null) {
            return service.getSourceDescriptor();
        }
        throw new CstlServiceException("No provider service for: " + serviceName + " has been found", INVALID_PARAMETER_VALUE);
    }

    /**
     * Return a description of the available providers.
     *
     * @return A description of the available providers.
     */
    private ProvidersReport listProviderServices(){
        final List<ProviderServiceReport> providerServ = new ArrayList<ProviderServiceReport>();

        final Collection<LayerProvider> layerProviders = LayerProviderProxy.getInstance().getProviders();
        final Collection<StyleProvider> styleProviders = StyleProviderProxy.getInstance().getProviders();
        for (ProviderService service : services.values()) {

            final List<ProviderReport> providerReports = new ArrayList<ProviderReport>();
            for (LayerProvider p : layerProviders) {
                if (p.getService().equals(service)) {
                    final List<String> keys = new ArrayList<String>();
                    for(Name n : p.getKeys()){
                        keys.add(DefaultName.toJCRExtendedForm(n));
                    }

                    providerReports.add(new ProviderReport(p.getId(), service.getName(), keys));
                }
            }
            for (StyleProvider p : styleProviders) {
                if (p.getService().equals(service)) {
                    final List<String> keys = new ArrayList<String>();
                    for(String n : p.getKeys()){
                        keys.add(n);
                    }
                    providerReports.add(new ProviderReport(p.getId(), service.getName(), keys));
                }
            }
            providerServ.add(new ProviderServiceReport(service.getName(),
                    service instanceof StyleProviderService, providerReports));
        }

        return new ProvidersReport(providerServ);
    }


    /**
     * Returns a list of all process available in the current factories.
     */
    private StringList ListProcess(){
        final List<Name> names = CstlScheduler.getInstance().listProcess();
        final StringList lst = new StringList();
        for(Name n : names){
            lst.getList().add(DefaultName.toJCRExtendedForm(n));
        }
        return lst;
    }

    /**
     * Returns a list of all tasks.
     */
    private StringTreeNode ListTasks(){
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
    private GeneralParameterDescriptor getProcessDescriptor(final MultivaluedMap<String, String> parameters) throws CstlServiceException{
        final String authority = getParameter("authority", true, parameters);
        final String code = getParameter("code", true, parameters);

        final ProcessDescriptor desc;
        try {
            desc = ProcessFinder.getProcessDescriptor(authority,code);
        } catch (NoSuchIdentifierException ex) {
            throw new CstlServiceException("No Process for id : {" + authority + "}"+code+" has been found", INVALID_PARAMETER_VALUE);
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
    private Object getTaskParameters(final MultivaluedMap<String, String> parameters) throws CstlServiceException{
        final String id = getParameter("id", true, parameters);

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
    private AcknowlegementType createTask(final MultivaluedMap<String, String> parameters,
            final Object objectRequest) throws CstlServiceException{
        final String authority = getParameter("authority", true, parameters);
        final String code = getParameter("code", true, parameters);
        String title = getParameter("title", false, parameters);
        final int step = Integer.valueOf(getParameter("step", true, parameters));
        final String id = getParameter("id", true, parameters);

        if(title == null || title.trim().isEmpty()){
            title = id;
        }

        final GeneralParameterDescriptor retypedDesc = getProcessDescriptor(parameters);


        final ParameterValueGroup params;
        final ParameterValueReader reader = new ParameterValueReader(retypedDesc);
        try {
            reader.setInput(objectRequest);
            params = (ParameterValueGroup) reader.read();
            reader.dispose();
        } catch (XMLStreamException ex) {
            throw new CstlServiceException(ex);
        } catch (IOException ex) {
            throw new CstlServiceException(ex);
        }

        //rebuild original values since we have changed the namespace
        final ParameterDescriptorGroup originalDesc;
        try {
            originalDesc = ProcessFinder.getProcessDescriptor(authority,code).getInputDescriptor();
        } catch (NoSuchIdentifierException ex) {
            return new AcknowlegementType("Failure", "No process for given id.");
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
        CstlScheduler.getInstance().addTask(task);

        return new AcknowlegementType("Success", "The task has been created");
    }

    /**
     * Update a task.
     */
    private Object updateTask(final MultivaluedMap<String, String> parameters,
            final Object objectRequest) throws CstlServiceException{
        final String authority = getParameter("authority", true, parameters);
        final String code = getParameter("code", true, parameters);
        String title = getParameter("title", false, parameters);
        final int step = Integer.valueOf(getParameter("step", true, parameters));
        final String id = getParameter("id", true, parameters);

        if(title == null || title.trim().isEmpty()){
            title = id;
        }

        final GeneralParameterDescriptor retypedDesc = getProcessDescriptor(parameters);


        final ParameterValueGroup params;
        final ParameterValueReader reader = new ParameterValueReader(retypedDesc);
        try {
            reader.setInput(objectRequest);
            params = (ParameterValueGroup) reader.read();
            reader.dispose();
        } catch (XMLStreamException ex) {
            throw new CstlServiceException(ex);
        } catch (IOException ex) {
            throw new CstlServiceException(ex);
        }

        //rebuild original values since we have changed the namespace
        final ParameterDescriptorGroup originalDesc;
        try {
            originalDesc = ProcessFinder.getProcessDescriptor(authority,code).getInputDescriptor();
        } catch (NoSuchIdentifierException ex) {
            return new AcknowlegementType("Failure", "No process for given id.");
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

        if(CstlScheduler.getInstance().updateTask(task)){
            return new AcknowlegementType("Success", "The task has been updated.");
        }else{
            return new AcknowlegementType("Failure", "Could not find task for given id.");
        }
    }

    /**
     * Delete a task;
     */
    private Object deleteTask(final MultivaluedMap<String, String> parameters) throws CstlServiceException{
        final String id = getParameter("id", true, parameters);


        if( CstlScheduler.getInstance().removeTask(id)){
            return new AcknowlegementType("Success", "The task has been deleted");
        }else{
            return new AcknowlegementType("Failure", "Could not find task for given id.");
        }
    }

}
