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

import org.apache.sis.parameter.ParameterBuilder;
import org.apache.sis.util.iso.SimpleInternationalString;
import org.constellation.process.AbstractCstlProcess;
import org.constellation.process.AbstractCstlProcessDescriptor;
import org.constellation.process.ConstellationProcessFactory;
import org.geotoolkit.process.ProcessDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @author Quentin Boileau (Geomatys)
 */
public class RemoveDataFromMapContextDescriptor extends AbstractCstlProcessDescriptor {
    
    public static final String NAME = "remove-data-from-map-context";

    protected static final ParameterBuilder BUILDER = new ParameterBuilder();

    public static final ParameterDescriptor<String> CONTEXT_PROVIDER_ID = BUILDER
            .addName("context-provider")
            .setRemarks("The provider identifier of the coverages group")
            .setRequired(true)
            .create(String.class, null);

    public static final ParameterDescriptor<String> CONTEXT_NAME = BUILDER
            .addName("context-name")
            .setRemarks("The name of the group to add the layer")
            .setRequired(true)
            .create(String.class, null);

    public static final ParameterDescriptor<String> DATA_NAME = BUILDER
            .addName("data-name")
            .setRemarks("The name of the data to add int the group ")
            .setRequired(true)
            .create(String.class, null);

    public static final ParameterDescriptor<String> DATA_PROVIDER_ID = BUILDER
            .addName("data-provider-id")
            .setRemarks( "The identifier of the data provider")
            .setRequired(true)
            .create(String.class, null);

    /**Input parameters */
    public static final ParameterDescriptorGroup INPUT_DESC = BUILDER.addName("InputParameters").setRequired(true)
            .createGroup(CONTEXT_PROVIDER_ID, CONTEXT_NAME, DATA_NAME, DATA_PROVIDER_ID);
    
    /**Output parameters */
    public static final ParameterDescriptorGroup OUTPUT_DESC =  BUILDER.addName("OutputParameters").setRequired(true)
            .createGroup();


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
