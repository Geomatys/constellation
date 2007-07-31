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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Date;
import java.util.Set;
import java.util.SortedSet;
import java.util.logging.Logger;
import javax.imageio.IIOException;
import org.geotools.coverage.grid.GridGeometry2D;

import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;

import org.geotools.coverage.FactoryFinder;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.factory.Hints;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;

import net.sicade.catalog.CatalogException;
import net.sicade.coverage.catalog.CoverageReference;
import net.sicade.coverage.catalog.sql.GridCoverageTable;
import net.sicade.catalog.Database;
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
     * The database to connect to. Will be instantiate when first needed.
     */
    private static Database database;

    /**
     * The images table. Will be created when first needed.
     */
    private static GridCoverageTable table;

    /**
     * The entry to log messages during the process.
     */
    private static final Logger LOGGER = Logger.getLogger(PostGridReader.class.toString());

    /**
     * The format that created this reader.
     */
    private final Format format;

    /**
     * The layer where to fetch image from.
     */
    private final String layer;

    /**
     * Constructs a reader for the specified layer.
     *
     * @param layer  The name of the layer.
     * @param format The default format.
     * @param input  The input file or URL on the local system directory. May be null.
     * @param hints  An optional set of hints, or {@code null} if none.
     */
    public PostGridReader(final String layer, final Format format, final Object input, final Hints hints) {
        this.layer = layer;
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
        /*Observations obs = Observations.getDefault();
        CoverageReference ref = null;
        try {
            Layer s = obs.getLayer(layer);
            ref = s.getCoverageReference(date);
        } catch (CatalogException ex) {
            throw new IIOException(ex.getLocalizedMessage(), ex);
        } 

        GridCoverage2D coverage = ref.getCoverage(null); 
        return coverage.geophysics(false);*/
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
            if (time == null) {
                time = getAvailableTimes().iterator().next();
            }
            if (elevation == null) {
                elevation = getAvailableAltitudes().iterator().next();
            }
            return read(time, elevation);
        } catch (SQLException e) {
            throw new IIOException("Erreur connexion à la base de données.", e);
        } catch (CatalogException e) {
            throw new IIOException("Erreur connexion à la base de données.", e);
        }
    }

    /**
     * Returns a Set of all dates available for a request. In our implementation, these dates must be between 
     * the reference date (in 1970) and today.
     * @todo Get the series specified by the user.
     *
     * @return A set of dates available.
     * @throws SQLException
     * @throws IOException
     * @throws CatalogException
     */
    public static Set<Date> getAvailableTimes() throws SQLException, IOException, CatalogException {
        final GridCoverageTable table = getTable();
        table.setLayer("SST (Monde - Coriolis)"); // TODO
        table.setTimeRange(new Date(0), new Date());
        return table.getAvailableTimes();
    }

    /**
     * Returns a set of elevations that are commons to every dates present in the database. In our implementation, 
     * these dates must be between the reference date (in 1970) and today.
     * @todo Get the series specified by the user.
     *
     * @return A set of elevations common for all dates.
     * @throws SQLException
     * @throws IOException
     * @throws CatalogException
     */
    public static SortedSet<Number> getAvailableAltitudes() throws SQLException, IOException, CatalogException {
        final GridCoverageTable table = getTable();
        table.setLayer("SST (Monde - Coriolis)"); // TODO
        table.setTimeRange(new Date(0), new Date());
        return table.getAvailableAltitudes();
    }

    /**
     * Returns the image table.
     */
    private static synchronized GridCoverageTable getTable() throws SQLException, IOException {
        if (database == null) {
            database = new Database();
        }
        if (table == null) {
            table = new GridCoverageTable(database.getTable(GridCoverageTable.class));
        }
        return table;
    }

    /**
     * Returns an image for the given layer at the given date.
     */
    private GridCoverage read(final Date time, final Number elevation) throws SQLException, IOException, CatalogException {
        final long TIMESPAN = 12*60*60*1000L;  // 12 hours
        final GridCoverageTable table = getTable();
        table.setLayer(layer);
        table.setTimeRange(new Date(time.getTime() - TIMESPAN), new Date(time.getTime() + TIMESPAN));
        if (elevation != null) {
            double z = elevation.doubleValue();
            table.setVerticalRange(z,z);
        } else {
            table.setVerticalRange(0,0);
        }
        CoverageReference ref = table.getEntry();
        System.out.println(ref);
        if (ref != null) {
            return trimTo2D(ref.getCoverage(null));
        }
        if (true) {
            // Returns an arbitrary image. TODO: we need to do something better.
            table.setTimeRange(new Date(0), new Date());
            ref = table.getEntry();
            if (ref != null) {
                return trimTo2D(ref.getCoverage(null));
            }
        }            
        final BufferedImage image = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.drawString("Pas d'images à la date " + time, 10, 200);
        g.dispose();
        return FactoryFinder.getGridCoverageFactory(null).create("Erreur", image, new Envelope2D(null, -180, -90, 360, 180));
    }

    /**
     * Reduces the specified coverage to a 2D form.
     */
    private static GridCoverage2D trimTo2D(GridCoverage2D coverage) {
        coverage = coverage.geophysics(false);
        final GridGeometry2D geometry = (GridGeometry2D) coverage.getGridGeometry();
        coverage = FactoryFinder.getGridCoverageFactory(null).create(coverage.getName(),
                coverage.getRenderedImage(), geometry.getCoordinateReferenceSystem2D(),
                geometry.getGridToCRS2D(), coverage.getSampleDimensions(), null, null);
        return coverage.geophysics(false);
    }
}
