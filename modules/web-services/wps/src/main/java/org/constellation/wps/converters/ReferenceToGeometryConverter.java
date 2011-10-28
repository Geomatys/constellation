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


import com.vividsolutions.jts.geom.Geometry;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.constellation.ws.MimeType;
import org.geotoolkit.gml.GeometrytoJTS;
import org.geotoolkit.gml.xml.v311.AbstractGeometryType;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.geotoolkit.util.converter.SimpleConverter;
import org.geotoolkit.wps.xml.WPSMarshallerPool;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.util.FactoryException;



/**
 * Implementation of ObjectConverter to convert a reference into a Geometry.
 * Reference is define by a <code>Map<String,String></code> with entries keys :
 * <ul>
 * <li>href : Url to the data</li>
 * <li>mime : mime type of the data like text/xml, ...</li>
 * <li>schema : is the data requires a schema</li>
 * <li>encoding : the data encoding like UTF8, ...</li>
 * <li>method : GET or POST</li>
 * </ul>
 * @author Quentin Boileau
 */
public final class ReferenceToGeometryConverter extends SimpleConverter<Map<String,String>, Geometry> {

    private static ReferenceToGeometryConverter INSTANCE;

    private ReferenceToGeometryConverter(){
    }

    public static ReferenceToGeometryConverter getInstance(){
        if(INSTANCE == null){
            INSTANCE = new ReferenceToGeometryConverter();
        }
        return INSTANCE;
    }

    @Override
    public Class<? super Map> getSourceClass() {
        return Map.class;
    }

    @Override
    public Class<? extends Geometry> getTargetClass() {
        return Geometry.class ;
    }
 
    @Override
    public Geometry convert(Map<String,String> source) throws NonconvertibleObjectException {
                    
        if (source.get("mime") == null) {
            throw new NonconvertibleObjectException("Invalid reference input : typeMime can't be null.");
        }
        if(source.get("mime").equalsIgnoreCase(MimeType.TEXT_XML)){
            Unmarshaller unmarsh = null;
            try {
                unmarsh = WPSMarshallerPool.getInstance().acquireUnmarshaller();
                Object value = unmarsh.unmarshal(new URL(source.get("href")));
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