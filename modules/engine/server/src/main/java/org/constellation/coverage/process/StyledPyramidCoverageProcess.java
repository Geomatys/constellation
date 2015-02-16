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
import static org.constellation.coverage.process.StyledPyramidCoverageDescriptor.STYLE;
import static org.geotoolkit.parameter.Parameters.getOrCreate;
import static org.geotoolkit.parameter.Parameters.value;

import java.awt.Dimension;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Set;
import java.util.logging.Level;

import javax.xml.namespace.QName;

import org.apache.sis.storage.DataStore;
import org.apache.sis.storage.DataStoreException;
import org.constellation.business.IStyleBusiness;
import org.constellation.configuration.DataBrief;
import org.constellation.configuration.TargetNotFoundException;
import org.constellation.engine.register.jooq.tables.pojos.Data;
import org.constellation.engine.register.jooq.tables.pojos.Dataset;
import org.constellation.engine.register.jooq.tables.pojos.Domain;
import org.constellation.engine.register.jooq.tables.pojos.Provider;
import org.constellation.process.StyleProcessReference;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviders;
import org.constellation.provider.Providers;
import org.constellation.util.StyleReference;
import org.geotoolkit.coverage.AbstractCoverageStoreFactory;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.CoverageStore;
import org.geotoolkit.coverage.GridCoverageStack;
import org.geotoolkit.coverage.PyramidalCoverageReference;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.coverage.grid.ViewType;
import org.geotoolkit.coverage.io.GridCoverageReadParam;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.coverage.xmlstore.XMLCoverageStore;
import org.geotoolkit.feature.type.DefaultName;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.referencing.OutOfDomainOfValidityException;
import org.geotoolkit.style.MutableStyle;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.opengis.util.NoSuchIdentifierException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @author Quentin Boileau (Geomatys)
 */
public class StyledPyramidCoverageProcess extends AbstractPyramidCoverageProcess {

    @Autowired
    private IStyleBusiness styleBusiness;
    
    public StyledPyramidCoverageProcess(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
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
     * @param styleRef
     */
    public StyledPyramidCoverageProcess (final CoverageReference inCoverageRef,
                                         final Data orinigalData,
                                         final String pyramidName,
                                         final String providerID,
                                         final File pyramidFolder,
                                         final Domain domain,
                                         final Dataset dataset,
                                         final CoordinateReferenceSystem[] pyramidCRS,
                                         final StyleProcessReference styleRef,
                                         final Boolean updatePyramid) {
        this(StyledPyramidCoverageDescriptor.INSTANCE, toParameters(inCoverageRef, orinigalData, pyramidName, providerID, pyramidFolder,
                domain, dataset, pyramidCRS, styleRef, updatePyramid));
    }

    private static ParameterValueGroup toParameters(final CoverageReference inCoverageRef,
                                                    final Data orinigalData,
                                                    final String pyramidName,
                                                    final String providerID,
                                                    final File pyramidFolder,
                                                    final Domain domain,
                                                    final Dataset dataset,
                                                    final CoordinateReferenceSystem[] pyramidCRS,
                                                    final StyleProcessReference styleRef,
                                                    final Boolean updatePyramid){
        final ParameterValueGroup params = StyledPyramidCoverageDescriptor.INSTANCE.getInputDescriptor().createValue();
        fillParameters(inCoverageRef, orinigalData, pyramidName, providerID, pyramidFolder, domain, dataset, pyramidCRS, updatePyramid, params);
        getOrCreate(STYLE, params).setValue(styleRef);
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
        final StyleProcessReference styleRef   = value(STYLE, inputParameters);
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

            try {
                final Set<Name> names = outputCoverageStore.getNames();
                referenceName = new DefaultName(namespace, pyramidName);

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
                final File finalPyramidFolder = new File(pyramidFolder, pyramidName);
                referenceName = new DefaultName(pyramidName);
                outputCoverageStore = getOrCreateXMLCoverageStore(finalPyramidFolder);
            } catch (DataStoreException | MalformedURLException e) {
                throw new ProcessException(e.getMessage(), this, e);
            }
        }

        //get layer style
        final MutableStyle style;
        try {
            style = styleBusiness.getStyle(styleRef.getId());
        } catch (TargetNotFoundException ex) {
            throw new ProcessException("Style not found: " + ex.getMessage(), this, ex);
        }

        //build pyramid
        try {

            final GridCoverageReader reader = inCovRef.acquireReader();
            final GridCoverageReadParam readParam = new GridCoverageReadParam();
            readParam.setDeferred(true);
            final GridCoverage coverage = reader.read(inCovRef.getImageIndex(), readParam);
            inCovRef.recycle(reader);

            if (coverage instanceof GridCoverageStack) {
                throw new ProcessException("CoverageStack implementation not supported.", this, null);
            }

            final GridCoverage2D gridCoverage2D = (GridCoverage2D) coverage;
            for (CoordinateReferenceSystem pyramidCRS2D : pyramidCRSs) {

                final PyramidalCoverageReference outCovRef =
                        (PyramidalCoverageReference) getOrCreateCRef((XMLCoverageStore) outputCoverageStore, referenceName, PNG_FORMAT, ViewType.RENDERED);

                final double[] scales = getPyramidScales((GridCoverage2D) coverage, outCovRef, pyramidCRS2D);

                final Envelope finalPyramidEnv = getFixedPyramidEnvelop(pyramidCRS2D, coverage.getEnvelope());
                pyramidStyledData(inCovRef, finalPyramidEnv, scales, outCovRef, style, update);
            }
        } catch (DataStoreException | FactoryException | OutOfDomainOfValidityException | TransformException e) {
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
     * Create a mapcontext with our input coverage and pyramid it into output Pyramidal CoverageStore
     * @param reference
     * @param dataEnv
     * @param scales
     * @param outputRef
     * @param style
     * @throws ProcessException
     */
    private void pyramidStyledData(CoverageReference reference, Envelope dataEnv, double[] scales,
                                   PyramidalCoverageReference outputRef, MutableStyle style, boolean update) throws ProcessException {
        final MapContext context = MapBuilder.createContext();
        final CoverageMapLayer layer = MapBuilder.createCoverageLayer(reference, style);
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
        input.parameter("update").setValue(update);
        final org.geotoolkit.process.Process p = desc.createProcess(input);
        p.call();
    }
}
