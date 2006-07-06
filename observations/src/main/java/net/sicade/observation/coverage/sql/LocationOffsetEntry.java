/*
 * Sicade - Syst�mes int�gr�s de connaissances pour l'aide � la d�cision en environnement
 * (C) 2005, Institut de Recherche pour le D�veloppement
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sicade.observation.coverage.sql;

import org.geotools.resources.Utilities;
import net.sicade.observation.sql.Entry;
import net.sicade.observation.coverage.LocationOffset;

import static java.lang.Double.compare;
import static java.lang.Double.doubleToLongBits;


/**
 * Impl�mentation d'une entr�e repr�sentant une {@linkplain LocationOffset d�calage spatio-temporel}.
 * Ces entr�es sont comparables entre elles. Leur ordre naturel place les entr�es en ordre croissant
 * de {@linkplain #getDayOffset d�calage spatio-temporel}. Si deux entr�es ont le m�me d�calage temporel,
 * alors ils seront ordonn�s selon leur {@linkplain #getAltitudeOffset d�calage d'altitude}, et ainsi
 * de suite avec le {@linkplain #getNorthing d�calage de latitude} et le {@linkplain #getEasting d�calage
 * de longitude}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public class LocationOffsetEntry extends Entry implements LocationOffset, Comparable<LocationOffsetEntry> {
    /**
     * Pour compatibilit�s entre les enregistrements binaires de diff�rentes versions.
     */
    private static final long serialVersionUID = -8724628577303211607L;

    /**
     * Une entr�e � utiliser � la place de {@code null}.
     */
    public static final LocationOffsetEntry NULL = new LocationOffsetEntry("", 0, 0, 0, 0); 

    /**
     * Nombre de millisecondes dans une journ�e.
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
     * et la date � prendre en compte dans les param�tres environnementaux.
     */
    private long timeOffset;

    /**
     * Construit une nouvelle entr�.
     *
     * @param name Le nom de cette entr�e.
     * @param dx   Ecart de position en x.
     * @param dy   Ecart de position en y.
     * @param dz   Ecart de position en z.
     * @param dt   Ecart de temps (en nombre de millisecondes) entre la date de l'observation
     *             et la date � prendre en compte dans les param�tres environnementaux.
     */
    protected LocationOffsetEntry(final String  name,
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
     * Compare l'objet sp�cifi� avec cette entr�.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final LocationOffsetEntry that = (LocationOffsetEntry) object;
            return this.timeOffset           == that.timeOffset           &&
                   doubleToLongBits(this.dx) == doubleToLongBits(that.dx) &&
                   doubleToLongBits(this.dy) == doubleToLongBits(that.dy) &&
                   doubleToLongBits(this.dz) == doubleToLongBits(that.dz);
        }
        return false;
    }

    /**
     * Compare l'objet sp�cifi� avec cette entr�.
     */
    public int compareTo(final LocationOffsetEntry that) {
        if (this.timeOffset < that.timeOffset) return -1;
        if (this.timeOffset > that.timeOffset) return +1;
        int c;
        if ((c=compare(this.dz, that.dz)) != 0) return c;
        if ((c=compare(this.dy, that.dy)) != 0) return c;
        if ((c=compare(this.dx, that.dx)) != 0) return c;
        return 0;
    }
}
