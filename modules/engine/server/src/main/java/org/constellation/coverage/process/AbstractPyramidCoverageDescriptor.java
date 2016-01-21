package org.constellation.coverage.process;

import org.apache.sis.parameter.ParameterBuilder;
import org.constellation.database.api.jooq.tables.pojos.Data;
import org.constellation.database.api.jooq.tables.pojos.Dataset;
import org.constellation.process.AbstractCstlProcessDescriptor;
import org.constellation.process.provider.ProviderDescriptorConstant;
import org.geotoolkit.storage.coverage.CoverageReference;
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


    protected static final ParameterBuilder BUILDER = new ParameterBuilder();

    public static final String IN_COVERAGE_REF_NAME = "in_coverage_ref";
    private static final String IN_COVERAGE_REF_REMARKS = "Input coverage reference to pyramid.";
    public static final ParameterDescriptor<CoverageReference> IN_COVERAGE_REF = BUILDER
            .addName(IN_COVERAGE_REF_NAME)
            .setRemarks(IN_COVERAGE_REF_REMARKS)
            .setRequired(true)
            .create(CoverageReference.class, null);

    public static final String ORIGINAL_DATA_NAME = "orinial_data";
    private static final String ORIGINAL_DATA_REMARKS = "Data linked to input CoverageReference in order to link with pyramid data.";
    public static final ParameterDescriptor<Data> ORIGINAL_DATA = BUILDER
            .addName(ORIGINAL_DATA_NAME)
            .setRemarks(ORIGINAL_DATA_REMARKS)
            .setRequired(false)
            .create(Data.class, null);

    public static final String PYRAMID_NAME_NAME = "pyramid_name";
    private static final String PYRAMID_NAME_REMARKS = "Output pyramid reference name. (Optional, use input coverage reference name if not set.)";
    public static final ParameterDescriptor<String> PYRAMID_NAME = BUILDER
            .addName(PYRAMID_NAME_NAME)
            .setRemarks(PYRAMID_NAME_REMARKS)
            .setRequired(false)
            .create(String.class, null);

    public static final String PYRAMID_FOLDER_NAME = "pyramid_folder";
    private static final String PYRAMID_FOLDER_REMARKS = "The path of the folder where the pyramid will be saved.";
    public static final ParameterDescriptor<File> PYRAMID_FOLDER = BUILDER
            .addName(PYRAMID_FOLDER_NAME)
            .setRemarks(PYRAMID_FOLDER_REMARKS)
            .setRequired(true)
            .create(File.class, null);

    public static final String PYRAMID_CRS_NAME = "pyramid_crs";
    private static final String PYRAMID_CRS_REMARKS = "Output pyramid CRSs.";
    public static final ParameterDescriptor<CoordinateReferenceSystem[]> PYRAMID_CRS = BUILDER
            .addName(PYRAMID_CRS_NAME)
            .setRemarks(PYRAMID_CRS_REMARKS)
            .setRequired(false)
            .create(CoordinateReferenceSystem[].class, null);

    public static final String PROVIDER_OUT_ID_NAME = "pyramid_provider_identifer";
    private static final String PROVIDER_OUT_ID_REMARKS = "The identifier of the output provider.";
    public static final ParameterDescriptor<String> PROVIDER_OUT_ID = BUILDER
            .addName(PROVIDER_OUT_ID_NAME)
            .setRemarks(PROVIDER_OUT_ID_REMARKS)
            .setRequired(true)
            .create(String.class, null);

    public static final String PYRAMID_DATASET_NAME = "pyramid_dataset";
    private static final String PYRAMID_DATASET_REMARKS = "the name of the dataset where to add the datas.";
    public static final ParameterDescriptor<Dataset> PYRAMID_DATASET = BUILDER
            .addName(PYRAMID_DATASET_NAME)
            .setRemarks(PYRAMID_DATASET_REMARKS)
            .setRequired(true)
            .create(Dataset.class, null);

    public static final String DOMAIN_NAME = "domain";
    private static final String DOMAIN_REMARKS = "Identifier of the domain to add data.";

    public static final String UPDATE_NAME = "update";
    private static final String UPDATE_REMARKS = "Flag that enable update of output pyramid tiles.";
    public static final ParameterDescriptor<Boolean> UPDATE = BUILDER
            .addName(UPDATE_NAME)
            .setRemarks(UPDATE_REMARKS)
            .setRequired(false)
            .create(Boolean.class, Boolean.FALSE);

    public static final String OUT_PYRAMID_PROVIDER_NAME = "out_pyramid_provider";
    private static final String OUT_PYRAMID_PROVIDER_REMARKS = "A provider configuration.";
    public static final ParameterDescriptor<ParameterValueGroup> OUT_PYRAMID_PROVIDER_CONF = BUILDER
            .addName(OUT_PYRAMID_PROVIDER_NAME)
            .setRemarks(OUT_PYRAMID_PROVIDER_REMARKS)
            .setRequired(true)
            .create(ParameterValueGroup.class, null);


    public AbstractPyramidCoverageDescriptor(String name, InternationalString abs,
                                             ParameterDescriptorGroup inputDesc,
                                             ParameterDescriptorGroup outputdesc) {
        super(name, ProviderDescriptorConstant.IDENTIFICATION_CSTL, abs, inputDesc, outputdesc);
    }
}
