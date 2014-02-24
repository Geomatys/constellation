package org.constellation.setup;

import org.apache.sis.util.logging.Logging;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.SecurityManagerAdapter;
import org.constellation.configuration.ws.rs.ConfigurationUtilities;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.internal.image.io.SetupBIL;
import org.geotoolkit.internal.image.io.SetupGeoTiff;
import org.geotoolkit.internal.io.Installation;
import org.geotoolkit.lang.Setup;

import javax.imageio.ImageIO;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Use this installer to copy a file-system configuration into db config.
 *
 * @author Alexis Manin (Geomatys)
 */
public class Installer implements ServletContextListener {

    private static final Logger LOGGER = Logging.getLogger(Installer.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
            Hints.putSystemDefault(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);

        LOGGER.log(Level.INFO, "=== Starting GeotoolKit ===");

        try{

            ConfigurationEngine.setSecurityManager(new SecurityManagerAdapter() {
                @Override
                public String getCurrentUserLogin() {
                    return "admin";
                }
            });

            Hints.putSystemDefault(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);

            //Initialize geotoolkit
            Installation.allowSystemPreferences = false;
            // A crappy hack to ensure setup system will initialize image readers before coverage providers. If not, file coverage provider could no list all managed formats.
            ImageIO.scanForPlugins();
            new org.geotoolkit.internal.image.Setup().initialize(null, false);
            new org.geotoolkit.internal.image.io.Setup().initialize(null, false);
            new SetupGeoTiff().initialize(null, false);
            new SetupBIL().initialize(null, false);
            Setup.initialize(null);

            try {
                Class.forName("javax.media.jai.JAI");
            } catch (ClassNotFoundException ex) {
                LOGGER.log(Level.SEVERE, "JAI libraries are not in the classpath. Please install it.\n "
                        + ex.getLocalizedMessage(), ex);
            }
            LOGGER.log(Level.INFO, "=== GeotoolKit successfully started ===");
        } catch(Exception ex) {
            LOGGER.log(Level.WARNING, "=== GeotoolKit failed to start ===\n"+ex.getLocalizedMessage(), ex);
        }

        try {
            ConfigurationUtilities.FileToDBConfig(null);
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "Failed to copy file-system configuration. "+e.getLocalizedMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        LOGGER.log(Level.INFO, "=== Stopping GeotoolKit ===");
        try{
            Setup.shutdown();
            //wait for threads to die
            wait(2000);
            LOGGER.log(Level.INFO, "=== GeotoolKit successfully stopped ===");
        }catch(Exception ex){
            LOGGER.log(Level.WARNING, "=== GeotoolKit failed to stop ===\n"+ex.getLocalizedMessage(), ex);
        }
    }
}
