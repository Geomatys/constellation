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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import org.apache.sis.storage.DataStoreException;
import org.constellation.coverage.PyramidCoverageHelper;
import org.constellation.process.AbstractCstlProcess;
import org.constellation.provider.DataProviderFactory;
import org.constellation.provider.DataProviders;
import org.constellation.provider.configuration.ProviderParameters;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.xmlstore.XMLCoverageStoreFactory;
import org.geotoolkit.feature.type.DefaultName;

import static org.constellation.coverage.process.PyramidCoverageDescriptor.*;
import static org.geotoolkit.parameter.Parameters.getOrCreate;
import static org.geotoolkit.parameter.Parameters.value;
import org.geotoolkit.parameter.ParametersExt;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.coverage.mathcalc.MathCalcDescriptor;
import org.opengis.coverage.Coverage;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @author Quentin Boileau (Geomatys)
 */
public class PyramidCoverageProcess extends AbstractCstlProcess {

    public PyramidCoverageProcess(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
        super(desc, parameter);
    }

    /**
     * Quick constructor to create process
     * @param pyramidFolder
     * @param providerID
     * @param imageFilePath
     * @param coverageBaseName
     */
    public PyramidCoverageProcess (final File pyramidFolder, final String providerID, final String imageFilePath, final String coverageBaseName) {
        this(PyramidCoverageDescriptor.INSTANCE, toParameters(pyramidFolder, providerID, imageFilePath, coverageBaseName));
    }

    private static ParameterValueGroup toParameters(final File pyramidFolder, final String providerID, final String imageFilePath, final String coverageBaseName){
        final ParameterValueGroup params = PyramidCoverageDescriptor.INSTANCE.getInputDescriptor().createValue();
        getOrCreate(PROVIDER_OUT_ID, params).setValue(providerID);
        getOrCreate(IMAGE_FILE_PATH, params).setValue(imageFilePath);
        getOrCreate(COVERAGE_BASE_NAME, params).setValue(coverageBaseName);
        getOrCreate(PYRAMID_FOLDER, params).setValue(pyramidFolder);
        return params;
    }

    @Override
    protected void execute() throws ProcessException {
        final String providerID       = value(PROVIDER_OUT_ID, inputParameters);
        final String imageFilePath    = value(IMAGE_FILE_PATH, inputParameters);
        final String coverageBaseName = value(COVERAGE_BASE_NAME, inputParameters);
        final File pyramidFolder      = value(PYRAMID_FOLDER, inputParameters);
        try {
            PyramidCoverageHelper pyramidHelper = PyramidCoverageHelper.builder(coverageBaseName).
                    inputFormat("AUTO").withDeeps(new double[]{1}).withBaseCoverageNamer(new SimpleCoverageNamer()).
                    fromImage(imageFilePath).toFileStore(pyramidFolder.getAbsolutePath()).build();
            pyramidHelper.buildPyramid(null);
        } catch (DataStoreException | TransformException | FactoryException | MalformedURLException ex) {
            throw new ProcessException("Error while building pyramid", this, ex);
        }
        
        try {
            final DataProviderFactory factory = DataProviders.getInstance().getFactory("coverage-store");
            final ParameterValueGroup pparams = factory.getProviderDescriptor().createValue();
            ParametersExt.getOrCreateValue(pparams, ProviderParameters.SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue(providerID);
            ParametersExt.getOrCreateValue(pparams, ProviderParameters.SOURCE_TYPE_DESCRIPTOR.getName().getCode()).setValue("coverage-store");
            final ParameterValueGroup choiceparams = ParametersExt.getOrCreateGroup(pparams, factory.getStoreDescriptor().getName().getCode());
            final ParameterValueGroup xmlpyramidparams = ParametersExt.getOrCreateGroup(choiceparams, XMLCoverageStoreFactory.PARAMETERS_DESCRIPTOR.getName().getCode());
            final URL fileUrl = URI.create("file:"+ pyramidFolder.getAbsolutePath() +"/tiles").toURL();
            ParametersExt.getOrCreateValue(xmlpyramidparams, XMLCoverageStoreFactory.PATH.getName().getCode()).setValue(fileUrl);
            ParametersExt.getOrCreateValue(xmlpyramidparams, XMLCoverageStoreFactory.NAMESPACE.getName().getCode()).setValue("no namespace");
            getOrCreate(PROVIDER_SOURCE, outputParameters).setValue(pparams);
        } catch (MalformedURLException ex) {
            throw new ProcessException("the pyramid folder path is malformed", this, ex);
        }
    } 
 
    private static class SimpleCoverageNamer implements PyramidCoverageHelper.CoverageNamer {

        @Override
        public DefaultName getName(String baseName, int n) {
            return new DefaultName(baseName);
        }
    
}
}
