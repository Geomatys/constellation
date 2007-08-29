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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sicade.observation.fishery;

// J2SE dependencies
import java.awt.Color;
import java.util.Locale;
import org.opengis.observation.Phenomenon;


/**
 * Représentation d'une espèce animale.
 * 
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public interface Species extends Phenomenon {
    /**
     * Constante désignant la langue "Latin".
     * Souvent utilisée pour nommer les espèces.
     *
     * @see Locale#ENGLISH
     * @see Locale#FRENCH
     * @see Locale#SPANISH
     */
    Locale LATIN = new Locale("la", "");

    /**
     * Constante désignant les codes de la FAO. Il ne s'agit pas d'une langue à proprement
     * parler. Toutefois, cette constante est utile pour désignant la façon de représenter
     * le {@linkplain #getName nom d'une espèce}.
     */
    Locale FAO = new Locale("fao", "");

    /**
     * Retourne les langues dans lesquelles peuvent être exprimées le nom de cette espèce.
     */
    Locale[] getLocales();

    /**
     * Retourne le nom de cette espèce dans la langue spécifiée. Cette langue peut être typiquement
     * {@linkplain Locale#ENGLISH l'anglais}, {@linkplain Locale#FRENCH le français} ou {@linkplain
     * Locale#SPANISH l'espagnol}. La "langue" {@link #FAO} fait partie des valeurs légales. Elle
     * signifie que la chaîne désirée est un code représentant l'espèce. Par exemple, le code de
     * la FAO pour l'albacore (<cite>Thunnus albacares</cite>, ou <cite>Yellowfin tuna</cite> en
     * anglais) est "YFT".
     * <p>
     * Si la langue spécifiée est {@code null}, alors cette méthode tentera de retourner
     * un nom dans la {@linkplain Locale#getDefault() langue par défaut du système}. Si
     * aucun nom n'est disponible dans la langue du système, alors cette méthode tentera
     * de retourner un nom dans une autre langue. Le code de l'espèce (tel que retourné
     * par {@code getName(FAO)}) ne sera retourné qu'en dernier recours.
     *
     * @param  locale Langue désirée pour le nom de l'espèce, or {@code null} pour
     *         un nom dans une langue par défaut.
     * @return Le nom de l'espèce dans la langue spécifiée, ou {@code null} si
     *         aucun nom n'est disponible dans la langue spécifiée.
     */
    String getName(Locale locale);
    
    /**
     * Construit un nouvel icone représentant cette espèce.
     */
    Icon getIcon();

    /**
     * Icône représentant une espèce. Un icône peut servir à positionner
     * sur une carte plusieurs individus d'une même espèce, et peut aussi
     * apparaître devant une étiquette dans les listes déroulantes.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     * @author Antoine Hnawia
     */
    public interface Icon extends javax.swing.Icon {
        /**
         * Retourne l'espèce associée à cet icône.
         */
        Species getSpecies();

        /**
         * Retourne la couleur de cet icône.
         */
        Color getColor();

        /**
         * Change la couleur de cet icône.
         */
        void setColor(Color color);
    }
}
