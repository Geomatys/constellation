/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
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
package net.sicade.coverage.catalog.sql;

import org.geotools.resources.Utilities;
import net.sicade.catalog.Entry;
import net.sicade.coverage.catalog.Format;
import net.sicade.coverage.catalog.Layer;
import net.sicade.coverage.catalog.Series;


/**
 * A series entry.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class SeriesEntry extends Entry implements Series {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -7991804359597967276L;

    /**
     * The layer which contains this series.
     */
    private final Layer layer;

    /**
     * The format of all coverages in this series.
     */
    private final Format format;

    /**
     * Creates a new series entry.
     *
     * @param name    The name for this series.
     * @param layer   The layer which contains this series.
     * @param format  The format of all coverages in this series.
     * @param remarks The remarks, or {@code null} if none.
     */
    protected SeriesEntry(final String name, final Layer layer, final Format format, final String remarks) {
        super(name, remarks);
        this.layer  = layer;
        this.format = format;
    }

    /**
     * {@inheritDoc}
     */
    public Layer getLayer() {
        return layer;
    }

    /**
     * {@inheritDoc}
     */
    public Format getFormat() {
        return format;
    }

    /**
     * Compare this series entry with the specified object for equality.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final SeriesEntry that = (SeriesEntry) object;
            return Utilities.equals(this.layer,  that.layer ) &&
                   Utilities.equals(this.format, that.format);
        }
        return false;
    }
}
