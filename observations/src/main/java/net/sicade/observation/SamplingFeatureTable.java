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
package net.sicade.observation;

import java.sql.ResultSet;
import java.sql.SQLException;
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
     * Initialise l'identifiant de la table.
     */
    private SamplingFeatureTable(final SamplingFeatureQuery query) {
        super(query);
        setIdentifierParameters(query.byIdentifier, null);
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
     * Construit une station pour l'enregistrement courant. L'implémentation par défaut extrait une
     * première série d'informations telles que le {@linkplain Station#getName nom de la station},
     * {@linkplain Station#getProvider son fournisseur}, <cite>etc.</cite> et appele la méthode
     * <code>{@linkplain #createEntry(int,String,Platform,DataQuality,Citation,ResultSet)
     * createEntry}(name, identifier, ...)</code> avec ces informations.
     */
    protected SamplingFeature createEntry(final ResultSet result) throws CatalogException, SQLException {
        final SamplingFeatureQuery query = (SamplingFeatureQuery) super.query;
        return new SamplingFeatureEntry(result.getString(indexOf(query.identifier)),
                                        result.getString(indexOf(query.name)),
                                        result.getString(indexOf(query.description)),
                                        result.getString(indexOf(query.sampledFeature)));
        
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
