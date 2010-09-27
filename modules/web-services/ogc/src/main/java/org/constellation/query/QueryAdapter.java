/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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
package org.constellation.query;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;

import org.geotoolkit.lang.Immutable;
import org.geotoolkit.sld.MutableStyledLayerDescriptor;
import org.geotoolkit.sld.xml.Specification.StyledLayerDescriptor;
import org.geotoolkit.sld.xml.XMLUtilities;
import org.geotoolkit.util.Version;
import org.geotoolkit.util.logging.Logging;
import org.opengis.util.FactoryException;


/**
 * Convenient class to transform Strings to real Java objects.
 *
 * @version $Id$
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
@Immutable
public final class QueryAdapter {
    /**
     * The default logger.
     */
    private static final Logger LOGGER = Logging.getLogger(QueryAdapter.class);

    private QueryAdapter() {}
    
    /**
     * Verify that all layers are queryable for a {@code GetFeatureInfo}.
     *
     * @param queryLayers A list of requested layer names
     * @param version The version of the WMS service.
     * @return The same list as provided if all layers are queryable.
     *
     * @todo The method {@link Layer#isQueryable} is not valid. It should verify in the
     *       database if a layer is queryable, meaning if a layer is queryable by a
     *       {@code GetFeatureInfo} request. Either rename the {@link Layer#isQueryable}
     *       or create a new one that provides this information.
     */
    public static List<String> areQueryableLayers(final List<String> queryLayers,
                                final Version version)
    {
        /* Do nothing for the moment, waiting for a method in {@link Layer} in order to
         * handle the queryable attribute for a {@link Layer}.
         */

        /*final NamedLayerDP dp = NamedLayerDP.getInstance();
        for (String layerName : queryLayers) {
            final LayerDetails layer = dp.get(layerName);
            if (!layer.isQueryable(Service.WMS)) {
                throw new WMSWebServiceException("Layer "+ layerName +" is not queryable",
                        WMSExceptionCode.LAYER_NOT_QUERYABLE, version);
            }
        }*/
        return queryLayers;
    }

    public static MutableStyledLayerDescriptor toSLD(final String sldURL, final StyledLayerDescriptor version)
                                                                                  throws MalformedURLException
    {

        if (sldURL == null || sldURL.trim().isEmpty()) {
            return null;
        }

        final URL url = new URL(sldURL.trim());


        MutableStyledLayerDescriptor sld = null;

        final XMLUtilities sldUtilities = new XMLUtilities();

        try {
            sld = sldUtilities.readSLD(url, version);
        } catch (JAXBException ex) {
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
        } catch (FactoryException ex) {
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
        }

        return sld;
    }
}
