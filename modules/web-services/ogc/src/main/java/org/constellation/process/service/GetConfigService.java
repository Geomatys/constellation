/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.process.service;

import java.io.FileNotFoundException;
import javax.xml.bind.JAXBException;
import org.constellation.admin.ConfigurationEngine;
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

        final String serviceType       = value(SERVICE_TYPE, inputParameters);
        final String identifier        = value(IDENTIFIER, inputParameters);
        final Class configurationClass = value(CONFIGURATION_CLASS, inputParameters);

        if (identifier == null || identifier.isEmpty()) {
            throw new ProcessException("Service instance identifier can't be null or empty.", this, null);
        }

        try {
            final Object obj = ConfigurationEngine.getConfiguration(serviceType, identifier);
            if (obj.getClass().isAssignableFrom(configurationClass)) {
                getOrCreate(CONFIGURATION, outputParameters).setValue(obj);
            } else {
                throw new ProcessException("The configuration does not contain a " + configurationClass.getName() + " object.", this, null);
            }
        } catch (JAXBException ex) {
            throw new ProcessException(ex.getMessage(), this, ex);
        } catch (FileNotFoundException ex) {
            throw new ProcessException("Service instance " + identifier + " doesn't exist.", this, null);
       }
    }
}
