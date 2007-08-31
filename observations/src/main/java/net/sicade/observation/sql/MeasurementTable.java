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

import java.sql.SQLException;

// Sicade dependencies
import net.sicade.catalog.ConfigurationKey;
import net.sicade.catalog.Database;
import net.sicade.coverage.model.Distribution;

// OpenGis dependencies
import org.opengis.observation.Measure;
import org.opengis.observation.Measurement;
import org.opengis.observation.Phenomenon;
import org.opengis.observation.Process;
import org.opengis.observation.sampling.SamplingFeature;
import org.opengis.temporal.TemporalObject;
import org.opengis.metadata.MetaData;
import org.opengis.metadata.quality.Element;


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
     * Construit une mesure pour l'enregistrement courant
     */
    protected Measurement createEntry(final SamplingFeature featureOfInterest,
                                      final Phenomenon      observedProperty,
                                      final Process         procedure,
                                      final Distribution    distribution,
                                      final Element         quality,
                                      final Measure         result,
                                      final TemporalObject  samplingTime,
                                      final MetaData        observationMetadata,
                                      final String          resultDefinition,
                                      final TemporalObject  procedureTime,
                                      final Object          procedureParameter) throws SQLException
    {
        return new MeasurementEntry(featureOfInterest, observedProperty, procedure, distribution, quality,
                result, samplingTime, observationMetadata, resultDefinition, procedureTime, procedureParameter);
    }

}
