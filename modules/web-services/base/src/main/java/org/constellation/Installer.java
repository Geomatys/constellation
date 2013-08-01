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
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.internal.io.Installation;
import org.geotoolkit.lang.Setup;
import org.apache.sis.util.logging.Logging;

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
            ImageIO.scanForPlugins();
            Setup.initialize(null);

            try {
                Class.forName("javax.media.jai.JAI");
            } catch (ClassNotFoundException ex) {
                LOGGER.log(Level.SEVERE, "JAI librairies are not in the classpath. Please install it.\n "
                        + ex.getLocalizedMessage(), ex);
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
