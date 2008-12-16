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
package org.constellation.security;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.FileDataStoreFactorySpi;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStoreFactory;
import org.geotools.display.exception.PortrayalException;
import org.geotools.display.service.DefaultPortrayalService;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.identity.FeatureIdImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapBuilder;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.style.MutableStyle;
import org.geotools.style.StyleFactory;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.style.Symbolizer;

/**
 * Utility class to manipulate the {@code BufferedImage} objects returned by an 
 * OGC Service.
 *
 * @author Johann Sorel (Geomatys)
 * @version $Id$
 * @since 0.3
 */
public class ImageUtilities {

    private static final MapBuilder MAP_BUILDER            = MapBuilder.getInstance();
    private static final DefaultPortrayalService PORTRAYAL = DefaultPortrayalService.getInstance();
    private static final StyleFactory STYLE_FACTORY        = CommonFactoryFinder.getStyleFactory(null);
    
    
    public static FeatureCollection<SimpleFeatureType,SimpleFeature> createClipFeatureCollection(
    		                    final CoordinateReferenceSystem dataCRS,
    		                    final double[] coords){
    	
    	//
    	final int DIMENSION = 2;//This is JTS
    	final int ordinateNumber = coords.length/DIMENSION;
    	final Coordinate [] coordArray = new Coordinate[ordinateNumber];
    	for (int i = 0; i< coords.length; i=i+2){
    		coordArray [i/2] = new Coordinate(coords[i],coords[i+1]);
    	}
        //create a JTS geometry from the given coordinates ---------------------
        final GeometryFactory geometryFactory = new GeometryFactory();
        final LinearRing ring = geometryFactory.createLinearRing(coordArray);
        final Geometry geom = geometryFactory.createPolygon(ring, new LinearRing[0]);

        //create a feature type with a single attribute for the geometry -------
        final SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.setName("defaultFeatureType");
        typeBuilder.setCRS(dataCRS);
        typeBuilder.add("geom", Polygon.class);
        final SimpleFeatureType sft = typeBuilder.buildFeatureType();

        //create a feature with our geometry -----------------------------------
        final List<Object> objects = new ArrayList<Object>();
        objects.add(geom);
        final SimpleFeature sf = new SimpleFeatureImpl(objects, sft, new FeatureIdImpl("0"));

        //create a feature collection with our feature -------------------------
        final FeatureCollection<SimpleFeatureType, SimpleFeature> features = FeatureCollections.newCollection();
        features.add(sf);
    	
        return features;
    }
    
    public static BufferedImage createMask(final ReferencedEnvelope env, final Dimension dimension,
             final CoordinateReferenceSystem dataCRS, final double[] coords) throws PortrayalException{
    	
    	final FeatureCollection<SimpleFeatureType, SimpleFeature> features = createClipFeatureCollection(dataCRS,coords);
    	
        //create the maplayer --------------------------------------------------
        final MapLayer layer = MAP_BUILDER.createFeatureLayer(features, STYLE_FACTORY.createStyle());

        return createMask(env, dimension, layer);
    }
    
    public static BufferedImage createMask(final ReferencedEnvelope env, final Dimension dimension,
            final FeatureCollection<SimpleFeatureType,SimpleFeature> clipSource) throws PortrayalException{
        MapLayer layer = MAP_BUILDER.createFeatureLayer(clipSource, STYLE_FACTORY.createStyle());
        return createMask(env, dimension, layer);
    }

    public static BufferedImage createMask(final ReferencedEnvelope env, final Dimension dimension,
            final FeatureSource<SimpleFeatureType,SimpleFeature> clipSource) throws PortrayalException{
        MapLayer layer = MAP_BUILDER.createFeatureLayer(clipSource, STYLE_FACTORY.createStyle());
        return createMask(env, dimension, layer);
    }

    public static BufferedImage createMask(final ReferencedEnvelope env, final Dimension dimension,
            final MapLayer clipSource) throws PortrayalException{

        Symbolizer symbol = STYLE_FACTORY.createPolygonSymbolizer(
                STYLE_FACTORY.createStroke(Color.BLACK, 1),
                STYLE_FACTORY.createFill(Color.BLACK),
                "geom");
        MutableStyle style = STYLE_FACTORY.createStyle(symbol);

        MapContext context = MAP_BUILDER.createContext(env.getCoordinateReferenceSystem());
        clipSource.setStyle(style);
        context.layers().add(clipSource);

        return PORTRAYAL.portray(context, env, dimension, true);

    }


    public static BufferedImage applyMask(BufferedImage source, BufferedImage mask){
        final BufferedImage maskcopy = new BufferedImage(mask.getWidth(), mask.getHeight(), BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = maskcopy.createGraphics();
        g.drawImage(mask, 0, 0, null);
        g.setComposite(AlphaComposite.SrcOut);
        g.drawImage(source, 0,0,null);
        return maskcopy;
    }

    /**
     * Returns a combined image with the given images, the img2 parameter being painted above
     * the img1.
     */
    public static BufferedImage combine(final BufferedImage img1, final BufferedImage img2){
        img1.createGraphics().drawImage(img2, 0, 0, null);
        return img1;
    }

    
//    public static DataStore readShapeFile(File shape){
//
//        Map<String,Object> params;
//        DataStore store;
//        FeatureSource<SimpleFeatureType, SimpleFeature> fs;
//        MutableStyle style;
//
//        params = new HashMap<String,Object>();
//        params.put( "url", shape.toURI().toURL() );
//        store = DataStoreFinder.getDataStore(params);
//
//        //a shapefile datastore is composed of only ONE feature source
//        //to grab it, use :
//        //store.getFeatureSource(store.getTypeNames()[0]);
//
//        return store;
//    }


    /**
     * Create a shapefile at the given url and the given features.
     *
     * @throws java.io.IOException if the datastore could not be created or could not write in the datastore.
     */
    public static DataStore createShapeFile(URL url, FeatureCollection<SimpleFeatureType,SimpleFeature> features) throws IOException{

        // Create the datastore ------------------------------------------------
        final FileDataStoreFactorySpi factory = new IndexedShapefileDataStoreFactory();

        // Create a Map object used by our DataStore Factory
        final Map<String, Serializable> map = Collections.singletonMap("url", (Serializable)url );

        // Create the ShapefileDataStore from our factory based on our Map object
        final ShapefileDataStore myData = (ShapefileDataStore) factory.createNewDataStore(map);

        final SimpleFeatureType sft = features.getSchema();

        // Create the Shapefile (empty at this point)
        myData.createSchema(sft);

        // Tell the DataStore what type of Coordinate Reference System (CRS) to use
        myData.forceSchemaCRS(sft.getCoordinateReferenceSystem());


        // Insert the features in the datastore --------------------------------
        final FeatureSource<SimpleFeatureType,SimpleFeature> source = myData.getFeatureSource(myData.getTypeNames()[0]);

        if( !(source instanceof FeatureStore)){
            throw new IOException("DataStore is not writable");
        }

        final FeatureStore store = (FeatureStore) source;

        final DefaultTransaction transaction = new DefaultTransaction();
        store.setTransaction(transaction);

        try {
            store.addFeatures(features);
            transaction.commit();
        } catch (Exception ex) {
            new IOException(ex);
            try {
                store.getTransaction().rollback();
            } catch (IOException io) {
                throw io;
            }
        }finally{
            transaction.close();
        }

        return myData;
    }

}
