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
import org.geotools.resources.Utilities;

/**
 * Valeur d'un champ de dataRecord scalaire ou textuelle. 
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class AnyScalarEntry extends Entry{
    
    /**
     * Identifiant du datarecord qui contient cet valeur.
     */
    private String idDataRecord;
    
    /**
     * Identifiant du champ.
     */
    private String name;
    
    /**
     * description du champ.
     */
    private String definition;
    
    /**
     * Type swe du champ (Time, Quantity, Boolean).
     */
    private String type;
    
    /**
     * l'unité de mesure du champ.
     */
    private String uom;
    
    /**
     * La valeur de type text ou float
     */
    private Object value;
    
    /**
     * Constructeur utilisé par jaxB.
     */
    public AnyScalarEntry() {}
            
    /**
     * crée un nouveau champ de DataRecord.
     */
    public AnyScalarEntry(String idDataRecord, String name, String definition, String type,
            String uom, Object value) {
        super(name, definition);
        this.idDataRecord = idDataRecord;
        this.name         = name;
        this.definition   = definition;
        this.type         = type;
        this.value        = value;
        this.uom          = uom;
        
    }

    /** 
     * retourne l'identifiant du data record qui contient ce champ.
     */
    public String getIdDataRecord() {
        return idDataRecord;
    }

    /**
     * Retourne la definition du champ.
     */
    public String getDefinition() {
        return definition;
    }

    /**
     * Retourne le type swe du champ.
     */
    public String getType() {
        return type;
    }
    
    /**
     * retourne la valeur du champ textuelle ou scalaire.
     */
    public Object getValue() {
        return value;
    }
    
    /**
     * Retourne L'unité de mesure du champ.
     */
    public String getUom() {
        return uom;
    }

    /**
     * Retourne un code représentant ce champ de dataRecord.
     */
    @Override
    public final int hashCode() {
        return name.hashCode() + 13 * idDataRecord.hashCode();
    }

    /**
     * Vérifie si cette entré est identique à l'objet spécifié.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final AnyScalarEntry that = (AnyScalarEntry) object;
            return Utilities.equals(this.name,         that.name) &&
                   Utilities.equals(this.idDataRecord, that.idDataRecord) &&
                   Utilities.equals(this.definition,   that.definition) && 
                   Utilities.equals(this.type,         that.type) &&
                   Utilities.equals(this.uom,          that.uom) && 
                   Utilities.equals(this.value,        that.value) ;
        }
        return false;
    }
    
    /**
     * Retourne une representation de l'objet (debug).
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append('[').append(this.getClass().getSimpleName()).append("]:").append(idDataRecord)
        .append('-').append(name).append(" type=").append(type).append(" uom=").append(uom);
                return buffer.toString();
    }

    
}
