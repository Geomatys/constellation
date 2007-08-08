/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2006, Institut de Recherche pour le Développement
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
package net.sicade.coverage.io;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.logging.Logger;
import javax.imageio.IIOException;

import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;

import org.geotools.coverage.FactoryFinder;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;

import net.sicade.catalog.CatalogException;
import net.sicade.coverage.catalog.CoverageReference;
import net.sicade.catalog.Database;
import net.sicade.coverage.catalog.Layer;
import net.sicade.coverage.catalog.sql.LayerTable;
import org.geotools.coverage.grid.GridCoverage2D;


/**
 * An implementation of {@link org.opengis.coverage.grid.GridCoverageReader} backed by the
 * PostGrid database.
 * 
 * @version $Id$
 * @author Cédric Briançon
 */
public class PostGridReader extends AbstractGridCoverage2DReader {
    /**
     * The logger.
     */
    private static final Logger LOGGER = Logger.getLogger("net.sicade.coverage.io");

    /**
     * The layer. Will be instantiated when first needed.
     *
     * @todo Should not be a static constant. Unfortunatly the {@link #getAvailableTimes}
     *       and {@link #getAvailableAltitudes} methods must be static for now, until we
     *       figure out how to pass a {@code PostGridReader} instance to the streaming
     *       renderer run by Geoserver.
     */
    private static Layer layer;

    /**
     * The format that created this reader.
     */
    private final Format format;

    /**
     * Constructs a reader for the specified layer.
     *
     * @param format The default format.
     * @param input  The input file or URL on the local system directory. May be null.
     * @param hints  An optional set of hints, or {@code null} if none.
     */
    public PostGridReader(final Format format, final Object input, final Hints hints) {
        if (hints != null) {
            this.hints.putAll(hints);
        }
        if (input != null) {
            this.source = input;
            if (source instanceof File) {
                this.coverageName = ((File)source).getName();
            } else {
                if (source instanceof URL) {
                    this.coverageName = ((URL) source).getFile();
                } else {
                    this.coverageName = "postgrid_coverage";
                }
            }

            // gets the coverage name without the extension and the dot
            final int dotIndex = coverageName.lastIndexOf('.');
            if (dotIndex >= 0) {
                coverageName = coverageName.substring(0, dotIndex);
            }
        }
        this.format = format;
        this.crs = DefaultGeographicCRS.WGS84;
        this.originalEnvelope = new GeneralEnvelope(crs);
        this.originalEnvelope.setRange(0, -180, +180);
        this.originalEnvelope.setRange(1, -90, +90);
        this.originalGridRange = new GeneralGridRange(originalEnvelope); 
    }

    /**
     * Gets information about the Observation format.
     */
    public Format getFormat() {
        return format;
    }

    /**
     * Read the coverage and generate the Grid Coverage associated.
     *
     * @param params Contains the parameters values for this coverage.
     * @return The grid coverage generated from the reading of the netcdf file.
     * @throws IllegalArgumentException
     * @throws IOException
     */
    public GridCoverage read(final GeneralParameterValue[] params)
            throws IllegalArgumentException, IOException
    {
        Date time = null;
        Number elevation = null;
        for (int i=0; i<params.length; i++) {
            final GeneralParameterValue param = params[i];
            if (param instanceof ParameterValue) {
                final ParameterValue value = (ParameterValue) param;
                final String name = value.getDescriptor().getName().getCode().trim();
                if (name.equalsIgnoreCase("TIME")) {
                    time = (Date) value.getValue();
                }
                if (name.equalsIgnoreCase("ELEVATION")) {
                    elevation = (Number) value.getValue();
                }
            }
        }
        try {
            return read(time, elevation);
        } catch (SQLException e) {
            throw new IIOException(e.toString(), e);
        } catch (CatalogException e) {
            throw new IIOException(e.toString(), e);
        }
    }

    /**
     * Returns a Set of all dates available for a request.
     *
     * @return A set of dates available.
     * @throws SQLException
     * @throws IOException
     * @throws CatalogException
     *
     * @todo Get the series specified by the user.
     */
    public static Set<Date> getAvailableTimes() throws SQLException, IOException, CatalogException {
        return getLayer().getAvailableTimes();
    }

    /**
     * Returns a set of elevations that are commons to every dates present in the database.
     *
     * @return A set of elevations common for all dates.
     * @throws SQLException
     * @throws IOException
     * @throws CatalogException
     *
     * @todo Get the series specified by the user.
     */
    public static SortedSet<Number> getAvailableAltitudes() throws SQLException, IOException, CatalogException {
        return getLayer().getAvailableElevations();
    }

    /**
     * Returns the layer.
     */
    private static synchronized Layer getLayer() throws IOException, SQLException, CatalogException {
        if (layer == null) {
            final LayerTable table = new LayerTable(new Database());
            layer = table.getEntry("SST (Monde - Coriolis)"); // TODO
        }
        return layer;
    }

    /**
     * Returns an image for the given layer at the given date.
     */
    private GridCoverage read(final Date time, final Number elevation) throws SQLException, IOException, CatalogException {
        final Layer layer = getLayer();
        final CoverageReference ref;
        String name = null;
        if (time != null) {
            ref = layer.getCoverageReference(time, elevation);
            LOGGER.info("time=" + time + ", elevation=" + elevation); // TODO: kick down logging level after debugging.
        } else {
            name = "coriolis";
            final Iterator<CoverageReference> it = layer.getCoverageReferences().iterator();
            if (it.hasNext()) {
                ref = it.next();
            } else {
                throw new CatalogException("Aucune image dans la série.");
            }
            if (elevation != null) {
                LOGGER.warning("Profondeur ignorée: " + elevation);
            }
            LOGGER.warning("Choix d'une image aléatoire.");
        }
        LOGGER.info("Image sélectionnée: " + ref);  // TODO: kick down logging level after debugging.
        return trimTo2D(name, ref.getCoverage(null));
    }

    /**
     * Reduces the specified coverage to a 2D form.
     */
    private static GridCoverage2D trimTo2D(CharSequence name, GridCoverage2D coverage) {
        coverage = coverage.geophysics(false);
        if (name == null) {
            name = coverage.getName();
        }
        final GridGeometry2D geometry = (GridGeometry2D) coverage.getGridGeometry();
        coverage = FactoryFinder.getGridCoverageFactory(null).create(name,
                coverage.getRenderedImage(), geometry.getCoordinateReferenceSystem2D(),
                geometry.getGridToCRS2D(), coverage.getSampleDimensions(), null, null);
        return coverage.geophysics(false);
    }
}
