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
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.io.IOException;
import java.sql.SQLException;

// Geotools dependencies
import org.geotools.resources.CRSUtilities;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.referencing.crs.DefaultTemporalCRS;

// Sicade dependencies
import net.sicade.observation.sql.CRS;

/**
 * Evalue les prédictions d'un modèle linéaire.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo Ajouter des tests sur le même modèle que ceux que l'on peut trouver dans le projet SICADE.
 */
public class ModelTest extends AbstractTest {
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
    protected void setUp() throws SQLException, IOException {
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
}
