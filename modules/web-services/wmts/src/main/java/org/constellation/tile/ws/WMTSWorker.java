/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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

//J2SE dependencies
import java.awt.image.BufferedImage;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.Worker;
import org.geotoolkit.coverage.TileReference;
import org.geotoolkit.wmts.xml.v100.Capabilities;
import org.geotoolkit.wmts.xml.v100.GetCapabilities;
import org.geotoolkit.wmts.xml.v100.GetFeatureInfo;
import org.geotoolkit.wmts.xml.v100.GetTile;


/**
 * Abstract definition of a {@code Web Map Tile Service} worker called by a facade
 * to perform the logic for a particular WMTS instance.
 *
 * @version $Id$
 * 
 * @author Cédric Briançon (Geomatys)
 * @author Guilhem Legal (Geomatys)
 * @since 0.3
 */
public interface WMTSWorker extends Worker {

    /**
     * Returns an unmarshalled {@linkplain Capabilities get capabilities} object.
     *
     * @param getCapabilities The {@linkplain GetCapabilities get capabilities} request done on this service.
     * @throws CstlServiceException
     */
    Capabilities getCapabilities(final GetCapabilities getCapabilities) throws CstlServiceException;

    /**
     * Returns a string, which will contain the result of a {@code GetFeatureInfo} request.
     *
     * @param getFeatureInfo The {@linkplain GetFeatureInfo get feature info} request done on this service.
     * @throws CstlServiceException
     */
    String getFeatureInfo(final GetFeatureInfo getFeatureInfo) throws CstlServiceException;

    /**
     * Returns a {@link BufferedImage}, which is the result of a {@code GetTile} request.
     *
     * @param getTile The {@linkplain GetTile get tile} request done on this service.
     * @throws CstlServiceException
     */
    TileReference getTile(final GetTile getTile) throws CstlServiceException;

}
