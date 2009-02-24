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

import org.constellation.catalog.CatalogException;
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
import org.geotools.map.ElevationModel;
import org.geotools.map.MapBuilder;
import org.geotools.util.SoftValueHashMap;


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


    private static final Logger LOGGER = Logger.getLogger("org.constellation.provider.postgrid");

    private final Map<String,Layer> index = new HashMap<String,Layer>();
    private final Map<String,List<String>> favorites = new  HashMap<String, List<String>>();
    private final Map<String,PostGridReader> cache = new SoftValueHashMap<String, PostGridReader>(6);

    private final ProviderSource source;
    private final Database database;

    protected PostGridProvider(ProviderSource source) throws IOException, SQLException {
        this.source = source;

        final Properties properties = new Properties();
        for(String key : source.parameters.keySet()){
            properties.put(key, source.parameters.get(key));
        }

        database = new Database(null,properties);
        
        visit();
    }

    protected ProviderSource getSource(){
        return source;
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
    public boolean contains(String key) {
        return index.containsKey(key);
    }

    /**
     * {@inheritDoc }
     */
    public PostGridLayerDetails get(final String key) {
        PostGridReader reader = null;
        synchronized(cache){
            reader = cache.get(key);

            if(reader == null) {
                //coverage reader is not in the cache, try to load it
                final Layer layer = index.get(key);

                if(layer == null) return null;

                GridCoverageTable gridTable = null;
                try {
                    gridTable = database.getTable(GridCoverageTable.class);
                } catch (NoSuchTableException ex) {
                    LOGGER.log(Level.SEVERE, "No GridCoverageTable", ex);
                }
                //create a mutable copy
                gridTable = new GridCoverageTable(gridTable);
                gridTable.setLayer(layer);
                reader = new PostGridReader(gridTable);
                cache.put(key, reader);
            }

        }

        final ProviderLayer layer = source.getLayer(key);
        final List<String> styles = new ArrayList<String>();
        final String elevationModel;
        if(layer != null){
            styles.addAll(layer.styles);
            elevationModel = layer.elevationModel;
        }else{
            elevationModel = null;
        }

        return (reader != null) ? new PostGridLayerDetails(reader, styles, elevationModel) : null;
    }

    /**
     * {@inheritDoc }
     */
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

        ProviderLayer layer = source.getLayer(name);
        if(layer != null && layer.isElevationModel){
            PostGridLayerDetails pgld = get(name);
            if(pgld != null){
                return MapBuilder.createElevationModel(pgld.getReader(), null);
            }
        }
        
        return null;
    }

}
