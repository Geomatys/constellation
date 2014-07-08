/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
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

import org.apache.sis.util.logging.Logging;
import org.geotoolkit.util.FileUtilities;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Temporary copy of static methods from the WebService class (in module web-base),
 * in order to retrieve the configuration directory of Constellation.
 * <p/>
 * TODO: this implementation should probably been handled by the server registry, so
 * move it there.
 *
 * @author Cédric Briançon (Geomatys)
 * @author Guilhem Legal (Geomatys)
 * @version $Id$
 */
public final class ConfigDirectory {

    /**
     * The default debugging logger.
     */
    private static final Logger LOGGER = Logging.getLogger("org.constellation.provider.configuration");
    /**
     * The user directory where configuration files are stored on Unix platforms.
     * TODO: How does this relate to the directories used in deployment? This is
     * in the home directory of the user running the container?
     */
    private static final String UNIX_DIRECTORY = ".constellation";
    /**
     * The user directory where configuration files are stored on Windows platforms.
     */
    private static final String WINDOWS_DIRECTORY = "Application Data\\Constellation";
    /**
     * This should be a class loader from the main constellation application.
     */
    private static final ClassLoader baseClassLoader;

    //we try to load this variable at the start by reading a properties file
    static {
        baseClassLoader = Thread.currentThread().getContextClassLoader();
        final File webInfDirectory = getWebInfDiretory();
        final File propertiesFile = new File(webInfDirectory, "constellation.properties");
        if (propertiesFile.exists()) {
            try {
                Properties prop = FileUtilities.getPropertiesFromFile(propertiesFile);

                USER_DIRECTORY    = prop.getProperty("configuration_directory");
                DATA_DIRECTORY    = prop.getProperty("data_directory");
                DATA_INTEGRATED_DIRECTORY    = prop.getProperty("integrated_directory");
                UPLOAD_DIRECTORY    = prop.getProperty("upload_directory");
                METADATA_DIRECTORY= prop.getProperty("metadata_directory");
            } catch (IOException ex) {
                LOGGER.warning("IOException while reading the constellation properties file");
            }
        }
    }

    /**
     * The user directory where configuration files are stored.
     * this variable is fill by the user in the JSF interface.
     */
    public static String USER_DIRECTORY = null;
    public static String DATA_DIRECTORY = null;
    public static String DATA_INTEGRATED_DIRECTORY = null;
    public static String UPLOAD_DIRECTORY = null;
    public static String METADATA_DIRECTORY = null;
    public static String STYLE_DIRECTORY = null;
    /**
     * Specifies if the process is running on a Glassfish application server.
     */
    private static Boolean runningOnGlassfish = null;

    private ConfigDirectory() {
    }
    
    public static File getUserHomeDirectory(){
    	final String home = System.getProperty("user.home");
    	return new File(home);
    }

    private static File getWebInfDiretory() {
        final URL url = baseClassLoader.getResource("org/constellation/configuration/ConfigDirectory.class");
        String path = url.toString();
        path = path.substring(path.lastIndexOf(':') + 1); // we remove the file type
        final int separator = path.indexOf('!'); // we remove the path inside the jar
        if (separator != -1) {
            path = path.substring(0, separator);
        }
        File f = new File(path);
        f = f.getParentFile(); // lib
        f = f.getParentFile(); // WEB-INF
        return f;
    }

    /**
     * Try to get properties defined in constellation.properties file, which should be located into the web-inf directory.
     * @return {@link java.util.Properties} contained in the constellation.properties file, or null if we cannot get it.
     */
    public static Properties getConstellationProperties() {
        final File propertiesFile = new File(getWebInfDiretory(), "constellation.properties");
        if (propertiesFile.exists()) {
            try {
                return FileUtilities.getPropertiesFromFile(propertiesFile);
            } catch (IOException ex) {
                LOGGER.warning("IOException while reading the constellation properties file");
            }
        }
        return null;
    }

    /**
     * Give a data directory {@link java.io.File} defined on constellation.properties or
     * by default on .constellation-data from user home directory
     *
     * @return data directory as {@link java.io.File}
     */
    public static File getDataDirectory() {
        File constellationDataDirectory;

        if (DATA_DIRECTORY != null && !DATA_DIRECTORY.isEmpty()) {
            constellationDataDirectory = new File(DATA_DIRECTORY);
            if (!constellationDataDirectory.exists()) {
                LOGGER.log(Level.INFO, "The configuration directory {0} does not exist", DATA_DIRECTORY);
            } else if (!constellationDataDirectory.isDirectory()) {
                LOGGER.log(Level.INFO, "The configuration path {0} is not a directory", DATA_DIRECTORY);
            }
        } else {
            constellationDataDirectory = new File(System.getProperty("user.home"), ".constellation-data");
            if (!constellationDataDirectory.exists()) {
                constellationDataDirectory.mkdir();
            }
        }
        return constellationDataDirectory;
    }
    
    
    /**
     * Give a integrated data directory {@link java.io.File} defined on constellation.properties or
     * by default on .constellation-data/integrated/ from user home directory
     *
     * @return providers directory as {@link java.io.File}
     */
    public static File getDataIntegratedDirectory() {
    	
        File constellationDataIntegratedDirectory;
        File constellationDataDirectory = getDataDirectory();

        if (DATA_INTEGRATED_DIRECTORY != null && !DATA_INTEGRATED_DIRECTORY.isEmpty()) {
        	constellationDataIntegratedDirectory = new File(UPLOAD_DIRECTORY);
            if (!constellationDataIntegratedDirectory.exists()) {
                LOGGER.log(Level.INFO, "The configuration directory {0} does not exist", UPLOAD_DIRECTORY);
            } else if (!constellationDataIntegratedDirectory.isDirectory()) {
                LOGGER.log(Level.INFO, "The configuration path {0} is not a directory", UPLOAD_DIRECTORY);
            }
        } else {
        	constellationDataIntegratedDirectory = new File(constellationDataDirectory, "integrated");
            if (!constellationDataIntegratedDirectory.exists()) {
            	constellationDataIntegratedDirectory.mkdir();
            }
        }
       
        return constellationDataIntegratedDirectory;
    }
    
    /**
     * Give a integrated data directory {@link java.io.File} defined on constellation.properties or
     * by default on .constellation-data/integrated/ from user home directory
     * for given provider.
     *
     * @return providers directory as {@link java.io.File}
     */
    public static File getDataIntegratedDirectory(String providerId) {
    	final File rootFolder = getDataIntegratedDirectory();
        final File f = new File(rootFolder, providerId);
        f.mkdirs();
        return f;
    }
    
    
    /**
     * remove upload directory for the sessionId {@link java.io.File} 
     * by default on .constellation-data/upload/<sessionId> from user home directory
     * 
     * @param sessionId 
     */
    public static void removeUploadDirectory(String sessionId) {
    	File constellationDataDirectory;
    	File constellationUploadDirectory;
    	File sessionUploadDirectory;
    	
    	if (DATA_DIRECTORY != null && !DATA_DIRECTORY.isEmpty()) {
            constellationDataDirectory = new File(DATA_DIRECTORY);
            if (!constellationDataDirectory.exists()) {
                LOGGER.log(Level.INFO, "The configuration directory {0} does not exist", DATA_DIRECTORY);
            } else if (!constellationDataDirectory.isDirectory()) {
                LOGGER.log(Level.INFO, "The configuration path {0} is not a directory", DATA_DIRECTORY);
            }
        } else {
            constellationDataDirectory = new File(System.getProperty("user.home"), ".constellation-data");
        }
       
        if (UPLOAD_DIRECTORY != null && !UPLOAD_DIRECTORY.isEmpty()) {
        	constellationUploadDirectory = new File(UPLOAD_DIRECTORY);
            if (!constellationUploadDirectory.exists()) {
                LOGGER.log(Level.INFO, "The configuration directory {0} does not exist", UPLOAD_DIRECTORY);
            } else if (!constellationUploadDirectory.isDirectory()) {
                LOGGER.log(Level.INFO, "The configuration path {0} is not a directory", UPLOAD_DIRECTORY);
            }
        } else {
        	constellationUploadDirectory = new File(constellationDataDirectory, "upload");
            
        }
        sessionUploadDirectory = new File(constellationUploadDirectory, sessionId);
        if (sessionUploadDirectory.exists()) {
        	deleteFolder(sessionUploadDirectory);
        }
        
	}

    private static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    /**
     * Give a upload directory {@link java.io.File} defined on constellation.properties or
     * by default on .constellation-data/upload/<sessionId> from user home directory
     * 
     * @param sessionId 
     *
     * @return providers directory as {@link java.io.File}
     */
    public static File getUploadDirectory(String sessionId) {

    	File constellationDataDirectory = getDataDirectory();
    	File constellationUploadDirectory;
    	File sessionUploadDirectory;
        
        if (UPLOAD_DIRECTORY != null && !UPLOAD_DIRECTORY.isEmpty()) {
        	constellationUploadDirectory = new File(UPLOAD_DIRECTORY);
            if (!constellationUploadDirectory.exists()) {
                LOGGER.log(Level.INFO, "The configuration directory {0} does not exist", UPLOAD_DIRECTORY);
            } else if (!constellationUploadDirectory.isDirectory()) {
                LOGGER.log(Level.INFO, "The configuration path {0} is not a directory", UPLOAD_DIRECTORY);
            }
        } else {
        	constellationUploadDirectory = new File(constellationDataDirectory, "upload");
            if (!constellationUploadDirectory.exists()) {
            	constellationUploadDirectory.mkdir();
            }
        }
        sessionUploadDirectory = new File(constellationUploadDirectory, sessionId);
        if (!sessionUploadDirectory.exists()) {
        	sessionUploadDirectory.mkdir();
        }
        
        return sessionUploadDirectory;
	}
    
    /**
     * Give Metadata directory {@link java.io.File} defined on constellation.properties or
     * by default on .constellation-data/metadata from user home directory
     *
     * @return metadata directory as {@link java.io.File}
     */
    public static File getMetadataDirectory() {
        final File constellationMetadataFolder;

        if (METADATA_DIRECTORY != null && !METADATA_DIRECTORY.isEmpty()) {
            constellationMetadataFolder = new File(METADATA_DIRECTORY);
            if (!constellationMetadataFolder.exists()) {
                LOGGER.log(Level.INFO, "The configuration directory {0} does not exist", METADATA_DIRECTORY);
            } else if (!constellationMetadataFolder.isDirectory()) {
                LOGGER.log(Level.INFO, "The configuration path {0} is not a directory", METADATA_DIRECTORY);
            }
        } else {
            constellationMetadataFolder = new File(System.getProperty("user.home") + "/.constellation-data", "metadata");
            if (constellationMetadataFolder.mkdir()){
                LOGGER.log(Level.INFO, "metadata folder created");
            }
        }

        return constellationMetadataFolder;
    }

    /**
     * Give styles directory {@link java.io.File} defined on constellation.properties or
     * by default on .constellation-data/metadata from user home directory
     *
     * @return styles directory as {@link java.io.File}
     */
    public static File getStyleDirectory() {
        final File constellationStyleFolder;

        if (STYLE_DIRECTORY != null && !STYLE_DIRECTORY.isEmpty()) {
            constellationStyleFolder = new File(STYLE_DIRECTORY);
            if (!constellationStyleFolder.exists()) {
                LOGGER.log(Level.INFO, "The configuration directory {0} does not exist", STYLE_DIRECTORY);
            } else if (!constellationStyleFolder.isDirectory()) {
                LOGGER.log(Level.INFO, "The configuration path {0} is not a directory", STYLE_DIRECTORY);
            }
        } else {
            constellationStyleFolder = new File(getDataDirectory(), "style");
            if(constellationStyleFolder.mkdirs()){
                LOGGER.log(Level.INFO, "style folder created");
            }
        }

        return constellationStyleFolder;
    }

    /**
     * Give temporary styles directory.
     *
     * @return temporary styles directory as {@link java.io.File}
     */
    public static File getStyleTempDirectory() {
        final File constellationStyleFolder = new File(getDataDirectory(), "style_temp");
        if(constellationStyleFolder.mkdirs()){
            LOGGER.log(Level.INFO, "style folder created");
        }

        return constellationStyleFolder;
    }

    /**
     * Return the configuration directory.
     * <p/>
     * priority is :
     * 1) packaged war file
     * 2) resource packaged config
     * 3) user defined directory
     * 4) .constellation in home directory
     */
    public static File getConfigDirectory() {
        File constellationDirectory;

        /*
         * 1) WAR packaged config located in WEB-INF
         */
        final File webInfDirectory = getWebInfDiretory();

        constellationDirectory = new File(webInfDirectory, "constellation");
        if (constellationDirectory.isDirectory()) {
            return constellationDirectory;
        }

        /*
         * 2) resource packaged config
         */
        constellationDirectory = FileUtilities.getDirectoryFromResource("constellation");
        if (constellationDirectory != null && constellationDirectory.isDirectory()) {
            return constellationDirectory;
        }

        /*
         * 3) user defined config
         */
        if (USER_DIRECTORY != null && !USER_DIRECTORY.isEmpty()) {
            constellationDirectory = new File(USER_DIRECTORY);
            if (!constellationDirectory.exists()) {
                LOGGER.log(Level.INFO, "The configuration directory {0} does not exist", USER_DIRECTORY);
            } else if (!constellationDirectory.isDirectory()) {
                LOGGER.log(Level.INFO, "The configuration path {0} is not a directory", USER_DIRECTORY);
            }
            return constellationDirectory;
        }

        /*
         * 4) .constellation in home directory
         */
        constellationDirectory = getConstellationDirectory();

        return constellationDirectory;
    }

    public static void setConfigDirectory(final File directory) {
        USER_DIRECTORY = null;
        if (directory != null && directory.isDirectory()) {
            if (!directory.getPath().equals(getConstellationDirectory().getPath())) {
                USER_DIRECTORY = directory.getPath();
            }
        }
        //store the configuration properties file
        final File webInfDirectory = getWebInfDiretory();
        final File propertiesFile = new File(webInfDirectory, "constellation.properties");
        try {
            if (!propertiesFile.exists()) {
                propertiesFile.createNewFile();
            }
            final Properties prop = new Properties();
            final String pathValue;
            if (USER_DIRECTORY == null) {
                pathValue = "";
            } else {
                pathValue = USER_DIRECTORY;
            }
            prop.put("configuration_directory", pathValue);
            FileUtilities.storeProperties(prop, propertiesFile);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "IOException while writing the constellation properties file", ex);
        }

    }

    public static void setDataDirectory(final File directory) {
        DATA_DIRECTORY = null;
        if (directory != null && directory.isDirectory()) {
            if (!directory.getPath().equals(getConstellationDirectory().getPath())) {
                DATA_DIRECTORY = directory.getPath();
            }
        }
        //store the configuration properties file
        final File webInfDirectory = getWebInfDiretory();
        final File propertiesFile = new File(webInfDirectory, "constellation.properties");
        try {
            if (!propertiesFile.exists()) {
                propertiesFile.createNewFile();
            }
            final Properties prop = new Properties();
            final String pathValue;
            if (DATA_DIRECTORY == null) {
                pathValue = "";
            } else {
                pathValue = DATA_DIRECTORY;
            }
            prop.put("data_directory", pathValue);
            FileUtilities.storeProperties(prop, propertiesFile);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "IOException while writing the constellation properties file", ex);
        }

    }

    /**
     * Return the directory for the configuration files of all instance of serviceType?
     *
     * @param serviceType the service type (CSW, WMS, WCS, ...)
     * @return
     */
    public static File getServiceDirectory(final String serviceType) {
        return new File(ConfigDirectory.getConfigDirectory(), serviceType);
    }

    public static List<File> getInstanceDirectories(final String serviceType) {
        return Arrays.asList(ConfigDirectory.getServiceDirectory(serviceType).listFiles());
    }

    public static File getInstanceDirectory(final String serviceType, final String instance) {
        return new File(ConfigDirectory.getServiceDirectory(serviceType), instance);
    }

    /**
     * Return the ".constellation" configuration directory in the user home.
     *
     * @return
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
     * Return a folder named 'provider' at the root in the configuration directory.
     */
    public static File getProviderConfigDirectory() {
        final File constellationDirectory = getConfigDirectory();

        if (!constellationDirectory.exists()) {
            constellationDirectory.mkdirs();
        }

        final File providerDirectory = new File(constellationDirectory, "provider");
        if (!providerDirectory.exists()) {
            providerDirectory.mkdirs();
        }

        return providerDirectory;
    }

    /**
     * Return a folder named 'admin' at the root in the configuration directory.
     */
    public static File getAdminConfigDirectory() {
        final File constellationDirectory = getConfigDirectory();

        if (!constellationDirectory.exists()) {
            constellationDirectory.mkdirs();
        }

        final File adminDirectory = new File(constellationDirectory, "admin");
        if (!adminDirectory.exists()) {
            adminDirectory.mkdirs();
        }

        return adminDirectory;
    }

    /**
     * Return a file at the root in the provider directory.
     */
    public static File getProviderConfigFile(final String fileName) {
        final File providerDirectory = getProviderConfigDirectory();
        return new File(providerDirectory, fileName);
    }

    /**
     * Get the value for a property defined in the JNDI context chosen.
     *
     * @param propGroup If you use Glassfish, you have to specify the name of the resource that
     *                  owns the property you wish to get. Otherwise you should specify {@code null}
     * @param propName  The name of the property to get.
     * @return The property value defines in the context, or {@code null} if no property of this name
     * is defined in the resource given in parameter.
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
     * @param key     The key to search in the context.
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

    public static File setupTestEnvironement(final String directoryName) {
        final File configDir = new File(directoryName);
        if (configDir.exists()) {
            FileUtilities.deleteDirectory(configDir);
        }
        configDir.mkdir();
        setConfigDirectory(configDir);

        return configDir;
    }

    public static void shutdownTestEnvironement(final String directoryName) {
        FileUtilities.deleteDirectory(new File(directoryName));
        setConfigDirectory(null);
    }
}
