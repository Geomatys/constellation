/*
 * Sicade - Systémes intégrés de connaissances pour l'aide é la décision en environnement
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
 * Unité de mesure.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class UnitOfMeasureEntry extends Entry implements BaseUnit {
    /**
     * l'identifiant de l'unité ( exemple cm, és, ...)
     */
    private String id;
    
    /**
     * Le nom de l'unité.
     */
    private String name;
    
    /**
     * le type de l'unité de mesure (longueur, temporelle, ...).
     */
    private String quantityType;
    
    /**
     * Le system qui definit cette unité de mesure.
     */
    private String unitsSystem;
    
    /**
     * Créé une nouvelle unité de mesure.
     */
    public UnitOfMeasureEntry(String id, String name, String quantityType, String unitsSystem) {
        super(name);
        this.id           = id;
        this.name         = name;
        this.quantityType = quantityType;
        this.unitsSystem  = unitsSystem;
    }
    
    /**
     * Retourne l'identifiant.
     */
    public String getId() {
        return id;
    }
    
    /**
     * retourne le type de l'unité de mesure.
     */
    public String quantityType() {
        return quantityType;
    }
    
    /**
     * retourne le nom du systeme qui definit cette unité.
     */
    public String getUnitsSystem() {
        return unitsSystem;
    }
    
}
