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
package net.sicade.coverage.catalog;

// JUnit dependencies
import junit.framework.TestCase;

/**
 * Teste l'utilisation de la classe {@link Catalog}.
 * 
 * 
 * @author Martin Desruisseaux
 * @version $Id$
 * @todo Ajouter des tests sur le même modèle que ceux que l'on peut trouver dans le projet SICADE.
 */
public class ObservationsTest extends TestCase {
    /**
     * Construit un test.
     */
    public ObservationsTest(final String name) {
        super(name);
    }

    /**
     * Teste la création d'un modèle de table à partir d'une couche.
     */
    public void testCoverageTableModel() throws CatalogException {
        final Layer layer = Catalog.getDefault().getLayer("CHL (Monde - mensuelles)");
        final CoverageTableModel model = new CoverageTableModel();
        model.setLayer(layer);
    }
}
