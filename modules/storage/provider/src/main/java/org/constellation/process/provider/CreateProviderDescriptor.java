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

import org.apache.sis.util.iso.SimpleInternationalString;
import org.constellation.process.AbstractCstlProcess;
import org.constellation.process.AbstractCstlProcessDescriptor;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.opengis.parameter.GeneralParameterDescriptor;
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

    /**
     * {@link org.constellation.provider.ProviderFactoryType}
     */
    public static final String PROVIDER_TYPE_NAME = "provider_type";
    private static final String PROVIDER_TYPE_REMARKS = "Provider factory name like 'feature-store', 'coverage-store', ... .";
    public static final ParameterDescriptor<String> PROVIDER_TYPE =
            new DefaultParameterDescriptor<String>(PROVIDER_TYPE_NAME, PROVIDER_TYPE_REMARKS, String.class, null, true);


    public static final String SOURCE_NAME = "parameters";
    private static final String SOURCE_REMARKS = "ParameterValueGroup use to create provider.";
    public static final ParameterDescriptor<ParameterValueGroup> SOURCE =
            new DefaultParameterDescriptor(SOURCE_NAME, SOURCE_REMARKS, ParameterValueGroup.class, null, true);
    
    public static final String DOMAIN_ID_NAME = "domain-id";
    private static final String DOMAIN_ID_REMARKS = "Identifier of the domain to add data.";
    public static final ParameterDescriptor<Integer> DOMAIN_ID =
            new DefaultParameterDescriptor(DOMAIN_ID_NAME, DOMAIN_ID_REMARKS, Integer.class, null, false);

    /**Input parameters */
    public static final ParameterDescriptorGroup INPUT_DESC =
            new DefaultParameterDescriptorGroup("InputParameters",
            new GeneralParameterDescriptor[]{PROVIDER_TYPE, SOURCE, DOMAIN_ID});


    /**Output parameters */
    public static final ParameterDescriptorGroup OUTPUT_DESC = new DefaultParameterDescriptorGroup("OutputParameters");

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
