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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.logging.Logger;
import javax.imageio.IIOException;

// SEAGIS dependencies
import net.sicade.observation.CatalogException;
import net.sicade.observation.coverage.CoverageReference;
import net.sicade.observation.coverage.sql.GridCoverageTable;
import net.sicade.observation.sql.Database;
import org.geotools.coverage.FactoryFinder;

// Geotools dependencies
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.data.DataSourceException;
import org.geotools.factory.Hints;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;

// OpenGIS dependencies
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
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
     * The database to connect to. Will be instantiate when first needed.
     */
    private static Database database;

    /**
     * The images table. Will be created when first needed.
     */
    private static GridCoverageTable table;

    /**
     * The entry to log messages during the process.
     */
    private static final Logger LOGGER = Logger.getLogger(PostGridReader.class.toString());
    
    /**
     * The format that created this reader.
     */
    private final Format format;
    
    /**
     * The layer where to fetch image from.
     */
    private String layer;
    
    /**
     * The default date to work with.
     */
    private static final String SDATE = "19/08/1998 00:00:00";
    
    /**
     * The date of the image to fetch.
     */
    private final Date date = stringToDate(SDATE, "dd/MM/yyyy hh:mm:ss");
    
    /**
     * Constructs a reader with the specified layer.
     *
     * @param layer  The string name of the layer.
     * @param format The default format.
     * @param input  The input file or URL on the local system directory. May be null.
     * @throws CatalogException if the layer can't be found.
     */
    public PostGridReader(final String layer, final Format format, final Object input) {
        if (!layer.equals("")) {
            this.layer = layer;
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
     * Constructs a reader based on the input file. The layer will stay to a null value.
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
     * @param layer The string name of the layer.
     * @throws DataSourceException
     */
    public PostGridReader(final Format format, Object input, Hints hints, String layer) throws DataSourceException {
        this(format, input, hints);
        this.layer = layer;
    }    
    
    /**
     * Gets information about the Observation format.
     */
    public Format getFormat() {
        return format;
    }
    
    /**
     * Gets the layer name.
     */
    public String getLayer() {
        return layer;
    }
    
    /**
     * Convenience method to set a new value for the layer.
     * @param layer A string value which will be used.
     */
    public void setLayer(String layer) {
        this.layer = layer;
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
        /*Observations obs = Observations.getDefault();
        CoverageReference ref = null;
        try {
            Layer s = obs.getLayer(layer);
            ref = s.getCoverageReference(date);
        } catch (CatalogException ex) {
            throw new IIOException(ex.getLocalizedMessage(), ex);
        } 
        
        GridCoverage2D coverage = ref.getCoverage(null); 
        return coverage.geophysics(false);*/
        Date time = null;
        Number elevation = null;
        for (int i=0; i<params.length; i++) {
            final GeneralParameterValue param = params[i];
            if (param instanceof ParameterValue) {
                final ParameterValue value = (ParameterValue) param;
                final String name = value.getDescriptor().getName().getCode().trim();
                if (name.equalsIgnoreCase("TIME")) {
                    time = (Date) value.getValue();
                }
                if (name.equalsIgnoreCase("ELEVATION")) {
                    elevation = (Number) value.getValue();
                }
            }
        }
        try {
            if (time == null) {
                time = getAvailableTimes().iterator().next();
            }
            if (elevation == null) {
                elevation = getAvailableAltitudes().iterator().next();
            }
            return read("SST (Monde - Coriolis)", time, elevation);
        } catch (SQLException s) {
            throw new IIOException("Erreur connexion à la base de données.", s);
        } catch (CatalogException c) {
            throw new IIOException("Erreur connexion à la base de données.", c);
        }
    }

    /**
     * Returns a Set of all dates available for a request. In our implementation, these dates must be between 
     * the reference date (in 1970) and today.
     * @todo Get the series specified by the user.
     *
     * @return A set of dates available.
     * @throws SQLException
     * @throws IOException
     * @throws CatalogException
     */
    public static Set<Date> getAvailableTimes() throws SQLException, IOException, CatalogException {
        final GridCoverageTable table = getTable();
        table.setLayer("SST (Monde - Coriolis)"); // TODO
        table.setTimeRange(new Date(0), new Date());
        return table.getAvailableTimes();
    }
    
    /**
     * Returns a Map of all dates available for a request, with their appropriate elevations. In our implementation,
     * these dates must be between the reference date (in 1970) and today.
     * @todo Get the series specified by the user.
     *
     * @return A map of dates available, with their appropriate elevations.
     * @throws SQLException
     * @throws IOException
     * @throws CatalogException
     */
    public static SortedMap<Date, SortedSet<Number>> getAvailableCentroids() throws SQLException, IOException, CatalogException {
        final GridCoverageTable table = getTable();
        table.setLayer("SST (Monde - Coriolis)"); // TODO
        table.setTimeRange(new Date(0), new Date());
        return table.getAvailableCentroids();
    }
    
    /**
     * Returns a set of elevations that are commons to every dates present in the database. In our implementation, 
     * these dates must be between the reference date (in 1970) and today.
     * @todo Get the series specified by the user.
     *
     * @return A set of elevations common for all dates.
     * @throws SQLException
     * @throws IOException
     * @throws CatalogException
     */
    public static SortedSet<Number> getAvailableAltitudes() throws SQLException, IOException, CatalogException {
        final GridCoverageTable table = getTable();
        table.setLayer("SST (Monde - Coriolis)"); // TODO
        table.setTimeRange(new Date(0), new Date());
        return table.getAvailableAltitudes();
    }

    /**
     * Returns the image table.
     */
    private static synchronized GridCoverageTable getTable() throws SQLException, IOException {
        if (database == null) {
            database = new Database();
        }
        if (table == null) {
            table = database.getTable(GridCoverageTable.class);
        }
        return table;
    }

    /**
     * Returns an image for the given layer at the given date.
     */
    private GridCoverage read(final String layer, final Date time, final Number elevation) throws SQLException, IOException, CatalogException {
        final long TIMESPAN = 12*60*60*1000L;  // 12 hours
        final GridCoverageTable table = getTable();
        table.setLayer(layer);
        table.setTimeRange(new Date(time.getTime() - TIMESPAN), new Date(time.getTime() + TIMESPAN));
        if (elevation != null) {
            double z = elevation.doubleValue();
            table.setVerticalRange(z,z);
        } else {
            table.setVerticalRange(0,0);
        }
        final CoverageReference ref = table.getEntry();
        System.out.println(ref);
        if (ref != null) {
            
            return ref.getCoverage(null).geophysics(false);
        }
        final BufferedImage image = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.drawString("Pas d'images à la date " + date, 10, 200);
        g.dispose();
        return FactoryFinder.getGridCoverageFactory(null).create("Erreur", image, new Envelope2D(null, -180, -90, 360, 180));
    }
    
    /**
     * Converts a string notation of a date into a Date, using the format specified.
     * @param sDate A string representation of a date.
     * @param sFormat The format of the date.
     * @return A date using the appropriate format.
     */
    private static Date stringToDate(String sDate, String sFormat) {
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
