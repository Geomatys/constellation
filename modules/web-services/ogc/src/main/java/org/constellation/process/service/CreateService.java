/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.process.service;

import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.dto.Service;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import static org.constellation.process.service.CreateServiceDescriptor.*;
import org.constellation.util.ReflectionUtilities;
import static org.geotoolkit.parameter.Parameters.*;
//import org.constellation.ws.rs.MapServices;
import org.geotoolkit.process.AbstractProcess;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;

/**
 * Process that create a new instance configuration from the service name (WMS, WMTS, WCS or WFS) for a specified instance name.
 * If the instance directory is created but no configuration file exist, the process will create one.
 * Execution will throw ProcessExeption if the service name is different from WMS, WMTS of WFS (no matter of case) or if
 * a configuration file already exist fo this instance name.
 * @author Quentin Boileau (Geomatys).
 */
public class CreateService extends AbstractProcess {

    public CreateService(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
        super(desc, parameter);
    }

    /**
     * Create a new instance and configuration for a specified service and instance name.
     * @throws ProcessException in cases :
     * - if the service name is different from WMS, WMTS, WCS of WFS (no matter of case)
     * - if a configuration file already exist for this instance name.
     * - if error during file creation or marshalling phase.
     */
    @Override
    protected void execute() throws ProcessException {

        String serviceName = value(SERVICE_TYPE, inputParameters);
        final String identifier = value(IDENTIFIER, inputParameters);
        Object configuration = value(CONFIGURATION, inputParameters);
        File instanceDirectory = value(INSTANCE_DIRECTORY, inputParameters);
        Service serviceMetadata = value(SERVICE_METADATA, inputParameters);
        final Class configurationClass = value(CONFIGURATION_CLASS, inputParameters);
        final String configFileName    = value(FILENAME, inputParameters);

        if (serviceName != null && !serviceName.isEmpty()
        && ("WMS".equalsIgnoreCase(serviceName) || "WMTS".equalsIgnoreCase(serviceName) ||
            "WFS".equalsIgnoreCase(serviceName) || "WCS".equalsIgnoreCase(serviceName)  ||
            "WPS".equalsIgnoreCase(serviceName) || "SOS".equalsIgnoreCase(serviceName)  ||
            "CSW".equalsIgnoreCase(serviceName))) {
            serviceName = serviceName.toUpperCase();
        } else {
            throw new ProcessException("Service name can't be null or empty but one of these (\"WMS\", \"WMTS\", \"WFS\", \"WCS\", \"WPS\", \"CSW\", \"SOS\").", this, null);
        }

        if (identifier == null || identifier.isEmpty()) {
            throw new ProcessException("Service instance identifier can't be null or empty.", this, null);
        }

        if (configuration == null) {
            configuration = ReflectionUtilities.newInstance(configurationClass);
        }

        //get config directory .constellation if null
        if (instanceDirectory == null) {
            final File configDirectory = ConfigDirectory.getConfigDirectory();


            if (configDirectory != null && configDirectory.isDirectory()) {

                //get service directory ("WMS", "WMTS", "WFS", "WCS")
                final File serviceDir = new File(configDirectory, serviceName);
                if (serviceDir.exists() && serviceDir.isDirectory()) {

                    //create service instance directory
                    instanceDirectory = new File(serviceDir, identifier);

                } else {
                    throw new ProcessException("Service directory can't be found for service name : " + serviceName, this, null);
                }
            } else {
                throw new ProcessException("Configuration directory can't be found.", this, null);
            }
        }


        File configurationFile = null;
        boolean createConfig = true;
        if (instanceDirectory.exists()) {
            configurationFile = new File(instanceDirectory, configFileName);

            //get configuration if aleady exist.
            if (configurationFile.exists()) {
                createConfig = false;
                try {
                    final Unmarshaller unmarshaller = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();
                    final Object obj = unmarshaller.unmarshal(configurationFile);
                    GenericDatabaseMarshallerPool.getInstance().recycle(unmarshaller);
                    if (obj.getClass().isAssignableFrom(configurationClass)) {
                        configuration = obj;
                    } else {
                        throw new ProcessException("The " + configFileName + " file does not contain a " + configurationClass.getName() + " object", this, null);
                    }
                } catch (JAXBException ex) {
                    throw new ProcessException(ex.getMessage(), this, ex);
                }
            }

            /* TODO RESTORE
             * Write the service metadata.
            if (serviceMetadata != null) {
                try {
                    MapServices.writeMetadata(instanceDirectory, serviceMetadata);
                } catch (IOException ex) {
                    throw new ProcessException("An error occurred while trying to write serviceMetadata.xml file.", this, null);
                }
            }*/

        } else if (instanceDirectory.mkdir()) {
            configurationFile = new File(instanceDirectory, configFileName);
        } else {
            throw new ProcessException("Service instance directory can' be created. Check permissions.", this, null);
        }

        if (createConfig) {
            //create layerContext.xml file for the default configuration.
            try {
                final Marshaller marshaller = GenericDatabaseMarshallerPool.getInstance().acquireMarshaller();
                marshaller.marshal(configuration, configurationFile);
                GenericDatabaseMarshallerPool.getInstance().recycle(marshaller);

            } catch (JAXBException ex) {
                throw new ProcessException(ex.getMessage(), this, ex);
            }
        }

        getOrCreate(OUT_CONFIGURATION, outputParameters).setValue(configuration);
    }
}
