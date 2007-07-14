/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
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
package net.sicade.observation.sql;

// J2SE dependencies
import java.util.Date;
import java.util.Calendar;
import java.sql.Timestamp;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import static java.lang.Math.min;
import static java.lang.Math.max;
import static java.lang.Double.doubleToLongBits;

// OpenGIS dependencies
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

// Geotools dependencies
import org.geotools.util.NumberRange;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import static org.geotools.referencing.CRS.equalsIgnoreMetadata;
import static org.geotools.referencing.CRS.findMathTransform;
import static org.geotools.referencing.CRS.transform;

// Sicade dependencies
import net.sicade.util.DateRange;
import net.sicade.observation.Element;
import net.sicade.observation.ServerException;
import net.sicade.observation.CatalogException;


/**
 * Base class for tables with a {@code getEntry(...)} method restricted to the elements
 * contained in some spatio-temporal bounding box. The bounding box is defined either by
 * an {@link #getEnvelope Envelope} expressed in this {@linkplain #getCoordinateReferenceSystem
 * table CRS}, or by a combinaison of {@link #getGeographicBoundingBox GeographicBoundingBox},
 * {@link #getVerticalRange VerticalRange} and {@link #getTimeRange TimeRange} expressed in
 * standard CRS.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class BoundedSingletonTable<E extends Element> extends SingletonTable<E> {
    /**
     * The upper limit for the default envelope temporal component,
     * as milliseconds after the current time.
     */
    private static final long LOOK_AHEAD = 30 * 24 * 60 * 60 * 1000L;

    /**
     * The type of the CRS used.
     */
    private final CRS crsType;

    /**
     * The envelope time component, in milliseconds since January 1st, 1970.
     */
    private long tMin, tMax;

    /**
     * The envelope spatial component. The longitude range may be larger than needed
     * (±360° instead of ±180°) because we don't know in advance if the longitudes
     * are inside the [-180 .. +180°} range or the [0 .. 360°] range.
     */
    private double xMin, xMax, yMin, yMax, zMin, zMax;

    /**
     * {@code true} if the {@link #ensureTrimmed} method already shrinked the
     * {@linkplain #getEnvelope spatio-temporal envelope} for this table.
     */
    private boolean trimmed;

    /**
     * {@code true} if the user invoked {@link #trimEnvelope}. In such case, {@link #ensureTrimmed}
     * will need to shrink the {@linkplain #getEnvelope spatio-temporal envelope} next time it will
     * be invoked.
     */
    private boolean trimRequested;

    /**
     * The transform from the reference system designated by {@link #crsType} to
     * {@link #getCoordinateReferenceSystem}. Will be created only when first needed.
     */
    private transient MathTransform standardToUser;

    /**
     * Creates a new table connected to the specified database. Subclass constructors should
     * add {@link Column} and {@code Parameter} instances to their {@linkplain #query query}.
     * See {@linkplain Table class javadoc}.
     *
     * @param database The database that contains this table.
     * @param crsType  The type of the {@linkplain #getEnvelope envelope} coordinate reference system.
     */
    protected BoundedSingletonTable(final Database database, final CRS crsType) {
        super(database);
        this.crsType = crsType;
        tMin =  0;
        tMax =  System.currentTimeMillis() + LOOK_AHEAD;
        xMin = -360;
        xMax = +360;
        yMin =  -90;
        yMax =  +90;
        zMin = Double.NEGATIVE_INFINITY;
        zMax = Double.POSITIVE_INFINITY;
    }

    /**
     * Creates a new table connected to the same {@linkplain #database database} and using
     * the same {@linkplain #query query} than the specified table. Subclass constructors
     * should <strong>not</strong> modify the query.
     * <p>
     * In addition, the new table is initialized to the same spatio-temporal envelope than the
     * specified table. This constructor assumes that the {@linkplain #getCoordinateReferenceSystem
     * coordinate reference system} stay the same for the two tables.
     */
    protected BoundedSingletonTable(final BoundedSingletonTable<E> table) {
        super(table);
        crsType        = table.crsType;
        tMin           = table.tMin;
        tMax           = table.tMax;
        xMin           = table.xMin;
        xMax           = table.xMax;
        yMin           = table.yMin;
        yMax           = table.yMax;
        zMin           = table.zMin;
        zMax           = table.zMax;
        trimmed        = table.trimmed;
        trimRequested  = table.trimRequested;
        standardToUser = table.standardToUser;
    }

    /**
     * Returns the coordinate reference system used by {@code [get|set]Envelope} methods. The default
     * implementation returns a CRS inferred from the {@code crsType} argument given to the constructor.
     * This CRS may contain the following dimensions:
     * <p>
     * <ul>
     *   <li>The longitude in decimal degrees relative to Greenwich meridian.</li>
     *   <li>The latitude in decimal degrees.</li>
     *   <li>Altitude in metres above the WGS 84 ellipsoid.</li>
     *   <li>Time in fractional days since epoch.</li>
     * </ul>
     * <p>
     * Not all those dimensions need to be present. For example {@link CRS#XYT}
     * does not contain an altitude axis.
     * <p>
     * Subclasses can override this method in order to return a different CRS, but the CRS must
     * be compatible with the value given to the constructor, i.e. a transform must exists between
     * them.
     *
     * @see CRS
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crsType.getCoordinateReferenceSystem();
    }

    /**
     * Retourne la transformation allant du système de référence des coordonnées {@link #crsType}
     * vers le système {@link #getCoordinateReferenceSystem}, ou {@code null} si cette
     * transformation est la transformation identitée.
     *
     * @throws CatalogException si la transformation n'a pas pu être construite.
     */
    private MathTransform getStandardToUser() throws CatalogException {
        assert Thread.holdsLock(this);
        if (standardToUser == null) {
            final CoordinateReferenceSystem sourceCRS = crsType.getCoordinateReferenceSystem();
            final CoordinateReferenceSystem targetCRS =    this.getCoordinateReferenceSystem();
            try {
                standardToUser = findMathTransform(sourceCRS, targetCRS);
            } catch (FactoryException exception) {
                throw new ServerException(exception);
            }
        }
        return standardToUser;
    }

    /**
     * Returns the spatio-temporal envelope of the elements to be read by this table. The
     * {@linkplain Envelope#getCoordinateReferenceSystem envelope CRS} is the one returned
     * by {@link #getCoordinateReferenceSystem}.
     * <p>
     * The default implementation creates an envelope from the informations returned by
     * {@link #getGeographicBoundingBox}, {@link #getVerticalRange} and {@link #getTimeRange},
     * applying a coordinate transformation if needed.
     *
     * @throws CatalogException if the envelope can not be obtained or an error occured
     *         during the transformation.
     *
     * @see #getGeographicBoundingBox
     * @see #getVerticalRange
     * @see #getTimeRange
     * @see #trimEnvelope
     */
    public synchronized Envelope getEnvelope() throws CatalogException {
        final GeographicBoundingBox box = getGeographicBoundingBox();
        final NumberRange      altitude = getVerticalRange();
        final DateRange            time = getTimeRange();
        GeneralEnvelope envelope = new GeneralEnvelope(crsType.getCoordinateReferenceSystem());
        if (crsType.xdim >= 0) {
            envelope.setRange(crsType.xdim, box.getWestBoundLongitude(), box.getEastBoundLongitude());
        }
        if (crsType.ydim >= 0) {
            envelope.setRange(crsType.ydim, box.getSouthBoundLatitude(), box.getNorthBoundLatitude());
        }
        if (crsType.zdim >= 0) {
            envelope.setRange(crsType.zdim, altitude.getMinimum(), altitude.getMaximum());
        }
        if (crsType.tdim >= 0) {
            envelope.setRange(crsType.tdim, CRS.TEMPORAL.toValue((Date) time.getMinValue()),
                                            CRS.TEMPORAL.toValue((Date) time.getMaxValue()));
        }
        final MathTransform standardToUser = getStandardToUser();
        if (!standardToUser.isIdentity()) try {
            envelope = transform(standardToUser, envelope);
        } catch (TransformException exception) {
            throw new ServerException(exception);
        }
        return envelope;
    }

    /**
     * Sets the spatio-temporal envelope of the elements to be read by this table.
     * Any element intercepting this envelope will be considered by next calls to
     * {@link #getEntries}.
     * <p>
     * The default implementation delegates to {@link #setGeographicBoundingBox},
     * {@link #setVerticalRange} and {@link #setTimeRange}, applying a coordinate
     * transformation if needed.
     *
     * @param  envelope The envelope.
     * @return {@code true} if the envelope changed as a result of this call, or
     *         {@code false} if the specified envelope is equals to the one already set.
     * @throws CatalogException if an error occured during the transformation or
     *         the envelope can not be set.
     */
    public synchronized boolean setEnvelope(Envelope envelope) throws CatalogException {
        final MathTransform userToStandard;
        final CoordinateReferenceSystem crs = envelope.getCoordinateReferenceSystem();
        try {
            if (crs == null || equalsIgnoreMetadata(crs, getCoordinateReferenceSystem())) {
                userToStandard = getStandardToUser().inverse();
            } else {
                userToStandard = findMathTransform(crs, crsType.getCoordinateReferenceSystem());
            }
            if (!userToStandard.isIdentity()) {
                envelope = transform(userToStandard, envelope);
            }
        } catch (FactoryException exception) {
            throw new ServerException(exception);
        } catch (TransformException exception) {
            throw new ServerException(exception);
        }
        boolean changed = false;
        if (crsType.xdim >= 0 && crsType.ydim >= 0) {
            changed |= setGeographicBoundingBox(new GeographicBoundingBoxImpl(
                    envelope.getMinimum(crsType.xdim),
                    envelope.getMaximum(crsType.xdim),
                    envelope.getMinimum(crsType.ydim),
                    envelope.getMaximum(crsType.ydim)));
        }
        if (crsType.zdim >= 0) {
            changed |= setVerticalRange(envelope.getMinimum(crsType.zdim),
                                        envelope.getMaximum(crsType.zdim));
        }
        if (crsType.tdim >= 0) {
            changed |= setTimeRange(CRS.TEMPORAL.toDate(envelope.getMinimum(crsType.tdim)),
                                    CRS.TEMPORAL.toDate(envelope.getMaximum(crsType.tdim)));
        }
        return changed;
    }

    /**
     * Returns the geographic bounding box of the elements to be read by this table.
     * This bounding box will not be greater than the box specified at the last call
     * to {@link #setGeographicBoundingBox setGeographicBoundingBox(...)}, but it may
     * be smaller if {@link #trimEnvelope} has been invoked.
     *
     * @return The bounding box of the elements to be read.
     * @throws CatalogException if the bounding box can not be obtained.
     *
     * @see #getVerticalRange
     * @see #getTimeRange
     * @see #getEnvelope
     * @see #trimEnvelope
     */
    public synchronized GeographicBoundingBox getGeographicBoundingBox() throws CatalogException {
        try {
            ensureTrimmed(QueryType.BOUNDING_BOX);
        } catch (SQLException e) {
            throw new ServerException(e);
        }
        return new GeographicBoundingBoxImpl(xMin, xMax, yMin, yMax);
    }

    /**
     * Sets the geographic bounding box of the elements to be read by this table.
     * Coordinates must be in degrees of longitude and latitude on the WGS&nbsp;1984
     * ellipsoid.
     *
     * @param  area The geographic bounding box in in degrees of longitude and latitude.
     * @return {@code true} if the bounding box changed as a result of this call, or
     *         {@code false} if the specified box is equals to the one already set.
     */
    public synchronized boolean setGeographicBoundingBox(final GeographicBoundingBox area) {
        boolean change;
        change  = (xMin != (xMin = area.getWestBoundLongitude()));
        change |= (xMax != (xMax = area.getEastBoundLongitude()));
        change |= (yMin != (yMin = area.getSouthBoundLatitude()));
        change |= (yMax != (yMax = area.getNorthBoundLatitude()));
        trimRequested = false;
        if (change) {
            trimmed = false;
            fireStateChanged("GeographicBoundingBox");
        }
        return change;
    }

    /**
     * Returns the vertical range of the elements to be read by this table.
     * This vertical range will not be greater than the box specified at the last call
     * to {@link #setVerticalRange setVerticalRange(...)}, but it may be smaller if
     * {@link #trimEnvelope} has been invoked.
     *
     * @return The vertical range of the elements to be read.
     * @throws CatalogException if the vertical range can not be obtained.
     *
     * @see #getGeographicBoundingBox
     * @see #getTimeRange
     * @see #getEnvelope
     * @see #trimEnvelope
     */
    public synchronized NumberRange getVerticalRange() throws CatalogException {
        try {
            ensureTrimmed(QueryType.BOUNDING_BOX);
        } catch (SQLException e) {
            throw new ServerException(e);
        }
        return new NumberRange(zMin, zMax);
    }

    /**
     * Sets the vertical range of the elements to be read by this table.
     * The range should be specified in metres above the WGS&nbsp;1984 ellipsoid.
     *
     * @param  range The vertical range.
     * @return {@code true} if the vertical range changed as a result of this call, or
     *         {@code false} if the specified range is equals to the one already set.
     */
    public final boolean setVerticalRange(final NumberRange range) {
        return setVerticalRange(range.getMinimum(true), range.getMaximum(true));
    }

    /**
     * Sets the vertical range of the elements to be read by this table.
     * The range should be specified in metres above the WGS&nbsp;1984 ellipsoid.
     *
     * @param  minimum The minimal <var>z</var> value.
     * @param  maximum The maximal <var>z</var> value.
     * @return {@code true} if the vertical range changed as a result of this call, or
     *         {@code false} if the specified range is equals to the one already set.
     */
    public synchronized boolean setVerticalRange(final double minimum, final double maximum) {
        boolean change;
        change  = (doubleToLongBits(zMin) != doubleToLongBits(zMin = minimum));
        change |= (doubleToLongBits(zMax) != doubleToLongBits(zMax = maximum));
        trimRequested = false;
        if (change) {
            trimmed = false;
            fireStateChanged("VerticalRange");
        }
        return change;
    }

    /**
     * Returns the time range of the elements to be read by this table.
     * This time range will not be greater than the box specified at the last call
     * to {@link #setTimeRange setTimeRange(...)}, but it may be smaller if
     * {@link #trimEnvelope} has been invoked.
     *
     * @return The time range of the elements to be read.
     * @throws CatalogException if the time range can not be obtained.
     *
     * @see #getGeographicBoundingBox
     * @see #getVerticalRange
     * @see #getEnvelope
     * @see #trimEnvelope
     */
    public synchronized DateRange getTimeRange() throws CatalogException {
        try {
            ensureTrimmed(QueryType.BOUNDING_BOX);
        } catch (SQLException e) {
            throw new ServerException(e);
        }
        return new DateRange(new Date(tMin), new Date(tMax));
    }

    /**
     * Sets the time range of the elements to be read by this table.
     *
     * @param  timeRange The time range.
     * @return {@code true} if the time range changed as a result of this call, or
     *         {@code false} if the specified range is equals to the one already set.
     */
    public final boolean setTimeRange(final DateRange timeRange) {
        Date startTime = timeRange.getMinValue();
        Date   endTime = timeRange.getMaxValue();
        if (!timeRange.isMinIncluded()) {
            startTime = new Date(startTime.getTime()+1);
        }
        if (!timeRange.isMaxIncluded()) {
            endTime = new Date(endTime.getTime()-1);
        }
        return setTimeRange(startTime, endTime);
    }

    /**
     * Sets the time range of the elements to be read by this table.
     *
     * @param  startTime The start time, inclusive.
     * @param  endTime   The end time, <strong>inclusive</strong>.
     * @return {@code true} if the time range changed as a result of this call, or
     *         {@code false} if the specified range is equals to the one already set.
     */
    public synchronized boolean setTimeRange(final Date startTime, final Date endTime) {
        boolean change;
        change  = (tMin != (tMin = startTime.getTime()));
        change |= (tMax != (tMax =   endTime.getTime()));
        trimRequested = false;
        if (change) {
            trimmed = false;
            fireStateChanged("TimeRange");
        }
        return change;
    }

    /**
     * Shrinks the {@linkplain #getEnvelope spatio-temporal envelope} to a smaller envelope
     * containing all the elements to be returned by this table.  This method iterates over
     * the elements that intercept the envelope specified by {@code setXXX(...)} methods.
     * Then the envelope is altered in such a way that the {@code getXXX(...)} method returns
     * an identical or smaller envelope intercepting the same set of elements.
     */
    public synchronized void trimEnvelope() {
        trimRequested = true;
    }

    /**
     * Process to envelope shrinking, if not already done. This method is invoked when needed by
     * {@link #getGeographicBoundingBox}, {@link #getVerticalRange} and {@link #getTimeRange}. A
     * shrinking is performed only if explicitly requested by a call to {@link #trimEnvelope}.
     *
     * @throws SQLException if an error occured while reading the database.
     */
    private void ensureTrimmed(final QueryType type) throws SQLException {
        assert Thread.holdsLock(this);
        if (trimRequested && !trimmed) {
            final PreparedStatement statement = getStatement(type);
            if (statement != null) {
                final int timeParameter = query.indexOfParameter(type, Role.TIME_RANGE);
                final int bboxParameter = query.indexOfParameter(type, Role.SPATIAL_ENVELOPE);
                final Column bboxColumn = query.getColumns(type).get(bboxParameter);
                final ResultSet result = statement.executeQuery();
                while (result.next()) { // Should contains only one record.
                    Date time;
                    final Calendar calendar = getCalendar();
                    time = result.getTimestamp(timeParameter, calendar);
                    if (time != null) {
                        tMin = max(tMin, time.getTime());
                    }
                    time = result.getTimestamp(timeParameter + 1, calendar);
                    if (time != null) {
                        tMax = min(tMax, time.getTime());
                    }
                    final Envelope envelope; // Defined in this context as always (x,y,z)
                    if (bboxColumn instanceof SpatialColumn.Box) {
                        envelope = ((SpatialColumn.Box) bboxColumn).getEnvelope(result, type);
                    } else {
                        envelope = SpatialColumn.Box.getEnvelope(result, bboxParameter);
                    }
                    final int dimension = envelope.getDimension();
                    for (int i=0; i<dimension; i++) {
                        final double min = envelope.getMinimum(i);
                        final double max = envelope.getMaximum(i);
                        switch (i) {
                            case 0: if (min > xMin) xMin = min;
                                    if (max < xMax) xMax = max; break;
                            case 1: if (min > yMin) yMin = min;
                                    if (max < yMax) yMax = max; break;
                            case 2: if (min > zMin) zMin = min;
                                    if (max < zMax) zMax = max; break;
                            default: break; // Ignore extra dimensions, if any.
                        }
                    }
                }
                result.close();
                fireStateChanged("Envelope");
            }
            trimmed = true;
        }
    }

    /**
     * Configure la requête SQL spécifiée en fonction des limites spatio-temporelles définies
     * dans cette table. Cette méthode est appelée automatiquement lorsque cette table a
     * {@linkplain #fireStateChanged changé d'état}.
     */
    @Override
    protected void configure(final QueryType type, final PreparedStatement statement) throws SQLException {
        super.configure(type, statement);
        int index = query.indexOfParameter(type, Role.TIME_RANGE);
        if (index != 0) {
            final Calendar calendar = getCalendar();
            statement.setTimestamp(index,     new Timestamp(tMin), calendar);
            statement.setTimestamp(index + 1, new Timestamp(tMax), calendar);
        }
        index = query.indexOfParameter(type, Role.SPATIAL_ENVELOPE);
        // TODO
    }

    /**
     * Notifies that this table state changed.
     */
    @Override
    protected void fireStateChanged(final String property) {
        super.fireStateChanged(property);
        if ("CoordinateReferenceSystem".equalsIgnoreCase(property)) {
            standardToUser = null;
        }
    }
}
