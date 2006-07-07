/*
 * Sicade - Syst�mes int�gr�s de connaissances pour l'aide � la d�cision en environnement
 * (C) 1997, P�ches et Oc�ans Canada
 * (C) 2006, Institut de Recherche pour le D�veloppement
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
package net.sicade.numeric.table;

// J2SE dependencies
import java.util.Arrays;
import java.nio.DoubleBuffer;
import java.lang.reflect.Field;

// Static imports
import static java.lang.Math.abs;
import static java.lang.Double.NaN;
import static java.lang.Double.isNaN;
import static java.lang.System.arraycopy;
import static net.sicade.numeric.table.DataOrder.*;
import static org.geotools.resources.XArray.isSorted;


/**
 * Un vecteur ordonn� dont les donn�es sont sp�cifi�es dans un buffer.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class BufferedOrderedVector extends OrderedVector {
    /**
     * Les valeurs de ce vecteur. Il est de la responsabilit� de l'utilisateur de s'assurer que
     * ces donn�es restent en ordre croissant ou d�croissant. Pour des raisons de performance,
     * �a ne sera pas v�rifi�.
     */
    private final DoubleBuffer data;

    /**
     * Construit un vecteur ordonn� pour les donn�es sp�cifi�es. Les donn�es doivent obligatoirement
     * �tre en ordre croissant ou d�croissant. Pour des raisons de performances (et aussi parce qu'il
     * n'est pas possible de s'assurer que l'utilisateur ne modifiera pas les donn�es apr�s la
     * construction de ce vecteur), �a ne sera pas v�rifi�.
     * <p>
     * Les premi�res donn�es prises en compte seront les donn�es � la {@linkplain DoubleBuffer#position()
     * position courante}. Le nombre de donn�es prises en compte sera le {@linkplain DoubleBuffer#remaining()
     * nombre de donn�es restantes}. Apr�s la construction du vecteur, les changements de
     * {@linkplain DoubleBuffer#position() position} ou de {@linkplain DoubleBuffer#limit() limite}
     * de l'objet {@code data} donn� en argument n'affecteront pas ce vecteur. Toutefois tout changement
     * des donn�es contenues dans le buffer affecteront ce vecteur.
     */
    public BufferedOrderedVector(final DoubleBuffer data) {
        this.data = data.slice();
    }

    /**
     * Retourne l'ordre des donn�es dans ce vecteur. Cette m�thode �value l'ordre chaque fois qu'elle
     * est invoqu�e. Elle peut donc servir � d�terminer si un changement de donn�es a eu un impact sur
     * leur ordre.
     */
    public DataOrder getDataOrder() {
        double  previous   = NaN;
        boolean strict     = true;
        boolean ascending  = true;
        boolean determined = false;
        data.rewind();
        while (data.hasRemaining()) {
            final double value = data.get();
            if (isNaN(value)) {
                continue;
            }
            if (!isNaN(previous)) {
                if (value > previous) {
                    if (!determined) {
                        determined = true;
                        ascending  = true;
                    } else if (!ascending) {
                        return UNORDERED;
                    }
                } else if (value < previous) {
                    if (!determined) {
                        determined = true;
                        ascending = false;
                    } else if (ascending) {
                        return UNORDERED;
                    }
                } else {
                    strict = false;
                }
            }
            previous = value;
        }
        if (!determined) {
            return FLAT;
        }
        return (ascending) ?
            (strict ? STRICTLY_ASCENDING  : ASCENDING) :
            (strict ? STRICTLY_DESCENDING : DESCENDING);
    }

    /**
     * Retourne la longueur de ce vecteur.
     */
    public final int length() {
        return data.limit();
    }

    /**
     * Retourne la valeur � l'index sp�cifi�. La valeur de cet index peut varier de 0 inclusivement
     * jusqu'� {@link #length} exclusivement.
     *
     * @param  index La valeur de l'index.
     * @return La valeur du vecteur � l'index sp�cifi�.
     * @throws IndexOutOfBoundsException si l'index est en dehors des limites permises.
     */
    public final double get(final int index) throws IndexOutOfBoundsException {
        return data.get(index);
    }

    /**
     * {@inheritDoc}
     */
    public boolean locate(final double x) {
        /*
         * Positionne la plage [upper..lower] aux extr�mit�s du vecteur. S'il y a des NaN, ils seront
         * pris en compte en peu plus bas. 'xlo' et 'xhi' seront les valeurs de x correspondantes.
         */
        value = x;
        lower = 0;
        upper = data.limit();
        if (--upper < 0) {
            return false;
        }
        double xhi = data.get(upper);
        double xlo = data.get(lower);
        boolean ascending = (xlo < xhi);
        /*
         * Si la valeur de x sp�cifi�e n'est pas comprise dans la plage de valeurs de ce vecteur,
         * ignore les NaN et v�rifie si la valeur est �gale � celle de l'une des extr�mit�s. Note:
         * le '!' dans l'expression ci-dessous est n�cessaire pour attraper les valeurs NaN.
         */
        while (ascending ? !(xlo<x && x<xhi) : !(xlo>x && x>xhi)) {
            if (x == xlo) {upper=lower; return true;}
            if (x == xhi) {lower=upper; return true;}
            boolean changed = false;
            if (isNaN(xlo)) {
                do if (++lower > upper) return false;
                while (isNaN(xlo = data.get(lower)));
                changed = true;
            }
            if (isNaN(xhi)) {
                do if (--upper < lower) return false;
                while (isNaN(xhi = data.get(upper)));
                changed = true;
            }
            if (!changed) {
                return false;
            }
            ascending = (xlo < xhi);
        }
        /*
         * A partir de ce point, on a l'assurance qu'il existe au moins une donn�e autre que NaN
         * et que la valeur de 'x' est comprise entre 'xlo' et 'xhi'. On peut lancer la recherche
         * bilin�aire.
         */
search: while (upper - lower > 1) {
            int k = (upper+lower) >> 1;     // Indice au centre de la plage [lower..upper]
            int scan = 0;                   // Utilis� en cas de valeurs NaN seulement.
            do {
                assert k>lower && k<upper : k;
                final double xk = data.get(k);
                if (x < xk) {
                    if (ascending) upper = k;
                    else           lower = k;
                    continue search;
                }
                if (x > xk) {
                    if (ascending) lower = k;
                    else           upper = k;
                    continue search;
                }
                if (x == xk) {
                    lower = upper = k;
                    return true;
                }
                /*
                 * Le code suivant ne sera ex�cut� que si l'on vient de tomber sur un NaN.
                 * On recherche lin�airement une valeur qui ne soit pas NaN autour de k en
                 * testant dans l'ordre k-1, k+1, k-2, k+2, k-3, k+3, etc.
                 */
                assert isNaN(xk) || isNaN(x) : xk;
                // Dans la ligne ci-dessous, le premier (k-scan) restore la valeur originale de k
                // tandis que le second (k-scan) anticipe sur le changement de 'scan' � venir.
                k -= (scan << 1);
                if (scan >= 0) {
                    scan = ~scan;  // Equivaut � (-scan - 1)
                    if (--k > lower) {
                        assert k-scan == (upper+lower) >> 1 : scan; // (k-scan) devrait �tre la valeur originale.
                        continue;
                    }
                    k -= (scan << 1);  // Annule le test de 'k-scan' et passe au test de 'k+scan'.
                }
                scan = -scan;
                assert scan > 0 : scan;
                assert k-scan == (upper+lower) >> 1 : scan; // (k-scan) devrait �tre la valeur originale.
            } while (k < upper);
            /*
             * On atteint ce point si aucune valeur autre que NaN n'a �t� trouv�e entre
             * 'lower' et 'upper'. Les index 'lower' et 'upper' toutefois sont valides.
             */
            break;
        }
        assert data.get(lower) <= x : lower;
        assert data.get(upper) >= x : upper;
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean locateAroundIndex(final int index) {
        lower = upper = index;
        final int length = data.limit();
        do if (++upper >= length) {
            return false;
        } while (isNaN(data.get(upper)));
        do if (--lower < 0) {
            return false;
        } while (isNaN(data.get(lower)));
        value = data.get(index);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean copyIndexInto(final int[] index) {
        int lower = this.lower;  // Protect from changes.
        int upper = this.upper;  // Protect from changes.
        final int length = data.limit();
        int center = index.length;
        if (center >= 2) {
            center >>= 1;
            int i = center;
            /*
             * Si 'upper' et 'lower' sont identiques, on n'�crira pas 'lower' afin de ne pas
             * r�p�ter deux fois le m�me index. On �crira seulement 'upper.' La boucle 'loop'
             * copie au d�but du tableau 'index' les index qui pr�c�dent 'lower'.
             */
            if (upper != lower) {
                index[--i] = lower;
            }
loop:       while (i > 0) {
                do if (--lower < 0) {
                    center -= i;
                    arraycopy(index, i, index, 0, center);
                    break loop;
                } while (isNaN(data.get(lower)));
                index[--i] = lower;
            }
            /*
             * La boucle suivante copie 'upper' et les index qui le suivent dans le tableau 'index'.
             * Si on a atteint la fin des donn�es sans avoir r�ussi � copier tous les index, on
             * d�calera vers la droite les index qui ont �t� copi�s et on tentera de combler le trou
             * cr�� � gauche en copiant d'autres index qui pr�c�daient 'lower'.
             */
            i = center;
            index[i++] = upper;
loop:       while (i < index.length) {
                do if (++upper >= length) {
                    int remainder = index.length-i;
                    // center += remainder; // (not needed)
                    arraycopy(index, 0, index, remainder, i);
                    i = remainder;
                    do {
                        do if (--lower < 0) {
                            return false;
                        } while (isNaN(data.get(lower)));
                        index[--i] = lower;
                    } while (i > 0);
                    break loop;
                } while (isNaN(data.get(upper)));
                index[i++] = upper;
            }
        } else if (center > 0) {
            index[0] = lower;
        }
        assert isSorted(index) : Arrays.toString(index);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public double getInterval() {
        int k0, k1;
        /*
         *	Rep�re les index pointant vers les donn�es � utiliser pour le calcul
         *	de l'intervalle. En l'absence de NaN on obtient:
         *
         *		klo0 = lower			khi0 = upper
         *		klo1 = klo0-1			khi1 = upper+1
         *
         *	Le schema ci-dessous donne un exemple de la fa�on dont se comporte
         *	le code en la pr�sence de NaN pour des 'lower' et 'upper' donn�s.
         *
         *		                   lower          upper
         *		                     |              |
         *		140  145  150  NaN  160  165  170  NaN  180  185  190
         *		           ^         ^         ^         ^
         *		          k1        k0         k0        k1
         */
        k0 = k1 = lower;
        final int length = data.limit();
        while (isNaN(data.get(k0))) {
            if (++k0 >= length) {
                return NaN;
            }
        }
        do if (--k1 < 0) {
            k1 = k0;
            do if (++k0 >= length) {
                return NaN;
            } while (isNaN(data.get(k0)));
            break;
        } while (isNaN(data.get(k1)));
        double x0 = data.get(k0);
        final double xlo = (data.get(k1)-x0) / (k1-k0) * (lower-k0-0.5) + x0;

        k0 = k1 = upper;
        while (isNaN(data.get(k0))) {
            if (--k0 < 0) {
                return NaN;
            }
        }
        do if (++k1 >= length) {
            k1 = k0;
            do if (--k0 < 0) {
                return NaN;
            } while (isNaN(data.get(k0)));
            break;
        }
        while (isNaN(data.get(k1)));
        x0 = data.get(k0);
        return (data.get(k1)-x0) / (k1-k0)*(upper-k0+0.5)+x0 - xlo;
    }

    /**
     * Retourne une copie de ce vecteur. Le buffer {@link #data} est copi� (afin de pouvoir �tre
     * utilis� dans un autre thread), mais pas les donn�es qu'il contient. Cette copie reste donc
     * relativement �conomique.
     */
    @Override
    public BufferedOrderedVector clone() {
        final BufferedOrderedVector copy = (BufferedOrderedVector) super.clone();
        final Field data;
        try {
            data = BufferedOrderedVector.class.getField("data");
        } catch (NoSuchFieldException e) {
            // Should never happen, since the field exists.
            throw new AssertionError(e);
        }
        data.setAccessible(true);
        try {
            data.set(copy, copy.data.duplicate());
        } catch (IllegalAccessException e) {
            // Should never happen, since we made the field accessible.
            throw new AssertionError(e);
        }
        data.setAccessible(false);
        return copy;
    }
}
