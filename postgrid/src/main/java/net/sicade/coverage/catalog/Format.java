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
 */
package net.sicade.coverage.catalog;

import java.util.Locale;
import org.opengis.coverage.SampleDimension;
import org.geotools.gui.swing.tree.MutableTreeNode;

/**
 * Information sur un format d'image. Le {@linkplain #getName nom} d'une entrée devrait être
 * le nom MIME du format d'image.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface Format extends Element {
    /**
     * Retourne les listes des bandes qui permettent de décoder les valeurs des paramètres
     * géophysiques. Cette méthode peut retourner plusieurs objets {@link SampleDimension},
     * un par bande. Leur type (géophysique ou non) correspond au type des images dans leur
     * format natif. Par exemple les valeurs des pixels seront des entiers (<code>{@linkplain
     * org.geotools.coverage.GridSampleDimension#geophysics geophysics}(false)</code>) si l'image
     * est enregistrée au format PNG, tandis que les plages de valeurs peuvent être des nombres
     * réels (<code>{@linkplain org.geotools.coverage.GridSampleDimension#geophysics geophysics}(true)</code>)
     * si l'image est enregistrée dans un format brut ou ASCII.
     */
    SampleDimension[] getSampleDimensions();

    /**
     * Retourne une arborescence qui représentera ce format, ses bandes et les catégories de
     * chaque bandes.
     *
     * @param  locale La langue à utiliser pour formatter le contenu textuel de l'arborescence.
     * @return La racine de l'arborescence.
     */
    MutableTreeNode getTree(Locale locale);
}
