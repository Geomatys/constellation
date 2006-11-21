/*
 * (C) 2006, Geomatys
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
import java.util.Date;
import java.text.DateFormat;
import javax.media.jai.widget.ScrollingImagePanel;
import javax.swing.JFrame;
import net.sicade.coverage.io.*;

// SEAGIS Dependencies
import net.sicade.observation.CatalogException;

// Geotools dependencies
import org.geotools.coverage.grid.GridCoverage2D;

/**
 * Tests the {@link ObservationCoverageReader} implementation.
 *
 * @author Cédric Briançon
 *
 * @todo Make a JUnit test of this class.
 */
public class ObservationCoverageReaderTest {
        
    /**
     * Creates a new instance of ObservationCoverageReaderTest
     */
    public ObservationCoverageReaderTest() {
    }
    
    /**
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        final JFrame frame = new JFrame();
        final ObservationCoverageReader obs = new ObservationCoverageReader();
        GridCoverage2D gridCoverage2D = null;
        final Date date = new Date("01/01/1983"); 
        System.out.println(date.toString());
        try {
            //gridCoverage2D = obs.read("CHL (Monde - hebdomadaires) - historique", date);
            gridCoverage2D = obs.read("SST (Monde - mensuelles)", date);
        } catch (CatalogException ex) {
            ex.printStackTrace();
        }
        RenderedImage image = gridCoverage2D.geophysics(false).getRenderedImage();//gridCoverage2D.getRenderableImage(0,1).createDefaultRendering();
        final ScrollingImagePanel scroll = new ScrollingImagePanel(image, 1024, 768);
        frame.add(scroll);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
}
