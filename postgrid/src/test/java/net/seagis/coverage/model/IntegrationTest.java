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
package net.seagis.coverage.model;

import net.seagis.catalog.*;
import net.seagis.coverage.catalog.*;
import java.util.Set;
import java.util.Date;
import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.sql.SQLException;

import org.opengis.coverage.Coverage;
import org.opengis.metadata.extent.GeographicBoundingBox;

import org.geotools.coverage.CoverageStack;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.referencing.crs.DefaultTemporalCRS;
import org.geotools.coverage.SpatioTemporalCoverage3D;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;

import net.seagis.catalog.DatabaseTest;
import net.seagis.catalog.CatalogException;


/**
 * Tests {@link GridCoverage2D#evaluate} (directly or directly) on coverage obtained from the
 * PostGrid database.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo This test class is merely a skeleton in current state. Needs to be expanded.
 */
public class IntegrationTest extends DatabaseTest {
    /**
     * Connexion vers la table des couches.
     */
    private LayerTable layers;

    /**
     * Connexion vers la table des descripteurs.
     */
    private DescriptorTable descriptors;

    /**
     * La couverture à tester. Sera construites par les différentes méthodes {@code testXXX}.
     */
    private SpatioTemporalCoverage3D coverage;

    /**
     * Objet à utiliser pour lire et écrire des dates.
     * Le format attendu est de la forme "24/12/1997".
     */
    private DateFormat dateFormat;

    /**
     * Établit la connexion avec la base de données.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        layers      = database.getTable(LayerTable.class);
        descriptors = database.getTable(DescriptorTable.class);
        dateFormat  = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CANADA);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Retourne un objet {@code DirectPosition} pour les coordonnées spatio-temporelles spécifiées.
     */
    private GeneralDirectPosition createPosition(final double longitude,
                                                 final double latitude,
                                                 final String date)
            throws ParseException
    {
        final Date d = dateFormat.parse(date);
        final GeneralDirectPosition position = new GeneralDirectPosition(database.getCoordinateReferenceSystem());
        final DefaultTemporalCRS crs = DefaultTemporalCRS.wrap(
                org.geotools.referencing.CRS.getTemporalCRS(position.getCoordinateReferenceSystem()));
        position.setOrdinate(0, longitude);
        position.setOrdinate(1, latitude);
        position.setOrdinate(2, crs.toValue(d));
        return position;
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
                    table.getCoordinateReferenceSystem(), table.getEntries());
            stack.setInterpolationEnabled(interpolate);
            c = stack;
        }
        coverage = new SpatioTemporalCoverage3D(seriesName, c);
    }

    /**
     * Retourne la valeur de la première bande évaluée à la position spécifiée.
     * Cette méthode est un bon endroit où placer un point d'arrêt à des fins de déboguage.
     */
    private float evaluate(final double x, final double y, final String date) throws ParseException {
        final Point2D coord = new Point2D.Double(x,y);
        final Date    time  = dateFormat.parse(date);
        float[] array=null, compare=null;
        array = coverage.evaluate(coord, time, array); //       <--- Break point ici
        compare = coverage.getGridCoverage2D(time).evaluate(coord, compare);
        assertTrue(Arrays.equals(array, compare));
        return array[0];
    }

    /**
     * Teste l'obtention de la liste des couches, incluant un filtrage par région géographique.
     */
    public void testLayers() throws Exception {
        final LayerTable table = new LayerTable(database.getTable(LayerTable.class));
        final Set<Layer> all = table.getEntries();
        assertFalse(all.isEmpty());
        final GeographicBoundingBox bbox = new GeographicBoundingBoxImpl(-60, 40, 15, 80);
        table.setGeographicBoundingBox(bbox);
        assertEquals(bbox, table.getGeographicBoundingBox());
        table.trimEnvelope(); // Should have no effect when the selection contains world rasters.
        assertEquals(bbox, table.getGeographicBoundingBox());
        final Set<Layer> selected = table.getEntries();
        assertFalse(selected.isEmpty());
        assertTrue(selected.size() <= all.size());
        assertTrue(all.containsAll(selected));
    }
}
