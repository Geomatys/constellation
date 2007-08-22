/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2006, Institut de Recherche pour le Développement
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sicade.observation.fishery.sql;

// Sicade dependencies
import net.sicade.observation.sql.ProcessEntry;
import net.sicade.observation.fishery.FisheryType;


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
