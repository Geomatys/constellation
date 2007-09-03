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

import java.sql.ResultSet;
import java.sql.SQLException;
import net.sicade.observation.SamplingFeatureEntry;
import org.opengis.observation.sampling.SamplingFeature;
import org.opengis.observation.Observation;
import net.sicade.catalog.CatalogException;
import net.sicade.catalog.Database;
import net.sicade.catalog.SingletonTable;
import org.opengis.observation.sampling.SurveyProcedure;



/**
 * Connexion vers la table des {@linkplain Station stations}.
 *
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
@Deprecated
public class SamplingFeatureTable extends SingletonTable<SamplingFeature> {
   
       
    /**
     * Connexion vers la table des observations.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    private ObservationTable<? extends Observation> observations;

     

    /**
     * {@code true} si l'on autorise cette classe à construire des objets {@link StationEntry}
     * qui contiennent moins d'informations, afin de réduire le nombre de requêtes SQL. Utile
     * si l'on souhaite obtenir une {@linkplain #getEntries liste de nombreuses stations}.
     */
    private boolean abridged;

    /** 
     * Construit une nouvelle connexion vers la table des stations.
     */
    public SamplingFeatureTable(final Database database) {
        super(new SamplingFeatureQuery(database));
    }

   
    /**
     * Définie la table des observations à utiliser. Cette méthode peut être appelée par
     * {@link ObservationTable} avant toute première utilisation de {@code StationTable}.
     *
     * @param  platforms Table des observations à utiliser.
     * @throws IllegalStateException si cette instance utilise déjà une autre table des observations.
     */
    protected synchronized void setObservationTable(final ObservationTable<? extends Observation> observations)
            throws IllegalStateException
    {
        if (this.observations != observations) {
            if (this.observations != null) {
                throw new IllegalStateException();
            }
            this.observations = observations; // Doit être avant tout appel de setTable(this).
            observations.setStationTable(this);
        }
    }

   
    /**
     * Retourne la table des observations à utiliser pour la création des objets {@link StationEntry}.
     */
    public ObservationTable<? extends Observation> getObservationTable() {
        assert Thread.holdsLock(this);
        if (observations == null) {
            setObservationTable(getDatabase().getTable(MeasurementTable.class));
        }
        return observations;
    }
   
    /**
     * Indique si cette table est autorisée à construire des objets {@link Station}
     * qui contiennent moins d'informations. Cet allègement permet de réduire le nombre de
     * requêtes SQL, ce qui peut accélérer l'obtention d'une {@linkplain #getEntries liste
     * de nombreuses stations}.
     *
     * @see #setAbridged
     */
    public final boolean isAbridged() {
        return abridged;
    }

    /**
     * Spécifie si cette table est autorisée à construire des objets {@link Station}
     * qui contiennent moins d'informations. Cet allègement permet de réduire le nombre de
     * requêtes SQL, ce qui peut accélérer l'obtention d'une {@linkplain #getEntries liste
     * de nombreuses stations}.
     *
     * @see #isAbridged
     */
    public synchronized void setAbridged(final boolean abridged) {
        if (abridged != this.abridged) {
            this.abridged = abridged;
            clearCache();
            fireStateChanged("abridged");
        }
    }

    /**
     * Configure la requête SQL spécifiée en fonction de la {@linkplain #getPlatform plateforme
     * courante} de cette table. Cette méthode est appelée automatiquement lorsque cette table a
     * {@linkplain #fireStateChanged changé d'état}.
     
    @Override
    protected void configure(final QueryType type, final PreparedStatement statement) throws SQLException {
        super.configure(type, statement);
        switch (type) {
            case LIST: {
                final String name = (platform != null) ? platform.getName() : null;
                statement.setString(ARGUMENT_PLATFORM, name);
                break;
            }
        }
    }
    */
    
    /**
     * Construit une station pour l'enregistrement courant. L'implémentation par défaut extrait une
     * première série d'informations telles que le {@linkplain Station#getName nom de la station},
     * {@linkplain Station#getProvider son fournisseur}, <cite>etc.</cite> et appele la méthode
     * <code>{@linkplain #createEntry(int,String,Platform,DataQuality,Citation,ResultSet)
     * createEntry}(name, identifier, ...)</code> avec ces informations.
     */
    protected SamplingFeature createEntry(final ResultSet result) throws CatalogException, SQLException {
        final SamplingFeatureQuery query = (SamplingFeatureQuery) super.query;
        return new SamplingFeatureEntry(results.getString(indexOf(query.name   )),
                                       results.getString(indexOf(query.remarks)));
        
    }

    /**
     * Construit une station à partir des informations spécifiées. Cette méthode est appelée
     * automatiquement par {@link #createEntry(ResultSet)} après avoir extrait les informations
     * communes à tous les types de stations. L'implémentation par défaut ne fait que construire
     * un objet {@link StationEntry} sans extraire davantage d'informations. Les classes dérivées
     * devraient redéfinir cette méthode si elles souhaitent construire un type de station plus
     * élaboré.
     *
     * @param table      La table qui a produit cette entrée.
     * @param identifier L'identifiant numérique de la station.
     * @param name       Le nom de la station.
     * @param coordinate Une coordonnée représentative en degrés de longitude et de latitude,
     *                   ou {@code null} si inconue.
     * @param timeRange  Plage de temps de cet élément, ou {@code null} si inconue.
     * @param platform   La plateforme (par exemple un bateau) sur laquelle a été prise cette
     *                   station, ou {@code null} si inconnue.
     * @param quality    La qualité de la donnée, ou {@code null} si inconnue.
     * @param provider   La provenance de la donnée, ou {@code null} si inconnue.
     * @param result     La ligne courante de la requête SQL. A utiliser seulement si les sous-classes
     *                   ont besoin d'extraire davantage d'informations que celles qui ont été fournies
     *                   par les arguments précédents.
     *
     * @throws SQLException si un accès à la base de données était nécessaire et a échoué.
     */
    protected SamplingFeature createEntry(final int          identifier,
                                          final String       name,
                                          final SurveyProcedure surveyDetail)
            throws SQLException
    {
        return new SamplingFeatureEntry(this, identifier, name, surveyDetail);
    }

    /**
     * Indique si la méthode {@link #getEntries} devrait accepter la station spécifiée.
     * L'implémentation par défaut vérifie si le {@linkplain Station#getProvider fournisseur}
     * est l'un de ceux qui ont été spécifiés à la méthode {@link #acceptableProvider(Citation)
     * acceptableProvider}. Si la station ne donne pas d'indication sur le fournisseur, alors
     * cette méthode va l'accepter comme approche conservative.
     */
    @Override
    protected boolean accept(final SamplingFeature entry) throws CatalogException, SQLException {
        
        return super.accept(entry);
    }
}
