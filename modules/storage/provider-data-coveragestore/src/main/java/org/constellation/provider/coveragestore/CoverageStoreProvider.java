/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.provider.coveragestore;

import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.metadata.iso.identification.AbstractIdentification;
import org.apache.sis.metadata.iso.identification.DefaultDataIdentification;
import org.apache.sis.storage.DataStoreException;
import org.constellation.admin.EmbeddedDatabase;
import org.constellation.admin.dao.DataRecord;
import org.constellation.admin.dao.ProviderRecord;
import org.constellation.admin.dao.Session;
import org.constellation.provider.AbstractLayerProvider;
import org.constellation.provider.DefaultCoverageStoreLayerDetails;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.ProviderService;
import org.constellation.utils.MetadataUtilities;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.CoverageStore;
import org.geotoolkit.coverage.CoverageStoreFinder;
import org.geotoolkit.coverage.postgresql.PGCoverageStore;
import org.geotoolkit.version.VersionControl;
import org.geotoolkit.version.VersioningException;
import org.opengis.feature.type.Name;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class CoverageStoreProvider extends AbstractLayerProvider{

    private CoverageStore store;
    private Set<Name> names;

    public CoverageStoreProvider(ProviderService service, ParameterValueGroup param){
        super(service,param);
        visit();
    }

    @Override
    public synchronized void reload() {
        super.reload();
        dispose();

        //parameter is a choice of different types
        //extract the first one
        ParameterValueGroup param = getSource();
        param = param.groups("choice").get(0);
        ParameterValueGroup factoryconfig = null;
        for(GeneralParameterValue val : param.values()){
            if(val instanceof ParameterValueGroup){
                factoryconfig = (ParameterValueGroup) val;
                break;
            }
        }

        if(factoryconfig == null){
            getLogger().log(Level.WARNING, "No configuration for coverage store source.");
            names = Collections.EMPTY_SET;
            return;
        }
        try {
            //create the store
            store = CoverageStoreFinder.open(factoryconfig);
            if(store == null){
                throw new DataStoreException("Could not create coverage store for parameters : "+factoryconfig);
            }
            names = store.getNames();
        } catch (DataStoreException ex) {
            names = Collections.EMPTY_SET;
            getLogger().log(Level.WARNING, ex.getMessage(), ex);
        }

        visit();
    }

    @Override
    public synchronized void dispose() {
        super.dispose();
        if(store != null){
            store.dispose();
            store = null;
            names = null;
        }
    }

    @Override
    public Set<Name> getKeys() {
        if(names == null){
            reload();
        }
        return names;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public LayerDetails get(final Name key) {
        return get(key, null);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public LayerDetails get(Name key, Date version) {
        key = fullyQualified(key);
        if(!contains(key)){
            return null;
        }
        try {
            if (store != null) {
                CoverageReference coverageReference = null;
                if ( store.handleVersioning()) {
                    VersionControl control = store.getVersioning(key);
                    if (control.isVersioned() && version != null) {
                        coverageReference = store.getCoverageReference(key, control.getVersion(version));
                    }
                }
                if(coverageReference == null) {
                    coverageReference = store.getCoverageReference(key);
                }
                return new DefaultCoverageStoreLayerDetails(key, coverageReference);
            }
        } catch (DataStoreException ex) {
            getLogger().log(Level.WARNING, ex.getMessage(), ex);
        } catch (VersioningException ex) {
            getLogger().log(Level.WARNING, ex.getMessage(), ex);
        }
        return null;
    }

    @Override
    public void remove(Name key) {
        if (store == null) {
            reload();
        }

        try {
            store.delete(key);
            reload();
       } catch (DataStoreException ex) {
            getLogger().log(Level.WARNING, ex.getMessage(), ex);
        }
    }
    
    @Override
    public void removeAll() {
        try {
            for (Name name : names) {
                store.delete(name);
            }
            reload();

            if (store instanceof PGCoverageStore) {
                final PGCoverageStore pgStore = (PGCoverageStore)store;
                final String dbSchema = pgStore.getDatabaseSchema();
                if (dbSchema != null && !dbSchema.isEmpty()) {
                    pgStore.dropPostgresSchema(dbSchema);
                }
            }
        } catch (DataStoreException e) {
            getLogger().log(Level.WARNING, e.getMessage(), e);
        }
    }


    @Override
    protected void visit() {
        super.visit();

        // Update administration database.
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            ProviderRecord pr = session.readProvider(this.getId());
            if (pr == null) {
                pr = session.writeProvider(this.getId(), ProviderRecord.ProviderType.LAYER, "coverage-store", getSource(), null);
            }
            final List<DataRecord> list = pr.getData();

            // Remove no longer existing data.
            for (final DataRecord data : list) {
                boolean found = false;
                for (final Name key : this.names) {
                    if (data.getName().equals(key.getLocalPart())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    session.deleteData(this.getId(), data.getName());
                }
            }

            // Add not registered new data.
            for (final Name key : this.names) {
                boolean found = false;
                for (final DataRecord data : list) {
                    if (key.getLocalPart().equals(data.getName())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    DataRecord dr = session.writeData(key.getLocalPart(), pr, DataRecord.DataType.COVERAGE, null);

                    // TODO
                    //Get metadata
                    final String providerId = this.getId();
                    final DefaultMetadata metadata = MetadataUtilities.loadMetadata(providerId);
                    if(metadata!=null){
                        final String description = metadata.getIdentificationInfo().iterator().next().getAbstract().toString();
                        final String title = metadata.getIdentificationInfo().iterator().next().getCitation().getTitle().toString();
                        final Locale locale = metadata.getLocales().iterator().next();

                        //Save title and description
                        dr.setTitle(locale, title);
                        dr.setDescription(locale, description);
                    }
                }
            }


        } catch (IOException | SQLException ex) {
            getLogger().log(Level.WARNING, "An error occurred while updating database on provider startup.", ex);
        } finally {
            if (session != null) session.close();
        }

    }
}
