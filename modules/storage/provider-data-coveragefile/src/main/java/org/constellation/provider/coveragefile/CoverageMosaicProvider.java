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

import java.util.List;
import org.opengis.parameter.ParameterValueGroup;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import org.constellation.provider.AbstractLayerProvider;
import org.constellation.provider.LayerDetails;

import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.coverage.io.GridCoverageReaders;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.image.io.mosaic.TileManager;
import org.geotoolkit.map.ElevationModel;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.util.logging.Logging;

import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import static org.constellation.provider.coveragefile.CoverageMosaicProviderService.*;
import static org.constellation.provider.configuration.ProviderParameters.*;
import static org.geotoolkit.parameter.Parameters.*;

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
                String nmsp = value(NAMESPACE_DESCRIPTOR, source);
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

    private final File folder;

    protected CoverageMosaicProvider(final CoverageMosaicProviderService service,
            final ParameterValueGroup source) throws IOException, SQLException {
        super(service,source);
        final String path = value(FOLDER_DESCRIPTOR, getSourceConfiguration(source));

        if (path == null) {
            throw new IllegalArgumentException("Found configuration file but a path parameter is not defined.");
        }

        folder = new File(path);

        if (!folder.exists()) {
            throw new IllegalArgumentException("Did not find a tile manager definition file or a mosaic directory for path: "+ path);
        }

        reload();
    }

    private static ParameterValueGroup getSourceConfiguration(final ParameterValueGroup params){

        final List<ParameterValueGroup> groups = params.groups(
                CoverageMosaicProviderService.SOURCE_DESCRIPTOR.getName().getCode());
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
        final Entry<TileManager,CoordinateReferenceSystem> entry = index.get(key);

        if (entry != null) {
            final String name = key.getLocalPart();
            final ParameterValueGroup layer = getLayer(source, name);
            final String elemodel = (layer==null)?null:value(LAYER_ELEVATION_MODEL_DESCRIPTOR, layer);
            final Name em = (layer == null || elemodel == null) ? null : DefaultName.valueOf(elemodel);
            final GridCoverageReader reader;
            try {
                reader = GridCoverageReaders.toCoverageReader(entry.getKey(), entry.getValue());
            } catch (CoverageStoreException ex) {
                getLogger().log(Level.WARNING, "Failed to load coverage reader from tile manager.", ex);
                return null;
            }
            if (layer == null) {
                return new GridCoverageReaderLayerDetails(reader,null,em, key);

            } else {
                List<String> styles = getLayerStyles(layer);
                return new GridCoverageReaderLayerDetails(reader,styles,em,key);
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
            String nmsp = value(NAMESPACE_DESCRIPTOR, getSourceConfiguration(source));
            if (nmsp == null) {
                nmsp = DEFAULT_NAMESPACE;
            } else if ("no namespace".equals(nmsp)) {
                nmsp = null;
            }

            //find the layer name
            final Name name;
            final List<ParameterValueGroup> layers = getLayers(source);
            if(!layers.isEmpty()){
                //we use the first layer name
                name = new DefaultName(nmsp, value(LAYER_NAME_DESCRIPTOR, layers.get(0)));
            }else{
                //the name is the folder name
                name = new DefaultName(nmsp, folder.getName());
            }

            try{
                final Entry<TileManager,CoordinateReferenceSystem> entry = GridCoverageReaders.openTileManager(folder);
                index.put(name, entry);
                visit(); //will log errors if any
            }catch(IOException ex){
                Logging.getLogger(CoverageMosaicProvider.class.getName()).log(Level.WARNING, "Failed to load mosaic reader.", ex);
            }catch(CoverageStoreException ex){
                Logging.getLogger(CoverageMosaicProvider.class.getName()).log(Level.WARNING, "Failed to load mosaic reader.", ex);
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
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ElevationModel getElevationModel(final Name name) {

        final ParameterValueGroup layer = getLayer(source, name.getLocalPart());
        if(layer != null){
            Boolean isEleModel = value(LAYER_IS_ELEVATION_MODEL_DESCRIPTOR, layer);
            if(!Boolean.TRUE.equals(isEleModel)){
                return null;
            }

            final GridCoverageReaderLayerDetails pgld = (GridCoverageReaderLayerDetails) getByIdentifier(name);
            if(pgld != null){
                return MapBuilder.createElevationModel(pgld.getReader());
            }
        }
        
        return null;
    }

}
