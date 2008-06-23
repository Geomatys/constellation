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
package net.seagis.swe;

//jaxB import
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;

// Sicade dependencies
import javax.xml.bind.annotation.XmlType;
import net.seagis.gml.v311.DefinitionType;
import org.opengis.observation.Phenomenon;


/**
 * Implementation of an entry representing a {@linkplain Phenomenon phenomenon}.
 * 
 * @version $Id$
 * @author Antoine Hnawia
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Phenomenon")
@XmlSeeAlso({ CompoundPhenomenonEntry.class })
public class PhenomenonEntry extends DefinitionType implements Phenomenon {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = 5140595674231914861L;

    
    /**
     * Empty constructor used by JAXB.
     */
    protected PhenomenonEntry(){}
    
    /**
     * Construit un nouveau phénomène du nom spécifié.
     *
     * @param id L'identifiant de ce phenomene.
     * @param name Le nom du phénomène.
     */
    public PhenomenonEntry(final String id, final String name) {
        super(id, name, null);
        
    }

    /**
     * 
     * Construit un nouveau phénomène du nom spécifié.
     * 
     * 
     * @param id L'identifiant de ce phenomene.
     * @param name Le nom du phénomène.
     * @param description La description de ce phénomène, ou {@code null}.
     */
    public PhenomenonEntry(final String id, final String name, final String description ) {
        super(id, name, description);
    }

    /**
     * Retourne l'identifiant du phénomène.
    
    public String getId() {
        return id;
    }

    /**
     * Retoune la description du phénomène.
     
    public String getDescription() {
        return description;
    }
    
    /**
     * Retourne le nom du phenomene (une URN le plus souvent).
     
    public String getPhenomenonName(){
        return name;
    } 
    
    /**
     * Retourne un code représentant ce phenomene.
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Verify if this entry is identical to specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        } else return super.equals(object);
    }
    
    /**
     * Retourne une chaine de charactere representant le phenomene.
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("[phenomenonEntry]").append(super.toString());
        return s.toString();
    }
}
