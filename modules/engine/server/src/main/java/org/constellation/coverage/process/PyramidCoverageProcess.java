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

import static org.constellation.coverage.process.AbstractPyramidCoverageDescriptor.DOMAIN;
import static org.constellation.coverage.process.AbstractPyramidCoverageDescriptor.IN_COVERAGE_REF;
import static org.constellation.coverage.process.AbstractPyramidCoverageDescriptor.ORIGINAL_DATA;
import static org.constellation.coverage.process.AbstractPyramidCoverageDescriptor.OUT_PYRAMID_PROVIDER_CONF;
import static org.constellation.coverage.process.AbstractPyramidCoverageDescriptor.PROVIDER_OUT_ID;
import static org.constellation.coverage.process.AbstractPyramidCoverageDescriptor.PYRAMID_CRS;
import static org.constellation.coverage.process.AbstractPyramidCoverageDescriptor.PYRAMID_DATASET;
import static org.constellation.coverage.process.AbstractPyramidCoverageDescriptor.PYRAMID_FOLDER;
import static org.constellation.coverage.process.AbstractPyramidCoverageDescriptor.PYRAMID_NAME;
import static org.constellation.coverage.process.AbstractPyramidCoverageDescriptor.UPDATE;
import static org.geotoolkit.parameter.Parameters.getOrCreate;
import static org.geotoolkit.parameter.Parameters.value;

import java.awt.Dimension;
import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.sis.storage.DataStore;
import org.apache.sis.storage.DataStoreException;
import org.constellation.configuration.DataBrief;
import org.constellation.engine.register.jooq.tables.pojos.Data;
import org.constellation.engine.register.jooq.tables.pojos.Dataset;
import org.constellation.engine.register.jooq.tables.pojos.Domain;
import org.constellation.engine.register.jooq.tables.pojos.Provider;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviders;
import org.geotoolkit.coverage.AbstractCoverageStoreFactory;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.CoverageStore;
import org.geotoolkit.coverage.CoverageUtilities;
import org.geotoolkit.coverage.GridCoverageStack;
import org.geotoolkit.coverage.PyramidalCoverageReference;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.coverage.grid.ViewType;
import org.geotoolkit.coverage.io.GridCoverageReadParam;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.coverage.xmlstore.XMLCoverageStore;
import org.geotoolkit.feature.type.DefaultName;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.image.interpolation.InterpolationCase;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.referencing.OutOfDomainOfValidityException;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.opengis.util.NoSuchIdentifierException;

/**
 * Process that create a pyramid "conform" from a CoverageReference.
 * This pyramid will be defined on the validity domains of inputs {@code pyramidCRS}.
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
     *
     * @param inCoverageRef
     * @param pyramidName
     * @param providerID
     * @param pyramidFolder
     * @param domain
     * @param dataset
     * @param pyramidCRS
     */
    public PyramidCoverageProcess (final CoverageReference inCoverageRef,
                                   final Data orinigalData,
                                   final String pyramidName,
                                   final String providerID,
                                   final File pyramidFolder,
                                   final Domain domain,
                                   final Dataset dataset,
                                   final CoordinateReferenceSystem[] pyramidCRS,
                                   final Boolean updatePyramid) {
        this(PyramidCoverageDescriptor.INSTANCE, toParameters(inCoverageRef, orinigalData, pyramidName, providerID,
                pyramidFolder, domain, dataset, pyramidCRS, updatePyramid));
    }

    private static ParameterValueGroup toParameters(final CoverageReference inCoverageRef,
                                                    final Data orinigalData,
                                                    final String pyramidName,
                                                    final String providerID,
                                                    final File pyramidFolder,
                                                    final Domain domain,
                                                    final Dataset dataset,
                                                    final CoordinateReferenceSystem[] pyramidCRS,
                                                    final Boolean updatePyramid){
        final ParameterValueGroup params = PyramidCoverageDescriptor.INSTANCE.getInputDescriptor().createValue();
        fillParameters(inCoverageRef, orinigalData, pyramidName, providerID, pyramidFolder, domain, dataset, pyramidCRS, updatePyramid, params);
        return params;
    }

    @Override
    protected void execute() throws ProcessException {
        final CoverageReference inCovRef = value(IN_COVERAGE_REF, inputParameters);
        final CoordinateReferenceSystem[] pyramidCRSs = value(PYRAMID_CRS, inputParameters);
        final Data originalData         = value(ORIGINAL_DATA, inputParameters);
        final String providerID         = value(PROVIDER_OUT_ID, inputParameters);
        final File pyramidFolder        = value(PYRAMID_FOLDER, inputParameters);
        final Domain domain             = value(DOMAIN, inputParameters);
        final Dataset dataset           = value(PYRAMID_DATASET, inputParameters);
        String pyramidName              = value(PYRAMID_NAME, inputParameters);
        final Integer domainId          = domain != null ? domain.getId() : null;
        Boolean update                  = value(UPDATE, inputParameters);

        if (update == null) {
            update = Boolean.FALSE;
        }

        if (pyramidName == null) {
            pyramidName = inCovRef.getName().getLocalPart();
        }

        Provider providerEntity = providerBusiness.getProvider(providerID);
        DataProvider dataProvider = null;
        CoverageStore outputCoverageStore = null;
        Name referenceName = null;

        //provider already exist -> try to get pyramid Reference
        if (providerEntity != null) {
            dataProvider = DataProviders.getInstance().getProvider(providerID);
            final DataStore mainStore = dataProvider.getMainStore();
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
            referenceName = new DefaultName(namespace, pyramidName);

        }

        if (outputCoverageStore == null) {
            //create XMLCoverageStore
            try {
                final File finalPyramidFolder = new File(pyramidFolder, pyramidName);
                referenceName = new DefaultName(pyramidName);
                outputCoverageStore = getOrCreateXMLCoverageStore(finalPyramidFolder);
            } catch (DataStoreException | MalformedURLException e) {
                throw new ProcessException(e.getMessage(), this, e);
            }
        }

        //build pyramid
        final Dimension tileDim = new Dimension(TILE_SIZE, TILE_SIZE);
        try {
            final PyramidalCoverageReference outCovRef =
                    (PyramidalCoverageReference) getOrCreateCRef((XMLCoverageStore)outputCoverageStore, referenceName, TIFF_FORMAT, ViewType.GEOPHYSICS);

            final GridCoverageReader reader = inCovRef.acquireReader();
            final GridCoverageReadParam readParam = new GridCoverageReadParam();
            readParam.setDeferred(true);
            final GridCoverage coverage = reader.read(inCovRef.getImageIndex(), readParam);
            inCovRef.recycle(reader);

            if (coverage instanceof GridCoverageStack) {
                throw new ProcessException("CoverageStack implementation not supported.", this, null);
            }

            final GridCoverage2D gridCoverage2D = (GridCoverage2D) coverage;
            final CoordinateReferenceSystem coverageCRS = gridCoverage2D.getCoordinateReferenceSystem();

            final Map<Envelope, double[]> map = new HashMap<>();
            for (CoordinateReferenceSystem pyramidCRS2D : pyramidCRSs) {

                final double[] scales = getPyramidScales(gridCoverage2D, outCovRef, pyramidCRS2D);

                final Envelope finalPyramidEnv = getFixedPyramidEnvelop(pyramidCRS2D, coverage.getEnvelope());
                map.put(finalPyramidEnv, scales);
                //Prepare pyramid's mosaics.
                CoverageUtilities.getOrCreatePyramid(outCovRef, finalPyramidEnv, tileDim, scales);
            }

            pyramidData(inCovRef, outputCoverageStore, referenceName, map, tileDim, update);

        } catch (DataStoreException | FactoryException | TransformException | OutOfDomainOfValidityException e) {
            throw new ProcessException(e.getMessage(), this, e);
        }

        //finally create provider from store configuration
        if (dataProvider == null) {
            dataProvider = createProvider(providerID, outputCoverageStore, domainId, dataset.getId());
        }

        if (providerEntity == null) {
            providerEntity = providerBusiness.getProvider(providerID);
        }

        // link original data with the tiled data.
        if (originalData != null) {
            final QName qName = new QName(referenceName.getNamespaceURI(), referenceName.getLocalPart());
            final DataBrief pyramidDataBrief = dataBusiness.getDataBrief(qName, providerEntity.getId());
            dataBusiness.linkDataToData(originalData.getId(), pyramidDataBrief.getId());
        }

        getOrCreate(OUT_PYRAMID_PROVIDER_CONF, outputParameters).setValue(dataProvider.getSource());
    }

    /**
     * Create conform pyramid
     * @param inRef
     * @param outCoverageStore
     * @param outName
     * @param envScales
     * @param tileDim
     * @param update
     * @throws ProcessException
     */
    private void pyramidData(CoverageReference inRef, CoverageStore outCoverageStore, Name outName,
                             Map<Envelope, double[]> envScales, Dimension tileDim, Boolean update) throws ProcessException {

        final ProcessDescriptor desc;
        try {
            desc = ProcessFinder.getProcessDescriptor("coverage", "coveragepyramid");
        } catch (NoSuchIdentifierException ex) {
            throw new ProcessException("Process coverage.coveragepyramid not found " + ex.getMessage(),this,ex);
        }

        final ParameterValueGroup input = desc.getInputDescriptor().createValue();
        input.parameter("coverageref").setValue(inRef);
        input.parameter("in_coverage_store").setValue(outCoverageStore);
        input.parameter("tile_size").setValue(tileDim);
        input.parameter("pyramid_name").setValue(outName.getLocalPart());
        input.parameter("interpolation_type").setValue(InterpolationCase.NEIGHBOR);
        input.parameter("resolution_per_envelope").setValue(envScales);
        input.parameter("reuse_tiles").setValue(update);
        final org.geotoolkit.process.Process p = desc.createProcess(input);
        p.call();
    }
}
