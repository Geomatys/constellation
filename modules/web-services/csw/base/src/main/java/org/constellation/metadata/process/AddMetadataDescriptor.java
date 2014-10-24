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

import java.io.File;
import org.apache.sis.util.iso.SimpleInternationalString;
import org.constellation.process.ConstellationProcessFactory;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.geotoolkit.process.AbstractProcessDescriptor;
import org.geotoolkit.process.ProcessDescriptor;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.InternationalString;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class AddMetadataDescriptor extends AbstractProcessDescriptor {
    
    public static final String NAME = "metadata.add";
    public static final InternationalString ABSTRACT = new SimpleInternationalString("Add a metadata to a CSW service.");


    public static final String SERVICE_IDENTIFIER_NAME = "service_identifier";
    private static final String SERVICE_IDENTIFIER_REMARKS = "the identifier of the CSW servicer.";
    public static final ParameterDescriptor<String> SERVICE_IDENTIFIER =
            new DefaultParameterDescriptor<>(SERVICE_IDENTIFIER_NAME, SERVICE_IDENTIFIER_REMARKS, String.class, null, true);
    
    public static final String METADATA_FILE_NAME = "metadata";
    private static final String METADATA_FILE_REMARKS = "The metadata to add to CSW.";
    public static final ParameterDescriptor<File> METADATA_FILE =
            new DefaultParameterDescriptor<>(METADATA_FILE_NAME, METADATA_FILE_REMARKS, File.class, null, true);
    
    public static final String METADATA_ID_NAME = "metadata-id";
    private static final String METADATA_ID_REMARKS = "The metadata identifier.";
    public static final ParameterDescriptor<String> METADATA_ID =
            new DefaultParameterDescriptor<>(METADATA_ID_NAME, METADATA_ID_REMARKS, String.class, null, true);
    
    public static final String REFRESH_NAME = "refresh";
    private static final String REFRESH_REMARKS = "If set this flag the CSW will be refreshed.";
    public static final ParameterDescriptor<Boolean> REFRESH =
            new DefaultParameterDescriptor<>(REFRESH_NAME, REFRESH_REMARKS, Boolean.class, true, true);
    
    /**Input parameters */
    public static final ParameterDescriptorGroup INPUT_DESC = new DefaultParameterDescriptorGroup("InputParameters",
            new GeneralParameterDescriptor[]{SERVICE_IDENTIFIER, METADATA_FILE, METADATA_ID, REFRESH});
    
     /**Output parameters */
    public static final ParameterDescriptorGroup OUTPUT_DESC = new DefaultParameterDescriptorGroup("OutputParameters");
    
    /**
     * Public constructor use by the ServiceRegistry to find and instantiate all ProcessDescriptor.
     */
    public AddMetadataDescriptor() {
        super(NAME, ConstellationProcessFactory.IDENTIFICATION, ABSTRACT, INPUT_DESC, OUTPUT_DESC);
    }

    public static final ProcessDescriptor INSTANCE = new AddMetadataDescriptor();
    
    @Override
    public org.geotoolkit.process.Process createProcess(ParameterValueGroup input) {
        return new AddMetadaProcess(this, input);
    }
}
