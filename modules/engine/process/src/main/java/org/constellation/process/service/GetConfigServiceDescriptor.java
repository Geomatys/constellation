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

import static org.constellation.process.service.ServiceProcessCommon.SUPPORTED_SERVICE_TYPE;
/**
 *
 * @author Quentin Boileau (Geoamtys)
 */
public class GetConfigServiceDescriptor extends AbstractCstlProcessDescriptor {


    public static final String NAME = "service.get_config";
    public static final InternationalString ABSTRACT = new SimpleInternationalString("Get configuration of an existing map service (WMS, WMTS, WFS, WCS) in constellation.");

    private static final ParameterBuilder BUILDER = new ParameterBuilder();

    public static final String SERVICE_TYPE_NAME = "service_type";
    private static final String SERVICE_TYPE_REMARKS = "The type of the service WMS, WFS, WMTS, WCS.";
    private static final String[] SERVICE_TYPE_VALID_VALUES = SUPPORTED_SERVICE_TYPE.toArray(new String[SUPPORTED_SERVICE_TYPE.size()]);
    public static final ParameterDescriptor<String> SERVICE_TYPE = BUILDER
            .addName(SERVICE_TYPE_NAME)
            .setRemarks(SERVICE_TYPE_REMARKS)
            .setRequired(true)
            .createEnumerated(String.class, SERVICE_TYPE_VALID_VALUES, null);

    public static final String IDENTIFIER_NAME = "identifier";
    private static final String IDENTIFIER_REMARKS = "Identifier of the service instance.";
    public static final ParameterDescriptor<String> IDENTIFIER = BUILDER
            .addName(IDENTIFIER_NAME)
            .setRemarks(IDENTIFIER_REMARKS)
            .setRequired(true)
            .create(String.class, "default");

    /**Input parameters */
    public static final ParameterDescriptorGroup INPUT_DESC = BUILDER.addName("InputParameters").setRequired(true)
            .createGroup(SERVICE_TYPE, IDENTIFIER);


    public static final String CONFIG_NAME = "configuration";
    private static final String CONFIG_REMARKS = "The configuration object for the specified service instance.";
    public static final ParameterDescriptor<Object> CONFIGURATION = BUILDER
            .addName(CONFIG_NAME)
            .setRemarks(CONFIG_REMARKS)
            .setRequired(false)
            .create(Object.class, null);

    /**Output parameters */
    public static final ParameterDescriptorGroup OUTPUT_DESC = BUILDER.addName("OutputParameters").setRequired(true)
            .createGroup(CONFIGURATION);
    /**
     * Public constructor use by the ServiceRegistry to find and instantiate all ProcessDescriptor.
     */
    public GetConfigServiceDescriptor() {
        super(NAME, ConstellationProcessFactory.IDENTIFICATION, ABSTRACT, INPUT_DESC, OUTPUT_DESC);
    }

    @Override
    public AbstractCstlProcess buildProcess(ParameterValueGroup input) {
        return new GetConfigService(this, input);
    }

}
