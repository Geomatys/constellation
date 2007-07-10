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
package net.sicade.observation.coverage.sql;

// J2SE dependencies
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

// Geotools dependencies
import org.geotools.resources.Utilities;

// Sicade dependencies
import net.sicade.observation.ConfigurationKey;
import net.sicade.observation.CatalogException;
import net.sicade.observation.coverage.Format;
import net.sicade.observation.coverage.Layer;
import net.sicade.observation.coverage.Series;
import net.sicade.observation.sql.Use;
import net.sicade.observation.sql.UsedBy;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.QueryType;
import net.sicade.observation.sql.SingletonTable;


/**
 * Connexion vers la table des séries. Cette connexion est utilisée en interne par le
 * {@linkplain LayerTable table des couches}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
@Use(FormatTable.class)
@UsedBy(LayerTable.class)
public class SeriesTable extends SingletonTable<Series> {
    /**
     * Requête SQL utilisée pour obtenir une séries par son nom.
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("Series:SELECT",
        "SELECT identifier, layer, format, NULL as remarks\n"  +
        "  FROM \"Series\"\n"                                  +
        " WHERE identifier=?");

    /**
     * Requête SQL utilisée pour obtenir une liste de séries.
     */
    private static final ConfigurationKey LIST = new ConfigurationKey("Series:LIST",
        "SELECT identifier, layer, format, NULL as remarks\n"  +
        "  FROM \"Series\"\n"                                  +
        " WHERE layer LIKE ?\n"                                +
        " ORDER BY identifier");

    /** Numéro de colonne. */ private static final int NAME    = 1;
    /** Numéro de colonne. */ private static final int LAYER   = 2;
    /** Numéro de colonne. */ private static final int FORMAT  = 3;
    /** Numéro de colonne. */ private static final int REMARKS = 4;

    /**
     * Connexion vers la table des formats.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    private FormatTable formats;

    /**
     * Couche dont on veut les séries, ou {@code null} pour les prendre tous.
     */
    private Layer layer;

    /**
     * Construit une table qui interrogera la base de données spécifiée.
     *
     * @param database  Connexion vers la base de données d'observations.
     */
    public SeriesTable(final Database database) {
        super(database);
    }

    /**
     * Retourne la couche d'images dont on veut les séries, ou {@code null} si toutes les
     * séries sont retenues.
     */
    public Layer getLayer() {
        return layer;
    }

    /**
     * Définit la couche d'images dont on veut les séries. Les prochains appels de la métohdes
     * {@link #getEntries() getEntries()} ne retourneront que les séries de cette couche. La
     * valeur {@code null} fera retourner toutes les séries.
     */
    public synchronized void setLayer(final Layer layer) {
        if (!Utilities.equals(layer, this.layer)) {
            this.layer = layer;
            fireStateChanged("layer");
        }
    }

    /**
     * Retourne la requête SQL à utiliser pour obtenir les séries.
     */
    @Override
    protected String getQuery(final QueryType type) throws SQLException {
        switch (type) {
            case SELECT: return getProperty(SELECT);
            case LIST:   return getProperty(LIST);
            default:     return super.getQuery(type);
        }
    }

    /**
     * Configure la requête SQL spécifiée en fonction de la {@linkplain #getLayer couche recherchée}
     * par cette table. Cette méthode est appelée automatiquement lorsque cette table a
     * {@linkplain #fireStateChanged changé d'état}.
     */
    @Override
    protected void configure(final QueryType type, final PreparedStatement statement) throws SQLException {
        super.configure(type, statement);
        switch (type) {
            case LIST: {
                statement.setString(1, escapeSearch(layer!=null ? layer.getName() : null));
            }
        }
    }

    /**
     * Construit une série pour l'enregistrement courant.
     */
    protected Series createEntry(final ResultSet results) throws CatalogException, SQLException {
        final String name    = results.getString(NAME);
        final String remarks = results.getString(REMARKS);
        if (formats == null) {
            formats = database.getTable(FormatTable.class);
        }
        final Format format = formats.getEntry(results.getString(FORMAT));
        return new SeriesEntry(name, format, remarks);
    }
}
