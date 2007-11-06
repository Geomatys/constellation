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
import javax.xml.bind.annotation.XmlType;
import org.geotools.resources.Utilities;

/**
 * Encodage textuel de donn�e.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TextBlock", propOrder = {
    "tokenSeparator",
    "decimalSeparator",
    "blockSeparator"})
public class TextBlockEntry extends AbstractEncodingEntry implements TextBlock {
    
    /**
     * chaine de 3 caractere maximum pour separer les tokens.
     */
    @XmlAttribute(required = true)
    private String tokenSeparator;
    
    /**
     * chaine de 3 caractere maximum pour separer les blocks.
     */
    @XmlAttribute(required = true)
    private String blockSeparator;
    
    /**
     * un caractere pour separer les decimaux.
     */
    @XmlAttribute(required = true)
    private char decimalSeparator;
    
    /**
     * Constructeur utilisé par jaxB.
     */
    public TextBlockEntry() {}
    
    /**
     * Crée un nouveau encodage de texte.
     */
    public TextBlockEntry(String id, String tokenSeparator, String blockSeparator, char decimalSeparator) {
        super(id);
        this.tokenSeparator   = tokenSeparator;
        this.blockSeparator   = blockSeparator;
        this.decimalSeparator = decimalSeparator;
    }

    /**
     * surcharge la methode getName() de Entry pour accepter les valeurs nulles.
     */
    @Override
    public String getName() {
        return this.name;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getTokenSeparator() {
        return tokenSeparator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBlockSeparator() {
        return blockSeparator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public char getDecimalSeparator() {
        return decimalSeparator;
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
            return Utilities.equals(this.tokenSeparator,          that.tokenSeparator)   &&
                   Utilities.equals(this.blockSeparator,    that.blockSeparator)   && 
                   Utilities.equals(this.decimalSeparator,   that.decimalSeparator) ;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + (this.tokenSeparator != null ? this.tokenSeparator.hashCode() : 0);
        hash = 11 * hash + (this.blockSeparator != null ? this.blockSeparator.hashCode() : 0);
        hash = 11 * hash + this.decimalSeparator;
        return hash;
    }
    
    /**
     * Retourne une representation de l'objet (debug).
     */
    @Override
    public String toString() {
        return '[' + this.getClass().getSimpleName() + "]:" + this.blockSeparator 
                + '|' + this.decimalSeparator + '|' + this.tokenSeparator;
    }
    
    
}
