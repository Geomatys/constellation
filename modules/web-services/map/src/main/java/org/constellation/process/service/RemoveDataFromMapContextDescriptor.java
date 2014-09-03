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

import org.apache.sis.util.iso.SimpleInternationalString;
import org.constellation.process.AbstractCstlProcess;
import org.constellation.process.AbstractCstlProcessDescriptor;
import org.constellation.process.ConstellationProcessFactory;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.geotoolkit.process.ProcessDescriptor;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @author Quentin Boileau (Geomatys)
 */
public class RemoveDataFromMapContextDescriptor extends AbstractCstlProcessDescriptor {
    
    public static final String NAME = "remove-data-from-map-context";

    public static final ParameterDescriptor<String> CONTEXT_PROVIDER_ID =
            new DefaultParameterDescriptor("context-provider", "The provider identifier of the coverages group", 
            String.class, null, true);
    
    public static final ParameterDescriptor<String> CONTEXT_NAME =
            new DefaultParameterDescriptor("context-name", "The name of the group to add the layer", 
            String.class, null, true);
    
    public static final ParameterDescriptor<String> DATA_NAME =
            new DefaultParameterDescriptor("data-name", "The name of the data to add int the group ", 
            String.class, null, true);
    
    public static final ParameterDescriptor<String> DATA_PROVIDER_ID =
            new DefaultParameterDescriptor("data-provider-id", "The identifier of the data provider", 
            String.class, null, true);

    /** 
     * Input Parameters 
     */
    public static final ParameterDescriptorGroup INPUT_DESC =
            new DefaultParameterDescriptorGroup("InputParameters",
            new GeneralParameterDescriptor[]{CONTEXT_PROVIDER_ID, CONTEXT_NAME, DATA_NAME, DATA_PROVIDER_ID});

    
     /** 
     * Output Parameters 
     */
    public static final ParameterDescriptorGroup OUTPUT_DESC =
            new DefaultParameterDescriptorGroup("OutputParameters");
    
    public static final ProcessDescriptor INSTANCE = new RemoveDataFromMapContextDescriptor();
    
    public RemoveDataFromMapContextDescriptor() {
         super(NAME, ConstellationProcessFactory.IDENTIFICATION,
                new SimpleInternationalString("Remove a layer to a coverages-group."),
                INPUT_DESC, OUTPUT_DESC);
    }

    @Override
    protected AbstractCstlProcess buildProcess(ParameterValueGroup input) {
        return new RemoveDataFromMapContext(input);
    }
    
}
