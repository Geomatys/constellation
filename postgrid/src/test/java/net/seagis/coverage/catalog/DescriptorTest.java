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
package net.seagis.coverage.catalog;

import java.util.Locale;
import java.io.IOException;
import java.sql.SQLException;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.geotools.resources.Arguments;
import org.geotools.coverage.SpatioTemporalCoverage3D;

import net.seagis.catalog.CatalogException;
import net.seagis.coverage.model.DescriptorTable;


/**
 * Teste le fonctionnement de {@link net.seagis.observation.coverage.sql.GridCoverageTable#evaluate}.
 * Ce test peut fonctionner avec ou sans serveur RMI disponible.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo Ajouter des tests sur le même modèle que ceux que l'on peut trouver dans le projet SICADE.
 */
public class DescriptorTest extends AbstractTest {
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
        final Arguments arguments = new Arguments(args);
        Locale.setDefault(arguments.locale);
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Etablit la connexion avec la base de données.
     */
    @Override
    protected void setUp() throws Exception {
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
}
