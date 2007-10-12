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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import net.sicade.catalog.Entry;
import net.sicade.gml.ReferenceEntry;

/**
 * Enregistrement permettant de regrouper plusieur type de resultat en un meme type.
 * (implementation decrivant une classe union) hormis l'identifiant, 
 * il ne doit y avoir qu'un attibut differend de {@code null}. 
 *
 * @version $Id:
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Any")
public class AnyResultEntry extends Entry {
    
    /**
     * Lidentifiant du resultat.
     */
    @XmlAttribute
    private String id;
    
    /**
     * Le resultat peut etre de type Reference.
     */
    private ReferenceEntry reference;
    
    /**
     * Le resultat peut être un bloc de donnée.
     */
    private String dataBlock;
    
    /**
     * Constructeur utilisé par jaxB
     */
    public AnyResultEntry(){}
    
    /**
     * créé un nouveau resultat en specifiant son type.
     *
     * @param id l'identifiant du resultat.
     * @param reference l'identifiant de la reference si le resultat en est une, {@code null} sinon.
     * @param dataBlockDefinition l'identifiant du dataBlock si le resultat en est un, {@code null} sinon.
     */
    public AnyResultEntry(String id, ReferenceEntry reference, String dataBlock) {
        super(null);
        this.id = id;
        this.reference = reference;
        this.dataBlock = dataBlock;
    }

    /**
     * Retourne l'identifiant du resultat
     */
    public String getId() {
        return id;
    }

    /**
     * retourne un resultat de type reference si s'en est un, {@code null} sinon.
     */
    public ReferenceEntry getReference() {
        return reference;
    }

    /**
     * retourne un resultat de type dataBlockDefinition si s'en est un, {@code null} sinon.
     */
    public String getDataBlock() {
        return dataBlock;
    }
    
    /**
     * retourne une chaine de caractere decrivant le resultat (debug)
     * 
     */
    @Override
    public String toString() {
        String res;
        if (reference == null)
            res = dataBlock;
        else
            res = reference.toString();
        
        return "id = " + id + " value/idref: " + res;
    }
}
