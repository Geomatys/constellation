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
import java.util.List;
import java.util.Map;
import org.geotoolkit.gml.GeometrytoJTS;
import org.geotoolkit.gml.xml.v311.AbstractGeometryType;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.geotoolkit.util.converter.SimpleConverter;
import org.opengis.util.FactoryException;



/**
 * Implementation of ObjectConverter to convert a complex input into a JTS Geometry array.
 * Complex Input is define by a <code>List</code> of <code>Object</code>
 * @author Quentin Boileau
 */
public final class ComplexToGeometryArrayConverter extends SimpleConverter<Map<String,Object>, Geometry[]> {

    private static ComplexToGeometryArrayConverter INSTANCE;

    private ComplexToGeometryArrayConverter(){
    }

    public static ComplexToGeometryArrayConverter getInstance(){
        if(INSTANCE == null){
            INSTANCE = new ComplexToGeometryArrayConverter();
        }
        return INSTANCE;
    }

    @Override
    public Class<? super Map<String,Object>> getSourceClass() {
        return Map.class;
    }

    @Override
    public Class<? extends Geometry[]> getTargetClass() {
        return Geometry[].class ;
    }
 
    @Override
    public Geometry[] convert(Map<String,Object> source) throws NonconvertibleObjectException {

        try {                
            final List<Object> data = (List<Object>) source.get("data");
            if(!data.isEmpty()){
                List<Geometry> geoms = new ArrayList<Geometry>();
                for(int i = 0; i<source.size(); i++){
                    geoms.add(GeometrytoJTS.toJTS((AbstractGeometryType) data.get(i)));
                }
                return geoms.toArray(new Geometry[geoms.size()]);
            }else{
                throw new NonconvertibleObjectException("Invalid data input : Empty geometry list.");
            }
        }catch(ClassCastException ex){
            throw new NonconvertibleObjectException("Invalid data input : empty GML geometry.",ex);
        }catch (FactoryException ex) {
            throw new NonconvertibleObjectException("Invalid data input : Cannot convert GML geometry.",ex);
        }
    }
}