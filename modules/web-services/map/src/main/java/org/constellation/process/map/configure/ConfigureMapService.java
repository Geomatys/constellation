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
package org.constellation.process.map.configure;

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
import static org.constellation.process.map.configure.ConfigureMapServiceDescriptor.*;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public class ConfigureMapService extends AbstractProcess {

    public ConfigureMapService(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
        super(desc, parameter);
    }

    /**
     * Update configuration of an existing instance for a specified service and instance name.
     *
     * @throws ProcessException in cases : 
     * - if the service name is different from WMS, WMTS of WFS (no mather of case).
     * - if instance name doesn't exist.
     * - if error during file creation or marshalling phase.
     */
    @Override
    protected void execute() throws ProcessException {

        String serviceName = value(SERVICE_NAME, inputParameters);
        final String identifier = value(IDENTIFIER, inputParameters);
        final LayerContext configuration = value(CONFIGURATION, inputParameters);

        if (serviceName != null && !serviceName.isEmpty() && ("WMS".equalsIgnoreCase(serviceName) || "WMTS".equalsIgnoreCase(serviceName) || "WFS".equalsIgnoreCase(serviceName))) {
            serviceName = serviceName.toUpperCase();
        } else {
            throw new ProcessException("Service name can't be null or empty but one of these (\"WMS\", \"WMTS\", \"WFS\").", this, null);
        }

        if (identifier == null || identifier.isEmpty()) {
            throw new ProcessException("Service instance identifier can't be null or empty.", this, null);
        }

        //get config directory .constellation
        final File configDirectory = ConfigDirectory.getConfigDirectory();
        if (configDirectory != null && configDirectory.isDirectory()) {

            //get service directory ("WMS", "WMTS", "WFS")
            final File serviceDir = new File(configDirectory, serviceName);
            if (serviceDir.exists() && serviceDir.isDirectory()) {

                //get service instance directory
                final File instanceDirectory = new File(serviceDir, identifier);

                if (instanceDirectory.exists() && serviceDir.isDirectory()) {
                    
                    //get layerContext.xml file.
                    File configurationFile = new File(instanceDirectory, "layerContext.xml");
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
                    throw new ProcessException("Service instance " + identifier + " doesn't exist.", this, null);
                }
                
            } else {
                throw new ProcessException("Service directory can' be found for service name : " + serviceName, this, null);
            }
            
        } else {
            throw new ProcessException("Configuration directory can' be found.", this, null);
        }
    }
}
