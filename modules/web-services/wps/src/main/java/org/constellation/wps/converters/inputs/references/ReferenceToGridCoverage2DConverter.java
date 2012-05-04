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
import org.constellation.wps.converters.inputs.AbstractInputConverter;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.coverage.io.CoverageIO;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.util.converter.NonconvertibleObjectException;

/**
 * Implementation of ObjectConverter to convert a reference into a GridCoverage2D.
 *
 * @author Quentin Boileau (Geomatys).
 */
public final class ReferenceToGridCoverage2DConverter extends AbstractInputConverter {

    private static ReferenceToGridCoverage2DConverter INSTANCE;

    private ReferenceToGridCoverage2DConverter() {
    }

    public static synchronized ReferenceToGridCoverage2DConverter getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ReferenceToGridCoverage2DConverter();
        }
        return INSTANCE;
    }

    @Override
    public Class<? extends Object> getTargetClass() {
        return GridCoverage2D.class;
    }

    /**
     * {@inheritDoc}
     *
     * @return GridCoverage2D.
     */
    @Override
    public GridCoverage2D convert(final Map<String, Object> source) throws NonconvertibleObjectException {

        final String href = (String) source.get(IN_HREF);
        GridCoverageReader reader = null;
        try {

            final URL url = new URL(href);
            reader = CoverageIO.createSimpleReader(url);
            return (GridCoverage2D) reader.read(0, null);

        } catch (MalformedURLException ex) {
            throw new NonconvertibleObjectException("Reference coverage invalid input : IO", ex);
        } catch (CoverageStoreException ex) {
            throw new NonconvertibleObjectException("Reference coverage invalid input : Can't read coverage", ex);
        } finally {
            if (reader != null) {
                try {
                    reader.dispose();
                } catch (CoverageStoreException ex) {
                    throw new NonconvertibleObjectException("Error during release the coverage reader.", ex);
                }
            }
        }
    }
}