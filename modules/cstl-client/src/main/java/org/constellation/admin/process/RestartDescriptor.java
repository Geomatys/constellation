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

import org.apache.sis.util.iso.SimpleInternationalString;
import org.constellation.admin.service.ConstellationServerFactory;
import org.constellation.process.ConstellationProcessFactory;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.geotoolkit.process.AbstractProcessDescriptor;
import org.geotoolkit.process.Process;
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
    
    // Constellation
    public static final GeneralParameterDescriptor CSTL_WS_INSTANCE = new DefaultParameterDescriptor("WSInstance",
            "Name of the WebService instance.",String.class,"default", true);
    public static final GeneralParameterDescriptor CSTL_WS_TYPE = new DefaultParameterDescriptor("WSType",
            "Type of the WebService.",String.class,null, true);
    public static final ParameterDescriptorGroup CSTL_DESCRIPTOR_GROUP =
            new DefaultParameterDescriptorGroup("Constellation",
                                                 ConstellationServerFactory.URL,
                                                 ConstellationServerFactory.USER,
                                                 ConstellationServerFactory.PASSWORD,
                                                 CSTL_WS_INSTANCE,
                                                 CSTL_WS_TYPE);
    
    /**Input parameters */
    public static final ParameterDescriptorGroup INPUT_DESC = new DefaultParameterDescriptorGroup("InputParameters", CSTL_DESCRIPTOR_GROUP);

    /** Output parameters : nothing */
    public static final ParameterDescriptorGroup OUTPUT_DESC = new DefaultParameterDescriptorGroup("OutputParameters");

    
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
