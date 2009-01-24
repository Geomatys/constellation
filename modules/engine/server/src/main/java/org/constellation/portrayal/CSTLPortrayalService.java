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
import java.awt.Shape;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.constellation.ws.CstlServiceException;
import org.constellation.ws.ExceptionCode;
import org.constellation.ws.ServiceVersion;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.NamedLayerDP;

import org.geotools.display.canvas.BufferedImageCanvas2D;
import org.geotools.display.canvas.CanvasController2D;
import org.geotools.display.canvas.GraphicVisitor;
import org.geotools.display.canvas.VisitFilter;
import org.geotools.display.canvas.control.FailOnErrorMonitor;
import org.geotools.display.exception.PortrayalException;
import org.geotools.display.renderer.ContextRenderer2D;
import org.geotools.display.renderer.DefaultContextRenderer2D;
import org.geotools.display.renderer.Go2rendererHints;
import org.geotools.display.service.DefaultPortrayalService;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.sld.MutableLayer;
import org.geotools.sld.MutableLayerStyle;
import org.geotools.sld.MutableNamedLayer;
import org.geotools.sld.MutableNamedStyle;
import org.geotools.sld.MutableStyledLayerDescriptor;
import org.geotools.style.MutableStyle;

import org.opengis.referencing.operation.TransformException;


/**
 * Portrayal service, extends the GT portrayal service by adding support
 * to reconize Named layers and styles from constellation data providers.
 *
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public class CSTLPortrayalService extends DefaultPortrayalService {

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

    public void hit(final ReferencedEnvelope refEnv, final double azimuth,
            final Color background, final Dimension canvasDimension,
            final List<String> layers, final List<String> styles,
            final MutableStyledLayerDescriptor sld, final Map<String, Object> params,
            final ServiceVersion version, Shape selectedArea, GraphicVisitor visitor)
            throws PortrayalException,CstlServiceException {

        updateContext(layers, version, styles, sld, params);

        //TODO horrible TRY CATCH to remove when the renderer will have a fine
        //error handeling. This catch doesnt happen in normal case, but because of
        //some strange behavior when deployed in web app, we sometimes catch runtimeException or
        //thread exceptions.
        prepareCanvas(refEnv, azimuth, background, canvasDimension);

        try{
            canvas.getGraphicsIn(selectedArea, visitor, VisitFilter.INTERSECTS);
        }catch(Exception ex){
            if(ex instanceof PortrayalException){
                throw (PortrayalException)ex;
            }else if( ex instanceof CstlServiceException){
                throw (CstlServiceException) ex;
            }else{
                throw new PortrayalException(ex);
            }
        }finally{
            visitor.endVisit();
            canvas.clearCache();
            renderer.clearCache();
            context.layers().clear();
        }

    }

    public BufferedImage portray(final ReferencedEnvelope refEnv, final double azimuth,
            final Color background, final Dimension canvasDimension,
            final List<String> layers, final List<String> styles, 
            final MutableStyledLayerDescriptor sld, final Map<String, Object> params,
            final ServiceVersion version) throws PortrayalException,CstlServiceException {

        updateContext(layers, version, styles, sld, params);

        //TODO horrible TRY CATCH to remove when the renderer will have a fine
        //error handeling. This catch doesnt happen in normal case, but because of
        //some strange behavior when deployed in web app, we sometimes catch runtimeException or
        //thread exceptions.
        try {
            return portrayUsingCache(refEnv, azimuth, background, canvasDimension);
        } catch (Exception ex) {
            if (ex instanceof PortrayalException) {
                throw (PortrayalException)ex;
            } else if(ex instanceof CstlServiceException) {
                throw (CstlServiceException) ex;
            } else {
                throw new PortrayalException(ex);
            }
        }finally{
            canvas.clearCache();
            renderer.clearCache();
            context.layers().clear();
        }

    }

    private void prepareCanvas(final ReferencedEnvelope contextEnv, final double azimuth,
            final Color background, final Dimension canvasDimension) throws PortrayalException{

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
            final Color background, final Dimension canvasDimension) throws PortrayalException{
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
                                    throws PortrayalException, CstlServiceException{
        context.layers().clear();

        for (int index=0, n=layers.size(); index<n; index++) {
            final String layerName = layers.get(index);
            final LayerDetails details = LAYERDP.get(layerName);
            if (details == null) {
                throw new CstlServiceException("Layer "+layerName+" could not be found.",
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
            layer.setVisible(true);
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
