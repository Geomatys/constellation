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

import java.util.Comparator;
import net.sicade.observation.Observable;
import net.sicade.observation.Distribution;     // Pour javadoc
import net.sicade.observation.CatalogException;


/**
 * Un descripteur du paysage oc�anique. Un descripteur est une variable explicative
 * donn�e en entr� aux mod�les lin�aires. Un descripteur du paysage oc�anique comprend:
 * <p>
 * <ul>
 *   <li>une {@linkplain Series s�rie d'images} contenant les donn�es du param�tre environnemental;</li>
 *   <li>une {@linkplain Operation op�ration} � appliquer (par exemple un op�rateur de Sobel pour
 *       calculer les gradients);</li>
 *   <li>une {@linkplain LocationOffset position relative} � laquelle �valuer le r�sultat de
 *       l'op�ration sur le param�tre environnemental;</li>
 *   <li>une {@linkplain Distribution distribution th�orique}, que l'on essaiera de ramener � la
 *       distribution normale par un changement de variable.</li>
 * </ul>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public interface Descriptor extends Observable {
    /**
     * Un comparateur pour classer des descripteurs en ordre croissant de
     * {@linkplain LocationOffset#getDayOffset d�calage temporel}. Si deux
     * descripteurs ont le m�me d�calage temporels, alors d'autres crit�res
     * telles que le nom de la s�rie peuvent �tre utilis�s.
     */
    Comparator<Descriptor> TIME_ORDER = new Comparator<Descriptor>() {
        public int compare(final Descriptor d1, final Descriptor d2) {
            final double dt1 = d1.getLocationOffset().getDayOffset();
            final double dt2 = d2.getLocationOffset().getDayOffset();
            if (dt1 < dt2) return -1;
            if (dt1 > dt2) return +1;
            int c = d1.getPhenomenon().getName().compareTo(d2.getPhenomenon().getName());
            if (c != 0) {
                return c;
            }
            return d1.getName().compareTo(d2.getName());
        }
    };

    /**
     * Retourne la s�rie d'images d'o� proviennent les donn�es du param�tre environnemental �tudi�.
     * Il peut s'agir par exemple d'une s�rie d'images de temp�rature.
     */
    Series getPhenomenon();

    /**
     * Retourne l'op�ration appliqu�e sur les images de la s�rie. Il peut s'agir par exemple
     * d'un op�rateur de gradient. Si aucune op�ration n'est appliqu�e, alors cette m�thode
     * retourne une op�ration identit�.
     */
    Operation getProcedure();

    /**
     * Retourne la position relative � laquelle �valuer les images de la s�rie.
     * Cette position est relative aux positions des observations.
     */
    LocationOffset getLocationOffset();

    /**
     * Retourne le num�ro de la bande dans laquelle extraire les valeurs des images.
     */
    short getBand();

    /**
     * Retourne {@code true} si ce descripteur est le <cite>descripteur identit�</cite>.
     * Le "descripteur identit�" est un descripteur artificiel repr�sentant une image
     * dont tous les pixels auraient la valeur 1. Il est utilis� dans des expressions de
     * la forme <code>y = C0 + C1*x + C2*x� + ...</code>, ou {@code C0} peut s'�crire
     * <code>C0&times;identit�</code>.
     */
    boolean isIdentity();

    /**
     * Retourne une vue des donn�es de ce descripteur sous forme de fonction. Chaque valeur peut
     * �tre �valu�e � une position (<var>x</var>,<var>y</var>,<var>t</var>), en faisant intervenir
     * des interpolations si n�cessaire. Cette m�thode retourne une fonction plus �labor�e que celle
     * de {@link Series#getCoverage} pour les raisons suivantes:
     * <p>
     * <ul>
     *   <li>Une {@linkplain #getProcedure op�ration} peut-�tre appliqu�e sur les images (par
     *       exemple calcul de un gradient)</li>
     *   <li>Un {@linkplain #getLocationOffset d�calage spatio-temporel} peut �tre appliqu�e
     *       sur la position � laquelle �valuer les donn�es.</li>
     *   <li>En cas de donn�e manquante, la {@linkplain Series#getFallback s�rie de second
     *       recours} est test�e.</li>
     *   <li>Les donn�es peuvent �tre �valu�es sur un serveur distant sans jamais transmettre
     *       d'images compl�tes via le r�seau.</li>
     * </ul>
     *
     * @throws CatalogException si la fonction n'a pas pu �tre construite.
     */
    DynamicCoverage getCoverage() throws CatalogException;
}
