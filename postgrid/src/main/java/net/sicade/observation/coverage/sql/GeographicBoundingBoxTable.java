/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2006, Institut de Recherche pour le Développement
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

import java.awt.Dimension;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;

import net.sicade.observation.Element;
import net.sicade.observation.CatalogException;
import net.sicade.observation.sql.Table;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.Parameter;
import net.sicade.observation.sql.Shareable;
import net.sicade.observation.sql.QueryType;
import net.sicade.resources.i18n.Resources;
import net.sicade.resources.i18n.ResourceKeys;


/**
 * Connection to a table of geographic bounding box.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public class GeographicBoundingBoxTable extends Table implements Shareable {
    /**
     * Constructs a new {@code GeographicBoundingBoxTable}.
     *
     * @param  connection The connection to the database.
     * @throws SQLException if the table can't be constructed.
     */
    public GeographicBoundingBoxTable(final Database database) throws SQLException {
        super(new GeographicBoundingBoxQuery(database));
    }

    /**
     * Returns the identifier for the specified geographic bounding box and dimension.
     * If no matching record is found, then this method returns {@code null}. This is
     * a convenience method for the two-dimensional case. The <var>z</var> value is
     * set to 0 and the depth (in pixels) is set to 1.
     *
     * @param  bbox The geographic bounding box.
     * @param  size The image size, in pixels.
     * @throws SQLException if the operation failed.
     */
    public String getIdentifier(final GeographicBoundingBox bbox, final Dimension size)
            throws SQLException, CatalogException
    {
        final GeneralEnvelope envelope = new GeneralEnvelope(DefaultGeographicCRS.WGS84_3D);
        envelope.setRange(0, bbox.getWestBoundLongitude(), bbox.getEastBoundLongitude());
        envelope.setRange(1, bbox.getSouthBoundLatitude(), bbox.getNorthBoundLatitude());
        // The 2th dimension is initialized to 0, which is exactly what we want.
        return getIdentifier(envelope, new int[] {size.width, size.height, 1});
    }

    /**
     * Returns the identifier for the specified envelope and grid range.
     * If no matching record is found, then this method returns {@code null}.
     *
     * @param  spatialExtent The three-dimensional envelope.
     * @param  size The image width, height and depth (in pixels) as an array of length 3.
     * @throws SQLException if the operation failed.
     */
    public synchronized String getIdentifier(final Envelope spatialExtent, final int[] size)
            throws SQLException
    {
        final GeographicBoundingBoxQuery query = (GeographicBoundingBoxQuery) super.query;
        final PreparedStatement statement = getStatement(QueryType.SELECT);
        query.byExtent.setEnvelope(statement, QueryType.SELECT, spatialExtent);
        for (int i=0; i<3; i++) {
            final Parameter p;
            switch (i) {
                case 0: p = query.byWidth;  break;
                case 1: p = query.byHeight; break;
                case 2: p = query.byDepth;  break;
                default: throw new AssertionError(i);
            }
            statement.setInt(indexOf(p), i < size.length ? size[i] : 1);
        }
        String ID = null;
        final ResultSet result = statement.executeQuery();
        while (result.next()) {
            final String nextID = result.getString(1);
            if (ID!=null && !ID.equals(nextID)) {
                final LogRecord record = Resources.getResources(getDatabase().getLocale()).
                        getLogRecord(Level.WARNING, ResourceKeys.ERROR_DUPLICATED_GEOMETRY_$1, nextID);
                record.setSourceClassName("GeographicBoundingBoxTable");
                record.setSourceMethodName("getIdentifier");
                Element.LOGGER.log(record);
            } else {
                ID = nextID;
            }
        }
        result.close();
        return ID;
    }

    /**
     * Ajoute une entrée pour l'étendue géographique et la dimension d'image spécifiée.
     *
     * @todo Not yet implemented.
     */
    @Deprecated
    public synchronized void addEntry(final String          identifier,
                                      final GeographicBoundingBox bbox,
                                      final Dimension             size)
            throws CatalogException, SQLException
    {
        if (true) {
            throw new CatalogException("Not yet implemented.");
        }
        final PreparedStatement statement = getStatement(QueryType.INSERT);
        statement.setString(1, identifier);
//        setBoundingBox(statement, 1, bbox, size);
        if (statement.executeUpdate() != 1) {
            throw new CatalogException("L'étendue géographique n'a pas été ajoutée.");
        }
    }
}
