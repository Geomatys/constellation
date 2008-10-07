/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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
package org.constellation.provider.postgrid;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;

import org.constellation.catalog.CatalogException;
import org.constellation.catalog.ConfigurationKey;
import org.constellation.catalog.Database;
import org.constellation.catalog.NoSuchTableException;
import org.constellation.coverage.catalog.Layer;
import org.constellation.coverage.catalog.LayerTable;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerDataProvider;
import org.constellation.provider.configuration.LayerLinkReader;
import org.constellation.ws.rs.WebService;

import org.xml.sax.SAXException;


/**
 *
 * @version $Id$
 * @author Johann Sorel (Geomatys)
 */
public class PostGridNamedLayerDP implements LayerDataProvider{

    private static final String KEY_POSTGRID_STYLES = "postgrid_style";
    protected static final Logger LOGGER = Logger.getLogger("org.constellation.provider.postgrid");
    private static PostGridNamedLayerDP instance = null;

    private final Map<String,Layer> index = new HashMap<String,Layer>();
    private final Map<String,List<String>> favorites = new  HashMap<String, List<String>>();

    protected static final Database database;

    static {
        Database tempDB = null;
        try {
            final Connection connection;
            final Properties properties = new Properties();
            String permission = null, readOnly = null, rootDir = null;
            final InitialContext ctx = new InitialContext();
            /* domain.name is a property only present when using the glassfish application
             * server.
             */
            if (System.getProperty("domain.name") != null) {
                final DataSource ds = (DataSource) ctx.lookup("Coverages");
                if (ds == null) {
                    throw new NamingException("DataSource \"Coverages\" is not defined.");
                }
                connection = ds.getConnection();
                final Reference props = (Reference) getContextProperty("Coverages Properties", ctx);
                if (props != null) {
                    final RefAddr permissionAddr = (RefAddr) props.get("Permission");
                    if (permissionAddr != null) {
                        permission = (String) permissionAddr.getContent();
                    }
                    final RefAddr rootDirAddr = (RefAddr) props.get("RootDirectory");
                    if (rootDirAddr != null) {
                        rootDir = (String) rootDirAddr.getContent();
                    }
                    final RefAddr readOnlyAddr = (RefAddr) props.get("ReadOnly");
                    if (readOnlyAddr != null) {
                        readOnly = (String) readOnlyAddr.getContent();
                    }
                } else {
                    throw new NamingException("Coverages Properties is not defined.");
                }
            } else {
                // Here we are not in glassfish, probably in a Tomcat application server.
                final Context envContext = (Context) ctx.lookup("java:/comp/env");
                final DataSource ds = (DataSource) envContext.lookup("Coverages");
                if (ds == null) {
                    throw new NamingException("DataSource \"Coverages\" is not defined.");
                }
                connection = ds.getConnection();
                permission = (String) getContextProperty("Permission", envContext);
                readOnly = (String) getContextProperty("ReadOnly", envContext);
                rootDir = (String) getContextProperty("RootDirectory", envContext);
            }
            // Put all properties found in the JNDI reference into the Properties HashMap
            if (permission != null) {
                properties.setProperty(ConfigurationKey.PERMISSION.getKey(), permission);
            }
            if (readOnly != null) {
                properties.setProperty(ConfigurationKey.READONLY.getKey(), readOnly);
            }
            if (rootDir != null) {
                properties.setProperty(ConfigurationKey.ROOT_DIRECTORY.getKey(), rootDir);
            }
            try {
                tempDB = new Database(connection, properties);
            } catch (IOException io) {
                /* This error should never appear, because the IOException on the Database
                 * constructor can only overcome if we use the constructor
                 * Database(DataSource, Properties, String), and here the string for the
                 * configuration file is null, so no reading method on a file will be used.
                 * Anyways if this error occurs, an AssertionError is then thrown.
                 */
                throw new AssertionError(io);
             }
        } catch (NamingException n) {
            /* If a NamingException occurs, it is because the JNDI connection is not
             * correctly defined, and some information are lacking.
             * In this case we try to use the old system of configuration file.
             */

            /* Ifremer's server does not contain any .sicade directory, so the
             * configuration file is put under the WEB-INF directory of constellation.
             * todo: get the webservice name (here ifremerWS) from the servlet context.
             */
            LOGGER.warning("Connecting to the database using config.xml file !");
            File configFile = null;
            File dirCatalina = null;
            final String catalinaPath = System.getenv().get("CATALINA_HOME");
            if (catalinaPath != null) {
                dirCatalina = new File(catalinaPath);
            }
            if (dirCatalina != null && dirCatalina.exists()) {
                configFile = new File(dirCatalina, "webapps/ifremerWS/WEB-INF/config.xml");
                if (!configFile.exists()) {
                    configFile = null;
                }
            }
            try {
                tempDB = (configFile != null) ? new Database(configFile) : new Database();
            } catch(IOException io) {
                LOGGER.log(Level.SEVERE, "Unable to retrieve information from the config file", io);
            }
        } catch (SQLException sql) {
            LOGGER.log(Level.SEVERE,"Unable to connect to the database", sql);
        }

        database = tempDB;
    }

    /**
     * Returns the context value for the key specified, or {@code null} if not found
     * in this context.
     *
     * @param key The key to search in the context.
     * @param context The context which to consider.
     */
    private static Object getContextProperty(final String key, final Context context) {
        Object value = null;
        try {
            value = context.lookup(key);
        } catch (NamingException n) {
            // Do nothing, the key is not found in the context and the value is still null.
        } finally {
            return value;
        }
    }

    private PostGridNamedLayerDP() {
        visit();
    }

    /**
     * {@inheritDoc }
     */
    public Class<String> getKeyClass() {
        return String.class;
    }

    /**
     * {@inheritDoc }
     */
    public Class<LayerDetails> getValueClass() {
        return LayerDetails.class;
    }

    /**
     * {@inheritDoc }
     */
    public Set<String> getKeys() {
        return index.keySet();
    }

    /**
     * {@inheritDoc }
     */
    public boolean contains(String key) {
        return index.containsKey(key);
    }

    /**
     * {@inheritDoc }
     */
    public LayerDetails get(String key) {
        final Layer layer = index.get(key);
        return (layer != null) ?
            new PostGridLayerDetails(database, layer, getFavoriteStyles(key)) : null;
    }

    /**
     * {@inheritDoc }
     */
    public void reload() {
        synchronized(this){
            favorites.clear();
            index.clear();
            visit();
        }
    }

    /**
     * {@inheritDoc }
     */
    public void dispose() {
        synchronized(this){
            favorites.clear();
            index.clear();
        }
    }

    private void visit() {
        LayerTable layers = null;

        try {
            layers = database.getTable(LayerTable.class);
        } catch (NoSuchTableException ex) {
            LOGGER.log(Level.SEVERE, "Unknown specified type in the database", ex);
        }

        if(layers != null) {
            Set<Layer> set = null;
            try {
                set = layers.getEntries();
            } catch (CatalogException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }

            if(set != null && !set.isEmpty()) {
                for(Layer layer : set) {
                    index.put(layer.getName(),layer);
                }
            }

        } else {
            LOGGER.log(Level.SEVERE, "Layer table is null");
        }

        extractLinks();
    }

    private void extractLinks() {

        String styleLinks = "";
        try {
            styleLinks = WebService.getPropertyValue(JNDI_GROUP, KEY_POSTGRID_STYLES);
        } catch (NamingException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        if (styleLinks == null) {
            return;
        }
        final File file = new File(styleLinks);

        if(file.exists()){
            Map<String,List<String>> links = null;
            try {
                links = LayerLinkReader.read(file);
            } catch (ParserConfigurationException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } catch (SAXException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }

            if(links != null){
                favorites.putAll(links);
            }
        }
    }

    public List<String> getFavoriteStyles(final String layerName) {
        List<String> favs = favorites.get(layerName);
        if(favs == null){
            favs = Collections.emptyList();
        }
        return favs;
    }

    public static PostGridNamedLayerDP getDefault(){
        if(instance == null){
            instance = new PostGridNamedLayerDP();
        }
        return instance;
    }

}
