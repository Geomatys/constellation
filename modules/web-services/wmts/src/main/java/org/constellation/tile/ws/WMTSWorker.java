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

import java.awt.image.BufferedImage;
import java.util.logging.Logger;

import org.constellation.wmts.v100.Capabilities;
import org.constellation.wmts.v100.GetCapabilities;
import org.constellation.wmts.v100.GetFeatureInfo;
import org.constellation.wmts.v100.GetTile;
import org.constellation.ws.CstlServiceException;


/**
 * Working part of the WMTS service.
 *
 * @todo Implements it.
 *
 * @version $Id$
 *
 * @author Cédric Briançon (Geomatys)
 * @since 0.3
 */
public class WMTSWorker extends AbstractWMTSWorker {

    /**
     * The default logger.
     */
    private static final Logger LOGGER = Logger.getLogger("org.constellation.tile.ws");

    /**
     * Instanciates the working class for a SOAP client, that do request on a SOAP PEP service.
     */
    public WMTSWorker() {
        LOGGER.info("WMTS Service started");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Capabilities getCapabilities(GetCapabilities gc) throws CstlServiceException {
        // TODO: implements GetCapabilities
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFeatureInfo(GetFeatureInfo gf) throws CstlServiceException {
        // TODO: implements GetFeatureInfo
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BufferedImage getTile(GetTile gt) throws CstlServiceException {
        // TODO: implements GetTile
        throw new UnsupportedOperationException();
    }
}
