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
package net.sicade.coverage.catalog.sql;

import org.geotools.resources.Utilities;
import net.sicade.catalog.Entry;
import net.sicade.coverage.catalog.RegionOfInterest;

import static java.lang.Double.compare;
import static java.lang.Double.doubleToLongBits;


/**
 * Implémentation d'une entrée représentant une {@linkplain RegionOfInterest décalage spatio-temporel}.
 * Ces entrées sont comparables entre elles. Leur ordre naturel place les entrées en ordre croissant
 * de {@linkplain #getDayOffset décalage spatio-temporel}. Si deux entrées ont le même décalage temporel,
 * alors ils seront ordonnés selon leur {@linkplain #getAltitudeOffset décalage d'altitude}, et ainsi
 * de suite avec le {@linkplain #getNorthing décalage de latitude} et le {@linkplain #getEasting décalage
 * de longitude}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public class RegionOfInterestEntry extends Entry implements RegionOfInterest, Comparable<RegionOfInterestEntry> {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = -8724628577303211607L;

    /**
     * Une entrée à utiliser à la place de {@code null}.
     */
    public static final RegionOfInterestEntry NULL = new RegionOfInterestEntry("", 0, 0, 0, 0); 

    /**
     * Nombre de millisecondes dans une journée.
     */
    static final double DAY = 24*60*60*1000.0;

    /**
     * Ecart de position en x
     */
    private final double dx;
    
    /**
     * Ecart de position en y
     */
    private final double dy;
    
    /**
     * Ecart de position en z
     */
    private final double dz;
    
    /**
     * Ecart de temps (en nombre de millisecondes) entre la date de l'observation
     * et la date à prendre en compte dans les paramètres environnementaux.
     */
    private long timeOffset;

    /**
     * Construit une nouvelle entré.
     *
     * @param name Le nom de cette entrée.
     * @param dx   Ecart de position en x.
     * @param dy   Ecart de position en y.
     * @param dz   Ecart de position en z.
     * @param dt   Ecart de temps (en nombre de millisecondes) entre la date de l'observation
     *             et la date à prendre en compte dans les paramètres environnementaux.
     */
    protected RegionOfInterestEntry(final String  name,
                                    final double  dx,
                                    final double  dy, 
                                    final double  dz, 
                                    final long    dt)
    {
        super(name);
        this.timeOffset = dt;
        this.dx         = dx;
        this.dy         = dy;
        this.dz         = dz;
    }

    /**
     * {@inheritDoc}
     */
    public double getDayOffset() {
        return timeOffset / DAY;
    }

    /**
     * {@inheritDoc}
     */
    public double getEasting() {
        return dx;
    }

    /**
     * {@inheritDoc}
     */
    public double getNorthing() {
        return dy;
    }

    /**
     * {@inheritDoc}
     */
    public double getAltitudeOffset() {
        return dz;
    }

    /**
     * Compare l'objet spécifié avec cette entré.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final RegionOfInterestEntry that = (RegionOfInterestEntry) object;
            return this.timeOffset           == that.timeOffset           &&
                   doubleToLongBits(this.dx) == doubleToLongBits(that.dx) &&
                   doubleToLongBits(this.dy) == doubleToLongBits(that.dy) &&
                   doubleToLongBits(this.dz) == doubleToLongBits(that.dz);
        }
        return false;
    }

    /**
     * Compare l'objet spécifié avec cette entré.
     */
    public int compareTo(final RegionOfInterestEntry that) {
        if (this.timeOffset < that.timeOffset) return -1;
        if (this.timeOffset > that.timeOffset) return +1;
        int c;
        if ((c=compare(this.dz, that.dz)) != 0) return c;
        if ((c=compare(this.dy, that.dy)) != 0) return c;
        if ((c=compare(this.dx, that.dx)) != 0) return c;
        return 0;
    }
}
