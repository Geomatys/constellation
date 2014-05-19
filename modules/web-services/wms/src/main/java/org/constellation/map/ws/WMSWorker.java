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
package org.constellation.map.ws;

//J2SE dependencies
import java.awt.image.BufferedImage;
import java.util.Map;

//Constellation dependencies
import org.constellation.portrayal.internal.PortrayalResponse;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.Worker;

//Geotoolkit dependencies
import org.geotoolkit.wms.xml.AbstractWMSCapabilities;
import org.geotoolkit.wms.xml.DescribeLayer;
import org.geotoolkit.wms.xml.GetCapabilities;
import org.geotoolkit.wms.xml.GetMap;
import org.geotoolkit.wms.xml.GetFeatureInfo;
import org.geotoolkit.sld.xml.GetLegendGraphic;
import org.geotoolkit.sld.xml.v110.DescribeLayerResponseType;


/**
 * Definition of a {@code Web Map Service} worker called by a facade
 * to perform the logic for a particular WMS instance.
 *
 * @version $Id$
 *
 * @author Johann Sorel (Geomatys)
 */
public interface WMSWorker extends Worker{

    /**
     * Returns a description of the requested layer.
     *
     * @param descLayer The {@linkplain DescribeLayer describe layer} request done on this service.
     * @throws CstlServiceException
     */
    DescribeLayerResponseType describeLayer(final DescribeLayer descLayer) throws CstlServiceException;

    /**
     * Returns an unmarshalled {@linkplain AbstractWMSCapabilities get capabilities} object.
     *
     * @param getCapabilities The {@linkplain GetCapabilities get capabilities} request done on this service.
     * @throws CstlServiceException
     */
    AbstractWMSCapabilities getCapabilities(final GetCapabilities getCapabilities) throws CstlServiceException;

    /**
     * Returns an Entry, which will contain the result of a {@code GetFeatureInfo} request and the requested mimeType.
     *
     * @param getFI The {@linkplain org.geotoolkit.wms.xml.GetFeatureInfo get feature info} request done on this service.
     * @return Map.Entry with requested mimeType and getFeatureInfo result object.
     * @throws CstlServiceException
     */
    Map.Entry<String, Object> getFeatureInfo(final GetFeatureInfo getFI) throws CstlServiceException;

    /**
     * Returns a {@link BufferedImage}, which is the result of a {@code GetLegendGraphic} request.
     *
     * @param getLegend The {@linkplain GetLegendGraphic get legend graphic} request done on this service.
     * @throws CstlServiceException
     */
    PortrayalResponse getLegendGraphic(final GetLegendGraphic getLegend) throws CstlServiceException;

    /**
     * Returns a {@link BufferedImage}, which is the result of a {@code GetMap} request.
     *
     * @param getMap The {@linkplain GetMap get map} request done on this service.
     * @throws CstlServiceException
     */
    PortrayalResponse getMap(final GetMap getMap) throws CstlServiceException;
}
