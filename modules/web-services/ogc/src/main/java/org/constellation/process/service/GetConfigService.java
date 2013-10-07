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
import java.io.FileNotFoundException;
import javax.xml.bind.JAXBException;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.configuration.ConfigDirectory;
import org.geotoolkit.process.AbstractProcess;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;

import static org.constellation.process.service.GetConfigServiceDescriptor.*;
import static org.geotoolkit.parameter.Parameters.*;

/**
 *
 * @author Quentin Boileau (Geoamtys)
 */
public class GetConfigService extends AbstractProcess {

    public GetConfigService(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
        super(desc, parameter);
    }

    /**
     * Get the configuration of an existing instance for a specified service and instance name.
     *
     * @throws ProcessException in cases :
     * - if the service name is different from WMS, WMTS, WCS of WFS (no matter of case).
     * - if instance name doesn't exist.
     * - if error during file creation or unmarshalling phase.
     */
    @Override
    protected void execute() throws ProcessException {

        String serviceName             = value(SERVICE_TYPE, inputParameters);
        final String identifier        = value(IDENTIFIER, inputParameters);
        File instanceDirectory         = value(INSTANCE_DIRECTORY, inputParameters);
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

        //get config directory .constellation if null
        if (instanceDirectory == null) {
            final File configDirectory = ConfigDirectory.getConfigDirectory();


            if (configDirectory != null && configDirectory.isDirectory()) {

                //get service directory ("WMS", "WMTS", "WFS", "WCS", "WPS", "CSW", "SOS")
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

            try {
                final Object obj = ConfigurationEngine.getConfiguration(instanceDirectory, identifier);
                if (obj.getClass().isAssignableFrom(configurationClass)) {
                    getOrCreate(CONFIGURATION, outputParameters).setValue(obj);
                } else {
                    throw new ProcessException("The " + configFileName + " file does not contain a " + configurationClass.getName() + " object.", this, null);
                }
            } catch (JAXBException ex) {
                throw new ProcessException(ex.getMessage(), this, ex);
            } catch (FileNotFoundException ex) {
                throw new ProcessException("Service instance " + identifier + " doesn't have any configuration file.", this, null);
            }
        } else {
            throw new ProcessException("Service instance " + identifier + " doesn't exist.", this, null);
        }
    }
}
