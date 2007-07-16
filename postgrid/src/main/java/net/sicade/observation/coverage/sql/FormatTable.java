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
package net.sicade.observation.coverage.sql;

// J2SE dependencies
import java.sql.ResultSet;
import java.sql.SQLException;
import net.sicade.observation.IllegalRecordException;

// Seagis dependencies
import net.sicade.observation.coverage.Format;
import net.sicade.observation.CatalogException;
import net.sicade.observation.sql.Column;
import net.sicade.observation.sql.SingletonTable;
import net.sicade.observation.sql.Shareable;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.Parameter;
import net.sicade.observation.sql.Role;
import net.sicade.observation.sql.UsedBy;
import net.sicade.observation.sql.Use;
import net.sicade.observation.sql.QueryType;
import static net.sicade.observation.sql.QueryType.*;


/**
 * Connexion vers une table des {@linkplain Format formats d'images}. Cette table construit
 * des objets {@link Format} pour un nom spécifié. Ces formats sont utilisés pour le décodage
 * d'objets {@link org.geotools.coverage.grid.GridCoverage2D}.
 * <p>
 * Cette table est utilisée par {@link GridCoverageTable}, qui construit des objets
 * de plus haut niveau.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
@Use(SampleDimensionTable.class)
@UsedBy({SeriesTable.class, GridCoverageTable.class})
public class FormatTable extends SingletonTable<Format> implements Shareable {
    /**
     * Column name declared in the {@linkplain #query query}.
     */
    private final Column name, mime, type;

    /**
     * Parameter declared in the {@linkplain #query query}.
     */
    private final Parameter byName;

    /**
     * Connexion vers la table des bandes.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    private SampleDimensionTable bands;

    /**
     * Construit une table en utilisant la connexion spécifiée.
     *
     * @param  database Connexion vers la base de données d'observations.
     */
    public FormatTable(final Database database) {
        super(database);
        final QueryType[] usage = {SELECT, LIST};
        name   = new Column   (query, "Formats", "name", usage);
        mime   = new Column   (query, "Formats", "mime", usage);
        type   = new Column   (query, "Formats", "type", usage);
        byName = new Parameter(query, name, SELECT);
        name.setRole(Role.NAME);
        name.setOrdering("ASC");
    }

    /**
     * Construit un format pour l'enregistrement courant.
     */
    protected Format createEntry(final ResultSet results) throws CatalogException, SQLException {
        final String name     = results.getString(indexOf(this.name));
        final String mimeType = results.getString(indexOf(this.mime));
        final String type     = results.getString(indexOf(this.type));
        if (bands == null) {
            bands = getDatabase().getTable(SampleDimensionTable.class);
        }
        final boolean geophysics;
        if ("geophysics".equalsIgnoreCase(type)) {
            geophysics = true;
        } else if ("native".equalsIgnoreCase(type)) {
            geophysics = false;
        } else {
            final String table = results.getMetaData().getTableName(indexOf(this.type));
            throw new IllegalRecordException(table, "Type d'image inconnu: " + type);
        }
        return new FormatEntry(name, mimeType, geophysics, bands.getSampleDimensions(name));
    }
}
