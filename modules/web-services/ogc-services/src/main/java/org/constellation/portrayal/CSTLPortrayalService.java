/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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
package org.constellation.portrayal;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.constellation.ws.WebServiceException;
import org.constellation.ws.ExceptionCode;
import org.constellation.ws.Service;
import org.constellation.ws.ServiceVersion;
import org.constellation.gml.v311.DirectPositionType;
import org.constellation.gml.v311.EnvelopeEntry;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.NamedLayerDP;
import org.constellation.query.wcs.WCSQuery;
import org.constellation.query.wms.GetFeatureInfo;
import org.constellation.query.wms.GetMap;
import org.constellation.query.wms.WMSQuery;
import org.constellation.wcs.AbstractGetCoverage;

import org.geotools.display.canvas.BufferedImageCanvas2D;
import org.geotools.display.canvas.CanvasController2D;
import org.geotools.display.canvas.control.FailOnErrorMonitor;
import org.geotools.display.exception.PortrayalException;
import org.geotools.display.primitive.GraphicFeatureJ2D;
import org.geotools.display.primitive.GraphicJ2D;
import org.geotools.display.renderer.ContextRenderer2D;
import org.geotools.display.renderer.DefaultContextRenderer2D;
import org.geotools.display.renderer.Go2rendererHints;
import org.geotools.display.renderer.stateless.CoverageGraphicLayerJ2D;
import org.geotools.display.renderer.stateless.FeatureGraphicLayerJ2D;
import org.geotools.display.service.DefaultPortrayalService;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.sld.MutableLayer;
import org.geotools.sld.MutableLayerStyle;
import org.geotools.sld.MutableNamedLayer;
import org.geotools.sld.MutableNamedStyle;
import org.geotools.sld.MutableStyledLayerDescriptor;
import org.geotools.style.MutableStyle;
import org.geotools.util.MeasurementRange;

import org.opengis.display.primitive.Graphic;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import static org.constellation.ws.ExceptionCode.*;


/**
 * Portrayal service, extends the GT portrayal service by adding support
 * to reconize Named layers and styles from constellation data providers.
 *
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public class CSTLPortrayalService extends DefaultPortrayalService {

    private static final int PIXEL_TOLERANCE = 3;
    private static final MapContextInfoExtractor extractor = new MapContextInfoExtractor();

    private static final Logger LOGGER = Logger.getLogger("org/constellation/portrayal/CSTLPortrayalService");
    
    /**
     * static instance, thread singleton.
     */
    private static final ThreadLocal<CSTLPortrayalService> instances = new ThreadLocal<CSTLPortrayalService>() {
        @Override
        protected CSTLPortrayalService initialValue() {
            return new CSTLPortrayalService();
        }
    };
    
    /**
     * Data provider for layers.
     */
    private final NamedLayerDP LAYERDP = NamedLayerDP.getInstance();
    
    private final ReportMonitor monitor = new ReportMonitor();
        
    private final BufferedImageCanvas2D canvas;
    private final ContextRenderer2D renderer;
    private final MapContext context;
    
    
    private CSTLPortrayalService(){
        canvas = new  BufferedImageCanvas2D(new Dimension(1,1),null);
        renderer = new DefaultContextRenderer2D(canvas, false);
        context = MAP_BUILDER.createContext(DefaultGeographicCRS.WGS84);
        
        canvas.setRenderer(renderer);
        canvas.getController().setAutoRepaint(false);
        
        //disable mutlithread rendering, to avoid possibility of several buffers created
        canvas.setRenderingHint(Go2rendererHints.KEY_MULTI_THREAD, Go2rendererHints.MULTI_THREAD_OFF);
        
        //we specifically say to not repect X/Y proportions
        canvas.getController().setAxisProportions(Double.NaN);
        
        renderer.setContext(context);
        
    }

    public BufferedImage portray(final AbstractGetCoverage query)
            throws PortrayalException, WebServiceException
    {
        if (query == null) {
            throw new NullPointerException("The GetMap query cannot be null. The portray() method" +
                    " is not well used here.");
        }
        final List<String> layers = new ArrayList<String>();
        final ServiceVersion version;
        final Map<String, Object> params = new HashMap<String, Object>();
        final ReferencedEnvelope refEnv;
        final Dimension dimension;
        if (query instanceof org.constellation.wcs.v100.GetCoverage) {
            final org.constellation.wcs.v100.GetCoverage query100 = (org.constellation.wcs.v100.GetCoverage) query;
            layers.add(query100.getSourceCoverage());
            version = new ServiceVersion(Service.WCS, query100.getVersion());
            // Decode the CRS.
            String crsCode = query100.getOutput().getCrs().getValue();
            if (crsCode == null) {
                crsCode = query100.getDomainSubset().getSpatialSubSet().getEnvelope().getSrsName();
            }
            if (!crsCode.contains(":")) {
                crsCode = "EPSG:" + crsCode;
            }
            final CoordinateReferenceSystem crs;
            try {
                crs = CRS.decode(crsCode);
            } catch (NoSuchAuthorityCodeException ex) {
                throw new WebServiceException(ex, INVALID_CRS, version);
            } catch (FactoryException ex) {
                throw new WebServiceException(ex, INVALID_CRS, version);
            }
            // Calculate the bbox.
            final EnvelopeEntry envEntry = query100.getDomainSubset().getSpatialSubSet().getEnvelope();
            final List<DirectPositionType> positions = envEntry.getPos();
            refEnv = new ReferencedEnvelope(positions.get(0).getValue().get(0), positions.get(0).getValue().get(1),
                                            positions.get(1).getValue().get(0), positions.get(1).getValue().get(1), crs);
            // Additionnal parameters.
            if (query100.getDomainSubset().getTemporalSubSet() != null &&
                query100.getDomainSubset().getTemporalSubSet().getTimePositionOrTimePeriod().size() > 0)
            {
                params.put(WCSQuery.KEY_TIME, query100.getDomainSubset().getTemporalSubSet().getTimePositionOrTimePeriod().get(0));
            }
            if (envEntry.getPos().get(0).getValue().size() > 2) {
                params.put(WMSQuery.KEY_ELEVATION, positions.get(2).getValue().get(0));
            }
            final int width = 
                    query100.getDomainSubset().getSpatialSubSet().getGrid().getLimits().getGridEnvelope().getHigh().get(0).intValue() -
                    query100.getDomainSubset().getSpatialSubSet().getGrid().getLimits().getGridEnvelope().getLow().get(0).intValue();
            final int height =
                    query100.getDomainSubset().getSpatialSubSet().getGrid().getLimits().getGridEnvelope().getHigh().get(1).intValue() -
                    query100.getDomainSubset().getSpatialSubSet().getGrid().getLimits().getGridEnvelope().getLow().get(1).intValue();
            dimension = new Dimension(width, height);
        } else {
            final org.constellation.wcs.v111.GetCoverage query111 = (org.constellation.wcs.v111.GetCoverage) query;
            layers.add(query111.getIdentifier().getValue());
            version = new ServiceVersion(Service.WCS, query111.getVersion());
            // Decode the CRS.
            String crsCode = query111.getOutput().getGridCRS().getSrsName().getValue();
            if (crsCode == null) {
                crsCode = query111.getDomainSubset().getBoundingBox().getValue().getCrs();
            }
            if (!crsCode.contains(":")) {
                crsCode = "EPSG:" + crsCode;
            }
            final CoordinateReferenceSystem crs;
            try {
                crs = CRS.decode(crsCode);
            } catch (NoSuchAuthorityCodeException ex) {
                throw new WebServiceException(ex, INVALID_CRS, version);
            } catch (FactoryException ex) {
                throw new WebServiceException(ex, INVALID_CRS, version);
            }
            // Calculate the bbox.
            final List<Double> lowerCornerCoords = query111.getDomainSubset().getBoundingBox().getValue().getLowerCorner();
            final List<Double> upperCornerCoords = query111.getDomainSubset().getBoundingBox().getValue().getUpperCorner();
            refEnv = new ReferencedEnvelope(lowerCornerCoords.get(0), lowerCornerCoords.get(1),
                                            upperCornerCoords.get(0), upperCornerCoords.get(1), crs);
            // Additionnal parameters.
            if (query111.getDomainSubset().getTemporalSubset() != null &&
                query111.getDomainSubset().getTemporalSubset().getTimePositionOrTimePeriod().size() > 0)
            {
                params.put(WCSQuery.KEY_TIME, query111.getDomainSubset().getTemporalSubset().getTimePositionOrTimePeriod().get(0));
            }
            if (query111.getDomainSubset().getBoundingBox().getValue().getDimensions().intValue() > 2) {
                params.put(WMSQuery.KEY_ELEVATION, lowerCornerCoords.get(2));
            }
            // TODO: do the good calculation with grid origin, grid offset and the envelope size.
            dimension = new Dimension((int) Math.round(query111.getOutput().getGridCRS().getGridOrigin().get(0)),
                    (int) Math.round(query111.getOutput().getGridCRS().getGridOrigin().get(1)));
        }
        updateContext(layers, version, null, null, params);
        //TODO horrible TRY CATCH to remove when the renderer will have a fine
        //error handeling. This catch doesnt happen in normal case, but because of
        //some strange behavior when deployed in web app, we sometimes catch runtimeException or
        //thread exceptions.
        try {
            return portrayUsingCache(refEnv, 0, null, dimension);
        } catch (Exception ex) {
            if (ex instanceof PortrayalException) {
                throw (PortrayalException)ex;
            } else if(ex instanceof WebServiceException) {
                throw (WebServiceException) ex;
            } else {
                throw new PortrayalException(ex);
            }
        }finally{
            canvas.clearCache();
            renderer.clearCache();
        }
    }

    /**
     * Makes the portray of a {@code GetMap} request.
     *
     * @param query A {@link GetMap} query. Should not be {@code null}.
     *
     * @throws PortrayalException
     * @throws WebServiceException if an error occurs during the creation of the map context
     */
    public BufferedImage portray(final GetMap query)
                            throws PortrayalException, WebServiceException
    {
        if (query == null) {
            throw new NullPointerException("The GetMap query cannot be null. The portray() method" +
                    " is not well used here.");
        }

        final List<String> layers              = query.getLayers();
        final List<String> styles              = query.getStyles();
        final MutableStyledLayerDescriptor sld = query.getSld();
        final Envelope contextEnv              = query.getEnvelope();
        final ReferencedEnvelope refEnv        = new ReferencedEnvelope(contextEnv);
        final String mime                      = query.getFormat();
        final ServiceVersion version           = query.getVersion();
        final Double elevation                 = query.getElevation();
        final Date time                        = query.getTime();
        final MeasurementRange dimRange        = query.getDimRange();
        final Dimension canvasDimension        = query.getSize();
        final double azimuth                   = query.getAzimuth();
        final Map<String, Object> params       = new HashMap<String, Object>();
        params.put(WMSQuery.KEY_ELEVATION, elevation);
        params.put(WMSQuery.KEY_DIM_RANGE, dimRange);
        params.put(WMSQuery.KEY_TIME, time);
        updateContext(layers, version, styles, sld, params);
        final Color background                 = (query.getTransparent()) ? null : query.getBackground();

        if (false) {
            //for debug
            final StringBuilder builder = new StringBuilder();
            builder.append("Layers => ");
            for(String layer : layers){
                builder.append(layer +",");
            }
            builder.append("\n");
            builder.append("Styles => ");
            for(String style : styles){
                builder.append(style +",");
            }
            builder.append("\n");
            builder.append("Context env => " + contextEnv.toString() + "\n");
            builder.append("Azimuth => " + azimuth + "\n");
            builder.append("Mime => " + mime.toString() + "\n");
            builder.append("Dimension => " + canvasDimension.toString() + "\n");
            builder.append("BGColor => " + background + "\n");
            builder.append("Transparent => " + query.getTransparent() + "\n");
            System.out.println(builder.toString());
        }

        //TODO horrible TRY CATCH to remove when the renderer will have a fine
        //error handeling. This catch doesnt happen in normal case, but because of
        //some strange behavior when deployed in web app, we sometimes catch runtimeException or
        //thread exceptions.
        try{
            return portrayUsingCache(refEnv, azimuth, background, canvasDimension);
        }catch(Exception ex){
            if(ex instanceof PortrayalException){
                throw (PortrayalException)ex;
            }else if( ex instanceof WebServiceException){
                throw (WebServiceException) ex;
            }else{
                throw new PortrayalException(ex);
            }
        }finally{
            canvas.clearCache();
            renderer.clearCache();
        }
    }

    public Map<String, List<String>> hit(final GetFeatureInfo query)
                            throws PortrayalException, WebServiceException
    {

        if (query == null) {
            throw new NullPointerException("The GetMap query cannot be null. The portray() method" +
                    " is not well used here.");
        }

        final Map<String, List<String>> values = new HashMap<String, List<String>>();
        final List<String> layers              = query.getQueryLayers();
        final List<String> styles              = query.getStyles();
        final MutableStyledLayerDescriptor sld = query.getSld();
        final Envelope contextEnv              = query.getEnvelope();
        final ReferencedEnvelope refEnv        = new ReferencedEnvelope(contextEnv);
        final String mime                      = query.getFormat();
        final ServiceVersion version           = query.getVersion();
        final Double elevation                 = query.getElevation();
        final Date time                        = query.getTime();
        final MeasurementRange dimRange        = query.getDimRange();
        final Dimension canvasDimension        = query.getSize();
        final double azimuth                   = query.getAzimuth();
        final int infoX                        = query.getX();
        final int infoY                        = query.getY();
        final Map<String, Object> params       = new HashMap<String, Object>();
        params.put(WMSQuery.KEY_ELEVATION, elevation);
        params.put(WMSQuery.KEY_DIM_RANGE, dimRange);
        params.put(WMSQuery.KEY_TIME, time);
        updateContext(layers, version, styles, sld, params);
        final Color background                 = (query.getTransparent()) ? null : query.getBackground();
        final Rectangle selectedArea = new Rectangle(infoX-PIXEL_TOLERANCE, infoY-PIXEL_TOLERANCE, PIXEL_TOLERANCE*2, PIXEL_TOLERANCE*2);

        //fill in the values with empty lists
        for(String layer : layers){
            values.put(layer, new ArrayList<String>());
        }

        if (true) {
            //for debug
            final StringBuilder builder = new StringBuilder();
            builder.append("Layers => ");
            for(String layer : layers){
                builder.append(layer +",");
            }
            builder.append("\n");
            builder.append("Styles => ");
            for(String style : styles){
                builder.append(style +",");
            }
            builder.append("\n");
            builder.append(selectedArea);
            builder.append("\n");
            builder.append("Context env => " + contextEnv.toString() + "\n");
            builder.append("Azimuth => " + azimuth + "\n");
            builder.append("Mime => " + mime.toString() + "\n");
            builder.append("Dimension => " + canvasDimension.toString() + "\n");
            builder.append("BGColor => " + background + "\n");
            builder.append("Transparent => " + query.getTransparent() + "\n");
            System.out.println(builder.toString());
        }

        //TODO horrible TRY CATCH to remove when the renderer will have a fine
        //error handeling. This catch doesnt happen in normal case, but because of
        //some strange behavior when deployed in web app, we sometimes catch runtimeException or
        //thread exceptions.
        try{
            prepareCanvas(refEnv, azimuth, background, canvasDimension);
            
            final List<? extends Graphic> graphics = canvas.getGraphicsIn(selectedArea);

//            JOptionPane.showMessageDialog(null, "nb graphics = " + graphics.size());

            for(final Graphic graphic : graphics){
                if(graphic instanceof GraphicFeatureJ2D){
                    final GraphicFeatureJ2D j2d = (GraphicFeatureJ2D) graphic;
                    final String value = extractor.getHtmlDescription(graphic, selectedArea);

                    if(value != null){
//                        JOptionPane.showMessageDialog(null, value);

                        final String layerName = j2d.getSource().getName();
                        final List<String> lst;
                        if(values.containsKey(layerName)){
                            lst = values.get(layerName);
                        }else{
                            lst = new ArrayList<String>();
                            values.put(layerName, lst);
                        }
                        lst.add(value);
                    }

                }else if(graphic instanceof CoverageGraphicLayerJ2D){
                    final CoverageGraphicLayerJ2D j2d = (CoverageGraphicLayerJ2D) graphic;
                    final String value = extractor.getHtmlDescription(graphic, selectedArea);

                    if(value != null){
                        final String layerName = j2d.getUserObject().getName();
                        final List<String> lst;
                        if(values.containsKey(layerName)){
                            lst = values.get(layerName);
                        }else{
                            lst = new ArrayList<String>();
                            values.put(layerName, lst);
                        }
                        lst.add(value);
                    }
                }
            }

        }catch(Exception ex){
//            JOptionPane.showMessageDialog(null, "uneErreur");
            if(ex instanceof PortrayalException){
                throw (PortrayalException)ex;
            }else if( ex instanceof WebServiceException){
                throw (WebServiceException) ex;
            }else{
                throw new PortrayalException(ex);
            }
        }finally{
            canvas.clearCache();
            renderer.clearCache();
        }

        return values;
    }


    /**
     * Makes the portray of a {@code GetMap} request.
     *
     * @param query A {@link GetMap} query.
     * @param output The output file where to write the result of the {@link GetMap} request.
     * @throws PortrayalException
     * @throws WebServiceException if an error occurs during the creation of the map context
     */
    public void portray(final GetMap query, final File output)
            throws PortrayalException, WebServiceException {

        if (output == null) {
            throw new NullPointerException("Output file can not be null");
        }

        final String mime = query.getFormat();
        final BufferedImage image = portray(query);
        try {
            writeImage(image, mime, output);
        } catch (IOException ex) {
            throw new PortrayalException(ex);
        }
    }

    private void prepareCanvas(final ReferencedEnvelope contextEnv, final double azimuth,
            final Color background, final Dimension canvasDimension) throws PortrayalException
    {

        canvas.setSize(canvasDimension);
        canvas.setBackground(background);
        canvas.seMonitor(monitor);

        final CanvasController2D canvasController = canvas.getController();
        try {
            canvasController.setObjectiveCRS(contextEnv.getCoordinateReferenceSystem());
        } catch (TransformException ex) {
            throw new PortrayalException(ex);
        }

        try{
            canvasController.setVisibleArea(contextEnv);
            if (azimuth != 0) {
                canvasController.rotate( -Math.toRadians(azimuth) );
            }
        }catch(NoninvertibleTransformException ex){
            throw new PortrayalException(ex);
        }

    }

    /**
     * Portray a MapContext and outpur it in the given
     * stream.
     *
     * @param contextEnv MapArea to render
     * @param azimuth The azimuth to apply for the rendering (rotation).
     * @param backgroung The {@link Color} to apply for background.
     * @param canvasDimension The {@link Dimension} of the wanted image.
     *
     * @throws PortrayalException
     */
    private BufferedImage portrayUsingCache(final ReferencedEnvelope contextEnv, final double azimuth,
            final Color background, final Dimension canvasDimension) throws PortrayalException
    {
        prepareCanvas(contextEnv, azimuth, background, canvasDimension);
        canvas.repaint();

        //check if errors occured during rendering
        final Exception ex = monitor.getLastException();
        if(ex != null){
            //something goes wrong while rendering, send the error
            if(ex instanceof PortrayalException){
                throw (PortrayalException)ex;
            }else{
                throw new PortrayalException(ex);
            }
        }
                
        final BufferedImage image = canvas.getSnapShot();

        if (image == null) {
            throw new PortrayalException("No image created by the canvas.");
        }

        //clear the layers to avoid potential memory leack;
        context.layers().clear();

        return image;
    }
    
    private void updateContext(final List<String> layers, final ServiceVersion version,
                                    final List<String> styles, final MutableStyledLayerDescriptor sld,
                                    final Map<String, Object> params)
                                    throws PortrayalException, WebServiceException
    {
        for (int index=0, n=layers.size(); index<n; index++) {
            final String layerName = layers.get(index);
            final LayerDetails details = LAYERDP.get(layerName);
            if (details == null) {
                throw new WebServiceException("Layer "+layerName+" could not be found.",
                        ExceptionCode.LAYER_NOT_DEFINED, version, "layer");
            }
            final Object style;
            if (sld != null) {
                //try to use the provided SLD
                style = extractStyle(layerName,sld);
            } else if (styles != null && styles.size() > index) {
                //try to grab the style if provided
                //a style has been given for this layer, try to use it
                style = styles.get(index);
            } else {
                //no defined styles, use the favorite one, let the layer get it himself.
                style = null;
            }
            final MapLayer layer = details.getMapLayer(style, params);
            if (layer == null) {
                throw new PortrayalException("Map layer "+layerName+" could not be created");
            }
            layer.setSelectable(true);
            context.layers().add(layer);
        }
    }

    private Object extractStyle(final String layerName, final MutableStyledLayerDescriptor sld){
        if(sld == null){
            throw new NullPointerException("SLD should not be null");
        }

        for(final MutableLayer layer : sld.layers()){

            if(layer instanceof MutableNamedLayer && layerName.equals(layer.getName()) ){
                //we can only extract style from a NamedLayer that has the same name
                final MutableNamedLayer mnl = (MutableNamedLayer) layer;

                for(final MutableLayerStyle mls : mnl.styles()){
                    if(mls instanceof MutableNamedStyle){
                        final MutableNamedStyle mns = (MutableNamedStyle) mls;
                        return mns.getName();
                    }else if(mls instanceof MutableStyle){
                        return mls;
                    }

                }
            }
        }

        //no valid style found
        return null;
    }

    public File writeInImage(Exception e, int width, int height, File output, String mime)
                    throws IOException{
        Logger.getLogger(CSTLPortrayalService.class.getName()).log(Level.WARNING, "Error image created : " + output,e);
        final BufferedImage img = writeInImage(e, width, height);
        writeImage(img, mime, output);
        return output;
    }

    public BufferedImage writeInImage(Exception e, int width, int height){

        final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = img.createGraphics();
        final Font f = new Font("Dialog",Font.BOLD,12);
        final FontMetrics metrics = g.getFontMetrics(f);
        final int fontHeight = metrics.getHeight();
        final int maxCharPerLine = width / metrics.charWidth('A');
        final String message = e.getMessage();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        g.setColor(Color.RED);
        if(maxCharPerLine < 1){
            //not enough space to draw error, simply use a red background
            g.setColor(Color.RED);
            g.fillRect(0, 0, width, height);
        }else{
            int y = fontHeight;
            String remain = message;

            while(remain != null && remain.length() > 0){
                int lastChar = (maxCharPerLine > remain.length()) ? remain.length() : maxCharPerLine;
                final String oneLine = remain.substring(0, lastChar);
                remain = remain.substring(lastChar);
                g.drawString(oneLine, 2, y);
                y += fontHeight ;
                if(y > height){
                    //we are out of the painting area, no need to draw more text.
                    break;
                }
            }
        }
        g.dispose();

        return img;
    }
    
    public static CSTLPortrayalService getInstance(){
        return instances.get();
    }
    
    private class ReportMonitor extends FailOnErrorMonitor{

        private Exception lastError = null;
        
        public Exception getLastException(){
            return lastError;
        }
        
        @Override
        public void renderingStarted() {
            lastError = null;
            super.renderingStarted();
        }

        @Override
        public void exceptionOccured(Exception ex, Level level) {
            lastError = ex;
            //request stop rendering
            stopRendering();
            //log the error
            LOGGER.log(level,"", ex);
        }
        
        
        
        
    }
    
}
