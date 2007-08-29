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
package net.sicade.observation.sql;

// J2SE dependencies
import java.sql.Types;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.Observable;

// Geotools dependencies
import org.geotools.resources.Utilities;

// Sicade dependencies
import net.sicade.catalog.ConfigurationKey;
import net.sicade.catalog.Database;
import net.sicade.catalog.CatalogException;
import net.sicade.coverage.model.Distribution;
import net.sicade.observation.SamplingFeature;
import net.sicade.observation.Measurement;
import org.opengis.metadata.quality.DataQuality;
import org.opengis.observation.Phenomenon;
import org.opengis.observation.Process;



/**
 * Connexion vers la table des {@linkplain Measurement mesures}.
 *
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 *
 * @see MergedMeasurementTable
 * @see net.sicade.observation.coverage.MeasurementTableFiller
 */
@Deprecated
public class MeasurementTable extends ObservationTable<Measurement> {
    /**
     * Requête SQL pour obtenir les mesures pour une station et un observable donnés.
     */
    private static final ConfigurationKey SELECT = null; // new ConfigurationKey("Measurements:SELECT",
//            "SELECT station, observable, value, error\n"  +
//            "  FROM \"Measurements\"\n"                   +
//            " WHERE (station = ?) AND (observable = ?)");

    /**
     * Requête SQL pour insérer les mesures pour une station et un observable donnés.
     */
    private static final ConfigurationKey INSERT = null; // new ConfigurationKey("Measurements:INSERT",
//            "INSERT INTO \"Measurements\" (station, observable, value, error)\n"  +
//            "VALUES (?, ?, ?, ?)");

    /** Numéro de colonne. */ private static final int VALUE = 6;
    /** Numéro de colonne. */ private static final int ERROR = 7;

    /**
     * La clé désignant la requête à utiliser pour ajouter des valeurs.
     */
    private final ConfigurationKey insert;

    /**
     * Construit une nouvelle connexion vers la table des mesures.
     */
    public MeasurementTable(final Database database) {
        this(database, SELECT, INSERT);
    }

    /** 
     * Construit une nouvelle connexion vers la table des mesures.
     * 
     * @param  database Connexion vers la base de données des observations.
     * @param  select   Clé de la requête SQL à utiliser pour obtenir des valeurs.
     * @param  insert   Clé de la requête SQL à utiliser pour ajouter des valeurs,
     *                  ou {@code null} si les insertions ne sont pas supportées.
     */
    protected MeasurementTable(final Database       database,
                               final ConfigurationKey select,
                               final ConfigurationKey insert)
    {
        super(database, select);
        this.insert = insert;
    }

    /** 
     * Construit une nouvelle connexion vers la table des mesures pour les stations spécifiées.
     * 
     * @param  stations La table des stations à utiliser.
     * @param  select   Clé de la requête SQL à utiliser pour obtenir des valeurs.
     * @param  insert   Clé de la requête SQL à utiliser pour ajouter des valeurs,
     *                  ou {@code null} si les insertions ne sont pas supportées.
     */
    protected MeasurementTable(final SamplingFeatureTable   stations,
                               final ConfigurationKey select,
                               final ConfigurationKey insert)
    {
        super(stations, select);
        this.insert = insert;
    }

    /**
     * Construit une mesure pour l'enregistrement couran
     */
    @Override
    protected Measurement createEntry(final SamplingFeature featureOfInterest,
                                      final Phenomenon      observedProperty,
                                      final Process         procedure,
                                      final Distribution    distribution,
                                      final DataQuality  quality,
                                      final ResultSet       result) throws SQLException
    {
        float value = result.getFloat(VALUE); if (result.wasNull()) value=Float.NaN;
        float error = result.getFloat(ERROR); if (result.wasNull()) error=Float.NaN;
        return new MeasurementEntry(featureOfInterest, observedProperty, procedure, distribution, quality, value, error);
    }

    /**
     * Définie une valeur réelle pour la station et l'observable courant.
     *
     * @param  value Valeur à inscrire dans la base de données.
     * @param  error Une estimation de l'erreur, ou {@link Float#NaN} s'il n'y en a pas.
     * @throws CatalogException si la station ou le descripteur spécifié n'existe pas.
     * @throws SQLException si la mise à jour de la base de données a échoué pour une autre raison.
     */
    public synchronized void setValue(final float value, final float error) throws CatalogException, SQLException {
        if (insert == null) {
            throw new CatalogException("La table \"" + Utilities.getShortClassName(this) +
                                       "\" n'est pas modifiable.");
        }
        final SamplingFeature station = getFeatureOfInterest();
        if (station == null) {
            throw new CatalogException("La station doit être définie.");
        }
        
        if (Float.isNaN(value)) {
            return;
        }
        final PreparedStatement statement = getStatement(getProperty(insert));
        statement.setInt  (STATION,    station.getNumericIdentifier());
        
        statement.setFloat(VALUE, value);
        if (!Float.isNaN(error)) {
            statement.setFloat(ERROR, error);
        } else {
            statement.setNull(ERROR, Types.FLOAT);
        }
        final int count = statement.executeUpdate();
        if (count != 1) {
            Measurement.LOGGER.warning(count + " valeurs ajoutées pour \" \" à la station " + station);
        }
    }
}
