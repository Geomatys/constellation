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

import org.constellation.business.IServiceBusiness;
import org.constellation.configuration.ConfigurationException;
import org.constellation.process.AbstractCstlProcess;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;
import org.springframework.beans.factory.annotation.Autowired;

import static org.constellation.process.service.RenameServiceDescriptor.IDENTIFIER;
import static org.constellation.process.service.RenameServiceDescriptor.NEW_NAME;
import static org.constellation.process.service.RenameServiceDescriptor.SERVICE_TYPE;
import static org.geotoolkit.parameter.Parameters.value;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class RenameService extends AbstractCstlProcess {
    @Autowired
    public IServiceBusiness serviceBusiness;

    public RenameService(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
        super(desc, parameter);
    }

    @Override
    protected void execute() throws ProcessException {
        final String serviceType   = value(SERVICE_TYPE, inputParameters);
        final String identifier    = value(IDENTIFIER, inputParameters);
        final String newIdentifier = value(NEW_NAME, inputParameters);
        try {
            serviceBusiness.rename(serviceType, identifier, newIdentifier);
        } catch (ConfigurationException ex) {
            throw new ProcessException(ex.getMessage(), this, ex);
        }
    }
}
