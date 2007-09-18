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
package net.sicade.coverage.catalog;

import java.util.*;
import java.sql.Types;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import static java.lang.reflect.Array.getLength;
import static java.lang.reflect.Array.getDouble;

import org.opengis.coverage.grid.GridRange;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.datum.PixelInCell;

import org.geotools.geometry.GeneralEnvelope;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultCompoundCRS;
import org.geotools.referencing.operation.matrix.MatrixFactory;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;

import net.sicade.catalog.CatalogException;
import net.sicade.catalog.IllegalRecordException;
import net.sicade.catalog.SpatialFunctions;
import net.sicade.catalog.SingletonTable;
import net.sicade.catalog.Database;
import net.sicade.catalog.QueryType;
import net.sicade.resources.i18n.Resources;
import net.sicade.resources.i18n.ResourceKeys;


/**
 * Connection to a table of grid geometries.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public class GridGeometryTable extends SingletonTable<GridGeometryEntry> {
    /**
     * The authority for CRS.
     *
     * @todo Should be obtained from the "spatial_ref_sys" table instead.
     *       We should also parse the WKT if the code is not found.
     */
    private static final String CRS_AUTHORITY = "EPSG:";

    /**
     * Constructs a new {@code GridGeometryTable}.
     *
     * @param connection The connection to the database.
     */
    public GridGeometryTable(final Database database) {
        this(new GridGeometryQuery(database));
    }

    /**
     * Constructs a new {@code GridGeometryTable} from the specified query.
     */
    private GridGeometryTable(final GridGeometryQuery query) {
        super(query);
        setIdentifierParameters(query.byIdentifier, null);
    }

    /**
     * Creates a grid geometry from the current row in the specified result set.
     *
     * @param  results The result set to read.
     * @return The entry for current row in the specified result set.
     * @throws CatalogException if an inconsistent record is found in the database.
     * @throws SQLException if an error occured while reading the database.
     */
    @SuppressWarnings("fallthrough")
    protected GridGeometryEntry createEntry(final ResultSet results) throws CatalogException, SQLException {
        final GridGeometryQuery query  = (GridGeometryQuery) super.query;
        final String identifier        = results.getString(indexOf(query.identifier));
        final int    width             = results.getInt   (indexOf(query.width));
        final int    height            = results.getInt   (indexOf(query.height));
        final double scaleX            = results.getDouble(indexOf(query.scaleX));
        final double scaleY            = results.getDouble(indexOf(query.scaleY));
        final double translateX        = results.getDouble(indexOf(query.translateX));
        final double translateY        = results.getDouble(indexOf(query.translateY));
        final double shearX            = results.getDouble(indexOf(query.shearX));
        final double shearY            = results.getDouble(indexOf(query.shearY));
        final int    horizontalSRID    = results.getInt   (indexOf(query.horizontalSRID));
        final String horizontalExtent  = results.getString(indexOf(query.horizontalExtent));
        final int    verticalSRID      = results.getInt   (indexOf(query.verticalSRID));
        final Array  verticalOrdinates = results.getArray (indexOf(query.verticalOrdinates));
        /*
         * Creates the horizontal CRS. We will append a vertical CRS later if
         * the vertical, ordinates array is non-null, and a temporal CRS last.
         */
        CoordinateReferenceSystem crs;
        try {
            crs = CRS.decode(CRS_AUTHORITY + horizontalSRID, true);
        } catch (FactoryException exception) {
            throw new IllegalRecordException(exception, results, indexOf(query.horizontalSRID), identifier);
        }
        /*
         * Copies the vertical ordinates in an array of type double[].
         * The array will be 'null' if there is no vertical ordinates.
         */
        CoordinateReferenceSystem verticalCRS = null;
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        final double[] altitudes = asDoubleArray(verticalOrdinates);
        if (altitudes != null) {
            for (double z : altitudes) {
                if (z < min) min = z;
                if (z > max) max = z;
            }
            try {
                verticalCRS = CRS.decode(CRS_AUTHORITY + verticalSRID);
            } catch (FactoryException exception) {
                throw new IllegalRecordException(exception, results, indexOf(query.verticalSRID), identifier);
            }
        }
        /*
         * Adds the temporal axis. TODO: HARD CODED FOR NOW. We need to do something better.
         */
        final CoordinateReferenceSystem temporalCRS = CRS.getTemporalCRS(net.sicade.catalog.CRS.XYT.getCoordinateReferenceSystem());
        if (verticalCRS != null) {
            final String name = crs.getName().getCode() + ", " + verticalCRS.getName().getCode();
            crs = new DefaultCompoundCRS(name, new CoordinateReferenceSystem[] {crs, verticalCRS, temporalCRS});
        } else {
            crs = new DefaultCompoundCRS(crs.getName().getCode(), crs, temporalCRS);
        }
        /*
         * Creates the "grid to CRS" transform as a matrix. The coefficients for the vertical
         * axis assume that the vertical ordinates are evenly spaced. This is not always true;
         * a special processing will be performed later.
         */
        final int dim = crs.getCoordinateSystem().getDimension();
        final int[] lower = new int[dim];
        final int[] upper = new int[dim];
        final Matrix gridToCRS = MatrixFactory.create(dim + 1);
        gridToCRS.setElement(0, 0,   scaleX);
        gridToCRS.setElement(1, 1,   scaleY);
        gridToCRS.setElement(0, 1,   shearX);
        gridToCRS.setElement(1, 0,   shearY);
        gridToCRS.setElement(0, dim, translateX);
        gridToCRS.setElement(1, dim, translateY);
        if (altitudes != null) {
            upper[2] = altitudes.length;
            switch (altitudes.length) { // Fall through in every cases.
                default: gridToCRS.setElement(2, 2, (max - min) / altitudes.length);
                case 1:  gridToCRS.setElement(2, dim, min);
                case 0:  break;
            }
        }
        upper[1] = height;
        upper[0] = width;
        /*
         * Computes the envelope from the affine transform and creates the entry.
         */
        final GridRange gridRange = new GeneralGridRange(lower, upper);
        final GeneralEnvelope envelope = new GeneralEnvelope(gridRange, PixelInCell.CELL_CORNER,
                ProjectiveTransform.create(gridToCRS), crs);
        if (altitudes != null) {
            envelope.setRange(2, min, max); // For fixing rounding errors.
        }
        final GeneralEnvelope geographicEnvelope;
        if (horizontalExtent != null) {
            geographicEnvelope = SpatialFunctions.parse(horizontalExtent);
        } else {
            geographicEnvelope = envelope;
        }
        final GeographicBoundingBox bbox;
        try {
            bbox = new GeographicBoundingBoxImpl(geographicEnvelope);
        } catch (TransformException exception) {
            throw new IllegalRecordException(exception, results, indexOf(query.horizontalExtent), identifier);
        }
        /*
         * Creates the entry and performs some final checks.
         */
        final AffineTransform at = new AffineTransform(scaleX, shearY, shearX, scaleY, translateX, translateY);
        final GridGeometryEntry entry = new GridGeometryEntry(identifier, at, gridRange, envelope, bbox, altitudes);
        if (entry.geographicEnvelope.isEmpty()) {
            throw new IllegalRecordException("L'enveloppe géographique est vide. Elle a été calculée à partir de \"" +
                    horizontalExtent + "\".", results, indexOf(query.horizontalExtent), identifier);
        }
        return entry;
    }

    /**
     * Returns the specified SQL array as an array of type {@code double[]}, or {@code null}
     * if the SQL array is null. The array if {@linkplain Array#free freeded} by this method.
     */
    private static double[] asDoubleArray(final Array verticalOrdinates) throws SQLException {
        final double[] altitudes;
        if (verticalOrdinates != null) {
            final Object data = verticalOrdinates.getArray();
            final int length = getLength(data);
            altitudes = new double[length];
            for (int i=0; i<length; i++) {
                final double z = getDouble(data, i);
                altitudes[i] = z;
            }
//          altitudes.free(); // TODO: uncomment when we will be allowed to use Java 6.
        } else {
            altitudes = null;
        }
        return altitudes;
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
     * Unsafe setting on a map entry. Used because we are changing the map type in-place.
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
     * Returns the identifier for the specified grid geometry. If no matching record is found and
     * {@code allowCreate} is {@code true}, then a new one is created and added to the database.
     *
     * @param  size              The image width and height in pixels.
     * @param  gridToCRS         The transform from grid coordinates to "real world" coordinates.
     * @param  horizontalSRID    The "real world" horizontal coordinate reference system.
     * @param  verticalOrdinates The vertical coordinates, or {@code null}.
     * @param  verticalSRID      The "real world" vertical coordinate reference system.
     *                           Ignored if {@code verticalOrdinates} is {@code null}.
     * @param  newIdentifier
     *              If non-null, then this method is allowed to create a new entry if none was
     *              found and will use the specified identifier expanded with suffix if needed
     *              ({@code "-001"}, {@code "-002"}, <cite>etc.</cite>). If {@code null}, then
     *              this method will not create any new entry if none was found.
     * @return
     *              The identifier of a matching entry, or {@code null} if none if none was
     *              found and {@code newIdentifier} is {@code null}.
     * @throws SQLException
     *              If the operation failed.
     */
    final synchronized String getIdentifier(final Dimension size,
                                            final AffineTransform  gridToCRS, final int horizontalSRID,
                                            final double[] verticalOrdinates, final int verticalSRID,
                                            final String newIdentifier)
            throws SQLException, CatalogException
    {
        final GridGeometryQuery query = (GridGeometryQuery) super.query;
        PreparedStatement statement = getStatement(QueryType.FILTERED_LIST);
        statement.setInt   (indexOf(query.byWidth),          size.width );
        statement.setInt   (indexOf(query.byHeight),         size.height);
        statement.setDouble(indexOf(query.byScaleX),         gridToCRS.getScaleX());
        statement.setDouble(indexOf(query.byScaleY),         gridToCRS.getScaleY());
        statement.setDouble(indexOf(query.byTranslateX),     gridToCRS.getTranslateX());
        statement.setDouble(indexOf(query.byTranslateY),     gridToCRS.getTranslateY());
        statement.setDouble(indexOf(query.byShearX),         gridToCRS.getShearX());
        statement.setDouble(indexOf(query.byShearY),         gridToCRS.getShearY());
        statement.setInt   (indexOf(query.byHorizontalSRID), horizontalSRID);

        String ID = null;
        int idIndex = indexOf(query.identifier);
        int vsIndex = indexOf(query.verticalSRID);
        int voIndex = indexOf(query.verticalOrdinates);
        ResultSet results = statement.executeQuery();
        while (results.next()) {
            final String nextID = results.getString(idIndex);
            final int  nextSRID = results.getInt   (vsIndex);
            if (results.wasNull() != (verticalOrdinates == null) ||
                (verticalOrdinates != null && nextSRID != verticalSRID))
            {
                /*
                 * NOTE: we check for vertical SRID in Java code rather than in the SQL statement
                 * because it is uneasy to write a statement that work for both non-null and null
                 * values (the former requires "? IS NULL" since the "? = NULL" statement doesn't
                 * work with PostgreSQL 8.2.
                 */
                continue;
            }
            final double[] altitudes = asDoubleArray(results.getArray(voIndex));
            if (!Arrays.equals(altitudes, verticalOrdinates)) {
                /*
                 * NOTE:  we compare the array in this Java code rather than inserting it as a
                 * parameter in the WHERE clause in order to make sure that we are insensitive
                 * to the array type, since we convert to double[] in all cases.
                 */
                continue;
            }
            if (ID!=null && !ID.equals(nextID)) {
                // Could happen if there is insuffisient conditions in the WHERE clause.
                final LogRecord record = Resources.getResources(getDatabase().getLocale()).
                        getLogRecord(Level.WARNING, ResourceKeys.ERROR_DUPLICATED_GEOMETRY_$1, nextID);
                record.setSourceClassName("GridGeometryTable");
                record.setSourceMethodName("getIdentifier");
                LOGGER.log(record);
            } else {
                ID = nextID;
            }
        }
        results.close();
        if (ID != null || newIdentifier == null) {
            return ID;
        }
        /*
         * No match found. Adds a new record in the database.
         */
        ID = searchFreeIdentifier(newIdentifier);
        statement = getStatement(QueryType.INSERT);
        statement.setString(indexOf(query.identifier),     ID);
        statement.setInt   (indexOf(query.width),          size.width );
        statement.setInt   (indexOf(query.height),         size.height);
        statement.setDouble(indexOf(query.scaleX),         gridToCRS.getScaleX());
        statement.setDouble(indexOf(query.scaleY),         gridToCRS.getScaleY());
        statement.setDouble(indexOf(query.translateX),     gridToCRS.getTranslateX());
        statement.setDouble(indexOf(query.translateY),     gridToCRS.getTranslateY());
        statement.setDouble(indexOf(query.shearX),         gridToCRS.getShearX());
        statement.setDouble(indexOf(query.shearY),         gridToCRS.getShearY());
        statement.setInt   (indexOf(query.horizontalSRID), horizontalSRID);
        vsIndex = indexOf(query.verticalSRID);
        voIndex = indexOf(query.verticalOrdinates);
        if (verticalOrdinates == null || verticalOrdinates.length != 0) {
            statement.setNull(vsIndex, Types.INTEGER);
            statement.setNull(voIndex, Types.ARRAY);
        } else {
            statement.setInt(vsIndex, verticalSRID);
            if (false) {
                // TODO: Enable this bloc when we will be allowed to compile for J2SE 1.6, and
                //       if the PostgreSQL JDBC driver implements the createArrayOf(...) method.
                final Double[] numbers = new Double[verticalOrdinates.length];
                for (int i=0; i<numbers.length; i++) {
                    numbers[i] = verticalOrdinates[i];
                }
                final Array array = null;//statement.getConnection().createArrayOf("float8", numbers);
                statement.setArray(voIndex, array);
            } else {
                final StringBuilder buffer = new StringBuilder();
                char separator = '{';
                for (int i=0; i<verticalOrdinates.length; i++) {
                    buffer.append(separator).append(verticalOrdinates[i]);
                    separator = ',';
                }
                final String array = buffer.append('}').toString();
                statement.setString(voIndex, array);
            }
        }
        insertSingleton(statement);
        return ID;
    }
}
