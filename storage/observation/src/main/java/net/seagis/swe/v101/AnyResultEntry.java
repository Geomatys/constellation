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
package net.seagis.swe.v101;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import net.seagis.catalog.Entry;
import net.seagis.gml.v311.ReferenceEntry;

/**
 * Enregistrement permettant de regrouper plusieur type de resultat en un meme type.
 * (implementation decrivant une classe union) hormis l'identifiant, 
 * il ne doit y avoir qu'un attribut differend de {@code null}. 
 *
 * @version $Id:
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Any")
public class AnyResultEntry extends Entry {
    
    /**
     * The result identifier.
     */
    @XmlAttribute
    private String id;
    
    /**
     * The result can be a reference.
     */
    private ReferenceEntry reference;
    
    /**
     * The result can be an array.
     */
    private DataArrayPropertyType array;
    
    /**
     * Constructor used by jaxB
     */
    public AnyResultEntry(){}
    
    /**
     * build a new result with the specified reference.
     *
     * @param The result identifier.
     * @param reference The reference identifier.
     */
    public AnyResultEntry(String id, ReferenceEntry reference) {
        super(null);
        this.id = id;
        this.reference = reference;
    }
    
    /**
     * build a new result with the specified array of data.
     *
     * @param The result identifier.
     * @param reference The reference identifier.
     */
    public AnyResultEntry(String id, DataArrayEntry array) {
        super(null);
        this.id = id;
        this.array = new DataArrayPropertyType(array);
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
     * retourne un resultat de type dataArray si s'en est un, {@code null} sinon.
     */
    public DataArrayEntry getArray() {
        if (array != null) {
            return array.getDataArray();
        }
        return null;
    }
    
    /**
     * retourne un resultat de type dataArray si s'en est un, {@code null} sinon.
     */
    public DataArrayPropertyType getPropertyArray() {
        return array;
    }
    
    /**
     * retourne une chaine de caractere decrivant le resultat (debug)
     * 
     */
    @Override
    public String toString() {
        String res;
        if (reference == null)
            res = array.toString();
        else
            res = reference.toString();
        
        return "id = " + id + " value/idref: " + res;
    }
}
