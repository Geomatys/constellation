/*
 * Sicade - Systemes integres de connaissances pour l'aide a la decision en environnement
 * (C) 2005, Institut de Recherche pour le Developpement
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
package net.seagis.coverage.model;

import net.seagis.catalog.Entry;
import static java.lang.Double.doubleToLongBits;


/**
 * Implementation of a {@linkplain Distribution distribution} entry.
 * 
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 * @author Guilhem Legal
 */
public class DistributionEntry extends Entry implements Distribution {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = -9004700774687614563L;

    /**
     * Facteur par lequel on multiplie les donnees.
     */
    private double scale;
    
    /**
     * Constante a ajouter aux donnees.
     */
    private double offset;
    
    /**
     * Indique s'il s'agit d'une distribution log-normale.
     */
    private boolean log;
       
    /**
     * Constructeur utilise par JAXB.
     */
    DistributionEntry() {}
    
    /** 
     * Cree une nouvelle distibution.
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
        this.name   = name;
        this.scale  = scale;
        this.offset = offset;
        this.log    = log;
    }

    /**
     * surcharge la methode de la superclasse pour garder un name a null.
     */
    @Override
    public String getName() {
        return name;
    }
            
    /**
     * Applique un changement de variable. Cette methode calcule
     * <code>value&times;scale + offset</code>, et eventuellement
     * le logarithme naturel du resultat.
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
     * Retourne le facteur par lequel on multiplie les donnees.
     */
    public double getScale() {
        return scale;
    }

    /**
     * Retourne la constante a ajouter aux donnees.
     */
    public double getOffset() {
        return offset;
    }

    /**
     * Retourne un booleen indiquant s'il s'agit d'une distribution log-normale.
     */
    public boolean isLog() {
        return log;
    }
    
    /**
     * Verifie que cette distribution est identique a l'objet specifie
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
            final DistributionEntry that = (DistributionEntry) object;
            return doubleToLongBits(this.scale ) == doubleToLongBits(that.scale ) &&
                   doubleToLongBits(this.offset) == doubleToLongBits(that.offset) &&
                                   (this.log)    ==                 (that.log   );
    }

  
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
    
    /**
     * Return a string representation of the object
     */
    @Override
    public String toString() {
        StringBuffer s = new StringBuffer();
        char ret = '\n';
        s.append("name:").append(name).append(ret).append("scale:").append(scale).append(ret)
                .append("offset:").append(offset).append(ret).append("log:").append(log);
        return s.toString();
    }
    
}
