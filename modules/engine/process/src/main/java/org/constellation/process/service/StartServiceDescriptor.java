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
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.util.InternationalString;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public final class StartServiceDescriptor extends AbstractCstlProcessDescriptor {

    public static final String NAME = "service.start";
    public static final InternationalString ABSTRACT = new SimpleInternationalString("Start a new instance for the specified service identifier.");

    public static final String SERVICE_TYPE_NAME = "service_type";
    private static final String SERVICE_TYPE_REMARKS = "The type of the service.";
    private static final Map<String, Object> SERVICE_TYPE_PROPERTIES;
    private static final String[] SERVICE_TYPE_VALID_VALUES = ServiceProcessCommon.servicesAvaible();
    static {
        SERVICE_TYPE_PROPERTIES = new HashMap<>();
        SERVICE_TYPE_PROPERTIES.put(IdentifiedObject.NAME_KEY, SERVICE_TYPE_NAME);
        SERVICE_TYPE_PROPERTIES.put(IdentifiedObject.REMARKS_KEY, SERVICE_TYPE_REMARKS);
    }
    public static final ParameterDescriptor<String> SERVICE_TYPE =
            new DefaultParameterDescriptor(SERVICE_TYPE_PROPERTIES, String.class, SERVICE_TYPE_VALID_VALUES, null, null, null, null, true);

    public static final String IDENTIFIER_NAME = "identifier";
    private static final String IDENTIFIER_REMARKS = "Identifier of the new service instance.";
    public static final ParameterDescriptor<String> IDENTIFIER =
            new DefaultParameterDescriptor(IDENTIFIER_NAME, IDENTIFIER_REMARKS, String.class, null, true);

    /**Input parameters */
    public static final ParameterDescriptorGroup INPUT_DESC =
            new DefaultParameterDescriptorGroup("InputParameters",
            new GeneralParameterDescriptor[]{SERVICE_TYPE, IDENTIFIER});

    /**Output parameters */
    public static final ParameterDescriptorGroup OUTPUT_DESC = new DefaultParameterDescriptorGroup("OutputParameters");


    /**
     * Public constructor use by the ServiceRegistry to find and instantiate all ProcessDescriptor.
     */
    public StartServiceDescriptor() {
        super(NAME, ConstellationProcessFactory.IDENTIFICATION, ABSTRACT, INPUT_DESC, OUTPUT_DESC);
    }

    @Override
    public AbstractCstlProcess buildProcess(final ParameterValueGroup input) {
        return new StartService(this, input);
    }

}
