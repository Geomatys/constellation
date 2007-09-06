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
package net.sicade.observation;

import net.sicade.catalog.Entry;
import org.geotools.resources.Utilities;

/**
 * Valeur d'un champ de dataRecord scalaire ou textuelle. 
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class DataRecordFieldEntry extends Entry{
    
    /**
     * Identifiant du datarecord qui contient cet valeur.
     */
    private String idDataRecord;
    
    /**
     * Identifiant de la valeur.
     */
    private int idField;
    
    /**
     * La valeur de type text ou float
     */
    private Object value;
    
    /**
     * crée un nouveau champ de DataRecord.
     */
    public DataRecordFieldEntry(String idDataRecord, int idField, Object value) {
        super(idField + "");
        this.idDataRecord = idDataRecord;
        this.idField      = idField;
        this.value        = value;
        
    }

    /** 
     * retourne l'identifiant du data record qui contient ce champ.
     */
    public String getIdDataRecord() {
        return idDataRecord;
    }

    /**
     * retourne l'identifiant du champ.
     */
    public int getIdField() {
        return idField;
    }

    /**
     * retourne la valeur du champ textuelle ou scalaire.
     */
    public Object getValue() {
        return value;
    }
    
    /**
     * Retourne un code représentant ce champ de dataRecord.
     */
    @Override
    public final int hashCode() {
        return idField + 13 * idDataRecord.hashCode();
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
            final DataRecordFieldEntry that = (DataRecordFieldEntry) object;
            return Utilities.equals(this.idField,         that.idField) &&
                   Utilities.equals(this.idDataRecord, that.idDataRecord) &&
                   Utilities.equals(this.value,   that.value) ;
        }
        return false;
    }
    
}
