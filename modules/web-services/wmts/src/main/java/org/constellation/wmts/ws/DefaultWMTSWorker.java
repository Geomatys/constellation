/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009 - 2012, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.wmts.ws;

import java.awt.Color;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import javax.xml.bind.JAXBException;

// Constellation dependencies
import org.constellation.Cstl;
import org.constellation.ServiceDef;
import org.constellation.configuration.Layer;
import org.constellation.portrayal.PortrayalUtil;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.StyleProviderProxy;
import org.constellation.wmts.visitor.CSVGraphicVisitor;
import org.constellation.wmts.visitor.GMLGraphicVisitor;
import org.constellation.wmts.visitor.HTMLGraphicVisitor;
import org.constellation.wmts.visitor.TextGraphicVisitor;
import org.constellation.util.Util;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.LayerWorker;
import org.constellation.ws.MimeType;

// Geotoolkit dependencies
import org.geotoolkit.coverage.*;
import org.geotoolkit.display.exception.PortrayalException;
import org.geotoolkit.display2d.service.CanvasDef;
import org.geotoolkit.display2d.service.SceneDef;
import org.geotoolkit.display2d.service.ViewDef;
import org.geotoolkit.display2d.service.VisitDef;
import org.geotoolkit.geometry.jts.JTSEnvelope2D;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.ows.xml.v110.*;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.IdentifiedObjects;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.util.TimeParser;
import org.geotoolkit.wmts.WMTSUtilities;
import org.geotoolkit.wmts.xml.WMTSMarshallerPool;
import org.geotoolkit.wmts.xml.v100.*;
import org.geotoolkit.xml.MarshallerPool;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

// GeoAPI dependencies
import org.opengis.coverage.Coverage;
import org.opengis.feature.type.Name;
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
    public DefaultWMTSWorker(String id, File configurationDirectory) {
        super(id, configurationDirectory, ServiceDef.Specification.WMTS);
        if (isStarted) {
            LOGGER.log(Level.INFO, "WMTS worker {0} running", id);
        }
        //listen to changes on the providers to clear the getcapabilities cache
        LayerProviderProxy.getInstance().addPropertyListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                refreshUpdateSequence();
            }
        });
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

        //set the current updateSequence parameter
        final boolean returnUS = returnUpdateSequenceDocument(requestCapabilities.getUpdateSequence());
        if (returnUS) {
            return new Capabilities("1.0.0", getCurrentUpdateSequence());
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

        //we prepare the response document
        Capabilities c           = null;
        ServiceIdentification si = null;
        ServiceProvider       sp = null;
        OperationsMetadata    om = null;
        ContentsType        cont = null;
        List<Themes>      themes = null;

        SectionsType sections = requestCapabilities.getSections();
        if (sections == null) {
            sections = new SectionsType(SectionsType.getExistingSections("1.1.1"));
        }

        // we load the skeleton capabilities
        final Capabilities skeletonCapabilities = (Capabilities) getStaticCapabilitiesObject("1.0.0", "WMTS");
        if (skeletonCapabilities == null) {
            throw new CstlServiceException("Unable to find the capabilities skeleton", NO_APPLICABLE_CODE);
        }

        //we enter the information for service identification.
        if (sections.containsSection("ServiceIdentification") || sections.containsSection("All")) {
            si = skeletonCapabilities.getServiceIdentification();
        }

        //we enter the information for service provider.
        if (sections.containsSection("ServiceProvider") || sections.containsSection("All")) {
            sp = skeletonCapabilities.getServiceProvider();
        }

        //we enter the operation Metadata
        if (sections.containsSection("OperationsMetadata") || sections.containsSection("All")) {
           om = WMTSConstant.OPERATIONS_METADATA;
           //we update the URL
           om.updateURL(getServiceUrl());

        }

        if (sections.containsSection("Contents") || sections.containsSection("All")) {

            // Build the list of layers
            final List<LayerType> outputLayers = new ArrayList<LayerType>();
            // and the list of matrix set
            final List<TileMatrixSet> tileSets = new ArrayList<TileMatrixSet>();

            final Map<Name,Layer> declaredLayers = getLayers();

            for(final Name n : declaredLayers.keySet()){
                final LayerDetails details = LayerProviderProxy.getInstance().get(n);
                final Layer configlayer = declaredLayers.get(n);
                final Object origin = details.getOrigin();
                if(!(origin instanceof CoverageReference)){
                    //WMTS only handle CoverageRefenrece object
                    continue;
                }
                final CoverageReference ref = (CoverageReference) origin;
                if(!(ref instanceof PyramidalModel)){
                    //WMTS only handle PyramidalModel
                    continue;
                }

                try{
                    final PyramidalModel model = (PyramidalModel) ref;
                    final PyramidSet set = model.getPyramidSet();
                    String name;
                    if (configlayer.getAlias() != null && !configlayer.getAlias().isEmpty()) {
                        name = configlayer.getAlias().trim().replaceAll(" ", "_");
                    } else {
                        name = n.getLocalPart();
                    }

                    Envelope env = set.getEnvelope();

                    env = CRS.transform(env, DefaultGeographicCRS.WGS84);

                    final BoundingBoxType bbox = new WGS84BoundingBoxType(
                            env.getMinimum(0),
                            env.getMinimum(1),
                            env.getMaximum(0),
                            env.getMaximum(1));

                    final List<Dimension> dims = new ArrayList<Dimension>();

                    final LayerType outputLayer = new LayerType(
                            name,
                            "remarks",
                            bbox,
                            Collections.EMPTY_LIST,
                            dims);

                    outputLayer.setTitle(name);
                    outputLayer.setAbstract(name);

                    for(Pyramid pr : set.getPyramids()){
                        final TileMatrixSet tms = new TileMatrixSet();
                        tms.setIdentifier(new CodeType(pr.getId()));
                        tms.setSupportedCRS(IdentifiedObjects.getIdentifier(pr.getCoordinateReferenceSystem()));

                        final List<TileMatrix> tm = new ArrayList<TileMatrix>();
                        final double[] scales = pr.getScales();
                        for(int i=0; i<scales.length; i++){
                            final GridMosaic mosaic = pr.getMosaic(i);
                            double scale = mosaic.getScale();
                            //convert scale in the strange WMTS scale denominator
                            scale = WMTSUtilities.toScaleDenominator(pr.getCoordinateReferenceSystem(), scale);
                            final TileMatrix matrix = new TileMatrix();
                            matrix.setIdentifier(new CodeType(mosaic.getId()));
                            matrix.setScaleDenominator(scale);
                            matrix.setMatrixWidth(mosaic.getGridSize().width);
                            matrix.setMatrixHeight(mosaic.getGridSize().height);
                            matrix.setTileWidth(mosaic.getTileSize().width);
                            matrix.setTileHeight(mosaic.getTileSize().height);
                            matrix.getTopLeftCorner().add(mosaic.getUpperLeftCorner().getX());
                            matrix.getTopLeftCorner().add(mosaic.getUpperLeftCorner().getY());
                            tm.add(matrix);
                        }
                        tms.setTileMatrix(tm);

                        final TileMatrixSetLink tmsl = new TileMatrixSetLink();
                        tmsl.setTileMatrixSet(pr.getId());
                        outputLayer.getTileMatrixSetLink().add(tmsl);
                        tileSets.add(tms);
                    }

                    outputLayers.add(outputLayer);
                } catch(DataStoreException ex) {
                    LOGGER.log(Level.WARNING, ex.getMessage(),ex);
                } catch(TransformException ex) {
                    LOGGER.log(Level.WARNING, ex.getMessage(),ex);
                }
            }

            cont = new ContentsType();
            cont.setLayers(outputLayers);
            cont.setTileMatrixSet(tileSets);
        }

        if (sections.containsSection("Themes") || sections.containsSection("All")) {
            // TODO

            themes = new ArrayList<Themes>();
        }

        c = new Capabilities(si, sp, om, "1.0.0", null, cont, themes);

        LOGGER.log(logLevel, "getCapabilities processed in {0}ms.\n", (System.currentTimeMillis() - start));
        return c;


    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFeatureInfo(GetFeatureInfo request) throws CstlServiceException {

        //       -- get the List of layer references
        final GetTile getTile = request.getGetTile();

        final Name layerName        = Util.parseLayerName(getTile.getLayer());
        final LayerDetails layerRef = getLayerReference(layerName);

        Coverage c = null;

        // build an equivalent style List
        final String styleName = getTile.getStyle();

        final MutableStyle style        = getStyle(styleName);
        //       -- create the rendering parameter Map
        Double elevation =  null;
        Date time        = null;
        List<DimensionNameValue> dimensions = getTile.getDimensionNameValue();
        for (DimensionNameValue dimension : dimensions) {
            if (dimension.getName().equalsIgnoreCase("elevation")) {
                try {
                    elevation = Double.parseDouble(dimension.getValue());
                } catch (NumberFormatException ex) {
                    throw new CstlServiceException("Unable to perse the elevation value", INVALID_PARAMETER_VALUE, "elevation");
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
        final Map<String, Object> params       = new HashMap<String, Object>();
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
        final JTSEnvelope2D refEnv             = new JTSEnvelope2D(c.getEnvelope());
        final double azimuth                   = 0;//request.getAzimuth();
        final ViewDef vdef = new ViewDef(refEnv,azimuth);


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
        final TextGraphicVisitor visitor;
        if (infoFormat.equalsIgnoreCase(MimeType.TEXT_PLAIN)) {
            // TEXT / PLAIN
            visitor = new CSVGraphicVisitor(request);
        } else if (infoFormat.equalsIgnoreCase(MimeType.TEXT_HTML)) {
            // TEXT / HTML
            visitor = new HTMLGraphicVisitor(request, layerRef);
        } else if (infoFormat.equalsIgnoreCase(MimeType.APP_GML) || infoFormat.equalsIgnoreCase(MimeType.TEXT_XML) ||
                   infoFormat.equalsIgnoreCase(MimeType.APP_XML) || infoFormat.equalsIgnoreCase("xml") ||
                   infoFormat.equalsIgnoreCase("gml"))
        {
            // GML
            visitor = new GMLGraphicVisitor(request);
        } else {
            throw new CstlServiceException("MIME type " + infoFormat + " is not accepted by the service.\n" +
                    "You have to choose between: "+ MimeType.TEXT_PLAIN +", "+ MimeType.TEXT_HTML +", "+ MimeType.APP_GML +", "+ "gml" +
                    ", "+ MimeType.APP_XML +", "+ "xml"+", "+ MimeType.TEXT_XML,
                    INVALID_FORMAT, "infoFormat");
        }

        final VisitDef visitDef = new VisitDef();
        visitDef.setArea(selectionArea);
        visitDef.setVisitor(visitor);


        // We now build the response, according to the format chosen.
        try {
        	Cstl.getPortrayalService().visit(sdef,vdef,cdef,visitDef);
        } catch (PortrayalException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }

        return visitor.getResult();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TileReference getTile(GetTile request) throws CstlServiceException {

        //1 LAYER NOT USED FOR NOW
        Name layerName = Util.parseLayerName(request.getLayer());

        //switch alias -> name
        final Map<Name,Layer> declaredLayers = getLayers();
        if(declaredLayers != null){
            for(Entry<Name,Layer> entry : declaredLayers.entrySet()){
                if(layerName.getLocalPart().equalsIgnoreCase(entry.getValue().getAlias())){
                    layerName = entry.getKey();
                }
            }
        }

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
        final MutableStyle style  = getStyle(styleName);


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
            final LayerDetails details = LayerProviderProxy.getInstance().get(layerName);

            if(details == null){
                throw new CstlServiceException("No layer for name : " + layerName , INVALID_PARAMETER_VALUE, "layerName");
            }

            final Object origin = details.getOrigin();
            if(!(origin instanceof CoverageReference)){
                //WMTS only handle CoverageRefenrece object
                throw new CstlServiceException("Unvalid layer :" + layerName + " , layer is not a pyramid model" + layerName, INVALID_PARAMETER_VALUE, "layerName");
            }
            final CoverageReference ref = (CoverageReference) origin;
            if(!(ref instanceof PyramidalModel)){
                //WMTS only handle PyramidalModel
                throw new CstlServiceException("Unvalid layer :" + layerName + " , layer is not a pyramid model" + layerName, INVALID_PARAMETER_VALUE, "layerName");
            }


            final PyramidalModel model = (PyramidalModel) ref;
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
            for(int i=0;i<pyramid.getScales().length;i++){
                final GridMosaic gm = pyramid.getMosaic(i);
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

    private static MutableStyle getStyle(final String styleName) throws CstlServiceException {
        final MutableStyle style;
        if (styleName != null && !styleName.isEmpty()) {
            //try to grab the style if provided
            //a style has been given for this layer, try to use it
            style = StyleProviderProxy.getInstance().get(styleName);
            if (style == null) {
                throw new CstlServiceException("Style provided not found.", STYLE_NOT_DEFINED);
            }
        } else {
            //no defined styles, use the favorite one, let the layer get it himself.
            style = null;
        }
        return style;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
    }
}
