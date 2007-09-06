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

// GeoAPI dependencies 
import org.opengis.observation.Phenomenon;


/**
 * Implémentation d'une entrée représentant un {@linkplain Phenomenon phénomène}.
 * 
 * @version $Id$
 * @author Antoine Hnawia
 */
public class PhenomenonEntry extends Entry implements Phenomenon {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = 5140595674231914861L;

    /**
     * L'identifiant du phenomene.
     */
    private String id;
    
      
    /**
     * La description du phenomene.
     */
    private String description;
    
    /**
     * Construit un nouveau phénomène du nom spécifié.
     *
     * @param name Le nom du phénomène.
     */
    public PhenomenonEntry(final String id, final String name) {
        super(name);
        this.id = id;
        this.description = null;
    }

    /** 
     * Construit un nouveau phénomène du nom spécifié.
     *
     * @param name    Le nom du phénomène.
     * @param remarks Remarques s'appliquant à ce phénomène, ou {@code null}.
     */
    public PhenomenonEntry(final String id, final String name, final String description ) {
        super(name, description);
        this.id = id;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }
    
    /**
     * Retourne un code représentant ce phenomene.
     */
    @Override
    public final int hashCode() {
        return id.hashCode();
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
            final PhenomenonEntry that = (PhenomenonEntry) object;
            return Utilities.equals(this.id,          that.id) &&
                   Utilities.equals(this.description, that.description);
        }
        return false;
    }
}
