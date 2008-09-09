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

import org.constellation.catalog.CatalogException;
import org.constellation.catalog.ConfigurationKey;
import org.constellation.catalog.Database;
import org.constellation.catalog.NoSuchTableException;
import org.constellation.coverage.catalog.Layer;
import org.constellation.coverage.catalog.LayerTable;
import org.constellation.provider.LayerDataProvider;

import org.geotools.map.MapLayer;
import org.geotools.style.MutableStyle;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class PostGridNamedLayerDP implements LayerDataProvider<String,MapLayer>{

    private static PostGridNamedLayerDP instance = null;
    
    protected static final Logger logger = Logger.getLogger("net.seagis.provider.postgrid");
    
    protected static Database database;
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
             }catch (SQLException io) {
                /* Could not connect to postgrid.
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
            logger.warning("Connecting to the database using config.xml file !");
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
            try{
                tempDB = (configFile != null) ? new Database(configFile) : new Database();
            }catch(IOException io){
                logger.log(Level.SEVERE,"SQL Error",io);
            }
        } catch (SQLException n) {
            logger.log(Level.SEVERE,"SQL Error",n);
        }
        
        PostGridNamedLayerDP.database = tempDB;
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

    //--------------------------------------------------------------------------
    
    private final Map<String,Layer> index = new HashMap<String,Layer>();
    
    private PostGridNamedLayerDP(){
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
    public Class<MapLayer> getValueClass() {
        return MapLayer.class;
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
    public MapLayer get(String key) {
        MapLayer layer = null;
        
        Layer gridLayer = index.get(key);
        if(gridLayer != null){
            layer = new PostGridMapLayer(database, gridLayer);
        }
        
        return layer;
    }

    /**
     * {@inheritDoc }
     */
    public void reload() {
        synchronized(this){
            index.clear();
            visit();
        }
    }

    /**
     * {@inheritDoc }
     */
    public void dispose() {
        synchronized(this){
            index.clear();
        }
    }
    
    private void visit() {
        LayerTable layers = null;
        
        try {
            layers = database.getTable(LayerTable.class);
        } catch (NoSuchTableException ex) {
            Logger.getLogger(PostGridNamedLayerDP.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(layers != null){
            Set<Layer> set = null;
            try {
                set = layers.getEntries();
            } catch (CatalogException ex) {
                Logger.getLogger(PostGridNamedLayerDP.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(PostGridNamedLayerDP.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if(set != null && !set.isEmpty()){
                for(Layer layer : set){
                    index.put(layer.getName(),layer);
                }
            }
            
        }else{
            logger.log(Level.SEVERE,"Layer table is null");
        }
        
    }
        
    private MapLayer createMapLayer(Layer gridLayer){
        MapLayer layer = null;
        
        return layer;
    }
    
    public static PostGridNamedLayerDP getDefault(){
        if(instance == null){
            instance = new PostGridNamedLayerDP();
        }
        return instance;
    }

    public MapLayer get(String layerName, MutableStyle style) {
        return get(layerName);
    }

    public List<String> getFavoriteStyles(String layerName) {
        return Collections.emptyList();
    }
    
    
}