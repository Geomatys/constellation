/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.wmts.ws;

//J2SE dependencies

import org.constellation.ws.CstlServiceException;
import org.constellation.ws.Worker;
import org.geotoolkit.wmts.xml.v100.Capabilities;
import org.geotoolkit.wmts.xml.v100.GetCapabilities;
import org.geotoolkit.wmts.xml.v100.GetFeatureInfo;
import org.geotoolkit.wmts.xml.v100.GetTile;

import java.awt.image.BufferedImage;
import java.util.Map;
import org.geotoolkit.storage.coverage.TileReference;


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
     * Returns an Entry, which will contain the result of a {@code GetFeatureInfo} request and the requested mimeType.
     *
     * @param getFeatureInfo The {@linkplain GetFeatureInfo get feature info} request done on this service.
     * @return Map.Entry with requested mimeType and getFeatureInfo result object.
     * @throws CstlServiceException
     */
    Map.Entry<String, Object> getFeatureInfo(final GetFeatureInfo getFeatureInfo) throws CstlServiceException;

    /**
     * Returns a {@link BufferedImage}, which is the result of a {@code GetTile} request.
     *
     * @param getTile The {@linkplain GetTile get tile} request done on this service.
     * @throws CstlServiceException
     */
    TileReference getTile(final GetTile getTile) throws CstlServiceException;

}
