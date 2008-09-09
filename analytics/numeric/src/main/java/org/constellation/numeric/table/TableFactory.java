/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2006, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.numeric.table;

// J2SE dependencies and extensions
import java.nio.DoubleBuffer;
import javax.vecmath.MismatchedSizeException;


/**
 * Une fabrique de {@linkplain Table tables} à partir de vecteurs <var>x</var> et <var>y</var>
 * spécifiés.
 * <p>
 * Les données du vecteur <var>x</var> doivent obligatoirement être en ordre croissant ou
 * décroissant. Les premières données prises en compte par la table seront les données à la
 * {@linkplain DoubleBuffer#position() position courante} des vecteurs. Le nombre de données
 * pris en compte sera le {@linkplain DoubleBuffer#remaining() nombre de données restantes}
 * dans chacun de ces vecteurs. Après la construction de la table, les changements de
 * {@linkplain DoubleBuffer#position() position} ou de {@linkplain DoubleBuffer#limit() limite}
 * des objets {@code x} et {@code y} donnés en argument n'affecteront pas cette table. Toutefois
 * tout changement des données contenues dans les buffers affecteront cette table.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class TableFactory {
    /**
     * La fabrique par défaut.
     */
    private static TableFactory factory;

    /**
     * Construit une fabrique par défaut.
     */
    protected TableFactory() {
    }

    /**
     * Retourne l'instance par défaut de {@link TableFactory}.
     */
    public static TableFactory getDefault() {
        // Pas besoin de synchroniser. Ce n'est pas bien grave si deux instances sont créées.
        if (factory == null) {
            factory = new TableFactory();
        }
        return factory;
    }

    /**
     * Construit une table à partir du buffer des <var>x</var> et des buffers des <var>y</var>
     * spécifiés. La plupart des autres méthodes {@code create} de cette classes vont ultimement
     * appeler cette méthode.
     *
     * @param x Le vecteur des <var>x</var>, obligatoirement être en ordre croissant ou décroissant.
     * @param y Les vecteurs des <var>y</var>.
     * @param type Type d'interpolation à utiliser pour obtenir une valeur <var>y</var> à partir
     *        d'une position <var>x</var>.
     * @throws IllegalArgumentException si les valeurs des <var>x</var> ne sont pas strictement
     *         croissantes ou décroissantes.
     * @throws MismatchedSizeException si deux vecteurs n'ont pas la même longueur.
     */
    public Table create(final DoubleBuffer x, final DoubleBuffer[] y, final Interpolation type)
            throws IllegalArgumentException, MismatchedSizeException
    {
        final OrderedVector ord = new BufferedOrderedVector(x);
        switch (ord.getDataOrder()) {
            case STRICTLY_ASCENDING:  // Fall through
            case STRICTLY_DESCENDING: break;
            default: throw new IllegalArgumentException("Les valeurs des x ne sont pas croissantes ou décroissantes.");
        }
        switch (type) {
            case NEAREST: return new Table(ord, y);
            default: throw new IllegalArgumentException(String.valueOf(type));
        }
    }

    /**
     * Construit une table à partir du tableau des <var>x</var> et des tableaux des <var>y</var>
     * spécifiés. Cette méthode de commodité délègue son travail à {@link #create(DoubleBuffer,
     * DoubleBuffer[])}.
     *
     * @param x Le vecteur des <var>x</var>, obligatoirement être en ordre croissant ou décroissant.
     * @param y Les vecteurs des <var>y</var>.
     * @param type Type d'interpolation à utiliser pour obtenir une valeur <var>y</var> à partir
     *        d'une position <var>x</var>.
     * @throws IllegalArgumentException si les valeurs des <var>x</var> ne sont pas strictement
     *         croissantes ou décroissantes.
     * @throws MismatchedSizeException si deux vecteurs n'ont pas la même longueur.
     */
    public Table create(final double[] x, final double[][] y, final Interpolation type)
            throws IllegalArgumentException, MismatchedSizeException
    {
        final DoubleBuffer[] buffers = new DoubleBuffer[y.length];
        for (int i=0; i<y.length; i++) {
            buffers[i] = DoubleBuffer.wrap(y[i]);
        }
        return create(DoubleBuffer.wrap(x), buffers, type);
    }

    /**
     * Construit une table à partir du tableau des <var>x</var> et des <var>y</var> spécifiés.
     * Cette méthode de commodité délègue son travail à {@link #create(double[], double[][])}.
     *
     * @param x Le vecteur des <var>x</var>, obligatoirement être en ordre croissant ou décroissant.
     * @param y Le vecteur des <var>y</var>.
     * @param type Type d'interpolation à utiliser pour obtenir une valeur <var>y</var> à partir
     *        d'une position <var>x</var>.
     * @throws IllegalArgumentException si les valeurs des <var>x</var> ne sont pas strictement
     *         croissantes ou décroissantes.
     * @throws MismatchedSizeException si deux vecteurs n'ont pas la même longueur.
     */
    public Table create(final double[] x, final double[] y, final Interpolation type)
            throws IllegalArgumentException, MismatchedSizeException
    {
        return create(x, new double[][] {y}, type);
    }
}
