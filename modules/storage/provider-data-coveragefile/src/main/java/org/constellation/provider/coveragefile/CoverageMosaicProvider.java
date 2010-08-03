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

import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;

import org.constellation.provider.AbstractLayerProvider;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.configuration.ProviderLayer;
import org.constellation.provider.configuration.ProviderSource;

import org.geotoolkit.coverage.grid.GridGeometry2D;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.coverage.io.ImageCoverageReader;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.image.io.XImageIO;
import org.geotoolkit.image.io.metadata.MetadataHelper;
import org.geotoolkit.image.io.metadata.SpatialMetadata;
import org.geotoolkit.image.io.mosaic.Tile;
import org.geotoolkit.map.ElevationModel;
import org.geotoolkit.map.MapBuilder;
import org.opengis.coverage.grid.RectifiedGrid;

import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;


/**
 *
 * @version $Id$
 * @author Johann Sorel (Geomatys)
 */
public class CoverageMosaicProvider extends AbstractLayerProvider{

    public static final String KEY_FOLDER_PATH = "path";
    public static final String KEY_NAMESPACE = "namespace";

    private final Map<Name,GridCoverageReader> index = new HashMap<Name,GridCoverageReader>(){
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

    protected CoverageMosaicProvider(ProviderSource source) throws IOException, SQLException {
        this.source = source;
        final String path = source.parameters.get(KEY_FOLDER_PATH);

        if (path == null) {
            throw new IllegalArgumentException("Provided File does not exits or is not a folder.");
        }

        folder = new File(path);

        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            throw new IllegalArgumentException("Provided File does not exits or is not a folder.");
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
        final GridCoverageReader reader = index.get(key);

        if (reader != null) {
            final String name = key.getLocalPart();
            final ProviderLayer layer = source.getLayer(name);
            final Name em = (layer == null || layer.elevationModel == null) ? null : DefaultName.valueOf(layer.elevationModel);
            if (layer == null) {
                return new GridCoverageReaderLayerDetails(reader,null,em, key);

            } else {
                return new GridCoverageReaderLayerDetails(reader,layer.styles,em,key);
            }
        }

        return null;
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

            //our folder is a collection of tiles for the same layer
            final Collection<Tile> tiles = new ArrayList<Tile>();
            final CoordinateReferenceSystem crs = visit(folder,tiles);

            //find the namespace
            String nmsp = source.parameters.get(KEY_NAMESPACE);
            if (nmsp == null) {
                nmsp = DEFAULT_NAMESPACE;
            } else if (nmsp.equals("no namespace")) {
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

            ImageCoverageReader reader = new ImageCoverageReader(){
                @Override
                public GridGeometry2D getGridGeometry(int index) throws CoverageStoreException {
                    //override the CRS
                    GridGeometry2D gridGeom = super.getGridGeometry(index);                    
                    gridGeom = new GridGeometry2D(gridGeom.getGridRange(), gridGeom.getGridToCRS(), crs);                    
                    return gridGeom;
                }
            };
            try {
                System.out.println(">>>>>>>>>>>>>>>> " +tiles.size() + "    " + crs );
                reader.setInput(tiles);
                index.put(name, reader);
            } catch (CoverageStoreException ex) {
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
     * Visit all files and directories contained in the directory specified.
     *
     * @param file The starting file or folder.
     */
    private CoordinateReferenceSystem visit(final File file, Collection<Tile> tiles) {

        CoordinateReferenceSystem crs = null;

        if (file.isDirectory()) {
            final File[] list = file.listFiles();
            if (list != null) {
                for (int i = 0; i < list.length; i++) {
                    CoordinateReferenceSystem ccrs = visit(list[i],tiles);
                    if(ccrs != null){
                        crs = ccrs;
                    }
                }
            }
        } else {
            CoordinateReferenceSystem ccrs = test(file,tiles);
            if(ccrs != null){
                crs = ccrs;
            }
        }

        return crs;
    }

    /**
     * @param candidate Candidate to be an coverage tile.
     */
    private CoordinateReferenceSystem test(final File candidate, Collection<Tile> tiles){
        if (candidate.isFile()){
            try {
                final ImageReader reader = XImageIO.getReader(candidate, Boolean.TRUE, Boolean.FALSE);
                final IIOMetadata metadata = reader.getImageMetadata(0);

                if(metadata instanceof SpatialMetadata){
                    final SpatialMetadata meta = (SpatialMetadata) metadata;
                    final CoordinateReferenceSystem crs = meta.getInstanceForType(CoordinateReferenceSystem.class);
                    System.out.println("++++++++++++++ CRS : " + crs);
                    final RectifiedGrid grid = meta.getInstanceForType(RectifiedGrid.class);
                    final MathTransform trs = MetadataHelper.INSTANCE.getGridToCRS(grid);

                    final Tile tile = new Tile(reader.getOriginatingProvider(), candidate, 0,null,(AffineTransform)trs);
                    tiles.add(tile);
                    return crs;
                }

            } catch (IOException ex) {
                //don't log, this can happen very often when testing non image files
            }
        }
        return null;
    }

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
