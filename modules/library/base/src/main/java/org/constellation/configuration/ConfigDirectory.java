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

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;

import org.apache.sis.util.logging.Logging;

/**
 * Temporary copy of static methods from the WebService class (in module
 * web-base), in order to retrieve the configuration directory of Constellation.
 * <p/>
 * TODO: this implementation should probably been handled by the server
 * registry, so move it there.
 *
 * @author Cédric Briançon (Geomatys)
 * @author Guilhem Legal (Geomatys)
 * @version $Id$
 */
public final class ConfigDirectory {

    private static class Config {


        public Config(Builder builder) {
            this.home = builder.home;
            this.data = builder.data;
            this.dataIntegrated = builder.dataIntegrated;
            this.dataIndex = builder.dataIndex;
            this.dataUserUploads = builder.dataUserUploads;
            this.dataMetadata = builder.dataMetadata;
            this.dataStyle = builder.dataStyle;
            this.dataAdmin = builder.dataAdmin;
            this.dataServices = builder.dataServices;
            this.testing = builder.testing;
            
        }

        private static class Builder {
            private Path home;
            private Path data;
            private Path dataIntegrated;
            private Path dataIndex;
            private Path dataMetadata;
            private Path dataStyle;
            private Path dataAdmin;
            public Path dataServices;

            private String homeLocation;
            private String dataLocation;
            private Path dataUserUploads;
            private boolean testing;

            public Builder() {
                this.homeLocation = System.getProperty("cstl.home", System.getProperty("user.home") + "/.constellation");
                this.dataLocation = System.getProperty("cstl.data", homeLocation + "/data");
            }

            Config build() {
                this.home = initFolder(homeLocation);
                this.data = initFolder(dataLocation);
                this.dataIntegrated = initDataSubFolder("integrated");
                this.dataIndex = initDataSubFolder("index");
                this.dataUserUploads = initDataSubFolder("user", "uploads");
                this.dataMetadata = initDataSubFolder("metadata");
                this.dataStyle = initDataSubFolder("styles");
                this.dataAdmin = initDataSubFolder("admin");
                this.dataServices = initDataSubFolder("services");
                return new Config(this);
            }

            private Path initFolder(String absLocation) {
                return ConfigDirectory.initFolder(Paths.get(absLocation));
            }

            private Path initDataSubFolder(String sub, String... subs) {
                Path paths = Paths.get(sub, subs);
                return ConfigDirectory.initFolder(data.resolve(paths));
            }

           

            public Builder forTest(String filename) {
                this.homeLocation = filename;
                this.dataLocation = filename + "/data"; 
                this.testing = true;
                return this;
            }
        }

        final Path home;
        final Path data;
        final Path dataIntegrated;
        final Path dataIndex;
        final Path dataUserUploads;
        final Path dataMetadata;
        final Path dataStyle;
        final Path dataAdmin;
        final boolean testing;
        final Path dataServices;
    }

    /**
     * The default debugging logger.
     */
    private static final Logger LOGGER = Logging.getLogger("org.constellation.provider.configuration");

    private static Config config = new Config.Builder().build();

    /**
     * Specifies if the process is running on a Glassfish application server.
     */
    private static Boolean runningOnGlassfish = null;

    private ConfigDirectory() {
    }

    
     static Path initFolder(Path path) {
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path);
                LOGGER.info(path.toString() + " created.");
            } catch (IOException e) {
                throw new ConfigurationRuntimeException("Could not create: " + path.toString(), e);
            }
        }
        return path;
    }
    
    public static File getUserHomeDirectory() {
        final String home = System.getProperty("user.home");
        return new File(home);
    }

    /**
     * Give a data directory {@link java.io.File} defined on
     * constellation.properties or by default on .constellation-data from user
     * home directory
     *
     * @return data directory as {@link java.io.File}
     */
    public static File getDataDirectory() {
        return config.data.toFile();
    }

    /**
     * Give a integrated data directory {@link java.io.File} defined on
     * constellation.properties or by default on .constellation-data/integrated/
     * from user home directory
     *
     * @return providers directory as {@link java.io.File}
     */
    public static File getDataIntegratedDirectory() {
        return config.dataIntegrated.toFile();
    }

    /**
     * Give a index data directory {@link java.io.File} defined on
     * constellation.properties or by default on .constellation-data/index/ from
     * user home directory
     *
     * @return providers directory as {@link java.io.File}
     */
    public static File getDataIndexDirectory() {
        return config.dataIndex.toFile();
    }

    /**
     * Give a integrated data directory {@link java.io.File} defined on
     * constellation.properties or by default on .constellation-data/integrated/
     * from user home directory for given provider.
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
     * remove upload directory for the sessionId {@link java.io.File} by default
     * on .constellation-data/upload/<sessionId> from user home directory
     * 
     * @param sessionId
     */
    public static void removeUploadDirectory(String sessionId) {

        Path sessionFolder = resolveUserUploads(sessionId);

        deleteDir(sessionFolder);
    }

    private static void deleteDir(Path sessionFolder) {
        try {
            Files.walkFileTree(sessionFolder, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }

            });
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }

    private static Path resolveUserUploads(String sessionId) {
        return config.dataUserUploads.resolve(sessionId);
    }
    
    private static Path resolveInstanceDirectory(String type, String id) {
        Path typeService = resolveInstanceServiceDirectoryByType(type);
        return initFolder(typeService.resolve(id));
    }


    private static Path resolveInstanceServiceDirectoryByType(String type) {
        Path typeService = config.dataServices.resolve(type);
        ConfigDirectory.initFolder(typeService);
        return typeService;
    }

    /**
     * Give a upload directory {@link java.io.File} defined on
     * constellation.properties or by default on
     * .constellation-data/upload/<sessionId> from user home directory
     * 
     * @param sessionId
     *
     * @return providers directory as {@link java.io.File}
     */
    public static File getUploadDirectory(String sessionId) {
        return resolveUserUploads(sessionId).toFile();
    }

    /**
     * Give Metadata directory {@link java.io.File} defined on
     * constellation.properties or by default on .constellation-data/metadata
     * from user home directory
     *
     * @return metadata directory as {@link java.io.File}
     */
    public static File getMetadataDirectory() {
        return config.dataMetadata.toFile();
    }

    /**
     * Give styles directory {@link java.io.File} defined on
     * constellation.properties or by default on .constellation-data/metadata
     * from user home directory
     *
     * @return styles directory as {@link java.io.File}
     */
    public static File getStyleDirectory() {
        return config.dataStyle.toFile();
    }

    /**
     * Give temporary styles directory.
     *
     * @return temporary styles directory as {@link java.io.File}
     */
    public static File getStyleTempDirectory() {
        final File constellationStyleFolder = new File(getDataDirectory(), "style_temp");
        if (constellationStyleFolder.mkdirs()) {
            LOGGER.log(Level.INFO, "style folder created");
        }

        return constellationStyleFolder;
    }

    /**
     * Return a folder named 'admin' at the root in the configuration directory.
     */
    public static File getAdminConfigDirectory() {
        return config.dataAdmin.toFile();
    }

    /**
     * Get the value for a property defined in the JNDI context chosen.
     *
     * @param propGroup
     *            If you use Glassfish, you have to specify the name of the
     *            resource that owns the property you wish to get. Otherwise you
     *            should specify {@code null}
     * @param propName
     *            The name of the property to get.
     * @return The property value defines in the context, or {@code null} if no
     *         property of this name is defined in the resource given in
     *         parameter.
     * @throws NamingException
     *             if an error occurs while initializing the context, or if an
     *             empty value for propGroup has been passed while using a
     *             Glassfish application server.
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
     * Returns the context value for the key specified, or {@code null} if not
     * found in this context.
     *
     * @param key
     *            The key to search in the context.
     * @param context
     *            The context which to consider.
     */
    private static Object getContextProperty(final String key, final javax.naming.Context context) {
        Object value = null;
        try {
            value = context.lookup(key);
        } catch (NamingException n) {
            // Do nothing, the key is not found in the context and the value is
            // still null.
        }

        return value;
    }

    public static File getConfigDirectory() {
        return config.home.toFile();
    }

    public static File setupTestEnvironement(String filename) {
        config = new Config.Builder().forTest("target/" + filename).build();
        return config.home.toFile();
        
    }

    public static void shutdownTestEnvironement(String string) {
        if(config.testing) {
            deleteDir(config.home);
        }
    }

    public static void init() {
        config = new Config.Builder().build();
    }

    public static String getServiceURL() {
        return System.getProperty("cstl.service.url");
    }

    public static File getInstanceDirectory(String type, String id) {
        return resolveInstanceDirectory(type, id).toFile();
    }


    public static Collection<? extends File> getInstanceDirectories(String typeService) {
        Path instancesDirectory = resolveInstanceServiceDirectoryByType(typeService);
        return Arrays.asList(instancesDirectory.toFile().listFiles());
    }

    

}
