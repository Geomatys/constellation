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


import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.constellation.wps.converters.inputs.AbstractInputConverter;
import org.constellation.wps.utils.WPSMimeType;
import org.geotoolkit.gml.GeometrytoJTS;
import org.geotoolkit.gml.xml.v311.AbstractGeometryType;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.geotoolkit.wps.xml.WPSMarshallerPool;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.util.FactoryException;

/**
 * Implementation of ObjectConverter to convert a reference into a Geometry.
 * 
 * @author Quentin Boileau (Geomatys).
 */
public final class ReferenceToGeometryConverter extends AbstractInputConverter {

    private static ReferenceToGeometryConverter INSTANCE;

    private ReferenceToGeometryConverter(){
    }

    public static synchronized ReferenceToGeometryConverter getInstance(){
        if(INSTANCE == null){
            INSTANCE = new ReferenceToGeometryConverter();
        }
        return INSTANCE;
    }

    @Override
    public Object convert(final Map<String, Object> source) throws NonconvertibleObjectException {
                    
        final String mime = (String) source.get(IN_MIME);
        final String href = (String) source.get(IN_HREF);

        if (mime == null) {
            throw new NonconvertibleObjectException("Invalid reference input : typeMime can't be null.");
        }
        if (mime.equalsIgnoreCase(WPSMimeType.TEXT_XML.getValue()) || mime.equalsIgnoreCase(WPSMimeType.APP_GML.getValue()) ||
                mime.equalsIgnoreCase(WPSMimeType.TEXT_GML.getValue())) {
            
            Unmarshaller unmarsh = null;
            try {
                unmarsh = WPSMarshallerPool.getInstance().acquireUnmarshaller();
                Object value = unmarsh.unmarshal(new URL(href));
                if(value instanceof JAXBElement){
                    value = ((JAXBElement)value).getValue();
                }
                return GeometrytoJTS.toJTS((AbstractGeometryType) value);

            } catch (NoSuchAuthorityCodeException ex) {
                throw new NonconvertibleObjectException("Reference geometry invalid input",ex);
            } catch (FactoryException ex) {
                throw new NonconvertibleObjectException("Reference geometry invalid input",ex);
            } catch (MalformedURLException ex) {
                throw new NonconvertibleObjectException("Reference geometry invalid input : Malformed url",ex);
            } catch (JAXBException ex) {
                throw new NonconvertibleObjectException("Reference geometry invalid input : Unmarshallable geometry",ex);
            } finally {
                if (unmarsh != null){
                    WPSMarshallerPool.getInstance().release(unmarsh);
                }
            }
        }else{
         throw new NonconvertibleObjectException("Reference data mime is not supported");
        }
    }

}