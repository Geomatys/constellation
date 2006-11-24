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

package net.sicade.coverage.io;

// J2SE dependencies
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import javax.media.jai.widget.ScrollingImagePanel;
import javax.swing.JFrame;

// SEAGIS Dependencies
import net.sicade.observation.CatalogException;

// Geotools dependencies
import org.geotools.coverage.grid.GridCoverage2D;

/**
 *
 * @author Cédric Briançon
 */
public class ObservationCoverageReaderTest {
    
    /**
     * The entry to log messages during the process.
     */
    private static final Logger LOGGER = Logger.getLogger(ObservationCoverageReaderTest.class.toString());    
        
    /**
     * Creates a new instance of ObservationCoverageReaderTest
     */
    public ObservationCoverageReaderTest() {
    }
    
    /**
     * Converts a string notation of a date into a Date, using the format specified.
     * @param sDate A string representation of a date.
     * @param sFormat The format of the date.
     * @return A date using the appropriate format.
     */
    public static Date stringToDate(String sDate, String sFormat) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(sFormat);
        return sdf.parse(sDate);
    } 
    
    /**
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, CatalogException {
        final JFrame frame = new JFrame();
        Date date = null;
        try {
            date = stringToDate("19/08/1998 00:00:00", "dd/MM/yyyy hh:mm:ss");
        } catch (ParseException ex) {
            LOGGER.warning("Not a valid date !");
            date = new Date();
        }
        final ObservationCoverageReader obs = new ObservationCoverageReader(
                "SST (Monde - mensuelles)", date);
//        final ObservationCoverageReader obs = new ObservationCoverageReader(
//                "CHL (Monde - hebdomadaires) - historique", date);
        GridCoverage2D gridCoverage2D = (GridCoverage2D) obs.read(null);        
        RenderedImage image = gridCoverage2D.geophysics(false).getRenderedImage();
        final ScrollingImagePanel scroll = new ScrollingImagePanel(image, 1024, 768);
        frame.add(scroll);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
}
