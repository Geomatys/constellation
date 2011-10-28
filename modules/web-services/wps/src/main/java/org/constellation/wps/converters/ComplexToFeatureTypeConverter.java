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


import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBException;
import org.geotoolkit.feature.xml.jaxb.JAXBFeatureTypeReader;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.geotoolkit.util.converter.SimpleConverter;
import org.opengis.feature.type.FeatureType;
import org.w3c.dom.Node;



/**
 * Implementation of ObjectConverter to convert a complex input into a FeatureType.
 * Complex Input is define by a <code>Map<String,Object></code> with entries keys :
 * <ul>
 * <li>data : a <code>List</code> of <code>Object</code></li>
 * <li>mime : mime type of the data like text/xml, ...</li>
 * <li>schema : is the data requires a schema</li>
 * <li>encoding : the data encoding like UTF8, ...</li>
 * </ul>
 * @author Quentin Boileau
 */
public final class ComplexToFeatureTypeConverter extends SimpleConverter<Map<String,Object>,FeatureType> {

    private static ComplexToFeatureTypeConverter INSTANCE;

    private ComplexToFeatureTypeConverter(){
    }

    public static ComplexToFeatureTypeConverter getInstance(){
        if(INSTANCE == null){
            INSTANCE = new ComplexToFeatureTypeConverter();
        }
        return INSTANCE;
    }

    @Override
    public Class<? super Map<String,Object>> getSourceClass() {
        return Map.class;
    }

    @Override
    public Class<? extends FeatureType> getTargetClass() {
        return FeatureType.class ;
    }
 
    
    @Override
    public FeatureType convert(Map<String,Object> source) throws NonconvertibleObjectException {
        
        final List<Object> data = (List<Object>) source.get("data");
        if(data.size() > 1){
           throw new NonconvertibleObjectException("Invalid data input : Only one FeatureType expected.");
        }

        //Get FeatureType
        List<FeatureType> ft = null;
        try {
            final JAXBFeatureTypeReader xsdReader = new JAXBFeatureTypeReader();
            ft = xsdReader.read((Node)data.get(0));
            return ft.get(0);

        } catch (JAXBException ex) {
            throw new NonconvertibleObjectException("Unable to read feature type from xsd.", ex); 
        }
      
    }
}

