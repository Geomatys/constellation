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

// Sicade dependencies
import net.sicade.catalog.Entry;
import org.geotools.resources.Utilities;

// OpenGis dependencies
import org.opengis.observation.Measure;

/**
 * Resultat d'une observation de type {linkplain Measurement measurement}.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class MeasureEntry extends Entry implements Measure{
    
    /**
     * Le non de l'unité de mesure.
     */
    private String name;

    /**
     * L'unite de la mesure
     */
    private String uom;
    
    /**
     * La valeur de la mesure
     */
    private float value;
    
    /** 
     * crée un nouveau resultat de mesure.
     *
     * @param name  Le nom/identifiant du resultat.
     * @param uom   L'unité de mesure.
     * @param value La valeur mesurée.
     */
    public MeasureEntry(final String name,
                        final String uom,
                        final float value)
    {
        super(name);
        this.name = name;
        this.uom   = uom;
        this.value = value;        
    }
    
    /**
     * {@inheritDoc}
     *
     * @todo Implementer le retour des unites.
     */
    public String getUom() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public float getValue() {
        return value;
    }
    
     /**
     * Retourne un code représentant ce resultat de mesure.
     */
    @Override
    public final int hashCode() {
        return name.hashCode();
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
            final MeasureEntry that = (MeasureEntry) object;
            return Utilities.equals(this.name,  that.name) &&
                   Utilities.equals(this.uom,   that.uom) &&
                   Utilities.equals(this.value, that.value) ;
        }
        return false;
    }
    
}
