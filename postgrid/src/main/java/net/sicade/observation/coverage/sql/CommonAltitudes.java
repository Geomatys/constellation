/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2007, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package net.sicade.observation.coverage.sql;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import org.geotools.resources.Utilities;
import net.sicade.observation.coverage.Layer;


/**
 * A list of dates when the images have the same set of altitudes (<var>z</var>). For the
 * {@linkplain #getLayer given layer}, every images at the {@linkplain #getDates given dates}
 * have data available at the {@linkplain #getAltitudes given altitudes}.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 */
public final class CommonAltitudes implements Serializable {
    /**
     * For cross-version compatibility during serialization.
     */
    private static final long serialVersionUID = -5711801526749798234L;

    /**
     * The layer for the images, or {@code null}.
     */
    private final Layer layer;

    /**
     * The dates when the images have the same set of altitudes.
     */
    private final Set<Date> dates;

    /**
     * The common set of altitudes.
     */
    private final Set<Number> altitudes;

    /**
     * Creates a new instance for the given layer.
     *
     * @param layer The layer for the images, or {@code null}.
     * @param dates The dates when the images have the same set of altitudes.
     * @param altitudes The common set of altitudes.
     */
    CommonAltitudes(final Layer layer, final Set<Date> dates, final Set<Number> altitudes) {
        this.layer     = layer;
        this.dates     = dates;
        this.altitudes = altitudes;
    }

    /**
     * Returns the layer for the images, or {@code null}.
     */
    public Layer getLayer() {
        return layer;
    }

    /**
     * Returns the dates when the images have the same set of altitudes.
     */
    public Set<Date> getDates() {
        return dates;
    }

    /**
     * Returns the common set of altitudes.
     */
    public Set<Number> getAltitudes() {
        return altitudes;
    }

    /**
     * Returns a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return dates.hashCode() + 37*altitudes.hashCode();
    }

    /**
     * Compares this object with the specified object for equality.
     */
    @Override
    public boolean equals(final Object object) {
        if (object!=null && object.getClass().equals(getClass())) {
            final CommonAltitudes that = (CommonAltitudes) object;
            return Utilities.equals(this.layer,     that.layer) &&
                   Utilities.equals(this.dates,     that.dates) &&
                   Utilities.equals(this.altitudes, that.altitudes);
        }
        return false;
    }
}
