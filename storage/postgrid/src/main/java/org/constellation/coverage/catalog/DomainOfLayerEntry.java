/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
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
package org.constellation.coverage.catalog;

import java.awt.geom.Dimension2D;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.geotools.util.DateRange;
import org.geotools.util.Utilities;
import org.constellation.catalog.Entry;


/**
 * The spatio-temporal domain of a layer. For internal use by {@link LayerEntry} only.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 */
final class DomainOfLayerEntry extends Entry {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 2371725033886216666L;

    /**
     * The time range, or {@code null} if none.
     */
    final DateRange timeRange;

    /**
     * The geographic bounding box, or {@code null} if none.
     */
    final GeographicBoundingBox bbox;

    /**
     * The resolution, or {@code null} if none.
     */
    final Dimension2D resolution;

    /**
     * Creates a new entry with the specified values, which are <strong>not</strong> cloned.
     */
    protected DomainOfLayerEntry(final String name,
                                 final DateRange timeRange,
                                 final GeographicBoundingBox bbox,
                                 final Dimension2D resolution,
                                 final String remarks)
    {
        super(name, remarks);
        this.timeRange  = timeRange;
        this.bbox       = bbox;
        this.resolution = resolution;
    }

    /**
     * Compares this layer with the specified object for equality.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final DomainOfLayerEntry that = (DomainOfLayerEntry) object;
            return Utilities.equals(this.timeRange,  that.timeRange ) &&
                   Utilities.equals(this.bbox,       that.bbox)       &&
                   Utilities.equals(this.resolution, that.resolution);
        }
        return false;
    }
}
