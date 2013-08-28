/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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
package org.constellation.ws.rs;

import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerContext;
import org.constellation.dto.AccessConstraint;
import org.constellation.dto.AddLayer;
import org.constellation.dto.Contact;
import org.constellation.dto.Service;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.process.service.AddLayerToMapServiceDescriptor;
import org.constellation.process.service.CreateMapServiceDescriptor;
import org.constellation.process.service.SetConfigMapServiceDescriptor;
import org.constellation.provider.LayerProvider;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.configuration.ProviderParameters;
import org.constellation.util.DataReference;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.LayerWorker;
import org.constellation.ws.Worker;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_PARAMETER_VALUE;

/**
 * WMS, WMTS, WFS and WCS {@link org.constellation.ws.rs.ServiceConfiguration} implementation
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
public class GridServiceConfiguration extends AbstractServiceConfiguration implements ServiceConfiguration {

    public GridServiceConfiguration(final Class workerClass) {
        super(workerClass, null, null);
    }

    @Override
    public void configureInstance(File instanceDirectory, Object configuration, Object capabilitiesConfiguration, String serviceType) throws CstlServiceException {
        if (configuration instanceof LayerContext) {
            if (instanceDirectory.isDirectory()) {
                if (instanceDirectory.listFiles().length == 0) {
                    //Create
                    try {
                        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, CreateMapServiceDescriptor.NAME);
                        final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
                        inputs.parameter(CreateMapServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceType);
                        inputs.parameter(CreateMapServiceDescriptor.IDENTIFIER_NAME).setValue(instanceDirectory.getName());
                        inputs.parameter(CreateMapServiceDescriptor.CONFIG_NAME).setValue(configuration);
                        inputs.parameter(CreateMapServiceDescriptor.INSTANCE_DIRECTORY_NAME).setValue(instanceDirectory);
                        inputs.parameter(CreateMapServiceDescriptor.SERVICE_METADATA_NAME).setValue(capabilitiesConfiguration);

                        final org.geotoolkit.process.Process process = desc.createProcess(inputs);
                        process.call();

                    } catch (NoSuchIdentifierException | ProcessException ex) {
                        throw new CstlServiceException(ex);
                    }

                } else {

                    //Update
                    try {
                        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, SetConfigMapServiceDescriptor.NAME);
                        final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
                        inputs.parameter(SetConfigMapServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceType);
                        inputs.parameter(SetConfigMapServiceDescriptor.IDENTIFIER_NAME).setValue(instanceDirectory.getName());
                        inputs.parameter(SetConfigMapServiceDescriptor.CONFIG_NAME).setValue(configuration);
                        inputs.parameter(SetConfigMapServiceDescriptor.INSTANCE_DIRECTORY_NAME).setValue(instanceDirectory);
                        inputs.parameter(SetConfigMapServiceDescriptor.SERVICE_METADATA_NAME).setValue(capabilitiesConfiguration);

                        final org.geotoolkit.process.Process process = desc.createProcess(inputs);
                        process.call();

                    } catch (NoSuchIdentifierException | ProcessException ex) {
                        throw new CstlServiceException(ex);
                    }
                }
            }
        } else {
            throw new CstlServiceException("The configuration Object is not an LayerContext object", INVALID_PARAMETER_VALUE);
        }
    }

    @Override
    public void basicConfigure(final File instanceDirectory, Object capabilitiesConfiguration, String serviceType) throws CstlServiceException {
        configureInstance(instanceDirectory, new LayerContext(), capabilitiesConfiguration, serviceType);
    }

    @Override
    public List<Layer> getlayersNumber(Worker worker) {
        if(worker instanceof LayerWorker){
            final LayerWorker layerWorker = (LayerWorker)worker;
            return layerWorker.getConfigurationLayers(null);
        }
        return new ArrayList<>(0);
    }


    @Override
    public boolean addLayer(final AddLayer addLayerData) {
        LayerProvider provider = LayerProviderProxy.getInstance().getProvider(addLayerData.getProviderId());
        String namespace = ProviderParameters.getNamespace(provider);
        String layerId = "{"+namespace+"}"+addLayerData.getLayerId();

        // set layer and style provider reference
        final DataReference layerProviderReference = DataReference.createProviderDataReference(DataReference.PROVIDER_LAYER_TYPE, addLayerData.getProviderId(), layerId);
        final DataReference styleProviderReference = DataReference.createProviderDataReference(DataReference.PROVIDER_STYLE_TYPE, addLayerData.getStyleProviderId(), addLayerData.getStyleId());


        try {
            //build descriptor
            final ProcessDescriptor descriptor = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, "service.add_layer");
            final ParameterValueGroup inputs = descriptor.getInputDescriptor().createValue();
            inputs.parameter(AddLayerToMapServiceDescriptor.LAYER_REF_PARAM_NAME).setValue(layerProviderReference);
            inputs.parameter(AddLayerToMapServiceDescriptor.LAYER_ALIAS_PARAM_NAME).setValue(addLayerData.getLayerAlias());
            inputs.parameter(AddLayerToMapServiceDescriptor.LAYER_STYLE_PARAM_NAME).setValue(styleProviderReference);
            inputs.parameter(AddLayerToMapServiceDescriptor.SERVICE_TYPE_PARAM_NAME).setValue(addLayerData.getServiceType());
            inputs.parameter(AddLayerToMapServiceDescriptor.SERVICE_INSTANCE_PARAM_NAME).setValue(addLayerData.getServiceId());

            //call process
            final org.geotoolkit.process.Process process = descriptor.createProcess(inputs);
            final ParameterValueGroup outputs = process.call();
            final LayerContext outputContext = (LayerContext) outputs.parameter(AddLayerToMapServiceDescriptor.OUT_LAYER_CTX_PARAM_NAME).getValue();
            if(outputContext!=null){
                return true;
            }
        } catch (NoSuchIdentifierException e) {
            LOGGER.log(Level.WARNING, "error when try to create process descriptor", e);
        } catch (ProcessException e) {
            LOGGER.log(Level.WARNING, "error on process call", e);
        }
        return false;
    }
}
