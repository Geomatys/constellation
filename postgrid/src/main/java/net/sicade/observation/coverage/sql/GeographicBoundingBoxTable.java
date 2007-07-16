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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.geotools.geometry.GeneralEnvelope;

import net.sicade.observation.Element;
import net.sicade.observation.CatalogException;
import net.sicade.observation.sql.Column;
import net.sicade.observation.sql.Table;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.Parameter;
import net.sicade.observation.sql.Role;
import net.sicade.observation.sql.Shareable;
import net.sicade.observation.sql.SpatialColumn;
import net.sicade.observation.sql.QueryType;
import net.sicade.observation.sql.SpatialParameter;
import net.sicade.resources.i18n.Resources;
import net.sicade.resources.i18n.ResourceKeys;
import static net.sicade.observation.sql.QueryType.*;


/**
 * Connexion à la table des étendues géographiques des images.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public class GeographicBoundingBoxTable extends Table implements Shareable {
    /**
     * Facteur de tolérance pour la comparaison des limites géographiques.
     */
    private static final double EPSILON = 1E-7;

    /**
     * Column name declared in the {@linkplain #query query}.
     */
    private final Column name, width, height, depth;

    /**
     * Column name declared in the {@linkplain #query query}.
     */
    private final SpatialColumn.Box spatialExtent;

    /**
     * Parameter declared in the {@linkplain #query query}.
     */
    private final SpatialParameter.Box byExtent;

    /**
     * Parameter declared in the {@linkplain #query query}.
     */
    private final Parameter byWidth, byHeight, byDepth;

    /**
     * The SQL instruction for inserting a new geographic bounding box.
     *
     * @todo Choose the CRS.
     */
//    private static final SpatialConfigurationKey INSERT = new SpatialConfigurationKey("GeographicBoundingBoxes:INSERT",
//            "INSERT INTO \"GridGeometries\"\n" +
//            "  (id, \"westBoundLongitude\",\n" +
//            "       \"eastBoundLongitude\",\n" +
//            "       \"southBoundLatitude\",\n" +
//            "       \"northBoundLatitude\",\n" +
//            "       \"altitudeMin\",\n"        +
//            "       \"altitudeMax\",\n"        +
//            "       \"CRS\",\n"                +
//            "       width, height, depth)\n"   +
//            "  VALUES (?, ?, ?, ?, ?, ?, ?, 'IRD:WGS84(xyt)', ?, ?, ?)",
//
//            "INSERT INTO coverages.\"GridGeometries\"\n"+
//            "  (id, \"spatialExtent\",\n"               +
//            "       \"CRS\",\n"                         +
//            "       width, height, depth)\n"            +
//            "  VALUES (?, ?, 'IRD:WGS84(xyt)', ?, ?, ?)");

    /**
     * Constructs a new {@code GeographicBoundingBoxTable}.
     *
     * @param  connection The connection to the database.
     * @throws SQLException if the table can't be constructed.
     */
    public GeographicBoundingBoxTable(final Database database) throws SQLException {
        super(database);
        final QueryType[] usageLW = {LIST,   INSERT};
        final QueryType[] usageRW = {SELECT, INSERT};
        name          = new Column              (query, "GridGeometries", "id",            usageRW);
        spatialExtent = new SpatialColumn.Box   (query, "GridGeometries", "spatialExtent", usageLW);
        width         = new Column              (query, "GridGeometries", "width",         usageLW);
        height        = new Column              (query, "GridGeometries", "height",        usageLW);
        depth         = new Column              (query, "GridGeometries", "depth",         usageLW);
        byExtent      = new SpatialParameter.Box(query, spatialExtent,                     usageRW);
        byWidth       = new Parameter           (query, width,                             usageRW);
        byHeight      = new Parameter           (query, height,                            usageRW);
        byDepth       = new Parameter           (query, depth,                             usageRW);
        name.setRole(Role.NAME);
    }

    /**
     * Retourne l'identifieur de l'étendue géographique et la dimension d'image spécifiées.
     * Si aucun enregistrement n'a été trouvée, alors cette méthode retourne {@code null}.
     *
     * @param  bbox The geographic bounding box.
     * @param  size The image size, in pixels.
     * @throws SQLException if the operation failed.
     */
    public synchronized String getIdentifier(final GeographicBoundingBox bbox, final Dimension size)
            throws SQLException, CatalogException
    {
        return getIdentifier(new GeneralEnvelope(bbox), size);
    }

    /**
     * Todo: revisit and make public in replacement of previous method.
     */
    private String getIdentifier(final Envelope spatialExtent, final Dimension size)
            throws SQLException, CatalogException
    {
        final PreparedStatement statement = getStatement(SELECT);
        byExtent.setEnvelope(statement, SELECT, spatialExtent);
        statement.setInt(indexOf(byWidth ), size.width);
        statement.setInt(indexOf(byHeight), size.height);
        statement.setInt(indexOf(byDepth ), 1); // TODO
        String ID = null;
        final ResultSet result = statement.executeQuery();
        while (result.next()) {
            final String nextID = result.getString(1);
            if (ID!=null && !ID.equals(nextID)) {
                final LogRecord record = Resources.getResources(getDatabase().getLocale()).
                        getLogRecord(Level.WARNING, ResourceKeys.ERROR_DUPLICATED_GEOMETRY_$1, nextID);
                record.setSourceClassName("GeographicBoundingBoxTable");
                record.setSourceMethodName("getIdentifier");
                Element.LOGGER.log(record);
            } else {
                ID = nextID;
            }
        }
        result.close();
        return ID;
    }

    /**
     * Ajoute une entrée pour l'étendue géographique et la dimension d'image spécifiée.
     *
     * @todo Not yet implemented.
     */
    @Deprecated
    public synchronized void addEntry(final String          identifier,
                                      final GeographicBoundingBox bbox,
                                      final Dimension             size)
            throws CatalogException, SQLException
    {
        if (true) {
            throw new CatalogException("Not yet implemented.");
        }
        final PreparedStatement statement = getStatement(INSERT);
        statement.setString(1, identifier);
//        setBoundingBox(statement, 1, bbox, size);
        if (statement.executeUpdate() != 1) {
            throw new CatalogException("L'étendue géographique n'a pas été ajoutée.");
        }
    }
}
