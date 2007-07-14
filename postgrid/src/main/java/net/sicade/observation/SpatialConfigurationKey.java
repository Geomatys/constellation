/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2007, Geomatys
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


/**
 * Clé désignant un aspect d'une configuration d'une base de données spatiale. En plus de la
 * {@linkplain #getDefaultValue valeur par défaut} habituelle, cette clée contient une valeur
 * par défaut pour les bases de données supportant l'extension spatiale. Un exemple est
 * l'extension spatiale PostGIS pour PostgreSQL.
 *
 * @version $Id$
 * @author Cédric Briançon
 * @author Martin Desruisseaux
 */
@Deprecated
public class SpatialConfigurationKey extends ConfigurationKey {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = 8982886731478829196L;

    /**
     * La valeur de la clé pour les bases de données supportant l'extension spatiale.
     */
    private final String spatialValue;

    /**
     * Construit une nouvelle clé spatiale.
     *
     * @param name          Le nom de la clé à créer. Ce nom doit être unique.
     * @param defaultValue  Valeur par défaut associée à la clé, utilisée que si l'utilisateur n'a
     *                      pas spécifié explicitement de valeur.
     * @param spatialValue  Valeur par défaut is la base de données supporte une extension spatiale.
     */
    public SpatialConfigurationKey(final String name, final String defaultValue, final String spatialValue) {
        super(name, defaultValue);
        this.spatialValue = spatialValue;
    }

    /**
     * Retourne la valeur de la clé pour les bases de données supportant l'extension spatiale.
     */
    public String getSpatialValue() {
        return spatialValue;
    }
}
