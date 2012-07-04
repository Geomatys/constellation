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
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.LayerContext;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.geotoolkit.process.AbstractProcess;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;
import static org.geotoolkit.parameter.Parameters.*;
import static org.constellation.process.service.CreateMapServiceDescriptor.*;

/**
 * Process that create a new instance configuration from the service name (WMS, WMTS or WFS) for a specified instance name.
 * If the instance directory is created but no configuration file exist, the process will create one.
 * Execution will throw ProcessExeption if the service name is different from WMS, WMTS of WFS (no matter of case) or if
 * a configuration file already exist fo this instance name.
 * @author Quentin Boileau (Geomatys).
 */
public class CreateMapService extends AbstractProcess {

    public CreateMapService(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
        super(desc, parameter);
    }

    /**
     * Create a new instance and configuration for a specified service and instance name.
     * @throws ProcessException in cases :
     * - if the service name is different from WMS, WMTS of WFS (no matter of case)
     * - if a configuration file already exist for this instance name.
     * - if error during file creation or marshalling phase.
     */
    @Override
    protected void execute() throws ProcessException {

        String serviceName = value(SERVICE_NAME, inputParameters);
        final String identifier = value(IDENTIFIER, inputParameters);
        LayerContext configuration = value(CONFIGURATION, inputParameters);

        if (serviceName != null && !serviceName.isEmpty() && ("WMS".equalsIgnoreCase(serviceName) || "WMTS".equalsIgnoreCase(serviceName) || "WFS".equalsIgnoreCase(serviceName))) {
            serviceName = serviceName.toUpperCase();
        } else {
            throw new ProcessException("Service name can't be null or empty but one of these (\"WMS\", \"WMTS\", \"WFS\").", this, null);
        }

        if (identifier == null || identifier.isEmpty()) {
            throw new ProcessException("Service instance identifier can't be null or empty.", this, null);
        }

        if (configuration == null) {
            configuration = new LayerContext();
        }

        //get config directory .constellation
        final File configDirectory = ConfigDirectory.getConfigDirectory();
        if (configDirectory != null && configDirectory.isDirectory()) {

            //get service directory ("WMS", "WMTS", "WFS")
            final File serviceDir = new File(configDirectory, serviceName);
            if (serviceDir.exists() && serviceDir.isDirectory()) {

                //create service instance directory
                final File instanceDirectory = new File(serviceDir, identifier);

                File configurationFile = null;
                if (instanceDirectory.exists()) {
                     configurationFile = new File(instanceDirectory, "layerContext.xml");
                     if (configurationFile.exists()) {
                         throw new ProcessException("Instance identifier already exist.", this, null);
                     }

                } else if (instanceDirectory.mkdir()) {
                    configurationFile = new File(instanceDirectory, "layerContext.xml");
                } else {
                    throw new ProcessException("Service instance directory can' be created. Check permissions.", this, null);
                }

                //create layerContext.xml file for the default configuration.
                Marshaller marshaller = null;
                try {
                    marshaller = GenericDatabaseMarshallerPool.getInstance().acquireMarshaller();
                    marshaller.marshal(configuration, configurationFile);

                } catch (JAXBException ex) {
                    throw new ProcessException(null, this, ex);
                } finally {
                    if (marshaller != null) {
                        GenericDatabaseMarshallerPool.getInstance().release(marshaller);
                    }
                }

            } else {
                throw new ProcessException("Service directory can't be found for service name : "+serviceName, this, null);
            }
        } else {
            throw new ProcessException("Configuration directory can't be found.", this, null);
        }
    }
}
