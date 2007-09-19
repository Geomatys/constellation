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
import java.util.List;
import net.sicade.catalog.Entry;
import org.geotools.resources.Utilities;

/**
 * Resultat d'une observation de type DataBlockDefinition.
 * 
 * @version $Id:
 * @author Guilhem Legal
 */
public class DataBlockDefinitionEntry extends Entry implements DataBlockDefinition {
    
    /**
     * L'identifiant du resultat.
     */
    private String id;
    
    /**
     * Liste de composant Data record.
     */
     private Collection<? extends AbstractDataComponent> components;
     
    /**
     * Decrit l'encodage des données.
     */
     private AbstractEncoding encoding;
     
    /**
     * créé un nouveau resultat d'observation.
     * Liste de valeur decrite dans swe:DatablockDefinition de type simple,
     * pour valeur scalaire ou textuelle.
     *
     * @param id l'identifiant du resultat.
     * @param components liste de composant data record.
     * @param encoding encodage des données.
     */
    public DataBlockDefinitionEntry(final String id, final Collection<? extends AbstractDataComponent> components,
            final AbstractEncoding encoding) {
        super(null);
        this.id         = id;
        this.components = components;
        this.encoding   = encoding;
    }

    /**
     * {@inheritDoc}
     */
    public String getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    public Collection<? extends AbstractDataComponent> getComponents() {
        return components;
    }

    /**
     * {@inheritDoc}
     */
    public AbstractEncoding getEncoding() {
        return encoding;
    }
    
    /**
     * Retourne un code représentant ce dataBlock.
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
            final DataBlockDefinitionEntry that = (DataBlockDefinitionEntry) object;
            return Utilities.equals(this.id,         that.id) &&
                   Utilities.equals(this.components, that.components) &&
                   Utilities.equals(this.encoding,   that.encoding) ;
        }
        return false;
    }
    
}
