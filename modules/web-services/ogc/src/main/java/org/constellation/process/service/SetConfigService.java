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

import java.io.IOException;
import javax.xml.bind.JAXBException;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.dto.Service;
import org.geotoolkit.process.AbstractProcess;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;
import static org.geotoolkit.parameter.Parameters.*;
import static org.constellation.process.service.SetConfigServiceDescriptor.*;
import org.constellation.util.ReflectionUtilities;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public class SetConfigService extends AbstractProcess {

    public SetConfigService(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
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

        final String serviceType             = value(SERVICE_TYPE, inputParameters);
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

        //write configuration file.
        try {
            ConfigurationEngine.storeConfiguration(serviceType, identifier, configFileName, configuration);
        } catch (JAXBException ex) {
            throw new ProcessException(ex.getMessage(), this, ex);
        }

        // Override the service metadata.
        if (serviceMetadata != null) {
            try {
                ConfigurationEngine.writeMetadata(identifier, serviceType, serviceMetadata, null);
            } catch (IOException ex) {
                throw new ProcessException("An error occurred while trying to write serviceMetadata.xml file.", this, null);
            }
        }
    }
}
