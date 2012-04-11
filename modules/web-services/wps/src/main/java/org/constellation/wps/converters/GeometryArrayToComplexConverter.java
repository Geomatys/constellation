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
package org.constellation.wps.converters;


import com.vividsolutions.jts.geom.Geometry;
import java.util.ArrayList;
import java.util.Collection;
import org.geotoolkit.gml.JTStoGeometry;
import org.geotoolkit.gml.xml.v311.AbstractGeometryType;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.geotoolkit.util.converter.SimpleConverter;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.util.FactoryException;



/**
 * Implementation of ObjectConverter to convert a JTS Geometry array into a an object which can be supported
 * by JAXB.
 * 
 * @author Quentin Boileau
 */
public final class GeometryArrayToComplexConverter extends SimpleConverter<Geometry[], Collection> {

    private static GeometryArrayToComplexConverter INSTANCE;

    private GeometryArrayToComplexConverter(){
    }

    public static synchronized GeometryArrayToComplexConverter getInstance(){
        if(INSTANCE == null){
            INSTANCE = new GeometryArrayToComplexConverter();
        }
        return INSTANCE;
    }

    @Override
    public Class<? super Geometry[]> getSourceClass() {
        return Geometry[].class;
    }

    @Override
    public Class<? extends Collection> getTargetClass() {
        return Collection.class ;
    }
 
    
    @Override
    public Collection convert(Geometry[] source) throws NonconvertibleObjectException {
        
        final Collection<Object> collec = new ArrayList<Object>();
        AbstractGeometryType gmlGeom = null;
        
        try {
            for(Geometry jtsGeom : source){
                gmlGeom = JTStoGeometry.toGML(jtsGeom);
                collec.add(gmlGeom);
            }
        } catch (NoSuchAuthorityCodeException ex) {
           throw new NonconvertibleObjectException(ex);
        } catch (FactoryException ex) {
            throw new NonconvertibleObjectException(ex);
        }
      
        return collec;
    }
}

