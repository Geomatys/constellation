/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2006, Institut de Recherche pour le Développement
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
package net.sicade.image;

// J2SE dependencies
import java.awt.Color;
import java.io.IOException;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Geotools dependencies
import org.geotools.image.io.PaletteFactory;


/**
 * Teste le fonctionnement de {@link Utilities}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class UtilitiesTest extends TestCase {
    /**
     * Exécute la suite de tests à partir de la ligne de commande.
     */
    public static void main(final String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Retourne la suite de tests.
     */
    public static Test suite() {
        return new TestSuite(UtilitiesTest.class);
    }

    /**
     * Construit la suite de tests.
     */
    public UtilitiesTest(final String name) {
        super(name);
    }

    /**
     * Teste la palette retournée par {@link Utilities#getPaletteFactory}.
     */
    public void testPalette() throws IOException {
        final PaletteFactory palette = Utilities.getPaletteFactory(null);
        final Color[] colors = palette.getColors("seawifs");
        assertNotNull("Les ressources ne sont pas accessibles.", colors);
        assertEquals(new Color(132, 0, 124), colors[0]);
        assertEquals(new Color(195, 0,   0), colors[colors.length-1]);
    }
}
