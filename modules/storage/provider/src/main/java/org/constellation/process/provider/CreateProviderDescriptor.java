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
package org.constellation.process.provider;

import org.apache.sis.parameter.ParameterBuilder;
import org.apache.sis.util.iso.SimpleInternationalString;
import org.constellation.process.AbstractCstlProcess;
import org.constellation.process.AbstractCstlProcessDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.InternationalString;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public class CreateProviderDescriptor extends AbstractCstlProcessDescriptor {

    public static final String NAME = "provider.create";
    public static final InternationalString ABSTRACT = new SimpleInternationalString("Create a new provider in constellation.");

    private static final ParameterBuilder BUILDER = new ParameterBuilder();

    /**
     * {@link org.constellation.provider.ProviderFactoryType}
     */
    public static final String PROVIDER_TYPE_NAME = "provider_type";
    private static final String PROVIDER_TYPE_REMARKS = "Provider factory name like 'feature-store', 'coverage-store', ... .";
    public static final ParameterDescriptor<String> PROVIDER_TYPE = BUILDER
            .addName(PROVIDER_TYPE_NAME)
            .setRemarks(PROVIDER_TYPE_REMARKS)
            .setRequired(true)
            .create(String.class, null);


    public static final String SOURCE_NAME = "parameters";
    private static final String SOURCE_REMARKS = "ParameterValueGroup use to create provider.";
    public static final ParameterDescriptor<ParameterValueGroup> SOURCE = BUILDER
            .addName(SOURCE_NAME)
            .setRemarks(SOURCE_REMARKS)
            .setRequired(true)
            .create(ParameterValueGroup.class, null);

    public static final String DOMAIN_ID_NAME = "domain-id";
    private static final String DOMAIN_ID_REMARKS = "Identifier of the domain to add data.";
    public static final ParameterDescriptor<Integer> DOMAIN_ID = BUILDER
            .addName(DOMAIN_ID_NAME)
            .setRemarks(DOMAIN_ID_REMARKS)
            .setRequired(false)
            .create(Integer.class, null);

    /**Input parameters */
    public static final ParameterDescriptorGroup INPUT_DESC = BUILDER.addName("InputParameters").setRequired(true)
            .createGroup(PROVIDER_TYPE, SOURCE, DOMAIN_ID);


    /**Output parameters */
    public static final ParameterDescriptorGroup OUTPUT_DESC = new ParameterBuilder().addName("OutputParameters").setRequired(true)
            .createGroup();

    /**
     * Public constructor use by the ServiceRegistry to find and instantiate all ProcessDescriptor.
     */
    public CreateProviderDescriptor() {
        super(NAME, ProviderDescriptorConstant.IDENTIFICATION_CSTL, ABSTRACT, INPUT_DESC, OUTPUT_DESC);
    }

    public static final CreateProviderDescriptor INSTANCE = new CreateProviderDescriptor();

    @Override
    public AbstractCstlProcess buildProcess(ParameterValueGroup input) {
        return new CreateProvider(this, input);
    }

}
