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
package org.constellation.admin.process;

import org.apache.sis.parameter.ParameterBuilder;
import org.apache.sis.util.iso.SimpleInternationalString;
import org.constellation.admin.service.ConstellationServerFactory;
import org.constellation.process.ConstellationProcessFactory;
import org.geotoolkit.process.Process;
import org.geotoolkit.processing.AbstractProcessDescriptor;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;


/**
 * restart a constellation WebService instance.
 * 
 * @author Guilhem Legal (Geomatys) 
 */
public final class RestartDescriptor extends AbstractProcessDescriptor {

    public static final String NAME = "restart";

    private static final ParameterBuilder BUILDER = new ParameterBuilder();

    // Constellation
    public static final GeneralParameterDescriptor CSTL_WS_INSTANCE = BUILDER
            .addName("WSInstance")
            .setRemarks("Name of the WebService instance.")
            .setRequired(true)
            .create(String.class, "default");

    public static final GeneralParameterDescriptor CSTL_WS_TYPE = BUILDER
            .addName("WSType")
            .setRemarks("Type of the WebService.")
            .setRequired(true)
            .create(String.class, null);

    public static final ParameterDescriptorGroup CSTL_DESCRIPTOR_GROUP = BUILDER.addName("Constellation").setRequired(true)
            .createGroup(ConstellationServerFactory.URL,
                    ConstellationServerFactory.USER,
                    ConstellationServerFactory.PASSWORD,
                    CSTL_WS_INSTANCE,
                    CSTL_WS_TYPE);

    /**Input parameters */
    public static final ParameterDescriptorGroup INPUT_DESC = BUILDER.addName("InputParameters").setRequired(true)
            .createGroup(CSTL_DESCRIPTOR_GROUP);

    /** Output parameters : nothing */
    public static final ParameterDescriptorGroup OUTPUT_DESC = BUILDER.addName("OutputParameters").setRequired(true)
            .createGroup();

    
    /**
     * Public constructor use by the ServiceRegistry to find and intanciate all ProcessDescriptor.
     */
    public RestartDescriptor() {
        super(NAME, ConstellationProcessFactory.IDENTIFICATION,
                new SimpleInternationalString("Restart constellation web service instance"), 
                INPUT_DESC, OUTPUT_DESC);
    }

    /**
     *  {@inheritDoc }
     */
    @Override
    public Process createProcess(ParameterValueGroup pvg) {
        return new Restart(this, pvg);
    }
}
