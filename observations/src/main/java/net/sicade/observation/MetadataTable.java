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

// J2SE dependencies
import java.util.Map;
import java.util.HashMap;
import java.sql.SQLException;
import net.sicade.catalog.*;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.quality.DataQuality;

// Geotools dependencies
import org.geotools.metadata.sql.MetadataSource;
import org.geotools.metadata.iso.citation.CitationImpl;
import org.geotools.metadata.iso.quality.DataQualityImpl;


/**
 * Connexion vers la table des méta-données. Cette connexion cache les requêtes précédentes
 * par des références fortes. Nous supposons que les méta-données sont assez peu nombreuses
 * et n'ont pas besoin d'un mécanisme utilisant des références faibles.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo Le mécanisme de cache serait plus efficace à l'intérieur de {@link MetadataSource}.
 */
@Deprecated
public class MetadataTable extends Table {
    /**
     * Connexion vers la base des méta-données.
     */
    private MetadataSource source;

    /**
     * Ensemble des méta-données qui ont déjà été créées.
     */
    private final Map<Class, Map<String,Object>> pool = new HashMap<Class, Map<String,Object>>();

    /**
     * Construit une connexion vers la table des méta-données.
     * 
     * @param  database Connexion vers la table des plateformes qui utilisera cette table des stations.
     */
    public MetadataTable(final Database database) {
        super(new Query(database));
    }

    /**
     * Retourne la méta-données correspondant à l'identifiant spécifié.
     *
     * @param type Le type de méta-donnée (par exemple <code>{@linkplain Citation}.class</code>).
     * @param identifier L'identifiant de la méta-donnée désirée.
     *
     * @throws SQLException si l'accès à la base de données a échoué.
     */
    public synchronized <T> T getEntry(final Class<T> type, final String identifier) throws SQLException {
        Map<String,Object> p = pool.get(type);
        if (p == null) {
            p = new HashMap<String,Object>();
            pool.put(type, p);
        }
        T candidate = type.cast(p.get(identifier));
        if (candidate == null) {
            if (source == null) {
                source = new MetadataSource(getDatabase().getConnection());
            }
            candidate = type.cast(source.getEntry(type, identifier));
            /*
             * Extrait immédiatement les informations les plus utilisées telles que les titres
             * des citations, afin d'éviter des connexions trop fréquentes à la base de données
             * (par exemple chaque fois que l'on veut vérifier le fournisseur pour savoir si une
             * station doit être inclue dans une liste).
             */
            if (candidate instanceof DataQuality) {
                // TODO
            }
            if (candidate instanceof Citation) {
                candidate = type.cast(new CitationImpl((Citation) candidate));
            }
            p.put(identifier, candidate);
        }
        return candidate;
    }
}
