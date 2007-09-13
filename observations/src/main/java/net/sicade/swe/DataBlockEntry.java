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
 * Un bloc de données decrit par un {@linkplain DataBlockDefinition dataBlockDdefinition}
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class DataBlockEntry extends Entry{
    
    /**
     * L'identifiant du bloc de donn�es.
     */
    private String id;
    
    /**
     * Les données.
     */
    private String data;
    
    /** 
     * Créé un nouveau bloc de données.
     */
    public DataBlockEntry(String id, String data) {
        super(id);
        this.data = data;
        this.id   = id;
        
    }

    /**
     * Retourne l'identifiant du bloc de don�es.
     */
    public String getId() {
        return id;
    }

    /**
     * Retourne les donn�es
     */
    public String getData() {
        return data;
    }
    
}
