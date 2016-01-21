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
package org.constellation.process.provider.style;

import org.apache.sis.parameter.ParameterBuilder;
import org.apache.sis.util.iso.SimpleInternationalString;
import org.constellation.process.provider.ProviderDescriptorConstant;
import org.geotoolkit.processing.AbstractProcessDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.InternationalString;

/**
 * Remove a style from an existing style provider.
 *
 * @author Quentin Boileau (Geomatys).
 */
public class DeleteStyleToStyleProviderDescriptor extends AbstractProcessDescriptor {

    public static final String NAME = "style_provider.delete_style";
    public static final InternationalString ABSTRACT = new SimpleInternationalString("Remove a style from an existing StyleProvider.");

    private static final ParameterBuilder BUILDER = new ParameterBuilder();

    /*
     * StyleProvider identifier
     */
    public static final String PROVIDER_ID_NAME = "provider_id";
    private static final String PROVIDER_ID_REMARKS = "Identifier of the StyleProvider where the style will be deleted.";
    public static final ParameterDescriptor<String> PROVIDER_ID = BUILDER
            .addName(PROVIDER_ID_NAME)
            .setRemarks(PROVIDER_ID_REMARKS)
            .setRequired(true)
            .create(String.class, null);

    /*
     * Style name.
     */
    public static final String STYLE_ID_NAME = "style_id";
    private static final String STYLE_ID_REMARKS = "Name/Identifier of the style to remove.";
    public static final ParameterDescriptor<String> STYLE_ID = BUILDER
            .addName(STYLE_ID_NAME)
            .setRemarks(STYLE_ID_REMARKS)
            .setRequired(true)
            .create(String.class, null);

    /**
     * Input parameters
     */
    public static final ParameterDescriptorGroup INPUT_DESC = BUILDER.addName("InputParameters").setRequired(true)
            .createGroup(PROVIDER_ID, STYLE_ID);
    /**
     * Output parameters
     */
    public static final ParameterDescriptorGroup OUTPUT_DESC = BUILDER.addName("OutputParameters").setRequired(true)
            .createGroup();

    /**
     * Public constructor use by the ServiceRegistry to find and instantiate all ProcessDescriptor.
     */
    public DeleteStyleToStyleProviderDescriptor() {
        super(NAME, ProviderDescriptorConstant.IDENTIFICATION_CSTL, ABSTRACT, INPUT_DESC, OUTPUT_DESC);
    }

    @Override
    public org.geotoolkit.process.Process createProcess(ParameterValueGroup input) {
        return new DeleteStyleToStyleProvider(this, input);
    }
}
