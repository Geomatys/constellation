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

package org.constellation.metadata.process;

import org.apache.sis.parameter.ParameterBuilder;
import org.apache.sis.util.iso.SimpleInternationalString;
import org.constellation.process.ConstellationProcessFactory;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.processing.AbstractProcessDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.InternationalString;
import org.w3c.dom.Node;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class GetMetadataProcessDescriptor extends AbstractProcessDescriptor {
    
    public static final String NAME = "metadata.get";
    public static final InternationalString ABSTRACT = new SimpleInternationalString("Retrieve a metadata from a CSW service.");

    private static final ParameterBuilder BUILDER = new ParameterBuilder();

    public static final String SERVICE_IDENTIFIER_NAME = "service_identifier";
    private static final String SERVICE_IDENTIFIER_REMARKS = "the identifier of the CSW servicer.";
    public static final ParameterDescriptor<String> SERVICE_IDENTIFIER = BUILDER
            .addName(SERVICE_IDENTIFIER_NAME)
            .setRemarks(SERVICE_IDENTIFIER_REMARKS)
            .setRequired(true)
            .create(String.class, null);
    
    public static final String METADATA_ID_NAME = "metadata-id";
    private static final String METADATA_ID_REMARKS = "The metadata identifier.";
    public static final ParameterDescriptor<String> METADATA_ID = BUILDER
            .addName(METADATA_ID_NAME)
            .setRemarks(METADATA_ID_REMARKS)
            .setRequired(true)
            .create(String.class, null);
    
    /**Input parameters */
    public static final ParameterDescriptorGroup INPUT_DESC = BUILDER.addName("InputParameters").setRequired(true)
            .createGroup(SERVICE_IDENTIFIER, METADATA_ID);

    public static final String METADATA_NAME = "metadata";
    private static final String METADATA_REMARKS = "The metadata object (Node).";
    public static final ParameterDescriptor<Node> METADATA = BUILDER
            .addName(METADATA_NAME)
            .setRemarks(METADATA_REMARKS)
            .setRequired(false)
            .create(Node.class, null);

     /**Output parameters */
    public static final ParameterDescriptorGroup OUTPUT_DESC = BUILDER.addName("OutputParameters").setRequired(true)
             .createGroup(METADATA);

    /**
     * Public constructor use by the ServiceRegistry to find and instantiate all ProcessDescriptor.
     */
    public GetMetadataProcessDescriptor() {
        super(NAME, ConstellationProcessFactory.IDENTIFICATION, ABSTRACT, INPUT_DESC, OUTPUT_DESC);
    }

    public static final ProcessDescriptor INSTANCE = new GetMetadataProcessDescriptor();
    
    @Override
    public org.geotoolkit.process.Process createProcess(ParameterValueGroup input) {
        return new GetMetadataProcess(this, input);
    }
}
