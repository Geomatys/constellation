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
 * Enregistrement permettant de regrouper plusieur type de resultat en un meme type.
 * (implementation decrivant une classe union) hormis l'identifiant, 
 * il ne doit y avoir qu'un attibut differend de {@code null}. 
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class AnyResultEntry extends Entry {
    
    /**
     * Lidentifiant du resultat.
     */
    private int id;
    
    /**
     * Le resultat peut etre de type Reference.
     */
    private Reference reference;
    
    /**
     * Le resultat peut être de type DataBlockDefinition.
     */
    private DataBlockDefinition dataBlockDefinition;
    
    /**
     * créé un nouveau resultat en specifiant son type.
     *
     * @param id l'identifiant du resultat.
     * @param reference l'identifiant de la reference si le resultat en est une, {@code null} sinon.
     * @param dataBlockDefinition l'identifiant du dataBlock si le resultat en est un, {@code null} sinon.
     */
    public AnyResultEntry(int id, Reference reference, DataBlockDefinition dataBlockDefinition) {
        super(null);
        this.id = id;
        this.reference = reference;
        this.dataBlockDefinition = dataBlockDefinition;
    }
    
}
