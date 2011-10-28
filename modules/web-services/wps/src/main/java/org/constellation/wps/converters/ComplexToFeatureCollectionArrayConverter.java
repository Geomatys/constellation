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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import org.constellation.wps.ws.WPSWorker;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.feature.xml.jaxp.JAXPStreamFeatureReader;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.geotoolkit.util.converter.SimpleConverter;



/**
 * Implementation of ObjectConverter to convert a complex input into a FeatureCollection array.
 * Complex Input is define by a <code>Map<String,Object></code> with entries keys :
 * <ul>
 * <li>data : a <code>List</code> of <code>Object</code></li>
 * <li>mime : mime type of the data like text/xml, ...</li>
 * <li>schema : is the data requires a schema</li>
 * <li>encoding : the data encoding like UTF8, ...</li>
 * </ul>
 * @author Quentin Boileau
 */
public final class ComplexToFeatureCollectionArrayConverter extends SimpleConverter<Map<String,Object>, FeatureCollection[]> {

    private static ComplexToFeatureCollectionArrayConverter INSTANCE;

    private ComplexToFeatureCollectionArrayConverter(){
    }

    public static ComplexToFeatureCollectionArrayConverter getInstance(){
        if(INSTANCE == null){
            INSTANCE = new ComplexToFeatureCollectionArrayConverter();
        }
        return INSTANCE;
    }

    @Override
    public Class<? super Map<String,Object>> getSourceClass() {
        return Map.class;
    }

    @Override
    public Class<? extends FeatureCollection[]> getTargetClass() {
        return FeatureCollection[].class ;
    }
 
    
    @Override
    public FeatureCollection[] convert(Map<String,Object> source) throws NonconvertibleObjectException {
        
        final List<Object> data = (List<Object>) source.get("data");
            
        try {
            JAXPStreamFeatureReader fcollReader = null;
            if(!data.isEmpty()){
                
                final List<FeatureCollection> features = new ArrayList<FeatureCollection>();
                for(int i = 0; i<data.size(); i++){

                    fcollReader = new JAXPStreamFeatureReader();
                    //enable to read the FeatureType into the FeatureCollection schema
                    fcollReader.setReadEmbeddedFeatureType(true); 
                    FeatureCollection fc = (FeatureCollection)fcollReader.read(data.get(i));
                    fc = (FeatureCollection) WPSWorker.fixFeature(fc);
                    features.add(fc);
                }
               
                return features.toArray(new FeatureCollection[features.size()]);
            }else{
                throw new NonconvertibleObjectException("Invalid data input : Empty FeatureCollection list.");
            }

        } catch (CstlServiceException ex) {
            throw new NonconvertibleObjectException(ex);
        } catch (IOException ex) {
            throw new NonconvertibleObjectException("Unable to read feature from nodes.", ex);
        } catch (XMLStreamException ex) {
            throw new NonconvertibleObjectException("Unable to read feature from nodes.", ex);
        }
      
    }
}

 