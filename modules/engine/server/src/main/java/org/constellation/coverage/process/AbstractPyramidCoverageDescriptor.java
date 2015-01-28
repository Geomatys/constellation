package org.constellation.coverage.process;

import org.constellation.engine.register.Data;
import org.constellation.engine.register.Dataset;
import org.constellation.engine.register.Domain;
import org.constellation.process.AbstractCstlProcessDescriptor;
import org.constellation.process.provider.ProviderDescriptorConstant;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

import java.io.File;

/**
 * @author Quentin Boileau (Geomatys)
 */
public abstract class AbstractPyramidCoverageDescriptor extends AbstractCstlProcessDescriptor {


    public static final String IN_COVERAGE_REF_NAME = "in_coverage_ref";
    private static final String IN_COVERAGE_REF_REMARKS = "Input coverage reference to pyramid.";
    public static final ParameterDescriptor<CoverageReference> IN_COVERAGE_REF =
            new DefaultParameterDescriptor<>(IN_COVERAGE_REF_NAME, IN_COVERAGE_REF_REMARKS, CoverageReference.class, null, true);

    public static final String ORIGINAL_DATA_NAME = "orinial_data";
    private static final String ORIGINAL_DATA_REMARKS = "Data linked to input CoverageReference in order to link with pyramid data.";
    public static final ParameterDescriptor<Data> ORIGINAL_DATA =
            new DefaultParameterDescriptor<>(ORIGINAL_DATA_NAME, ORIGINAL_DATA_REMARKS, Data.class, null, false);

    public static final String PYRAMID_NAME_NAME = "pyramid_name";
    private static final String PYRAMID_NAME_REMARKS = "Output pyramid reference name. (Optional, use input coverage reference name if not set.)";
    public static final ParameterDescriptor<String> PYRAMID_NAME =
            new DefaultParameterDescriptor<>(PYRAMID_NAME_NAME, PYRAMID_NAME_REMARKS, String.class, null, false);

    public static final String PYRAMID_FOLDER_NAME = "pyramid_folder";
    private static final String PYRAMID_FOLDER_REMARKS = "The path of the folder where the pyramid will be saved.";
    public static final ParameterDescriptor<File> PYRAMID_FOLDER =
            new DefaultParameterDescriptor<>(PYRAMID_FOLDER_NAME, PYRAMID_FOLDER_REMARKS, File.class, null, true);

    public static final String PYRAMID_CRS_NAME = "pyramid_crs";
    private static final String PYRAMID_CRS_REMARKS = "Output pyramid CRSs.";
    public static final ParameterDescriptor<CoordinateReferenceSystem[]> PYRAMID_CRS =
            new DefaultParameterDescriptor(PYRAMID_CRS_NAME, PYRAMID_CRS_REMARKS, CoordinateReferenceSystem[].class, null, false);

    public static final String PROVIDER_OUT_ID_NAME = "pyramid_provider_identifer";
    private static final String PROVIDER_OUT_ID_REMARKS = "The identifier of the output provider.";
    public static final ParameterDescriptor<String> PROVIDER_OUT_ID =
            new DefaultParameterDescriptor<>(PROVIDER_OUT_ID_NAME, PROVIDER_OUT_ID_REMARKS, String.class, null, true);

    public static final String PYRAMID_DATASET_NAME = "pyramid_dataset";
    private static final String PYRAMID_DATASET_REMARKS = "the name of the dataset where to add the datas.";
    public static final ParameterDescriptor<Dataset> PYRAMID_DATASET =
            new DefaultParameterDescriptor<>(PYRAMID_DATASET_NAME, PYRAMID_DATASET_REMARKS, Dataset.class, null, true);

    public static final String DOMAIN_NAME = "domain";
    private static final String DOMAIN_REMARKS = "Identifier of the domain to add data.";
    public static final ParameterDescriptor<Domain> DOMAIN =
            new DefaultParameterDescriptor(DOMAIN_NAME, DOMAIN_REMARKS, Domain.class, null, false);


    public static final String OUT_PYRAMID_PROVIDER_NAME = "out_pyramid_provider";
    private static final String OUT_PYRAMID_PROVIDER_REMARKS = "A provider configuration.";
    public static final ParameterDescriptor<ParameterValueGroup> OUT_PYRAMID_PROVIDER_CONF =
            new DefaultParameterDescriptor<>(OUT_PYRAMID_PROVIDER_NAME, OUT_PYRAMID_PROVIDER_REMARKS, ParameterValueGroup.class, null, true);



    public AbstractPyramidCoverageDescriptor(String name, InternationalString abs,
                                             ParameterDescriptorGroup inputDesc,
                                             ParameterDescriptorGroup outputdesc) {
        super(name, ProviderDescriptorConstant.IDENTIFICATION_CSTL, abs, inputDesc, outputdesc);
    }
}
