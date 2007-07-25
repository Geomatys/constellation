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
import java.util.*;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import static java.lang.reflect.Array.getLength;
import static java.lang.reflect.Array.getDouble;

import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.operation.TransformException;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;

import net.sicade.observation.CatalogException;
import net.sicade.observation.IllegalRecordException;
import net.sicade.observation.sql.SpatialFunctions;
import net.sicade.observation.sql.SingletonTable;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.Shareable;


/**
 * Connection to a table of grid geometries.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public class GridGeometryTable extends SingletonTable<GridGeometryEntry> implements Shareable {
    /**
     * Constructs a new {@code GridGeometryTable}.
     *
     * @param  connection The connection to the database.
     * @throws SQLException if the table can't be constructed.
     */
    public GridGeometryTable(final Database database) throws SQLException {
        super(new GridGeometryQuery(database));
    }

    /**
     * Creates a grid geometry from the current row in the specified result set.
     *
     * @param  results The result set to read.
     * @return The entry for current row in the specified result set.
     * @throws CatalogException if an inconsistent record is found in the database.
     * @throws SQLException if an error occured while reading the database.
     */
    protected GridGeometryEntry createEntry(final ResultSet results) throws CatalogException, SQLException {
        final GridGeometryQuery query = (GridGeometryQuery) super.query;
        final String identifier = results.getString(indexOf(query.identifier));
        final int    width      = results.getInt   (indexOf(query.width));
        final int    height     = results.getInt   (indexOf(query.height));
        final String geometry   = results.getString(indexOf(query.horizontalExtent));
        final Array  vertical   = results.getArray (indexOf(query.verticalOrdinates));
        final double[] altitudes;
        if (vertical != null) {
            final Object data = vertical.getArray();
            final int length = getLength(data);
            altitudes = new double[length];
            for (int i=0; i<length; i++) {
                altitudes[i] = getDouble(data, i);
            }
//          altitudes.free(); // TODO: uncomment when we will be allowed to use Java 6.
        } else {
            altitudes = null;
        }
        final GeneralEnvelope envelope;
        try {
            envelope = SpatialFunctions.parse(geometry);
        } catch (NumberFormatException e) {
            throw new IllegalRecordException(getSpatialColumnName(results), e);
        }
        // TODO: Select a more appropriate CRS.
        switch (envelope.getDimension()) {
            case 2: envelope.setCoordinateReferenceSystem(DefaultGeographicCRS.WGS84   ); break;
            case 3: envelope.setCoordinateReferenceSystem(DefaultGeographicCRS.WGS84_3D); break;
        }
        final GeographicBoundingBox bbox;
        try {
            bbox = new GeographicBoundingBoxImpl(envelope);
        } catch (TransformException e) {
            throw new IllegalRecordException(getSpatialColumnName(results), e);
        }
        return new GridGeometryEntry(identifier, bbox, new Dimension(width, height), altitudes);
    }

    /**
     * Returns the name of the spatial column.
     */
    private String getSpatialColumnName(final ResultSet results) throws SQLException {
        return results.getMetaData().getTableName(indexOf(((GridGeometryQuery) query).horizontalExtent));
    }

    /**
     * For every values in the specified map, replace the collection of identifiers by a set of
     * altitudes. On input, the values are usually {@code List<String>}. On output, all values
     * will be {@code SortedSet<Number>}.
     *
     * @param  centroids The date-extents map.
     * @return The same reference than {@code centroids}, but casted as a date-altitudes map.
     */
    final SortedMap<Date,SortedSet<Number>> identifiersToAltitudes(final SortedMap<Date,List<String>> centroids)
            throws CatalogException, SQLException
    {
        final Map<Number,Number> numbers = new HashMap<Number,Number>(); // For sharing instances.
        final Map<SortedSet<Number>, SortedSet<Number>> pool = new HashMap<SortedSet<Number>, SortedSet<Number>>();
        final Map<Collection,SortedSet<Number>> altitudesMap = new HashMap<Collection,SortedSet<Number>>();
        for (final Map.Entry<Date,List<String>> entry : centroids.entrySet()) {
            final List<String> extents = entry.getValue();
            SortedSet<Number> altitudes = altitudesMap.get(extents);
            if (altitudes == null) {
                altitudes = new TreeSet<Number>();
                for (final String extent : extents) {
                    final double[] ordinates = getEntry(extent).getVerticalOrdinates();
                    if (ordinates != null) {
                        for (int i=0; i<ordinates.length; i++) {
                            final Number z = ordinates[i];
                            Number shared = numbers.get(z);
                            if (shared == null) {
                                shared = z;
                                numbers.put(shared, shared);
                            }
                            altitudes.add(shared);
                        }
                    }
                }
                /*
                 * Replaces the altitudes set by shared instances, in order to reduce memory usage.
                 * It is quite common to have many dates (if not all) associated with identical set
                 * of altitudes values.
                 */
                altitudes = Collections.unmodifiableSortedSet(altitudes);
                final SortedSet<Number> existing = pool.get(altitudes);
                if (existing != null) {
                    altitudes = existing;
                } else {
                    pool.put(altitudes, altitudes);
                }
                altitudesMap.put(extents, altitudes);
            }
            unsafe(entry, altitudes);
        }
        return unsafe(centroids);
    }

    /**
     * Unsafe setting on a map entry. Used because we changing the map type in-place.
     */
    @SuppressWarnings("unchecked")
    private static void unsafe(final Map.Entry entry, final SortedSet<Number> altitudes) {
        entry.setValue(altitudes);
    }

    /**
     * Unsafe cast of a map. Used because we changed the map type in-place.
     */
    @SuppressWarnings("unchecked")
    private static SortedMap<Date,SortedSet<Number>> unsafe(final SortedMap centroids) {
        return centroids;
    }

    /**
     * Returns the identifier for the specified grid geometry.
     * If no matching record is found, then this method returns {@code null}.
     *
     * @param  spatialExtent The three-dimensional envelope.
     * @param  size The image width, height and depth (in pixels) as an array of length 3.
     * @throws SQLException if the operation failed.
     */
//    public synchronized String getIdentifier(final Envelope spatialExtent, final int[] size)
//            throws SQLException
//    {
//        final GridGeometryQuery query = (GridGeometryQuery) super.query;
//        final PreparedStatement statement = getStatement(QueryType.SELECT);
//        query.byExtent.setEnvelope(statement, QueryType.SELECT, spatialExtent);
//        for (int i=0; i<3; i++) {
//            final Parameter p;
//            switch (i) {
//                case 0: p = query.byWidth;  break;
//                case 1: p = query.byHeight; break;
//                case 2: p = query.byDepth;  break;
//                default: throw new AssertionError(i);
//            }
//            statement.setInt(indexOf(p), i < size.length ? size[i] : 1);
//        }
//        String ID = null;
//        final ResultSet result = statement.executeQuery();
//        while (result.next()) {
//            final String nextID = result.getString(1);
//            if (ID!=null && !ID.equals(nextID)) {
//                final LogRecord record = Resources.getResources(getDatabase().getLocale()).
//                        getLogRecord(Level.WARNING, ResourceKeys.ERROR_DUPLICATED_GEOMETRY_$1, nextID);
//                record.setSourceClassName("GridGeometryTable");
//                record.setSourceMethodName("getIdentifier");
//                Element.LOGGER.log(record);
//            } else {
//                ID = nextID;
//            }
//        }
//        result.close();
//        return ID;
//    }

    /**
     * Ajoute une entrée pour l'étendue géographique et la dimension d'image spécifiée.
     */
//    public synchronized void addEntry(final String          identifier,
//                                      final GeographicBoundingBox bbox,
//                                      final Dimension             size)
//            throws CatalogException, SQLException
//    {
//        if (true) {
//            throw new CatalogException("Not yet implemented.");
//        }
//        final PreparedStatement statement = getStatement(QueryType.INSERT);
//        statement.setString(1, identifier);
//        setBoundingBox(statement, 1, bbox, size);
//        if (statement.executeUpdate() != 1) {
//            throw new CatalogException("L'étendue géographique n'a pas été ajoutée.");
//        }
//    }
}
