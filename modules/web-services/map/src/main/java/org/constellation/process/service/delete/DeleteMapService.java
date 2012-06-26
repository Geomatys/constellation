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
package org.constellation.process.service.delete;

import java.io.File;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.process.AbstractCstlProcess;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;
import static org.geotoolkit.parameter.Parameters.*;
import static org.constellation.process.service.delete.DeleteMapServiceDescriptor.*;
import org.constellation.ws.WSEngine;
import org.geotoolkit.util.FileUtilities;

/**
 *
 * @author Quentin Boileau (Geomatys)
 */
public class DeleteMapService extends AbstractCstlProcess {

     public DeleteMapService(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
        super(desc, parameter);
    }

     /**
     * Delete an instance and configuration for a specified service and instance name.
     * @throws ProcessException in cases :
     * - if the service name is different from WMS, WMTS of WFS (no matter of case)
     * - if identifier doesn't exist or is null/empty.
     * - if error during file erasing phase.
     */
    @Override
    protected void execute() throws ProcessException {
        String serviceName = value(SERVICE_NAME, inputParameters);
        final String identifier = value(IDENTIFIER, inputParameters);

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

                //create service instance directory
                final File instanceDirectory = new File(serviceDir, identifier);
                if (instanceDirectory.exists() && instanceDirectory.isDirectory()) {

                    //unregister the service instance if exist
                    if (WSEngine.serviceInstanceExist(serviceName, identifier)) {
                        WSEngine.shutdownInstance(serviceName, identifier);
                    }

                    //delete folder
                    if (!FileUtilities.deleteDirectory(instanceDirectory)) {
                        throw new ProcessException("Service instance directory " + identifier + " can't be deleted.", this, null);
                    }

                } else {
                    throw new ProcessException("Service instance " + identifier + " doesn't exist.", this, null);
                }

            } else {
                throw new ProcessException("Service directory can't be found for service name : "+serviceName, this, null);
            }
        } else {
            throw new ProcessException("Configuration directory can't be found.", this, null);
        }
    }

}
