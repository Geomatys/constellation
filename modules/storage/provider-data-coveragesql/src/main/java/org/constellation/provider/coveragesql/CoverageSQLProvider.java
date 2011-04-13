/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010-2011, Geomatys
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

import java.util.List;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import javax.sql.DataSource;

import org.constellation.provider.AbstractLayerProvider;
import org.constellation.provider.LayerDetails;

import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.sql.CoverageDatabase;
import org.geotoolkit.coverage.sql.LayerCoverageReader;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.map.ElevationModel;
import org.geotoolkit.jdbc.WrappedDataSource;
import org.geotoolkit.map.MapBuilder;

import org.opengis.feature.type.Name;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;

import org.postgresql.ds.PGConnectionPoolDataSource;

import static org.constellation.provider.coveragesql.CoverageSQLProviderService.*;
import static org.constellation.provider.configuration.ProviderParameters.*;
import static org.geotoolkit.parameter.Parameters.*;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class CoverageSQLProvider extends AbstractLayerProvider{

    private CoverageDatabase database;

    private final Set<Name> index = new HashSet<Name>();

    protected CoverageSQLProvider(final CoverageSQLProviderService service,
            final ParameterValueGroup source) throws IOException, SQLException {
        super(service,source);
        visit();
    }

    private ParameterValueGroup getSourceConfiguration(){
        final ParameterValueGroup params = getSource();

        final List<ParameterValueGroup> groups = params.groups(COVERAGESQL_DESCRIPTOR.getName().getCode());
        if(!groups.isEmpty()){
            return groups.get(0);
        }
        return null;
    }

    /**
     * Creates a {@linkplain CoverageDatabase database connection} with the provided
     * parameters.
     *
     * @throws SQLException if the login attempt reaches the timeout (5s here).
     */
    private void loadDataBase() throws SQLException{
        final Properties properties = new Properties();

        for(GeneralParameterValue param : getSourceConfiguration().values()){
            if(param instanceof ParameterValue){
                final ParameterValue pmval = (ParameterValue) param;
                final Object value = pmval.getValue();
                if(value != null){
                    properties.put(pmval.getDescriptor().getName().getCode(), value);
                }
            }
        }

        String server ="localhost";
        int port = 5432;
        String dbName = "";

        //parse string if old format : exemple jdbc:postgresql://server/database
        String oldDataBase = properties.getProperty(DATABASE_DESCRIPTOR.getName().getCode());
        if(oldDataBase != null && oldDataBase.contains("/")){
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
            server = properties.getProperty(SERVER_DESCRIPTOR.getName().getCode());
            final String portTxt = properties.getProperty(PORT_DESCRIPTOR.getName().getCode());
            if(server == null || server.trim().isEmpty()){
                server = "localhost";
            }
            try{
                port = Integer.parseInt(portTxt);
            }catch(Exception nf){
                //catch numberformat and nullpointer
                getLogger().log(Level.WARNING, "Port value for coverage-sql is not valid : {0}", portTxt);
                port = 5432;
            }
            dbName = properties.getProperty(DATABASE_DESCRIPTOR.getName().getCode());
        }


        final PGConnectionPoolDataSource pool = new PGConnectionPoolDataSource();
        pool.setServerName(server);
        pool.setPortNumber(port);
        pool.setDatabaseName(dbName);
        pool.setUser(properties.getProperty(USER_DESCRIPTOR.getName().getCode()));
        pool.setPassword(properties.getProperty(PASSWORD_DESCRIPTOR.getName().getCode()));
        pool.setLoginTimeout(5);

        final DataSource dataSource = new WrappedDataSource(pool);

        database = new CoverageDatabase(dataSource, properties);
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
    public LayerDetails get(final Name key) {
        // If the key is not present for this provider, it is not necessary to go further.
        // Without this test, an exception will be logged whose message is a warning about
        // the non presence of the requested key into the "Layers" table.
        if (!contains(key)) {
            return null;
        }

        if(database == null){
            return null;
        }

        LayerCoverageReader reader = null;
        try {
            reader = database.createGridCoverageReader(key.getLocalPart());
        } catch (CoverageStoreException ex) {
            getLogger().log(Level.WARNING, ex.getMessage(), ex);
        }


        if (reader != null) {
            final String name = key.getLocalPart();
            final ParameterValueGroup layer = getLayer(getSource(), name);
            final String elemodel = (layer==null)?null:value(LAYER_ELEVATION_MODEL_DESCRIPTOR, layer);
            final Name em = (layer == null || elemodel == null) ? null : DefaultName.valueOf(elemodel);
            if (layer == null) {
                return new CoverageSQLLayerDetails(reader,null,em,key);

            } else {
                return new CoverageSQLLayerDetails(reader,getLayerStyles(layer),em,key);
            }
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
                getLogger().log(Level.WARNING, ex.getMessage(), ex);
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
            if(database != null){
                database.dispose();
            }
        }
    }

    /**
     * Visit all layers detected from the database table {@code Layers}.
     */
    @Override
    protected void visit() {
        try {
            loadDataBase();
        } catch (SQLException ex) {
            getLogger().log(Level.WARNING,ex.getLocalizedMessage(),ex);
            return;
        }

        try {
            final Set<String> layers = database.getLayers().result();

            for(String name : layers){
                test(name);
            }
        } catch (CoverageStoreException ex) {
            getLogger().log(Level.WARNING, ex.getMessage(), ex);
        } catch (CancellationException ex) {
            getLogger().log(Level.WARNING, ex.getMessage(), ex);
        }
        super.visit();
    }


    /**
     * Test whether the provided candidate layer can be handled by the provider or not.
     *
     * @param candidate Candidate to be a layer.
     */
    private void test(final String candidate){
        final String name = candidate;
        if (isLoadAll(getSource()) || containLayer(getSource(), name)){
            String nmsp = value(NAMESPACE_DESCRIPTOR, getSourceConfiguration());
            if (nmsp == null) {
                nmsp = DEFAULT_NAMESPACE;
            } else if (nmsp.equals(NO_NAMESPACE)){
                nmsp = null;
            }
            index.add(new DefaultName(nmsp,name));
        }
    }

    @Override
    public ElevationModel getElevationModel(Name name) {

        final ParameterValueGroup layer = getLayer(getSource(), name.getLocalPart());
        if(layer != null){
            Boolean isEleModel = value(LAYER_IS_ELEVATION_MODEL_DESCRIPTOR, layer);

            if(!Boolean.TRUE.equals(isEleModel)){
                return null;
            }

            final CoverageSQLLayerDetails pgld = (CoverageSQLLayerDetails) getByIdentifier(name);
            if(pgld != null){
                return MapBuilder.createElevationModel(pgld.getReader());
            }
        }
        
        return null;
    }

}
