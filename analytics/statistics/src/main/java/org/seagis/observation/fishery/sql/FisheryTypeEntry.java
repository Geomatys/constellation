/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2006, Institut de Recherche pour le Développement
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
package net.seagis.observation.fishery.sql;

// Sicade dependencies
import net.seagis.observation.ProcessEntry;
import net.seagis.observation.fishery.FisheryType;


/**
 * Implémentation d'une entrée représentant un {@linkplain FisheryType type de pêche}.
 * 
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class FisheryTypeEntry extends ProcessEntry implements FisheryType {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = 6718082896471037388L;

    /**
     * Crée une nouvelle entrée du nom spécifié.
     */
    public FisheryTypeEntry(final String name, final String remarks) {
        super(name, remarks);
    }
}
