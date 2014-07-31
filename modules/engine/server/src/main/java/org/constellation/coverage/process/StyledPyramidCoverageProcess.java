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

import java.awt.Dimension;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.ArraysExt;
import org.constellation.configuration.TargetNotFoundException;
import static org.constellation.coverage.process.StyledPyramidCoverageDescriptor.COVERAGE_BASE_NAME;
import static org.constellation.coverage.process.StyledPyramidCoverageDescriptor.DOMAIN_ID;
import static org.constellation.coverage.process.StyledPyramidCoverageDescriptor.IMAGE_FILE_PATH;
import static org.constellation.coverage.process.StyledPyramidCoverageDescriptor.PROVIDER_OUT_ID;
import static org.constellation.coverage.process.StyledPyramidCoverageDescriptor.PYRAMID_FOLDER;
import static org.constellation.coverage.process.StyledPyramidCoverageDescriptor.STYLE;
import org.constellation.process.AbstractCstlProcess;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviderFactory;
import org.constellation.provider.DataProviders;
import org.constellation.provider.Providers;
import org.constellation.provider.configuration.ProviderParameters;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.CoverageStore;
import org.geotoolkit.coverage.CoverageStoreFinder;
import org.geotoolkit.coverage.CoverageUtilities;
import org.geotoolkit.coverage.filestore.FileCoverageStoreFactory;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.coverage.grid.GridGeometry2D;
import org.geotoolkit.coverage.grid.ViewType;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.coverage.xmlstore.XMLCoverageReference;
import org.geotoolkit.coverage.xmlstore.XMLCoverageStoreFactory;
import org.geotoolkit.feature.type.DefaultName;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.parameter.Parameters;
import static org.geotoolkit.parameter.Parameters.value;
import org.geotoolkit.parameter.ParametersExt;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.adapters.NetcdfCRS;
import org.geotoolkit.style.MutableStyle;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.opengis.util.NoSuchIdentifierException;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class StyledPyramidCoverageProcess extends AbstractCstlProcess {

    
    public StyledPyramidCoverageProcess(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
        super(desc, parameter);
    }

    @Override
    protected void execute() throws ProcessException {
        String tileFormat = "PNG";
        String crs = "EPSG:4326";
    
        final String providerID       = value(PROVIDER_OUT_ID, inputParameters);
        final String imageFilePath    = value(IMAGE_FILE_PATH, inputParameters);
        final String styleName        = value(STYLE, inputParameters);
        final File pyramidFolder      = value(PYRAMID_FOLDER, inputParameters);
        final String coverageBaseName = value(COVERAGE_BASE_NAME, inputParameters);
        final Integer domainId        = value(DOMAIN_ID, inputParameters);
        
        final ParameterValueGroup params = FileCoverageStoreFactory.PARAMETERS_DESCRIPTOR.createValue();
        try {
            Parameters.getOrCreate(FileCoverageStoreFactory.PATH, params).setValue(new File(imageFilePath).toURI().toURL());
        } catch (MalformedURLException ex) {
            throw new ProcessException(ex.getMessage(), this, ex);
        }
        Parameters.getOrCreate(FileCoverageStoreFactory.TYPE, params).setValue("geotiff");

        final List<GridCoverage2D> coverages;
        try {
            CoverageStore inputStore = CoverageStoreFinder.open(params);
            coverages = buildCoverages(inputStore);
        } catch (DataStoreException ex) {
            throw new ProcessException("Error while opening output datastore", this, ex);
        }
        
        for (GridCoverage2D coverage : coverages) {
        
            final GridGeometry2D gg = coverage.getGridGeometry();
            int gridspan = gg.getExtent2D().getSpan(0);
            
            //get pyramid CRS, we force longiude first on the pyramids
            // WMTS is made for display like WMS, so longitude is expected to be on the X axis.
            // Note : this is not writen in the spec.
            final CoordinateReferenceSystem coordsys;
            Envelope dataEnv = coverage.getEnvelope();
            try {
                coordsys = CRS.decode(crs,true);
            } catch (FactoryException ex) {
                throw new ProcessException("Invalid CRS code : "+crs, this, ex);
            }
            try {
                //reproject data envelope
                dataEnv = CRS.transform(dataEnv, coordsys);
            } catch (TransformException ex) {
                throw new ProcessException("Could not transform data envelope to crs "+crs, this, ex);
            }
            

            //calculate scales
            final double spanX = dataEnv.getSpan(0);
            final double baseScale = spanX / gridspan;
            double scale = spanX / 256;
            double[] scales = new double[0];
            while (true) {
                if (scale <= baseScale) {
                    //fit to exact match to preserve base quality.
                    scale = baseScale;
                }
                scales = ArraysExt.insert(scales, scales.length, 1);
                scales[scales.length - 1] = scale;

                if (scale <= baseScale) {
                    break;
                }
                scale = scale / 2;
            }

            //get upper corner 
            final Double upperCornerX = dataEnv.getMinimum(0);
            final Double upperCornerY = dataEnv.getMaximum(1);
            

            

            //create the output provider
            final DataProvider outProvider;
            try {
                final DataProviderFactory factory = DataProviders.getInstance().getFactory("coverage-store");
                final ParameterValueGroup pparams = factory.getProviderDescriptor().createValue();
                ParametersExt.getOrCreateValue(pparams, ProviderParameters.SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue(providerID);
                ParametersExt.getOrCreateValue(pparams, ProviderParameters.SOURCE_TYPE_DESCRIPTOR.getName().getCode()).setValue("coverage-store");
                final ParameterValueGroup choiceparams = ParametersExt.getOrCreateGroup(pparams, factory.getStoreDescriptor().getName().getCode());
                final ParameterValueGroup xmlpyramidparams = ParametersExt.getOrCreateGroup(choiceparams, XMLCoverageStoreFactory.PARAMETERS_DESCRIPTOR.getName().getCode());
                ParametersExt.getOrCreateValue(xmlpyramidparams, XMLCoverageStoreFactory.PATH.getName().getCode()).setValue(pyramidFolder.toURL());
                ParametersExt.getOrCreateValue(xmlpyramidparams, XMLCoverageStoreFactory.NAMESPACE.getName().getCode()).setValue("no namespace");
                outProvider = DataProviders.getInstance().createProvider(providerID, factory, pparams);
            } catch (Exception ex) {
                throw new ProcessException("Failed to create pyramid provider "+ex.getMessage(), this, ex);
            }

            //create the output pyramid coverage reference
            CoverageStore pyramidStore = (CoverageStore) outProvider.getMainStore();
            XMLCoverageReference outputRef;
            Name name = new DefaultName(coverageBaseName);
            try{
                outputRef = (XMLCoverageReference) pyramidStore.create(name);
                name = outputRef.getName();
                outputRef.setPackMode(ViewType.RENDERED);
                outputRef.setPreferredFormat(tileFormat);
            }catch(DataStoreException ex){
                Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                throw new ProcessException("Failed to create pyramid layer "+ex.getMessage(), this, ex);
            }

            //prepare the pyramid and mosaics
            final int tileSize = 256;
            final Dimension tileDim = new Dimension(tileSize, tileSize);
            try {
                CoverageUtilities.getOrCreatePyramid(outputRef, dataEnv, tileDim, scales);
            } catch (Exception ex) {
                Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                throw new ProcessException("Failed to create pyramid and mosaics in store "+ex.getMessage(), this, ex);
            }


            //get the coverage reference after reload, otherwise this won't be the same reference
            pyramidStore = (CoverageStore) outProvider.getMainStore();
            try{
                outputRef = (XMLCoverageReference) pyramidStore.getCoverageReference(name);
            }catch(DataStoreException ex){
                Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                throw new ProcessException("Failed to create pyramid layer "+ex.getMessage(), this, ex);
            }

            final MutableStyle style;
            try {
                style = styleBusiness.getStyle("sld", styleName);
            } catch (TargetNotFoundException ex){
                throw new ProcessException("Style not found: "+ ex.getMessage(), this, ex);
            }
            
            //get the rendering process
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
            input.parameter("tilesize").setValue(tileDim);
            input.parameter("scales").setValue(scales);
            input.parameter("container").setValue(outputRef);
            final org.geotoolkit.process.Process p = desc.createProcess(input);
            p.call();
            outProvider.reload();
            
            if (domainId != null) {
                int count = domainRepository.addProviderDataToDomain(providerID, domainId );
                LOGGER.info("Added " + count + " data to domain " + domainId);
            }
        }
    }
    
    private List<GridCoverage2D> buildCoverages(final CoverageStore store) throws CancellationException,
            DataStoreException {
        final List<GridCoverage2D> coverages = new ArrayList<>(0);

        for (Name name : store.getNames()) {
            final CoverageReference ref = store.getCoverageReference(name);
            final GridCoverageReader reader = ref.acquireReader();

            final GridCoverage2D coverage = (GridCoverage2D) reader.read(0,
                    null);
            final GridGeometry2D gridGeometry = (GridGeometry2D) reader
                    .getGridGeometry(ref.getImageIndex());

            if ((gridGeometry.getCoordinateReferenceSystem() instanceof NetcdfCRS)) {
                break;
            }

            final double widthGeometry = gridGeometry.getExtent2D().getWidth();
            final double heightGeometry = gridGeometry.getExtent2D().getHeight();

            final double userWidth     = 500;
            final double userHeight    = 500;

            // If coverage size higher than user selected size else add on an
            // other list to create separate file
            if (widthGeometry > userWidth || heightGeometry > userHeight) {
                coverages.add(coverage);
            }
        }
        return coverages;
    }
    
    
}
