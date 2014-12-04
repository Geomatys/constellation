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
import org.constellation.process.provider.ProviderDescriptorConstant;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.geotoolkit.process.AbstractProcessDescriptor;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
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

    public static final String IMAGE_FILE_FORMAT_NAME = "image_file_format";
    private static final String IMAGE_FILE_FORMAT_REMARKS = "The input image file format.";
    public static final ParameterDescriptor<String> IMAGE_FILE_FORMAT =
            new DefaultParameterDescriptor<>(IMAGE_FILE_FORMAT_NAME, IMAGE_FILE_FORMAT_REMARKS, String.class, "AUTO", true);
    
    public static final String PYRAMID_FOLDER_NAME = "pyramid_folder";
    private static final String PYRAMID_FOLDER_REMARKS = "The path of the folder where the pyramid will be saved.";
    public static final ParameterDescriptor<File> PYRAMID_FOLDER =
            new DefaultParameterDescriptor<>(PYRAMID_FOLDER_NAME, PYRAMID_FOLDER_REMARKS, File.class, null, true);

    public static final String PROVIDER_OUT_ID_NAME = "output provider id";
    private static final String PROVIDER_OUT_ID_REMARKS = "The identifier of the output provider.";
    public static final ParameterDescriptor<String> PROVIDER_OUT_ID =
            new DefaultParameterDescriptor<>(PROVIDER_OUT_ID_NAME, PROVIDER_OUT_ID_REMARKS, String.class, null, true);
    
    public static final String DATASET_ID_NAME = "dataset_id";
    private static final String DATASET_ID_REMARKS = "the name of the dataset where to add the datas.";
    public static final ParameterDescriptor<String> DATASET_ID =
            new DefaultParameterDescriptor<>(DATASET_ID_NAME, DATASET_ID_REMARKS, String.class, null, false);

    public static final String DOMAIN_ID_NAME = "domain-id";
    private static final String DOMAIN_ID_REMARKS = "Identifier of the domain to add data.";
    public static final ParameterDescriptor<Integer> DOMAIN_ID =
            new DefaultParameterDescriptor(DOMAIN_ID_NAME, DOMAIN_ID_REMARKS, Integer.class, null, false);

    public static final String PYRAMID_CRS_NAME = "pyramid-crs";
    private static final String PYRAMID_CRS_REMARKS = "Output pyramid CRS.";
    public static final ParameterDescriptor<CoordinateReferenceSystem> PYRAMID_CRS =
            new DefaultParameterDescriptor(PYRAMID_CRS_NAME, PYRAMID_CRS_REMARKS, CoordinateReferenceSystem.class, null, false);

    /**Input parameters */
    public static final ParameterDescriptorGroup INPUT_DESC = new DefaultParameterDescriptorGroup("InputParameters",
            new GeneralParameterDescriptor[]{COVERAGE_BASE_NAME, IMAGE_FILE_PATH, IMAGE_FILE_FORMAT, PYRAMID_FOLDER,
                    PROVIDER_OUT_ID, DOMAIN_ID, DATASET_ID, PYRAMID_CRS});

    
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
        super(NAME, ProviderDescriptorConstant.IDENTIFICATION_CSTL, ABSTRACT, INPUT_DESC, OUTPUT_DESC);
    }

    public static final PyramidCoverageDescriptor INSTANCE = new PyramidCoverageDescriptor();
    
    @Override
    public org.geotoolkit.process.Process createProcess(ParameterValueGroup input) {
        return new PyramidCoverageProcess(this, input);
    }
}
