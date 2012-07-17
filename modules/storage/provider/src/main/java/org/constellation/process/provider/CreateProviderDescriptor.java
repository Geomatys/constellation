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
package org.constellation.process.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.constellation.process.ConstellationProcessFactory;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.geotoolkit.process.AbstractProcessDescriptor;
import org.geotoolkit.process.Process;
import org.geotoolkit.util.SimpleInternationalString;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.util.InternationalString;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public class CreateProviderDescriptor extends AbstractProcessDescriptor {

    public static final String NAME = "provider.create";
    public static final InternationalString ABSTRACT = new SimpleInternationalString("Create a new provider in constellation.");


    public static final String PROVIDER_TYPE_NAME = "provider_type";
    private static final String PROVIDER_TYPE_REMARKS = "The type of he provider like 'data-store', 'coverage-store', ... .";
    private static final Map<String, Object> PROVIDER_TYPE_PROPERTIES;
    private static final String[] PROVIDER_TYPE_VALID_VALUES;
    static {
        PROVIDER_TYPE_PROPERTIES = new HashMap<String, Object>();
        PROVIDER_TYPE_PROPERTIES.put(IdentifiedObject.NAME_KEY, PROVIDER_TYPE_NAME);
        PROVIDER_TYPE_PROPERTIES.put(IdentifiedObject.REMARKS_KEY, PROVIDER_TYPE_REMARKS);

        final List<String> validValues = new ArrayList<String>();
        for (ProviderType providerType : ProviderType.values()) {
            validValues.add(providerType.getCode());
        }
        PROVIDER_TYPE_VALID_VALUES = validValues.toArray(new String[validValues.size()]);
    }
    public static final ParameterDescriptor<String> PROVIDER_TYPE =
            new DefaultParameterDescriptor(PROVIDER_TYPE_PROPERTIES, String.class, PROVIDER_TYPE_VALID_VALUES, ProviderType.DATA_STORE.getCode(), null, null, null, true);



    public static final String SOURCE_NAME = "parameters";
    private static final String SOURCE_REMARKS = "ParameterValueGroup use to create provider.";
    public static final ParameterDescriptor<ParameterValueGroup> SOURCE =
            new DefaultParameterDescriptor(SOURCE_NAME, SOURCE_REMARKS, ParameterValueGroup.class, null, true);

    /**Input parameters */
    public static final ParameterDescriptorGroup INPUT_DESC =
            new DefaultParameterDescriptorGroup("InputParameters",
            new GeneralParameterDescriptor[]{PROVIDER_TYPE, SOURCE});


    /**Output parameters */
    public static final ParameterDescriptorGroup OUTPUT_DESC = new DefaultParameterDescriptorGroup("OutputParameters");

    /**
     * Public constructor use by the ServiceRegistry to find and instantiate all ProcessDescriptor.
     */
    public CreateProviderDescriptor() {
        super(NAME, ConstellationProcessFactory.IDENTIFICATION, ABSTRACT, INPUT_DESC, OUTPUT_DESC);
    }

    @Override
    public Process createProcess(ParameterValueGroup input) {
        return new CreateProvider(this, input);
    }

}
