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
import java.util.Collection;
import java.awt.geom.Point2D;
import java.sql.SQLException;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.quality.DataQuality;

// Sicade dependencies
import net.sicade.util.DateRange;
import net.sicade.observation.SamplingFeature;
import net.sicade.observation.SamplingFeatureCollection;
import net.sicade.observation.Observable;
import net.sicade.observation.Observation;
import net.sicade.catalog.ServerException;
import net.sicade.catalog.CatalogException;


/**
 * Implémentation d'une entrée représentant une {@link Station station}.
 *
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 *
 * @todo L'implémentation actuelle n'est pas <cite>serializable</cite> du fait qu'elle nécessite
 *       une connexion à la base de données. Une version future devrait rétablir la connexion au
 *       moment de la <cite>deserialization</cite>.
 */
public class SamplingFeatureEntry extends LocatedEntry implements SamplingFeature {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = 8822736167506306189L;

    /**
     * L'identifiant numérique de la station.
     */
    private final int identifier;

    /**
     * La plateforme (par exemple un bateau) sur laquelle a été prise cette station.
     * Peut être nul si cette information n'est pas disponible.
     */
    private final SamplingFeatureCollection platform;

    /**
     * La qualité de la donnée. Peut être nul si cette information n'est pas disponible.
     */
    private final DataQuality quality;

    /**
     * La provenance de la donnée. Peut être nul si cette information n'est pas disponible.
     */
    private final Citation provider;

    /**
     * Connexion vers la table des observations. Contrairement à la plupart des autres
     * entrées du paquet {@code net.sicade.observation}, les observations ne seront pas
     * conservées dans une cache car elle sont potentiellement très nombreuses. Il nous
     * faudra donc conserver la connexion en permanence.
     */
    private final ObservationTable<? extends Observation> table;

    /** 
     * Construit une entrée pour l'identifiant de station spécifié.
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
     */
    protected SamplingFeatureEntry(final SamplingFeatureTable table,
                           final int          identifier,
                           final String       name,
                           final Point2D      coordinate,
                           final DateRange    timeRange,
                           final SamplingFeatureCollection     platform,
                           final DataQuality  quality,
                           final Citation     provider)
    {
        super((table.isAbridged() && coordinate!=null && timeRange!=null) ? null :
               table.getLocationTable(), name, coordinate, timeRange);
        this.identifier = identifier;
        this.platform   = platform;
        this.quality    = quality;
        this.provider   = provider;
        this.table      = table.getObservationTable();
    }

    /**
     * {@inheritDoc}
     */
    public int getNumericIdentifier() {
        return identifier;
    }

    /**
     * {@inheritDoc}
     */
    public Citation getProvider() {
        return provider;
    }
    
    /**
     * {@inheritDoc}
     */
    public SamplingFeatureCollection getPlatform() {
        return platform;
    }

    /**
     * {@inheritDoc}
     */
    public DataQuality getQuality() {
        return quality;
    }

    /**
     * {@inheritDoc}
     
    public Observation getObservation(final Observable observable) getRelatedObservationException {
        try {
            synchronized (table) {
                table.setStation(this);
                table.setObservable(observable);
                return table.getEntry();
            }
        } catch (SQLException exception) {
            throw new ServerException(exception);
        }
    }
    */

    /**
     * {@inheritDoc}
     */
    public Collection<? extends Observation> getRelatedObservations() throws CatalogException {
        try {
            synchronized (table) {
                table.setStation   (this);
                //table.setObservable(null);
                return table.getEntries();
            }
        } catch (SQLException exception) {
            throw new ServerException(exception);
        }
    }

    /**
     * Retourne le code numérique identifiant cette entrée.
     */
    @Override
    public int hashCode() {
        return identifier;
    }

    /**
     * Vérifie que cette station est identique à l'objet spécifié
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final SamplingFeatureEntry that = (SamplingFeatureEntry) object;
            return                 (this.identifier == that.identifier) &&
                   Utilities.equals(this.platform,     that.platform)   &&
                   Utilities.equals(this.quality,      that.quality)    &&
                   Utilities.equals(this.provider,     that.provider);
        }
        return false;
    }
}
