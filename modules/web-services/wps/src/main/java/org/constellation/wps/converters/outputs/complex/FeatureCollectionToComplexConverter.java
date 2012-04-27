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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import org.constellation.wps.utils.WPSUtils;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.feature.xml.XmlFeatureTypeWriter;
import org.geotoolkit.feature.xml.jaxb.JAXBFeatureTypeWriter;
import org.geotoolkit.feature.xml.jaxp.ElementFeatureWriter;
import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.geotoolkit.wps.xml.v100.ComplexDataType;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;

/**
 * Implementation of ObjectConverter to convert a FeatureCollection into a an object which can be supported by JAXB.
 *
 * @author Quentin Boileau
 */
public final class FeatureCollectionToComplexConverter extends AbstractComplexOutputConverter {

    private static FeatureCollectionToComplexConverter INSTANCE;

    private FeatureCollectionToComplexConverter() {
    }

    public static synchronized FeatureCollectionToComplexConverter getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FeatureCollectionToComplexConverter();
        }
        return INSTANCE;
    }

    @Override
    public ComplexDataType convert(final Map<String, Object> source) throws NonconvertibleObjectException {

        final ComplexDataType complex = new ComplexDataType();

        complex.setMimeType((String) source.get(OUT_MIME));
        complex.setEncoding((String) source.get(OUT_ENCODING));

        if (!(source.get(OUT_DATA) instanceof FeatureCollection)) {
            throw new NonconvertibleObjectException("The requested output data is not an instance of FeatureCollection.");
        }

        final FeatureCollection featureColl = (FeatureCollection) source.get(OUT_DATA);
        final FeatureType ft = featureColl.getFeatureType();
        final String namespace = ft.getName().getURI();
        final Map<String, String> schemaLocation = new HashMap<String, String>();

        try {

            final String schemaFileName = "schema_" + UUID.randomUUID().toString() + ".xsd";
            //create file
            final File schemaFile = new File((String) source.get(OUT_TMP_DIR_PATH), schemaFileName);
            final OutputStream stream = new FileOutputStream(schemaFile);
            //write featureType xsd on file
            final XmlFeatureTypeWriter xmlFTWriter = new JAXBFeatureTypeWriter();
            xmlFTWriter.write(ft, stream);

            complex.setSchema((String) source.get(OUT_TMP_DIR_URL) + "/" + schemaFileName);
            schemaLocation.put(namespace, complex.getSchema());
        } catch (JAXBException ex) {
            throw new NonconvertibleObjectException("Can't write FeatureType into xsd schema.", ex);
        } catch (FileNotFoundException ex) {
            throw new NonconvertibleObjectException("Can't create xsd schema file.", ex);
        }

        try {

            final ElementFeatureWriter efw = new ElementFeatureWriter(schemaLocation);
            complex.getContent().add(efw.writeFeatureCollection(featureColl, true, false));

        } catch (DataStoreException ex) {
            throw new NonconvertibleObjectException("Can't write FeatureCollection into ResponseDocument.", ex);
        } catch (ParserConfigurationException ex) {
            throw new NonconvertibleObjectException("Can't write FeatureCollection into ResponseDocument.", ex);
        }

        return complex;

    }
}
