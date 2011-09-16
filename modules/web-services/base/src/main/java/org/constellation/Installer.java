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
package org.constellation;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.image.jai.Registry;
import org.geotoolkit.internal.io.Installation;
import org.geotoolkit.lang.Setup;
import org.geotoolkit.util.logging.Logging;

/**
 * Class responsible for starting and stopping geotoolkit.
 * 
 * @author Johann Sorel (Geomatys)
 */
public final class Installer implements ServletContextListener{

    private static final Logger LOGGER = Logging.getLogger(Installer.class);
    
    @Override
    public synchronized void contextInitialized(ServletContextEvent sce) {
        
        LOGGER.log(Level.WARNING, "=== Starting GeotoolKit ===");
        
        try{
            Hints.putSystemDefault(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);

            //Initialize geotoolkit
            Installation.allowSystemPreferences = false;
            Setup.initialize(null);

            ImageIO.scanForPlugins();
            try {
                Class.forName("javax.media.jai.JAI");
            } catch (ClassNotFoundException ex) {
                LOGGER.log(Level.SEVERE, "JAI librairies are not in the classpath. Please install it.\n "
                        + ex.getLocalizedMessage(), ex);
            }
            
            //reset values, only allow pure java readers
            for(String jn : ImageIO.getReaderFormatNames()){
                Registry.setNativeCodecAllowed(jn, ImageReaderSpi.class, false);
            }
            
            //reset values, only allow pure java writers
            for(String jn : ImageIO.getWriterFormatNames()){
                Registry.setNativeCodecAllowed(jn, ImageWriterSpi.class, false);
            }
            
            LOGGER.log(Level.WARNING, "=== GeotoolKit sucessfully started ===");
        } catch(Exception ex) {
            LOGGER.log(Level.WARNING, "=== GeotoolKit failed to start ===\n"+ex.getLocalizedMessage(), ex);
        }
    }

    @Override
    public synchronized void contextDestroyed(ServletContextEvent sce) {
        
        LOGGER.log(Level.WARNING, "=== Stopping GeotoolKit ===");
        try{
            Setup.shutdown();
            //wait for threads to die
            wait(2000);
            LOGGER.log(Level.WARNING, "=== GeotoolKit sucessfully stopped ===");
        }catch(Exception ex){
            LOGGER.log(Level.WARNING, "=== GeotoolKit failed to stop ===\n"+ex.getLocalizedMessage(), ex);            
        }        
    }
    
}
