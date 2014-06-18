package org.constellation.admin;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.sis.util.logging.Logging;
import org.constellation.configuration.ConfigDirectory;
import org.springframework.stereotype.Component;

@Component
public class ConfigurationBusiness {
    private static final Logger LOGGER = Logging.getLogger(ConfigurationBusiness.class);


    public File getConfigurationDirectory() {
        return ConfigDirectory.getConfigDirectory();
    }

    public File getDataDirectory() {
        return ConfigDirectory.getDataDirectory();
    }

    public static String getProperty(final String key) {
        return ConfigurationEngine.getConstellationProperty(key, null);
    }
    
    public static void setProperty(final String key, final String value) {
        ConfigurationEngine.setConstellationProperty(key, value);
    }
    
    public static boolean setConfigPath(final String path) {
        // Set the new user directory
        if (path != null && !path.isEmpty()) {
            final File userDirectory = new File(path);
            if (!userDirectory.isDirectory()) {
                userDirectory.mkdir();
            }
            ConfigDirectory.setConfigDirectory(userDirectory);
            return true;
        }
        return false;
    }
    
    public static String getConfigPath() {
        return ConfigDirectory.getConfigDirectory().getPath();
    }

    public static Properties getMetadataTemplateProperties() {
        final File cstlDir = ConfigDirectory.getConfigDirectory();
        final File propFile = new File(cstlDir, "metadataTemplate.properties");
        final Properties prop = new Properties();
        if (propFile.exists()) {
            try {
                prop.load(new FileReader(propFile));
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "IOException while loading metadata template properties file", ex);
            }
        }
        return prop;
    }
}
