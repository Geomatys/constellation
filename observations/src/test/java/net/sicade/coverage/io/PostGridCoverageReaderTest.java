/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2006, Institut de Recherche pour le Développement
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
import java.io.IOException;
import java.util.logging.Logger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Geotools dependencies
import org.geotools.coverage.grid.GridCoverage2D;

/**
 * Test class for PostGrid plugin.
 *
 * @author Cédric Briançon
 */
public class PostGridCoverageReaderTest extends TestCase {
    
    /**
     * The entry to log messages during the process.
     */
    private static final Logger LOGGER = Logger.getLogger(
            PostGridCoverageReaderTest.class.toString());    
        
    /**
     * Creates a new instance of PostGridCoverageReaderTest
     */
    public PostGridCoverageReaderTest() {
        super("PostGrid Coverage Reader Test");
    }
    
    /**
     * Launch all the tests.
     */
    public static Test suite() {
        return new TestSuite(PostGridCoverageReaderTest.class);
    }        
    
    /**
     * Test whether a {@code GridCoverage} has been found for the series in 
     * the PostGrid DataBase.
     */
    public void testGridCoverage() {
        final PostGridReader obs = new PostGridReader("SST (Monde - mensuelles)",
                null, null);
        GridCoverage2D gridCoverage2D = null;
        try {
            gridCoverage2D = (GridCoverage2D) obs.read(null);
        } catch (IllegalArgumentException ex) {
            LOGGER.severe("The serie specified does not exist in the postgrid " +
                    "database. "+ ex.getMessage());
        } catch (IOException ex) {
            LOGGER.severe("Enable to get the coverage. "+ ex.getMessage());
        }        
        assertNotNull(gridCoverage2D);        
    }
    
    /**
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
}
