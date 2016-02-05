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
import org.geotoolkit.process.Process;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.processing.AbstractProcessDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.InternationalString;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public class DeleteProviderDescriptor extends AbstractProcessDescriptor {

    public static final String NAME = "provider.delete";
    public static final InternationalString ABSTRACT = new SimpleInternationalString("Delete a provider from constellation.");

    private static final ParameterBuilder BUILDER = new ParameterBuilder();

    public static final String PROVIDER_ID_NAME = "provider_id";
    private static final String PROVIDER_ID_REMARKS = "Identifier of the provider to remove.";
    public static final ParameterDescriptor<String> PROVIDER_ID = BUILDER
            .addName(PROVIDER_ID_NAME)
            .setRemarks(PROVIDER_ID_REMARKS)
            .setRequired(true)
            .create(String.class, null);

    public static final String DELETE_DATA_NAME = "delete_data";
    private static final String DELETE_DATA_REMARKS = "Delete data.";
    public static final ParameterDescriptor<Boolean> DELETE_DATA = BUILDER
            .addName(DELETE_DATA_NAME)
            .setRemarks(DELETE_DATA_REMARKS)
            .setRequired(false)
            .create(Boolean.class, null);

    /**Input parameters */
    public static final ParameterDescriptorGroup INPUT_DESC = BUILDER.addName("InputParameters").setRequired(true)
            .createGroup(PROVIDER_ID, DELETE_DATA);


    /**Output parameters */
    public static final ParameterDescriptorGroup OUTPUT_DESC = BUILDER.addName("OutputParameters").setRequired(true)
            .createGroup();

    /**
     * Public constructor use by the ServiceRegistry to find and instantiate all ProcessDescriptor.
     */
    public DeleteProviderDescriptor() {
        super(NAME, ProviderDescriptorConstant.IDENTIFICATION_CSTL, ABSTRACT, INPUT_DESC, OUTPUT_DESC);
    }

    public static final ProcessDescriptor INSTANCE = new DeleteProviderDescriptor();

    @Override
    public Process createProcess(ParameterValueGroup input) {
        return new DeleteProvider(this, input);
    }

}
