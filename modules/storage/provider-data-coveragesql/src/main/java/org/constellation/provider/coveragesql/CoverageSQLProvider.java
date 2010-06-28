/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.geotoolkit.coverage.io.CoverageStoreException;

import org.geotoolkit.coverage.sql.CoverageDatabase;
import org.constellation.provider.AbstractLayerProvider;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.configuration.ProviderLayer;
import org.constellation.provider.configuration.ProviderSource;
import org.geotoolkit.coverage.sql.LayerCoverageReader;
import org.geotoolkit.feature.DefaultName;

import org.geotoolkit.map.ElevationModel;
import org.geotoolkit.jdbc.WrappedDataSource;
import org.geotoolkit.util.logging.Logging;

import org.opengis.feature.type.Name;

import org.postgresql.ds.PGConnectionPoolDataSource;


/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class CoverageSQLProvider extends AbstractLayerProvider{
    /**
     * Default logger for this provider.
     */
    private static final Logger LOGGER = Logging.getLogger(CoverageSQLProvider.class);

    /**
     * Keys to use in configuration file.
     */
    public static final String KEY_SERVER = "server";
    public static final String KEY_PORT = "port";
    public static final String KEY_DATABASE = "database";
    public static final String KEY_SCHEMA = "schema";
    public static final String KEY_USER = "user";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_READONLY = "readOnly";
    public static final String KEY_DRIVER = "driver";
    public static final String KEY_ROOT_DIRECTORY = "rootDirectory";
    public static final String KEY_NAMESPACE = "namespace";

    private final ProviderSource source;
    private CoverageDatabase database;

    private final Set<Name> index = new HashSet<Name>();

    protected CoverageSQLProvider(ProviderSource source) throws IOException, SQLException {
        this.source = source;

        loadDataBase();
        visit();
    }

    /**
     * Creates a {@linkplain CoverageDatabase database connection} with the provided
     * parameters.
     *
     * @throws SQLException if the login attempt reaches the timeout (5s here).
     */
    private void loadDataBase() throws SQLException{
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
            int index = oldDataBase.lastIndexOf('/');
            dbName = oldDataBase.substring(index+1, oldDataBase.length());
            oldDataBase = oldDataBase.substring(0, index);
            index = oldDataBase.lastIndexOf('/');
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
            final String portTxt = properties.getProperty(KEY_PORT);
            if(server == null || server.trim().isEmpty()){
                server = "localhost";
            }
            try{
                port = Integer.parseInt(portTxt);
            }catch(Exception nf){
                //catch numberformat and nullpointer
                LOGGER.log(Level.WARNING, "Port value for coverage-sql is not valid : {0}", portTxt);
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
    }


    protected ProviderSource getSource(){
        return source;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Set<Name> getKeys() {
        return Collections.unmodifiableSet(index);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Set<Name> getKeys(String service) {
        if (source.services.contains(service) || source.services.isEmpty()) {
            return getKeys();
        }
        return new HashSet();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean contains(Name key) {
        return index.contains(key);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public LayerDetails get(final Name key) {
        // If the key is not present for this provider, it is not necessary to go further.
        // Without this test, an exception will be logged whose message is a warning about
        // the non presence of the requested key into the "Layers" table.
        if (!contains(key)) {
            return null;
        }
        LayerCoverageReader reader = null;
        try {
            reader = database.createGridCoverageReader(key.getLocalPart());
        } catch (CoverageStoreException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }


        if (reader != null) {
            final String name = key.getLocalPart();
            final ProviderLayer layer = source.getLayer(name);
            if (layer == null) {
                return new CoverageSQLLayerDetails(reader,null,null,key);

            } else {
                return new CoverageSQLLayerDetails(reader,layer.styles,null,key);
            }
        }

        return null;
    }

    /**
     * {@inheritDoc }
     */
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
        dispose();
        synchronized(this){
            index.clear();
            try {
                loadDataBase();
            } catch (SQLException ex) {
                LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            }
            visit();
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void dispose() {
        synchronized(this){
            index.clear();
            database.dispose();
        }
    }

    /**
     * Visit all layers detected from the database table {@code Layers}.
     */
    private void visit() {
        try {
            final Set<String> layers = database.getLayers().result();

            for(String name : layers){
                test(name);
            }
        } catch (CoverageStoreException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        } catch (CancellationException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }

    }


    /**
     * Test whether the provided candidate layer can be handled by the provider or not.
     *
     * @param candidate Candidate to be a layer.
     */
    private void test(final String candidate){
        final String name = candidate;
        if (source.loadAll || source.containsLayer(name)){
            String nmsp = source.parameters.get(KEY_NAMESPACE);
            if (nmsp == null) {
                nmsp = DEFAULT_NAMESPACE;
            }
            index.add(new DefaultName(nmsp,name));
        }
    }

    @Override
    public ElevationModel getElevationModel(Name name) {

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
