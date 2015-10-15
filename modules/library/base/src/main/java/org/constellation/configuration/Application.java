/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2015 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * Utility class that gathers and merge configurations from embedded/external property files,
 * system properties and system environment variables.
 * The resolving order is :
 * <ol>
 *     <li>System environment variables. Evaluated at runtime</li>
 *     <li>System property variables (with <code>-Dvar=value</code> or <code>System.setProperty("var", "value");</code>). Evaluated at runtime</li>
 *     <li>External property file (referenced with <code>-Dcstl.config=/path/to/config.properties</code> option). Evaluated once.</li>
 *     <li>Embedded property file in resources. Evaluated once</li>
 * </ol>
 *
 * Usage : <br\>
 * <code>
 *     String cstlHome = Application.getProperty(Application.CSTL_HOME_KEY);
 * </code>
 * @author Quentin Boileau (Geomatys)
 */
public final class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger("org.constellation.configuration");

    private static final Properties APP_PROPERTIES = new Properties();

    //lazy singleton to call initialization
    private static final Application INSTANCE = new Application();

    private Application() {

        //load embedded configuration file
        final String resourcePath = "/org/constellation/configuration/constellation.properties";
        try (final InputStream classLoaderSettings = Application.class.getResourceAsStream(resourcePath)) {
            LOGGER.info("Load default configuration.");
            APP_PROPERTIES.load(classLoaderSettings);
        } catch (IOException e) {
            throw new ConfigurationRuntimeException("Unable to load default configuration file from current class loader", e);
        }

        //search for external configuration file
        final String externalConf = System.getProperty(AppProperty.CSTL_CONFIG.getKey());
        if (externalConf != null) {
            LOGGER.info("Load external configuration from "+externalConf);

            final File externalFile = new File(externalConf);
            if (!externalFile.isFile()) {
                LOGGER.warn("Unable to load external configuration because path is not a valid file.");
            }

            try (FileInputStream fin = new FileInputStream(externalFile)) {
                APP_PROPERTIES.load(fin);
            } catch (IOException e) {
                //no need to crash application because of an invalid external configuration file.
                LOGGER.warn("Unable to load properties from external configuration file", e);
            }
        }

    }

    /**
     * Get all constellation settings.
     *
     * @return a merge of default, external and environment properties
     */
    private static Properties getProperties() {
        return (Properties) APP_PROPERTIES.clone();
    }

    /**
     * Search for the property with the specified key in {@link #getProperties()}
     * Look first in system environment variables and if nothing found, fallback
     * to application properties.
     *
     * @param key AppProperty
     * @return property value or <code>null</code> if not found.
     */
    public static String getProperty(AppProperty key) {
        return getProperty(key.getKey(), null);
    }

    /**
     * Search for the property with the specified key in {@link #getProperties()}
     * Look first in system environment variables and if nothing found, fallback
     * to application properties.
     *
     * @param key property key
     * @return property value or <code>null</code> if not found.
     */
    public static String getProperty(String key) {
        return getProperty(key, null);
    }

    /**
     * Search for the property with the specified key in {@link #getProperties()}
     * Look first in system environment variables and if nothing found, fallback
     * to application properties.
     *
     * @param key AppProperty
     * @param fallback fallback used if property not found.
     * @return property value or fallback value if not found.
     */
    public static String getProperty(AppProperty key, String fallback) {
        return getProperty(key.getKey(), fallback);
    }

    /**
     * Search for the property with the specified key in {@link #getProperties()}
     * Look first in system environment variables and if nothing found, fallback
     * to application properties.
     *
     * @param key property key
     * @param fallback fallback used if property not found.
     * @return property value or fallback value if not found.
     */
    public static String getProperty(String key, String fallback) {
        //check env
        final String envValue = System.getenv(key);
        if (envValue != null) {
            return envValue;
        }

        //check system properties
        final String propValue = System.getProperty(key);
        if (propValue != null) {
            return propValue;
        }

        //check conf
        return getProperties().getProperty(key, fallback);
    }

    /**
     * Search for all application properties matching given prefix.
     *
     * @param prefix property key
     * @return all property that match given prefix. Output {@link Properties} can be
     * empty but never null.
     */
    public static Properties getProperties(String prefix) {
        Properties properties = new Properties();
        final Properties appProperties = getProperties();
        for (Map.Entry<Object, Object> appProp : appProperties.entrySet()) {
            if (((String)appProp.getKey()).startsWith(prefix)) {
                properties.put(appProp.getKey(), appProp.getValue());
            }
        }

        //override with system properties variables
        final Properties systemProperties = System.getProperties();
        for (Map.Entry<Object, Object> sysProp : systemProperties.entrySet()) {
            if (((String)sysProp.getKey()).startsWith(prefix)) {
                properties.put(sysProp.getKey(), sysProp.getValue());
            }
        }

        //override with system environment variables
        final Map<String, String> envProps = System.getenv();
        for (Map.Entry<String, String> env : envProps.entrySet()) {
            if (env.getKey().startsWith(prefix)) {
                properties.put(env.getKey(), env.getValue());
            }
        }
        return properties;
    }
}
