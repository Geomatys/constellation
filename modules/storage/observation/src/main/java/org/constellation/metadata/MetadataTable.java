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
package org.constellation.metadata;

// J2SE dependencies
import java.util.Map;
import java.util.HashMap;
import java.sql.SQLException;
import org.constellation.catalog.*;

// OpenGIS dependencies
import org.geotoolkit.metadata.MetadataStandard;
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.quality.DataQuality;

// Geotools dependencies
import org.geotoolkit.metadata.sql.MetadataSource;
import org.geotoolkit.metadata.iso.citation.DefaultCitation;


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
        super(new Query(database, "metadata"));
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
                source = new MetadataSource(MetadataStandard.ISO_19115, null, "metadata"); // TODO: provide a data source.
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
                candidate = type.cast(new DefaultCitation((Citation) candidate));
            }
            p.put(identifier, candidate);
        }
        return candidate;
    }
}
