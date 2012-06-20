/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.process.provider.create;

import org.constellation.process.ConstellationProcessFactory;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.geotoolkit.process.AbstractProcessDescriptor;
import org.geotoolkit.process.Process;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.util.SimpleInternationalString;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.InternationalString;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public class CreateProviderDescriptor extends AbstractProcessDescriptor {

    public static final String NAME = "createProvider";
    public static final InternationalString ABSTRACT = new SimpleInternationalString("Create a new provider in constellation.");
    
  
    private static final String SERVICE_NAME_NAME = "service_Name";
    private static final String SERVICE_NAME_REMARKS = "The name of the service.";
    public static final ParameterDescriptor<String> SERVICE_NAME = 
            new DefaultParameterDescriptor(SERVICE_NAME_NAME, SERVICE_NAME_REMARKS, String.class, null, true);

    
    
    private static final String SOURCE_NAME = "parameters";
    private static final String SOURCE_REMARKS = "ParameterValueGroup use to create provider.";
    public static final ParameterDescriptor<ParameterValueGroup> SOURCE =
            new DefaultParameterDescriptor(SOURCE_NAME, SOURCE_REMARKS, ParameterValueGroup.class, null, true);

    /**Input parameters */
    public static final ParameterDescriptorGroup INPUT_DESC =
            new DefaultParameterDescriptorGroup("InputParameters",
            new GeneralParameterDescriptor[]{SERVICE_NAME, SOURCE});

    
    /**Output parameters */
    public static final ParameterDescriptorGroup OUTPUT_DESC = new DefaultParameterDescriptorGroup("OutputParameters");
    
    public static final ProcessDescriptor INSTANCE = new CreateProviderDescriptor();
    
    private CreateProviderDescriptor() {
        super(NAME, ConstellationProcessFactory.IDENTIFICATION, ABSTRACT, INPUT_DESC, OUTPUT_DESC);
    }
    
    @Override
    public Process createProcess(ParameterValueGroup input) {
        return new CreateProvider(input);
    }
    
}
