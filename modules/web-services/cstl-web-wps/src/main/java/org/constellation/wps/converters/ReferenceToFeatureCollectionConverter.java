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


import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import org.constellation.wps.ws.WPSWorker;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;
import org.geotoolkit.data.DataStore;
import org.geotoolkit.data.DataStoreFinder;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.feature.xml.XmlFeatureTypeReader;
import org.geotoolkit.feature.xml.jaxb.JAXBFeatureTypeReader;
import org.geotoolkit.feature.xml.jaxp.JAXPStreamFeatureReader;
import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.geotoolkit.util.converter.SimpleConverter;



/**
 * Implementation of ObjectConverter to convert a reference into a FeatureCollection.
 * Reference is define by a <code>Map<String,String></code> with entries keys :
 * <ul>
 * <li>href : Url to the data GML or shapefile</li>
 * <li>mime : mime type of the data like text/xml, ...</li>
 * <li>schema : is the data requires a schema</li>
 * <li>encoding : the data encoding like UTF8, ...</li>
 * <li>method : GET or POST</li>
 * </ul>
 * @author Quentin Boileau
 * @module pending
 */
public class ReferenceToFeatureCollectionConverter extends SimpleConverter<Map<String,String>, FeatureCollection> {

    private static ReferenceToFeatureCollectionConverter INSTANCE;

    private ReferenceToFeatureCollectionConverter(){
    }

    public static ReferenceToFeatureCollectionConverter getInstance(){
        if(INSTANCE == null){
            INSTANCE = new ReferenceToFeatureCollectionConverter();
        }
        return INSTANCE;
    }

    @Override
    public Class<? super Map> getSourceClass() {
        return Map.class;
    }

    @Override
    public Class<? extends FeatureCollection> getTargetClass() {
        return FeatureCollection.class ;
    }
 
    @Override
    public FeatureCollection convert(Map<String,String> source) throws NonconvertibleObjectException {

        if (source.get("mime") == null) {
                throw new NonconvertibleObjectException("Invalid reference input : typeMime can't be null.");
            }
            //XML
            if(source.get("mime").equalsIgnoreCase(MimeType.TEXT_XML)){
                 try {
                    final XmlFeatureTypeReader xsdReader = new JAXBFeatureTypeReader();
                    JAXPStreamFeatureReader fcollReader;
                    
                    if(source.get("schema") != null){
                        final URL schemaURL = new URL(source.get("schema"));
                        fcollReader = new JAXPStreamFeatureReader(xsdReader.read(schemaURL.openStream()));
                    }else{
                         fcollReader = new JAXPStreamFeatureReader();
                         fcollReader.setReadEmbeddedFeatureType(true);
                    }
                    
                    FeatureCollection fcoll = (FeatureCollection)fcollReader.read(new URL(source.get("href")));
                     fcoll = (FeatureCollection) WPSWorker.fixFeature(fcoll);
                    return fcoll;

                } catch (CstlServiceException ex) {
                    throw new NonconvertibleObjectException("Invalid reference input : can't spread CRS.",ex);
                } catch (JAXBException ex) {
                    throw new NonconvertibleObjectException("Invalid reference input : can't read reference schema.",ex);
                }catch (MalformedURLException ex){
                    throw new NonconvertibleObjectException("Invalid reference input : Malformed schema or resource.",ex);
                }catch (IOException ex){
                    throw new NonconvertibleObjectException("Invalid reference input : IO.",ex);
                }catch (XMLStreamException ex) {
                    throw new NonconvertibleObjectException("Invalid reference input.",ex);
                }
            // SHP
            }else if(source.get("mime").equalsIgnoreCase("application/octec-stream")){

                try {
                    Hints.putSystemDefault(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);
                    final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
                    parameters.put("url", new URL(source.get("href")));
                    
                    final DataStore store = DataStoreFinder.getDataStore(parameters);

                    if(store == null){
                        throw new NonconvertibleObjectException("Invalid URL");
                    }

                    if(store.getNames().size() != 1){
                        throw new NonconvertibleObjectException("More than one FeatureCollection in the file");
                    }

                    final FeatureCollection collection = store.createSession(true).getFeatureCollection(QueryBuilder.all(store.getNames().iterator().next()));
                    if(collection != null){
                        return collection;
                    }else{
                        throw new NonconvertibleObjectException("Collection not found");
                    }
                    
                } catch (DataStoreException ex) {
                    throw new NonconvertibleObjectException("Invalid reference input : Malformed schema or resource.",ex);
                }catch (MalformedURLException ex){
                    throw new NonconvertibleObjectException("Invalid reference input : Malformed schema or resource.",ex);
                }
                
            }else {
                 throw new NonconvertibleObjectException("Reference data mime is not supported");
            }
    }
}