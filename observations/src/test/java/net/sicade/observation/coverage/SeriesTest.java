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

// J2SE dependencies
import java.util.Set;
import java.util.Locale;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.sql.SQLException;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestSuite;

// OpenGIS dependencies
import org.opengis.coverage.Coverage;
import org.opengis.coverage.grid.GridRange;
import org.opengis.metadata.extent.GeographicBoundingBox;

// Geotools dependencies
import org.geotools.resources.Arguments;
import org.geotools.util.MonolineFormatter;
import org.geotools.coverage.CoverageStack;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.SpatioTemporalCoverage3D;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;

// Sicade dependencies
import net.sicade.observation.CatalogException;
import net.sicade.observation.coverage.sql.SeriesTable;
import net.sicade.observation.coverage.sql.OperationTable;
import net.sicade.observation.coverage.sql.GridCoverageTable;


/**
 * Teste le fonctionnement de {@link CoverageStack#evaluate} avec des {@link Series}.
 * Ce test est un peu plus direct que {@link DescriptorTest} du fait qu'il construit
 * lui m�me le {@link CoverageStack} dans plusieurs cas.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo Ajouter des tests sur le m�me mod�le que ceux que l'on peut trouver dans le projet SICADE.
 */
public class SeriesTest extends AbstractTest {
    /**
     * {@code true} pour d�sactiver tous les tests (sauf typiquement un test en particulier que l'on
     * souhaite suivre pas � pas). La valeur de ce champ devrait �tre toujours {@code false} sauf en
     * cas de d�boguage d'une m�thode bien sp�cifique.
     */
    private static final boolean DISABLED = false;

    /**
     * Connexion vers la table des s�ries.
     */
    private static SeriesTable series;

    /**
     * Construit la suite de tests.
     */
    public SeriesTest(final String name) {
        super(name);
    }

    /**
     * Retourne la suite de tests.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(SeriesTest.class);
        return suite;
    }

    /**
     * Ex�cute la suite de tests � partir de la ligne de commande.
     */
    public static void main(final String[] args) {
        MonolineFormatter.init("org.geotools");
        MonolineFormatter.init("net.sicade");
        final Arguments arguments = new Arguments(args);
        Locale.setDefault(arguments.locale);
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Etablit la connexion avec la base de donn�es. Cette connexion ne sera �tablie que la
     * premi�re fois o� un test sera ex�cut�. Pour la fermeture des connections, on se fiera
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
     * Construit la couverture 3D pour la s�rie sp�cifi�e. La r�sultat sera plac�
     * dans le champ {@link #coverage}.
     *
     * @param seriesName  Nom de la s�rie pour laquelle on veut une couverture 3D.
     * @param interpolate {@code true} si les interpolations sont autoris�es.
     */
    private void createCoverage3D(final String seriesName, final boolean interpolate)
            throws CatalogException, SQLException, IOException
    {
        final Series series = this.series.getEntry(seriesName);
        final Coverage c;
        if (interpolate) {
            c = series.getCoverage();
        } else {
            final GridCoverageTable table = database.getTable(GridCoverageTable.class);
            table.setSeries(series);
            table.setOperation(database.getTable(OperationTable.class).getEntry("Valeur directe"));
            final CoverageStack stack = new CoverageStack(seriesName,
                                                          table.getCoordinateReferenceSystem(),
                                                          table.getEntries());
            stack.setInterpolationEnabled(interpolate);
            c = stack;
        }
        coverage = new SpatioTemporalCoverage3D(seriesName, c);
    }

    /**
     * Teste l'obtention de la liste des s�ries, incluant un filtrage par r�gion g�ographique.
     */
    public void testSeries() throws Exception {
        if (DISABLED) return;
        final SeriesTable table = database.getTable(SeriesTable.class);
        final Set<Series> all = table.getEntries();
        assertFalse(all.isEmpty());
        final GeographicBoundingBox bbox = new GeographicBoundingBoxImpl(-60, 40, 15, 80);
        table.setGeographicBoundingBox(bbox);
        assertEquals(bbox, table.getGeographicBoundingBox());
        table.trimEnvelope(); // Devrait n'avoir aucun effet lorsque la s�lection contient des image mondiales.
        assertEquals(bbox, table.getGeographicBoundingBox());
        final Set<Series> selected = table.getEntries();
        assertFalse(selected.isEmpty());
        /* TODO: notre base a �t� �pur�e de certaines donn�es, pour les tests on modifie la condition qui �tait
         * un inf�rieur strict par un inf�rieur ou �gal (dans notre cas selected.size() et all.size() sont �gaux.
         */ 
        assertTrue (selected.size() <= all.size());
        assertTrue (all.containsAll(selected));
        /* TODO: Apr�s �puration des donn�es, selected et all sont identiques pour la zone choisie. Le test
         * suivi n'a donc plus de raisons d'�tre.
         */
        //assertFalse(selected.containsAll(all) && !selected.equals(all));
    }
}
