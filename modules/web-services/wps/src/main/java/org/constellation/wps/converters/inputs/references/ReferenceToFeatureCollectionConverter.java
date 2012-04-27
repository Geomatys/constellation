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
package org.constellation.wps.converters.inputs.references;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import org.constellation.wps.converters.inputs.AbstractInputConverter;
import org.constellation.wps.utils.WPSMimeType;
import org.constellation.wps.utils.WPSUtils;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.data.DataStore;
import org.geotoolkit.data.DataStoreFinder;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.feature.xml.XmlFeatureReader;
import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.util.converter.NonconvertibleObjectException;

/**
 * Implementation of ObjectConverter to convert a reference into a FeatureCollection. Reference is define by a
 * <code>Map<String,String></code> with entries keys : <ul> <li>href : Url to the data GML or shapefile</li> <li>mime :
 * mime type of the data like text/xml, ...</li> <li>schema : is the data requires a schema</li> <li>encoding : the data
 * encoding like UTF8, ...</li> <li>method : GET or POST</li> </ul>
 *
 * @author Quentin Boileau
 */
public final class ReferenceToFeatureCollectionConverter extends AbstractInputConverter {

    private static ReferenceToFeatureCollectionConverter INSTANCE;

    private ReferenceToFeatureCollectionConverter() {
    }

    public static synchronized ReferenceToFeatureCollectionConverter getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ReferenceToFeatureCollectionConverter();
        }
        return INSTANCE;
    }

    @Override
    public Object convert(Map<String, Object> source) throws NonconvertibleObjectException {

        
        final String mime = (String) source.get(IN_MIME);
        final String href = (String) source.get(IN_HREF);
        
        if (source.get(IN_MIME) == null) {
            throw new NonconvertibleObjectException("Invalid reference input : typeMime can't be null.");
        }
        
        //XML
        if (mime.equalsIgnoreCase(WPSMimeType.TEXT_XML.getValue()) || mime.equalsIgnoreCase(WPSMimeType.APP_GML.getValue()) ||
                mime.equalsIgnoreCase(WPSMimeType.TEXT_GML.getValue())) {
            
            XmlFeatureReader fcollReader = null;
            try {
                fcollReader = getFeatureReader(source);
                final FeatureCollection fcoll = (FeatureCollection) fcollReader.read(new URL(href));
                return (FeatureCollection) WPSUtils.fixFeature(fcoll);

            } catch (CstlServiceException ex) {
                throw new NonconvertibleObjectException("Invalid reference input : can't spread CRS.", ex);
            } catch (JAXBException ex) {
                throw new NonconvertibleObjectException("Invalid reference input : can't read reference schema.", ex);
            } catch (MalformedURLException ex) {
                throw new NonconvertibleObjectException("Invalid reference input : Malformed schema or resource.", ex);
            } catch (IOException ex) {
                throw new NonconvertibleObjectException("Invalid reference input : IO.", ex);
            } catch (XMLStreamException ex) {
                throw new NonconvertibleObjectException("Invalid reference input.", ex);
            } finally {
                if (fcollReader != null) {
                    fcollReader.dispose();
                }
            }
            // SHP
        } else if (mime.equalsIgnoreCase(WPSMimeType.APP_SHP.getValue()) || mime.equalsIgnoreCase(WPSMimeType.APP_OCTET.getValue())) {

            try {
                Hints.putSystemDefault(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);
                final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
                parameters.put("url", new URL(href));

                final DataStore store = DataStoreFinder.get(parameters);

                if (store == null) {
                    throw new NonconvertibleObjectException("Invalid URL");
                }

                if (store.getNames().size() != 1) {
                    throw new NonconvertibleObjectException("More than one FeatureCollection in the file");
                }

                final FeatureCollection collection = store.createSession(true).getFeatureCollection(QueryBuilder.all(store.getNames().iterator().next()));
                if (collection != null) {
                    return collection;
                } else {
                    throw new NonconvertibleObjectException("Collection not found");
                }

            } catch (DataStoreException ex) {
                throw new NonconvertibleObjectException("Invalid reference input : Malformed schema or resource.", ex);
            } catch (MalformedURLException ex) {
                throw new NonconvertibleObjectException("Invalid reference input : Malformed schema or resource.", ex);
            }

        } else {
            throw new NonconvertibleObjectException("Reference data mime is not supported");
        }
    }
}