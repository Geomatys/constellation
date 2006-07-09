/*
 * Sicade - Systèmes intégrés de connaissances
 *          pour l'aide à la décision en environnement
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
 * lui même le {@link CoverageStack} dans plusieurs cas.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class SeriesTest extends EvaluateTest {
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
    public SeriesTest(final String name) {
        super(name);
    }

    /**
     * Etablit la connexion avec la base de données.
     */
    @Override
    protected void setUp() throws SQLException, IOException {
        super.setUp();
        if (series == null) {
            series = database.getTable(SeriesTable.class);
        }
    }

    /**
     * Construit la couverture 3D pour la série spécifiée.
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
     * Teste l'obtention de la liste des séries, incluant un filtrage par région géographique.
     */
    public void testSeries() throws Exception {
        if (DISABLED) return;
        final SeriesTable table = database.getTable(SeriesTable.class);
        final Set<Series> all   = table.getEntries();
        assertFalse(all.isEmpty());
        final GeographicBoundingBox bbox = new GeographicBoundingBoxImpl(-60, 40, 15, 80);
        table.setGeographicBoundingBox(bbox);
        assertEquals(bbox, table.getGeographicBoundingBox());
        table.trimEnvelope(); // Devrait n'avoir aucun effet lorsque la sélection contient des image mondiales.
        assertEquals(bbox, table.getGeographicBoundingBox());
        final Set<Series> selected = table.getEntries();
        assertFalse(selected.isEmpty());
        assertTrue (selected.size() < all.size());
        assertTrue (all.containsAll(selected));
        assertFalse(selected.containsAll(all));

        final double EPS = 1E-7;
        Series series = table.getEntry("SST (Nouvelle-Calédonie - synthèse 5 jours - Centrée)");
        GeographicBoundingBox b = series.getGeographicBoundingBox();
        assertEquals(5,   b.getNorthBoundLatitude(), EPS);
        assertEquals(136, b.getWestBoundLongitude(), EPS);
        assertEquals(-65, b.getSouthBoundLatitude(), EPS);
        assertEquals(196, b.getEastBoundLongitude(), EPS);
        series = table.getEntry("SST (Réunion - synthèse 5 jours)");
        b = series.getGeographicBoundingBox();
        // TODO: Tester le "bounding box".
    }

    /**
     * Teste quelques valeurs de chlorophylle.
     */
    public void testCHL_historique() throws Exception {
        if (DISABLED) return;
        createCoverage3D("CHL (Monde - hebdomadaires) - historique", false);
//        assertEquals(0.0851138f, evaluate(66.6100,  -3.2100, "24/12/1997"), 0.00001f);
//        assertEquals(0.0851138f, evaluate(60.9576, -11.6657, "15/03/1998"), 0.00001f);
        assertTrue  (Float.isNaN(evaluate(52.6300,  +3.6600, "15/06/1999")));
    }

    /**
     * Teste quelques valeurs de chlorophylle.
     */
    public void testCHL() throws Exception {
        if (DISABLED) return;
        createCoverage3D("CHL (Monde - hebdomadaires)", false);
        assertEquals(0.116291724f, evaluate(66.6100,  -3.2100, "24/07/2002"), 0.00001f);
        assertEquals(0.6477361f,   evaluate(52.6300,  +3.6600, "15/06/2004"), 0.00001f);
        assertTrue  (Float.isNaN(  evaluate(60.9576, -11.6657, "15/03/2003")));
    }

    /**
     * Teste quelques valeurs de hauteur de l'eau sur une seule image.
     */
    public void testSLA1() throws Exception {
        if (DISABLED) return;
        final GridCoverageTable table;
        CoverageReference       entry;
        GridCoverage2D          grid;
        final GridRange         fullRange;
        final GridRange         clippedRange;
        double[]                output = null;
        final double            EPS = 0.0001;

        table = database.getTable(GridCoverageTable.class);
        table.setSeries(series.getEntry("SLA (Monde - TP/ERS)"));
        table.setTimeRange(dateFormat.parse("01/03/1996"), dateFormat.parse("02/03/1996"));
        entry     = table.getEntry();
        grid      = entry.getCoverage(null);
        fullRange = grid.getGridGeometry().getGridRange();
        assertEquals(  4.1, (output=grid.evaluate(new Point2D.Double(12.00+.25/2, -61.50+.25/2), output))[0], EPS);
        assertEquals( 11.7, (output=grid.evaluate(new Point2D.Double(17.00+.25/2, -52.25+.25/2), output))[0], EPS);
        assertEquals( 15.0, (output=grid.evaluate(new Point2D.Double(20.00+.25/2, -41.25+.25/2), output))[0], EPS);
        assertEquals( -0.1, (output=grid.evaluate(new Point2D.Double(22.50+.25/2,  75.25+.25/2), output))[0], EPS);
        /*
         * Essaie de réduire la région d'intérêt, et vérifie que l'on obtient les mêmes valeurs.
         */
        final GeographicBoundingBox subarea = new GeographicBoundingBoxImpl(15, 25, -65, 80);
        table.setGeographicBoundingBox(subarea);
        assertEquals(subarea, table.getGeographicBoundingBox());
        entry        = table.getEntry();
        grid         = entry.getCoverage(null);
        clippedRange = grid.getGridGeometry().getGridRange();
        assertTrue(clippedRange.getLength(0) < fullRange.getLength(0));
        assertTrue(clippedRange.getLength(1) < fullRange.getLength(1));
        assertEquals(  4.1, (output=grid.evaluate(new Point2D.Double(12.00+.25/2, -61.50+.25/2), output))[0], EPS);
        assertEquals( 11.7, (output=grid.evaluate(new Point2D.Double(17.00+.25/2, -52.25+.25/2), output))[0], EPS);
        assertEquals( 15.0, (output=grid.evaluate(new Point2D.Double(20.00+.25/2, -41.25+.25/2), output))[0], EPS);
        assertEquals( -0.1, (output=grid.evaluate(new Point2D.Double(22.50+.25/2,  75.25+.25/2), output))[0], EPS);
    }

    /**
     * Teste quelques valeurs de hauteur de l'eau.
     */
    public void testSLA() throws Exception {
        if (DISABLED) return;
        createCoverage3D("SLA (Monde - TP/ERS)", false);
        assertEquals( 20.4f, evaluate(60.9576, -11.6657, "15/03/1998"), 0.0001f);
        assertEquals(-10.9f, evaluate(61.7800,  -3.5100, "06/01/1997"), 0.0001f);
        assertEquals( 20.5f, evaluate(49.6000,  -5.8600, "03/03/1993"), 0.0001f);

        // Valeurs puisées dans les fichiers textes.
        assertEquals(  4.1f, evaluate(12.00+.25/2, -61.50+.25/2, "01/03/1996"), 0.0001f);
        assertEquals( 11.7f, evaluate(17.00+.25/2, -52.25+.25/2, "01/03/1996"), 0.0001f);
        assertEquals( 15.0f, evaluate(20.00+.25/2, -41.25+.25/2, "01/03/1996"), 0.0001f);
        assertEquals( -0.1f, evaluate(22.50+.25/2,  75.25+.25/2, "01/03/1996"), 0.0001f);

        // Valeurs puisées dans les fichiers textes aux même positions deux jours consécutifs.
        assertEquals(  4.5f, evaluate( 6.75+.25/2,  77.00+.25/2, "04/07/1999"), 0.0001f);
        assertEquals(-18.1f, evaluate(15.25+.25/2,  35.00+.25/2, "04/07/1999"), 0.0001f);
        assertEquals(-40.0f, evaluate(17.25+.25/2, -40.75+.25/2, "04/07/1999"), 0.0001f);
        assertEquals( 13.9f, evaluate(21.25+.25/2, -45.50+.25/2, "04/07/1999"), 0.0001f);

        assertEquals(  3.5f, evaluate( 6.75+.25/2,  77.00+.25/2, "14/07/1999"), 0.0001f);
        assertEquals(-13.5f, evaluate(15.25+.25/2,  35.00+.25/2, "14/07/1999"), 0.0001f);
        assertEquals(-38.1f, evaluate(17.25+.25/2, -40.75+.25/2, "14/07/1999"), 0.0001f);
        assertEquals(  8.3f, evaluate(21.25+.25/2, -45.50+.25/2, "14/07/1999"), 0.0001f);

        createCoverage3D("SLA (Monde - TP/ERS)", true);
        // Utilise une tolérance égale à la pente de la droite reliant les deux points
        // dans le temps: (SLA2 - SLA1) / 10 jours.  Autrement dit, accepte une erreur
        // de 24 heures dans la date.
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
     * Teste une valeur de pente.
     */
//    public void testPente() throws Exception {
//        if (DISABLED) return;
//        final GridCoverage2D coverage = series.getEntry("Pente").getCoverageReferences()
//                                              .iterator().next().getCoverage(null);
//        coverage.show();
//        Thread.currentThread().sleep(50000);
//        assertEquals(11.1f, evaluate(166.0, -22.0, "01/01/2006"), 0.01f);
//        createCoverage3D("Pente", true);
//        assertEquals(11.1f, evaluate(166.0, -22.0, "01/01/2006"), 0.01f);
//    }

    /**
     * Teste l'obtention d'une image d'un nom spécifique.
     */
    public void testNamed() throws Exception {
        if (DISABLED) return;
        final GridCoverageTable table = database.getTable(GridCoverageTable.class);
        table.setSeries(series.getEntry("Potentiel de pêche (Calédonie) ALB-opérationnel"));
        CoverageReference reference = table.getEntry("PP20060611");
        assertTrue(reference.getName().endsWith("PP20060611"));
    }

    /**
     * Retourne la suite de tests.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(SeriesTest.class);
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
