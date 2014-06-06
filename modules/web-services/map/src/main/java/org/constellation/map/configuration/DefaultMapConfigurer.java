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
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.constellation.provider.*;

import org.constellation.configuration.AbstractConfigurer;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.provider.configuration.ProviderParameters;
import org.constellation.ws.CstlServiceException;

import org.geotoolkit.xml.parameter.ParameterValueReader;

import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import static org.constellation.api.QueryConstants.*;

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

       /* if ( REQUEST_CREATE_STYLE.equalsIgnoreCase(request)
          || REQUEST_UPDATE_STYLE.equalsIgnoreCase(request)) {
            return true;
        }*/

        return super.needCustomUnmarshall(request,parameters);
    }

    @Override
    public Object unmarshall(final String request, final MultivaluedMap<String, String> parameters,
            final InputStream stream) throws JAXBException, CstlServiceException {

        /*if ( REQUEST_CREATE_STYLE.equalsIgnoreCase(request)
          || REQUEST_UPDATE_STYLE.equalsIgnoreCase(request)) {

            final StyleXmlIO util = new StyleXmlIO();
            try {
                return util.readStyle(stream, SymbologyEncoding.V_1_1_0);
            } catch (FactoryException ex) {
                throw new JAXBException(ex.getMessage(),ex);
            }
        }*/

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
}
