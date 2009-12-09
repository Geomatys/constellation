/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.constellation.provider.shapefile;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.constellation.provider.AbstractLayerProvider;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.configuration.ProviderLayer;
import org.constellation.provider.configuration.ProviderSource;

import org.geotoolkit.data.DataStore;
import org.geotoolkit.data.DataStoreFinder;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.map.ElevationModel;
import org.geotoolkit.util.collection.Cache;
import org.opengis.feature.type.Name;



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
     * Default logger.
     */
    private static final Logger LOGGER = Logger.getLogger("org.constellation.provider.shapefile");

    /**
     * Key for the path of the directory which contains shapefiles.
     */
    public static final String KEY_FOLDER_PATH = "path";

    /**
     * Mask to select only shape files.
     */
    private static final String MASK = ".shp";

    /**
     * Folder where are stored shape files.
     */
    private final File folder;
    private final ProviderSource source;

    /**
     * Keeps a link between the file name and the file.
     */
    private final Map<Name,File> index = new HashMap<Name,File>();
    private final Map<Name,DataStore> cache = new Cache<Name, DataStore>(10, 10, true);


    protected ShapeFileProvider(final ProviderSource source) throws IllegalArgumentException {
        this.source = source;
        final String path = source.parameters.get(KEY_FOLDER_PATH);

        if(path == null){
            throw new IllegalArgumentException("Provided File does not exits or is not a folder.");
        }

        folder = new File(path);

        if(folder == null || !folder.exists() || !folder.isDirectory()){
            throw new IllegalArgumentException("Provided File does not exits or is not a folder.");
        }

        visit(folder);
    }

    protected ProviderSource getSource(){
        return source;
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
    public boolean contains(final Name key) {
        return index.containsKey(key);
    }

    /**
     * {@inheritDoc }
     *
     * @todo Should use {@code cache.getOrCreate(...)} for concurrent access.
     */
    @Override
    public LayerDetails get(final Name key) {
        DataStore store = cache.get(key);

        if(store == null) {
            //datastore is not in the cache, try to load it
            final File f = index.get(key);
            if (f != null) {
                //we have this data source in the folder
                store = loadDataStore(f);
                if (store != null) {
                    //cache the datastore
                    cache.put(key, store);
                }
            }
        }

        if(store != null) {
            final ProviderLayer layer = source.getLayer(key.getLocalPart());
            if (layer == null) {
                try {
                    return new ShapeFileLayerDetails(key.getLocalPart(), store.getFeatureSource(key.getLocalPart()), null,
                            null, null, null, null);
                } catch (IOException ex) {
                    //we could not create the feature source
                    LOGGER.log(Level.SEVERE, "we could not create the feature source", ex);
                }
            } else {
                final List<String> styles = layer.styles;
                try {
                    return new ShapeFileLayerDetails(key.getLocalPart(), store.getFeatureSource(key.getLocalPart()), styles,
                            layer.dateStartField, layer.dateEndField,
                            layer.elevationStartField, layer.elevationEndField);
                } catch (IOException ex) {
                    //we could not create the feature source
                    LOGGER.log(Level.SEVERE, "we could not create the feature source", ex);
                }
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
            visit(folder);
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
            source.layers.clear();
            source.parameters.clear();
        }
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
                if (source.loadAll || source.containsLayer(name)){
                    index.put(new DefaultName(DEFAULT_NAMESPACE,name), candidate);
                }
            }
        }
    }

    private static DataStore loadDataStore(final File f) {
        if (f == null || !f.exists()) {
            return null;
        }

        final Map<String,Serializable> params = new HashMap<String,Serializable>();
        try {
            params.put("url", f.toURI().toURL());
            return DataStoreFinder.getDataStore(params);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            return null;
        }
    }


    @Override
    public ElevationModel getElevationModel(String name) {
        return null;
    }

}
