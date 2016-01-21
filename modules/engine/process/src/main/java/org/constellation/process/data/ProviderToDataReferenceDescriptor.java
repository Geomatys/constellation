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

package org.constellation.process.data;

import org.apache.sis.parameter.ParameterBuilder;
import org.apache.sis.util.iso.ResourceInternationalString;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.util.DataReference;
import org.geotoolkit.processing.AbstractProcessDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.InternationalString;

import java.util.Date;

/**
 *
 * @author Quentin Boileau (Geomatys)
 */
public class ProviderToDataReferenceDescriptor extends AbstractProcessDescriptor {

    /*
     * Bundle path and keys
     */
    private static final String BUNDLE = "org/constellation/process/data/bundle";
    private static final String PROVIDER_TO_DATAREFERENCE_ABSTRACT_KEY   = "data.provider_to_data_reference_Abstract";
    private static final String PROVIDER_TO_DATAREFERENCE_DISPNAME_KEY   = "data.provider_to_data_reference_DispName";
    private static final String PROVIDER_ID_PARAM_REMARKS_KEY            = "data.provider_to_data_reference_Provider_id_Param";
    private static final String PROVIDER_TYPE_PARAM_REMARKS_KEY          = "data.provider_to_data_reference_Provider_type_Param";
    private static final String LAYER_ID_PARAM_REMARKS_KEY               = "data.provider_to_data_reference_Layer_id_Param";
    private static final String VERSION_PARAM_REMARKS_KEY                = "data.provider_to_data_reference_Version_Param";
    private static final String DATA_REFERENCE_PARAM_REMARKS_KEY         = "data.provider_to_data_reference_Data_Reference_Param";

    /*
     * Name and description
     */
    private static final String NAME = "data.provider_to_data_reference";
    private static final InternationalString DISPLAY_NAME = new ResourceInternationalString(BUNDLE, PROVIDER_TO_DATAREFERENCE_DISPNAME_KEY);
    public static final InternationalString ABSTRACT = new ResourceInternationalString(BUNDLE, PROVIDER_TO_DATAREFERENCE_ABSTRACT_KEY);

    private static final ParameterBuilder BUILDER = new ParameterBuilder();

    /*
     * Provider ID parameter
     */
    public static final String PROVIDER_ID_PARAM_NAME = "providerId";
    public static final InternationalString PROVIDER_ID_PARAM_REMARKS = new ResourceInternationalString(BUNDLE, PROVIDER_ID_PARAM_REMARKS_KEY);
    public static final ParameterDescriptor<String> PROVIDER_ID  = BUILDER
            .addName(PROVIDER_ID_PARAM_NAME)
            .setRemarks(PROVIDER_ID_PARAM_REMARKS.toString())
            .setRequired(true)
            .create(String.class, null);

    /*
    * Provider Type parameter
    */
    public static final String PROVIDER_TYPE_PARAM_NAME = "providerType";
    public static final InternationalString PROVIDER_TYPE_PARAM_REMARKS = new ResourceInternationalString(BUNDLE, PROVIDER_TYPE_PARAM_REMARKS_KEY);
    private static final String[] PROVIDER_TYPE_VALID_VALUES = new String [] {"layer", "style"};
    public static final ParameterDescriptor<String> PROVIDER_TYPE = BUILDER
             .addName(PROVIDER_TYPE_PARAM_NAME)
             .setRemarks(PROVIDER_TYPE_PARAM_REMARKS)
             .setRequired(true)
             .createEnumerated(String.class, PROVIDER_TYPE_VALID_VALUES, "layer");

    /*
     * Layer ID parameter
     */
    public static final String LAYER_ID_PARAM_NAME = "layerId";
    public static final InternationalString LAYER_ID_PARAM_REMARKS = new ResourceInternationalString(BUNDLE, LAYER_ID_PARAM_REMARKS_KEY);
    public static final ParameterDescriptor<String> LAYER_ID  = BUILDER
            .addName(LAYER_ID_PARAM_NAME)
            .setRemarks(LAYER_ID_PARAM_REMARKS.toString())
            .setRequired(true)
            .create(String.class, null);

     /*
     * Layer ID parameter
     */
    public static final String VERSION_PARAM_NAME = "version";
    public static final InternationalString VERSION_PARAM_REMARKS = new ResourceInternationalString(BUNDLE, VERSION_PARAM_REMARKS_KEY);
    public static final ParameterDescriptor<Date> VERSION  = BUILDER
            .addName(VERSION_PARAM_NAME)
            .setRemarks(VERSION_PARAM_REMARKS.toString())
            .setRequired(false)
            .create(Date.class, null);

    /**Input parameters */
    public static final ParameterDescriptorGroup INPUT_DESC = BUILDER.addName("InputParameters").setRequired(true)
            .createGroup(PROVIDER_ID, PROVIDER_TYPE, LAYER_ID, VERSION);


    /*
     * LayerMap output parameter
     */
    public static final String DATA_REFERENCE_PARAM_NAME = "dataReference";
    public static final InternationalString DATA_REFERENCE_PARAM_REMARKS = new ResourceInternationalString(BUNDLE, DATA_REFERENCE_PARAM_REMARKS_KEY);
    public static final ParameterDescriptor<DataReference> DATA_REFERENCE = BUILDER
            .addName(DATA_REFERENCE_PARAM_NAME)
            .setRemarks(DATA_REFERENCE_PARAM_REMARKS.toString())
            .setRequired(true)
            .create(DataReference.class, null);

    /**Output parameters */
    public static final ParameterDescriptorGroup OUTPUT_DESC = BUILDER.addName("OutputParameters").setRequired(true)
            .createGroup(DATA_REFERENCE);


    public static final ProviderToDataReferenceDescriptor INSTANCE = new ProviderToDataReferenceDescriptor();

    public ProviderToDataReferenceDescriptor() {
        super(NAME, ConstellationProcessFactory.IDENTIFICATION ,ABSTRACT, DISPLAY_NAME, INPUT_DESC, OUTPUT_DESC);
    }

    @Override
    public org.geotoolkit.process.Process createProcess(ParameterValueGroup input) {
        return new ProviderToDataReference(input);
    }
}
