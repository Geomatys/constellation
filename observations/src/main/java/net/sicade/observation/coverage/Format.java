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

import java.util.Locale;
import org.opengis.coverage.SampleDimension;
import org.geotools.gui.swing.tree.MutableTreeNode;
import net.sicade.observation.Element;


/**
 * Information sur un format d'image. Le {@linkplain #getName nom} d'une entr�e devrait �tre
 * le nom MIME du format d'image.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface Format extends Element {
    /**
     * Retourne les listes des bandes qui permettent de d�coder les valeurs des param�tres
     * g�ophysiques. Cette m�thode peut retourner plusieurs objets {@link SampleDimension},
     * un par bande. Leur type (g�ophysique ou non) correspond au type des images dans leur
     * format natif. Par exemple les valeurs des pixels seront des entiers (<code>{@linkplain
     * org.geotools.coverage.GridSampleDimension#geophysics geophysics}(false)</code>) si l'image
     * est enregistr�e au format PNG, tandis que les plages de valeurs peuvent �tre des nombres
     * r�els (<code>{@linkplain org.geotools.coverage.GridSampleDimension#geophysics geophysics}(true)</code>)
     * si l'image est enregistr�e dans un format brut ou ASCII.
     */
    SampleDimension[] getSampleDimensions();

    /**
     * Retourne une arborescence qui repr�sentera ce format, ses bandes et les cat�gories de
     * chaque bandes.
     *
     * @param  locale La langue � utiliser pour formatter le contenu textuel de l'arborescence.
     * @return La racine de l'arborescence.
     */
    MutableTreeNode getTree(Locale locale);
}
