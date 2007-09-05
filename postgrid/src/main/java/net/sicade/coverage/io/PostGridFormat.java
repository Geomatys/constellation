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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.geotools.util.NumberRange;

import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterDescriptor;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.factory.Hints;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.parameter.ParameterGroup;


/**
 * Description of PostGrid DataBase format.
 *
 * @version $Id$
 * @author Cédric Briançon
 */
public class PostGridFormat extends AbstractGridFormat {
    /**
     *
     */
    private static final DefaultParameterDescriptor TIME = new DefaultParameterDescriptor(
            "TIME", List.class, null, null);

    /**
     *
     */
    private static final DefaultParameterDescriptor ELEVATION = new DefaultParameterDescriptor(
            "ELEVATION", Integer.TYPE, null, null);

    /**
     *
     */
    private static final DefaultParameterDescriptor DIM_RANGE = new DefaultParameterDescriptor(
            "DIM_RANGE", NumberRange.class, null, null);

    /**
     * The series for this coverage.
     */
    private final String layerName;

    /**
     * Creates a new instance of PostGridFormat.
     * Contains the main information about the PostGrid DataBase format.
     *
     */
    public PostGridFormat(final String layerName) {
        this.layerName = layerName;
        writeParameters = null;
        mInfo = new HashMap();
        mInfo.put("name", "PostGrid");
        mInfo.put("description", "PostGrid");
        mInfo.put("vendor", "Geomatys");
        mInfo.put("docURL", "http://www.geomatys.fr/");
        mInfo.put("version", "1.0");
        readParameters = new ParameterGroup(
                new DefaultParameterDescriptorGroup(mInfo,
                new GeneralParameterDescriptor[] { READ_GRIDGEOMETRY2D, TIME, ELEVATION, DIM_RANGE}));
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
        return new PostGridReader(this, layerName);
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
