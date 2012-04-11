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


import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import org.constellation.wps.utils.WPSUtils;
import org.constellation.wps.ws.WPSWorker;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.feature.xml.XmlFeatureReader;
import org.geotoolkit.feature.xml.XmlFeatureTypeReader;
import org.geotoolkit.feature.xml.jaxb.JAXBFeatureTypeReader;
import org.geotoolkit.feature.xml.jaxp.JAXPStreamFeatureReader;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.geotoolkit.util.converter.SimpleConverter;
import org.opengis.feature.type.FeatureType;



/**
 * Implementation of ObjectConverter to convert a complex input into a FeatureCollection.
 * Complex Input is define by a <code>Map<String,Object></code> with entries keys :
 * <ul>
 * <li>data : a <code>List</code> of <code>Object</code></li>
 * <li>mime : mime type of the data like text/xml, ...</li>
 * <li>schema : is the data requires a schema</li>
 * <li>encoding : the data encoding like UTF8, ...</li>
 * </ul>
 * @author Quentin Boileau
 */
public final class ComplexToFeatureCollectionConverter extends SimpleConverter<Map<String,Object>, FeatureCollection> {

    private static ComplexToFeatureCollectionConverter INSTANCE;

    private ComplexToFeatureCollectionConverter(){
    }

    public static synchronized ComplexToFeatureCollectionConverter getInstance(){
        if(INSTANCE == null){
            INSTANCE = new ComplexToFeatureCollectionConverter();
        }
        return INSTANCE;
    }

    @Override
    public Class<? super Map<String,Object>> getSourceClass() {
        return Map.class;
    }

    @Override
    public Class<? extends FeatureCollection> getTargetClass() {
        return FeatureCollection.class ;
    }
 
    
    @Override
    public FeatureCollection convert(Map<String,Object> source) throws NonconvertibleObjectException {
        
        final List<Object> data = (List<Object>) source.get("data");
        
        if(data.size() > 1){
           throw new NonconvertibleObjectException("Invalid data input : Only one Feature/FeatureCollection expected.");
        }
        Object extractData = null;
        
        //Get FeatureType
        List<FeatureType> ft = null;
        if (source.get("schema") != null) {
            try {
                final XmlFeatureTypeReader xsdReader = new JAXBFeatureTypeReader();
                final URL schemaURL = new URL((String) source.get("schema"));
                ft = xsdReader.read(schemaURL.openStream());
            } catch (IOException ex) {
                throw new NonconvertibleObjectException("Unable to read feature type from xsd.", ex);
            } catch (JAXBException ex) {
                throw new NonconvertibleObjectException("Unable to read feature type from xsd.", ex);
            }
        }
        
        //Read featureCollection
        try {
            final XmlFeatureReader fcollReader = new JAXPStreamFeatureReader(ft);
            extractData = fcollReader.read(data.get(0));
        } catch (IOException ex) {
            throw new NonconvertibleObjectException("Unable to read feature from nodes.", ex);
        } catch (XMLStreamException ex) {
            throw new NonconvertibleObjectException("Unable to read feature from nodes.", ex);
        }
        
        //Fix FeatureType CRS
        try {
            return (FeatureCollection) WPSUtils.fixFeature((FeatureCollection)extractData);
        } catch (CstlServiceException ex) {
           throw new NonconvertibleObjectException(ex);
        }
      
    }
}