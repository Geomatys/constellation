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

package org.constellation.process;

import org.geotoolkit.processing.AbstractProcessDescriptor;
import org.opengis.metadata.Identifier;
import org.opengis.metadata.identification.Identification;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.InternationalString;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public abstract class AbstractCstlProcessDescriptor extends AbstractProcessDescriptor {

    public AbstractCstlProcessDescriptor(final String name, final Identification factoryId, final InternationalString abs, 
            final ParameterDescriptorGroup inputDesc, final ParameterDescriptorGroup outputdesc) {
        super(name, factoryId, abs, inputDesc, outputdesc);
    }

    public AbstractCstlProcessDescriptor(final Identifier id, final InternationalString abs, final InternationalString displayName, 
            final ParameterDescriptorGroup inputDesc, final ParameterDescriptorGroup outputdesc, final String version) {
        super(id, abs, displayName, inputDesc, outputdesc, version);
    }
    
    public AbstractCstlProcessDescriptor(final Identifier id, final InternationalString abs, final InternationalString displayName, 
            final ParameterDescriptorGroup inputDesc, final ParameterDescriptorGroup outputdesc) {
        super(id, abs, displayName, inputDesc, outputdesc);
    }
    
    public AbstractCstlProcessDescriptor(final String name, final Identification factoryId, final InternationalString abs, 
            final InternationalString displayName, final ParameterDescriptorGroup inputDesc, final ParameterDescriptorGroup outputdesc) {
        super(name, factoryId, abs, displayName, inputDesc, outputdesc);
    }

    @Override
    public final AbstractCstlProcess createProcess(final ParameterValueGroup input) {
        return buildProcess(input);
    }

    /**
     * 
     * @param input
     * @return 
     */
    protected abstract AbstractCstlProcess buildProcess(final ParameterValueGroup input);
}
