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
package net.sicade.observation.coverage.sql;

import net.sicade.observation.sql.Entry;
import net.sicade.observation.Distribution;
import static java.lang.Double.doubleToLongBits;


/**
 * Implementation of a {@linkplain Distribution distribution} entry.
 * 
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public class DistributionEntry extends Entry implements Distribution {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = -9004700774687614563L;

    /**
     * Une constante prédéfinie pour laquelle {@link #normalize} est l'opérateur identité.
     */
    public static Distribution NORMAL = new DistributionEntry("normale", 1, 0, false);

    /**
     * Facteur par lequel on multiplie les données.
     */
    private final double scale;
    
    /**
     * Constante à ajouter aux données.
     */
    private final double offset;
    
    /**
     * Indique s'il s'agit d'une distribution log-normale.
     */
    private final boolean log;
    
    /** 
     * Crée une nouvelle distibution.
     *
     * @param   name    le nom de la distribution.
     * @param   scale   le facteur multiplicatif.
     * @param   offset  la constante additive.
     * @param   log     {@code true} s'il s'agit d'une distribution log-normale. 
     */
    protected DistributionEntry(final String name,   final double  scale, 
                                final double offset, final boolean log) 
    {
        super(name);
        this.scale  = scale;
        this.offset = offset;
        this.log    = log;
    }

    /**
     * Applique un changement de variable. Cette méthode calcule
     * <code>value&times;scale + offset</code>, et éventuellement
     * le logarithme naturel du résultat.
     */
    public double normalize(double value) {
        value = scale*value + offset;
        if (log) {
            value = Math.log(value);
        }
        return value;
    }

    /**
     * Retourne {@code true} si {@link #normalize normalize} n'effectue aucune transformation.
     */
    public boolean isIdentity() {
        return !log && scale==1 && offset==0;
    }

    /**
     * Vérifie que cette distribution est identique à l'objet spécifié
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final DistributionEntry that = (DistributionEntry) object;
            return doubleToLongBits(this.scale ) == doubleToLongBits(that.scale ) &&
                   doubleToLongBits(this.offset) == doubleToLongBits(that.offset) &&
                                   (this.log)    ==                 (that.log   );
        }
        return false;
    }
}
