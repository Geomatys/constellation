package org.constellation.admin;

import java.io.File;

import org.constellation.configuration.ConfigDirectory;
import org.springframework.stereotype.Component;

@Component
public class ConfigurationBusiness {

    File getConfigurationDirectory() {
        return ConfigDirectory.getConfigDirectory();
    }

    File getDataDirectory() {
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
}
