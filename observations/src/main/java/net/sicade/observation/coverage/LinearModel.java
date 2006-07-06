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
package net.sicade.observation.coverage;

// J2SE dependencies
import java.util.List;
import java.util.Locale;
import java.io.Writer;
import java.io.IOException;

// Sicade dependencies
import net.sicade.observation.Element;


/**
 * Un mod�le lin�aire.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface LinearModel extends Model {
    /**
     * Retourne les termes d'un mod�le lin�aire calculant le param�tre. Un param�tre peut �tre le
     * r�sultat d'une combinaison de d'autres param�tres, par exemple sous la forme de l'�quation
     * suivante:
     *
     * <p align="center">{@code PP} = <var>C</var><sub>0</sub> +
     * <var>C</var><sub>1</sub>&times;{@code SST} +
     * <var>C</var><sub>2</sub>&times;{@code SLA} +
     * <var>C</var><sub>3</sub>&times;{@code SST}&times;{@code SLA} + ...</p>
     *
     * Chacun des termes � droite du signe = est d�crit par un objet
     * {@link net.sicade.observation.coverage.LinearModel.Term}.
     * Ces descriptions incluent le coefficient <var>C</var><sub>n</sub>,
     * qui r�sulte g�n�ralement d'une r�gression lin�aire multiple.
     *
     * @return La liste de tous les termes composant le mod�le lin�aire. Cette liste est immutable.
     */
    List<Term> getTerms();

    /**
     * �crit le mod�le lin�aire vers le flot de sortie sp�cifi�. Cette m�thode utilisera une
     * ligne par terme du mod�le lin�aire, et tentera d'aligner les termes comme dans un tableau.
     *
     * @param  out Le flot de sortie dans lequel �crire.
     * @param  locale Les conventions locales � utiliser, o� {@code null} pour les conventions
     *         par d�faut.
     * @throws IOException si une erreur est survenue lors de l'�criture.
     */
    void print(final Writer out, final Locale locale) throws IOException;

    /**
     * Un terme dans un mod�le lin�aire. Un mod�le lin�aire peut s'�crire de la forme suivante:
     *
     * <p align="center"><var>y</var> = <var>C</var><sub>0</sub> +
     * <var>C</var><sub>1</sub>&times;<var>x</var><sub>1</sub> +
     * <var>C</var><sub>2</sub>&times;<var>x</var><sub>2</sub> +
     * <var>C</var><sub>3</sub>&times;<var>x</var><sub>3</sub> + ...</p>
     *
     * Dans ce mod�le, le terme <var>C</var><sub>0</sub> est repr�sent� par un objet
     * {@code LinearModel.Term}, le terme <var>C</var><sub>1</sub>&times;<var>x</var><sub>1</sub>
     * par un autre objet {@code LinearModel.Term}, et ainsi de suite.
     * <p>
     * Les variables ind�pendantes <var>x</var><sub>1</sub>, <var>x</var><sub>2</sub>,
     * <cite>etc.</cite> sont les {@linkplain Descriptor descripteurs du paysage oc�anique},
     * eux-m�mes d�riv�s d'une {@linkplain Series s�rie d'images} repr�sentant un param�tre
     * environnemental.
     * <p>
     * La variable d�pendante <var>y</var> sera stock�e dans un nouveau param�tre 
     * environnemental (par exemple un param�tre appel� "potentiel de p�che"). 
     * Elle pourra donc servir d'entr�e � un autre mod�le lin�aire.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    public interface Term extends Element {
        /**
         * Retourne le coefficient <var>C</var> de ce terme. Ce coefficient a 
         * g�n�ralement �t� obtenu par une r�gression lin�aire multiple.
         */
        double getCoefficient();

        /**
         * Retourne les descripteurs du paysage oc�anique composant ce terme. Par exemple, le terme
         * {@code this} pourrait �tre <var>C</var>&times;{@code SST}&times;{@code SLA}, o� <var>C</var>
         * est le {@linkplain #getCoefficient coefficient} d�termin� par la r�gression lin�aire, tandis
         * que {@code SST} et {@code SLA} sont les valeurs {@linkplain Distribution#normalize normalis�es}
         * de temp�rature de surface et d'anomalie de la hauteur de l'eau respectivement. Pour cet exemple,
         * {@code getDescriptors()} retournerait dans une liste les deux descripteurs {@code SST} et
         * {@code SLA}.
         */
        List<Descriptor> getDescriptors();
    }
}
