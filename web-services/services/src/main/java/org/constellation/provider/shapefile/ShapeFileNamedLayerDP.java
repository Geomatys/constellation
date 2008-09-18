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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.constellation.provider.LayerDataProvider;
import org.constellation.provider.SoftHashMap;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.map.MapLayer;
import org.geotools.map.MapLayerBuilder;
import org.geotools.style.MutableStyle;
import org.geotools.style.RandomStyleFactory;
import org.geotools.style.StyleFactory;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Shapefile Data provider. index and cache Datastores for the shapefiles
 * whithin the given folder.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class ShapeFileNamedLayerDP implements LayerDataProvider<String,MapLayer>{

    private static final String FILE_LAYERLINKS = "layerlinks.xml";
    private static ShapeFileNamedLayerDP instance = null;
    
    private static final StyleFactory STYLE_FACTORY = CommonFactoryFinder.getStyleFactory(null);
    private static final RandomStyleFactory RANDOM_FACTORY = new RandomStyleFactory();
    private static final String mask = ".shp";
    
    private final File folder;
    private final Map<String,File> index = new HashMap<String,File>();
    private final Map<String,List<String>> favorites = new  HashMap<String, List<String>>();
    private final SoftHashMap<String,DataStore> cache = new SoftHashMap<String, DataStore>(20);
    
    
    public ShapeFileNamedLayerDP(File folder){
        if(folder == null || !folder.exists() || !folder.isDirectory()){
            throw new IllegalArgumentException("Provided File does not exits or is not a folder.");
        }
        
        this.folder = folder;
        visit(folder);
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
    public Class<MapLayer> getValueClass() {
        return MapLayer.class;
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
    public MapLayer get(String key) {
        MapLayer layer = null;
        
        DataStore store = cache.get(key);
        
        if(store == null){
            File f = index.get(key);
            if(f != null){
                //we have this data source in the folder
                store = loadDataStore(f);
                if(store != null){
                    //cache the datastore
                    cache.put(key, store);
                }
            }
        }
        
        if(store != null){
            //DataStore is in cache, reuse it.
            layer = createMapLayer(store,null);
        }
        
        return layer;
    }
    
    /**
     * {@inheritDoc }
     */
    public MapLayer get(String key, MutableStyle style) {
        MapLayer layer = null;
        
        DataStore store = cache.get(key);
        
        if(store == null){
            File f = index.get(key);
            if(f != null){
                //we have this data source in the folder
                store = loadDataStore(f);
                if(store != null){
                    //cache the datastore
                    cache.put(key, store);
                }
            }
        }
        
        if(store != null){
            //DataStore is in cache, reuse it.
            layer = createMapLayer(store,style);
        }
        
        return layer;
    }
    
    /**
     * {@inheritDoc }
     */
    public List<String> getFavoriteStyles(String layerName) {
        List<String> favs = favorites.get(layerName);
        if(favs == null){
            favs = Collections.emptyList();
        }
        return favs;
    }
    
    /**
     * {@inheritDoc }
     */
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
    public void dispose() {
        synchronized(this){
            index.clear();
            cache.clear();
        }
    }

    private void visit(File file) {

        if (file.isDirectory()) {
            findLayerLinks(file);
            File[] list = file.listFiles();
            if (list != null) {
                for (int i = 0; i < list.length; i++) {
                    visit(list[i]);
                }
            }
        }else{
            test(file);
        }
    }
    
    private void findLayerLinks(File folder) {
        String path = folder.getPath();
        
        //append the end slash
        if(!path.endsWith(File.separator)) path += File.separator;
        path += FILE_LAYERLINKS;
        
        File candidate = new File(path);
        if(candidate.exists()){
            
            Map<String,String> links = new HashMap<String, String>();
            try {
                links = extract(links, candidate);
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(ShapeFileNamedLayerDP.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SAXException ex) {
                Logger.getLogger(ShapeFileNamedLayerDP.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ShapeFileNamedLayerDP.class.getName()).log(Level.SEVERE, null, ex);
            }
            Set<String> keys = links.keySet();
            for(String key : keys){
                String strStyles = links.get(key);
                List<String> styles = parseStyles(strStyles);
                favorites.put(key, styles);
            }
            
        }
        
    }
    
    private Map<String,String> extract(Map<String,String>links,File f) throws ParserConfigurationException, SAXException, IOException{
        
        DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();
        DocumentBuilder constructeur = fabrique.newDocumentBuilder();

        Document document = constructeur.parse(f);
        
        Element racine = document.getDocumentElement();
        String tag = "Link";
        NodeList liste = racine.getElementsByTagName(tag);
        for(int i=0, n=liste.getLength(); i<n; i++){
            Element link = (Element)liste.item(i);
            if(link.hasAttribute("LayerName")){
                String layer = link.getAttribute("LayerName");
                String styles = link.getTextContent();
                if(layer != null && styles != null){
                    links.put(layer, styles);
                }
            }
                    
        }
        
        return links;
    }
    
    
    private void test(File candidate){
        if(candidate.isFile()){
            String fullName = candidate.getName();
            if(fullName.toLowerCase().endsWith(mask)){
                String name = fullName.substring(0, fullName.length()-4);
                index.put(name, candidate);
            }
        }
    }
    
    private List<String> parseStyles(String strStyles) {
        if(strStyles == null || strStyles.trim().isEmpty()){
            return Collections.emptyList();
        }
        List<String> styles = new ArrayList<String>();
        StringTokenizer token = new StringTokenizer(strStyles.trim(),";",false);
        while(token.hasMoreTokens()){
            styles.add(token.nextToken());
        }
        return styles;
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
                style = RANDOM_FACTORY.createRandomVectorStyle(fs);
            }
            
            layer = new MapLayerBuilder().create(fs, style);
        }else{
            System.err.println(ShapeFileNamedLayerDP.class +" Error : Could not create shapefile maplayer.");
            //TODO log error
        }
        
        return layer;
    }
    
    private DataStore loadDataStore(File f){
        DataStore store = null;
        
        if(f.exists()){
            Map<String,Object> params = new HashMap<String,Object>();
            
            try{
                params.put( "url", f.toURI().toURL() );
                store = DataStoreFinder.getDataStore(params);
            }catch(IOException ex){
                ex.printStackTrace();
                //TODO log error
            }
        }else{
            System.err.println(ShapeFileNamedLayerDP.class +" Error : Could not create shapefile datastore. File does not exits.");
            //TODO log error
        }
        
        return store;
    }

    
}
