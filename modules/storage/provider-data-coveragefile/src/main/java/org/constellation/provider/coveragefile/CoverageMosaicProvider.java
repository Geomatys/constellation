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

import org.opengis.parameter.ParameterValue;

import java.util.*;

import org.opengis.parameter.ParameterValueGroup;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;

import org.constellation.provider.AbstractLayerProvider;
import org.constellation.provider.LayerDetails;
import org.geotoolkit.coverage.io.CoverageIO;

import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.map.ElevationModel;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.util.logging.Logging;

import org.opengis.feature.type.Name;

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

    private final Map<Name,GridCoverageReader> index =
            new HashMap<Name,GridCoverageReader>(){
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

    protected CoverageMosaicProvider(final CoverageMosaicProviderService service,
            final ParameterValueGroup source) throws IOException, SQLException {
        super(service,source);
        reload();
    }

    private static ParameterValueGroup getSourceConfiguration(final ParameterValueGroup params){

        final List<ParameterValueGroup> groups = params.groups(
                CoverageMosaicProviderService.SOURCE_CONFIG_DESCRIPTOR.getName().getCode());
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
        final GridCoverageReader reader = index.get(key);

        if (reader != null) {
            final String name = key.getLocalPart();
            final ParameterValueGroup layer = getLayer(getSource(), name);
            final String elemodel = (layer==null)?null:value(LAYER_ELEVATION_MODEL_DESCRIPTOR, layer);
            final Name em = (layer == null || elemodel == null) ? null : DefaultName.valueOf(elemodel);
            return new GridCoverageReaderLayerDetails(reader,null,em, key);
        }

        return null;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void reload() {

        final ParameterValue<String> param = (ParameterValue<String>) getSourceConfiguration(getSource()).parameter(FOLDER_DESCRIPTOR.getName().getCode());

        if(param == null){
            getLogger().log(Level.WARNING,"Provided File path is not defined.");
            return;
        }

        final String path = param.getValue();

        if (path == null) {
            getLogger().log(Level.WARNING, "Found configuration file but a path parameter is not defined.");
            return;
        }

        folder = new File(path);

        if (!folder.exists()) {
            getLogger().log(Level.WARNING, "Found configuration file but a path parameter is not defined.");
            return;
        }

        synchronized(this){
            index.clear();

            //find the namespace
            String nmsp = value(NAMESPACE_DESCRIPTOR, getSourceConfiguration(getSource()));
            if (nmsp == null) {
                nmsp = DEFAULT_NAMESPACE;
            } else if ("no namespace".equals(nmsp)) {
                nmsp = null;
            }

            //find the layer name
            final Name name;
            final List<ParameterValueGroup> layers = getLayers(getSource());
            if(!layers.isEmpty()){
                //we use the first layer name
                name = new DefaultName(nmsp, value(LAYER_NAME_DESCRIPTOR, layers.get(0)));
            }else{
                //the name is the folder name
                name = new DefaultName(nmsp, folder.getName());
            }

            try{
                final GridCoverageReader entry = CoverageIO.writeOrReuseMosaic(folder); //GridCoverageReaders.openTileManager(folder);
                index.put(name, entry);
                visit(); //will log errors if any
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

        final ParameterValueGroup layer = getLayer(getSource(), name.getLocalPart());
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
