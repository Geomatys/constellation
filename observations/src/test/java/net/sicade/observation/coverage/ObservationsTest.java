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

// JUnit dependencies
import junit.framework.TestCase;

// Sicade dependencies
import net.sicade.observation.Observations;
import net.sicade.observation.CatalogException;


/**
 * Teste l'utilisation de la classe {@link Observations}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo Ajouter des tests sur le m�me mod�le que ceux que l'on peut trouver dans le projet SICADE.
 */
public class ObservationsTest extends TestCase {
    /**
     * Construit un test.
     */
    public ObservationsTest(final String name) {
        super(name);
    }

    /**
     * Teste la cr�ation d'un mod�le de table � partir d'une s�rie.
     */
    public void testCoverageTableModel() throws CatalogException {
        final Series series = Observations.getDefault().getSeries("CHL (Monde - mensuelles)");
        final CoverageTableModel model = new CoverageTableModel();
        model.setSeries(series);
    }
}
