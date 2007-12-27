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
package net.seagis.catalog;

import java.util.Date;
import java.util.Calendar;
import java.sql.Timestamp;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.awt.geom.Dimension2D;
import static java.lang.Math.min;
import static java.lang.Math.max;
import static java.lang.Double.doubleToLongBits;

import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.geotools.util.DateRange;
import org.geotools.util.NumberRange;
import org.geotools.resources.Utilities;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.resources.geometry.XDimension2D;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import static org.geotools.referencing.CRS.getCoordinateOperationFactory;
import static org.geotools.referencing.CRS.equalsIgnoreMetadata;
import static org.geotools.referencing.CRS.transform;


/**
 * Base class for tables with a {@code getEntry(...)} method restricted to the elements
 * contained in some spatio-temporal bounding box. The bounding box is defined either by
 * an {@link #getEnvelope Envelope} expressed in this {@linkplain #getCoordinateReferenceSystem
 * table CRS}, or by a combinaison of {@link #getGeographicBoundingBox GeographicBoundingBox},
 * {@link #getVerticalRange VerticalRange} and {@link #getTimeRange TimeRange} expressed in
 * standard CRS.
 * <p>
 * Subclasses should invoke {@link #setExtentParameters} in their constructor.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class BoundedSingletonTable<E extends Element> extends SingletonTable<E> {
    /**
     * The default start time, in milliseconds since January 1st, 1970.
     */
    private static final long DEFAULT_START_TIME = 0;

    /**
     * The default end time, in milliseconds after current time.
     * The current value is 30 days.
     */
    private static final long DEFAULT_END_TIME = 30 * (24 * 60 * 60 * 1000L);

    /**
     * The parameter to use for looking an element by time range, or {@code null} if unset.
     *
     * @see #setExtentParameters
     */
    private Parameter byTimeRange;

    /**
     * The parameter to use for looking an element by spatial extent, or {@code null} if unset.
     *
     * @see #setExtentParameters
     */
    private Parameter bySpatialExtent;

    /**
     * The type of the CRS used for the {@linkplain #getEnvelope envelope}.
     */
    private final CRS crsType;

    /**
     * The transform from the reference system designated by {@link #crsType} to
     * {@link #getCoordinateReferenceSystem}. Will be created only when first needed.
     * If non-null, then the source CRS must be {@link CRS#getCoordinateReferenceSystem}
     * and the target CRS is the one returned by {@link #getCoordinateReferenceSystem}.
     */
    private CoordinateOperation standardToUser;

    /**
     * The bounding box computed by {@link #getEnvelope}, or {@code null}
     * if not yet computed. Cached for performance reasons.
     */
    private transient GeneralEnvelope envelope;

    /**
     * The envelope time component, in milliseconds since January 1st, 1970.
     * May be {@link Long#MIN_VALUE} or {@link Long#MAX_VALUE} if unbounded.
     */
    private long tMin, tMax;

    /**
     * The envelope spatial component in WGS84 coordinates. The longitude range may be larger
     * than needed (±360° instead of ±180°) because we don't know in advance if the longitudes
     * are inside the [-180 .. +180°} range or the [0 .. 360°] range.
     */
    private double xMin, xMax, yMin, yMax, zMin, zMax;

    /**
     * The preferred resolution along the <var>x</var> and <var>y</var> axis. Units shall be
     * degrees of longitude or latitude, as in the geographic bounding box. This information
     * is only approximative; there is no garantee that an image to be read will have that
     * resolution. A null value (zero) means that the best resolution should be used.
     */
    private float xResolution, yResolution;

    /**
     * {@code true} if the {@link #trimEnvelope} method already shrinked the
     * {@linkplain #getEnvelope spatio-temporal envelope} for this table.
     */
    private boolean trimmed;

    /**
     * Creates a new table using the specified query. The query given in argument should be some
     * subclass with {@link Query#addColumn addColumn} and {@link Query#addParameter addParameter}
     * methods invoked in its constructor.
     */
    protected BoundedSingletonTable(final Query query, final CRS crsType) {
        super(query);
        this.crsType = crsType;
        tMin =  Long.MIN_VALUE;
        tMax =  Long.MAX_VALUE;
        xMin = -360;
        xMax = +360;
        yMin =  -90;
        yMax =  +90;
        zMin = Double.NEGATIVE_INFINITY;
        zMax = Double.POSITIVE_INFINITY;
    }

    /**
     * Creates a new table connected to the same {@linkplain #getDatabase database} and using
     * the same {@linkplain #query query} than the specified table. Subclass constructors should
     * not modify the query, since it is shared.
     * <p>
     * In addition, the new table is initialized to the same spatio-temporal envelope and the
     * same {@linkplain #getCoordinateReferenceSystem coordinate reference system} than the
     * specified table.
     */
    protected BoundedSingletonTable(final BoundedSingletonTable<E> table) {
        super(table);
        crsType         = table.crsType;
        envelope        = table.envelope;
        tMin            = table.tMin;
        tMax            = table.tMax;
        xMin            = table.xMin;
        xMax            = table.xMax;
        yMin            = table.yMin;
        yMax            = table.yMax;
        zMin            = table.zMin;
        zMax            = table.zMax;
        xResolution     = table.xResolution;
        yResolution     = table.yResolution;
        trimmed         = table.trimmed;
        standardToUser  = table.standardToUser;
        byTimeRange     = table.byTimeRange;
        bySpatialExtent = table.bySpatialExtent;
    }

    /**
     * Sets the parameter to use for looking an element by extent. This information is
     * usually specified at construction time.
     *
     * @param  byTimeRange The parameter for looking an element by time range, or {@code null} if none.
     * @param  bySpatialExtent The parameter for looking an element by spatial extent, or {@code null} if none.
     */
    protected synchronized void setExtentParameters(final Parameter byTimeRange, final Parameter bySpatialExtent) {
        if (!Utilities.equals(this.byTimeRange, byTimeRange) || !Utilities.equals(this.bySpatialExtent, bySpatialExtent)) {
            this.byTimeRange     = byTimeRange;
            this.bySpatialExtent = bySpatialExtent;
            flush();
            fireStateChanged("extentParameters");
        }
    }

    /**
     * Returns the coordinate reference system used by {@code [get|set]Envelope} methods. The
     * default CRS is inferred from the {@code crsType} argument given to the constructor. This
     * CRS may contain the following dimensions:
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
     *
     * @see CRS
     */
    public final CoordinateReferenceSystem getCoordinateReferenceSystem() {
        final CoordinateOperation standardToUser = this.standardToUser; // Protect from change.
        return (standardToUser != null) ? standardToUser.getTargetCRS() : crsType.getCoordinateReferenceSystem();
    }

    /**
     * Sets the coordinate reference system used by {@code [get|set]Envelope} methods.
     *
     * @param  crs The new CRS, or {@code null} for restoring the default one.
     * @throws CatalogException if the specified CRS is not compatible with the CRS type
     *         given to the constructor.
     */
    public synchronized void setCoordinateReferenceSystem(CoordinateReferenceSystem crs)
            throws CatalogException
    {
        final CoordinateReferenceSystem sourceCRS = crsType.getCoordinateReferenceSystem();
        if (crs == null || crs.equals(sourceCRS)) {
            standardToUser = null;
            envelope = null;
            fireStateChanged("CoordinateReferenceSystem");
            return;
        }
        final CoordinateOperationFactory factory = getCoordinateOperationFactory(true);
        final CoordinateOperation candidate;
        try {
            candidate = factory.createOperation(sourceCRS, crs);
        } catch (FactoryException exception) {
            throw new ServerException(exception);
        }
        if (!candidate.equals(standardToUser)) {
            standardToUser = candidate;
            envelope = null;
            fireStateChanged("CoordinateReferenceSystem");
        }
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
     * @throws CatalogException if a logical error occured.
     * @throws SQLException if an error occured while reading the database.
     *
     * @see #getGeographicBoundingBox
     * @see #getVerticalRange
     * @see #getTimeRange
     * @see #trimEnvelope
     */
    public synchronized Envelope getEnvelope() throws CatalogException, SQLException {
        if (envelope != null) {
            return envelope.clone();
        }
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
            final Date startTime = time.getMinValue();
            final Date   endTime = time.getMaxValue();
            envelope.setRange(crsType.tdim,
                    startTime!=null ? CRS.TEMPORAL.toValue(startTime) : Double.NEGATIVE_INFINITY,
                      endTime!=null ? CRS.TEMPORAL.toValue(  endTime) : Double.POSITIVE_INFINITY);
        }
        if (standardToUser != null) try {
            envelope = transform(standardToUser, envelope);
        } catch (TransformException exception) {
            throw new ServerException(exception);
        }
        this.envelope = envelope;
        return envelope.clone();
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
     * @param  envelope The envelope, or {@code null} to reset full coverage.
     * @return {@code true} if the envelope changed as a result of this call, or
     *         {@code false} if the specified envelope is equals to the one already set.
     * @throws CatalogException if an error occured during the transformation or
     *         the envelope can not be set.
     */
    public synchronized boolean setEnvelope(Envelope envelope) throws CatalogException {
        if (envelope == null) {
            boolean changed;
            changed  = setGeographicBoundingBox(null);
            changed |= setVerticalRange        (null);
            changed |= setTimeRange            (null);
            return changed;
        }
        final CoordinateReferenceSystem sourceCRS = envelope.getCoordinateReferenceSystem();
        if (sourceCRS != null) {
            final CoordinateReferenceSystem targetCRS = getCoordinateReferenceSystem();
            if (!equalsIgnoreMetadata(sourceCRS, targetCRS)) {
                final CoordinateOperationFactory factory = getCoordinateOperationFactory(true);
                final CoordinateOperation userToStandard;
                try {
                    userToStandard = factory.createOperation(sourceCRS, targetCRS);
                    envelope = transform(userToStandard, envelope);
                } catch (FactoryException exception) {
                    throw new ServerException(exception);
                } catch (TransformException exception) {
                    throw new ServerException(exception);
                }
            }
        }
        boolean changed = false;
        if (crsType.xdim >= 0 && crsType.ydim >= 0) {
            final GeographicBoundingBox bbox = new GeographicBoundingBoxImpl(
                    envelope.getMinimum(crsType.xdim),
                    envelope.getMaximum(crsType.xdim),
                    envelope.getMinimum(crsType.ydim),
                    envelope.getMaximum(crsType.ydim));
            changed |= setGeographicBoundingBox(bbox);
        }
        if (crsType.zdim >= 0) {
            final double minimum = envelope.getMinimum(crsType.zdim);
            final double maximum = envelope.getMaximum(crsType.zdim);
            changed |= setVerticalRange(minimum, maximum);
        }
        if (crsType.tdim >= 0) {
            final Date minimum = CRS.TEMPORAL.toDate(envelope.getMinimum(crsType.tdim));
            final Date maximum = CRS.TEMPORAL.toDate(envelope.getMaximum(crsType.tdim));
            changed |= setTimeRange(minimum, maximum);
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
     *
     * @see #getVerticalRange
     * @see #getTimeRange
     * @see #getEnvelope
     * @see #trimEnvelope
     */
    public synchronized GeographicBoundingBox getGeographicBoundingBox() {
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
        change  = (xMin != (xMin = area!=null ? Math.max(area.getWestBoundLongitude(), -360) : -360));
        change |= (xMax != (xMax = area!=null ? Math.min(area.getEastBoundLongitude(), +360) : +360));
        change |= (yMin != (yMin = area!=null ? Math.max(area.getSouthBoundLatitude(),  -90) :  -90));
        change |= (yMax != (yMax = area!=null ? Math.min(area.getNorthBoundLatitude(),  +90) :  +90));
        if (change) {
            envelope = null;
            trimmed = false;
            fireStateChanged("GeographicBoundingBox");
            // Do not invoke 'flush()' - the decision is left to subclasses.
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
     *
     * @see #getGeographicBoundingBox
     * @see #getTimeRange
     * @see #getEnvelope
     * @see #trimEnvelope
     */
    public synchronized NumberRange getVerticalRange() {
        return new NumberRange(zMin, zMax);
    }

    /**
     * Sets the vertical range of the elements to be read by this table.
     * The range should be specified in metres above the WGS&nbsp;1984 ellipsoid.
     *
     * @param  range The vertical range, or {@code null} for full coverage.
     * @return {@code true} if the vertical range changed as a result of this call, or
     *         {@code false} if the specified range is equals to the one already set.
     */
    public final boolean setVerticalRange(final NumberRange range) {
        final double minimum, maximum;
        if (range != null) {
            minimum = range.getMinimum(true);
            maximum = range.getMaximum(true);
        } else {
            minimum = Double.NEGATIVE_INFINITY;
            maximum = Double.POSITIVE_INFINITY;
        }
        return setVerticalRange(minimum, maximum);
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
        if (change) {
            envelope = null;
            trimmed = false;
            fireStateChanged("VerticalRange");
            // Do not invoke 'flush()' - the decision is left to subclasses.
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
     *
     * @see #getGeographicBoundingBox
     * @see #getVerticalRange
     * @see #getEnvelope
     * @see #trimEnvelope
     */
    public synchronized DateRange getTimeRange() {
        return new DateRange(tMin != Long.MIN_VALUE ? new Date(tMin) : null,
                             tMax != Long.MAX_VALUE ? new Date(tMax) : null);
    }

    /**
     * Sets the time range of the elements to be read by this table.
     *
     * @param  timeRange The time range.
     * @return {@code true} if the time range changed as a result of this call, or
     *         {@code false} if the specified range is equals to the one already set.
     */
    public final boolean setTimeRange(final DateRange timeRange) {
        Date startTime, endTime;
        if (timeRange != null) {
            startTime = timeRange.getMinValue();
            endTime   = timeRange.getMaxValue();
        } else {
            startTime = null;
            endTime   = null;
        }
        if (startTime!=null && !timeRange.isMinIncluded()) {
            startTime = new Date(startTime.getTime() + 1);
        }
        if (endTime!=null && !timeRange.isMaxIncluded()) {
            endTime = new Date(endTime.getTime() - 1);
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
        change  = (tMin != (tMin = (startTime != null) ? startTime.getTime() : Long.MIN_VALUE));
        change |= (tMax != (tMax = (  endTime != null) ?   endTime.getTime() : Long.MAX_VALUE));
        if (change) {
            envelope = null;
            trimmed = false;
            fireStateChanged("TimeRange");
            // Do not invoke 'flush()' - the decision is left to subclasses.
        }
        return change;
    }

    /**
     * Returns the approximative resolution desired, or {@code null} for best
     * resolution. The units are degrees of longitude or latitude, as in the
     * {@linkplain #getGeographicBoundingBox geographic bounding box}.
     */
    public synchronized Dimension2D getPreferredResolution() {
        if (xResolution > 0 || yResolution > 0) {
            return new XDimension2D.Float(xResolution, yResolution);
        } else {
            return null;
        }
    }

    /**
     * Sets the preferred resolution in degrees of longitude or latitude. This is only an
     * approximative hint, since there is no garantee that an image will be read with that
     * resolution. A null values means that the best available resolution should be used.
     *
     * @param  resolution The preferred geographic resolution, or {@code null} for best resolution.
     * @return {@code true} if the resolution changed as a result of this call, or
     *         {@code false} if the specified resolution is equals to the one already set.
     */
    public synchronized boolean setPreferredResolution(final Dimension2D resolution) {
        float x,y;
        if (resolution != null) {
            x = (float) resolution.getWidth ();
            y = (float) resolution.getHeight();
            if (!(x >= 0)) x = 0; // '!' for catching NaN
            if (!(y >= 0)) y = 0;
        } else {
            x = 0;
            y = 0;
        }
        boolean change;
        change  = (xResolution != (xResolution = x));
        change |= (yResolution != (yResolution = y));
        if (change) {
            // Do not clear the envelope since it still valid.
            fireStateChanged("PreferredResolution");
        }
        return change;
    }

    /**
     * Shrinks the {@linkplain #getEnvelope spatio-temporal envelope} to a smaller envelope
     * containing all the elements to be returned by this table.  This method iterates over
     * the elements that intercept the envelope specified by {@code setXXX(...)} methods.
     * Then the envelope is altered in such a way that the {@code getXXX(...)} method returns
     * an identical or smaller envelope intercepting the same set of elements.
     *
     * @throws CatalogException if a logical error occured.
     * @throws SQLException if an error occured while reading the database.
     */
    public synchronized void trimEnvelope() throws CatalogException, SQLException {
        if (trimmed) {
            return;
        }
        final QueryType type = QueryType.BOUNDING_BOX;
        final int timeColumn = (byTimeRange     != null) ? byTimeRange    .column.indexOf(type) : 0;
        final int bboxColumn = (bySpatialExtent != null) ? bySpatialExtent.column.indexOf(type) : 0;
        if (timeColumn != 0 || bboxColumn != 0) {
            final PreparedStatement statement = getStatement(type);
            if (statement != null) {
                envelope = null; // Clears now in case of failure.
                final ResultSet results = statement.executeQuery();
                while (results.next()) { // Should contains only one record.
                    if (timeColumn != 0) {
                        Date time;
                        final Calendar calendar = getCalendar();
                        time = results.getTimestamp(timeColumn, calendar);
                        if (time != null) {
                            tMin = max(tMin, time.getTime());
                        }
                        time = results.getTimestamp(timeColumn + 1, calendar);
                        if (time != null) {
                            tMax = min(tMax, time.getTime());
                        }
                    }
                    if (bboxColumn != 0) {
                        final String bbox = results.getString(bboxColumn);
                        if (bbox == null) {
                            continue;
                        }
                        final Envelope envelope;
                        try {
                            envelope = SpatialFunctions.parse(bbox);
                        } catch (NumberFormatException e) {
                            throw new IllegalRecordException(e, this, results, bboxColumn, null);
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
                }
                results.close();
                fireStateChanged("Envelope");
            }
        }
        trimmed = true;
    }

    /**
     * Invoked automatically by for a newly created statement or when this table
     * {@linkplain #fireStateChanged changed its state}. The default implementation
     * set the parameter values to the spatio-temporal bounding box.
     *
     * @param  type The query type (mat be {@code null}).
     * @param  statement The statement to configure (never {@code null}).
     * @throws CatalogException if the statement can not be configured.
     * @throws SQLException if a SQL error occured while configuring the statement.
     */
    @Override
    protected void configure(final QueryType type, final PreparedStatement statement)
            throws CatalogException, SQLException {
        super.configure(type, statement);
        if (byTimeRange != null) {
            final int index = byTimeRange.indexOf(type);
            if (index != 0) {
                final long min = (tMin != Long.MIN_VALUE) ? tMin : DEFAULT_START_TIME;
                final long max = (tMax != Long.MAX_VALUE) ? tMax : (DEFAULT_END_TIME + System.currentTimeMillis());
                final Calendar calendar = getCalendar();
                statement.setTimestamp(index + 1, new Timestamp(min), calendar);
                statement.setTimestamp(index,     new Timestamp(max), calendar);
            }
        }
        if (bySpatialExtent != null) {
            final int index = bySpatialExtent.indexOf(type);
            if (index != 0) {
                final GeneralEnvelope envelope = new GeneralEnvelope(
                        new double[] {xMin, yMin, zMin},
                        new double[] {xMax, yMax, zMax});
                statement.setString(index, SpatialFunctions.formatPolygon(envelope));
            }
        }
    }

    /**
     * Notifies that this table state changed.
     */
    @Override
    protected void fireStateChanged(final String property) {
        if (!property.equalsIgnoreCase("PreferredResolution")) {
            envelope = null; // As a safety (not that expensive to recompute).
        }
        super.fireStateChanged(property);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void flush() {
        envelope = null;
        super.flush();
    }
}
