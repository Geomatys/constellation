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
package org.constellation.wmts.ws;

import java.awt.Color;
import java.awt.Rectangle;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import org.apache.sis.storage.DataStoreException;

// Constellation dependencies
import org.constellation.ServiceDef;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.Layer;
import org.constellation.map.featureinfo.FeatureInfoFormat;
import org.constellation.map.featureinfo.FeatureInfoUtilities;
import org.constellation.portrayal.PortrayalUtil;
import org.constellation.provider.Data;
import org.constellation.util.DataReference;
import org.constellation.util.Util;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.LayerWorker;
import org.constellation.ws.MimeType;

// Geotoolkit dependencies
import org.geotoolkit.coverage.*;
import org.geotoolkit.display.PortrayalException;
import org.geotoolkit.display2d.service.CanvasDef;
import org.geotoolkit.display2d.service.SceneDef;
import org.geotoolkit.display2d.service.ViewDef;
import org.geotoolkit.geometry.jts.JTSEnvelope2D;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.ows.xml.AbstractCapabilitiesCore;
import org.geotoolkit.ows.xml.v110.*;
import org.geotoolkit.referencing.CRS;
import org.apache.sis.referencing.IdentifiedObjects;
import org.apache.sis.referencing.CommonCRS;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.util.TimeParser;
import org.geotoolkit.wmts.WMTSUtilities;
import org.geotoolkit.wmts.xml.WMTSMarshallerPool;
import org.geotoolkit.wmts.xml.v100.*;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.dto.Service;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

// GeoAPI dependencies
import org.opengis.coverage.Coverage;
import org.geotoolkit.feature.type.Name;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.operation.TransformException;

/**
 * Working part of the WMTS service.
 *
 * @todo Implements it.
 *
 * @version $Id$
 *
 * @author Cédric Briançon (Geomatys)
 * @author Guilhem Legal (Geomatys)
 * @since 0.3
 */
public class DefaultWMTSWorker extends LayerWorker implements WMTSWorker {

    /**
     * A list of supported MIME type
     */
    private static final List<String> ACCEPTED_OUTPUT_FORMATS;
    static {
        ACCEPTED_OUTPUT_FORMATS = Arrays.asList(MimeType.TEXT_XML,
                                                MimeType.APP_XML,
                                                MimeType.TEXT_PLAIN);
    }

    /**
     * Instanciates the working class for a SOAP client, that do request on a SOAP PEP service.
     */
    public DefaultWMTSWorker(final String id) {
        super(id, ServiceDef.Specification.WMTS);
        if (isStarted) {
            LOGGER.log(Level.INFO, "WMTS worker {0} running", id);
        }
    }

    @Override
    protected MarshallerPool getMarshallerPool() {
        return WMTSMarshallerPool.getInstance();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Capabilities getCapabilities(GetCapabilities requestCapabilities) throws CstlServiceException {
        LOGGER.log(logLevel, "getCapabilities request processing\n");
        final long start = System.currentTimeMillis();
        final String userLogin  = getUserLogin();

        //we verify the base request attribute
        if (requestCapabilities.getService() != null) {
            if (!requestCapabilities.getService().equals("WMTS")) {
                throw new CstlServiceException("service must be \"WMTS\"!",
                                                 INVALID_PARAMETER_VALUE, "service");
            }
        } else {
            throw new CstlServiceException("Service must be specified!",
                                             MISSING_PARAMETER_VALUE, "service");
        }
        final AcceptVersionsType versions = requestCapabilities.getAcceptVersions();
        if (versions != null) {
            if (!versions.getVersion().contains("1.0.0")){
                 throw new CstlServiceException("version available : 1.0.0",
                                             VERSION_NEGOTIATION_FAILED, "acceptVersion");
            }
        }

        final AcceptFormatsType formats = requestCapabilities.getAcceptFormats();
        if (formats != null && formats.getOutputFormat().size() > 0 ) {
            boolean found = false;
            for (String form: formats.getOutputFormat()) {
                if (ACCEPTED_OUTPUT_FORMATS.contains(form)) {
                    found = true;
                }
            }
            if (!found) {
                throw new CstlServiceException("accepted format : text/xml, application/xml",
                                                 INVALID_PARAMETER_VALUE, "acceptFormats");
            }
        }

        SectionsType sections = requestCapabilities.getSections();
        if (sections == null) {
            sections = new SectionsType(SectionsType.getExistingSections("1.1.1"));
        }

        //set the current updateSequence parameter
        final boolean returnUS = returnUpdateSequenceDocument(requestCapabilities.getUpdateSequence());
        if (returnUS) {
            return new Capabilities("1.0.0", getCurrentUpdateSequence());
        }

        // If the getCapabilities response is in cache, we just return it.
        final AbstractCapabilitiesCore cachedCapabilities = getCapabilitiesFromCache("1.0.0", null);
        if (cachedCapabilities != null) {
            return (Capabilities) cachedCapabilities.applySections(sections);
        }

        // we load the skeleton capabilities
        final Service skeleton = getStaticCapabilitiesObject("WMTS", null);
        final Capabilities skeletonCapabilities = (Capabilities) WMTSConstant.createCapabilities("1.0.0", skeleton);

         //we prepare the response document
        final ServiceIdentification si = skeletonCapabilities.getServiceIdentification();
        final ServiceProvider       sp = skeletonCapabilities.getServiceProvider();
        final OperationsMetadata    om = (OperationsMetadata) WMTSConstant.OPERATIONS_METADATA.clone();
        // TODO
        final List<Themes>      themes = new ArrayList<>();

        //we update the URL
        om.updateURL(getServiceUrl());

        // Build the list of layers
        final List<LayerType> outputLayers = new ArrayList<>();
        // and the list of matrix set
        final List<TileMatrixSet> tileSets = new ArrayList<>();

        final List<Layer> declaredLayers = getConfigurationLayers(userLogin);

       for (final Layer configLayer : declaredLayers){
            final Data details = getLayerReference(userLogin, configLayer.getName());
            final Object origin        = details.getOrigin();
            if(!(origin instanceof CoverageReference)){
                //WMTS only handle CoverageReference object
                LOGGER.log(Level.INFO, "Layer {0} has not a coverageReference origin. It will not be included in capabilities", configLayer.getName());
                continue;
            }
            final CoverageReference ref = (CoverageReference) origin;
            if(!(ref instanceof PyramidalCoverageReference)){
                //WMTS only handle PyramidalModel
                LOGGER.log(Level.INFO, "Layer {0} has not a PyramidalModel origin. It will not be included in capabilities", configLayer.getName());
                continue;
            }

            try{
                final PyramidalCoverageReference pmodel = (PyramidalCoverageReference) ref;
                final PyramidSet set = pmodel.getPyramidSet();
                final String name;
                if (configLayer.getAlias() != null && !configLayer.getAlias().isEmpty()) {
                    name = configLayer.getAlias().trim().replaceAll(" ", "_");
                } else {
                    name = configLayer.getName().getLocalPart();
                }

                Envelope env = set.getEnvelope();

                env = CRS.transform(env, CommonCRS.WGS84.normalizedGeographic());

                final BoundingBoxType bbox = new WGS84BoundingBoxType(
                        env.getMinimum(0),
                        env.getMinimum(1),
                        env.getMaximum(0),
                        env.getMaximum(1));

                final List<Dimension> dims  = new ArrayList<>();
                final LayerType outputLayer = new LayerType(
                        name,
                        name,
                        name,
                        bbox,
                        WMTSConstant.DEFAULT_STYLES,
                        dims);

                final List<String> pformats = set.getFormats();
                outputLayer.setFormat(pformats);

                final List<URLTemplateType> resources = new ArrayList<>();
                for (String pformat : pformats) {
                    String url = getServiceUrl();
                    url = url.substring(0, url.length() - 1) + "/" + name + "/{tileMatrixSet}/{tileMatrix}/{tileRow}/{tileCol}.{format}";
                    final URLTemplateType tileURL = new URLTemplateType(pformat, "tile", url);
                    resources.add(tileURL);
                }
                outputLayer.setResourceURL(resources);

                for(Pyramid pr : set.getPyramids()){
                    final TileMatrixSet tms = new TileMatrixSet();
                    tms.setIdentifier(new CodeType(pr.getId()));
                    tms.setSupportedCRS(IdentifiedObjects.getIdentifierOrName(pr.getCoordinateReferenceSystem()));

                    final List<TileMatrix> tm = new ArrayList<>();
                    final double[] scales = pr.getScales();
                    for(int i=0; i<scales.length; i++){
                        final GridMosaic mosaic = pr.getMosaics(i).iterator().next();
                        double scale = mosaic.getScale();
                        //convert scale in the strange WMTS scale denominator
                        scale = WMTSUtilities.toScaleDenominator(pr.getCoordinateReferenceSystem(), scale);
                        final TileMatrix matrix = new TileMatrix();
                        matrix.setIdentifier(new CodeType(mosaic.getId()));
                        matrix.setScaleDenominator(scale);
                        matrix.setMatrixDimension(mosaic.getGridSize());
                        matrix.setTileDimension(mosaic.getTileSize());
                        matrix.getTopLeftCorner().add(mosaic.getUpperLeftCorner().getOrdinate(0));
                        matrix.getTopLeftCorner().add(mosaic.getUpperLeftCorner().getOrdinate(1));
                        tm.add(matrix);
                    }
                    tms.setTileMatrix(tm);

                    final TileMatrixSetLink tmsl = new TileMatrixSetLink(pr.getId());
                    outputLayer.addTileMatrixSetLink(tmsl);
                    tileSets.add(tms);
                }

                outputLayers.add(outputLayer);
            } catch(DataStoreException | TransformException ex) {
                LOGGER.log(Level.WARNING, ex.getMessage(),ex);
            }
        }
        final ContentsType cont = new ContentsType(outputLayers, tileSets);

        // put full capabilities in cache
        final Capabilities c = new Capabilities(si, sp, om, "1.0.0", null, cont, themes);
        putCapabilitiesInCache("1.0.0", null, c);
        LOGGER.log(logLevel, "getCapabilities processed in {0}ms.\n", (System.currentTimeMillis() - start));
        return (Capabilities) c.applySections(sections);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map.Entry<String, Object> getFeatureInfo(GetFeatureInfo request) throws CstlServiceException {

        //       -- get the List of layer references
        final GetTile getTile       = request.getGetTile();
        final String userLogin      = getUserLogin();
        final Name layerName        = Util.parseLayerName(getTile.getLayer());
        final Data layerRef = getLayerReference(userLogin, layerName);
        final Layer configLayer     = getConfigurationLayer(layerName, userLogin);

        // build an equivalent style List
        final String styleName       = getTile.getStyle();
        final DataReference styleRef = configLayer.getStyle(styleName);
        final MutableStyle style     = getStyle(styleRef);


        Coverage c = null;
        //       -- create the rendering parameter Map
        Double elevation =  null;
        Date time        = null;
        final List<DimensionNameValue> dimensions = getTile.getDimensionNameValue();
        for (DimensionNameValue dimension : dimensions) {
            if (dimension.getName().equalsIgnoreCase("elevation")) {
                try {
                    elevation = Double.parseDouble(dimension.getValue());
                } catch (NumberFormatException ex) {
                    throw new CstlServiceException("Unable to parse the elevation value", INVALID_PARAMETER_VALUE, "elevation");
                }
            }
            if (dimension.getName().equalsIgnoreCase("time")) {
                try {
                    time = TimeParser.toDate(dimension.getValue());
                } catch (ParseException ex) {
                    throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE, "time");
                }
            }
        }
        final Map<String, Object> params = new HashMap<>();
        params.put("ELEVATION", elevation);
        params.put("TIME", time);
        final SceneDef sdef = new SceneDef();

        try {
            final MapContext context = PortrayalUtil.createContext(layerRef, style, params);
            sdef.setContext(context);
        } catch (PortrayalException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }

        // 2. VIEW
        final JTSEnvelope2D refEnv = new JTSEnvelope2D(c.getEnvelope());
        final double azimuth       = 0;//request.getAzimuth();
        final ViewDef vdef         = new ViewDef(refEnv,azimuth);


        // 3. CANVAS
        final java.awt.Dimension canvasDimension = null;//request.getSize();
        final Color background = null;
        final CanvasDef cdef = new CanvasDef(canvasDimension,background);

        // 4. SHAPE
        //     a
        final int pixelTolerance = 3;
        final int i = request.getI();
        final int j = request.getJ();
        if (i < 0 || i > canvasDimension.width) {
            throw new CstlServiceException("The requested point has an invalid X coordinate.", INVALID_POINT);
        }
        if (j < 0 || j > canvasDimension.height) {
            throw new CstlServiceException("The requested point has an invalid Y coordinate.", INVALID_POINT);
        }
        final Rectangle selectionArea = new Rectangle( request.getI()-pixelTolerance,
        		                               request.getJ()-pixelTolerance,
        		                               pixelTolerance*2,
        		                               pixelTolerance*2);

        // 5. VISITOR
        String infoFormat = request.getInfoFormat();
        if (infoFormat == null) {
            //Should not happen since the info format parameter is mandatory for the GetFeatureInfo request.
            infoFormat = MimeType.TEXT_PLAIN;
        }

        FeatureInfoFormat featureInfo = null;
        try {
            featureInfo = FeatureInfoUtilities.getFeatureInfoFormat( getConfiguration(), configLayer, infoFormat);
        } catch (ClassNotFoundException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        } catch (ConfigurationException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }

        if (featureInfo == null) {
            throw new CstlServiceException("INFO_FORMAT="+infoFormat+" not supported for layers : "+layerName, NO_APPLICABLE_CODE);
        }

        try {
            final Object result = featureInfo.getFeatureInfo(sdef, vdef, cdef, selectionArea, request);
            return new AbstractMap.SimpleEntry<>(infoFormat, result);
        } catch (PortrayalException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TileReference getTile(final GetTile request) throws CstlServiceException {

        //1 LAYER NOT USED FOR NOW
        final Name layerName = Util.parseLayerName(request.getLayer());
        final String userLogin  = getUserLogin();
        final Layer configLayer = getConfigurationLayer(layerName, userLogin);


        // 2. PARAMETERS NOT USED FOR NOW
        Double elevation =  null;
        Date time        = null;
        List<DimensionNameValue> dimensions = request.getDimensionNameValue();
        for (DimensionNameValue dimension : dimensions) {
            if (dimension.getName().equalsIgnoreCase("elevation")) {
                try {
                    elevation = Double.parseDouble(dimension.getValue());
                } catch (NumberFormatException ex) {
                    throw new CstlServiceException("Unable to parse the elevation value", INVALID_PARAMETER_VALUE, "elevation");
                }
            }
            if (dimension.getName().equalsIgnoreCase("time")) {
                try {
                    time = TimeParser.toDate(dimension.getValue());
                } catch (ParseException ex) {
                    throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE, "time");
                }
            }
        }

        // 3 STYLE NOT USED FOR NOW
        final String styleName    = request.getStyle();
        final DataReference styleRef = configLayer.getStyle(styleName);
        final MutableStyle style  = getStyle(styleRef);


        // 4. We get the parameters
        final int columnIndex         = request.getTileCol();
        final int rowIndex            = request.getTileRow();
        final String matrixSetName    = request.getTileMatrixSet();
        final String level            = request.getTileMatrix();


        // 5. we verify the parameters
        if (columnIndex < 0 || rowIndex < 0) {
            throw new CstlServiceException("TileCol and TileRow must be > 0", INVALID_PARAMETER_VALUE);
        }


        try{
            final Data details = getLayerReference(userLogin, layerName);
            if(details == null){
                throw new CstlServiceException("No layer for name : " + layerName , INVALID_PARAMETER_VALUE, "layerName");
            }

            final Object origin = details.getOrigin();
            if(!(origin instanceof CoverageReference)){
                //WMTS only handle CoverageRefenrece object
                throw new CstlServiceException("Unvalid layer :" + layerName + " , layer is not a pyramid model" + layerName, INVALID_PARAMETER_VALUE, "layerName");
            }
            final CoverageReference ref = (CoverageReference) origin;
            if(!(ref instanceof PyramidalCoverageReference)){
                //WMTS only handle PyramidalCoverageReference
                throw new CstlServiceException("Unvalid layer :" + layerName + " , layer is not a pyramid model" + layerName, INVALID_PARAMETER_VALUE, "layerName");
            }


            final PyramidalCoverageReference model = (PyramidalCoverageReference) ref;
            final PyramidSet set = model.getPyramidSet();
            Pyramid pyramid = null;
            for(Pyramid pr : set.getPyramids()){
                if(pr.getId().equals(matrixSetName)){
                    pyramid = pr;
                    break;
                }
            }
            if(pyramid == null){
                throw new CstlServiceException("Undefined matrixSet:" + matrixSetName + " for layer:" + layerName, INVALID_PARAMETER_VALUE, "tilematrixset");
            }
            GridMosaic mosaic = null;
            for(GridMosaic gm : pyramid.getMosaics()){
                if(gm.getId().equals(level)){
                    mosaic = gm;
                    break;
                }
            }
            if (mosaic == null) {
                throw new CstlServiceException("Undefined matrix:" + level + " for matrixSet:" + matrixSetName, INVALID_PARAMETER_VALUE, "tilematrix");
            }

            if (columnIndex >= mosaic.getGridSize().width) {
                throw new CstlServiceException("TileCol out of band" + columnIndex + " > " +  mosaic.getGridSize().width, INVALID_PARAMETER_VALUE, "tilecol");
            }
            if (rowIndex >= mosaic.getGridSize().height) {
                throw new CstlServiceException("TileRow out of band" + rowIndex + " > " +  mosaic.getGridSize().height, INVALID_PARAMETER_VALUE, "tilerow");
            }

            final Map hints = new HashMap();
            final TileReference tile = mosaic.getTile(columnIndex, rowIndex, hints);
            return tile;

        } catch(DataStoreException ex) {
            throw new CstlServiceException("Unexpected error : " + ex.getMessage(), ex , NO_APPLICABLE_CODE);
        }

    }
}
