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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import net.seagis.catalog.Entry;
import org.geotools.resources.Utilities;

/**
 * Valeur d'un champ de dataRecord scalaire ou textuelle. 
 *
 * @version $Id:
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AnyScalar")
@XmlSeeAlso({TimeType.class, QuantityType.class, BooleanType.class})
public class AnyScalarEntry extends Entry{
    
    /**
     * Identifiant du datarecord qui contient cet valeur.
     */
    @XmlTransient
    private String idDataRecord;
    
    @XmlAttribute
    private String name;
    
    private AbstractDataComponentEntry component;
    
    /**
     * Constructeur utilisé par jaxB.
     */
    public AnyScalarEntry() {}
            
    /**
     * crée un nouveau champ de DataRecord.
     */
    public AnyScalarEntry(String idDataRecord, String name, AbstractDataComponentEntry component) {
        super(name);
        this.name         = name;
        this.idDataRecord = idDataRecord;
        this.component = component;
        
    }

    /**
     * surcharge le getName() de Entry
     */
    @Override
    public String getName() {
        return this.name;
    }
    /** 
     * retourne l'identifiant du data record qui contient ce champ.
     */
    public String getIdDataRecord() {
        return idDataRecord;
    }

    /**
     * Retourne le component du champ.
     */
    public AbstractDataComponentEntry getComponent() {
        return component;
    }
        
    /**
     * Vérifie si cette entré est identique à l'objet spécifié.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        final AnyScalarEntry that = (AnyScalarEntry) object;
        return Utilities.equals(this.name,         that.name) &&
               Utilities.equals(this.idDataRecord, that.idDataRecord) &&
               Utilities.equals(this.component,    that.component);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (this.idDataRecord != null ? this.idDataRecord.hashCode() : 0);
        hash = 31 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
    
    /**
     * Retourne une representation de l'objet (debug).
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        if (this != null) {
            buffer.append('[').append(this.getClass().getSimpleName()).append("]:").append(idDataRecord)
            .append('-').append(name);
            if(component != null) {
                buffer.append(" type=").append(component.getClass().getSimpleName()).append('\n');
                buffer.append(component.toString());
            } else 
                buffer.append(" COMPONENT NULL");
        }   
        return buffer.toString();
    }

    
}
