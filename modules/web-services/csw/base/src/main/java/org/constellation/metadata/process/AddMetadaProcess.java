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
package org.constellation.metadata.process;

import java.io.File;
import org.constellation.ServiceDef;
import org.constellation.configuration.ConfigurationException;
import org.constellation.metadata.configuration.CSWConfigurer;
import static org.constellation.metadata.process.AddMetadataDescriptor.METADATA_FILE;
import static org.constellation.metadata.process.AddMetadataDescriptor.SERVICE_IDENTIFIER;
import org.constellation.process.AbstractCstlProcess;
import org.constellation.ws.ServiceConfigurer;
import static org.geotoolkit.parameter.Parameters.value;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class AddMetadaProcess extends AbstractCstlProcess {

    public AddMetadaProcess(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
        super(desc, parameter);
    }
    
    @Override
    protected void execute() throws ProcessException {
        final String serviceID  = value(SERVICE_IDENTIFIER, inputParameters);
        final File metadataFile = value(METADATA_FILE, inputParameters);

        try {
            final CSWConfigurer configurer = (CSWConfigurer) ServiceConfigurer.newInstance(ServiceDef.Specification.CSW);
            configurer.importRecords(serviceID, metadataFile, metadataFile.getName());
        } catch (ConfigurationException ex) {
            throw new ProcessException(null, this, ex);
        }
    }
}
