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
package org.constellation.coverage.process;

import java.io.File;
import org.apache.sis.util.iso.SimpleInternationalString;
import org.constellation.process.ConstellationProcessFactory;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.geotoolkit.process.AbstractProcessDescriptor;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.InternationalString;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class PyramidCoverageDescriptor extends AbstractProcessDescriptor {
    
    public static final String NAME = "coverage.pyramid";
    public static final InternationalString ABSTRACT = new SimpleInternationalString("Build a pyramid from the specified file.");


    public static final String COVERAGE_BASE_NAME_NAME = "coverage_base_name";
    private static final String COVERAGE_BASE_NAME_REMARKS = "the basic name of the output pyramid.";
    public static final ParameterDescriptor<String> COVERAGE_BASE_NAME =
            new DefaultParameterDescriptor<>(COVERAGE_BASE_NAME_NAME, COVERAGE_BASE_NAME_REMARKS, String.class, null, true);
    
    public static final String IMAGE_FILE_PATH_NAME = "image_file";
    private static final String IMAGE_FILE_PATH_REMARKS = "The input image file.";
    public static final ParameterDescriptor<String> IMAGE_FILE_PATH =
            new DefaultParameterDescriptor<>(IMAGE_FILE_PATH_NAME, IMAGE_FILE_PATH_REMARKS, String.class, null, true);
    
    public static final String PYRAMID_FOLDER_NAME = "pyramid_folder";
    private static final String PYRAMID_FOLDER_REMARKS = "The path of the folder where the pyramid will be saved.";
    public static final ParameterDescriptor<File> PYRAMID_FOLDER =
            new DefaultParameterDescriptor<>(PYRAMID_FOLDER_NAME, PYRAMID_FOLDER_REMARKS, File.class, null, true);

    public static final String PROVIDER_OUT_ID_NAME = "output provider id";
    private static final String PROVIDER_OUT_ID_REMARKS = "the  name of the output provider id.";
    public static final ParameterDescriptor<String> PROVIDER_OUT_ID =
            new DefaultParameterDescriptor<>(PROVIDER_OUT_ID_NAME, PROVIDER_OUT_ID_REMARKS, String.class, null, true);
    
    /**Input parameters */
    public static final ParameterDescriptorGroup INPUT_DESC = new DefaultParameterDescriptorGroup("InputParameters",
            new GeneralParameterDescriptor[]{COVERAGE_BASE_NAME, IMAGE_FILE_PATH, PYRAMID_FOLDER, PROVIDER_OUT_ID});

    
    public static final String PROVIDER_SOURCE_NAME = "provider_source";
    private static final String PROVIDER_SOURCE_REMARKS = "A provider description.";
    public static final ParameterDescriptor<ParameterValueGroup> PROVIDER_SOURCE =
            new DefaultParameterDescriptor<>(PROVIDER_SOURCE_NAME, PROVIDER_SOURCE_REMARKS, ParameterValueGroup.class, null, true);
    
    /**Output parameters */
    public static final ParameterDescriptorGroup OUTPUT_DESC = new DefaultParameterDescriptorGroup("OutputParameters",
            new GeneralParameterDescriptor[]{PROVIDER_SOURCE});

    /**
     * Public constructor use by the ServiceRegistry to find and instantiate all ProcessDescriptor.
     */
    public PyramidCoverageDescriptor() {
        super(NAME, ConstellationProcessFactory.IDENTIFICATION, ABSTRACT, INPUT_DESC, OUTPUT_DESC);
    }

    
    @Override
    public org.geotoolkit.process.Process createProcess(ParameterValueGroup input) {
        return new PyramidCoverageProcess(this, input);
    }
}
