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


import java.util.ArrayList;
import java.util.Collection;
import javax.xml.parsers.ParserConfigurationException;
import org.geotoolkit.feature.xml.jaxp.ElementFeatureWriter;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.geotoolkit.util.converter.SimpleConverter;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;



/**
 * Implementation of ObjectConverter to convert a Feature into a an object which can be supported
 * by JAXB.
 * 
 * @author Quentin Boileau
 */
public final class FeatureToComplexConverter extends SimpleConverter<Feature, Collection> {

    private static FeatureToComplexConverter INSTANCE;

    private FeatureToComplexConverter(){
    }

    public static FeatureToComplexConverter getInstance(){
        if(INSTANCE == null){
            INSTANCE = new FeatureToComplexConverter();
        }
        return INSTANCE;
    }

    @Override
    public Class<? super Feature> getSourceClass() {
        return Feature.class;
    }

    @Override
    public Class<? extends Collection> getTargetClass() {
        return Collection.class ;
    }
 
    
    @Override
    public Collection convert(Feature source) throws NonconvertibleObjectException {
        
        final Collection<Object> collec = new ArrayList<Object>();

        try {
            final ElementFeatureWriter efw = new ElementFeatureWriter();
            collec.add(efw.writeFeature(source, null, true));

        } catch (ParserConfigurationException ex) {
             throw new NonconvertibleObjectException("Can't write FeatureCollection into ResponseDocument",ex);
        }

       return  collec;
      
    }
}

