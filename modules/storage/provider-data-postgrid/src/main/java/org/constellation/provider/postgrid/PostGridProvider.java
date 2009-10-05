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

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.ConfigurationKey;
import org.constellation.catalog.Database;
import org.constellation.catalog.NoSuchTableException;
import org.constellation.coverage.catalog.GridCoverageTable;
import org.constellation.coverage.catalog.Layer;
import org.constellation.coverage.catalog.LayerTable;
import org.constellation.map.PostGridReader;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProvider;
import org.constellation.provider.configuration.ProviderLayer;
import org.constellation.provider.configuration.ProviderSource;

import org.geotoolkit.map.ElevationModel;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.sql.WrappedDataSource;
import org.geotoolkit.util.logging.Logging;
import org.postgresql.ds.PGConnectionPoolDataSource;


/**
 *
 * @version $Id$
 * @author Johann Sorel (Geomatys)
 */
public class PostGridProvider implements LayerProvider{

    public static final String KEY_DATABASE = "Database";
    public static final String KEY_USER = "User";
    public static final String KEY_PASSWORD = "Password";
    public static final String KEY_READONLY = "ReadOnly";
    public static final String KEY_DRIVER = "Driver";
    public static final String KEY_ROOT_DIRECTORY = "RootDirectory";


    private static final Logger LOGGER = Logging.getLogger(PostGridProvider.class);

    private final Map<String,Layer> index = new HashMap<String,Layer>();
    private final Map<String,List<String>> favorites = new  HashMap<String, List<String>>();
    private final Map<String,PostGridLayerDetails> cache = new HashMap<String, PostGridLayerDetails>();

    private final ProviderSource source;
    private final Database database;
    private final GridCoverageTable coverageTable;

    protected PostGridProvider(ProviderSource source) throws IOException, SQLException {
        this.source = source;

        final Properties properties = new Properties();
        for(String key : source.parameters.keySet()){
            properties.put(key, source.parameters.get(key));
        }

        String server ="localhost";
        int port = 5432;
        String dbName = "";

        //parse string if old format : exemple jdbc:postgresql://server/database
        String oldDataBase = properties.getProperty(ConfigurationKey.DATABASE.getKey());
        if(oldDataBase.contains("/")){
            int index = oldDataBase.lastIndexOf("/");
            dbName = oldDataBase.substring(index+1, oldDataBase.length());
            oldDataBase = oldDataBase.substring(0, index);
            index = oldDataBase.lastIndexOf("/");
            server = oldDataBase.substring(index+1, oldDataBase.length());

            if(server.contains(":")){
                final String[] split = server.split(":");
                server = split[0];
                port = Integer.valueOf(split[1]);

            }else{
                port = 5432;
            }


        }else{
            server = (properties.getProperty(ConfigurationKey.SERVER.getKey()));
            String portTxt = (properties.getProperty(ConfigurationKey.PORT.getKey()));
            if(server == null || server.trim().isEmpty()){
                server = "localhost";
            }
            try{
                port = Integer.parseInt(portTxt);
            }catch(Exception nf){
                //catch numberformat and nullpointer
                LOGGER.log(Level.WARNING,"Port value for postgrid is not valid : "+ portTxt);
                port = 5432;
            }
            dbName = properties.getProperty(ConfigurationKey.DATABASE.getKey());
        }

        
        final PGConnectionPoolDataSource pool = new PGConnectionPoolDataSource();
        pool.setServerName(server);
        pool.setPortNumber(port);
        pool.setDatabaseName(dbName);
        pool.setUser(properties.getProperty(ConfigurationKey.USER.getKey()));
        pool.setPassword(properties.getProperty(ConfigurationKey.PASSWORD.getKey()));
        pool.setLoginTimeout(5);

        final DataSource dataSource = new WrappedDataSource(pool);
        database = new Database(dataSource,properties);

        GridCoverageTable gridTable = null;
        try {
            gridTable = database.getTable(GridCoverageTable.class);
        } catch (NoSuchTableException ex) {
            LOGGER.log(Level.SEVERE, "No GridCoverageTable", ex);
        }
        coverageTable = gridTable;
        
        visit();
    }

    protected ProviderSource getSource(){
        return source;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class<String> getKeyClass() {
        return String.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class<LayerDetails> getValueClass() {
        return LayerDetails.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Set<String> getKeys() {
        return index.keySet();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean contains(String key) {
        return index.containsKey(key);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public LayerDetails get(final String key) {

        PostGridLayerDetails detail = cache.get(key);

        if(detail == null){
            //coverage reader is not in the cache, try to load it
            final Layer layer = index.get(key);

            if(layer == null) return null;

            //create a mutable copy
            final GridCoverageTable gridTable = new GridCoverageTable(coverageTable);
            gridTable.setLayer(layer);
            final PostGridReader reader = new PostGridReader(gridTable);

            /*-reader is null, this layer is not registered in this provider.
            if(reader == null) return null;*/

            final ProviderLayer layerDef = source.getLayer(key);
            final List<String> styles = new ArrayList<String>();
            final String elevationModel;
            if(layerDef != null){
                styles.addAll(layerDef.styles);
                elevationModel = layerDef.elevationModel;
            }else{
                elevationModel = null;
            }

            detail = new PostGridLayerDetails(reader, styles, elevationModel);
            cache.put(key, detail);
        }

        return detail;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void reload() {
        synchronized(this){
            favorites.clear();
            index.clear();
            cache.clear();
            visit();
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void dispose() {
        synchronized(this){
            favorites.clear();
            index.clear();
            cache.clear();
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

    }

    @Override
    public ElevationModel getElevationModel(String name) {

        final ProviderLayer layer = source.getLayer(name);
        if(layer != null && layer.isElevationModel){
            final PostGridLayerDetails pgld = (PostGridLayerDetails) get(name);
            if(pgld != null){
                return MapBuilder.createElevationModel(pgld.getReader());
            }
        }
        
        return null;
    }

}
