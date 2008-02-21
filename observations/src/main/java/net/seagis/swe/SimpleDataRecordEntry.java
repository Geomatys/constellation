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

import java.util.Collection;
import java.util.Iterator;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.geotools.resources.Utilities;

/**
 * Liste de valeur scalaire ou textuelle utilisé dans le resultat d'une observation.
 * 
 * @version $Id:
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SimpleDataRecord")
public class SimpleDataRecordEntry extends AbstractDataRecordEntry implements SimpleDataRecord {
    
    /**
     * L'identifiant du dataBlock qui contient ce data record.
     */
    @XmlTransient
    private String blockId;
    
    /**
     * List de valeur textuelle ou scalaire.
     */
    private Collection<AnyScalarPropertyType> field;
   
    /**
     *  Constructor used by jaxB.
     */
    public SimpleDataRecordEntry() {}
    
    /** 
     * Créé une nouvelle Liste de valeur textuelle ou scalaire.
     */
    public SimpleDataRecordEntry(final String blockId, final String id, final String definition, final boolean fixed,
            final Collection<AnyScalarPropertyType> fields) {
        super(id, definition, fixed);
        this.blockId = blockId;
        this.field = fields;
    }

    /**
     * {@inheritDoc}
     */
    public Collection<AnyScalarPropertyType> getField() {
        return field;
    }

    /**
     * Retourne l'identifiant du block qui contient ce data record.
     */
    public String getBlockId() {
        return blockId;
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
            if (this.field.size() != that.field.size())
                return false;
        
            Iterator<AnyScalarPropertyType> i = field.iterator();
            while (i.hasNext()) {
                if (!that.field.contains(i.next()))
                    return false;
            }
            return Utilities.equals(this.blockId,    that.blockId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode() + 37 * this.getBlockId().hashCode();
    }
    
    /**
     * Retourne une representation de l'objet (debug).
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        final String lineSeparator = System.getProperty("line.separator", "\n");
        buffer.append('[').append(this.getClass().getSimpleName()).append("]:").append(blockId).append(lineSeparator);
        appendTo(buffer, "", lineSeparator);
        return buffer.toString();
    }
    
    /**
     * Ajoute la description des composants du dataBlock definition.
     */
    private void appendTo(final StringBuilder buffer, String margin, final String lineSeparator) {
        buffer.append("fields: ").append(lineSeparator);
        margin += "  ";
        for (final AnyScalarPropertyType a : field) {
            buffer.append(margin).append(a.toString()).append(lineSeparator);
        }
    }
    
    
}
