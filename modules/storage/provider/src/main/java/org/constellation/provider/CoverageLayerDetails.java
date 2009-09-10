/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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
package org.constellation.provider;

import java.util.Set;
import org.constellation.coverage.catalog.Layer;
import org.constellation.coverage.catalog.Series;


/**
 * Coverage extension of a {@link LayerDetails}, which add some methods specific
 * for coverage layers.
 *
 * @version $Id$
 * @author Cédric Briançon (Geomatys)
 *
 * @since 0.4
 */
public interface CoverageLayerDetails extends LayerDetails {
    /**
     * @see Layer#getSeries
     */
    Set<Series> getSeries();

    /**
     * @see Layer#getRemarks
     */
    String getRemarks();

    /**
     * @see Layer#getThematic
     */
    String getThematic();
}
