/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2011, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
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
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.geotoolkit.util.converter.SimpleConverter;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.util.FactoryException;



/**
 * Implementation of ObjectConverter to convert a JTS Geometry into a an object which can be supported
 * by JAXB.
 * 
 * @author Quentin Boileau
 * @module pending
 */
public class GeometryToComplexConverter extends SimpleConverter<Geometry, Collection> {

    private static GeometryToComplexConverter INSTANCE;

    private GeometryToComplexConverter(){
    }

    public static GeometryToComplexConverter getInstance(){
        if(INSTANCE == null){
            INSTANCE = new GeometryToComplexConverter();
        }
        return INSTANCE;
    }

    @Override
    public Class<? super Geometry> getSourceClass() {
        return Geometry.class;
    }

    @Override
    public Class<? extends Collection> getTargetClass() {
        return Collection.class ;
    }
 
    
    @Override
    public Collection convert(Geometry source) throws NonconvertibleObjectException {
        
        final Collection<Object> collec = new ArrayList<Object>();
        
        try {
            final Geometry jtsGeom = source;
            collec.add(JTStoGeometry.toGML(jtsGeom));
            
            return collec;
            
        } catch (NoSuchAuthorityCodeException ex) {
            throw new NonconvertibleObjectException(ex);
        } catch (FactoryException ex) {
            throw new NonconvertibleObjectException(ex);
        }
      
    }
}

