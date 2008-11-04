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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.constellation.coverage.web.WebServiceException;
import org.constellation.coverage.web.ExceptionCode;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.NamedLayerDP;
import org.constellation.query.wms.GetMap;
import org.constellation.query.wms.WMSQuery;
import org.constellation.query.wms.WMSQueryVersion;

import org.geotools.display.canvas.BufferedImageCanvas2D;
import org.geotools.display.exception.PortrayalException;
import org.geotools.display.renderer.Go2rendererHints;
import org.geotools.display.renderer.J2DRenderer;
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
import org.geotools.util.MeasurementRange;

import org.opengis.geometry.Envelope;
import org.opengis.referencing.operation.TransformException;


/**
 * Portrayal service, extends the GT portrayal service by adding support
 * to reconize Named layers and styles from constellation data providers.
 *
 * @author Johann Sorel (Geomatys)
 */
public class CSTLPortrayalService extends DefaultPortrayalService {
        
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
    
    private final BufferedImageCanvas2D canvas;
    private final J2DRenderer renderer;
    private final MapContext context;
    
    
    private CSTLPortrayalService(){
        canvas = new  BufferedImageCanvas2D(new Dimension(1,1),null);
        renderer = new J2DRenderer(canvas);
        context = MAP_BUILDER.createContext(DefaultGeographicCRS.WGS84);
        
        canvas.setRenderer(renderer);
        canvas.getController().setAutoRepaint(false);
        
        //disable mutlithread rendering, to avoid possibility of several buffers created
        canvas.setRenderingHint(Go2rendererHints.KEY_MULTI_THREAD, Go2rendererHints.MULTI_THREAD_OFF);
        
        //we specifically say to not repect X/Y proportions
        try {
            canvas.getController().setAxisProportions(Double.NaN);
        } catch (PortrayalException ex) {
            Logger.getLogger(CSTLPortrayalService.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            renderer.setContext(context);
        } catch (IOException ex) {
            Logger.getLogger(CSTLPortrayalService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformException ex) {
            Logger.getLogger(CSTLPortrayalService.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    /**
     * Makes the portray of a {@code GetMap} request.
     *
     * @param query A {@link GetMap} query.
     * @param output The output file where to write the result of the {@link GetMap} request.
     * @throws PortrayalException
     * @throws WebServiceException if an error occurs during the creation of the map context
     */
    public synchronized BufferedImage portray(final GetMap query)
                            throws PortrayalException, WebServiceException{

        if(query == null){
            throw new NullPointerException("Query cannot be null");
        }

        final List<String> layers              = query.getLayers();
        final List<String> styles              = query.getStyles();
        final MutableStyledLayerDescriptor sld = query.getSld();
        final Envelope contextEnv              = query.getEnvelope();
        final ReferencedEnvelope refEnv        = new ReferencedEnvelope(contextEnv);
        final String mime                      = query.getFormat();
        final WMSQueryVersion version          = query.getVersion();
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
        }
    }

    /**
     * Makes the portray of a {@code GetMap} request.
     *
     * @param query A {@link GetMap} query.
     * @param output The output file where to write the result of the {@link GetMap} request.
     * @throws PortrayalException
     * @throws WebServiceException if an error occurs during the creation of the map context
     */
    public synchronized void portray(final GetMap query, final File output)
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

    /**
     * Portray a MapContext and outpur it in the given
     * stream.
     *
     * @param context : Mapcontext to render
     * @param contextEnv : MapArea to render
     * @param output : output srteam or file or url
     * @param mime : mime output type
     * @param canvasDimension : size of the wanted image
     * @param hints : canvas hints
     */
    private BufferedImage portrayUsingCache(final ReferencedEnvelope contextEnv,
            final double azimuth, final Color background, final Dimension canvasDimension)
            throws PortrayalException {

        canvas.setSize(canvasDimension);
        canvas.setBackground(background);
        
        try {
            canvas.getController().setObjectiveCRS(contextEnv.getCoordinateReferenceSystem());
        } catch (TransformException ex) {
            throw new PortrayalException(ex);
        }

        canvas.getController().setVisibleArea(contextEnv);
        if(azimuth != 0){
            canvas.getController().rotate( -Math.toRadians(azimuth) );
        }
        canvas.repaint();

        final BufferedImage image = canvas.getSnapShot();

        if(image == null){
            throw new PortrayalException("No image created by the canvas.");
        }

        //clear the layers to avoid potential memory leack;
        context.layers().clear();

        return image;
    }
    
    private void updateContext(final List<String> layers, final WMSQueryVersion version,
                                    final List<String> styles, final MutableStyledLayerDescriptor sld,
                                    final Map<String, Object> params)
                                    throws PortrayalException, WebServiceException {

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
            } else if (styles.size() > index) {
                //try to grab the style if provided
                //a style has been given for this layer, try to use it
                style = styles.get(index);
            } else {
                //no defined styles, use the favorite one, let the layer get it himself.
                style = null;
            }
            final MapLayer layer = details.getMapLayer(style,params);
            if (layer == null) {
                throw new PortrayalException("Map layer "+layerName+" could not be created");
            }
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
    
}
