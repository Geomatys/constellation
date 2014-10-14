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
import org.constellation.business.IStyleBusiness;
import org.constellation.configuration.TargetNotFoundException;
import org.constellation.engine.register.repository.DomainRepository;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviders;
import org.constellation.provider.Providers;
import org.constellation.util.StyleReference;
import org.geotoolkit.coverage.*;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.coverage.io.GridCoverageReadParam;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.feature.type.DefaultName;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.style.MutableStyle;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.FactoryException;
import org.opengis.util.NoSuchIdentifierException;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Set;
import java.util.logging.Level;

import static org.constellation.coverage.process.PyramidCoverageDescriptor.IMAGE_FILE_FORMAT;
import static org.constellation.coverage.process.StyledPyramidCoverageDescriptor.*;
import static org.geotoolkit.parameter.Parameters.getOrCreate;
import static org.geotoolkit.parameter.Parameters.value;
import org.geotoolkit.referencing.OutOfDomainOfValidityException;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @author Quentin Boileau (Geomatys)
 */
public class StyledPyramidCoverageProcess extends AbstractPyramidCoverageProcess {
    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private IStyleBusiness styleBusiness;
    
    public StyledPyramidCoverageProcess(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
        super(desc, parameter);
    }

    /**
     * Quick constructor to create process
     *
     * @param pyramidFolder
     * @param providerID
     * @param imageFilePath
     * @param coverageBaseName
     * @param styleRef
     * @param domainId
     */
    public StyledPyramidCoverageProcess (final File pyramidFolder, final String providerID, final String imageFilePath, final String imageFileFormat,
                                         final String coverageBaseName, final StyleReference styleRef, final Integer domainId, final String dataset) {
        this(StyledPyramidCoverageDescriptor.INSTANCE, toParameters(pyramidFolder, providerID, imageFilePath, imageFileFormat, coverageBaseName, styleRef, domainId, dataset));
    }

    private static ParameterValueGroup toParameters(final File pyramidFolder, final String providerID, final String imageFilePath, final String imageFileFormat,
                                                    final String coverageBaseName, final StyleReference styleRef, final Integer domainId, final String dataset){
        final ParameterValueGroup params = StyledPyramidCoverageDescriptor.INSTANCE.getInputDescriptor().createValue();
        getOrCreate(StyledPyramidCoverageDescriptor.PROVIDER_OUT_ID, params).setValue(providerID);
        getOrCreate(StyledPyramidCoverageDescriptor.IMAGE_FILE_PATH, params).setValue(imageFilePath);
        getOrCreate(StyledPyramidCoverageDescriptor.IMAGE_FILE_FORMAT, params).setValue(imageFileFormat);
        getOrCreate(StyledPyramidCoverageDescriptor.COVERAGE_BASE_NAME, params).setValue(coverageBaseName);
        getOrCreate(StyledPyramidCoverageDescriptor.PYRAMID_FOLDER, params).setValue(pyramidFolder);
        getOrCreate(StyledPyramidCoverageDescriptor.STYLE, params).setValue(styleRef);
        getOrCreate(StyledPyramidCoverageDescriptor.DOMAIN_ID, params).setValue(domainId);
        getOrCreate(StyledPyramidCoverageDescriptor.DATASET_ID, params).setValue(dataset);
        return params;
    }

    @Override
    protected void execute() throws ProcessException {
        final String providerID       = value(PROVIDER_OUT_ID, inputParameters);
        final String imageFilePath    = value(IMAGE_FILE_PATH, inputParameters);
        final String imageFileFormat  = value(IMAGE_FILE_FORMAT, inputParameters);
        final StyleReference styleRef = value(STYLE, inputParameters);
        final File pyramidFolder      = value(PYRAMID_FOLDER, inputParameters);
        final String coverageBaseName = value(COVERAGE_BASE_NAME, inputParameters);
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
                outputCoverageStore = createXMLCoverageStore(finalPyramidFolder, referenceName, super.TILE_FORMAT);
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

        //get layer style
        final MutableStyle style;
        try {
            style = styleBusiness.getStyle(styleRef.getProviderId(), styleRef.getLayerId().getLocalPart());
        } catch (TargetNotFoundException ex) {
            throw new ProcessException("Style not found: " + ex.getMessage(), this, ex);
        }

        //build pyramid
        try {
            final PyramidalCoverageReference outCovRef = (PyramidalCoverageReference) getOrCreateCRef(outputCoverageStore, referenceName);
            final Envelope pyramidEnv = getPyramidWorldEnvelope();
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
                pyramidStyledData((GridCoverage2D) coverage, pyramidEnv, scales, coverageBaseName, outCovRef, style);
            }
        } catch (DataStoreException | FactoryException | OutOfDomainOfValidityException | TransformException e) {
            throw new ProcessException(e.getMessage(), this, e);
        }

        //finally create provider from store configuration
        if (provider == null) {
            provider = createProvider(providerID, outputCoverageStore, domainId, datasetName);
        }

        getOrCreate(PROVIDER_SOURCE, outputParameters).setValue(provider.getSource());
    }

    /**
     * Create a mapcontext with our input coverage and pyramid it into output Pyramidal CoverageStore
     * @param coverage
     * @param dataEnv
     * @param scales
     * @param coverageBaseName
     * @param outputRef
     * @param style
     * @throws ProcessException
     */
    private void pyramidStyledData(GridCoverage2D coverage, Envelope dataEnv, double[] scales, String coverageBaseName,
                                   PyramidalCoverageReference outputRef, MutableStyle style) throws ProcessException {
        final MapContext context = MapBuilder.createContext();
        final CoverageMapLayer layer = MapBuilder.createCoverageLayer(coverage, style, coverageBaseName);
        context.items().add(layer);

        final ProcessDescriptor desc;
        try {
            desc = ProcessFinder.getProcessDescriptor("engine2d", "mapcontextpyramid");
        } catch (NoSuchIdentifierException ex) {
            Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            throw new ProcessException("Process engine2d.mapcontextpyramid not found "+ex.getMessage(), this, ex);
        }
        final ParameterValueGroup input = desc.getInputDescriptor().createValue();
        input.parameter("context").setValue(context);
        input.parameter("extent").setValue(dataEnv);
        input.parameter("tilesize").setValue(new Dimension(TILE_SIZE, TILE_SIZE));
        input.parameter("scales").setValue(scales);
        input.parameter("container").setValue(outputRef);
        final org.geotoolkit.process.Process p = desc.createProcess(input);
        p.call();
    }
}
