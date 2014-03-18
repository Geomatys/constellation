/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2014, Geomatys
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
package org.constellation.map.featureinfo;

import org.constellation.configuration.GetFeatureInfoCfg;
import org.constellation.provider.Data;
import org.geotoolkit.display.PortrayalException;
import org.geotoolkit.display2d.service.CanvasDef;
import org.geotoolkit.display2d.service.SceneDef;
import org.geotoolkit.display2d.service.ViewDef;
import org.geotoolkit.ows.xml.GetFeatureInfo;

import java.awt.Rectangle;
import java.util.List;

/**
 * FeatureInfo formatter.
 *
 * @author Quentin Boileau (Geomatys)
 */
public interface FeatureInfoFormat {

    /**
     * Compute and return FeatureInfoFormat object.
     * Parameters sdef, vdef, cdef and searchArea must be used to create a GraphicVisitor which search the intersected
     * Features/Coverage from which FeatureInfo object can be computed.
     *
     * @param sdef {@link SceneDef}
     * @param vdef {@link ViewDef}
     * @param cdef {@link CanvasDef}
     * @param searchArea {@link Rectangle} searching area
     * @param getFI {@link GetFeatureInfo} source request from a map service like WMS or WMTS.
     * @return an object compatible with requested mimetype. For example an BufferedImage for image/png or String for
     * text/html or text/plain.
     * @throws PortrayalException
     * @see AbstractFeatureInfoFormat#getCandidates(SceneDef, ViewDef, CanvasDef, Rectangle, Integer)
     */
    public Object getFeatureInfo(final SceneDef sdef, final ViewDef vdef, final CanvasDef cdef, final Rectangle searchArea,
                                 final GetFeatureInfo getFI) throws PortrayalException;

    /**
     * FeatureInfoFormat supported mimeTypes.
     *
     * @return a list of supported mimeTypes of the current FeatureInfoFormat.
     */
    public List<String> getSupportedMimeTypes();

    /**
     * Set {@link org.constellation.configuration.GetFeatureInfoCfg} configuration from a service configuration.
     * This object can contain parameters which can be used to render FeatureInfoFormat
     *
     * @param conf {@link org.constellation.configuration.GetFeatureInfoCfg}
     */
    public void setConfiguration(GetFeatureInfoCfg conf);

    /**
     * Get {@link org.constellation.configuration.GetFeatureInfoCfg} set configuration
     * This object can contain parameters which can be used to render FeatureInfoFormat
     *
     * @return {@link org.constellation.configuration.GetFeatureInfoCfg} configuration, can be null.
     */
    public GetFeatureInfoCfg getConfiguration();

    /**
     * Set the list of {@link Data} from which the {@link SceneDef}
     * {@link org.geotoolkit.map.MapContext} was build.
     * @param layers
     */
    public void setLayersDetails(List<Data> layers);

    /**
     * Get the list of {@link Data} from which the {@link SceneDef}
     * {@link org.geotoolkit.map.MapContext} was build.
     * @return layers or null
     */
    public List<Data> getLayersDetails();
}
