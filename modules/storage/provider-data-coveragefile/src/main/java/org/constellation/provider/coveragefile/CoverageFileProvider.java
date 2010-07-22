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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageReader;

import org.constellation.provider.AbstractLayerProvider;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.configuration.ProviderLayer;
import org.constellation.provider.configuration.ProviderSource;

import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.coverage.io.ImageCoverageReader;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.image.io.XImageIO;
import org.geotoolkit.map.ElevationModel;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.util.collection.Cache;
import org.geotoolkit.util.logging.Logging;

import org.opengis.feature.type.Name;


/**
 *
 * @version $Id$
 * @author Johann Sorel (Geomatys)
 */
public class CoverageFileProvider extends AbstractLayerProvider{

    public static final String KEY_FOLDER_PATH = "path";
    public static final String KEY_NAMESPACE = "namespace";

    private static final Logger LOGGER = Logging.getLogger(CoverageFileProvider.class);

    private final Map<Name,File> index = new HashMap<Name,File>(){

        @Override
        public File get(Object key) {
            if(key instanceof Name && ((Name)key).isGlobal()){
                String nmsp = source.parameters.get(KEY_NAMESPACE);
                if (nmsp == null) {
                    nmsp = DEFAULT_NAMESPACE;
                } else if (nmsp.equals("no namespace")) {
                    nmsp = null;
                }

                key = new DefaultName(nmsp, ((Name)key).getLocalPart());
            }

            return super.get(key);
        }

    };

    private final Map<Name,GridCoverageReader> cache = new Cache<Name, GridCoverageReader>(10, 10, true){

        @Override
        public GridCoverageReader get(Object key) {
            if(key instanceof Name && ((Name)key).isGlobal()){
                String nmsp = source.parameters.get(KEY_NAMESPACE);
                if (nmsp == null) {
                    nmsp = DEFAULT_NAMESPACE;
                } else if (nmsp.equals("no namespace")) {
                    nmsp = null;
                }

                key = new DefaultName(nmsp, ((Name)key).getLocalPart());
            }

            return super.get(key);
        }

    };

    private final ProviderSource source;
    private final File folder;

    protected CoverageFileProvider(ProviderSource source) throws IOException, SQLException {
        this.source = source;
        final String path = source.parameters.get(KEY_FOLDER_PATH);

        if (path == null) {
            throw new IllegalArgumentException("Provided File does not exits or is not a folder.");
        }

        folder = new File(path);

        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            throw new IllegalArgumentException("Provided File does not exits or is not a folder.");
        }

        visit(folder);
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
    public Set<Name> getKeys(String service) {
        if (source.services.contains(service) || source.services.isEmpty()) {
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
        GridCoverageReader reader = cache.get(key);

        if (reader == null) {
            //reader is not in the cache, try to load it
            final File f = index.get(key);
            if (f != null) {
                try {
                    //we have this data source in the folder
                    reader = loadReader(f);
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                } catch (CoverageStoreException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
                if (reader != null) {
                    //cache the reader
                    cache.put(key, reader);
                }
            }
        }

        if (reader != null) {
            final String name = key.getLocalPart();
            final ProviderLayer layer = source.getLayer(name);
            final Name em = (layer == null || layer.elevationModel == null) ? null : DefaultName.valueOf(layer.elevationModel);
            if (layer == null) {
                return new CoverageFileLayerDetails(reader,null,em, key);

            } else {
                return new CoverageFileLayerDetails(reader,layer.styles,em,key);
            }
        }

        return null;
    }

    private GridCoverageReader loadReader(File f) throws IOException, CoverageStoreException{
        final ImageCoverageReader reader = new ImageCoverageReader();
        reader.setInput(f);
        return reader;
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
     *
     * @param candidate Candidate to be a world image file.
     */
    private void test(final File candidate){
        if (candidate.isFile()){
            try {
                //don't comment this block, This raise an error if no reader for the file can be found
                //this way we are sure that the file is an image.
                final ImageReader reader = XImageIO.getReaderBySuffix(candidate, Boolean.TRUE, Boolean.TRUE);

                final String fullName = candidate.getName();
                final int idx = fullName.lastIndexOf('.');
                final String name = fullName.substring(0, idx);
                if (source.loadAll || source.containsLayer(name)){
                    String nmsp = source.parameters.get(KEY_NAMESPACE);
                    if (nmsp == null) {
                        nmsp = DEFAULT_NAMESPACE;
                    } else if (nmsp.equals("no namespace")) {
                        nmsp = null;
                    }
                    index.put(new DefaultName(nmsp,name), candidate);
                }

            } catch (IOException ex) {
            }
        }
    }

    @Override
    public ElevationModel getElevationModel(Name name) {

        final ProviderLayer layer = source.getLayer(name.getLocalPart());
        if(layer != null && layer.isElevationModel){
            final CoverageFileLayerDetails pgld = (CoverageFileLayerDetails) getByIdentifier(name);
            if(pgld != null){
                return MapBuilder.createElevationModel(pgld.getReader());
            }
        }
        
        return null;
    }

}
