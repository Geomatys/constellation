/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
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

package org.constellation.lucene.filter;

import java.io.IOException;
import java.util.BitSet;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.geotools.geometry.GeneralEnvelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

/**
 *
 * @author Guilhem Legal
 */
public class OverlapsFilter extends SpatialFilter {

    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 7025924989513225447L;

    /**
     * Initialize the filter with the specified geometry and filterType.
     *
     * @param geometry   A geometry object, supported types are: GeneralEnvelope, GeneralDirectPosition, Line2D.
     * @param filterType A flag representing the type of spatial filter to apply restricted to Beyond and Dwithin.
     * @param distance   The distance to applies to this filter.
     * @param units      The unit of measure of the distance.
     */
    public OverlapsFilter(Object geometry, String crsName) throws NoSuchAuthorityCodeException, FactoryException  {
       super(geometry, crsName);
    }

    @Override
    public BitSet bits(IndexReader reader) throws IOException {
        // we prepare the result
        BitSet bits = new BitSet(reader.maxDoc());

        TermDocs termDocs = reader.termDocs(new Term("geometry"));

        // we search for matching box
        termDocs.seek(new Term("geometry", "boundingbox"));
        while (termDocs.next()) {
            int docNum = termDocs.doc();
            GeneralEnvelope tempBox = readBoundingBox(reader, docNum);
            if (tempBox == null)
                continue;
            if (boundingBox != null && GeometricUtilities.overlaps(boundingBox, tempBox))
                        bits.set(docNum);
        }
        return bits;
    }
}
