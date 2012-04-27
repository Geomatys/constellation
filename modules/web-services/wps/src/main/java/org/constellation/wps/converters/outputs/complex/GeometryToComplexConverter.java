/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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
package org.constellation.wps.converters.outputs.complex;


import com.vividsolutions.jts.geom.Geometry;
import java.util.Map;
import org.geotoolkit.gml.JTStoGeometry;
import org.geotoolkit.gml.xml.v311.AbstractGeometryType;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.geotoolkit.wps.xml.v100.ComplexDataType;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.util.FactoryException;

/**
 * Implementation of ObjectConverter to convert a JTS Geometry into a an object which can be supported
 * by JAXB.
 * 
 * @author Quentin Boileau
 */
public final class GeometryToComplexConverter extends AbstractComplexOutputConverter {

    private static GeometryToComplexConverter INSTANCE;

    private GeometryToComplexConverter(){
    }

    public static synchronized GeometryToComplexConverter getInstance(){
        if(INSTANCE == null){
            INSTANCE = new GeometryToComplexConverter();
        }
        return INSTANCE;
    }
 
     @Override
    public ComplexDataType convert(final Map<String, Object> source) throws NonconvertibleObjectException {
        
        final ComplexDataType complex = new ComplexDataType();
        
        complex.setMimeType((String) source.get(OUT_MIME));
        complex.setSchema((String) source.get(OUT_SCHEMA));
        complex.setEncoding((String) source.get(OUT_ENCODING));
        
        AbstractGeometryType gmlGeom = null;
        try {
            
            gmlGeom = JTStoGeometry.toGML((Geometry) source.get(OUT_DATA));
            complex.getContent().add(gmlGeom);
            
        } catch (NoSuchAuthorityCodeException ex) {
           throw new NonconvertibleObjectException(ex);
        } catch (FactoryException ex) {
            throw new NonconvertibleObjectException(ex);
        }
      
        return complex;
    }
}

