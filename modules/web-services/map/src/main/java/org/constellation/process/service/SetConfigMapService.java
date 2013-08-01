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
import static org.constellation.process.service.SetConfigMapServiceDescriptor.*;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public class SetConfigMapService extends AbstractProcess {

    public SetConfigMapService(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
        super(desc, parameter);
    }

    /**
     * Update configuration of an existing instance for a specified service and instance name.
     *
     * @throws ProcessException in cases :
     * - if the service name is different from WMS, WMTS, WCS of WFS (no matter of case).
     * - if instance name doesn't exist.
     * - if error during file creation or marshalling phase.
     */
    @Override
    protected void execute() throws ProcessException {

        String serviceName = value(SERVICE_TYPE, inputParameters);
        final String identifier = value(IDENTIFIER, inputParameters);
        LayerContext configuration = value(CONFIGURATION, inputParameters);
        File instanceDirectory = value(INSTANCE_DIRECTORY, inputParameters);

        if (serviceName != null && !serviceName.isEmpty() && ("WMS".equalsIgnoreCase(serviceName) || "WMTS".equalsIgnoreCase(serviceName)
                || "WFS".equalsIgnoreCase(serviceName) || "WCS".equalsIgnoreCase(serviceName))) {
            serviceName = serviceName.toUpperCase();
        } else {
            throw new ProcessException("Service name can't be null or empty but one of these (\"WMS\", \"WMTS\", \"WFS\", \"WCS\").", this, null);
        }

        if (identifier == null || identifier.isEmpty()) {
            throw new ProcessException("Service instance identifier can't be null or empty.", this, null);
        }

        if (configuration == null) {
            configuration = new LayerContext();
        }

        //get config directory .constellation if null
        if (instanceDirectory == null) {
            final File configDirectory = ConfigDirectory.getConfigDirectory();


            if (configDirectory != null && configDirectory.isDirectory()) {

                //get service directory ("WMS", "WMTS", "WFS", "WCS")
                final File serviceDir = new File(configDirectory, serviceName);
                if (serviceDir.exists() && serviceDir.isDirectory()) {

                    //get service instance directory
                    instanceDirectory = new File(serviceDir, identifier);

                } else {
                    throw new ProcessException("Service directory can't be found for service name : " + serviceName, this, null);
                }

            } else {
                throw new ProcessException("Configuration directory can't be found.", this, null);
            }
        }

        if (instanceDirectory.exists() && instanceDirectory.isDirectory()) {

            //get layerContext.xml file.
            File configurationFile = new File(instanceDirectory, "layerContext.xml");
            try {
                final Marshaller marshaller = GenericDatabaseMarshallerPool.getInstance().acquireMarshaller();
                marshaller.marshal(configuration, configurationFile);
                GenericDatabaseMarshallerPool.getInstance().recycle(marshaller);

            } catch (JAXBException ex) {
                throw new ProcessException(ex.getMessage(), this, ex);
            }

        } else {
            throw new ProcessException("Service instance " + identifier + " doesn't exist.", this, null);
        }
    }
}
