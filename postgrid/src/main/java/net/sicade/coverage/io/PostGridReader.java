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
import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;
import javax.imageio.IIOException;
import javax.imageio.ImageReader;
import javax.media.jai.PlanarImage;

// SEAGIS dependencies
import net.sicade.observation.CatalogException;
import net.sicade.observation.Observations;
import net.sicade.observation.coverage.CoverageReference;
import net.sicade.observation.coverage.Series;

// Geotools dependencies
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.processing.Operations;
import org.geotools.data.DataSourceException;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;

// OpenGIS dependencies
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;


/**
 * A reader of PostGrid DataBases, to obtain a Grid Coverage from these data. 
 * 
 * @version $Id$
 * @author Cédric Briançon
 */
public class PostGridReader extends AbstractGridCoverage2DReader implements GridCoverageReader {
    /**
     * The entry to log messages during the process.
     */
    private static final Logger LOGGER = Logger.getLogger(PostGridReader.class.toString());
    
    /**
     * The format that created this reader.
     */
    private final Format format;
    
    /**
     * The series where to fetch image from.
     */
    private String series;
    
    /**
     * The default date to work with.
     */
    private static final String SDATE = "19/08/1998 00:00:00";
    
    /**
     * The date of the image to fetch.
     */
    private final Date date = stringToDate(SDATE, "dd/MM/yyyy hh:mm:ss");
    
    /**
     * Constructs a reader with the specified series.
     *
     * @param series The string name of the series.
     * @param format The default format.
     * @param input The input file or URL on the local system directory. May be null.
     * @throws CatalogException if the series can't be found.
     */
    public PostGridReader(final String series, final Format format, final Object input) {
        if (!series.equals("")) {
            this.series = series;
        }
        if (input != null) {
            this.source = input;
            if (source instanceof File) {
                this.coverageName = ((File)source).getName();
            } else {
                if (source instanceof URL) {
                    File tmp = new File((String)source);
                    this.coverageName = tmp.getName();
                } else {
                    this.coverageName = "postgrid_coverage";
                }
            }
        
            // gets the coverage name without the extension and the dot
            final int dotIndex = coverageName.lastIndexOf('.');
            if (dotIndex != -1 && dotIndex != coverageName.length()) {
                coverageName = coverageName.substring(0, dotIndex);
            }
        }        
        this.format = format;
        try {
            this.crs = CRS.decode("EPSG:4326");
        } catch (NoSuchAuthorityCodeException ex) {
            this.crs = DefaultGeographicCRS.WGS84;
        } catch (FactoryException ex) {
            this.crs = DefaultGeographicCRS.WGS84;
        }        
        this.originalEnvelope = new GeneralEnvelope(crs);
        this.originalEnvelope.setRange(0, -180, +180);
        this.originalEnvelope.setRange(1, -90, +90);
        this.originalGridRange = new GeneralGridRange(originalEnvelope); 
    }
    
    /**
     * Constructs a reader based on the input file. The series will stay to a null value.
     *
     * @param format The default format.
     * @param input The input file or URL on the local system directory. May be null.
     * @param hints 
     * @throws DataSourceException
     */
    public PostGridReader(final Format format, Object input, Hints hints) throws DataSourceException {
        this("", format, input);
        this.hints = hints;
    }
    
    /**
     * Constructs a reader.
     *
     * @param format The default format.
     * @param input The input file or URL on the local system directory. May be null.
     * @param hints
     * @param series The string name of the series.
     * @throws DataSourceException
     */
    public PostGridReader(final Format format, Object input, Hints hints, String series) throws DataSourceException {
        this(format, input, hints);
        this.series = series;
    }    
    
    /**
     * Gets information about the Observation format.
     */
    public Format getFormat() {
        return format;
    }
    
    /**
     * Gets the series name.
     */
    public String getSeries() {
        return series;
    }
    
    /**
     * Convenience method to set a new value for the series.
     * @param series A string value which will be used.
     */
    public void setSeries(String series) {
        this.series = series;
    }
       
    /**
     * Read the coverage and generate the Grid Coverage associated.
     *
     * @param params Contains the parameters values for this coverage.
     * @return The grid coverage generated from the reading of the netcdf file.
     * @throws IllegalArgumentException
     * @throws IOException
     */
    public GridCoverage read(GeneralParameterValue[] params) throws IllegalArgumentException, IOException {
        Observations obs = Observations.getDefault();
        CoverageReference ref = null;
        try {
            Series s = obs.getSeries(series);
            ref = s.getCoverageReference(date);            
        } catch (CatalogException ex) {
            throw new IIOException(ex.getLocalizedMessage(), ex);
        } 
        
        GridCoverage2D coverage = ref.getCoverage(null); 
        return coverage.geophysics(false);        
    }
    
    /**
     * Converts a string notation of a date into a Date, using the format specified.
     * @param sDate A string representation of a date.
     * @param sFormat The format of the date.
     * @return A date using the appropriate format.
     */
    public static Date stringToDate(String sDate, String sFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(sFormat);
        try {
            return sdf.parse(sDate);
        } catch (ParseException ex) {
            LOGGER.severe("Unable to parse the Date, the actual time will be chosen.");
            return new Date();
        }
    } 
    
    /**
     * Desallocate the input stream. If in IOException is caught, this implementation will retry.
     */
    public void dispose() {
        while (inStream != null) {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {}
            }
        }
    }
}
