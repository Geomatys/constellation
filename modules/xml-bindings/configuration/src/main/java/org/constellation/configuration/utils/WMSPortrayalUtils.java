/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.constellation.configuration.utils;

import java.awt.Color;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingConstants;
import org.constellation.configuration.PositionableDecoration;
import org.geotoolkit.util.Converters;
import org.apache.sis.util.logging.Logging;

/**
 *
 * @author Quentin Boileau (Geomatys)
 */
public final class WMSPortrayalUtils {
    
    private static final Logger LOGGER = Logging.getLogger(WMSPortrayalUtils.class);
    
    private WMSPortrayalUtils() {
    }
     
    public static float parseFloat(final String str, final int fallback){
        if(str == null) return fallback;

        try{
            return Float.parseFloat(str);
        }catch(NumberFormatException ex){
            LOGGER.log(Level.WARNING, "Not a valid float : "+str,ex);
            return fallback;
        }
    }
    
    public static Color parseColor(final String strColor, final Float strOpacity, final Color fallback){
        if(strColor == null) return fallback;

        Color color = Converters.convert(strColor, Color.class);

        if(color == null) return fallback;

        if(strOpacity != null){
            float opa = strOpacity != null ? strOpacity : 1.0f;
            if(opa<0 || opa>1){
                opa = 1;
            }
            color = new Color(color.getRed()/256f, color.getGreen()/256f, color.getBlue()/256f, opa);
        }

        return color;
    }
    
    public static String colorToHex(final Color color) {
        
        String colorCode = "";
        if (color != null ) {
            String redCode = Integer.toHexString(color.getRed());
            String greenCode = Integer.toHexString(color.getGreen());
            String blueCode = Integer.toHexString(color.getBlue());
            if (redCode.length() == 1)      redCode = "0" + redCode;
            if (greenCode.length() == 1)    greenCode = "0" + greenCode;
            if (blueCode.length() == 1)     blueCode = "0" + blueCode;

            int alpha = color.getAlpha();
            if(alpha != 255){
                String alphaCode = Integer.toHexString(alpha);
                if (alphaCode.length() == 1) alphaCode = "0" + alphaCode;
                colorCode = "#" + alphaCode + redCode + greenCode + blueCode;
            }else{
                colorCode = "#" + redCode + greenCode + blueCode;
            }        
        }
        
        return colorCode.toUpperCase();
    }
    
    public static Float getColorOpacity (final Color color) {
        Float opacity = 1.0f;
        if (color != null) {
            final int alpha = color.getAlpha();
            opacity = alpha * (1.0f/255);
        }
        return opacity;
    }
    
    public static URL parseURL(final String url, final URL fallback){
        if(url == null) return fallback;

        try{
            return new URL(url);
        }catch(Exception ex){
            LOGGER.log(Level.WARNING, "Could not parse url",ex);
            return fallback;
        }
    }
}
