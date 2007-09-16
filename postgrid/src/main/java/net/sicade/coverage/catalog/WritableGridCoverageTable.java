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
package net.sicade.coverage.catalog;

import java.sql.Timestamp;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.net.URL;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.imageio.ImageReader;
import javax.units.SI;

import org.opengis.metadata.extent.GeographicBoundingBox;

import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;

import net.sicade.util.DateRange;
import net.sicade.catalog.Database;
import net.sicade.catalog.QueryType;
import net.sicade.catalog.CatalogException;
import net.sicade.coverage.io.MetadataParser;


/**
 * A grid coverage table with write capabilities. This class can be used in order to insert new
 * image in the database. Note that adding new records in the {@code "GridCoverages"} table may
 * imply adding new records in dependent tables like {@code "GridGeometries"}. This class may
 * add new records, but will never modify existing records.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public class WritableGridCoverageTable extends GridCoverageTable {
    /**
     * Constructs a new {@code WritableGridCoverageTable}.
     *
     * @param connection The connection to the database.
     */
    public WritableGridCoverageTable(final Database database) {
        super(database);
    }

    /**
     * Constructs a new {@code WritableGridCoverageTable} with the same initial configuration
     * than the specified table.
     */
    public WritableGridCoverageTable(final WritableGridCoverageTable table) {
        super(table);
    }

    /**
     * Returns the series in which to insert the coverages.
     *
     * @throws CatalogException if no series has been specified, or if the layer contains
     *         zero or more than one series.
     */
    private Series getSeries() throws CatalogException {
        final Layer layer = getLayer();
        if (layer == null) {
            throw new CatalogException("Aucune couche n'a été spécifiée."); // TODO: localize
        }
        final Iterator<Series> it = layer.getSeries().iterator();
        if (it.hasNext()) {
            final Series series = it.next();
            if (!it.hasNext()) {
                return series;
            }
        }
        throw new CatalogException("La couche devrait contenir exactement une série."); // TODO: localize
    }

    /**
     * Returns the path for the specified input. The returned file should not be opened
     * since it may be invalid (especially if built from a URL input). Its only purpose
     * is to split the name part and the path part.
     *
     * @param  input The input.
     * @return The input as a file.
     * @throws CatalogException if the input is not recognized.
     */
    private static File path(final Object input) throws CatalogException {
        if (input instanceof File) {
            return (File) input;
        }
        if (input instanceof URL) {
            return new File(((URL) input).getPath());
        }
        if (input instanceof CharSequence) {
            return new File(input.toString());
        }
        throw new CatalogException(Errors.format(ErrorKeys.UNKNOW_TYPE_$1, Utilities.getShortClassName(input)));
    }

    /**
     * Adds entries (usually only one) inferred from the specified image reader.
     * The {@linkplain ImageReader#getInput reader input} must be set, and its
     * {@linkplain ImageReader#getImageMetadata metadata} shall conforms to the
     * Geotools {@linkplain GeographicMetadata geographic metadata}.
     * <p>
     * This method will typically not read the full image, but only the metadata required.
     *
     * @param readers The image reader.
     */
    public void addEntry(final ImageReader reader) throws CatalogException, SQLException, IOException {
        addEntry(Collections.singleton(reader).iterator(), 0);
    }

    /**
     * Adds entries inferred from the specified image readers. The {@linkplain ImageReader#getInput
     * reader input} must be set, and its {@linkplain ImageReader#getImageMetadata metadata} shall
     * conforms to the Geotools {@linkplain GeographicMetadata geographic metadata}.
     * <p>
     * This method will typically not read the full image, but only the metadata required.
     *
     * @param readers    The image readers. The iterator may recycle the same reader with different
     *                   {@linkplain ImageReader#getInput input} on each call to {@link Iterator#next}.
     * @param imageIndex The index of the image to insert in the database.
     */
    public synchronized void addEntry(final Iterator<ImageReader> readers, final int imageIndex)
            throws CatalogException, SQLException, IOException
    {
        final GridCoverageQuery query     = (GridCoverageQuery) this.query;
        final Series            series    = getSeries();
        final Calendar          calendar  = getCalendar();
        final PreparedStatement statement = getStatement(QueryType.INSERT);
        final GridGeometryTable gridTable = getDatabase().getTable(GridGeometryTable.class);
        final int bySeries    = indexOf(query.series);
        final int byFilename  = indexOf(query.filename);
        final int byStartTime = indexOf(query.startTime);
        final int byEndTime   = indexOf(query.endTime);
        final int byExtent    = indexOf(query.spatialExtent);
        statement.setString(bySeries, series.getName());
        while (readers.hasNext()) {
            final ImageReader reader = readers.next();
            final File  input = path(reader.getInput());
            final String path = input.getParent();
            final String filename, extension;
            if (true) {
                final String name = input.getName();
                final int split = name.lastIndexOf('.');
                if (split >= 0) {
                    filename  = name.substring(0, split);
                    extension = name.substring(split + 1);
                } else {
                    filename  = name;
                    extension = "";
                }
            }
            /*
             * Gets the metadata of interest.
             */
            final MetadataParser metadata = new MetadataParser(reader, imageIndex);
            final DateRange[] dates = metadata.getDateRanges();
            if (dates == null) {
                warning("Aucune méta-donnée pour le fichier \"" + filename + "\"."); // TODO: localize
                continue;
            }
            final int width  = reader.getWidth (imageIndex);
            final int height = reader.getHeight(imageIndex);
            final AffineTransform gridToCRS = metadata.getGridToCRS(0, 1);
            final int horizontalSRID = metadata.getHorizontalSRID();
            final int verticalSRID = metadata.getVerticalSRID();
            final double[] verticalOrdinates = metadata.getVerticalValues(SI.METER);
            final String extent = gridTable.getIdentifier(new Dimension(width, height), gridToCRS,
                                            horizontalSRID, verticalOrdinates, verticalSRID, true);
            /*
             * Adds the entries for each image found in the file.
             * There is often only one image per file, but not always.
             */
            statement.setString(byFilename, filename);
            statement.setString(byExtent, extent);
            for (int i=0; i<dates.length; i++) {
                final Date startTime = dates[i].getMinValue();
                final Date   endTime = dates[i].getMaxValue();
                statement.setTimestamp(byStartTime, new Timestamp(startTime.getTime()), calendar);
                statement.setTimestamp(byEndTime,   new Timestamp(endTime.getTime())  , calendar);
                // TODO.
            }
        }
    }

    /**
     * Logs a warning.
     */
    private static final void warning(final String message) {
        final LogRecord record = new LogRecord(Level.WARNING, message);
        record.setSourceClassName(WritableGridCoverageTable.class.getName());
        record.setSourceMethodName("addEntry");
        LOGGER.log(record);
    }

    /**
     * Ajoute une entrée dans la table "{@code GridCoverages}". La méthode {@link #setLayer}
     * doit d'abord avoir été appelée au moins une fois.
     *
     * @param   filename    Le nom de l'image, sans son chemin ni son extension.
     * @param   startTime   La date de début de la plage de temps concernée par l'image.
     * @param   endTime     La date de fin de la plage de temps concernée par l'image.
     * @param   bbox        La région géographique de l'image.
     * @param   size        La dimension de l'image, en nombre de pixels.
     * @throws  SQLException     Si l'exécution d'une requête SQL a échouée.
     * @throws  CatalogException Si l'exécution a échouée pour une autre raison.
     */
    public synchronized void addEntry(final String            filename,
                                      final Date             startTime,
                                      final Date               endTime,
                                      final GeographicBoundingBox bbox,
                                      final Dimension             size)
            throws CatalogException, SQLException
    {
        final String bboxID = null; // TODO gridGeometries.getIdentifier(bbox, size);
        if (bboxID == null) {
            throw new CatalogException("L'étendue géographique n'est pas déclarée dans la base de données.");
        }
        final Calendar          calendar  = getCalendar();
        final Series            series    = getSeries();
        final PreparedStatement statement = getStatement(QueryType.INSERT);
        statement.setString   (1, series.getName());
        statement.setString   (2, filename);
        statement.setTimestamp(3, new Timestamp(startTime.getTime()), calendar);
        statement.setTimestamp(4, new Timestamp(endTime.getTime())  , calendar);
        statement.setString   (5, bboxID);
        if (statement.executeUpdate() != 1) {
            throw new CatalogException("L'image n'a pas été ajoutée.");
        }
    }
}
