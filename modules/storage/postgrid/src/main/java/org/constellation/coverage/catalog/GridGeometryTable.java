/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2006, Institut de Recherche pour le Développement
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

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.geotoolkit.util.collection.CanonicalSet;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.operation.transform.AffineTransform2D;
import org.geotoolkit.referencing.factory.IdentifiedObjectFinder;
import org.geotools.referencing.factory.wkt.PostgisAuthorityFactory;

import org.constellation.catalog.CatalogException;
import org.constellation.catalog.IllegalRecordException;
import org.constellation.catalog.SingletonTable;
import org.constellation.catalog.Column;
import org.constellation.catalog.Database;
import org.constellation.catalog.QueryType;
import org.constellation.resources.i18n.Resources;
import org.constellation.resources.i18n.ResourceKeys;


/**
 * Connection to a table of grid geometries.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
final class GridGeometryTable extends SingletonTable<GridGeometryEntry> {
    /**
     * The authority factory connected to the PostGIS {@code "spatial_ref_sys"} table.
     * Will be created when first needed.
     */
    private transient PostgisAuthorityFactory crsFactory;

    /**
     * A map of CRS created up to date.
     */
    private transient Map<Integer,CoordinateReferenceSystem> cachedCRS;

    /**
     * A set of CRS created up to date. Cached because we will typically have many grid
     * geometries using the same set of CRS.
     */
    private transient CanonicalSet<SpatialRefSysEntry> gridCRS;

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
     * Returns the CRS authority factory backed by the PostGIS {@code "spatial_ref_sys"} table.
     *
     * @throws SQLException if an error occured while querying the database.
     */
    private PostgisAuthorityFactory getAuthorityFactory() throws SQLException {
        assert Thread.holdsLock(this);
        if (crsFactory == null) {
            crsFactory = new PostgisAuthorityFactory(null, getDatabase().getConnection());
        }
        return crsFactory;
    }

    /**
     * Returns a CRS for the specified code from the {@code "spatial_ref_sys"} table.
     * This method does <strong>not</strong> look in other CRS databases like what
     * {@link org.geotoolkit.referencing.CRS#decode(String)} does.
     *
     * @param  code The CRS identifier.
     * @return The coordinate reference system for the given code.
     * @throws SQLException if an error occured while querying the database.
     * @throws FactoryException if the CRS was not found or can not be created.
     */
    public synchronized CoordinateReferenceSystem getSpatialReferenceSystem(final String code)
            throws SQLException, FactoryException
    {
        return getSpatialReferenceSystem(getAuthorityFactory().getPrimaryKey(code));
    }

    /**
     * Returns a CRS for the specified identifier. The given identifier should be a primary
     * key in the PostGIS {@code "spatial_ref_sys"} table.
     *
     * @param  srid The CRS identifier.
     * @return The coordinate reference system for the given identifier.
     * @throws SQLException if an error occured while querying the database.
     * @throws FactoryException if the CRS was not found or can not be created.
     */
    public synchronized CoordinateReferenceSystem getSpatialReferenceSystem(final int srid)
            throws SQLException, FactoryException
    {
        final Integer key = srid;
        if (cachedCRS == null) {
            cachedCRS = new HashMap<Integer,CoordinateReferenceSystem>();
        }
        CoordinateReferenceSystem crs = cachedCRS.get(key);
        if (crs == null) {
            crs = getAuthorityFactory().createCoordinateReferenceSystem(key.toString());
            cachedCRS.put(key, crs);
        }
        return crs;
    }

    /**
     * Returns a CRS identifier for the specified WKT. The given WKT should appears in the PostGIS
     * {@code "spatial_ref_sys"} table. The returned value is a primary key in the same table.
     *
     * @param  wkt The WKT of the CRS to search.
     * @return The identifier for the given CRS, or 0 if none.
     * @throws FactoryException if the CRS was not found or can not be created.
     */
    public synchronized int getSRID(final String wkt) throws SQLException, FactoryException {
        final CoordinateReferenceSystem crs = CRS.parseWKT(wkt);
        final PostgisAuthorityFactory factory = getAuthorityFactory();
        final IdentifiedObjectFinder finder = factory.getIdentifiedObjectFinder(CoordinateReferenceSystem.class);
        String srid = finder.findIdentifier(crs);
        if (srid == null) {
            return 0;
        }
        return factory.getPrimaryKey(srid);
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
        final int    verticalSRID      = results.getInt   (indexOf(query.verticalSRID));
        final Array  verticalOrdinates = results.getArray (indexOf(query.verticalOrdinates));
        /*
         * Creates the SpatialRefSysEntry object, looking for an existing one in the cache first.
         * If a new object has been created, it will be completed after insertion in the cache.
         */
        SpatialRefSysEntry srsEntry = new SpatialRefSysEntry(horizontalSRID, verticalSRID,
                CRS.getTemporalCRS(getDatabase().getCoordinateReferenceSystem()));
        assert Thread.holdsLock(this);
        Column column = query.identifier;
        if (gridCRS == null) {
            gridCRS = CanonicalSet.newInstance(SpatialRefSysEntry.class);
        }
        final SpatialRefSysEntry candidate = gridCRS.unique(srsEntry);
        if (candidate != srsEntry) {
            srsEntry = candidate;
        } else try {
            column = query.horizontalSRID;
            srsEntry.createSingleCRS(this, true, false);
            column = query.verticalSRID;
            srsEntry.createSingleCRS(this, false, true);
            column = query.identifier;
            srsEntry.createCompoundCRS(getAuthorityFactory().getCRSFactory());
        } catch (FactoryException exception) {
            throw new IllegalRecordException(exception, this, results, indexOf(column), identifier);
        } catch (ClassCastException exception) {
            throw new IllegalRecordException(exception, this, results, indexOf(column), identifier);
        }
        final double[] altitudes = asDoubleArray(verticalOrdinates);
        final AffineTransform2D at = new AffineTransform2D(scaleX, shearY, shearX, scaleY, translateX, translateY);
        final GridGeometryEntry entry;
        try {
            entry = new GridGeometryEntry(identifier, new Dimension(width, height), srsEntry, at, altitudes);
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) { // We want to catch only the checked exceptions here.
            throw new IllegalRecordException(exception, this, results, indexOf(column), identifier);
        }
        if (entry.isEmpty()) {
            throw new IllegalRecordException("The geographic envelope is empty.", // TODO: localize
                    this, results, indexOf(column), identifier);
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
            final Number[] asNumbers = (data instanceof Number[]) ? (Number[]) data : null;
            for (int i=0; i<length; i++) {
                final double z;
                if (asNumbers != null) {
                    z = asNumbers[i].doubleValue();
                } else {
                    z = getDouble(data, i);
                }
                altitudes[i] = z;
            }
//          altitudes.free(); // TODO: uncomment when we will be allowed to use Java 6.
        } else {
            altitudes = null;
        }
        return altitudes;
    }

    /**
     * Returns {@code true} if the specified arrays are equal when comparing the values
     * at {@code float} precision. This method is a workaround for the cases where some
     * original array was stored with {@code double} precision while the other array has
     * been casted to {@code float} precision. The precision lost cause the comparaison
     * to fails when comparing the array at full {@code double} precision. For example
     * {@code (double) 0.1f} is not equals to {@code 0.1}.
     */
    private static boolean equalsAsFloat(final double[] a1, final double[] a2) {
        if (a1 == null || a2 == null || a1.length != a2.length) {
            return false;
        }
        for (int i=0; i<a1.length; i++) {
            if (Float.floatToIntBits((float) a1[i]) != Float.floatToIntBits((float) a2[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * For every values in the specified map, replaces the collection of {@link GridGeometryEntry}
     * identifiers by a set of altitudes. On input, the values are usually {@code List<String>}.
     * On output, all values will be {@code SortedSet<Number>}.
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
        ensureNonNull("size",      size);
        ensureNonNull("gridToCRS", gridToCRS);
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
        boolean strictlyEquals = false;
        int idIndex = indexOf(query.identifier);
        int vsIndex = indexOf(query.verticalSRID);
        int voIndex = indexOf(query.verticalOrdinates);
        ResultSet results = statement.executeQuery();
        while (results.next()) {
            final String nextID = results.getString(idIndex);
            final int  nextSRID = results.getInt   (vsIndex);
            /*
             * We check vertical SRID in Java code rather than in the SQL statement because it is
             * uneasy to write a statement that works for both non-null and null values (the former
             * requires "? IS NULL" since the "? = NULL" statement doesn't work with PostgreSQL 8.2.
             */
            if (results.wasNull() != (verticalOrdinates == null) ||
                (verticalOrdinates != null && nextSRID != verticalSRID))
            {
                continue;
            }
            /*
             * We compare the arrays in this Java code rather than in the SQL statement (in the
             * WHERE clause) in order to make sure that we are insensitive to the array type
             * (since we convert to double[] in all cases), and because we need to relax the
             * tolerance threshold in some cases.
             */
            final double[] altitudes = asDoubleArray(results.getArray(voIndex));
            final boolean strict;
            if (Arrays.equals(altitudes, verticalOrdinates)) {
                strict = true;
            } else if (equalsAsFloat(altitudes, verticalOrdinates)) {
                strict = false;
            } else {
                continue;
            }
            /*
             * If there is more than one record with different ID, then there is a choice:
             *   1) If the new record is more accurate than the previous one, keep the new one.
             *   2) Otherwise we keep the previous record. A warning will be logged if and only
             *      if the two records are strictly equals.
             */
            if (ID!=null && !ID.equals(nextID)) {
                if (!strict) {
                    continue;
                }
                if (strictlyEquals) {
                    // Could happen if there is insuffisient conditions in the WHERE clause.
                    final LogRecord record = Resources.getResources(getDatabase().getLocale()).
                            getLogRecord(Level.WARNING, ResourceKeys.ERROR_DUPLICATED_GEOMETRY_$1, nextID);
                    record.setSourceClassName("GridGeometryTable");
                    record.setSourceMethodName("getIdentifier");
                    LOGGER.log(record);
                    continue;
                }
            }
            ID = nextID;
            strictlyEquals = strict;
        }
        results.close();
        if (ID != null || newIdentifier == null) {
            return ID;
        }
        /*
         * No match found. Adds a new record in the database.
         */
        boolean success = false;
        transactionBegin();
        try {
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
            if (verticalOrdinates == null || verticalOrdinates.length == 0) {
                statement.setNull(vsIndex, Types.INTEGER);
                statement.setNull(voIndex, Types.ARRAY);
            } else {
                statement.setInt(vsIndex, verticalSRID);
                final Double[] numbers = new Double[verticalOrdinates.length];
                for (int i=0; i<numbers.length; i++) {
                    numbers[i] = verticalOrdinates[i];
                }
                // TODO: Use the following line instead when we will be allowed to compile for J2SE 1.6,
                //       and if the PostgreSQL JDBC driver implements the createArrayOf(...) method.
                //
                //       array = statement.getConnection().createArrayOf("float8", numbers);
                final Array array = new DoubleArray(numbers);
                statement.setArray(voIndex, array);
            }
            success = updateSingleton(statement);
            // 'success' must be assigned last in this try block.
        } finally {
            transactionEnd(success);
        }
        return ID;
    }

    /**
     * Invoked by a timer after this instance has been unused for a while.
     */
    @Override
    protected void notifySleeping() {
        if (crsFactory != null) {
            crsFactory.dispose(false);
        }
        crsFactory = null;
        super.notifySleeping();
    }

    /**
     * Clears the cache.
     */
    @Override
    public synchronized void flush() {
        if (cachedCRS != null) {
            cachedCRS.clear();
        }
        if (gridCRS != null) {
            gridCRS.clear();
        }
        super.flush();
    }
}
