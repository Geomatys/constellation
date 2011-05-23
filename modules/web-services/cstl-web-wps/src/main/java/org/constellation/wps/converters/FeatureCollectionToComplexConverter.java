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


import java.util.ArrayList;
import java.util.Collection;
import javax.xml.parsers.ParserConfigurationException;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.feature.xml.jaxp.ElementFeatureWriter;
import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.geotoolkit.util.converter.SimpleConverter;
import org.opengis.feature.type.FeatureType;



/**
 * Implementation of ObjectConverter to convert a FeatureCollection into a an object which can be supported
 * by JAXB.
 * 
 * @author Quentin Boileau
 * @module pending
 */
public class FeatureCollectionToComplexConverter extends SimpleConverter<FeatureCollection, Collection> {

    private static FeatureCollectionToComplexConverter INSTANCE;

    private FeatureCollectionToComplexConverter(){
    }

    public static FeatureCollectionToComplexConverter getInstance(){
        if(INSTANCE == null){
            INSTANCE = new FeatureCollectionToComplexConverter();
        }
        return INSTANCE;
    }

    @Override
    public Class<? super FeatureCollection> getSourceClass() {
        return FeatureCollection.class;
    }

    @Override
    public Class<? extends Collection> getTargetClass() {
        return Collection.class ;
    }
 
    
    @Override
    public Collection convert(FeatureCollection source) throws NonconvertibleObjectException {
        
        final Collection<Object> collec = new ArrayList<Object>();
        
        try {

            final ElementFeatureWriter efw = new ElementFeatureWriter();
            collec.add(efw.writeFeatureCollection(source, true, false));

        } catch (DataStoreException ex) {
            throw new NonconvertibleObjectException("Can't write FeatureCollection into ResponseDocument",ex);
        } catch (ParserConfigurationException ex) {
             throw new NonconvertibleObjectException("Can't write FeatureCollection into ResponseDocument",ex);
        }

       return  collec;
      
    }
}

