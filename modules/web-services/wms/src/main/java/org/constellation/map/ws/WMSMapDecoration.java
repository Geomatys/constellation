/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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

package org.constellation.map.ws;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.measure.unit.Unit;
import javax.swing.SwingConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.constellation.provider.configuration.ConfigDirectory;
import org.geotoolkit.display.exception.PortrayalException;
import org.geotoolkit.display2d.canvas.J2DCanvas;
import org.geotoolkit.display2d.ext.BackgroundTemplate;
import org.geotoolkit.display2d.ext.DefaultBackgroundTemplate;
import org.geotoolkit.display2d.ext.PositionedGraphic2D;
import org.geotoolkit.display2d.ext.grid.DefaultGridTemplate;
import org.geotoolkit.display2d.ext.grid.GraphicGridJ2D;
import org.geotoolkit.display2d.ext.grid.GridTemplate;
import org.geotoolkit.display2d.ext.image.DefaultImageTemplate;
import org.geotoolkit.display2d.ext.image.GraphicImageJ2D;
import org.geotoolkit.display2d.ext.image.ImageTemplate;
import org.geotoolkit.display2d.ext.legend.DefaultLegendTemplate;
import org.geotoolkit.display2d.ext.legend.GraphicLegendJ2D;
import org.geotoolkit.display2d.ext.legend.LegendTemplate;
import org.geotoolkit.display2d.ext.northarrow.DefaultNorthArrowTemplate;
import org.geotoolkit.display2d.ext.northarrow.GraphicNorthArrowJ2D;
import org.geotoolkit.display2d.ext.northarrow.NorthArrowTemplate;
import org.geotoolkit.display2d.ext.scalebar.DefaultScaleBarTemplate;
import org.geotoolkit.display2d.ext.scalebar.GraphicScaleBarJ2D;
import org.geotoolkit.display2d.ext.scalebar.ScaleBarTemplate;
import org.geotoolkit.display2d.ext.text.DefaultTextTemplate;
import org.geotoolkit.display2d.ext.text.GraphicTextJ2D;
import org.geotoolkit.display2d.ext.text.TextTemplate;
import org.geotoolkit.display2d.service.PortrayalExtension;
import org.geotoolkit.lang.ThreadSafe;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.util.Converters;
import org.geotoolkit.util.logging.Logging;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Utility class to parse a wms decoration extension configuration file as a
 * portrayal extension.
 * @author Johann Sorel (Geomatys)
 */
@ThreadSafe(concurrent=false)
public final class WMSMapDecoration {

    private static final Logger LOGGER = Logging.getLogger(WMSMapDecoration.class);

    private static final String TAG_DECORATION  = "Decoration";
    private static final String TAG_BACKGROUND  = "Background";
    private static final String TAG_PARAMETER   = "Parameter";
    private static final String TAG_MAIN        = "Main";
    private static final String TAG_SECOND      = "Second";
    
    private static final String ATT_NAME = "name";

    private static final String PARAM_POSITION = "position";
    private static final String PARAM_OFFSET_X = "offset-x";
    private static final String PARAM_OFFSET_Y = "offset-y";
    private static final String PARAM_STROKE_COLOR = "stroke-color";
    private static final String PARAM_STROKE_OPACITY = "stroke-opacity";
    private static final String PARAM_STROKE_WIDTH = "stroke-width";
    private static final String PARAM_STROKE_DASHES = "stroke-dashes";
    private static final String PARAM_FILL_COLOR = "fill-color";
    private static final String PARAM_FILL_OPACITY = "fill-opacity";
    private static final String PARAM_INSETS = "insets";
    private static final String PARAM_ROUND = "round";
    private static final String PARAM_HEIGHT = "height";
    private static final String PARAM_WIDTH = "width";
    private static final String PARAM_SOURCE = "source";
    private static final String PARAM_TEXT = "text";
    private static final String PARAM_FONT = "font";
    private static final String PARAM_GAP = "gap";
    private static final String PARAM_MAIN_FONT = "main-font";
    private static final String PARAM_SECOND_FONT = "second-font";
    private static final String PARAM_GLYPH_HEIGHT = "glyph-height";
    private static final String PARAM_GLYPH_WIDTH = "glyph-width";
    private static final String PARAM_LAYER_NAME = "layer-name";
    private static final String PARAM_UNIT = "unit";
    private static final String PARAM_CRS = "crs";

    private static final String TYPE_IMAGE          = "image";
    private static final String TYPE_TEXT           = "text";
    private static final String TYPE_LEGEND         = "legend";
    private static final String TYPE_COMPAS         = "compas";
    private static final String TYPE_SCALE_NUMERIC  = "scalebar-numeric";
    private static final String TYPE_SCALE_GRAPHIC  = "scalebar-graphic";
    private static final String TYPE_GRID           = "grid";

    private static final String POSTION_NORTH       = "north";
    private static final String POSTION_NORTH_EAST  = "north-east";
    private static final String POSTION_NORTH_WEST  = "north-west";
    private static final String POSTION_SOUTH       = "south";
    private static final String POSTION_SOUTH_EAST  = "south-east";
    private static final String POSTION_SOUTH_WEST  = "south-west";
    private static final String POSTION_EAST        = "east";
    private static final String POSTION_WEST        = "west";
    private static final String POSTION_CENTER      = "center";


    /**
     * Decoration extension for map queries.
     */
    private static PortrayalExtension EXTENSION = null;

    /**
     * The default legend template.
     */
    private static final LegendTemplate LEGEND_TEMPLATE = new DefaultLegendTemplate(
                    new DefaultBackgroundTemplate(
                        new BasicStroke(1),
                        Color.LIGHT_GRAY,
                        Color.WHITE,
                        new Insets(4, 4, 4, 4),
                        10),
                    5,
                    new Dimension(30, 24),
                    new Font("Arial", Font.PLAIN, 10),
                    false,
                    new Font("Arial", Font.BOLD, 12));

    private WMSMapDecoration(){}

    /**
     * First call to this method will parse the configuration file if there is one.
     * 
     * @return PortrayalExtension, never null
     */
    public static synchronized PortrayalExtension getExtension() {

        if(EXTENSION != null) return EXTENSION;

        //load the portrayal extension
        final String path = ConfigDirectory.getConfigDirectory().getPath() + File.separator + "WMSPortrayal.xml";

        final File f = new File(path);
        if(f.exists()){
            try {
                EXTENSION = read(f);
            } catch (ParserConfigurationException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            } catch (SAXException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }

        //no configuration available, make an empty
        if(EXTENSION == null){
            LOGGER.log(Level.FINE, "No WMS portrayal extension found, will create an empty extension");
            EXTENSION = new DecorationExtension();
        }


        return EXTENSION;
    }

    public static PortrayalExtension read(File configFile)
            throws ParserConfigurationException, SAXException, IOException{

        if(!configFile.exists()){
            return null;
        }

        final DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();
        final DocumentBuilder constructeur = fabrique.newDocumentBuilder();
        final Document document = constructeur.parse(configFile);
        final NodeList nodes = document.getElementsByTagName(TAG_DECORATION);

        final DecorationExtension ext = new DecorationExtension();

        for(int i=0, n=nodes.getLength(); i<n; i++){
            final Element decoNode = (Element)nodes.item(i);
            parseDecoration(ext, decoNode);
        }

        return ext;
    }

    /**
     * Returns the default legend template.
     */
    public static LegendTemplate getDefaultLegendTemplate(){
        //TODO: must link with wms portrayal template
        return LEGEND_TEMPLATE;
    }

    private static void parseDecoration(final DecorationExtension deco, final Element decoNode){
        final String type = decoNode.getAttribute(ATT_NAME);

        final Map<String,String> params = parseParameters(decoNode);
        final BackgroundTemplate background = parseBackground(decoNode);
        final Map<String,Object> parsed = new HashMap<String,Object>();

        //offsets are the same for everyone
        parsed.put(PARAM_OFFSET_X, parseInteger(params.get(PARAM_OFFSET_X), 0));
        parsed.put(PARAM_OFFSET_Y, parseInteger(params.get(PARAM_OFFSET_Y), 0));

        if(type.equalsIgnoreCase(TYPE_COMPAS)){
            final NorthArrowTemplate template = new DefaultNorthArrowTemplate(
                    background,
                    parseURL(params.get(PARAM_SOURCE),WMSMapDecoration.class.getResource("/org/geotoolkit/icon/boussole.svg")),
                    new Dimension(
                        parseInteger(params.get(PARAM_WIDTH),100),
                        parseInteger(params.get(PARAM_HEIGHT),100))
                    );
            parsed.put(ATT_NAME, TYPE_COMPAS);
            parsed.put(TYPE_COMPAS, template);
            parsed.put(PARAM_POSITION, parsePosition(params.get(PARAM_POSITION), SwingConstants.NORTH_EAST));

        }else if(type.equalsIgnoreCase(TYPE_GRID)){

            CoordinateReferenceSystem crs = null;
            if(params.get(PARAM_CRS) != null){
                try {
                    crs = CRS.decode(params.get(PARAM_CRS));
                } catch (FactoryException ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }
            }

            Stroke mainLineStroke = new BasicStroke(2);
            Paint mainLinePaint = Color.DARK_GRAY;
            Font mainLineFont = new Font("serial", Font.BOLD, 14);
            Stroke secondLineStroke = new BasicStroke(1,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,10,new float[]{5,5},0);
            Paint secondLinePaint = Color.GRAY;
            Font secondLineFont = new Font("serial", Font.BOLD, 14);

            NodeList nodes = decoNode.getElementsByTagName(TAG_MAIN);
            for(int i=0, n=nodes.getLength(); i<n; i++){
                final Element sub = (Element)nodes.item(i);
                final Map<String,String> subParams = parseParameters(sub);
                mainLineStroke = parseStroke(subParams.get(PARAM_STROKE_WIDTH), subParams.get(PARAM_STROKE_DASHES));
                mainLinePaint = parseColor(subParams.get(PARAM_STROKE_COLOR), subParams.get(PARAM_STROKE_OPACITY), Color.DARK_GRAY);
                mainLineFont = parseFont(subParams.get(PARAM_FONT));
            }

            nodes = decoNode.getElementsByTagName(TAG_SECOND);
            for(int i=0, n=nodes.getLength(); i<n; i++){
                final Element sub = (Element)nodes.item(i);
                final Map<String,String> subParams = parseParameters(sub);
                secondLineStroke = parseStroke(subParams.get(PARAM_STROKE_WIDTH), subParams.get(PARAM_STROKE_DASHES));
                secondLinePaint = parseColor(subParams.get(PARAM_STROKE_COLOR), subParams.get(PARAM_STROKE_OPACITY), Color.DARK_GRAY);
                secondLineFont = parseFont(subParams.get(PARAM_FONT));
            }


            final GridTemplate template = new DefaultGridTemplate(
                    crs,
                    mainLineStroke,
                    mainLinePaint,
                    secondLineStroke,
                    secondLinePaint,
                    mainLineFont,
                    mainLinePaint,
                    0,
                    new Color(0f,0f,0f,0f),
                    secondLineFont,
                    secondLinePaint,
                    0,
                    new Color(0f,0f,0f,0f));
            parsed.put(ATT_NAME, TYPE_GRID);
            parsed.put(TYPE_GRID, template);

        }else if(type.equalsIgnoreCase(TYPE_IMAGE)){

            final URL source = parseURL(params.get(PARAM_SOURCE), null);
            
            BufferedImage buffer;
            try {
                buffer = ImageIO.read(source);
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, null, ex);
                buffer = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            }

            final ImageTemplate template = new DefaultImageTemplate(background, buffer);
            parsed.put(ATT_NAME, TYPE_IMAGE);
            parsed.put(TYPE_IMAGE, template);
            parsed.put(PARAM_POSITION, parsePosition(params.get(PARAM_POSITION), SwingConstants.NORTH_WEST));
            



        }else if(type.equalsIgnoreCase(TYPE_LEGEND)){
            final LegendTemplate template = new DefaultLegendTemplate(
                    background,
                    parseInteger(params.get(PARAM_GAP),2),
                    new Dimension(
                        parseInteger(params.get(PARAM_GLYPH_WIDTH),30),
                        parseInteger(params.get(PARAM_GLYPH_HEIGHT),20)),
                    parseFont(params.get(PARAM_SECOND_FONT)),
                    parseBoolean(params.get(PARAM_LAYER_NAME),true),
                    parseFont(params.get(PARAM_MAIN_FONT)));
            parsed.put(ATT_NAME, TYPE_LEGEND);
            parsed.put(TYPE_LEGEND, template);
            parsed.put(PARAM_POSITION, parsePosition(params.get(PARAM_POSITION), SwingConstants.EAST));

        }else if(type.equalsIgnoreCase(TYPE_SCALE_GRAPHIC)){

            String unit = params.get(PARAM_UNIT);
            if(unit == null || unit.isEmpty()) unit = "km";

            final ScaleBarTemplate template = new DefaultScaleBarTemplate(
                    background,
                    new Dimension(
                        parseInteger(params.get(PARAM_WIDTH),250),
                        parseInteger(params.get(PARAM_HEIGHT),30)),
                    10,
                    false,
                    5,
                    NumberFormat.getNumberInstance(),
                    Color.BLACK,
                    Color.BLACK,
                    Color.WHITE,
                    3,
                    true,
                    false,
                    new Font("Serial", Font.PLAIN, 12),
                    true,
                    Unit.valueOf(unit)
             );
            parsed.put(ATT_NAME, TYPE_SCALE_GRAPHIC);
            parsed.put(TYPE_SCALE_GRAPHIC, template);
            parsed.put(PARAM_POSITION, parsePosition(params.get(PARAM_POSITION), SwingConstants.SOUTH_WEST));

        }else if(type.equalsIgnoreCase(TYPE_SCALE_NUMERIC)){

            //not handle yet

        }else if(type.equalsIgnoreCase(TYPE_TEXT)){
            String txt = params.get(PARAM_TEXT);
            if(txt == null) txt = "";

            final TextTemplate template = new DefaultTextTemplate(
                    background,
                    txt);
            parsed.put(ATT_NAME, TYPE_TEXT);
            parsed.put(TYPE_TEXT, template);
            parsed.put(PARAM_POSITION, parsePosition(params.get(PARAM_POSITION), SwingConstants.SOUTH_WEST));
        }

        deco.decorations.add(parsed);
    }

    private static Map<String, String> parseParameters(final Element decoNode) {
        final Map<String,String> params = new HashMap<String, String>();

        final NodeList nodes = decoNode.getElementsByTagName(TAG_PARAMETER);
        for(int i=0, n=nodes.getLength(); i<n; i++){
            final Element paramNode = (Element)nodes.item(i);
            params.put(
                    paramNode.getAttribute(ATT_NAME).trim().toLowerCase(),
                    paramNode.getTextContent().trim()
                    );
        }

        return params;
    }

    private static BackgroundTemplate parseBackground(final Element decoNode){
        final NodeList nodes = decoNode.getElementsByTagName(TAG_BACKGROUND);

        if(nodes.getLength() > 0){
            final Element bgNode = (Element) nodes.item(0);
            final Map<String,String> params = parseParameters(bgNode);

            //parse values
            final String strWidth = params.get(PARAM_STROKE_WIDTH);
            final String strDashes = params.get(PARAM_STROKE_DASHES);
            final String strStrokeColor = params.get(PARAM_STROKE_COLOR);
            final String strStrokeOpacity = params.get(PARAM_STROKE_OPACITY);
            final String strFillColor = params.get(PARAM_FILL_COLOR);
            final String strFillOpacity = params.get(PARAM_FILL_OPACITY);
            final String strInsets = params.get(PARAM_INSETS);
            final String strRound = params.get(PARAM_ROUND);

            final Color strokePaint = parseColor(strStrokeColor, strStrokeOpacity, Color.DARK_GRAY);
            final Color fill = parseColor(strFillColor, strFillOpacity, Color.WHITE);
            final int round = parseInteger(strRound, 12);
            final Stroke stroke = parseStroke(strWidth, strDashes);
            final Insets insets = parseInsets(strInsets, new Insets(5, 5, 5, 5));
            
            return new DefaultBackgroundTemplate(stroke, strokePaint, fill, insets, round);
        }

        return null;
    }

    private static int parseInteger(String str, int fallback){
        if(str == null) return fallback;

        try{
            return Integer.parseInt(str);
        }catch(NumberFormatException ex){
            LOGGER.log(Level.WARNING, "Not a valid integer : "+str,ex);
            return fallback;
        }
    }

    private static boolean parseBoolean(String str, boolean fallback){
        if(str == null) return fallback;

        try{
            return Boolean.parseBoolean(str);
        }catch(NumberFormatException ex){
            LOGGER.log(Level.WARNING, "Not a valid boolean : "+str,ex);
            return fallback;
        }
    }

    private static float parseFloat(String str, int fallback){
        if(str == null) return fallback;

        try{
            return Float.parseFloat(str);
        }catch(NumberFormatException ex){
            LOGGER.log(Level.WARNING, "Not a valid float : "+str,ex);
            return fallback;
        }
    }

    private static Font parseFont(String strFont){
        return Font.decode(strFont);
    }

    private static Insets parseInsets(String str, Insets fallback){
        if(str == null) return fallback;

        final String[] parts = str.split(",");
        if(parts.length == 4){
            return new Insets(
                    parseInteger(parts[0],5),
                    parseInteger(parts[1],5),
                    parseInteger(parts[2],5),
                    parseInteger(parts[3],5));

        }else{
            return fallback;
        }

    }

    private static Color parseColor(String strColor, String strOpacity, Color fallback){
        if(strColor == null) return fallback;

        Color color = Converters.convert(strColor, Color.class);

        if(color == null) return fallback;

        if(strOpacity != null){
            float opa = parseFloat(strOpacity, 1);
            if(opa<0 || opa>1){
                opa = 1;
            }

            color = new Color(color.getRed()/256f, color.getGreen()/256f, color.getBlue()/256f, opa);
        }

        return color;
    }

    private static int parsePosition(String str, int fallback){
        if(POSTION_CENTER.equalsIgnoreCase(str)){
            return SwingConstants.CENTER;
        }else if(POSTION_EAST.equalsIgnoreCase(str)){
            return SwingConstants.EAST;
        }else if(POSTION_WEST.equalsIgnoreCase(str)){
            return SwingConstants.WEST;
        }else if(POSTION_NORTH.equalsIgnoreCase(str)){
            return SwingConstants.NORTH;
        }else if(POSTION_NORTH_EAST.equalsIgnoreCase(str)){
            return SwingConstants.NORTH_EAST;
        }else if(POSTION_NORTH_WEST.equalsIgnoreCase(str)){
            return SwingConstants.NORTH_WEST;
        }else if(POSTION_SOUTH.equalsIgnoreCase(str)){
            return SwingConstants.SOUTH;
        }else if(POSTION_SOUTH_EAST.equalsIgnoreCase(str)){
            return SwingConstants.SOUTH_EAST;
        }else if(POSTION_SOUTH_WEST.equalsIgnoreCase(str)){
            return SwingConstants.SOUTH_WEST;
        }else{
            return fallback;
        }
    }

    private static URL parseURL(String url, URL fallback){
        if(url == null) return fallback;

        try{
            return new URL(url);
        }catch(Exception ex){
            LOGGER.log(Level.WARNING, "Could not parse url",ex);
            return fallback;
        }
    }

    private static Stroke parseStroke(String strWidth, String strDashes){

        final int width = parseInteger(strWidth, 1);

        if(strDashes != null && !strDashes.isEmpty()){
            final String[] parts = strDashes.split(",");
            if (parts.length > 0){
                final float[] dashes = new float[parts.length];
                for (int i=0; i<parts.length; i++){
                    dashes[i] = parseFloat(parts[i], 5);
                }
                return new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,10,dashes,0);
            }
        }

        return new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

    }

    private static final class DecorationExtension implements PortrayalExtension{

        private final List<Map<String,Object>> decorations = new ArrayList<Map<String,Object>>();

        @Override
        public void completeCanvas(J2DCanvas canvas) throws PortrayalException {

            for(final Map<String,Object> params : decorations){

                final String type = (String) params.get(ATT_NAME);

                if(type.equalsIgnoreCase(TYPE_COMPAS)){
                    final NorthArrowTemplate arrowTemplate = (NorthArrowTemplate) params.get(TYPE_COMPAS);
                    final PositionedGraphic2D compasDeco = new GraphicNorthArrowJ2D(canvas, arrowTemplate);
                    compasDeco.setPosition((Integer)params.get(PARAM_POSITION));
                    compasDeco.setOffset((Integer)params.get(PARAM_OFFSET_X), (Integer)params.get(PARAM_OFFSET_Y));
                    canvas.getContainer().add(compasDeco);

                }else if(type.equalsIgnoreCase(TYPE_GRID)){
                    final GridTemplate gridTemplate = (GridTemplate) params.get(TYPE_GRID);
                    final GraphicGridJ2D girdDeco = new GraphicGridJ2D(canvas, gridTemplate);
                    canvas.getContainer().add(girdDeco);

                }else if(type.equalsIgnoreCase(TYPE_IMAGE)){
                    final ImageTemplate imgTemplate = (ImageTemplate) params.get(TYPE_IMAGE);
                    final PositionedGraphic2D imageDeco = new GraphicImageJ2D(canvas, imgTemplate);
                    imageDeco.setPosition((Integer)params.get(PARAM_POSITION));
                    imageDeco.setOffset((Integer)params.get(PARAM_OFFSET_X), (Integer)params.get(PARAM_OFFSET_Y));
                    canvas.getContainer().add(imageDeco);

                }else if(type.equalsIgnoreCase(TYPE_LEGEND)){
                    final LegendTemplate legendTemplate = (LegendTemplate) params.get(TYPE_LEGEND);
                    final GraphicLegendJ2D legendDeco = new GraphicLegendJ2D(canvas, legendTemplate);
                    legendDeco.setPosition((Integer)params.get(PARAM_POSITION));
                    legendDeco.setOffset((Integer)params.get(PARAM_OFFSET_X), (Integer)params.get(PARAM_OFFSET_Y));
                    canvas.getContainer().add(legendDeco);

                }else if(type.equalsIgnoreCase(TYPE_SCALE_GRAPHIC)){
                    final ScaleBarTemplate template = (ScaleBarTemplate) params.get(TYPE_SCALE_GRAPHIC);
                    final GraphicScaleBarJ2D scaleDeco = new GraphicScaleBarJ2D(canvas);
                    scaleDeco.setTemplate(template);
                    scaleDeco.setPosition((Integer)params.get(PARAM_POSITION));
                    scaleDeco.setOffset((Integer)params.get(PARAM_OFFSET_X), (Integer)params.get(PARAM_OFFSET_Y));
                    canvas.getContainer().add(scaleDeco);

                }else if(type.equalsIgnoreCase(TYPE_SCALE_NUMERIC)){

                    //not handle yet

                }else if(type.equalsIgnoreCase(TYPE_TEXT)){
                    final TextTemplate textTemplate = (TextTemplate) params.get(TYPE_TEXT);
                    final PositionedGraphic2D textDeco = new GraphicTextJ2D(canvas, textTemplate);
                    textDeco.setPosition((Integer)params.get(PARAM_POSITION));
                    textDeco.setOffset((Integer)params.get(PARAM_OFFSET_X), (Integer)params.get(PARAM_OFFSET_Y));
                    canvas.getContainer().add(textDeco);

                }

            }
            
        }

    }


}
