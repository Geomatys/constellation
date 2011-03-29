/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.sampling;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import org.geotoolkit.internal.sql.table.CatalogException;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.internal.sql.table.QueryType;
import org.geotoolkit.internal.sql.table.SingletonTable;
import org.geotoolkit.sampling.xml.v100.SamplingFeatureType;
import org.geotoolkit.gml.xml.v311.FeaturePropertyType;
import org.geotoolkit.internal.sql.table.LocalCache;
import org.geotoolkit.internal.sql.table.LocalCache.Stmt;

/**
 * Connexion vers la table des {@linkplain Station stations}.
 *
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 * @author Guilhem Legal
 */
public class SamplingFeatureTable extends SingletonTable<SamplingFeatureType> implements Cloneable {
   
       
    /**
     * {@code true} si l'on autorise cette classe à construire des objets {@link StationType}
     * qui contiennent moins d'informations, afin de réduire le nombre de requêtes SQL. Utile
     * si l'on souhaite obtenir une {@linkplain #getEntries liste de nombreuses stations}.
     */
    private boolean abridged;

    /** 
     * Construit une nouvelle connexion vers la table des stations.
     */
    public SamplingFeatureTable(final Database database) {
        this(new SamplingFeatureQuery(database));
    }
    
    /**
     * Initialise l'identifiant de la table.
     */
    private SamplingFeatureTable(final SamplingFeatureQuery query) {
        super(query, query.byIdentifier);
    }

    /**
     * Construit une nouvelle table non partagée
     */
    private SamplingFeatureTable(final SamplingFeatureTable table) {
        super(table);
    }

    /**
     * Returns a copy of this table. This is a copy constructor used for obtaining
     * a new instance to be used concurrently with the original instance.
     */
    @Override
    protected SamplingFeatureTable clone() {
        return new SamplingFeatureTable(this);
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
            //clearCache();
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
    @Override
    protected SamplingFeatureType createEntry(final LocalCache lc, final ResultSet result, Comparable<?> identifier) throws CatalogException, SQLException {
        final SamplingFeatureQuery query = (SamplingFeatureQuery) super.query;
        // TODO result.getString(indexOf(query.sampledFeature))
        return new SamplingFeatureType(result.getString(indexOf(query.identifier)),
                                        result.getString(indexOf(query.name)),
                                        result.getString(indexOf(query.description)),
                                        new FeaturePropertyType(result.getString(indexOf(query.sampledFeature))));
        
    }

   

    /**
     * Indique si la méthode {@link #getEntries} devrait accepter la station spécifiée.
     * L'implémentation par défaut vérifie si le {@linkplain Station#getProvider fournisseur}
     * est l'un de ceux qui ont été spécifiés à la méthode {@link #acceptableProvider(Citation)
     * acceptableProvider}. Si la station ne donne pas d'indication sur le fournisseur, alors
     * cette méthode va l'accepter comme approche conservative.
     
    @Override
    protected boolean accept(final SamplingFeatureType entry) throws CatalogException, SQLException {
        
        return super.accept(entry);
    }*/
    
    /**
     * Retourne un nouvel identifier (ou l'identifier de la station passée en parametre si non-null)
     * et enregistre la nouvelle station dans la base de donnée.
     *
     * @param result le resultat a inserer dans la base de donnée.
     */
    public String getIdentifier(final SamplingFeatureType station) throws SQLException, CatalogException {
        final SamplingFeatureQuery query  = (SamplingFeatureQuery) super.query;
        String id;
        boolean success = false;
        final LocalCache lc = getLocalCache();
        synchronized (lc) {
            transactionBegin(lc);
            try {
                if (station.getId() != null) {
                    final Stmt statement = getStatement(lc, QueryType.EXISTS);
                    statement.statement.setString(indexOf(query.identifier), station.getId());
                    final ResultSet result = statement.statement.executeQuery();
                    if(result.next()) {
                        success = true;
                        result.close();
                        release(lc, statement);
                        return station.getId();
                    } else {
                        id = station.getId();
                    }
                    result.close();
                    release(lc, statement);
                } else {
                    id = searchFreeIdentifier(lc, "station");
                }

                final Stmt statement = getStatement(lc, QueryType.INSERT);
                statement.statement.setString(indexOf(query.identifier), id);

                if (station.getDescription() != null) {
                    statement.statement.setString(indexOf(query.description), station.getDescription());
                } else {
                    statement.statement.setNull(indexOf(query.description), java.sql.Types.VARCHAR);
                }

                statement.statement.setString(indexOf(query.name), station.getName());
                final Iterator i = station.getSampledFeatures().iterator();
                statement.statement.setString(indexOf(query.sampledFeature), (String)i.next());

                updateSingleton(statement.statement);
                release(lc, statement);
                success = true;
            } finally {
                transactionEnd(lc, success);
            }
        }
        return id;
    }
}
