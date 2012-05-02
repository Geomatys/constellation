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

import java.awt.image.RenderedImage;
import java.io.File;
import java.util.Map;
import java.util.UUID;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.coverage.io.CoverageIO;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageWriter;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.geotoolkit.wps.xml.v100.OutputReferenceType;
import org.opengis.coverage.Coverage;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public class CoverageToReferenceConverter extends AbstractReferenceOutputConverter {

    private static CoverageToReferenceConverter INSTANCE;

    private CoverageToReferenceConverter() {
    }

    public static synchronized CoverageToReferenceConverter getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CoverageToReferenceConverter();
        }
        return INSTANCE;
    }
    
    @Override
    public OutputReferenceType convert(final Map<String, Object> source) throws NonconvertibleObjectException {
        final OutputReferenceType reference = new OutputReferenceType();

        reference.setMimeType((String) source.get(OUT_MIME));
        reference.setEncoding((String) source.get(OUT_ENCODING));
        reference.setSchema((String) source.get(OUT_SCHEMA));

        final Object data = source.get(OUT_DATA);

        if (!(data instanceof Coverage)) {
            throw new NonconvertibleObjectException("The geometry is not an JTS geometry.");
        }
        
        final String randomFileName = UUID.randomUUID().toString();
        GridCoverageWriter writer = null;
    
        try {
            final File imageFile = new File((String) source.get(OUT_TMP_DIR_PATH), randomFileName);
            writer = CoverageIO.createSimpleWriter(imageFile);
            writer.write((GridCoverage2D) data, null);
        } catch (CoverageStoreException ex) {
            throw new NonconvertibleObjectException("Error during wrtie the coverage in the output file.",ex);
        } finally {
            if (writer != null) {
                try {
                    writer.dispose();
                } catch (CoverageStoreException ex) {
                    throw new NonconvertibleObjectException("Error during release the coverage writer.",ex);
                }
            }
        }
        
        return reference;
    }
    
}
