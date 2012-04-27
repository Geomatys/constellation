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
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBException;
import org.constellation.wps.converters.inputs.AbstractInputConverter;
import org.constellation.wps.utils.WPSMimeType;
import org.geotoolkit.feature.xml.XmlFeatureTypeReader;
import org.geotoolkit.feature.xml.jaxb.JAXBFeatureTypeReader;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.opengis.feature.type.FeatureType;



/**
 * Implementation of ObjectConverter to convert a reference into a FeatureType.
 *
 * @author Quentin Boileau (Geomatys).
 */
public final class ReferenceToFeatureTypeConverter extends AbstractInputConverter {

    private static ReferenceToFeatureTypeConverter INSTANCE;

    private ReferenceToFeatureTypeConverter(){
    }

    public static synchronized ReferenceToFeatureTypeConverter getInstance(){
        if(INSTANCE == null){
            INSTANCE = new ReferenceToFeatureTypeConverter();
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
             try {
                final XmlFeatureTypeReader xsdReader = new JAXBFeatureTypeReader();
                final URL schemaURL = new URL(href);
                final List<FeatureType> ft = xsdReader.read(schemaURL.openStream());

                if(ft.size() != 1){
                    throw new NonconvertibleObjectException("Invalid reference input : More than one FeatureType in schema.");
                }
                return ft.get(0);
            } catch (JAXBException ex) {
                throw new NonconvertibleObjectException("Invalid reference input : can't read reference schema.",ex);
            }catch (MalformedURLException ex){
                throw new NonconvertibleObjectException("Invalid reference input : Malformed schema or resource.",ex);
            }catch (IOException ex){
                throw new NonconvertibleObjectException("Invalid reference input : IO.",ex);
            }
        }else {
             throw new NonconvertibleObjectException("Reference data mime is not supported");
        }
    }
}