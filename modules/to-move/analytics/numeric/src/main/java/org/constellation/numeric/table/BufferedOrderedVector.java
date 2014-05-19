/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.numeric.table;

// J2SE dependencies
import java.util.Arrays;
import java.nio.DoubleBuffer;
import java.lang.reflect.Field;

// Static imports
import static java.lang.Math.abs;
import static java.lang.Double.NaN;
import static java.lang.Double.isNaN;
import static java.lang.System.arraycopy;
import static org.constellation.numeric.table.DataOrder.*;
import static org.apache.sis.util.ArraysExt.isSorted;


/**
 * Un vecteur ordonné dont les données sont spécifiées dans un buffer.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class BufferedOrderedVector extends OrderedVector {
    /**
     * Les valeurs de ce vecteur. Il est de la responsabilité de l'utilisateur de s'assurer que
     * ces données restent en ordre croissant ou décroissant. Pour des raisons de performance,
     * ça ne sera pas vérifié.
     */
    private final DoubleBuffer data;

    /**
     * Construit un vecteur ordonné pour les données spécifiées. Les données doivent obligatoirement
     * être en ordre croissant ou décroissant. Pour des raisons de performances (et aussi parce qu'il
     * n'est pas possible de s'assurer que l'utilisateur ne modifiera pas les données après la
     * construction de ce vecteur), ça ne sera pas vérifié.
     * <p>
     * Les premières données prises en compte seront les données à la {@linkplain DoubleBuffer#position()
     * position courante}. Le nombre de données prises en compte sera le {@linkplain DoubleBuffer#remaining()
     * nombre de données restantes}. Après la construction du vecteur, les changements de
     * {@linkplain DoubleBuffer#position() position} ou de {@linkplain DoubleBuffer#limit() limite}
     * de l'objet {@code data} donné en argument n'affecteront pas ce vecteur. Toutefois tout changement
     * des données contenues dans le buffer affecteront ce vecteur.
     */
    public BufferedOrderedVector(final DoubleBuffer data) {
        this.data = data.slice();
    }

    /**
     * Retourne l'ordre des données dans ce vecteur. Cette méthode évalue l'ordre chaque fois qu'elle
     * est invoquée. Elle peut donc servir à déterminer si un changement de données a eu un impact sur
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
     * Retourne la valeur à l'index spécifié. La valeur de cet index peut varier de 0 inclusivement
     * jusqu'à {@link #length} exclusivement.
     *
     * @param  index La valeur de l'index.
     * @return La valeur du vecteur à l'index spécifié.
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
         * Positionne la plage [upper..lower] aux extrémités du vecteur. S'il y a des NaN, ils seront
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
         * Si la valeur de x spécifiée n'est pas comprise dans la plage de valeurs de ce vecteur,
         * ignore les NaN et vérifie si la valeur est égale à celle de l'une des extrémités. Note:
         * le '!' dans l'expression ci-dessous est nécessaire pour attraper les valeurs NaN.
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
         * A partir de ce point, on a l'assurance qu'il existe au moins une donnée autre que NaN
         * et que la valeur de 'x' est comprise entre 'xlo' et 'xhi'. On peut lancer la recherche
         * bilinéaire.
         */
search: while (upper - lower > 1) {
            int k = (upper+lower) >> 1;     // Indice au centre de la plage [lower..upper]
            int scan = 0;                   // Utilisé en cas de valeurs NaN seulement.
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
                 * Le code suivant ne sera exécuté que si l'on vient de tomber sur un NaN.
                 * On recherche linéairement une valeur qui ne soit pas NaN autour de k en
                 * testant dans l'ordre k-1, k+1, k-2, k+2, k-3, k+3, etc.
                 */
                assert isNaN(xk) || isNaN(x) : xk;
                // Dans la ligne ci-dessous, le premier (k-scan) restore la valeur originale de k
                // tandis que le second (k-scan) anticipe sur le changement de 'scan' à venir.
                k -= (scan << 1);
                if (scan >= 0) {
                    scan = ~scan;  // Equivaut à (-scan - 1)
                    if (--k > lower) {
                        assert k-scan == (upper+lower) >> 1 : scan; // (k-scan) devrait être la valeur originale.
                        continue;
                    }
                    k -= (scan << 1);  // Annule le test de 'k-scan' et passe au test de 'k+scan'.
                }
                scan = -scan;
                assert scan > 0 : scan;
                assert k-scan == (upper+lower) >> 1 : scan; // (k-scan) devrait être la valeur originale.
            } while (k < upper);
            /*
             * On atteint ce point si aucune valeur autre que NaN n'a été trouvée entre
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
             * Si 'upper' et 'lower' sont identiques, on n'écrira pas 'lower' afin de ne pas
             * répéter deux fois le même index. On écrira seulement 'upper.' La boucle 'loop'
             * copie au début du tableau 'index' les index qui précèdent 'lower'.
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
             * Si on a atteint la fin des données sans avoir réussi à copier tous les index, on
             * décalera vers la droite les index qui ont été copiés et on tentera de combler le trou
             * créé à gauche en copiant d'autres index qui précédaient 'lower'.
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
         *	Repère les index pointant vers les données à utiliser pour le calcul
         *	de l'intervalle. En l'absence de NaN on obtient:
         *
         *		klo0 = lower			khi0 = upper
         *		klo1 = klo0-1			khi1 = upper+1
         *
         *	Le schema ci-dessous donne un exemple de la façon dont se comporte
         *	le code en la présence de NaN pour des 'lower' et 'upper' donnés.
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
     * Retourne une copie de ce vecteur. Le buffer {@link #data} est copié (afin de pouvoir être
     * utilisé dans un autre thread), mais pas les données qu'il contient. Cette copie reste donc
     * relativement économique.
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
