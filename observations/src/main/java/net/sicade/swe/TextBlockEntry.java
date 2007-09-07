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
 * Encodage textuel de donn�e.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class TextBlockEntry extends Entry implements TextBlock {
    
    /**
     * L'identifiant de l'encodage.
     */
    private String id;
    
    /**
     * chaine de 3 caractere maximum pour separer les tokens.
     */
    private String tokenSeparator;
    
    /**
     * chaine de 3 caractere maximum pour separer les blocks.
     */
    private String blockSeparator;
    
    /**
     * un caractere pour separer les decimaux.
     */
    private char decimalSeparator;
    
    /**
     *
     */
    public TextBlockEntry(String id, String tokenSeparator, String blockSeparator,char decimalSeparator) {
        super(id);
        this.id               = id;
        this.tokenSeparator   = tokenSeparator;
        this.blockSeparator   = blockSeparator;
        this.decimalSeparator = decimalSeparator;
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
    public String getTokenSeparator() {
        return tokenSeparator;
    }

    /**
     * {@inheritDoc}
     */
    public String getBlockSeparator() {
        return blockSeparator;
    }

    /**
     * {@inheritDoc}
     */
    public char getDecimalSeparator() {
        return decimalSeparator;
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
            final TextBlockEntry that = (TextBlockEntry) object;
            return Utilities.equals(this.id,              that.id) &&
                   Utilities.equals(this.tokenSeparator,          that.tokenSeparator)   &&
                   Utilities.equals(this.blockSeparator,    that.blockSeparator)   && 
                   Utilities.equals(this.decimalSeparator,   that.decimalSeparator) ;
        }
        return false;
    }
    
    
}
