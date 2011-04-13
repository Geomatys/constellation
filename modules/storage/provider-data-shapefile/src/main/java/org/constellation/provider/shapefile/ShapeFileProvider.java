/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2011, Geomatys
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
package org.constellation.provider.shapefile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.constellation.provider.AbstractLayerProvider;
import org.constellation.provider.DefaultDataStoreLayerDetails;
import org.constellation.provider.LayerDetails;

import org.geotoolkit.data.DataStore;
import org.geotoolkit.data.DataStoreFinder;
import org.geotoolkit.data.shapefile.ShapefileDataStoreFactory;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.util.collection.Cache;
import org.geotoolkit.storage.DataStoreException;

import org.opengis.feature.type.Name;
import org.opengis.parameter.ParameterValueGroup;

import static org.constellation.provider.shapefile.ShapeFileProviderService.*;
import static org.constellation.provider.configuration.ProviderParameters.*;
import static org.geotoolkit.parameter.Parameters.*;
import static org.geotoolkit.data.shapefile.ShapefileDataStoreFactory.*;


/**
 * Shapefile Data provider. index and cache Datastores for the shapefiles
 * whithin the given folder.
 *
 * @version $Id$
 *
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public class ShapeFileProvider extends AbstractLayerProvider {

    /**
     * Mask to select only shape files.
     */
    private static final String MASK = ".shp";

    /**
     * Folder where are stored shape files.
     */
    private final File folder;

    /**
     * Keeps a link between the file name and the file.
     */
    private final Map<Name,File> index = new HashMap<Name,File>();
    private final Map<Name,DataStore> cache = new Cache<Name, DataStore>(10, 10, true);


    protected ShapeFileProvider(final ShapeFileProviderService service,
            final ParameterValueGroup source) throws IllegalArgumentException {
        super(service,source);

        final ParameterValueGroup config = getSourceConfiguration(source, SOURCE_CONFIG_DESCRIPTOR);
        final String path = stringValue(FOLDER_DESCRIPTOR, config);

        if (path == null) {
            throw new IllegalArgumentException("Provided File does not exits or is not a folder.");
        }

        folder = new File(path);

        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            throw new IllegalArgumentException("Provided File does not exits or is not a folder.");
        }

        visit();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Set<Name> getKeys() {
        return index.keySet();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Set<Name> getKeys(String sourceName) {
        if (sourceName.equals(getSourceId(getSource()))) {
            return index.keySet();
        }
        return new HashSet();
    }

    /**
     * {@inheritDoc }
     *
     * @todo Should use {@code cache.getOrCreate(...)} for concurrent access.
     */
    @Override
    public LayerDetails get(Name key) {
        DataStore store = cache.get(key);

        if (store == null) {
            //datastore is not in the cache, try to load it
            final File f = index.get(key);
            if (f != null) {
                //we have this data source in the folder
                store = loadDataStore(f, value(NAMESPACE, getSourceConfiguration(getSource(), SOURCE_CONFIG_DESCRIPTOR)));
                if (store != null) {
                    //cache the datastore
                    cache.put(key, store);
                    try {
                        key = store.getNames().iterator().next();
                    } catch (DataStoreException ex) {
                        getLogger().log(Level.WARNING, ex.getLocalizedMessage(), ex);
                    }
                }
            }
        }

        if (store != null) {
            final ParameterValueGroup layer = getLayer(getSource(), key.getLocalPart());
            if (layer == null) {
                return new DefaultDataStoreLayerDetails(key, store, null, null, null, null, null);
                
            } else {
                final List<String> styles = getLayerStyles(layer);
                return new DefaultDataStoreLayerDetails(key, store, styles,
                        value(LAYER_DATE_START_FIELD_DESCRIPTOR, layer),
                        value(LAYER_DATE_END_FIELD_DESCRIPTOR, layer),
                        value(LAYER_ELEVATION_START_FIELD_DESCRIPTOR, layer),
                        value(LAYER_ELEVATION_END_FIELD_DESCRIPTOR, layer));
            }
        }

        return null;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void reload() {
        synchronized(this){
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
            index.clear();
            cache.clear();
        }
    }

    @Override
    protected void visit() {
        visit(folder);
        super.visit();
    }

    /**
     * Visit all files and directories contained in the directory specified, and add
     * all shape files in {@link #index}.
     *
     * @param file The starting file or folder.
     */
    private void visit(final File file) {
        if (file.isDirectory()) {
            final File[] list = file.listFiles();
            if (list != null) {
                for (int i = 0; i < list.length; i++) {
                    visit(list[i]);
                }
            }
        } else {
            test(file);
        }
    }

    /**
     * Add the file to the {@link #index} if it respects the {@linkplain #MASK mask}.
     *
     * @param candidate Candidate to be a shape file.
     */
    private void test(final File candidate){
        if (candidate.isFile()){
            final String fullName = candidate.getName();
            if (fullName.toLowerCase().endsWith(MASK)){
                final String name = fullName.substring(0, fullName.length()-4);
                if (isLoadAll(getSource()) || containLayer(getSource(), name)){
                    String nmsp = value(NAMESPACE, getSourceConfiguration(getSource(), SOURCE_CONFIG_DESCRIPTOR));
                    if (nmsp == null) {
                        nmsp = DEFAULT_NAMESPACE;
                    } else if (nmsp.equals(NO_NAMESPACE)) {
                        nmsp = null;
                    }
                    index.put(new DefaultName(nmsp,name), candidate);
                }
            }
        }
    }

    private static DataStore loadDataStore(final File f,String namespace) {
        if (f == null || !f.exists()) {
            return null;
        }

        final ParameterValueGroup params = ShapefileDataStoreFactory.PARAMETERS_DESCRIPTOR.createValue();
        try {
            params.parameter(URLP.getName().getCode()).setValue(f.toURI().toURL());
            params.parameter(NAMESPACE.getName().getCode()).setValue(namespace);
            return DataStoreFinder.getDataStore(params);
       } catch (DataStoreException ex) {
            getLogger().log(Level.WARNING, null, ex);
            return null;
        } catch (IOException ex) {
            getLogger().log(Level.WARNING, null, ex);
            return null;
        }
    }

}
