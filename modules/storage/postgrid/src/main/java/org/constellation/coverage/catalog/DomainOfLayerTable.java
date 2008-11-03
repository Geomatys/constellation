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
package org.constellation.coverage.catalog;

import java.util.Date;
import java.util.Calendar;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.constellation.catalog.Database;
import org.constellation.catalog.SingletonTable;
import org.constellation.catalog.CatalogException;

import org.geotools.util.DateRange;
import org.geotools.resources.geometry.XDimension2D;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;


/**
 * Connection to a table of domain of layers. For internal use by {@link LayerTable} only.
 * Defined as a separated table because while many layers entry may be created (and their
 * cache flushed), only one {@code DomainOfLayerTable} is enough, and we want to preserve
 * its cache. This is especially important since this table is actually a query that may
 * be costly.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class DomainOfLayerTable extends SingletonTable<DomainOfLayerEntry> {
    /**
     * Creates a domain of layer table.
     *
     * @param database Connection to the database.
     */
    public DomainOfLayerTable(final Database database) {
        this(new DomainOfLayerQuery(database));
    }

    /**
     * Constructs a new {@code DomainOfLayerTable} from the specified query.
     */
    private DomainOfLayerTable(final DomainOfLayerQuery query) {
        super(query);
        setIdentifierParameters(query.byLayer, null);
    }

    /**
     * Creates a layer from the current row in the specified result set.
     *
     * @param  results The result set to read.
     * @return The entry for current row in the specified result set.
     * @throws CatalogException if an inconsistent record is found in the database.
     * @throws SQLException if an error occured while reading the database.
     */
    protected DomainOfLayerEntry createEntry(final ResultSet results) throws CatalogException, SQLException {
        final DomainOfLayerQuery query = (DomainOfLayerQuery) super.query;
        final Calendar calendar = getCalendar();
        String name        = results.getString(indexOf(query.layer));
        Date   startTime   = results.getTimestamp(indexOf(query.startTime), calendar);
        Date   endTime     = results.getTimestamp(indexOf(query.endTime), calendar);
        double west        = results.getDouble(indexOf(query.west));  if (results.wasNull()) west  = -180;
        double east        = results.getDouble(indexOf(query.east));  if (results.wasNull()) east  = +180;
        double south       = results.getDouble(indexOf(query.south)); if (results.wasNull()) south =  -90;
        double north       = results.getDouble(indexOf(query.north)); if (results.wasNull()) north =  +90;
        double xResolution = results.getDouble(indexOf(query.xResolution));
        double yResolution = results.getDouble(indexOf(query.yResolution));
        // Replace java.sql.Timestamp by java.util.Date.
        if (startTime != null) {
            startTime = new Date(startTime.getTime());
        }
        if (endTime != null) {
            endTime = new Date(endTime.getTime());
        }
        final GeographicBoundingBoxImpl bbox = new GeographicBoundingBoxImpl(west, east, south, north);
        bbox.freeze();
        return new DomainOfLayerEntry(name,
                (startTime!=null || endTime!=null) ? new DateRange(startTime, endTime) : null, bbox,
                (xResolution>0 || yResolution>0) ? new XDimension2D.Double(xResolution, yResolution) : null, null);
    }
}
