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
package net.seagis.coverage.catalog;

import java.util.Set;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.Iterator;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Dimension2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;

import org.opengis.coverage.Coverage;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.geotools.util.DateRange;
import org.geotools.util.MeasurementRange;
import org.geotools.resources.Utilities;
import org.geotools.coverage.CoverageStack;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;

import net.seagis.catalog.Entry;
import net.seagis.catalog.CatalogException;
import net.seagis.catalog.ServerException;
import net.seagis.coverage.model.Model;
import net.seagis.resources.XArray;
import net.seagis.resources.i18n.Resources;
import net.seagis.resources.i18n.ResourceKeys;


/**
 * Implementation of a {@linkplain Layer layer}.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 */
final class LayerEntry extends Entry implements Layer {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 5283559646740856038L;

    /**
     * The theme of this layer (e.g. Temperature, Salinity, etc.).
     */
    private final String thematic;

    /**
     * Procedure applied for this layer (e.g. Gradients, etc.).
     */
    private final String procedure;

    /**
     * Typical time interval (in days) between images, or {@link Double#NaN} if unknown.
     */
    private final double timeInterval;

    /**
     * The domain for this layer. May be shared by many instances of {@code LayerEntry}.
     * May be {@code null} if not applicable.
     */
    private DomainOfLayerEntry domain;

    /**
     * If this layer is the result of some numerical model, the model. Other wise, {@code null}.
     * This field is set by {@link LayerTable#postCreateEntry} only.
     */
    Model model;

    /**
     * The series associated with their names. This map will be created by
     * {@link LayerTable#postCreateEntry}. It will contains every series,
     * including the hidden ones.
     */
    private Map<String,Series> seriesMap;

    /**
     * A immutable view over the visible series. It may contains less entries
     * than {@link #seriesMap} because some series may be hidden.
     */
    private Set<Series> series;

    /**
     * A fallback layer to be used if no image can be found for a given date in this layer.
     * May be {@code null} if there is no fallback.
     * <p>
     * Upon construction, this field contains only the layer name as a {@link String}. After
     * {@link LayerTable#postCreateEntry} processing, the string will have be replaced by a
     * {@link LayerEntry} instance.
     */
    Object fallback;

    /**
     * A tri or four-dimensional view over the data to be returned by {@link #getCoverage}.
     * Will be created only when first needed.
     */
    private transient Reference<Coverage> coverage;

    /**
     * Connection to the grid coverage table.
     */
    private GridCoverageTable data;

    /**
     * La fabrique à utiliser pour obtenir une seule image à une date spécifique.
     * Ne sera construit que la première fois où elle sera nécessaire.
     *
     * @deprecated Try to use {@link #data} instead.
     */
    private transient GridCoverageTable singleCoverageServer;

    /**
     * Creates a new layer.
     *
     * @param name         The layer name.
     * @param thematic     Thematic for this layer (e.g. Temperature, Salinity, etc.).
     * @param procedure    Procedure applied for this layer (e.g. Gradients, etc.).
     * @param timeInterval Typical time interval (in days) between images, or {@link Double#NaN} if unknown.
     * @param remarks      Optional remarks, or {@code null}.
     */
    protected LayerEntry(final String name, final String thematic, final String procedure,
                         final double timeInterval, final String remarks)
    {
        super(name, remarks);
        this.thematic     = thematic;
        this.procedure    = procedure;
        this.timeInterval = timeInterval;
    }

    /**
     * {@inheritDoc}
     */
    public String getThematic() {
        return thematic;
    }

    /**
     * {@inheritDoc}
     */
    public String getProcedure() {
        return procedure;
    }

    /**
     * {@inheritDoc}
     */
    public Layer getFallback() {
        final Object fallback = this.fallback; // Protect from changes in concurrent threads.
        return (fallback instanceof Layer) ? (Layer) fallback : null;
    }

    /**
     * Sets the series for this layer. This method is invoked by {@link LayerTable#postCreateEntry}
     * only. The series set given will be trimmed (every hidden series will be removed from it).
     * This works will be performed on a copy of the supplied set.
     *
     * @param series The series to set. This given set will not be modified.
     */
    final void setSeries(Set<Series> series) {
        // Copies the set in order to protect the user-supplied set from the changes we are going to
        // apply in this method, and also because the caller supplies a non-serializable set and we
        // want to make it serializable.
        series = new LinkedHashSet<Series>(series);
        final Map<String,Series> map = new HashMap<String,Series>((int) (series.size() / 0.75f) + 1);
        for (final Iterator<Series> it=series.iterator(); it.hasNext();) {
            final Series entry = it.next();
            assert entry.getLayer() == this : entry;
            final String name = entry.getName().trim();
            if (map.put(name, entry) != null) {
                throw new IllegalArgumentException(Resources.format(
                        ResourceKeys.ERROR_DUPLICATED_RECORD_$1, name));
            }
            // Following is specific to SeriesEntry implementation. If faced with a different
            // implementation, we will conservatively assume that all series are to be shown.
            if (entry instanceof SeriesEntry) {
                if (!((SeriesEntry) entry).visible) {
                    it.remove();
                }
            }
        }
        this.seriesMap = map;
        this.series = Collections.unmodifiableSet(series);
    }

    /**
     * {@inheritDoc}
     */
    public Set<Series> getSeries() {
        if (series != null) {
            // Note: The series Set may have less entries than
            // the series Map since some series may be hidden.
            assert seriesMap.values().containsAll(series) : this;
            return series;
        } else {
            return Collections.emptySet();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Series getSeries(final String name) {
        return (seriesMap != null && name != null) ? seriesMap.get(name.trim()) : null;
    }

    /**
     * {@inheritDoc}
     */
    public double getTimeInterval() {
        return timeInterval;
    }

    /**
     * {@inheritDoc}
     */
    public SortedSet<Date> getAvailableTimes() throws CatalogException {
        final GridCoverageTable data = this.data;   // Protect against concurrent changes.
        if (data != null) try {
            return data.getAvailableTimes();
        } catch (SQLException e) {
            throw new ServerException(e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public SortedSet<Number> getAvailableElevations() throws CatalogException {
        final GridCoverageTable data = this.data;   // Protect against concurrent changes.
        if (data != null) try {
            return data.getAvailableElevations();
        } catch (SQLException e) {
            throw new ServerException(e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public MeasurementRange[] getSampleValueRanges() {
        MeasurementRange[] ranges = null;
        for (final Series series : getSeries()) {
            final Format format = series.getFormat();
            if (format != null) {
                final MeasurementRange[] candidates = format.getSampleValueRanges();
                if (ranges == null) {
                    ranges = candidates;
                } else {
                    final int length;
                    if (candidates.length <= ranges.length) {
                        length = candidates.length;
                    } else {
                        length = ranges.length;
                        ranges = XArray.resize(ranges, candidates.length);
                        System.arraycopy(candidates, length, ranges, length, candidates.length-length);
                    }
                    for (int i=0; i<length; i++) {
                        ranges[i] = ranges[i].intersect(candidates[i]);
                    }
                }
            }
        }
        return ranges;
    }

    /**
     * {@inheritDoc}
     */
    public DateRange getTimeRange() throws CatalogException {
        if (domain != null) {
            final DateRange timeRange = domain.timeRange;
            if (timeRange != null) {
                return timeRange; // Immutable instance.
            }
        }
        // Fallback to a search in the GridCoverages table.
        final GridCoverageTable data = this.data;   // Protect against concurrent changes.
        if (data != null) {
            try {
                data.trimEnvelope(); // Do a real work only when first invoked.
            } catch (SQLException exception) {
                throw new ServerException(exception);
            }
            return data.getTimeRange();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public GeographicBoundingBox getGeographicBoundingBox() throws CatalogException {
        if (domain != null) {
            final GeographicBoundingBox bbox = domain.bbox;
            if (bbox != null) {
                return bbox; // Immutable instance.
            }
        }
        // Fallback to a search in the GridCoverages table.
        final GridCoverageTable data = this.data;   // Protect against concurrent changes.
        if (data != null) {
            try {
                data.trimEnvelope(); // Do a real work only when first invoked.
            } catch (SQLException exception) {
                throw new ServerException(exception);
            }
            return data.getGeographicBoundingBox();
        }
        return GeographicBoundingBoxImpl.WORLD;
    }

    /**
     * {@inheritDoc}
     */
    public Dimension2D getAverageResolution() throws CatalogException {
        if (domain != null) {
            final Dimension2D resolution = domain.resolution;
            if (resolution != null) {
                return (Dimension2D) resolution.clone();
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Rectangle getBounds() throws CatalogException {
        final Dimension2D resolution = getAverageResolution();
        if (resolution != null) {
            final GeographicBoundingBox box = getGeographicBoundingBox();
            if (box != null) {
                return new Rectangle(0, 0,
                    (int)Math.round((box.getEastBoundLongitude() - box.getWestBoundLongitude()) / resolution.getWidth()),
                    (int)Math.round((box.getNorthBoundLatitude() - box.getSouthBoundLatitude()) / resolution.getHeight()));
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized CoverageReference getCoverageReference(final Date time, final Number elevation)
            throws CatalogException
    {
        long delay = Math.round(timeInterval * (GridCoverageTable.MILLIS_IN_DAY / 2));
        if (delay <= 0) {
            delay = GridCoverageTable.MILLIS_IN_DAY / 2;
        }
        Date startTime, endTime;
        if (time != null) {
            final long t = time.getTime();
            startTime = new Date(t - delay);
            endTime = new Date(t + delay);
        } else {
            startTime = null;
            endTime   = null;
        }
        try {
            if (singleCoverageServer == null) {
                singleCoverageServer = new GridCoverageTable(data);
            }
            singleCoverageServer.setTimeRange(startTime, endTime);
            final double z = (elevation != null) ? elevation.doubleValue() : 0;
            singleCoverageServer.setVerticalRange(z, z); // TODO: choose a better range.
            return singleCoverageServer.getEntry();
        } catch (SQLException exception) {
            throw new ServerException(exception);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Set<CoverageReference> getCoverageReferences() throws CatalogException {
        final GridCoverageTable data = this.data;   // Protect against concurrent changes.
        if (data != null) try {
            return data.getEntries();
        } catch (SQLException exception) {
            throw new ServerException(exception);
        } else {
            return Collections.emptySet();
        }
    }

    /**
     * {@inheritDoc}
     */
    public CoverageReference getCoverageReference() throws CatalogException {
        final GridCoverageTable data = this.data;   // Protect against concurrent changes.
        if (data != null) try {
            return data.getEntry();
        } catch (SQLException exception) {
            throw new ServerException(exception);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public synchronized Coverage getCoverage() throws CatalogException {
        Coverage c = null;
        if (coverage != null) {
            c = coverage.get();
            if (c != null) {
                return c;
            }
            LOGGER.fine("Reconstruit à nouveau la converture de \"" + getName() + "\".");
        }
        if (data != null) try {
            final CoordinateReferenceSystem crs;
            if (false) {
                // TODO: current version doesn't have enough dimensions.
                crs = data.getCoordinateReferenceSystem();
            } else {
                crs = null; // Lets CoverageStack infers automatically.
            }
            c = new CoverageStack(getName(), crs, getCoverageReferences());
            coverage = new SoftReference<Coverage>(c);
        } catch (IOException exception) {
            throw new ServerException(exception);
        }
        return c;
    }

    /**
     * {@inheritDoc}
     */
    public BufferedImage getLegend(final Dimension dimension) {
        final Map<Format,Integer> count = new HashMap<Format,Integer>();
        Format format = null;
        int occurs = 0;
        // Search for the most frequently used format.
        for (final Series series : getSeries()) {
            final Format candidate = series.getFormat();
            int n = 1;
            final Integer c = count.put(candidate, n);
            if (c != null) {
                n = c + 1;
                count.put(candidate, n);
            }
            if (n > occurs) {
                occurs = n;
                format = candidate;
            }
        }
        return (format != null) ? format.getLegend(dimension) : null;
    }

    /**
     * {@inheritDoc}
     */
    public Model getModel() throws CatalogException {
        return model;
    }

    /**
     * Définit la connexion vers les données à utiliser pour cette entrée. Cette méthode est
     * appellée une et une seule fois par {@link LayerTable#postCreateEntry} pour chaque
     * entrée créée.
     *
     * @param data Connexion vers une vue des données comme une matrice tri-dimensionnelle.
     * @throws IllegalStateException si une connexion existait déjà pour cette entrée.
     */
    protected synchronized void setGridCoverageTable(final GridCoverageTable data,
            final DomainOfLayerEntry domain) throws IllegalStateException
    {
        if (this.data != null) {
            throw new IllegalStateException(getName());
        }
        this.data   = data;
        this.domain = domain;
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
            final LayerEntry that = (LayerEntry) object;
            return Utilities.equals(this.thematic,  that.thematic ) &&
                   Utilities.equals(this.procedure, that.procedure) &&
                   Double.doubleToLongBits(this.timeInterval) ==
                   Double.doubleToLongBits(that.timeInterval);
            /*
             * On ne teste pas 'fallback' car la méthode 'equals' doit fonctionner dès que la
             * construction de l'entrée est complétée, alors que 'fallback' est définit un peu
             * plus tard ('postCreateEntry'). Même chose pour 'domain'.
             */
        }
        return false;
    }
}
