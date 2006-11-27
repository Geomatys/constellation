/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
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
 */
package net.sicade.coverage.io;

// J2SE dependencies
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.logging.Logger;

// Seagis dependencies
import net.sicade.observation.CatalogException;
import net.sicade.observation.coverage.CoverageReference;
import net.sicade.observation.coverage.Series;
import net.sicade.observation.Observations;

// Geotools dependencies
import org.geotools.coverage.FactoryFinder;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.data.coverage.grid.AbstractGridCoverage2DReader;
import org.geotools.factory.Hints;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.resources.CRSUtilities;

// OpenGIS dependencies
import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;


/**
 * A reader for grid coverages from the observation database.
 *
 * @version $Id$
 * @author Cédric Briançon
 */
public class ObservationCoverageReader extends AbstractGridCoverage2DReader implements GridCoverageReader {
    /**
     * The entry to log messages during the process.
     */
    private static final Logger LOGGER = Logger.getLogger("net.sicade.coverage.io");    
    
    /**
     * The format that created this reader.
     */
    private final Format format;
    
    /**
     * The series where to fetch image from.
     */
    private final Series series;
    
    /**
     * The date of the image to fetch.
     */
    private final Date date;
    
    /**
     * Constructor to test the reading method with a direct Main class.
     *
     * @param series The string name of the series.
     * @param date The date to choose the appropriate image.
     * @throws CatalogException if the series can't be found.
     */
    public ObservationCoverageReader(final String series, final Date date) throws CatalogException {
        final Observations obs = Observations.getDefault();
        this.series = obs.getSeries(series);
        this.date   = date;
        this.format = null;
    }
    
    /**
     * Constructs a reader for the specified format.
     */
    public ObservationCoverageReader(final Format format, final Object input, final Hints hints) {
        this.hints  = hints;
        this.format = format;
        this.crs    = DefaultGeographicCRS.WGS84;
        Date date = null;
        try {
            date = stringToDate("19/08/1998 00:00:00", "dd/MM/yyyy hh:mm:ss");
        } catch (ParseException ex) {
            LOGGER.warning("Not a valid date !");
            date = new Date();
        }
        this.date = date;
        Observations obs = Observations.getDefault();
        Series series = null;
        try {
            series = obs.getSeries("SST (Monde - mensuelles)");
        } catch (CatalogException ex) {
            ex.printStackTrace();
        }
        this.series = series;
    }

    /**
     * Keep only the two-dimensonal part of the specified grid coverage.
     */
    private GridCoverage2D trimExtraDimensions(final GridCoverage2D gc) throws TransformException {
        // Set the CRS for the new GridCoverage2D generated.
        crs = CRSUtilities.getCRS2D(gc.getCoordinateReferenceSystem());
        GridSampleDimension[] bands = gc.getSampleDimensions();
        MathTransform gridToCRS = ((GridGeometry2D) gc.getGridGeometry()).getGridToCRS2D();
        GridCoverageFactory gcFact = FactoryFinder.getGridCoverageFactory(hints);
        return gcFact.create(gc.getName(), gc.geophysics(false).getRenderedImage(), crs, gridToCRS, bands, 
			null, gc.getProperties());
    }
    
    /**
     * Converts a string notation of a date into a {@link Date}, using the format specified.
     *
     * @param sDate A string representation of a date.
     * @param sFormat The format of the date.
     * @return A date using the appropriate format.
     */
    private static Date stringToDate(String sDate, String sFormat) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(sFormat, Locale.FRANCE);
        return sdf.parse(sDate);
    }
    
    /**
     * Returns the number of months between the date specified by user and the date for the first raster image.
     */
    public int getIndexInSeries() throws CatalogException {
        final double daysInterv = series.getTimeInterval();
        final Date dateMin = series.getTimeRange().getMinValue();
        final Date dateMax = series.getTimeRange().getMaxValue();
        // Each image represents one month
        System.out.println(daysInterv);
        if (date.after(dateMax) || date.before(dateMin)) {
            LOGGER.warning("The date specified '"+ date.toString()+ "' is out of the range of this serie !");
            LOGGER.warning("[" + series.getTimeRange().getMinValue().toString() + ", " + 
                  series.getTimeRange().getMaxValue().toString() + "]");
            return -1;
        } else {
            int nbYears = date.getYear() - dateMin.getYear();            
            int nbMonths = date.getMonth() - dateMin.getMonth();
            int nbDays = date.getDay() - dateMin.getDay();
            System.out.println("years "+ nbYears);
            System.out.println("months "+ nbMonths);
            System.out.println("days "+ nbDays);
            if (daysInterv == 31.00 || daysInterv == 30.00) {         
                return nbYears*12 + nbMonths;
            } else {
                if (daysInterv == 8.00) {
                    long res = Math.round((nbYears*365 + nbMonths*30 + nbDays) / daysInterv); 
                    System.out.println(res);
                    return Integer.valueOf(Long.toString(res));
                } else {
                    // not implemented
                    return -1;
                }
            }
        } 
    }
    
    /**
     * Gets information about the Observations format.
     */
    public Format getFormat() {
        return format;
    }
    
    /**
     * Get the names of metadata. Not implemented in this project.
     */
    public String[] getMetadataNames() throws IOException {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    /**
     * Not implemented.
     */
    public String getMetadataValue(String string) throws IOException, MetadataNameNotFoundException {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    /**
     * Not implemented.
     */
    public String[] listSubNames() throws IOException {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    /**
     * Not implemented.
     */
    public String getCurrentSubname() throws IOException {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    /**
     * Not implemented.
     */
    public boolean hasMoreGridCoverages() throws IOException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     * Read the coverage and generate the Grid Coverage associated.
     *
     * @param params Contains the parameters values for this coverage.
     * @return The grid coverage generated from the reading of the raster.
     * @throws IllegalArgumentException
     * @throws IOException
     */
    public GridCoverage read(GeneralParameterValue[] generalParameterValue) throws IllegalArgumentException, IOException {
        Iterator<CoverageReference> iter = null;
        int index = -1;
        try {
            iter = series.getCoverageReferences().iterator();
            index = getIndexInSeries();
        } catch (CatalogException ex) {
            ex.printStackTrace();
        }
        //System.out.println(index);      
        if (index < 0) {
            return null;
        } else {
            for (int i=0; i<index; i++) {
                System.out.println(iter.next());
            }
            GridCoverage2D coverage2D = iter.next().getCoverage(null);        
            if (iter.hasNext()) {
                System.out.println(iter.next().getCoordinateReferenceSystem());
            }
            try {
                return trimExtraDimensions(coverage2D);
            } catch (TransformException ex) {
                return coverage2D;
            }
            //return coverage2D;
        }
    }
    
    /**
     * Not implemented.
     */
    public void skip() throws IOException {
        throw new UnsupportedOperationException("Only one image supported.");
    }
    
    /**
     * Desallocate the input stream. If in IOException is caught, this implementation will retry.
     */
    public void dispose() throws IOException {
        if (inStream != null) {
            inStream.close();
            inStream = null;
        }
    }
    
}
