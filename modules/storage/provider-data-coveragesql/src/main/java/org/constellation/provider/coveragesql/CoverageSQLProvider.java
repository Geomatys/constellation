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
package org.constellation.provider.coveragesql;

import org.apache.sis.storage.DataStore;
import org.constellation.admin.dao.DataRecord;
import org.constellation.provider.AbstractDataProvider;
import org.constellation.provider.Data;
import org.constellation.provider.configuration.ProviderParameters;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.DefaultCoverageReference;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.sql.CoverageDatabase;
import org.geotoolkit.coverage.sql.LayerCoverageReader;
import org.geotoolkit.feature.type.DefaultName;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.map.ElevationModel;
import org.geotoolkit.map.MapBuilder;
import org.opengis.parameter.ParameterValueGroup;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;

import static org.constellation.provider.configuration.ProviderParameters.LAYER_ELEVATION_MODEL_DESCRIPTOR;
import static org.constellation.provider.configuration.ProviderParameters.LAYER_IS_ELEVATION_MODEL_DESCRIPTOR;
import static org.constellation.provider.configuration.ProviderParameters.containLayer;
import static org.constellation.provider.configuration.ProviderParameters.getLayer;
import static org.constellation.provider.configuration.ProviderParameters.isLoadAll;
import static org.constellation.provider.coveragesql.CoverageSQLProviderService.COVERAGESQL_DESCRIPTOR;
import static org.constellation.provider.coveragesql.CoverageSQLProviderService.NAMESPACE_DESCRIPTOR;
import static org.geotoolkit.parameter.Parameters.value;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class CoverageSQLProvider extends AbstractDataProvider{

    private CoverageDatabase database;

    private final Set<Name> index = new HashSet<>();

    protected CoverageSQLProvider(String providerId, final CoverageSQLProviderService service,
            final ParameterValueGroup source) {
        super(providerId,service,source);
        visit();
    }

    private ParameterValueGroup getSourceConfiguration(){
        return ProviderParameters.getSourceConfiguration(getSource(), COVERAGESQL_DESCRIPTOR);
    }

    @Override
    public DataStore getMainStore() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Creates a {@linkplain CoverageDatabase database connection} with the provided
     * parameters.
     *
     * @throws SQLException if the login attempt reaches the timeout (5s here).
     */
    private void loadDataBase() throws Exception{
        if(database != null){
            return;
        }

        final ParameterValueGroup params = getSourceConfiguration();
        database = new CoverageDatabase(params);
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
    public Data get(final Name key) {
        return get(key, null);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Data get(final Name key, Date version) {
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
            return new CoverageSQLLayerDetails(reader,null,em,key);
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
            visit();
        }
        fireUpdateEvent();
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
            database = null;
        }
    }

    /**
     * Visit all layers detected from the database table {@code Layers}.
     */
    @Override
    protected void visit() {
        try {
            loadDataBase();
        } catch (Exception ex) {
            getLogger().log(Level.WARNING,ex.getLocalizedMessage(),ex);
            return;
        }

        try {
            final Set<String> layers = database.getLayers().result();

            for(String name : layers){
                test(name);
            }
        } catch (CoverageStoreException | CancellationException ex) {
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
        if(name == null){
            return null;
        }

        final ParameterValueGroup layer = getLayer(getSource(), name.getLocalPart());
        if(layer != null){
            Boolean isEleModel = value(LAYER_IS_ELEVATION_MODEL_DESCRIPTOR, layer);

            if(!Boolean.TRUE.equals(isEleModel)){
                return null;
            }

            final CoverageSQLLayerDetails pgld = (CoverageSQLLayerDetails) getByIdentifier(name);
            if(pgld != null){
                final CoverageReference ref = new DefaultCoverageReference(pgld.getReader(), name);
                try {
                    return MapBuilder.createElevationModel(ref);
                } catch (CoverageStoreException ex) {
                    LOGGER.log(Level.WARNING, "error while creating elevation model", ex);
                }
            }
        }

        return null;
    }

    @Override
    public DataRecord.DataType getDataType() {
        return DataRecord.DataType.COVERAGE;
    }
}
