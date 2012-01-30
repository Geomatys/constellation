/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.tile.ws;

import java.io.InputStream;
import java.util.Map;
import org.geotoolkit.coverage.GridMosaic;
import org.geotoolkit.storage.DataStoreException;

/**
 * Reference to a single tile in a pyramid.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class StreamReference {
    
    private final GridMosaic mosaic;
    private final int col;
    private final int row;
    private final Map hints;

    public StreamReference(GridMosaic mosaic, int col, int row, Map hints) {
        this.mosaic = mosaic;
        this.col = col;
        this.row = row;
        this.hints = hints;
    }
    
    public InputStream getStream() throws DataStoreException{
        return mosaic.getTileStream(col, row, hints);
    }
    
}
