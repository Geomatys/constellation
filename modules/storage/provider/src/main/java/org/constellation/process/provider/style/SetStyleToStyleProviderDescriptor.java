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
import org.geotoolkit.style.MutableStyle;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.InternationalString;

/**
 * Add a style to an existing style provider.
 *
 * @author Quentin Boileau (Geomatys).
 */
public class SetStyleToStyleProviderDescriptor extends AbstractProcessDescriptor {

    public static final String NAME = "style_provider.set_style";
    public static final InternationalString ABSTRACT = new SimpleInternationalString("Add a style to an exising StyleProvider."
            + "If style name already exist, style will be update.");

    private static final ParameterBuilder BUILDER = new ParameterBuilder();
    /*
     * StyleProvider identifier
     */
    public static final String PROVIDER_ID_NAME = "provider_id";
    private static final String PROVIDER_ID_REMARKS = "Identifier of the StyleProvider where the new style will be added.";
    public static final ParameterDescriptor<String> PROVIDER_ID = BUILDER
            .addName(PROVIDER_ID_NAME)
            .setRemarks(PROVIDER_ID_REMARKS)
            .setRequired(true)
            .create(String.class, null);

    /*
     * Style name.
     */
    public static final String STYLE_ID_NAME = "style_Name";
    private static final String STYLE_ID_REMARKS = "Name/Identifier of the new style.";
    public static final ParameterDescriptor<String> STYLE_ID = BUILDER
            .addName(STYLE_ID_NAME)
            .setRemarks(STYLE_ID_REMARKS)
            .setRequired(false)
            .create(String.class, null);

    /*
     * Style
     */
    public static final String STYLE_NAME = "style";
    private static final String STYLE_REMARKS = "Style to add.";
    public static final ParameterDescriptor<MutableStyle> STYLE = BUILDER
            .addName(STYLE_NAME)
            .setRemarks(STYLE_REMARKS)
            .setRequired(true)
            .create(MutableStyle.class, null);

    /*
     * Owner
     */
    public static final String OWNER_NAME = "owner";
    private static final String OWNER_REMARKS = "The style owner login. Can be null.";
    public static final ParameterDescriptor<String> OWNER = BUILDER
            .addName(OWNER_NAME)
            .setRemarks(OWNER_REMARKS)
            .setRequired(false)
            .create(String.class, null);

    /**
     * Input parameters
     */
    public static final ParameterDescriptorGroup INPUT_DESC = BUILDER.addName("InputParameters").setRequired(true)
            .createGroup(PROVIDER_ID, STYLE_ID, STYLE, OWNER);
    /**
     * Output parameters
     */
    public static final ParameterDescriptorGroup OUTPUT_DESC = BUILDER.addName("OutputParameters").setRequired(true)
            .createGroup();

    /**
     * Public constructor use by the ServiceRegistry to find and instantiate all ProcessDescriptor.
     */
    public SetStyleToStyleProviderDescriptor() {
        super(NAME, ProviderDescriptorConstant.IDENTIFICATION_CSTL, ABSTRACT, INPUT_DESC, OUTPUT_DESC);
    }

    @Override
    public org.geotoolkit.process.Process createProcess(ParameterValueGroup input) {
        return new SetStyleToStyleProvider(this, input);
    }
}
