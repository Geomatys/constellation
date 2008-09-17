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
package org.constellation.provider;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import org.constellation.provider.postgrid.PostGridNamedLayerDP;
import org.constellation.ws.rs.WebService;

/**
 * Main data provider for MapLayer objects. This class act as a proxy for 
 * different kind of data sources, postgrid, shapefile ...
 * 
 * @author Johann Sorel (Geomatys)
 */
public class NamedLayerDP implements LayerDataProvider<String, LayerDetails>{

    private static final String KEY_SHAPEFILE_DP = "shapefile_folder";
    
    private static NamedLayerDP instance = null;
    
    private final Collection<LayerDataProvider<String,LayerDetails>> dps = new ArrayList<LayerDataProvider<String,LayerDetails>>();
    
    
    private NamedLayerDP(){
        
        List<File> folders = getShapefileFolders();
//        for(File folder : folders){
//            ShapeFileNamedLayerDP shapeDP = new ShapeFileNamedLayerDP(folder);
//            dps.add(shapeDP);
//        }
                
        PostGridNamedLayerDP postGridDP = PostGridNamedLayerDP.getDefault();
        dps.add(postGridDP);
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
        Set<String> keys = new HashSet<String>();
        for(DataProvider<String,LayerDetails> dp : dps){
            keys.addAll( dp.getKeys() );
        }
        return keys;
    }

    /**
     * {@inheritDoc }
     */
    public boolean contains(String key) {
        for(DataProvider<String,LayerDetails> dp : dps){
            if(dp.contains(key)) return true;
        }
        return false;
    }

    /**
     * {@inheritDoc }
     */
    public LayerDetails get(String key) {
        /*MapLayer layer = null;
        for(LayerDataProvider<String,MapLayer> dp : dps){
            layer = dp.get(key);
            if(layer != null) return layer;
        }*/
        return null;
    }

    /**
     * {@inheritDoc }
     */
    public List<String> getFavoriteStyles(String layerName) {
        List<String> styles = new ArrayList<String>();
        for(LayerDataProvider<String,LayerDetails> dp : dps){
            List<String> sts = dp.getFavoriteStyles(layerName);
            styles.addAll(sts);
        }
        return styles;
    }
    
    /**
     * {@inheritDoc }
     */
    public void reload() {
        for(DataProvider<String,LayerDetails> dp : dps){
            dp.reload();
        }
    }

    /**
     * {@inheritDoc }
     */
    public void dispose() {
        for(DataProvider<String,LayerDetails> dp : dps){
            dp.dispose();
        }
        dps.clear();
    }
    
    /**
     * 
     * @return List of folders holding shapefiles
     */
    private static List<File> getShapefileFolders(){
        List<File> folders = new ArrayList<File>();
        
        String strFolders = "";
        try{
            strFolders = WebService.getPropertyValue(JNDI_GROUP,KEY_SHAPEFILE_DP);
        }catch(NamingException ex){
            Logger.getLogger(NamedStyleDP.class.toString()).log(Level.WARNING, "Serveur property has not be set : "+JNDI_GROUP +" - "+ KEY_SHAPEFILE_DP);
        }
                
        StringTokenizer token = new StringTokenizer(strFolders, ";", false);
        while(token.hasMoreElements()){
            String path = token.nextToken();
            File f = new File(path);
            if(f.exists() && f.isDirectory()){
                folders.add(f);
            }else{
                Logger.getLogger(NamedStyleDP.class.toString()).log(Level.WARNING, "Shapefile folder provided is unvalid : "+ path);
            }
        }
        
        return folders;
    }
    
    public static NamedLayerDP getInstance(){
        if(instance == null){
            instance = new NamedLayerDP();
        }
        
        return instance;
    }
}