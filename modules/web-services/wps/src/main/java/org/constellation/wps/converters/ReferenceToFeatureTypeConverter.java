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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBException;
import org.constellation.ws.MimeType;
import org.geotoolkit.feature.xml.XmlFeatureTypeReader;
import org.geotoolkit.feature.xml.jaxb.JAXBFeatureTypeReader;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.geotoolkit.util.converter.SimpleConverter;
import org.opengis.feature.type.FeatureType;



/**
 * Implementation of ObjectConverter to convert a reference into a FeatureType.
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
public final class ReferenceToFeatureTypeConverter extends SimpleConverter<Map<String,String>, FeatureType> {

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
    public Class<? super Map> getSourceClass() {
        return Map.class;
    }

    @Override
    public Class<? extends FeatureType> getTargetClass() {
        return FeatureType.class ;
    }
 
    @Override
    public FeatureType convert(Map<String,String> source) throws NonconvertibleObjectException {

        if (source.get("mime") == null) {
            throw new NonconvertibleObjectException("Invalid reference input : typeMime can't be null.");
        }
        //XML
        if(source.get("mime").equalsIgnoreCase(MimeType.TEXT_XML)){
             try {
                final XmlFeatureTypeReader xsdReader = new JAXBFeatureTypeReader();
                final URL schemaURL = new URL(source.get("href"));
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