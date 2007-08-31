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

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectOutputStream;
import java.util.Set;
import java.util.Collections;
import java.sql.SQLException;
import java.util.List;

// Sicade dependencies
import net.sicade.catalog.ServerException;
import net.sicade.catalog.CatalogException;
import net.sicade.catalog.Entry;

// GeoAPI dependencies
import org.opengis.observation.sampling.SamplingFeature;
import org.opengis.observation.sampling.SamplingFeatureCollection;
import org.opengis.observation.Observation;
import org.opengis.observation.sampling.SurveyProcedure;

/**
 * Implémentation d'une entrée représentant une {@link Platform plateforme}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public class SamplingFeatureCollectionEntry extends SamplingFeatureEntry implements SamplingFeatureCollection {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = 791454287176154131L;
   
    /**
     * L'ensemble des stations de cette platform . Ne sera construit que la première fois où il sera nécessaire.
     */
    private Set<SamplingFeature> member;
    
    /**
     * Connexion vers la table des stations.
     * Sera mis à {@code null} lorsqu'elle ne sera plus nécessaire.
     */
    private transient SamplingFeatureTable stations;

    /**
     * Construit une entrée pour l'identifiant de plateforme spécifié.
     *
     * @param table La table qui a produit cette entrée.
     * @param name  Le nom de la plateforme (parfois assimilé à une campagne d'échantillonage).
     */
    protected SamplingFeatureCollectionEntry(SamplingFeatureTable stations,
                                             final int identifier,
                                             final String name,
                                             final SurveyProcedure surveyDetail)
    {
        super(stations, identifier, name, surveyDetail);
        stations = stations;
    }

  
    /**
     * {@inheritDoc}
     */
    public synchronized Set<SamplingFeature> getMembers() {
        if (member == null) try {
            if (stations != null) {
                final Set<SamplingFeature> set;
                synchronized (stations) {
                    assert equals(stations.getPlatform()) : this;
                    stations.setPlatform(this);
                    set = stations.getEntries();
                }
                member = Collections.unmodifiableSet(set);
            }
        } catch (SQLException exception) {
            throw new ServerException(exception);
        }
        return member;
    }
    
   

    /**
     * Complete the station informations before serialization.
     *
     * @param  out The output stream where to serialize this object.
     * @throws IOException if the serialization failed.
     */
    protected synchronized void writeObject(final ObjectOutputStream out) throws IOException {
        if (member == null) try {
            member = getMembers();
        } catch (CatalogException exception) {
            final InvalidObjectException e = new InvalidObjectException(exception.toString());
            e.initCause(exception);
            throw e;
        }
       out.defaultWriteObject();
    }
}
