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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import org.constellation.wps.converters.inputs.AbstractInputConverter;
import org.constellation.wps.utils.WPSMimeType;
import org.constellation.wps.utils.WPSUtils;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.feature.xml.XmlFeatureReader;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.opengis.feature.Feature;



/**
 * Implementation of ObjectConverter to convert a reference into a Feature.
 * 
 * @author Quentin Boileau (Geomatys).
 */
public final class ReferenceToFeatureConverter extends AbstractInputConverter {

    private static ReferenceToFeatureConverter INSTANCE;

    private ReferenceToFeatureConverter(){
    }

    public static synchronized ReferenceToFeatureConverter getInstance(){
        if(INSTANCE == null){
            INSTANCE = new ReferenceToFeatureConverter();
        }
        return INSTANCE;
    }

    @Override
    public Object convert(final Map<String, Object> source) throws NonconvertibleObjectException {
            
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
                final Feature fcoll = (Feature) fcollReader.read(new URL(href));
                return (Feature) WPSUtils.fixFeature(fcoll);

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

        } else {
            throw new NonconvertibleObjectException("Reference data mime is not supported");
        }
    }
}