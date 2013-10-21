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

import java.io.FileNotFoundException;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.dto.Service;
import org.constellation.util.ReflectionUtilities;
import static org.constellation.process.service.CreateServiceDescriptor.*;

import org.geotoolkit.process.AbstractProcess;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import static org.geotoolkit.parameter.Parameters.*;

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

        final String serviceType       = value(SERVICE_TYPE, inputParameters);
        final String identifier        = value(IDENTIFIER, inputParameters);
        Object configuration           = value(CONFIGURATION, inputParameters);
        final Service serviceMetadata  = value(SERVICE_METADATA, inputParameters);
        final Class configurationClass = value(CONFIGURATION_CLASS, inputParameters);
        final String configFileName    = value(FILENAME, inputParameters);

        if (identifier == null || identifier.isEmpty()) {
            throw new ProcessException("Service instance identifier can't be null or empty.", this, null);
        }

        if (configuration == null) {
            configuration = ReflectionUtilities.newInstance(configurationClass);
        }

        boolean createConfig = false;
        try {
            final Object obj = ConfigurationEngine.getConfiguration(serviceType, identifier, configFileName);
            if (obj.getClass().isAssignableFrom(configurationClass)) {
                configuration = obj;
            } else {
                throw new ProcessException("The " + configFileName + " file does not contain a " + configurationClass.getName() + " object", this, null);
            }
        } catch (JAXBException ex) {
            throw new ProcessException(ex.getMessage(), this, ex);
        } catch (FileNotFoundException ex) {
            createConfig = true;
        }

        if (createConfig) {
            //create config file for the default configuration.
            try {
                ConfigurationEngine.createConfiguration(serviceType, identifier, configFileName, configuration, serviceMetadata);
            } catch (JAXBException ex) {
                throw new ProcessException(ex.getMessage(), this, ex);
            } catch (IOException ex) {
                throw new ProcessException("An error occurred while trying to write serviceMetadata.xml file.", this, null);
            }
        }

        getOrCreate(OUT_CONFIGURATION, outputParameters).setValue(configuration);
    }
}
