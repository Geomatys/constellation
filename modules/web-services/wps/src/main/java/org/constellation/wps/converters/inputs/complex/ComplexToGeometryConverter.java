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
package org.constellation.wps.converters.inputs.complex;


import com.vividsolutions.jts.geom.Geometry;
import java.util.List;
import java.util.Map;
import org.constellation.wps.converters.inputs.AbstractInputConverter;
import org.geotoolkit.gml.GeometrytoJTS;
import org.geotoolkit.gml.xml.v311.AbstractGeometryType;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.geotoolkit.util.converter.SimpleConverter;
import org.opengis.util.FactoryException;



/**
 * Implementation of ObjectConverter to convert a complex input into a JTS Geometry.
 * 
 * @author Quentin Boileau (Geomatys).
 */
public final class ComplexToGeometryConverter extends AbstractInputConverter {

    private static ComplexToGeometryConverter INSTANCE;

    private ComplexToGeometryConverter(){
    }

    public static synchronized ComplexToGeometryConverter getInstance(){
        if(INSTANCE == null){
            INSTANCE = new ComplexToGeometryConverter();
        }
        return INSTANCE;
    }
 
    @Override
    public Object convert(Map<String,Object> source) throws NonconvertibleObjectException {

        try {                
            final List<Object> data = (List<Object>) source.get(IN_DATA);
            if(data.size() == 1){
                return GeometrytoJTS.toJTS((AbstractGeometryType) data.get(0));
            }else{
                throw new NonconvertibleObjectException("Invalid data input : Only one geometry expected.");
            }
        }catch(ClassCastException ex){
            throw new NonconvertibleObjectException("Invalid data input : empty GML geometry.",ex);
        }catch (FactoryException ex) {
            throw new NonconvertibleObjectException("Invalid data input : Cannot convert GML geometry.",ex);
        }
    }
}