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

import java.util.Collection;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import org.geotools.resources.Utilities;

/**
 * Liste de valeur scalaire ou textuelle utilisé dans le resultat d'une observation.
 * 
 * @version $Id:
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SimpleDataRecord", propOrder = {
    "definition",
    "fields"})
public class SimpleDataRecordEntry extends AbstractDataComponentEntry implements SimpleDataRecord {
    
    /**
     * L'identifiant du dataBlock qui contient ce data record.
     */
    @XmlAttribute
    private String blockId;
    
    /**
     * L'identifiant du dataRecord
     */
    @XmlAttribute
    private String id;
    
    @XmlAttribute
    private boolean fixed;
    
    /**
     * definition du record.
     */
    private String definition;
    
    /**
     * List de valeur textuelle ou scalaire.
     */
    private Collection<DataRecordFieldEntry> fields;
   
    /**
     *  Constructeur utilisé par jaxB.
     */
    public SimpleDataRecordEntry() {}
    
    /** 
     * Créé une nouvelle Liste de valeur textuelle ou scalaire.
     */
    public SimpleDataRecordEntry(final String blockId, final String id, final String definition, final boolean fixed,
            final Collection<DataRecordFieldEntry> fields) {
        super(id);
        this.id = id;
        this.blockId = blockId;
        this.definition = definition;
        this.fixed      = fixed;
        this.fields = fields;
    }

    /**
     * {@inheritDoc}
     */
    public Collection<DataRecordFieldEntry> getFields() {
        return fields;
    }

    /**
     * {@inheritDoc}
     */
    public String getDefinition() {
        return definition;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isFixed() {
        return fixed;
    }

    /**
     * Retourne l'identifiant du block qui contient ce data record.
     */
    public String getBlockId() {
        return blockId;
    }

     /**
     * Retourne l'identifiant de ce data record.
     */
    public String getId() {
        return id;
    }
    
    
    /**
     * Retourne le code numérique identifiant cette entrée.
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    /**
     * Vérifie que cette station est identique à l'objet spécifié
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final SimpleDataRecordEntry that = (SimpleDataRecordEntry) object;
            return Utilities.equals(this.id,         that.id) &&
                   Utilities.equals(this.blockId,    that.blockId)   &&
                   Utilities.equals(this.definition, that.definition)   && 
                   Utilities.equals(this.fields,     that.fields) &&
                   Utilities.equals(this.fixed,      that.fixed);
        }
        return false;
    }
    
}
