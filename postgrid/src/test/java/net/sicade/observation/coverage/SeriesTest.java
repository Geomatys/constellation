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
import net.sicade.observation.coverage.sql.LayerTable;
import net.sicade.observation.coverage.sql.OperationTable;
import net.sicade.observation.coverage.sql.GridCoverageTable;


/**
 * Teste le fonctionnement de {@link CoverageStack#evaluate} avec des {@link Layer}.
 * Ce test est un peu plus direct que {@link DescriptorTest} du fait qu'il construit
 * lui même le {@link CoverageStack} dans plusieurs cas.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo Ajouter des tests sur le même modèle que ceux que l'on peut trouver dans le projet SICADE.
 */
public class SeriesTest extends AbstractTest {
    /**
     * {@code true} pour désactiver tous les tests (sauf typiquement un test en particulier que l'on
     * souhaite suivre pas à pas). La valeur de ce champ devrait être toujours {@code false} sauf en
     * cas de déboguage d'une méthode bien spécifique.
     */
    private static final boolean DISABLED = false;

    /**
     * Connexion vers la table des couches.
     */
    private static LayerTable layers;

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
        if (layers == null) {
            layers = database.getTable(LayerTable.class);
        }
    }

    /**
     * Construit la couverture 3D pour la couche spécifiée. La résultat sera placé
     * dans le champ {@link #coverage}.
     *
     * @param seriesName  Nom de la couche pour laquelle on veut une couverture 3D.
     * @param interpolate {@code true} si les interpolations sont autorisées.
     */
    private void createCoverage3D(final String seriesName, final boolean interpolate)
            throws CatalogException, SQLException, IOException
    {
        final Layer layer = layers.getEntry(seriesName);
        final Coverage c;
        if (interpolate) {
            c = layer.getCoverage();
        } else {
            final GridCoverageTable table = database.getTable(GridCoverageTable.class);
            table.setLayer(layer);
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
     * Teste l'obtention de la liste des couches, incluant un filtrage par région géographique.
     */
    public void testSeries() throws Exception {
        if (DISABLED) return;
        final LayerTable table = database.getTable(LayerTable.class);
        final Set<Layer> all = table.getEntries();
        assertFalse(all.isEmpty());
        final GeographicBoundingBox bbox = new GeographicBoundingBoxImpl(-60, 40, 15, 80);
        table.setGeographicBoundingBox(bbox);
        assertEquals(bbox, table.getGeographicBoundingBox());
        table.trimEnvelope(); // Devrait n'avoir aucun effet lorsque la sélection contient des image mondiales.
        assertEquals(bbox, table.getGeographicBoundingBox());
        final Set<Layer> selected = table.getEntries();
        assertFalse(selected.isEmpty());
        /* TODO: notre base a été épurée de certaines données, pour les tests on modifie la condition qui était
         * un inférieur strict par un inférieur ou égal (dans notre cas selected.size() et all.size() sont égaux.
         */ 
        assertTrue (selected.size() <= all.size());
        assertTrue (all.containsAll(selected));
        /* TODO: Après épuration des données, selected et all sont identiques pour la zone choisie. Le test
         * suivi n'a donc plus de raisons d'être.
         */
        //assertFalse(selected.containsAll(all) && !selected.equals(all));
    }
}
