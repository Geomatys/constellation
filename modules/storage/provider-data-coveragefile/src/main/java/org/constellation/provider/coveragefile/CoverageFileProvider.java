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
package org.constellation.provider.coveragefile;

import org.apache.sis.util.collection.Cache;
import org.constellation.provider.AbstractLayerProvider;
import org.constellation.provider.LayerDetails;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.coverage.io.ImageCoverageReader;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.image.io.XImageIO;
import org.geotoolkit.map.ElevationModel;
import org.geotoolkit.map.MapBuilder;
import org.opengis.feature.type.Name;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;

import javax.imageio.ImageReader;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.constellation.admin.dao.DataRecord;

import static org.constellation.provider.configuration.ProviderParameters.LAYER_ELEVATION_MODEL_DESCRIPTOR;
import static org.constellation.provider.configuration.ProviderParameters.LAYER_IS_ELEVATION_MODEL_DESCRIPTOR;
import static org.constellation.provider.configuration.ProviderParameters.containLayer;
import static org.constellation.provider.configuration.ProviderParameters.getLayer;
import static org.constellation.provider.configuration.ProviderParameters.isLoadAll;
import static org.constellation.provider.coveragefile.CoverageFileProviderService.FOLDER_DESCRIPTOR;
import static org.constellation.provider.coveragefile.CoverageFileProviderService.NAMESPACE_DESCRIPTOR;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.DefaultCoverageReference;
import static org.geotoolkit.parameter.Parameters.value;

/**
 *
 * @version $Id$
 * @author Johann Sorel (Geomatys)
 */
public class CoverageFileProvider extends AbstractLayerProvider{

    public static final String KEY_FOLDER_PATH = "path";
    public static final String KEY_NAMESPACE = "namespace";

    private final Map<Name,File> index = new HashMap<Name,File>(){

        @Override
        public File get(Object key) {
            if(key instanceof Name && ((Name)key).isGlobal()){
                String nmsp = value(NAMESPACE_DESCRIPTOR, getSourceConfiguration(getSource()));
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

    private final Map<Name,GridCoverageReader> cache = new Cache<Name, GridCoverageReader>(10, 10, true){

        @Override
        public GridCoverageReader get(Object key) {
            if(key instanceof Name && ((Name)key).isGlobal()){
                String nmsp = value(NAMESPACE_DESCRIPTOR, getSourceConfiguration(getSource()));
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

    private File folder;

    protected CoverageFileProvider(final CoverageFileProviderService service,
            final ParameterValueGroup source) throws IOException, SQLException {
        super(service,source);
        visit();
    }

    private static ParameterValueGroup getSourceConfiguration(final ParameterValueGroup params){

        final List<ParameterValueGroup> groups = params.groups(CoverageFileProviderService.SOURCE_CONFIG_DESCRIPTOR.getName().getCode());
        if(!groups.isEmpty()){
            return groups.get(0);
        }
        return null;
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
    public LayerDetails get(final Name key) {
        return get(key, null);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public LayerDetails get(final Name key, Date version) {
        GridCoverageReader reader = cache.get(key);

        if (reader == null) {
            //reader is not in the cache, try to load it
            final File f = index.get(key);
            if (f != null) {
                try {
                    //we have this data source in the folder
                    reader = loadReader(f);
                } catch (IOException | CoverageStoreException ex) {
                    getLogger().log(Level.SEVERE, null, ex);
                }
                if (reader != null) {
                    //cache the reader
                    cache.put(key, reader);
                }
            }
        }

        if (reader != null) {
            final String name = key.getLocalPart();
            final ParameterValueGroup layer = getLayer(getSource(), name);
            final String elemodel = (layer==null)?null:value(LAYER_ELEVATION_MODEL_DESCRIPTOR, layer);
            final Name em = (layer == null || elemodel == null) ? null : DefaultName.valueOf(elemodel);
            return new GridCoverageReaderLayerDetails(reader,null,em,key);
        }

        return null;
    }

    private GridCoverageReader loadReader(File f) throws IOException, CoverageStoreException{
        final ImageCoverageReader reader = new ImageCoverageReader();
        reader.setInput(f);
        return reader;
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

        final ParameterValue<String> param = (ParameterValue<String>) getSourceConfiguration(getSource()).parameter(FOLDER_DESCRIPTOR.getName().getCode());

        if(param == null){
            getLogger().log(Level.WARNING,"Provided File path is not defined.");
            return;
        }

        final String path = param.getValue();

        if (path == null) {
            getLogger().log(Level.WARNING,"Provided File does not exits or is not a folder.");
            return;
        }

        folder = new File(path);

        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            getLogger().log(Level.WARNING,"Provided File does not exits or is not a folder.");
            return;
        }

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
                if (isLoadAll(getSource()) || containLayer(getSource(), name)){
                    String nmsp = value(NAMESPACE_DESCRIPTOR, getSourceConfiguration(getSource()));
                    if (nmsp == null) {
                        nmsp = DEFAULT_NAMESPACE;
                    } else if ("no namespace".equals(nmsp)) {
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

        final ParameterValueGroup layer = getLayer(getSource(), name.getLocalPart());
        if(layer != null){
            Boolean isEleModel = value(LAYER_IS_ELEVATION_MODEL_DESCRIPTOR, layer);
            if(!Boolean.TRUE.equals(isEleModel)){
                return null;
            }

            final GridCoverageReaderLayerDetails pgld = (GridCoverageReaderLayerDetails) getByIdentifier(name);
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
