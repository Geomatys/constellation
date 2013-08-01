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
package org.constellation.process.refreshindex;

import org.constellation.admin.service.ConstellationServerFactory;
import org.constellation.process.ConstellationProcessFactory;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.process.Process;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.geotoolkit.process.AbstractProcessDescriptor;
import org.apache.sis.util.iso.SimpleInternationalString;

import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class RefreshIndexDescriptor extends AbstractProcessDescriptor {
 
    public static final String NAME = "refreshIndex";
    
    // Constellation
    public static final GeneralParameterDescriptor CSTL_CSW_INSTANCE = new DefaultParameterDescriptor("CSWInstance",
            "Name of the CSW instance.",String.class,"default", true);
    public static final GeneralParameterDescriptor CSTL_ASYNCHRONOUS = new DefaultParameterDescriptor("Asynchronous",
            "Falg for asynchrone refresh index mode.", Boolean.class, Boolean.TRUE, true);
    public static final ParameterDescriptorGroup CSTL_DESCRIPTOR_GROUP =
            new DefaultParameterDescriptorGroup("Constellation",
                                                 ConstellationServerFactory.URL,
                                                 ConstellationServerFactory.USER,
                                                 ConstellationServerFactory.PASSWORD,
                                                 CSTL_CSW_INSTANCE,
                                                 CSTL_ASYNCHRONOUS);
    
    /**Input parameters */
    public static final ParameterDescriptorGroup INPUT_DESC = new DefaultParameterDescriptorGroup("InputParameters", CSTL_DESCRIPTOR_GROUP);

    /** Output parameters : nothing */
    public static final ParameterDescriptorGroup OUTPUT_DESC = new DefaultParameterDescriptorGroup("OutputParameters");

    /**
     * Public constructor use by the ServiceRegistry to find and intanciate all ProcessDescriptor.
     */
    public RefreshIndexDescriptor() {
        super(NAME, ConstellationProcessFactory.IDENTIFICATION,
                new SimpleInternationalString("Refresh CSW lucene Index"), 
                INPUT_DESC, OUTPUT_DESC);
    }

    /**
     *  {@inheritDoc }
     */
    @Override
    public Process createProcess(ParameterValueGroup pvg) {
        return new RefreshIndex(this, pvg);
    }
}
