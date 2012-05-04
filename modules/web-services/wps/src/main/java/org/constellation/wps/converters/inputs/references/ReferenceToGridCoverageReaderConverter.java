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
import java.util.Map;
import org.constellation.wps.converters.inputs.AbstractInputConverter;
import org.geotoolkit.coverage.io.CoverageIO;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.util.converter.NonconvertibleObjectException;

/**
 * Implementation of ObjectConverter to convert a reference into a GridCoverageReader.
 *
 * @author Quentin Boileau (Geomatys).
 */
public final class ReferenceToGridCoverageReaderConverter extends AbstractInputConverter {

    private static ReferenceToGridCoverageReaderConverter INSTANCE;

    private ReferenceToGridCoverageReaderConverter() {
    }

    public static synchronized ReferenceToGridCoverageReaderConverter getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ReferenceToGridCoverageReaderConverter();
        }
        return INSTANCE;
    }

    @Override
    public Class<? extends Object> getTargetClass() {
        return GridCoverageReader.class;
    }

    /**
     * {@inheritDoc}
     *
     * @return GridCoverageReader.
     */
    @Override
    public GridCoverageReader convert(final Map<String, Object> source) throws NonconvertibleObjectException {

        final String href = (String) source.get(IN_HREF);
        try {
            final URL url = new URL(href);
            return CoverageIO.createSimpleReader(url);
        } catch (MalformedURLException ex) {
            throw new NonconvertibleObjectException("Reference grid coverage invalid input : Malformed url", ex);
        } catch (CoverageStoreException ex) {
            throw new NonconvertibleObjectException("Reference grid coverage invalid input : Can't read coverage", ex);
        } catch (IOException ex) {
            throw new NonconvertibleObjectException("Reference grid coverage invalid input : IO", ex);
        }
    }
}