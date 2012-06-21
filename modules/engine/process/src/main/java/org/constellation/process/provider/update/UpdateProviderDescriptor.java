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
package org.constellation.process.provider.update;

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
public class UpdateProviderDescriptor extends AbstractProcessDescriptor {

    public static final String NAME = "updateProvider";
    public static final InternationalString ABSTRACT = new SimpleInternationalString("Update a provider from constellation.");
    
    /*
     * Provider to update identifier
     */
    private static final String PROVIDER_ID_NAME = "provider_id";
    private static final String PROVIDER_ID_REMARKS = "Identifier of the provider to remove.";
    public static final ParameterDescriptor<String> PROVIDER_ID = 
            new DefaultParameterDescriptor(PROVIDER_ID_NAME, PROVIDER_ID_REMARKS, String.class, null, true);
    
    /*
     * Source use to update.
     */
    private static final String SOURCE_NAME = "source";
    private static final String SOURCE_REMARKS = "ParameterValueGroup use to update provider source.";
    public static final ParameterDescriptor<ParameterValueGroup> SOURCE =
            new DefaultParameterDescriptor(SOURCE_NAME, SOURCE_REMARKS, ParameterValueGroup.class, null, true);

    /**Input parameters */
    public static final ParameterDescriptorGroup INPUT_DESC =
            new DefaultParameterDescriptorGroup("InputParameters",
            new GeneralParameterDescriptor[]{PROVIDER_ID, SOURCE});

    
    /**Output parameters */
    public static final ParameterDescriptorGroup OUTPUT_DESC = new DefaultParameterDescriptorGroup("OutputParameters");
    
    /**
     * Public constructor use by the ServiceRegistry to find and intanciate all ProcessDescriptor.
     */
    public UpdateProviderDescriptor() {
        super(NAME, ConstellationProcessFactory.IDENTIFICATION, ABSTRACT, INPUT_DESC, OUTPUT_DESC);
    }
    
    @Override
    public Process createProcess(ParameterValueGroup input) {
        return new UpdateProvider(this, input);
    }
    
}
