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
package org.constellation.query;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;

import org.geotools.sld.MutableStyledLayerDescriptor;
import org.geotools.style.sld.Specification.StyledLayerDescriptor;
import org.geotools.style.sld.XMLUtilities;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Convinient class to transform Strings to real Java objects.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class WMSQueryAdapter {

    public CoordinateReferenceSystem toCRS(String epsg) throws FactoryException{
        if(epsg.trim().endsWith("4326")){
            //TODO fix this
            //we should return the good EPSG 32662
            System.out.println("WARNING : CRS 4326 used.");
            return DefaultGeographicCRS.WGS84;
        }
        return CRS.decode(epsg);
    }
    
    public GeneralEnvelope toBBox(final String bbox) {
        GeneralEnvelope envelope = new GeneralEnvelope(2);
        envelope.setToInfinite();
        
        if (bbox == null) {
            if (envelope != null) {
                envelope.setToInfinite();
            }
            return envelope;
        }
        final StringTokenizer tokens = new StringTokenizer(bbox, ",;");
        if (envelope == null) {
            envelope = new GeneralEnvelope((tokens.countTokens() + 1) >> 1);
            envelope.setToInfinite();
        }
        final double[] coordinates = new double[envelope.getDimension() * 2];
        int index = 0;
        while (tokens.hasMoreTokens()) {
            final double value = toDouble(tokens.nextToken());
            if (index >= coordinates.length) {
                throw new IllegalArgumentException(Errors.format(ErrorKeys.MISMATCHED_DIMENSION_$3));
            }
            coordinates[index++] = value;
        }
        if ((index & 1) != 0) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.ODD_ARRAY_LENGTH_$1));
        }
        // Fallthrough in every cases.
        switch (index) {
            default: {
                while (index >= 6) {
                    final double maximum = coordinates[--index];
                    final double minimum = coordinates[--index];
                    envelope.setRange(index >> 1, minimum, maximum);
                }
            }
            case 4: envelope.setRange(1, coordinates[1], coordinates[3]);
            case 3:
            case 2: envelope.setRange(0, coordinates[0], coordinates[2]);
            case 1:
            case 0: break;
        }
        /*
         * Checks the envelope validity. Given that the parameter order in the bounding box
         * is a little-bit counter-intuitive, it is worth to perform this check in order to
         * avoid a NonInvertibleTransformException at some later stage.
         */
        final int dimension = envelope.getDimension();
        for (index=0; index<dimension; index++) {
            final double minimum = envelope.getMinimum(index);
            final double maximum = envelope.getMaximum(index);
            if (!(minimum < maximum)) {
                throw new IllegalArgumentException(Errors.format(ErrorKeys.BAD_RANGE_$2));
            }
        }
        
        return envelope;
    }
    
    public List<String> toLayers(String strLayers){
        List<String> layers = new ArrayList<String>();
        
        if(strLayers == null || strLayers.isEmpty()){
            return layers;
        }
        
        StringTokenizer token = new StringTokenizer(strLayers,",",false);
        while(token.hasMoreTokens()){
            layers.add(token.nextToken());
        }
        return layers;
    }

    public MutableStyledLayerDescriptor toSLD(String strSLD) {
        if(strSLD == null || strSLD.trim().length() == 0){
            return null;
        }
        
        MutableStyledLayerDescriptor sld = null;
        
        XMLUtilities sldUtilities = new XMLUtilities();
        
        //try sld v1.0
        try {
            sld = sldUtilities.readSLD(sld, StyledLayerDescriptor.V_1_0_0);
        } catch (JAXBException ex) {
            Logger.getLogger(WMSQueryAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(sld == null){
            //try sld v1.1
            try {
                sld = sldUtilities.readSLD(sld, StyledLayerDescriptor.V_1_1_0);
            } catch (JAXBException ex) {
                Logger.getLogger(WMSQueryAdapter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return sld;
    }
    
    public List<String> toStyles(String strLayers){
        List<String> styles = new ArrayList<String>();
        StringTokenizer token = new StringTokenizer(strLayers,",");
        while(token.hasMoreTokens()){
            styles.add(token.nextToken());
        }
        return styles;
    }
    
    public int toInt(final String name, String value) throws NumberFormatException {
        if (value == null) {
            throw new NullPointerException("value " + name+ " can not be null");
        }
        value = value.trim();
        return Integer.parseInt(value);
    }

    public double toDouble(String value) throws NumberFormatException {
        value = value.trim();
        return Double.parseDouble(value);
    }
    
    public Color toColor(String background) throws NumberFormatException{
        Color color = null;
        if (background != null) {
            background = background.trim();
            color = Color.decode(background);
        }else{
            //return the defautl specification color
            color = Color.WHITE;
        }
        return color;
    }
    
    public boolean toBoolean(String strTransparent) {
        if (strTransparent != null) {
            strTransparent = strTransparent.trim();
        }else{
            return false;
        }
        return Boolean.parseBoolean(strTransparent);
    }
    
    
}
