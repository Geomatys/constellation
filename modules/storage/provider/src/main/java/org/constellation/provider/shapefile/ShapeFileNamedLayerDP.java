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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;

import org.constellation.provider.LayerDataProvider;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.configuration.ProviderConfig;
import org.constellation.provider.configuration.ProviderLayer;
import org.constellation.provider.configuration.ProviderSource;
import org.constellation.ws.rs.WebService;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.map.ElevationModel;
import org.geotools.util.SoftValueHashMap;

import org.xml.sax.SAXException;


/**
 * Shapefile Data provider. index and cache Datastores for the shapefiles
 * whithin the given folder.
 *
 * @version $Id$
 *
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public class ShapeFileNamedLayerDP implements LayerDataProvider {
    /**
     * Default logger.
     */
    private static final Logger LOGGER = Logger.getLogger("org.constellation.provider.shapefile");
    private static final String KEY_SHAPEFILE_CONFIG = "shapefile_config";
    private static final String KEY_FOLDER_PATH = "path";

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
    private final Map<String,File> index = new HashMap<String,File>();
    private final Map<String,DataStore> cache = new SoftValueHashMap<String, DataStore>(10);


    private ShapeFileNamedLayerDP(final ProviderSource source) throws IllegalArgumentException {
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
    public boolean contains(final String key) {
        return index.containsKey(key);
    }

    /**
     * {@inheritDoc }
     */
    public LayerDetails get(final String key) {
        DataStore store = cache.get(key);

        if(store == null) {
            //datastore is not in the cache, try to load it
            File f = index.get(key);
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
            final ProviderLayer layer = source.getLayer(key);
            final List<String> styles = layer.styles;
            try {
                return new ShapeFileLayerDetails(key, store.getFeatureSource(key), styles,
                        layer.dateStartField,layer.dateEndField,
                        layer.elevationStartField,layer.elevationEndField);
            } catch (IOException ex) {
                //we could not create the feature source
                LOGGER.log(Level.SEVERE, "we could not create the feature source", ex);
            }
        }

        return null;
    }

    /**
     * {@inheritDoc }
     */
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
            File[] list = file.listFiles();
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
        if(candidate.isFile()){
            String fullName = candidate.getName();
            if(fullName.toLowerCase().endsWith(MASK)){
                String name = fullName.substring(0, fullName.length()-4);
                if(source.containsLayer(name)){
                    index.put(name, candidate);
                }
            }
        }
    }

    private DataStore loadDataStore(final File f) {
        if (f == null || !f.exists()) {
            return null;
        }

        final Map<String,Object> params = new HashMap<String,Object>();
        try {
            params.put("url", f.toURI().toURL());
            return DataStoreFinder.getDataStore(params);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            return null;
        }
    }

    public static final Collection<ShapeFileNamedLayerDP> loadProviders() {
        final ProviderConfig config;
        try {
            config = getConfig();
        } catch (ParserConfigurationException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return Collections.emptyList();
        } catch (SAXException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return Collections.emptyList();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return Collections.emptyList();
        } catch (NamingException ex) {
            return Collections.emptyList();
        }
        final Collection<ShapeFileNamedLayerDP> dps = new ArrayList<ShapeFileNamedLayerDP>();
        if (config == null) {
            return dps;
        }
        for (final ProviderSource ps : config.sources) {
            try {
                dps.add(new ShapeFileNamedLayerDP(ps));
            } catch(IllegalArgumentException ex) {
                LOGGER.log(Level.WARNING, "Invalide shapefile provider config", ex);
            }
        }

        final StringBuilder builder = new StringBuilder("DATA PROVIDER : ShapeFile ");
        for (final ShapeFileNamedLayerDP dp : dps) {
            builder.append("\n[" + dp.source.parameters.get(KEY_FOLDER_PATH) + "=");
            for (final String layer : dp.getKeys()) {
                builder.append(layer + ",");
            }
            builder.append("]");
        }
        LOGGER.log(Level.INFO, builder.toString());
        return dps;
    }

    /**
     *
     * @return List of folders holding shapefiles
     */
    private static final ProviderConfig getConfig() throws ParserConfigurationException,
            SAXException, IOException, NamingException
    {

        String configFile = WebService.getPropertyValue(JNDI_GROUP,KEY_SHAPEFILE_CONFIG);

        if (configFile == null || configFile.trim().isEmpty()) {
            return null;
        }

        return ProviderConfig.read(new File(configFile.trim()));
    }

    @Override
    public ElevationModel getElevationModel(String name) {
        return null;
    }

}
