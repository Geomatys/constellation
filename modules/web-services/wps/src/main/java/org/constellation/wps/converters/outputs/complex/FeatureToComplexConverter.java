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
package org.constellation.wps.converters.outputs.complex;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import org.constellation.wps.utils.WPSUtils;
import org.geotoolkit.feature.xml.XmlFeatureTypeWriter;
import org.geotoolkit.feature.xml.jaxb.JAXBFeatureTypeWriter;
import org.geotoolkit.feature.xml.jaxp.ElementFeatureWriter;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.geotoolkit.wps.xml.v100.ComplexDataType;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;



/**
 * Implementation of ObjectConverter to convert a Feature into a an object which can be supported
 * by JAXB.
 * 
 * @author Quentin Boileau
 */
public final class FeatureToComplexConverter extends AbstractComplexOutputConverter {

    private static FeatureToComplexConverter INSTANCE;

    private FeatureToComplexConverter(){
    }

    public static synchronized FeatureToComplexConverter getInstance(){
        if(INSTANCE == null){
            INSTANCE = new FeatureToComplexConverter();
        }
        return INSTANCE;
    } 
    
    @Override
    public ComplexDataType convert(final Map<String, Object> source) throws NonconvertibleObjectException {
        
        final ComplexDataType complex = new ComplexDataType();
        
        complex.setMimeType((String) source.get(OUT_MIME));
        complex.setEncoding((String) source.get(OUT_ENCODING));
        
        final Feature feature = (Feature) source.get(OUT_DATA);
        final FeatureType ft = feature.getType();
        
        try {
            
            final String schemaFileName = "schema_"+UUID.randomUUID().toString()+".xsd";
            //create file
            final File schemaFile = new File((String) source.get(OUT_TMP_DIR_PATH), schemaFileName);
            final OutputStream stream = new FileOutputStream(schemaFile);
            //write featureType xsd on file
            final XmlFeatureTypeWriter xmlFTWriter = new JAXBFeatureTypeWriter();
            xmlFTWriter.write(ft, stream);
            
            complex.setSchema((String) source.get(OUT_TMP_DIR_URL) + "/" +schemaFileName);
        
        } catch (JAXBException ex) {
            throw new NonconvertibleObjectException("Can't write FeatureType into xsd schema.",ex);
        } catch (FileNotFoundException ex) {
            throw new NonconvertibleObjectException("Can't create xsd schema file.",ex);
        }
        
        try {
            
            final ElementFeatureWriter efw = new ElementFeatureWriter();
            complex.getContent().add(efw.writeFeature(feature, null, true));
            
        } catch (ParserConfigurationException ex) {
             throw new NonconvertibleObjectException("Can't write FeatureCollection into ResponseDocument.",ex);
        }

       return  complex;
       
      
    }
}

