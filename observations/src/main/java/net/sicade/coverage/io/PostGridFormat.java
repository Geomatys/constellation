/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2007, Geomatys
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
import java.util.HashMap;

// Geotools dependencies
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.data.DataSourceException;
import org.geotools.factory.Hints;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.parameter.ParameterGroup;

// OpenGIS dependencies
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterDescriptor;


/**
 * Description of PostGrid DataBase format.
 *
 * @version $Id$
 * @author Cédric Briançon
 */
public class PostGridFormat extends AbstractGridFormat implements Format {    
    /**
     * Creates a new instance of PostGridFormat.
     * Contains the main information about the PostGrid DataBase format.
     */
    public PostGridFormat() {        
        writeParameters = null;
        mInfo = new HashMap();
        mInfo.put("name", "PostGrid");
        mInfo.put("description", "PostGrid"); 
        mInfo.put("vendor", "Geomatys");
	mInfo.put("docURL", "http://www.geomatys.fr/");
	mInfo.put("version", "1.0");
        readParameters = new ParameterGroup(
                new DefaultParameterDescriptorGroup(mInfo,
                new GeneralParameterDescriptor[] { READ_GRIDGEOMETRY2D }));
    }

    /**
     * Gets a reader for the raster chosen in the DataBase.
     * 
     * @param input The input object.
     * @return A reader on the grid coverage chosen.
     */
    public GridCoverageReader getReader(final Object input) {
        return getReader(input, null);
    }

    /**
     * Gets a reader for the raster, specifying some {@code hints}.
     * 
     * @param input The input object.
     * @param hints Hints value for the data.
     * @return A reader on the grid coverage chosen.
     */
    public GridCoverageReader getReader(final Object input, final Hints hints) {
        try {
            return new PostGridReader(this, input, null);
        } catch (DataSourceException ex) {
            throw new RuntimeException(ex); // TODO: trouver une meilleur exception.
        }
    }
    
    /**
     * Gets a reader for the raster. Allows to specify a series value.
     * 
     * @param input The input object.
     * @param hints Hints value for the data.
     * @param series The name of a Series.
     * @return A reader on the grid coverage chosen.
     */
    public GridCoverageReader getReader(final Object input, final Hints hints, 
            final String series) {
        try {
            return new PostGridReader(this, input, null, series);
        } catch (DataSourceException ex) {
            throw new RuntimeException(ex); // TODO: trouver une meilleur exception.
        }
    }

    /**
     * Gets a writer for the raster.
     * Not used in our implementation.
     *
     * @param object The source in which we will write.
     */
    public GridCoverageWriter getWriter(Object object) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    /**
     * Specifies if the source is a valid raster, and by the way is available.
     *
     * @param object The source to test.
     *
     * @todo Not yet implemented (previous implementation was useless).
     */
    public boolean accepts(Object object) {
        return true;
    }
    
    /**
     * Not used in our implementation.
     */
    public GeoToolsWriteParams getDefaultImageIOWriteParameters() {
        return null;
    }
   

}
