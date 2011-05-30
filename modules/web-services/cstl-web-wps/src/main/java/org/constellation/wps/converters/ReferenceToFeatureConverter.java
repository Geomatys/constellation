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
import java.util.Map;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import org.constellation.wps.ws.WPSWorker;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;
import org.geotoolkit.feature.xml.XmlFeatureTypeReader;
import org.geotoolkit.feature.xml.jaxb.JAXBFeatureTypeReader;
import org.geotoolkit.feature.xml.jaxp.JAXPStreamFeatureReader;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.geotoolkit.util.converter.SimpleConverter;
import org.opengis.feature.Feature;



/**
 * Implementation of ObjectConverter to convert a reference into a Feature.
 * Reference is define by a <code>Map<String,String></code> with entries keys :
 * <ul>
 * <li>href : Url to the data</li>
 * <li>mime : mime type of the data like text/xml, ...</li>
 * <li>schema : is the data requires a schema</li>
 * <li>encoding : the data encoding like UTF8, ...</li>
 * <li>method : GET or POST</li>
 * </ul>
 * @author Quentin Boileau
 * @module pending
 */
public class ReferenceToFeatureConverter extends SimpleConverter<Map<String,String>, Feature> {

    private static ReferenceToFeatureConverter INSTANCE;

    private ReferenceToFeatureConverter(){
    }

    public static ReferenceToFeatureConverter getInstance(){
        if(INSTANCE == null){
            INSTANCE = new ReferenceToFeatureConverter();
        }
        return INSTANCE;
    }

    @Override
    public Class<? super Map> getSourceClass() {
        return Map.class;
    }

    @Override
    public Class<? extends Feature> getTargetClass() {
        return Feature.class ;
    }
 
    @Override
    public Feature convert(Map<String,String> source) throws NonconvertibleObjectException {
            if (source.get("mime") == null) {
                throw new NonconvertibleObjectException("Invalid reference input : typeMime can't be null.");
            }
            //XML
            if(source.get("mime").equalsIgnoreCase(MimeType.TEXT_XML)){
                 try {
                    final XmlFeatureTypeReader xsdReader = new JAXBFeatureTypeReader();
                    JAXPStreamFeatureReader featReader;
                    
                    if(source.get("schema") != null){
                        final URL schemaURL = new URL(source.get("schema"));
                        featReader = new JAXPStreamFeatureReader(xsdReader.read(schemaURL.openStream()));
                    }else{
                         featReader = new JAXPStreamFeatureReader();
                         featReader.setReadEmbeddedFeatureType(true);
                    }
                    
                    Feature feat = (Feature)featReader.read(new URL(source.get("href")));
                    feat = (Feature) WPSWorker.fixFeature(feat);
                    return feat;

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
            }else {
                 throw new NonconvertibleObjectException("Reference data mime is not supported");
            }
    }
}