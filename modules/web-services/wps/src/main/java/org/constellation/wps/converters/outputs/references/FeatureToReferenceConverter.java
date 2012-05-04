/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.wps.converters.outputs.references;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import org.geotoolkit.data.DataStoreRuntimeException;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.feature.xml.XmlFeatureTypeWriter;
import org.geotoolkit.feature.xml.XmlFeatureWriter;
import org.geotoolkit.feature.xml.jaxb.JAXBFeatureTypeWriter;
import org.geotoolkit.feature.xml.jaxp.JAXPStreamFeatureWriter;
import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.geotoolkit.wps.xml.v100.OutputReferenceType;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;

/**
 * Implementation of ObjectConverter to convert a {@link Feature feature} into a {@link OutputReferenceType reference}.
 * 
 * @author Quentin Boileau (Geomatys).
 */
public class FeatureToReferenceConverter extends AbstractReferenceOutputConverter {

    private static FeatureToReferenceConverter INSTANCE;

    private FeatureToReferenceConverter(){
    }

    public static synchronized FeatureToReferenceConverter getInstance(){
        if(INSTANCE == null){
            INSTANCE = new FeatureToReferenceConverter();
        }
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputReferenceType convert(final Map<String,Object> source) throws NonconvertibleObjectException {
        
        
        final OutputReferenceType reference = new OutputReferenceType();
        
        reference.setMimeType((String) source.get(OUT_MIME));
        reference.setEncoding((String) source.get(OUT_ENCODING));
        
        final Object data = source.get(OUT_DATA);
        FeatureType ft = null;
        if (data instanceof Feature) {
            ft = ((Feature) data).getType();
        } else if (data instanceof FeatureCollection) {
            ft = ((FeatureCollection) data).getFeatureType();
        } else {
            throw new NonconvertibleObjectException("The requested output reference data is not an instance of Feature or FeatureCollection.");
        }
        
        final String namespace = ft.getName().getURI();
        final Map <String, String> schemaLocation = new HashMap<String, String>();
        
        final String randomFileName = UUID.randomUUID().toString();
        
        //Write FeatureType
        try {
            
            final String schemaFileName = randomFileName + "_schema" + ".xsd";
            
            //create file
            final File schemaFile = new File((String) source.get(OUT_TMP_DIR_PATH), schemaFileName);
            final OutputStream schemaStream = new FileOutputStream(schemaFile);
            
            //write featureType xsd on file
            final XmlFeatureTypeWriter xmlFTWriter = new JAXBFeatureTypeWriter();
            xmlFTWriter.write(ft, schemaStream);
            
            reference.setSchema((String) source.get(OUT_TMP_DIR_URL) + "/" +schemaFileName);
            schemaLocation.put(namespace, reference.getSchema());
            
        } catch (JAXBException ex) {
            throw new NonconvertibleObjectException("Can't write FeatureType into xsd schema.",ex);
        } catch (FileNotFoundException ex) {
            throw new NonconvertibleObjectException("Can't create xsd schema file.",ex);
        }
             
        //Write Feature
        XmlFeatureWriter featureWriter = null;
        try {
            
            final String dataFileName = randomFileName+".xml"; 
            
            //create file
            final File dataFile = new File((String) source.get(OUT_TMP_DIR_PATH), dataFileName);
            final OutputStream dataStream = new FileOutputStream(dataFile);
            
            //Write feature in file
            featureWriter = new JAXPStreamFeatureWriter(schemaLocation);
            featureWriter.write(data, dataStream);
            reference.setHref((String) source.get(OUT_TMP_DIR_URL) + "/" +dataFileName);
            
        } catch (IOException ex) {
            throw new NonconvertibleObjectException(ex);
        } catch (XMLStreamException ex) {
            throw new NonconvertibleObjectException("Stax exception while writing the feature collection", ex);
        } catch (DataStoreException ex) {
            throw new NonconvertibleObjectException("DataStore exception while writing the feature collection", ex);
        } catch (DataStoreRuntimeException ex) {
            throw new NonconvertibleObjectException("DataStoreRuntimeException exception while writing the feature collection", ex);
        } finally {
            try {
                if (featureWriter != null) {
                    featureWriter.dispose();
                }
            } catch (IOException ex) {
                 throw new NonconvertibleObjectException(ex);
            } catch (XMLStreamException ex) {
                 throw new NonconvertibleObjectException(ex);
            }
        }
        return reference;
    }
    
}
