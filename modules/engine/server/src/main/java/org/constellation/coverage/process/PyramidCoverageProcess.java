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

import org.apache.sis.storage.DataStore;
import org.apache.sis.storage.DataStoreException;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviders;
import org.geotoolkit.coverage.*;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.coverage.io.GridCoverageReadParam;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.feature.type.DefaultName;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.image.interpolation.InterpolationCase;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

import java.awt.Dimension;
import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.constellation.coverage.process.PyramidCoverageDescriptor.*;
import static org.geotoolkit.parameter.Parameters.getOrCreate;
import static org.geotoolkit.parameter.Parameters.value;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @author Quentin Boileau (Geomatys)
 */
public class PyramidCoverageProcess extends AbstractPyramidCoverageProcess {

    public PyramidCoverageProcess(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
        super(desc, parameter);
    }

    /**
     * Quick constructor to create process
     * @param pyramidFolder
     * @param providerID
     * @param imageFilePath
     * @param coverageBaseName
     * @param domainId
     */
    public PyramidCoverageProcess (final File pyramidFolder, final String providerID, final String imageFilePath,
                                   final String imageFileFormat, final String coverageBaseName, final Integer domainId, final String dataset) {
        this(PyramidCoverageDescriptor.INSTANCE, toParameters(pyramidFolder, providerID, imageFilePath, imageFileFormat, coverageBaseName, domainId, dataset));
    }

    private static ParameterValueGroup toParameters(final File pyramidFolder, final String providerID, final String imageFilePath,
                                                    final String imageFileFormat, final String coverageBaseName, final Integer domainId, final String dataset){
        final ParameterValueGroup params = PyramidCoverageDescriptor.INSTANCE.getInputDescriptor().createValue();
        getOrCreate(PROVIDER_OUT_ID, params).setValue(providerID);
        getOrCreate(IMAGE_FILE_PATH, params).setValue(imageFilePath);
        getOrCreate(IMAGE_FILE_FORMAT, params).setValue(imageFileFormat);
        getOrCreate(COVERAGE_BASE_NAME, params).setValue(coverageBaseName);
        getOrCreate(PYRAMID_FOLDER, params).setValue(pyramidFolder);
        getOrCreate(DOMAIN_ID, params).setValue(domainId);
        getOrCreate(DATASET_ID, params).setValue(dataset);
        return params;
    }

    @Override
    protected void execute() throws ProcessException {
        final String providerID       = value(PROVIDER_OUT_ID, inputParameters);
        final String imageFilePath    = value(IMAGE_FILE_PATH, inputParameters);
        final String imageFileFormat  = value(IMAGE_FILE_FORMAT, inputParameters);
        final String coverageBaseName = value(COVERAGE_BASE_NAME, inputParameters);
        final File pyramidFolder      = value(PYRAMID_FOLDER, inputParameters);
        final Integer domainId        = value(DOMAIN_ID, inputParameters);
        final String datasetName      = value(DATASET_ID, inputParameters);

        DataProvider provider = DataProviders.getInstance().getProvider(providerID);
        CoverageStore outputCoverageStore = null;
        Name referenceName = null;
        if (provider != null) {
            final DataStore mainStore = provider.getMainStore();
            if (!(mainStore instanceof CoverageStore)) {
                throw new ProcessException("Provider "+providerID+" reference a non coverage type store", this, null);
            }

            outputCoverageStore = (CoverageStore) mainStore;
            final ParameterValueGroup configuration = outputCoverageStore.getConfiguration();
            String namespace = value(AbstractCoverageStoreFactory.NAMESPACE, configuration);
            
            // to avoid error on comparison
            if (namespace!= null && namespace.equals("no namespace")) {
                namespace = null;
            }

            try {
                final Set<Name> names = outputCoverageStore.getNames();
                
                referenceName = new DefaultName(namespace, coverageBaseName);

                final CoverageReference coverageReference;
                if (names.contains(referenceName)) {
                    coverageReference = outputCoverageStore.getCoverageReference(referenceName);
                } else {
                    coverageReference = outputCoverageStore.create(referenceName);
                }

                if (!(coverageReference instanceof PyramidalCoverageReference)) {
                    throw new ProcessException("Provider "+providerID+" don't store pyramidal coverages.", this, null);
                }
            } catch (DataStoreException e) {
                throw new ProcessException(e.getMessage(), this, e);
            }
        }

        if (outputCoverageStore == null) {
            //create XMLCoverageStore
            try {
                final File finalPyramidFolder = new File(pyramidFolder, coverageBaseName);
                referenceName = new DefaultName(coverageBaseName);
                outputCoverageStore = createXMLCoverageStore(finalPyramidFolder, referenceName, "tif");
            } catch (DataStoreException | MalformedURLException e) {
                throw new ProcessException(e.getMessage(), this, e);
            }
        }

        // get input CoverageStore
        final CoverageStore inputCoverageStore;
        try {
            inputCoverageStore = getCoverageStoreFromInput(imageFilePath, imageFileFormat);
        } catch (MalformedURLException e) {
            throw new ProcessException(e.getMessage(), this, e);
        }

        //build pyramid
        final PyramidCoverageBuilder builder = new PyramidCoverageBuilder(new Dimension(TILE_SIZE, TILE_SIZE), InterpolationCase.BILINEAR, 1);
        final Envelope pyramidEnv = getPyramidWorldEnvelope();
        try {
            final PyramidalCoverageReference outCovRef = (PyramidalCoverageReference) getOrCreateCRef(outputCoverageStore, referenceName);
            final Set<Name> inputNames = inputCoverageStore.getNames();
            for (Name name : inputNames) {
                final CoverageReference ref = inputCoverageStore.getCoverageReference(name);
                final GridCoverageReader reader = ref.acquireReader();
                final GridCoverageReadParam readParam = new GridCoverageReadParam();
                readParam.setDeferred(true);
                final GridCoverage coverage = reader.read(ref.getImageIndex(), readParam);
                ref.recycle(reader);

                if (coverage instanceof GridCoverageStack) {
                    throw new ProcessException("CoverageStack implementation not supported.", this, null);
                }

                final double[] scales = getPyramidScales((GridCoverage2D) coverage, outCovRef);
                final Map<Envelope, double[]> map = new HashMap<>();
                map.put(pyramidEnv, scales);
                builder.create(ref, outputCoverageStore, referenceName, map, null, null, null);
            }
        } catch (DataStoreException | FactoryException | TransformException e) {
            throw new ProcessException(e.getMessage(), this, e);
        }

        //finally create provider from store configuration
        if (provider == null) {
            provider = createProvider(providerID, outputCoverageStore, domainId, datasetName);
        }

        getOrCreate(PROVIDER_SOURCE, outputParameters).setValue(provider.getSource());
    } 
 
}
