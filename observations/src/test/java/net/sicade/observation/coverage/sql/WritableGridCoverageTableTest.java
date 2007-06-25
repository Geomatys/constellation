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
package net.sicade.observation.coverage.sql;

// J2SE dependencies
import java.awt.Dimension;
import java.io.File;
import java.util.Set;
import java.util.Locale;
import java.io.IOException;
import java.sql.SQLException;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestSuite;
import net.sicade.observation.coverage.Series;

// Geotools dependencies
import org.geotools.resources.Arguments;
import org.geotools.util.MonolineFormatter;
import org.geotools.coverage.CoverageStack;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import net.sicade.observation.coverage.AbstractTest;
import org.opengis.metadata.extent.GeographicBoundingBox;


/**
 * Teste le fonctionnement de {@link CoverageStack#evaluate} avec des {@link Series}.
 * Ce test est un peu plus direct que {@link DescriptorTest} du fait qu'il construit
 * lui même le {@link CoverageStack} dans plusieurs cas.
 * 
 * 
 * @author Cédric Briançon
 * @version $Id: WritableGridCoverageTableTest.java 20 2007-05-22 11:04:09Z cedricbr $
 */
public class WritableGridCoverageTableTest extends AbstractTest {
    /**
     * {@code true} pour désactiver tous les tests (sauf typiquement un test en particulier que l'on
     * souhaite suivre pas à pas). La valeur de ce champ devrait être toujours {@code false} sauf en
     * cas de déboguage d'une méthode bien spécifique.
     */
    private static final boolean DISABLED = false;

    /**
     * Connexion vers la table des séries.
     */
    private static SeriesTable series;

    /**
     * Construit la suite de tests.
     */
    public WritableGridCoverageTableTest(final String name) {
        super(name);
    }

    /**
     * Retourne la suite de tests.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(WritableGridCoverageTableTest.class);
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

    /**
     * Etablit la connexion avec la base de données. Cette connexion ne sera établie que la
     * première fois où un test sera exécuté. Pour la fermeture des connections, on se fiera
     * au rammase-miettes et aux "shutdown hooks" mis en place par {@code Database}.
     */
    @Override
    protected void setUp() throws SQLException, IOException {
        super.setUp();
        if (series == null) {
            series = database.getTable(SeriesTable.class);
        }
    }

    /**
     * Teste l'obtention de la liste des séries, incluant un filtrage par région géographique.
     */
    public void testWritableGCT() throws Exception {
        if (DISABLED) return;
        final SeriesTable table = database.getTable(SeriesTable.class);
        final Set<Series> all = table.getEntries();
        final File file = new File("C:\\images\\Contrôles\\Afrique.png");
        final String fileNameWithExt = file.getName();
        final String fileName = fileNameWithExt.substring(0, fileNameWithExt.indexOf("."));
        assertFalse(all.isEmpty());
        final GeographicBoundingBox bbox = new GeographicBoundingBoxImpl(-180.0, 180.0, -90.0, 90.0);
        table.setGeographicBoundingBox(bbox);
        assertEquals(bbox, table.getGeographicBoundingBox());
//        table.trimEnvelope(); // Devrait n'avoir aucun effet lorsque la sélection contient des image mondiales.
//        assertEquals(bbox, table.getGeographicBoundingBox());
        final Series selected = table.getEntry("Images de tests");
        System.out.println(selected.getSubSeries());
        WritableGridCoverageTable writableGCT = new WritableGridCoverageTable(database);
        writableGCT.setSeries(selected);
        writableGCT.addEntry(fileName, dateFormat.parse("17/06/2007"), 
                dateFormat.parse("18/06/2007"), bbox, new Dimension(1024, 768));
    }
}
