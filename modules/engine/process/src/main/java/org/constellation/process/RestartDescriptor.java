/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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
package org.constellation.process;

import org.constellation.admin.service.ConstellationServerFactory;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.Process;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.geotoolkit.process.AbstractProcessDescriptor;
import org.geotoolkit.util.SimpleInternationalString;

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

    /** Instance */
    public static final ProcessDescriptor INSTANCE = new RestartDescriptor();

    private RestartDescriptor() {
        super(NAME, ConstellationProcessFactory.IDENTIFICATION,
                new SimpleInternationalString("Restart constellation web service instance"), 
                INPUT_DESC, OUTPUT_DESC);
    }

    /**
     *  {@inheritDoc }
     */
    @Override
    public Process createProcess(ParameterValueGroup pvg) {
        return new Restart(pvg);
    }
}
