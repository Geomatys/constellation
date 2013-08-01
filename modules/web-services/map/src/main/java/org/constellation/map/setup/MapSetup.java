/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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
package org.constellation.map.setup;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.StyleProviderProxy;

import org.geotoolkit.image.jai.Registry;
import org.geotoolkit.internal.SetupService;
import org.apache.sis.util.logging.Logging;
/**
 * specific setup for map service 
 * 
 * @author Guilhem Legal (Geomatys)
 */
public class MapSetup implements SetupService {

    private static final Logger LOGGER = Logging.getLogger(MapSetup.class);
    
    @Override
    public void initialize(Properties properties, boolean reinit) {
        
        LOGGER.log(Level.WARNING, "=== Activating Native Codec ===");
        
        //reset values, only allow pure java readers
        for(String jn : ImageIO.getReaderFormatNames()){
            Registry.setNativeCodecAllowed(jn, ImageReaderSpi.class, false);
        }

        for(String jn : ImageIO.getReaderFormatNames()){
            Registry.setNativeCodecAllowed(jn, ImageReaderSpi.class, false);
        }
        
        //reset values, only allow pure java writers
        for(String jn : ImageIO.getWriterFormatNames()){
            Registry.setNativeCodecAllowed(jn, ImageWriterSpi.class, false);
        }
        
        for(String jn : ImageIO.getWriterFormatNames()){
            Registry.setNativeCodecAllowed(jn, ImageWriterSpi.class, false);
        }
    }

    /**
     * Invoked when the module needs to be shutdown.
     */
    @Override
    public void shutdown() {
        LayerProviderProxy.getInstance().dispose();
        StyleProviderProxy.getInstance().dispose();
    }
}
