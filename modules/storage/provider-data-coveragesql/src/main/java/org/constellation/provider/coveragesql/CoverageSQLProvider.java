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
package org.constellation.provider.coveragesql;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

import org.geotoolkit.coverage.sql.CoverageDatabase;
import org.constellation.provider.AbstractLayerProvider;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.configuration.ProviderSource;

import org.geotoolkit.map.ElevationModel;
import org.geotoolkit.sql.WrappedDataSource;
import org.geotoolkit.util.logging.Logging;

import org.opengis.feature.type.Name;

import org.postgresql.ds.PGConnectionPoolDataSource;


/**
 *
 * @version $Id$
 * @author Johann Sorel (Geomatys)
 */
public class CoverageSQLProvider extends AbstractLayerProvider{

   private static final Logger LOGGER = Logging.getLogger(CoverageSQLProvider.class);

    public static final String KEY_SERVER = "server";
    public static final String KEY_PORT = "port";
    public static final String KEY_DATABASE = "database";
    public static final String KEY_SCHEMA = "schema";
    public static final String KEY_USER = "user";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_READONLY = "readOnly";
    public static final String KEY_DRIVER = "driver";
    public static final String KEY_ROOT_DIRECTORY = "rootDirectory";

    private final ProviderSource source;
    private final CoverageDatabase database;

    protected CoverageSQLProvider(ProviderSource source) throws IOException, SQLException {
        this.source = source;

        final Properties properties = new Properties();
        for(String key : source.parameters.keySet()){
            properties.put(key, source.parameters.get(key));
        }

        String server ="localhost";
        int port = 5432;
        String dbName = "";

        //parse string if old format : exemple jdbc:postgresql://server/database
        String oldDataBase = properties.getProperty(KEY_DATABASE);
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
            server = properties.getProperty(KEY_SERVER);
            String portTxt = properties.getProperty(KEY_PORT);
            if(server == null || server.trim().isEmpty()){
                server = "localhost";
            }
            try{
                port = Integer.parseInt(portTxt);
            }catch(Exception nf){
                //catch numberformat and nullpointer
                LOGGER.log(Level.WARNING,"Port value for coverage-sql is not valid : "+ portTxt);
                port = 5432;
            }
            dbName = properties.getProperty(KEY_DATABASE);
        }


        final PGConnectionPoolDataSource pool = new PGConnectionPoolDataSource();
        pool.setServerName(server);
        pool.setPortNumber(port);
        pool.setDatabaseName(dbName);
        pool.setUser(properties.getProperty(KEY_USER));
        pool.setPassword(properties.getProperty(KEY_PASSWORD));
        pool.setLoginTimeout(5);

        final DataSource dataSource = new WrappedDataSource(pool);

        database = new CoverageDatabase(dataSource, properties);
        
        visit();
    }

    protected ProviderSource getSource(){
        return source;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Set<Name> getKeys() {
        return Collections.emptySet();
    }

    @Override
    public Set<Name> getKeys(String service) {
        return Collections.emptySet();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean contains(Name key) {
        return false;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public LayerDetails get(final Name key) {
        return null;
    }

    @Override
    public LayerDetails get(Name key, String service) {
       if (source.services.contains(service) || source.services.isEmpty()) {
           return get(key);
       }
       return null;
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public void reload() {
        synchronized(this){
//            favorites.clear();
//            index.clear();
//            cache.clear();
            visit();
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void dispose() {
        synchronized(this){
//            favorites.clear();
//            index.clear();
//            cache.clear();
        }
    }

    private void visit() {
//        LayerTable layers = null;
//
//        try {
//            layers = database.getTable(LayerTable.class);
//        } catch (NoSuchTableException ex) {
//            LOGGER.log(Level.SEVERE, "Unknown specified type in the database", ex);
//        }
//
//        if(layers != null) {
//            Set<Layer> set = null;
//            try {
//                set = layers.getEntries();
//            } catch (CatalogException ex) {
//                LOGGER.log(Level.SEVERE, null, ex);
//            } catch (SQLException ex) {
//                LOGGER.log(Level.SEVERE, null, ex);
//            }
//
//            if(set != null && !set.isEmpty()) {
//                for(Layer layer : set) {
//                    index.put(new DefaultName(layer.getName()),layer);
//                }
//            }
//
//        } else {
//            LOGGER.log(Level.SEVERE, "Layer table is null");
//        }

    }

    @Override
    public ElevationModel getElevationModel(String name) {

//        final ProviderLayer layer = source.getLayer(name);
//        if(layer != null && layer.isElevationModel){
//            final CoverageSQLLayerDetails pgld = (CoverageSQLLayerDetails) getByIdentifier(name);
//            if(pgld != null){
//                return MapBuilder.createElevationModel(pgld.getReader());
//            }
//        }
        
        return null;
    }

}
