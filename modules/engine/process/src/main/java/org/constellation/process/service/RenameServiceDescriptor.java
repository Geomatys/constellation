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
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.InternationalString;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class RenameServiceDescriptor extends AbstractCstlProcessDescriptor {

    public static final String NAME = "service.rename";
    public static final InternationalString ABSTRACT = new SimpleInternationalString("rename a new ogc service in constellation.");

    private static final ParameterBuilder BUILDER = new ParameterBuilder();

    public static final String SERVICE_TYPE_NAME = "service_type";
    private static final String SERVICE_TYPE_REMARKS = "The type of the service WMS, WFS, WMTS, WCS.";
    private static final String[] SERVICE_TYPE_VALID_VALUES = ServiceProcessCommon.servicesAvaible();
    public static final ParameterDescriptor<String> SERVICE_TYPE = BUILDER
            .addName(SERVICE_TYPE_NAME)
            .setRemarks(SERVICE_TYPE_REMARKS)
            .setRequired(true)
            .createEnumerated(String.class, SERVICE_TYPE_VALID_VALUES, null);

    public static final String IDENTIFIER_NAME = "identifier";
    private static final String IDENTIFIER_REMARKS = "Identifier of the new service instance.";
    public static final ParameterDescriptor<String> IDENTIFIER = BUILDER
            .addName(IDENTIFIER_NAME)
            .setRemarks(IDENTIFIER_REMARKS)
            .setRequired(true)
            .create( String.class, null);

    public static final String NEW_NAME_NAME = "newName";
    private static final String NEW_NAME_REMARKS = "new name of the service instance.";
    public static final ParameterDescriptor<String> NEW_NAME =  BUILDER
            .addName(NEW_NAME_NAME)
            .setRemarks(NEW_NAME_REMARKS)
            .setRequired(true)
            .create( String.class, null);

    /**Input parameters */
    public static final ParameterDescriptorGroup INPUT_DESC = BUILDER.addName("InputParameters").setRequired(true)
            .createGroup(SERVICE_TYPE, IDENTIFIER, NEW_NAME);

    /**Output parameters */
    public static final ParameterDescriptorGroup OUTPUT_DESC = BUILDER.addName("OutputParameters").setRequired(true)
            .createGroup();


    /**
     * Public constructor use by the ServiceRegistry to find and instantiate all ProcessDescriptor.
     */
    public RenameServiceDescriptor() {
        super(NAME, ConstellationProcessFactory.IDENTIFICATION, ABSTRACT, INPUT_DESC, OUTPUT_DESC);
    }

    @Override
    public AbstractCstlProcess buildProcess(ParameterValueGroup input) {
        return new RenameService(this, input);
    }
}
