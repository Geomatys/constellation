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
package net.sicade.swe;

import net.sicade.catalog.Entry;

/**
 * Une reference decrivant un resultat pour une ressource MIME externe.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class ReferenceEntry extends Entry implements Reference{
    
    /**
     * L'identifiant de la reference.
     */
    private String id;
    
    /**
     * Créé une nouvelle reference.
     */
    public ReferenceEntry(String id) {
        super(id);
        this.id = id;
    }

    /**
     * retourne l'identifiant de la reference.
     */
    public String getId() {
        return id;
    }
    
}
