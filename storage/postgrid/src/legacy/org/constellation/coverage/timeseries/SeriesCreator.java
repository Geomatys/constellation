/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.coverage.timeseries;

// J2SE dependencies
import java.io.Writer;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

// OpenGIS dependencies
import org.opengis.coverage.Coverage;
import org.opengis.referencing.operation.TransformException;

// Geotools dependencies
import org.geotools.geometry.GeneralEnvelope;
//import org.geotools.gui.headless.ProgressPrinter;

// Sicade dependencies
import org.constellation.coverage.catalog.Catalog;
import org.constellation.catalog.CatalogException;
import org.geotools.resources.image.ImageUtilities;


/**
 * Construit des séries temporelles.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public class SeriesCreator {
    /**
     * Construit une instance de {@code SeriesCreator}.
     */
    public SeriesCreator() {
    }

    /**
     */
    public static void main(final String[] args)
            throws CatalogException, TransformException, IOException 
    {
        ImageUtilities.allowNativeCodec("png", true, false);
        final Coverage coverage = Catalog.getDefault().getDescriptorCoverage("SST");
        final GeneralEnvelope e = new GeneralEnvelope(coverage.getEnvelope());
        e.setRange(0,   136,   196);  // Plage de longitudes
        e.setRange(0,   136,   180);  // (coupe à la ligne +/-180)
        e.setRange(1,   -44,     3);  // Plage de latitudes
//        e.setRange(2, 12784, 19723);  // Plage de temps: 1er janvier 1985 au 1er janvier 2004.
//        e.setRange(2, 18262, 19358);  // Plage de temps: 1er janvier 2000 au 1er janvier 2003.
        e.setRange(2, 18262, 18293);  // Plage de temps: 1er janvier 2000 au 1er février 2000.
//        final TimeSeriesTile ts = new TimeSeriesTile(coverage, e, new double[] {5, 5, 1});
        final TimeSeriesTile ts = new TimeSeriesTile(coverage, e, new double[] {0.1, 0.1, 1}, 2);
        final Writer out = new BufferedWriter(new FileWriter("C:\\Documents and Settings\\Antoine\\Bureau\\TestTimeSeriesTile\\series.txt"));
//        final ProgressPrinter progress = new ProgressPrinter();
        try {
            ts.writeCoordinates(out, null);
//            ts.writeValues(out, null, progress);
            ts.writeValues(out, null, null);
            ts.writeImages(5, 0, 1, null);
        } finally {
            out.close();
            ts.dispose();
        }
    }
}
