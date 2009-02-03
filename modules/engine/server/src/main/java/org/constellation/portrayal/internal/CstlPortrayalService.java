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
package org.constellation.portrayal.internal;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.constellation.portrayal.AbstractGraphicVisitor;
import org.constellation.portrayal.Portrayal;
import org.constellation.portrayal.PortrayalServiceIF;
import org.constellation.provider.LayerDetails;
import org.geotools.display.canvas.BufferedImageCanvas2D;
import org.geotools.display.canvas.CanvasController2D;
import org.geotools.display.canvas.GraphicVisitor;
import org.geotools.display.canvas.VisitFilter;
import org.geotools.display.canvas.control.FailOnErrorMonitor;
import org.geotools.display.container.ContextContainer2D;
import org.geotools.display.container.DefaultContextContainer2D;
import org.geotools.display.exception.PortrayalException;
import org.geotools.display.renderer.Go2rendererHints;
import org.geotools.display.service.DefaultPortrayalService;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.operation.TransformException;


/**
 * Service class to portray or work with two dimensional scenes defined by a 
 * scene definition, a view definition, and a canvas definition.
 * <p>
 * <b>Users should *not* call this class directly.</b><br/>
 * Instead, the {@link Cstl.Portrayal} reference should be used.
 * </p>
 *
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 * @see Cst.Portrayal
 * @see Portrayal
 */
public class CstlPortrayalService extends DefaultPortrayalService implements PortrayalServiceIF {
    
	
    private static final Logger LOGGER = Logger.getLogger("org.constellation.portrayal");
    
    /**
     * This generates a new instance for each thread (i.e. each service request) 
     * but still allows us to have a statically accessible reference with the 
     * {@link #getInstance()} method.
     */
    private static final ThreadLocal<CstlPortrayalService> instances = new ThreadLocal<CstlPortrayalService>() {
        @Override
        protected CstlPortrayalService initialValue() {
            return new CstlPortrayalService();
        }
    };
    
    /**
     * An internal method which provides the instance for the thread in which 
     * the caller is running.
     * <p>
     * In this design, each CstlPortrayalService instance runs in its own thread, 
     * </p>
     * @return
     */
    public static CstlPortrayalService internal_getInstance(){
        return instances.get();
    }
    
    private final ReportMonitor monitor = new ReportMonitor();
    private final BufferedImageCanvas2D canvas;
    private final ContextContainer2D container;
    private final MapContext context;
    
    /*
     * TODO: document the thinking behind these defaults.
     */
    private CstlPortrayalService(){
    	
        canvas = new  BufferedImageCanvas2D(DefaultGeographicCRS.WGS84,new Dimension(1,1),null);
        container = new DefaultContextContainer2D(canvas, false);
        context = MAP_BUILDER.createContext(DefaultGeographicCRS.WGS84);//TODO: why are we building a context here?
        
        canvas.setContainer(container);
        canvas.getController().setAutoRepaint(false);
        
        //disable multithread rendering, to avoid possibility of several buffers created
        canvas.setRenderingHint(Go2rendererHints.KEY_MULTI_THREAD, Go2rendererHints.MULTI_THREAD_OFF);
        
        //we specifically say to not respect X/Y proportions
        canvas.getController().setAxisProportions(Double.NaN);
        
        container.setContext(context);
    }
    
    /**
     * Portray a set of Layers over a given geographic extent with a given 
     * resolution yielding a {@code BufferedImage} of the scene.
     * @param sdef A structure which defines the scene.
     * @param vdef A structure which defines the view.
     * @param cdef A structure which defines the canvas.
     * 
     * @return A rendered image of the scene, in the chosen view and for the 
     *           given canvas.
     * @throws PortrayalException For errors during portrayal, TODO: common examples?
     */
    @Override
    public BufferedImage portray( final Portrayal.SceneDef sdef,
                                  final Portrayal.ViewDef vdef,
                                  final Portrayal.CanvasDef cdef) 
    		throws PortrayalException {
    	
    	
        updateMapContext(sdef);
        
        prepareCanvas(vdef, cdef);
        
        //TODO horrible TRY CATCH to remove when the renderer will have a fine
        //error handeling. This catch doesnt happen in normal case, but because of
        //some strange behavior when deployed in web app, we sometimes catch runtimeException or
        //thread exceptions.
        try {
            return portrayUsingCache();
        } catch (Exception ex) {
            if (ex instanceof PortrayalException) {
                throw (PortrayalException)ex;
            } else {
                throw new PortrayalException(ex);
            }
        }finally{
            canvas.clearCache();
            container.clearCache();
            context.layers().clear();
        }

    }
    
    /**
     * Apply the Visitor to all the 
     * {@link org..opengis.display.primitive.Graphic} objects which lie within 
     * the {@link java.awt.Shape} in the given scene.
     * <p>
     * The visitor could be an extension of the AbstractGraphicVisitor class in
     * this same package.
     * </p>
     * 
     * TODO: why are the last two arguments not final?
     * 
     * @see AbstractGraphicVisitor
     */
    @Override
    public void visit( final Portrayal.SceneDef sdef,
                       final Portrayal.ViewDef vdef,
                       final Portrayal.CanvasDef cdef,
                       Shape selectedArea, 
                       GraphicVisitor visitor)
            throws PortrayalException {

        updateMapContext(sdef);
        
        prepareCanvas(vdef,cdef);

        //TODO horrible TRY CATCH to remove when the renderer will have a fine
        //error handeling. This catch doesnt happen in normal case, but because of
        //some strange behavior when deployed in web app, we sometimes catch runtimeException or
        //thread exceptions.
        try {
            canvas.getGraphicsIn(selectedArea, visitor, VisitFilter.INTERSECTS);
        } catch(Exception ex) {
            if(ex instanceof PortrayalException){
                throw (PortrayalException)ex;
            } else {
                throw new PortrayalException(ex);
            }
        } finally {
            visitor.endVisit();
            canvas.clearCache();
            container.clearCache();
            context.layers().clear();
        }

    }
    
    /*
     * TODO: document how the size of the text is chosen.
     */
    @Override
    public BufferedImage writeInImage(Exception e, Dimension dim){

        final BufferedImage img = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = img.createGraphics();
        final Font f = new Font("Dialog",Font.BOLD,12);
        final FontMetrics metrics = g.getFontMetrics(f);
        final int fontHeight = metrics.getHeight();
        final int maxCharPerLine = dim.width / metrics.charWidth('A');
        final String message = e.getMessage();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, dim.width, dim.height);

        g.setColor(Color.RED);
        if(maxCharPerLine < 1){
            //not enough space to draw error, simply use a red background
            g.setColor(Color.RED);
            g.fillRect(0, 0, dim.width, dim.height);
        }else{
            int y = fontHeight;
            String remain = message;

            while(remain != null && remain.length() > 0){
                int lastChar = (maxCharPerLine > remain.length()) ? remain.length() : maxCharPerLine;
                final String oneLine = remain.substring(0, lastChar);
                remain = remain.substring(lastChar);
                g.drawString(oneLine, 2, y);
                y += fontHeight ;
                if(y > dim.height){
                    //we are out of the painting area, no need to draw more text.
                    break;
                }
            }
        }
        g.dispose();

        return img;
    }
    
    
    
    
    private void updateMapContext( final Portrayal.SceneDef sdef ) throws PortrayalException {

    	assert ( sdef.layerRefs.size() == sdef.styleRefs.size() );
        context.layers().clear();
        
        for (int i = 0; i < sdef.layerRefs.size(); i++){
        	
        	final LayerDetails layerRef = sdef.layerRefs.get(i);
        	final Object style = sdef.styleRefs.get(i);
        	
        	assert ( null != layerRef );
        	//style can be null

            final MapLayer mapLayer = layerRef.getMapLayer(style, sdef.renderingParameters);
            if (mapLayer == null) {
                throw new PortrayalException("Could not create a mapLayer for layer: " + layerRef.getName());
            }
            mapLayer.setSelectable(true);
            mapLayer.setVisible(true);
            context.layers().add(mapLayer);
        }
    }

    private void prepareCanvas( final Portrayal.ViewDef vdef, 
    		                    final Portrayal.CanvasDef cdef) throws PortrayalException {

        canvas.setSize(cdef.dimension);
        canvas.setBackground(cdef.background);
        canvas.seMonitor(monitor);//TODO: this method name is wrong, needs to be fixed.

        final CanvasController2D canvasController = canvas.getController();
        try {
            canvasController.setObjectiveCRS(vdef.envelope.getCoordinateReferenceSystem());
        } catch (TransformException ex) {
            throw new PortrayalException(ex);
        }

        try{
            canvasController.setVisibleArea(vdef.envelope);
            if (vdef.azimuth != 0) {
                canvasController.rotate( -Math.toRadians(vdef.azimuth) );
            }
        }catch(NoninvertibleTransformException ex){
            throw new PortrayalException(ex);
        }
        

    }

    private BufferedImage portrayUsingCache() throws PortrayalException{
    	
        canvas.repaint();
        
        //check if errors occurred during rendering
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

        //clear the layers to avoid potential memory leak;//TODO: is this potential or real?
        context.layers().clear();

        return image;
    }
    
    /*
     * TODO: document me, pretty please.
     */
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
