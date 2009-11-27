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
package org.constellation.provider.configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import org.constellation.util.Util;

/**
 * Temporary copy of static methods from the WebService class (in module web-base),
 * in order to retrieve the configuration directory of Constellation.
 *
 * TODO: this implementation should probably been handled by the server registry, so
 *       move it there.
 *
 * @version $Id$
 *
 * @author Cédric Briançon (Geomatys)
 */
public final class ConfigDirectory {

    private ConfigDirectory() {}
    
    /**
     * The default debugging logger for all web services.
     */
    private static final Logger LOGGER = Logger.getLogger("org.constellation.provider.configuration");

    /**
     * Specifies if the process is running on a Glassfish application server.
     */
    private static Boolean runningOnGlassfish = null;

    /**
     * The user directory where configuration files are stored on Unix platforms.
     * TODO: How does this relate to the directories used in deployment? This is
     *       in the home directory of the user running the container?
     */
    public static final String UNIX_DIRECTORY = ".constellation";

    /**
     * The user directory where configuration files are stored on Windows platforms.
     */
    public static final String WINDOWS_DIRECTORY = "Application Data\\Constellation";

    /**
     * Returns the configuration directory for Constellation. It is either the one
     * defined in the JNDI variable, or the default in the home directory.
     */
    public static File getConfigDirectory() {
        try {
            final String path = getPropertyValue("Constellation", "config_dir");
            if (path != null) {
                final File folder = new File(path);
                if (folder.exists() && folder.canRead() && folder.canWrite()) {
                    return folder;
                } else {
                    try {
                        folder.createNewFile();
                        return folder;
                    } catch (IOException ex) {
                        LOGGER.log(Level.INFO, "Unable to create the file "+ folder, ex);
                    }
                }
            } else {
                throw new NamingException("config_dir is not defined in the Constellation JNDI resource.");
            }

        } catch (NamingException ex) {
            LOGGER.log(Level.FINE, "Does not succeed in retrieving Constellation's JNDI configuration. " +
                    "Using the default configuration directory instead.", ex);
        }

        return getConstellationDirectory();
    }

    /**
     * Return the ".constellation" directory.
     */
    private static File getConstellationDirectory() {
        File constellationDirectory;
        final String home = System.getProperty("user.home");

        if (System.getProperty("os.name", "").startsWith("Windows")) {
             constellationDirectory = new File(home, WINDOWS_DIRECTORY);
        } else {
             constellationDirectory = new File(home, UNIX_DIRECTORY);
        }
        return constellationDirectory;
    }

    /**
     * Get the value for a property defined in the JNDI context chosen.
     *
     * @param propGroup If you use Glassfish, you have to specify the name of the resource that
     *                  owns the property you wish to get. Otherwise you should specify {@code null}
     * @param propName  The name of the property to get.
     * @return The property value defines in the context, or {@code null} if no property of this name
     *         is defined in the resource given in parameter.
     * @throws NamingException if an error occurs while initializing the context, or if an empty value
     *                         for propGroup has been passed while using a Glassfish application server.
     */
    public static String getPropertyValue(final String propGroup, final String propName) throws NamingException {
        final InitialContext ctx = new InitialContext();
        if (runningOnGlassfish == null) {
            runningOnGlassfish = (System.getProperty("domain.name") != null) ? true : false;
        }
        if (runningOnGlassfish) {
            if (propGroup == null) {
                throw new NamingException("The coverage property group is not specified.");
            }
            final Reference props = (Reference) getContextProperty(propGroup, ctx);
            if (props == null) {
                throw new NamingException("The coverage property group specified does not exist.");
            }
            final RefAddr permissionAddr = (RefAddr) props.get(propName);
            if (permissionAddr != null) {
                return (String) permissionAddr.getContent();
            }
            return null;
        } else {
            final javax.naming.Context envContext = (javax.naming.Context) ctx.lookup("java:/comp/env");
            return (String) getContextProperty(propName, envContext);
        }
    }

    /**
     * Returns the context value for the key specified, or {@code null} if not found
     * in this context.
     *
     * @param key The key to search in the context.
     * @param context The context which to consider.
     */
    private static Object getContextProperty(final String key, final javax.naming.Context context) {
        Object value = null;
        try {
            value = context.lookup(key);
        } catch (NamingException n) {
            // Do nothing, the key is not found in the context and the value is still null.
        }

        return value;
    }

    /**
     * Returns the file named "config.xml" under the folder "WEB-INF/classes" of the
     * Constellation' web archive, or {@code null} if no config file was found.
     */
    public static File getWarPackagedConfig() {
        LOGGER.warning("Try to find the file config.xml into the web archive of Constellation !");

        final InputStream is = Util.getResourceAsStream("/config.xml");
        if (is != null) {
            int i;
            try {

                final File configFile = File.createTempFile("temp", ".xml");

                final FileOutputStream fos = new FileOutputStream(configFile);
                while ((i = is.read()) != -1) {
                    fos.write(i);
                }
                fos.close();
                return configFile;

            } catch (IOException ex) {
                Logger.getLogger(ConfigDirectory.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            LOGGER.info("No config.xml resource found in the archive.");
        }
        return null;
    }
}
