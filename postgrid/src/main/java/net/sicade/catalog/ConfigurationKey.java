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
package net.sicade.catalog;

import java.util.Map;
import java.util.HashMap;
import java.io.Serializable;


/**
 * Clé désignant un aspect d'une configuration d'une base de données. Une clé peut désigner
 * par exemple le pilote JDBC à utiliser pour la connexion, ou une des requête SQL utilisée.
 * Un ensemble de clés sont prédéfinies comme variables statiques dans les implémentations
 * côté serveur des différentes interfaces. Les valeurs correspondantes peuvent être modifiées
 * par {@link net.sicade.gui.swing.ConfigurationEditor}.
 *
 * @version $Id$
 * @author Remi Eve
 * @author Martin Desruisseaux
 */
public class ConfigurationKey implements Serializable {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = 3068136990916953221L;

    /**
     * Ensemble des clés déjà créées dans cette machine virtuelle. Chaque clé est créée une fois
     * pour toute et ajoutée à cet ensemble. La création des clés se fait habituellement lors de
     * l'initialisation des classes qui les contient comme variables statiques.
     */
    private static final Map<String,ConfigurationKey> POOL = new HashMap<String,ConfigurationKey>();

    /**
     * Le nom de la clé courante. Chaque clé a un nom unique.
     */
    private final String name;

    /**
     * Valeur par défaut associée à la clé. Cette valeur est codée en dur dans les implémentations
     * qui définissent des clés comme variables statiques. Cette valeur par défaut ne sera utilisée
     * que si l'utilisateur n'a pas spécifié explicitement de valeur dans le fichier de configuration.
     */
    private final String defaultValue;

    /**
     * Construit une nouvelle clé.
     *
     * @param name          Le nom de la clé à créer. Ce nom doit être unique.
     * @param defaultValue  Valeur par défaut associée à la clé, utilisée que si l'utilisateur n'a
     *                      pas spécifié explicitement de valeur.
     *
     * @throws IllegalStateException si une clé a déjà été créée précédemment pour le nom spécifié.
     */
    public ConfigurationKey(final String name, final String defaultValue) throws IllegalStateException {
        this.name         = name.trim();
        this.defaultValue = defaultValue;
        synchronized (POOL) {
            if (POOL.put(name, this) != null) {
                throw new IllegalStateException("Doublon dans les noms de clés."); // TODO: localize
            }
        }
    }

    /**
     * Retourne le nom de la clé courante. Chaque clé a un nom unique.
     */
    public String getName() {
        return name;
    }

    /**
     * Retourne la valeur par défaut associée à la clé. Cette valeur est codée en dur dans
     * les implémentations qui définissent des clés comme variables statiques. Cette valeur
     * par défaut ne sera utilisée que si l'utilisateur n'a pas spécifié explicitement de
     * valeur dans le fichier de configuration.
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Retourne le nom de cette clé, pour inclusion dans une interface graphique.
     */
    @Override
    public String toString() {
        return name;
    }
}
