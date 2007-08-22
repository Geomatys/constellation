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

// Sicade dependencies
import net.sicade.observation.PropertyType;
import net.sicade.catalog.Entry;


/**
 * Implémentation d'une entrée représentant un {@linkplain Phenomenon phénomène}.
 * 
 * @version $Id$
 * @author Antoine Hnawia
 */
public class PropertyTypeEntry extends Entry implements PropertyType {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = 5140595674231914861L;

    /**
     * Construit un nouveau phénomène du nom spécifié.
     *
     * @param name Le nom du phénomène.
     */
    protected PropertyTypeEntry(final String name) {
        super(name);
    }

    /** 
     * Construit un nouveau phénomène du nom spécifié.
     *
     * @param name    Le nom du phénomène.
     * @param remarks Remarques s'appliquant à ce phénomène, ou {@code null}.
     */
    protected PropertyTypeEntry(final String name, final String remarks) {
        super(name, remarks);
    }
}
