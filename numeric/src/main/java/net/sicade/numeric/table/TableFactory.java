/*
 * Sicade - Syst�mes int�gr�s de connaissances pour l'aide � la d�cision en environnement
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

// J2SE dependencies and extensions
import java.nio.DoubleBuffer;
import javax.vecmath.MismatchedSizeException;


/**
 * Une fabrique de {@linkplain Table tables} � partir de vecteurs <var>x</var> et <var>y</var>
 * sp�cifi�s.
 * <p>
 * Les donn�es du vecteur <var>x</var> doivent obligatoirement �tre en ordre croissant ou
 * d�croissant. Les premi�res donn�es prises en compte par la table seront les donn�es � la
 * {@linkplain DoubleBuffer#position() position courante} des vecteurs. Le nombre de donn�es
 * pris en compte sera le {@linkplain DoubleBuffer#remaining() nombre de donn�es restantes}
 * dans chacun de ces vecteurs. Apr�s la construction de la table, les changements de
 * {@linkplain DoubleBuffer#position() position} ou de {@linkplain DoubleBuffer#limit() limite}
 * des objets {@code x} et {@code y} donn�s en argument n'affecteront pas cette table. Toutefois
 * tout changement des donn�es contenues dans les buffers affecteront cette table.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class TableFactory {
    /**
     * La fabrique par d�faut.
     */
    private static TableFactory factory;

    /**
     * Construit une fabrique par d�faut.
     */
    protected TableFactory() {
    }

    /**
     * Retourne l'instance par d�faut de {@link TableFactory}.
     */
    public static TableFactory getDefault() {
        // Pas besoin de synchroniser. Ce n'est pas bien grave si deux instances sont cr��es.
        if (factory == null) {
            factory = new TableFactory();
        }
        return factory;
    }

    /**
     * Construit une table � partir du buffer des <var>x</var> et des buffers des <var>y</var>
     * sp�cifi�s. La plupart des autres m�thodes {@code create} de cette classes vont ultimement
     * appeler cette m�thode.
     *
     * @param x Le vecteur des <var>x</var>, obligatoirement �tre en ordre croissant ou d�croissant.
     * @param y Les vecteurs des <var>y</var>.
     * @param type Type d'interpolation � utiliser pour obtenir une valeur <var>y</var> � partir
     *        d'une position <var>x</var>.
     * @throws IllegalArgumentException si les valeurs des <var>x</var> ne sont pas strictement
     *         croissantes ou d�croissantes.
     * @throws MismatchedSizeException si deux vecteurs n'ont pas la m�me longueur.
     */
    public Table create(final DoubleBuffer x, final DoubleBuffer[] y, final Interpolation type)
            throws IllegalArgumentException, MismatchedSizeException
    {
        final OrderedVector ord = new BufferedOrderedVector(x);
        switch (ord.getDataOrder()) {
            case STRICTLY_ASCENDING:  // Fall through
            case STRICTLY_DESCENDING: break;
            default: throw new IllegalArgumentException("Les valeurs des x ne sont pas croissantes ou d�croissantes.");
        }
        switch (type) {
            case NEAREST: return new Table(ord, y);
            default: throw new IllegalArgumentException(String.valueOf(type));
        }
    }

    /**
     * Construit une table � partir du tableau des <var>x</var> et des tableaux des <var>y</var>
     * sp�cifi�s. Cette m�thode de commodit� d�l�gue son travail � {@link #create(DoubleBuffer,
     * DoubleBuffer[])}.
     *
     * @param x Le vecteur des <var>x</var>, obligatoirement �tre en ordre croissant ou d�croissant.
     * @param y Les vecteurs des <var>y</var>.
     * @param type Type d'interpolation � utiliser pour obtenir une valeur <var>y</var> � partir
     *        d'une position <var>x</var>.
     * @throws IllegalArgumentException si les valeurs des <var>x</var> ne sont pas strictement
     *         croissantes ou d�croissantes.
     * @throws MismatchedSizeException si deux vecteurs n'ont pas la m�me longueur.
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
     * Construit une table � partir du tableau des <var>x</var> et des <var>y</var> sp�cifi�s.
     * Cette m�thode de commodit� d�l�gue son travail � {@link #create(double[], double[][])}.
     *
     * @param x Le vecteur des <var>x</var>, obligatoirement �tre en ordre croissant ou d�croissant.
     * @param y Le vecteur des <var>y</var>.
     * @param type Type d'interpolation � utiliser pour obtenir une valeur <var>y</var> � partir
     *        d'une position <var>x</var>.
     * @throws IllegalArgumentException si les valeurs des <var>x</var> ne sont pas strictement
     *         croissantes ou d�croissantes.
     * @throws MismatchedSizeException si deux vecteurs n'ont pas la m�me longueur.
     */
    public Table create(final double[] x, final double[] y, final Interpolation type)
            throws IllegalArgumentException, MismatchedSizeException
    {
        return create(x, new double[][] {y}, type);
    }
}
