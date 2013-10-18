package org.constellation.configuration;

import java.util.Properties;

/**
 * Simple class to acces on constellation.properties file without pass by {@link ConfigDirectory}
 *
 * @author bgarcia
 * @version 0.9
 * @since 0.9
 */
public class ConfigProperties {

    /**
     * Properties extract from constellation.properties file
     */
    private static Properties cstlProperties;


    public static Properties getCstlProperties() {
        return cstlProperties;
    }

    public static void setCstlProperties(final Properties cstlProperties) {
        ConfigProperties.cstlProperties = cstlProperties;
    }
}
