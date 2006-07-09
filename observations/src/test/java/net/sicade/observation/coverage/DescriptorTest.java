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
package net.sicade.observation.coverage;

import java.util.Locale;
import java.io.IOException;
import java.sql.SQLException;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.geotools.resources.Arguments;
import org.geotools.util.MonolineFormatter;
import org.geotools.coverage.SpatioTemporalCoverage3D;

import net.sicade.observation.CatalogException;
import net.sicade.observation.coverage.sql.DescriptorTable;



/**
 * Teste le fonctionnement de {@link net.sicade.observation.coverage.sql.GridCoverageTable#evaluate}.
 * Ce test peut fonctionner avec ou sans serveur RMI disponible.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class DescriptorTest extends EvaluateTest {
    /**
     * Connexion vers la table des descripteurs.
     */
    private static DescriptorTable descriptors;

    /**
     * Construit la suite de tests.
     */
    public DescriptorTest(final String name) {
        super(name);
    }

    /**
     * Etablit la connexion avec la base de données.
     */
    @Override
    protected void setUp() throws SQLException, IOException {
        super.setUp();
        if (descriptors == null) {
            descriptors = database.getTable(DescriptorTable.class);
        }
    }

    /**
     * Construit une couverture pour le descripteur spécifié.
     */
    private void createCoverage3D(final String descriptorName)
            throws CatalogException, SQLException, IOException
    {
        coverage = new SpatioTemporalCoverage3D(descriptorName,
                       descriptors.getEntry(descriptorName).getCoverage());
    }

    /**
     * Teste quelques valeurs de hauteur de l'eau.
     */
    public void testSLA() throws Exception {
        createCoverage3D("SLA");
        assertEquals(  4.5f, evaluate( 6.75+.25/2,  77.00+.25/2, "04/07/1999"), 0.10f);
        assertEquals(-18.1f, evaluate(15.25+.25/2,  35.00+.25/2, "04/07/1999"), 0.46f);
        assertEquals(-40.0f, evaluate(17.25+.25/2, -40.75+.25/2, "04/07/1999"), 0.19f);
        assertEquals( 13.9f, evaluate(21.25+.25/2, -45.50+.25/2, "04/07/1999"), 0.56f);

        assertEquals(  3.5f, evaluate( 6.75+.25/2,  77.00+.25/2, "14/07/1999"), 0.10f);
        assertEquals(-13.5f, evaluate(15.25+.25/2,  35.00+.25/2, "14/07/1999"), 0.46f);
        assertEquals(-38.1f, evaluate(17.25+.25/2, -40.75+.25/2, "14/07/1999"), 0.19f);
        assertEquals(  8.3f, evaluate(21.25+.25/2, -45.50+.25/2, "14/07/1999"), 0.56f);

        assertEquals(  4.0f, evaluate( 6.75+.25/2,  77.00+.25/2, "09/07/1999"), 0.10f);
        assertEquals(-15.8f, evaluate(15.25+.25/2,  35.00+.25/2, "09/07/1999"), 0.46f);
        assertEquals(-39.1f, evaluate(17.25+.25/2, -40.75+.25/2, "09/07/1999"), 0.19f);
        assertEquals( 11.1f, evaluate(21.25+.25/2, -45.50+.25/2, "09/07/1999"), 0.56f);
    }

    /**
     * Retourne la suite de tests.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(DescriptorTest.class);
        return suite;
    }

    /**
     * Exécute la suite de tests à partir de la ligne de commande.
     */
    public static void main(final String[] args) {
        MonolineFormatter.init("org.geotools");
        MonolineFormatter.init("net.sicade");
        final Arguments arguments = new Arguments(args);
        Locale.setDefault(arguments.locale);
        junit.textui.TestRunner.run(suite());
    }
}
