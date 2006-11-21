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
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Logger;

// Geotools dependencies
import org.geotools.coverage.grid.GridCoverage2D;

// Seagis dependencies
import net.sicade.observation.CatalogException;
import net.sicade.observation.coverage.CoverageReference;
import net.sicade.observation.coverage.Series;
import net.sicade.observation.Observations;


/**
 * A coverage reader for grid coverages declared in the observation database.
 *
 * @version $Id$
 * @author Cédric Briançon
 *
 * @todo Implements the coverage reader interface.
 */
public class ObservationCoverageReader {    
    /**
     * The entry to log messages during the process.
     */
    private static final Logger LOGGER = Logger.getLogger(ObservationCoverageReader.class.toString());
    
    //private CoordinateReferenceSystem crs;
    
    /**
     *
     */
    public ObservationCoverageReader() {
//        try {
//            this.crs = CRS.decode("EPSG:4326"); 
//        } catch (NoSuchAuthorityCodeException ex) {
//            ex.printStackTrace();
//        } catch (FactoryException ex) {
//            ex.printStackTrace();
//        }
    }
    
    /**
     * Returns the number of months between the date specified by user and the date for the first raster image.
     */
    private int getIndexInSeries(Series series, Date date) throws CatalogException {
        double daysInterv = series.getTimeInterval();
        // Each image represents one month
        if (daysInterv == 31.00) {
            Date dateMin = series.getTimeRange().getMinValue();
            Date dateMax = series.getTimeRange().getMaxValue();
            if (date.after(dateMax)) {
                return -1;
            } else {
                int nbYear = date.getYear()-dateMin.getYear();
                System.out.println(date.getYear() + " "+ dateMin.getYear());
                System.out.println("Interv year " + nbYear);
                int nbMonth = date.getMonth()-dateMin.getMonth();
                System.out.println("Interv month " + nbMonth);
                return nbYear*12 + nbMonth;
            }
        } else {
            // not implemented
            return -1;
        }
    }
    
    public GridCoverage2D read(String subSeries, Date date) throws IllegalArgumentException, IOException, CatalogException {
        Observations obs = Observations.getDefault();
        Series series = obs.getSeries(subSeries);
        Iterator<CoverageReference> iter = series.getCoverageReferences().iterator();
//        while (iter.hasNext()) {
//            System.out.println(iter.next());
//        }
        int index = getIndexInSeries(series, date);
        System.out.println(index);
        for (int i=0; i<index; i++) {
            System.out.println(iter.next());
        }
        GridCoverage2D coverage2D = iter.next().getCoverage(null);        
        if (iter.hasNext()) {
            System.out.println(iter.next().getCoordinateReferenceSystem());
            //coverage2D = iter.next().getCoverage(null);
        }
//        RenderableImage renderable = coverage.getRenderableImage(0, 1);              
//        return renderable.createDefaultRendering();
        return coverage2D;
    }  
}
