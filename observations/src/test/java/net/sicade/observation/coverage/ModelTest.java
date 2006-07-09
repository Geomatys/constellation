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

// JUnit dependencies
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import junit.framework.TestCase;

// OpenGIS dependencies
import org.opengis.coverage.Coverage;

// Geotools dependencies
import org.geotools.resources.CRSUtilities;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.referencing.crs.DefaultTemporalCRS;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;

// Sicade dependencies
import net.sicade.observation.sql.CRS;
import net.sicade.observation.Observations;
import net.sicade.observation.CatalogException;


/**
 * Evalue les prédictions d'un modèle linéaire.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ModelTest extends TestCase {
    /**
     * Définir à {@code true} pour afficher des informations de déboguage.
     */
    private static final boolean verbose = true;

    /**
     * L'objet à utiliser pour lire des dates.
     */
    private DateFormat dateFormat;

    /**
     * Construit la suite de tests.
     */
    public ModelTest(final String name) {
        super(name);
    }

    /**
     * Initialise cette suite de test.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CANADA);
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
        final GeneralDirectPosition position = new GeneralDirectPosition(CRS.XYT.getCoordinateReferenceSystem());
        final DefaultTemporalCRS crs = DefaultTemporalCRS.wrap(CRSUtilities.getTemporalCRS(position.getCoordinateReferenceSystem()));
        position.setOrdinate(0, longitude);
        position.setOrdinate(1, latitude);
        position.setOrdinate(2, crs.toValue(d));
        return position;
    }

    /**
     * Testes des valeurs prédites par le modèle.
     */
    public void testModel() throws ParseException, CatalogException {
        final Observations observations = Observations.getDefault();
        final Series series = observations.getSeries(
                new GeographicBoundingBoxImpl(155, 165, -20, -15), null,
                "Potentiel de pêche ALB-optimal (Calédonie)");
        final LinearModel model = (LinearModel) series.getModel();
        if (verbose) {
            System.out.println(model);
            for (final LinearModel.Term t : model.getTerms()) {
                for (final Descriptor d : t.getDescriptors()) {
                    System.out.print(d.getNumericIdentifier());
                    System.out.print(' ');
                }
                System.out.println(t);
            }
        }
        double[] buffer = null;
        final Coverage coverage = model.asCoverage();
        final GeneralDirectPosition position = createPosition(160.1666666666666666666666,
                                                              -19.8833333333333333333333,
                                                              "2000-11-06 12:00");
        buffer = coverage.evaluate(position, buffer);
        final double value = buffer[0];
    }
}
