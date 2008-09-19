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

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import java.util.TreeSet;
import org.constellation.catalog.CatalogException;
import org.constellation.coverage.web.Service;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.NamedStyleDP;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapLayer;
import org.geotools.map.MapLayerBuilder;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.style.MutableStyle;
import org.geotools.style.RandomStyleFactory;
import org.geotools.style.StyleFactory;
import org.geotools.util.MeasurementRange;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
class ShapefileLayerDetails implements LayerDetails{

    private static final GeographicBoundingBox DUMMY_BBOX = 
            new GeographicBoundingBoxImpl(-180, 180, -77, +77);
    
    private static final StyleFactory STYLE_FACTORY = CommonFactoryFinder.getStyleFactory(null);
    private static final RandomStyleFactory RANDOM_FACTORY = new RandomStyleFactory();
    
    private final DataStore store;
    private final List<String> favorites;
    private final String name;
    
    ShapefileLayerDetails(String name, DataStore store, List<String> favorites){
        this.name = name;
        this.store = store;
        
        if(favorites == null){
            this.favorites = Collections.emptyList();
        }else{
            this.favorites = favorites;
        }
        
    }
    
    public MapLayer getMapLayer(final Map<String, Object> params) {
        return createMapLayer(store,null);
    }

    public MapLayer getMapLayer(MutableStyle style, final Map<String, Object> params) {
        return createMapLayer(store,style);
    }

    public String getName() {
        return name;
    }

    public boolean isQueryable(Service service) {
        return true;
    }

    public GeographicBoundingBox getGeographicBoundingBox() throws CatalogException {
        //TODO handle this correctly
        try{
            final FeatureSource<SimpleFeatureType,SimpleFeature> fs = store.getFeatureSource(store.getTypeNames()[0]);
            final ReferencedEnvelope env = fs.getBounds();

            Envelope renv = null;
            if(env.getCoordinateReferenceSystem().equals(DefaultGeographicCRS.WGS84)){
                renv = CRS.transform(env, DefaultGeographicCRS.WGS84);
            }
            
            if(renv != null){
                GeographicBoundingBox bbox = new GeographicBoundingBoxImpl(renv);
                System.out.println(bbox);
                return bbox;
            }
            
        }catch(Exception e){
            e.printStackTrace();
        }
        
        return DUMMY_BBOX;
    }

    public SortedSet<Date> getAvailableTimes() throws CatalogException {
        return Collections.unmodifiableSortedSet(new TreeSet<Date>());
    }

    public SortedSet<Number> getAvailableElevations() throws CatalogException {
        return Collections.unmodifiableSortedSet(new TreeSet<Number>());
    }

    public MeasurementRange<?>[] getSampleValueRanges() {
        return new MeasurementRange<?>[0];
    }

    public String getRemarks() {
        //TODO we should get this from metadata associated to the layer.
        return "Vector datas";
    }

    public String getThematic() {
        //TODO we should get this from metadata associated to the layer.
        return "Vector datas";
    }

    private MapLayer createMapLayer(DataStore store, MutableStyle style){
        MapLayer layer = null;
        
        FeatureSource<SimpleFeatureType,SimpleFeature> fs = null;
                
        try{
            fs = store.getFeatureSource(store.getTypeNames()[0]);
        }catch(IOException ex){
            //TODO log error
            ex.printStackTrace();
        }
        
        if(fs != null){
            
            if(style == null){
                //no style provided try to get the favorite one
                if(favorites.size() > 0){
                    String favorite = favorites.get(0);
                    style = NamedStyleDP.getInstance().get(favorite);
                }
                
                if(style == null){
                    //could not load a favorite style, create a random one
                    style = RANDOM_FACTORY.createRandomVectorStyle(fs);
                }
                
            }
            
            layer = new MapLayerBuilder().create(fs, style);
        }else{
            System.err.println(ShapeFileNamedLayerDP.class +" Error : Could not create shapefile maplayer.");
            //TODO log error
        }
        
        return layer;
    }

    public BufferedImage getLegendGraphic(Dimension dimension) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
