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
package net.sicade.observation.fishery;

// J2SE dependencies
import java.awt.Color;
import java.util.Locale;

// Sicade dependencies
import net.sicade.observation.Phenomenon;


/**
 * Repr�sentation d'une esp�ce animale.
 * 
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public interface Species extends Phenomenon {
    /**
     * Constante d�signant la langue "Latin".
     * Souvent utilis�e pour nommer les esp�ces.
     *
     * @see Locale#ENGLISH
     * @see Locale#FRENCH
     * @see Locale#SPANISH
     */
    Locale LATIN = new Locale("la", "");

    /**
     * Constante d�signant les codes de la FAO. Il ne s'agit pas d'une langue � proprement
     * parler. Toutefois, cette constante est utile pour d�signant la fa�on de repr�senter
     * le {@linkplain #getName nom d'une esp�ce}.
     */
    Locale FAO = new Locale("fao", "");

    /**
     * Retourne les langues dans lesquelles peuvent �tre exprim�es le nom de cette esp�ce.
     */
    Locale[] getLocales();

    /**
     * Retourne le nom de cette esp�ce dans la langue sp�cifi�e. Cette langue peut �tre typiquement
     * {@linkplain Locale#ENGLISH l'anglais}, {@linkplain Locale#FRENCH le fran�ais} ou {@linkplain
     * Locale#SPANISH l'espagnol}. La "langue" {@link #FAO} fait partie des valeurs l�gales. Elle
     * signifie que la cha�ne d�sir�e est un code repr�sentant l'esp�ce. Par exemple, le code de
     * la FAO pour l'albacore (<cite>Thunnus albacares</cite>, ou <cite>Yellowfin tuna</cite> en
     * anglais) est "YFT".
     * <p>
     * Si la langue sp�cifi�e est {@code null}, alors cette m�thode tentera de retourner
     * un nom dans la {@linkplain Locale#getDefault() langue par d�faut du syst�me}. Si
     * aucun nom n'est disponible dans la langue du syst�me, alors cette m�thode tentera
     * de retourner un nom dans une autre langue. Le code de l'esp�ce (tel que retourn�
     * par {@code getName(FAO)}) ne sera retourn� qu'en dernier recours.
     *
     * @param  locale Langue d�sir�e pour le nom de l'esp�ce, or {@code null} pour
     *         un nom dans une langue par d�faut.
     * @return Le nom de l'esp�ce dans la langue sp�cifi�e, ou {@code null} si
     *         aucun nom n'est disponible dans la langue sp�cifi�e.
     */
    String getName(Locale locale);
    
    /**
     * Construit un nouvel icone repr�sentant cette esp�ce.
     */
    Icon getIcon();

    /**
     * Ic�ne repr�sentant une esp�ce. Un ic�ne peut servir � positionner
     * sur une carte plusieurs individus d'une m�me esp�ce, et peut aussi
     * appara�tre devant une �tiquette dans les listes d�roulantes.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     * @author Antoine Hnawia
     */
    public interface Icon extends javax.swing.Icon {
        /**
         * Retourne l'esp�ce associ�e � cet ic�ne.
         */
        Species getSpecies();

        /**
         * Retourne la couleur de cet ic�ne.
         */
        Color getColor();

        /**
         * Change la couleur de cet ic�ne.
         */
        void setColor(Color color);
    }
}
