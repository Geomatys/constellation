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
package org.constellation.provider.coveragefile;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.constellation.provider.AbstractLayerProvider;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.configuration.ProviderLayer;
import org.constellation.provider.configuration.ProviderSource;

import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.coverage.io.GridCoverageReaders;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.image.io.mosaic.TileManager;
import org.geotoolkit.map.ElevationModel;
import org.geotoolkit.map.MapBuilder;

import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 *
 * @version $Id$
 * @author Johann Sorel (Geomatys)
 */
public class CoverageMosaicProvider extends AbstractLayerProvider{

    public static final String KEY_FOLDER_PATH = "path";
    public static final String KEY_NAMESPACE = "namespace";

    private final Map<Name,Entry<TileManager,CoordinateReferenceSystem>> index =
            new HashMap<Name,Entry<TileManager,CoordinateReferenceSystem>>(){
        @Override
        public Entry<TileManager,CoordinateReferenceSystem> get(Object key) {
            if(key instanceof Name && ((Name)key).isGlobal()){
                String nmsp = source.parameters.get(KEY_NAMESPACE);
                if (nmsp == null) {
                    nmsp = DEFAULT_NAMESPACE;
                } else if ("no namespace".equals(nmsp)) {
                    nmsp = null;
                }

                key = new DefaultName(nmsp, ((Name)key).getLocalPart());
            }

            return super.get(key);
        }
    };

    private final ProviderSource source;
    private final File folder;

    protected CoverageMosaicProvider(ProviderSource source) throws IOException, SQLException {
        this.source = source;
        final String path = source.parameters.get(KEY_FOLDER_PATH);

        if (path == null) {
            throw new IllegalArgumentException("Found configuration file but a path parameter is not defined.");
        }

        folder = new File(path);

        if (!folder.exists()) {
            throw new IllegalArgumentException("Did not find a tile manager definition file or a mosaic directory for path: "+ path);
        }

        reload();
    }

    @Override
    public ProviderSource getSource(){
        return source;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Set<Name> getKeys() {
        return index.keySet();
    }

    @Override
    public Set<Name> getKeys(String sourceName) {
       if (source.id.equals(sourceName)) {
            return index.keySet();
        }
        return new HashSet();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean contains(Name key) {
        return index.containsKey(key);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public LayerDetails get(final Name key) {
        final Entry<TileManager,CoordinateReferenceSystem> entry = index.get(key);

        if (entry != null) {
            final String name = key.getLocalPart();
            final ProviderLayer layer = source.getLayer(name);
            final Name em = (layer == null || layer.elevationModel == null) ? null : DefaultName.valueOf(layer.elevationModel);
            final GridCoverageReader reader;
            try {
                reader = GridCoverageReaders.toCoverageReader(entry.getKey(), entry.getValue());
            } catch (CoverageStoreException ex) {
                Logger.getLogger(CoverageMosaicProvider.class.getName()).log(Level.WARNING, "Failed to load coverage reader from tile manager.", ex);
                return null;
            }
            if (layer == null) {
                return new GridCoverageReaderLayerDetails(reader,null,em, key);

            } else {
                return new GridCoverageReaderLayerDetails(reader,layer.styles,em,key);
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

            //find the namespace
            String nmsp = source.parameters.get(KEY_NAMESPACE);
            if (nmsp == null) {
                nmsp = DEFAULT_NAMESPACE;
            } else if ("no namespace".equals(nmsp)) {
                nmsp = null;
            }

            //find the layer name
            final Name name;
            if(!source.layers.isEmpty()){
                //we use the first layer name
                name = new DefaultName(nmsp, source.layers.get(0).name);
            }else{
                //the name is the folder name
                name = new DefaultName(nmsp, folder.getName());
            }

            try{
                final Entry<TileManager,CoordinateReferenceSystem> entry = GridCoverageReaders.openTileManager(folder);
                index.put(name, entry);
            }catch(IOException ex){
                Logger.getLogger(CoverageMosaicProvider.class.getName()).log(Level.WARNING, "Failed to load mosaic reader.", ex);
            }catch(CoverageStoreException ex){
                Logger.getLogger(CoverageMosaicProvider.class.getName()).log(Level.WARNING, "Failed to load mosaic reader.", ex);
            }
            
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void dispose() {
        synchronized(this){
            index.clear();
            source.layers.clear();
            source.parameters.clear();
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ElevationModel getElevationModel(Name name) {

        final ProviderLayer layer = source.getLayer(name.getLocalPart());
        if(layer != null && layer.isElevationModel){
            final GridCoverageReaderLayerDetails pgld = (GridCoverageReaderLayerDetails) getByIdentifier(name);
            if(pgld != null){
                return MapBuilder.createElevationModel(pgld.getReader());
            }
        }
        
        return null;
    }

}
