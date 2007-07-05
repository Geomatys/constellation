/*
 * Sicade - Syst�mes int�gr�s de connaissances pour l'aide � la d�cision en environnement
 * (C) 2005, Institut de Recherche pour le D�veloppement
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sicade.observation.coverage.sql;

// J2SE dependencies
import java.io.File;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.awt.Dimension;
import java.util.Calendar;
import java.util.Date;
import java.util.IllegalFormatException;
import java.util.Set;
import java.util.logging.LogRecord;
import org.geotools.referencing.crs.DefaultTemporalCRS;

// Seagis dependencies
import net.sicade.observation.Element;
import net.sicade.observation.ConfigurationKey;
import net.sicade.observation.CatalogException;
import net.sicade.observation.LoggingLevel;
import net.sicade.observation.sql.Database;
import net.sicade.observation.coverage.Series;
import net.sicade.observation.coverage.SubSeries;
import net.sicade.observation.sql.QueryType;

// Geotools dependencies
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import org.geotools.referencing.CRS;

// OpenGIS dependencies
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.TemporalCRS;
import org.opengis.referencing.operation.TransformException;


/**
 * Ins�re de nouvelles entr�es dans la base de donn�es d'images. Par exemple, cette classe peut
 * �tre utilis�e pour ajouter de nouvelles entr�es dans la table {@code "GridCoverages"}, ce qui
 * peut impliquer l'ajout d'entr�s dans la table {@code "GeographicBoundingBoxes"} en m�me temps.
 * Cette classe peut ajouter de nouvelles lignes aux tables existantes, mais ne modifie jamais les
 * lignes existantes.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 *
 * @todo Beaucoup de parties de cette classe ne sont pas encore fonctionnelles.
 */
public class WritableGridCoverageTable extends GridCoverageTable {
    /**
     * The SQL instruction for inserting a new grid coverage.
     */
    private static final ConfigurationKey INSERT = new ConfigurationKey("GridCoverage:INSERT",
            "INSERT INTO coverages.\"GridCoverages\" " +
                "(series, filename, \"startTime\", \"endTime\", extent)\n" +
                "VALUES (?, ?, ?, ?, ?)");

    /**
     * Connexion vers la table des �tendues g�ographiques.
     */
    private GeographicBoundingBoxTable geographicBoundingBoxes;

    /**
     * Constructs a new {@code WritableGridCoverageTable}.
     *
     * @param  connection The connection to the database.
     * @throws SQLException if the table can't be constructed.
     */
    public WritableGridCoverageTable(final Database database) throws SQLException {
        super(database);
    }

    /**
     * Retourne la sous-s�ries � utiliser.
     * 
     * @throws CatalogException si aucune s�rie n'a �t� sp�cifi�e, ou si la s�rie ne contient pas 
     *                          exactement une sous-s�rie.
     */
    private SubSeries getSubSeries() throws CatalogException {
        final Series series = getSeries();
        if (series == null) {
            throw new CatalogException("Aucune s�rie n'a �t� sp�cifi�e.");
        }
        final Set<SubSeries> subseries = series.getSubSeries();
        if (subseries.size() != 1) {
            throw new CatalogException("La s�rie devrait contenir exactement une sous-s�rie.");
        }
        return subseries.iterator().next();
    }

    /**
     * Log a record. This is used for logging warning or information messages when the database
     * is updated. Since this class is used by {@link CoverageTable#addGridCoverage} only, we will
     * set source class and method name according.
     */
    private static void log(final LogRecord record) {
        record.setSourceClassName("WritableGridCoverageTable");
        record.setSourceMethodName("addEntry");
        Element.LOGGER.log(record);
    }

    /**
     * Log an "SQL_UPDATE" record with the specified query as the message.
     * This method replaces all question marks found in the query by the
     * specified argument values.
     */
    private static void logUpdate(final String query, final Object... values) {
        final StringBuilder buffer = new StringBuilder();
        int last = 0;
        for (int i=0; i<values.length; i++) {
            final int stop = query.indexOf('?', last);
            if (stop < 0) {
                // Missing arguments in the query. Since this method is used for logging
                // purpose only, we will not stop the normal execution flow for that.
                break;
            }
            final boolean isChar = (values[i] instanceof CharSequence);
            buffer.append(query.substring(last, stop));
            if (isChar) buffer.append('\'');
            buffer.append(values[i].toString());
            if (isChar) buffer.append('\'');
            last = stop+1;
        }
        buffer.append(query.substring(last));
        log(new LogRecord(LoggingLevel.UPDATE, buffer.toString()));
    }
    
    /**
     * Ajoute une entr�e dans la table "{@code GridCoverages}". La m�thode {@link #setSeries}
     * doit d'abord avoir �t� appel�e au moins une fois.
     * 
     * @param   file        Le fichier image avec son chemin complet.
     * @param   envelope    L'enveloppe g�ographique pour cette image.
     * @param   size        La dimension de l'image, en nombre de pixels.
     * @throws  SQLException     Si l'ex�cution d'une requ�te SQL a �chou�e.
     * @throws  FactoryException Si le Coordinate Reference System n'est pas de type "Temporel".
     * @throws  CatalogException Si l'ex�cution a �chou�e pour une autre raison.
     */
    public synchronized void addEntry(final File      file,
                                      final Envelope  envelope,
                                      final Dimension size) 
            throws CatalogException, FactoryException, SQLException
    {
        GeographicBoundingBox geoBBox = null;
        try {
            geoBBox = new GeographicBoundingBoxImpl(envelope);
        } catch (TransformException ex) {
            log(new LogRecord(LoggingLevel.SEVERE, ex.getMessage()));
        }        
        TemporalCRS temporalCRS = CRS.getTemporalCRS(envelope.getCoordinateReferenceSystem());
        if (temporalCRS == null) {
            throw new FactoryException("Le CRS sp�cifi� n'est pas de type \"Temporel\".");
        }
        DefaultTemporalCRS defaultTempCRS = DefaultTemporalCRS.wrap(temporalCRS);
        final Date startTime = defaultTempCRS.toDate(envelope.getMinimum(0));
        final Date endTime   = defaultTempCRS.toDate(envelope.getMaximum(0));
        String fileNameWithExt = file.getName();
        final String fileName = fileNameWithExt.substring(0, fileNameWithExt.indexOf("."));
        addEntry(fileName, startTime, endTime, geoBBox, size);
    }
    
    /**
     * Ajoute une entr�e dans la table "{@code GridCoverages}". La m�thode {@link #setSeries}
     * doit d'abord avoir �t� appel�e au moins une fois.
     * 
     * @param   filename    Le nom de l'image, sans son chemin ni son extension.
     * @param   startTime   La date de d�but de la plage de temps concern�e par l'image.
     * @param   endTime     La date de fin de la plage de temps concern�e par l'image.
     * @param   bbox        La r�gion g�ographique de l'image.
     * @param   size        La dimension de l'image, en nombre de pixels.
     * @throws  SQLException     Si l'ex�cution d'une requ�te SQL a �chou�e.
     * @throws  CatalogException Si l'ex�cution a �chou�e pour une autre raison.
     */
    public synchronized void addEntry(final String            filename,
                                      final Date             startTime,
                                      final Date               endTime, 
                                      final GeographicBoundingBox bbox,
                                      final Dimension             size)
            throws CatalogException, SQLException 
    {
        if (geographicBoundingBoxes == null) {
            geographicBoundingBoxes = database.getTable(GeographicBoundingBoxTable.class);
        }
        final String bboxID = geographicBoundingBoxes.getIdentifier(bbox, size);
        if (bboxID == null) {
            throw new CatalogException("L'�tendue g�ographique n'est pas d�clar�e dans la base de donn�es.");
        }
        final Calendar          calendar  = getCalendar();
        final SubSeries         subseries = getSubSeries();
        final PreparedStatement statement = getStatement(QueryType.INSERT);
        statement.setString   (1, subseries.getName());
        statement.setString   (2, filename);
        statement.setTimestamp(3, new Timestamp(startTime.getTime()), calendar);
        statement.setTimestamp(4, new Timestamp(endTime.getTime())  , calendar);
        statement.setString   (5, bboxID);
        if (statement.executeUpdate() != 1) {
            throw new CatalogException("L'image n'a pas �t� ajout�e.");
        }
        logUpdate(getProperty(INSERT), subseries.getName(), filename, startTime, endTime, bbox);
    }

    /**
     * Retourne la requ�te SQL � utiliser pour le type sp�cifi�.
     */
    @Override
    protected String getQuery(final QueryType type) throws SQLException {
        switch (type) {
            case INSERT: return getProperty(INSERT);
            default:     return super.getQuery(type);
        }
    }
}
