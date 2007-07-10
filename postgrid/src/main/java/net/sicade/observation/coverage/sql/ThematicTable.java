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

// Sicade dependencies
import net.sicade.observation.coverage.Layer;
import net.sicade.observation.coverage.Thematic;
import net.sicade.observation.ConfigurationKey;
import net.sicade.observation.sql.Table;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.QueryType;
import net.sicade.observation.sql.Shareable;
import net.sicade.observation.sql.SingletonTable;
import net.sicade.observation.IllegalRecordException;


/**
 * Connexion vers la table des {@linkplain Thematic thèmes} traités par les
 * {@linkplain Layer couches}.
 * 
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public class ThematicTable extends SingletonTable<Thematic> implements Shareable {
    /**
     * Requête SQL pour obtenir un thème.
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("Thematics:SELECT",
            "SELECT name, description\n" +
            "  FROM \"Thematics\"\n"     +
            " WHERE name=?");

    /** Numéro de colonne. */ private static final int NAME    = 1;
    /** Numéro de colonne. */ private static final int REMARKS = 2;

    /**
     * Une instance unique de la table des séries. Sera créée par {@link #getSeriesTable} la
     * première fois où elle sera nécessaire. <strong>Note:</strong> on évite de déclarer explicitement
     * le type {@link SeriesTable} afin d'éviter de charger les classes correspondantes trop tôt.
     */
    private transient Table series;

    /**
     * Construit une table des thèmes.
     * 
     * @param  database Connexion vers la base de données.
     */
    public ThematicTable(final Database database) {
        super(database);
    }

    /**
     * Retourne la requête SQL à utiliser pour obtenir les thèmes.
     */
    @Override
    protected String getQuery(final QueryType type) throws SQLException {
        switch (type) {
            case SELECT: return getProperty(SELECT);
            default:     return super.getQuery(type);
        }
    }

    /**
     * Construit un thème pour l'enregistrement courant.
     */
    protected Thematic createEntry(final ResultSet results) throws SQLException {
        return new ThematicEntry(results.getString(NAME), results.getString(REMARKS));
    }

    /**
     * Retourne une instance unique de la table des séries. Cette méthode est réservée à un
     * usage strictement interne par {@link LayerTable}. En principe, les {@link SeriesTable}
     * ne sont pas {@linkplain Shareable partageable} car elle possèdent une méthode {@code set}.
     * Dans le cas particulier de {@link LayerTable} toutefois, toutes les utilisations de
     * {@link SeriesTable} se font à l'intérieur d'un bloc synchronisé, de sorte qu'une
     * instance unique suffit.
     *
     * @param  type Doit obligatoirement être {@code SeriesTable.class}.
     * @return La table des séries.
     */
    final synchronized <T extends Table> T getTable(final Class<T> type) {
        if (series == null) {
            series = database.getTable(type);
        }
        return type.cast(series);
    }
}
