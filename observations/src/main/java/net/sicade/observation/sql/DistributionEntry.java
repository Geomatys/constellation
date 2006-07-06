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
package net.sicade.observation.sql;

import net.sicade.observation.Distribution;
import static java.lang.Double.doubleToLongBits;


/**
 * Impl�mentation d'une entr�e repr�sentant une {@linkplain Distribution distribution}.
 * 
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public class DistributionEntry extends Entry implements Distribution {
    /**
     * Pour compatibilit�s entre les enregistrements binaires de diff�rentes versions.
     */
    private static final long serialVersionUID = -9004700774687614563L;

    /**
     * Une constante pr�d�finie pour laquelle {@link #normalize} est l'op�rateur identit�.
     */
    public static Distribution NORMAL = new DistributionEntry("normale", 1, 0, false);

    /**
     * Facteur par lequel on multiplie les donn�es.
     */
    private final double scale;
    
    /**
     * Constante � ajouter aux donn�es.
     */
    private final double offset;
    
    /**
     * Indique s'il s'agit d'une distribution log-normale.
     */
    private final boolean log;
    
    /** 
     * Cr�e une nouvelle distibution.
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
     * Applique un changement de variable. Cette m�thode calcule
     * <code>value&times;scale + offset</code>, et �ventuellement
     * le logarithme naturel du r�sultat.
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
     * V�rifie que cette distribution est identique � l'objet sp�cifi�
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
