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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.sis.util.logging.Logging;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerContext;

import org.constellation.dto.AccessConstraint;
import org.constellation.dto.AddLayer;
import org.constellation.dto.Contact;
import org.constellation.dto.Service;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.process.service.CreateServiceDescriptor;
import org.constellation.process.service.GetConfigServiceDescriptor;
import org.constellation.process.service.SetConfigServiceDescriptor;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.Worker;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public abstract class AbstractServiceConfiguration implements ServiceConfiguration {

    protected static final Logger LOGGER = Logging.getLogger(AbstractServiceConfiguration.class);

    private Class workerClass;

    private final Class configurationClass;

    private final String configFileName;

    public AbstractServiceConfiguration(final Class workerClass, final Class configurationClass, final String configFileName) {
        this.workerClass        = workerClass;
        this.configurationClass = configurationClass;
        this.configFileName     = configFileName;
    }

    @Override
    public Class getWorkerClass() {
        return workerClass;
    }

    @Override
    public void setWorkerClass(final Class c) {
        this.workerClass = c;
    }

    @Override
    public void configureInstance(File instanceDirectory, Object configuration, Object capabilitiesConfiguration, String serviceType) throws CstlServiceException {
        if (configuration instanceof LayerContext) {
            if (instanceDirectory.isDirectory()) {
                if (instanceDirectory.listFiles().length == 0) {
                    //Create
                    try {
                        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, CreateServiceDescriptor.NAME);
                        final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
                        inputs.parameter(CreateServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceType);
                        inputs.parameter(CreateServiceDescriptor.IDENTIFIER_NAME).setValue(instanceDirectory.getName());
                        inputs.parameter(CreateServiceDescriptor.CONFIG_NAME).setValue(configuration);
                        inputs.parameter(CreateServiceDescriptor.INSTANCE_DIRECTORY_NAME).setValue(instanceDirectory);
                        inputs.parameter(CreateServiceDescriptor.SERVICE_METADATA_NAME).setValue(capabilitiesConfiguration);
                        inputs.parameter(CreateServiceDescriptor.CONFIGURATION_CLASS_NAME).setValue(configurationClass);
                        inputs.parameter(CreateServiceDescriptor.FILENAME_NAME).setValue(configFileName);

                        final org.geotoolkit.process.Process process = desc.createProcess(inputs);
                        process.call();

                    } catch (NoSuchIdentifierException | ProcessException ex) {
                        throw new CstlServiceException(ex);
                    }

                } else {

                    //Update
                    try {
                        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, SetConfigServiceDescriptor.NAME);
                        final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
                        inputs.parameter(SetConfigServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceType);
                        inputs.parameter(SetConfigServiceDescriptor.IDENTIFIER_NAME).setValue(instanceDirectory.getName());
                        inputs.parameter(SetConfigServiceDescriptor.CONFIG_NAME).setValue(configuration);
                        inputs.parameter(SetConfigServiceDescriptor.INSTANCE_DIRECTORY_NAME).setValue(instanceDirectory);
                        inputs.parameter(SetConfigServiceDescriptor.SERVICE_METADATA_NAME).setValue(capabilitiesConfiguration);
                        inputs.parameter(SetConfigServiceDescriptor.CONFIGURATION_CLASS_NAME).setValue(configurationClass);
                        inputs.parameter(SetConfigServiceDescriptor.FILENAME_NAME).setValue(configFileName);

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
    public Object getInstanceConfiguration(File instanceDirectory, String serviceType) throws CstlServiceException {
        try {
            final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, GetConfigServiceDescriptor.NAME);

            ParameterValueGroup in = desc.getInputDescriptor().createValue();
            in.parameter(GetConfigServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceType);
            in.parameter(GetConfigServiceDescriptor.IDENTIFIER_NAME).setValue(instanceDirectory.getName());
            in.parameter(GetConfigServiceDescriptor.INSTANCE_DIRECTORY_NAME).setValue(instanceDirectory);
            in.parameter(GetConfigServiceDescriptor.FILENAME_NAME).setValue(configFileName);

            final org.geotoolkit.process.Process proc = desc.createProcess(in);
            final ParameterValueGroup ouptuts = proc.call();

            return ouptuts.parameter(GetConfigServiceDescriptor.CONFIG_NAME).getValue();

        } catch (NoSuchIdentifierException | ProcessException ex) {
            throw new CstlServiceException(ex);
        }
    }


    
    @Override
    public String getAbstract(final File instanceDirectory){
        try{
            //unmarshall serviceMetadata.xml File to create Service object
            final JAXBContext context = JAXBContext.newInstance(Service.class, Contact.class, AccessConstraint.class);
            final Unmarshaller unmarshaller = context.createUnmarshaller();
            final File serviceMetadata = new File(instanceDirectory, "serviceMetadata.xml");
            final Service service = (Service) unmarshaller.unmarshal(serviceMetadata);
            return service.getDescription();
        } catch (JAXBException ex){
            LOGGER.log(Level.FINEST, "no serviceMetadata.xml");
        }
        return "";
    }

    @Override
    public List<Layer> getlayersNumber(final Worker worker) {
        return new ArrayList<>(0);
    }

    /**
     * Add layer on service
     *
     * @param addLayerData
     * @return <code>true</code> if layer created, <code>false</code> if layer can't be created
     */
    @Override
    public boolean addLayer(final AddLayer addLayerData) {
        return false;
    }
}
