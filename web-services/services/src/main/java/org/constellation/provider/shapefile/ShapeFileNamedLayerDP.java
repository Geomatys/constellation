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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;

import org.constellation.provider.LayerDataProvider;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.SoftHashMap;
import org.constellation.provider.configuration.ProviderConfig;
import org.constellation.provider.configuration.ProviderSource;
import org.constellation.ws.rs.WebService;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;

import org.xml.sax.SAXException;

/**
 * Shapefile Data provider. index and cache Datastores for the shapefiles
 * whithin the given folder.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class ShapeFileNamedLayerDP implements LayerDataProvider{

    private static final String KEY_SHAPEFILE_CONFIG = "shapefile_config";
    private static final String KEY_FOLDER_PATH = "path";
    
    private static final String mask = ".shp";
    
    private final File folder;
    private final ProviderSource source;
    private final Map<String,File> index = new HashMap<String,File>();
    private final SoftHashMap<String,DataStore> cache = new SoftHashMap<String, DataStore>(20);
    
    
    private ShapeFileNamedLayerDP(final ProviderSource source) throws IllegalArgumentException {
        this.source = source;
        final String path = source.parameters.get(KEY_FOLDER_PATH);
        
        if(path == null){
            throw new IllegalArgumentException("Provided File does not exits or is not a folder.");
        }
        
        folder = new File(path);
        
        if(folder == null || !folder.exists() || !folder.isDirectory()){
            throw new IllegalArgumentException("Provided File does not exits or is not a folder.");
        }
        
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
    public Class<LayerDetails> getValueClass() {
        return LayerDetails.class;
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
    public LayerDetails get(String key) {
        DataStore store = cache.get(key);
        
        if(store == null){
            //datastore is not in the cache, try to load it
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
            final List<String> styles = source.styleLinks.get(key);
            try {
                return new ShapeFileLayerDetails(key, store.getFeatureSource(key), styles);
            } catch (IOException ex) {
                //we could not create the feature source
                Logger.getLogger(ShapeFileNamedLayerDP.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return null;
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
        
    private void test(File candidate){
        if(candidate.isFile()){
            String fullName = candidate.getName();
            if(fullName.toLowerCase().endsWith(mask)){
                String name = fullName.substring(0, fullName.length()-4);
                if(!source.ignores.contains(name)){
                    index.put(name, candidate);
                }
            }
        }
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

    public static final Collection<ShapeFileNamedLayerDP> loadProviders(){
        final Collection<ShapeFileNamedLayerDP> dps = new ArrayList<ShapeFileNamedLayerDP>();
        final ProviderConfig config;
        try {
            config = getConfig();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(ShapeFileNamedLayerDP.class.getName()).log(Level.SEVERE, null, ex);
            return Collections.emptyList();
        } catch (SAXException ex) {
            Logger.getLogger(ShapeFileNamedLayerDP.class.getName()).log(Level.SEVERE, null, ex);
            return Collections.emptyList();
        } catch (IOException ex) {
            Logger.getLogger(ShapeFileNamedLayerDP.class.getName()).log(Level.SEVERE, null, ex);
            return Collections.emptyList();
        }
        
        for(final ProviderSource ps : config.sources){
            try{
                dps.add(new ShapeFileNamedLayerDP(ps));
            }catch(IllegalArgumentException ex){
                Logger.getLogger(ShapeFileNamedLayerDP.class.toString()).log(Level.WARNING, "Invalide shapefile provider config");
            }
        }
        
        return dps;
    }
    
    /**
     * 
     * @return List of folders holding shapefiles
     */
    private static final ProviderConfig getConfig() throws ParserConfigurationException, SAXException, IOException{
        
        String configFile = "";
        try{
            configFile = WebService.getPropertyValue(JNDI_GROUP,KEY_SHAPEFILE_CONFIG);
        }catch(NamingException ex){
            Logger.getLogger(ShapeFileNamedLayerDP.class.toString()).log(Level.WARNING, "Serveur property has not be set : "+JNDI_GROUP +" - "+ KEY_SHAPEFILE_CONFIG);
        }

        if (configFile == null || configFile.trim().isEmpty()) {
            return null;
        }
        
        return ProviderConfig.read(new File(configFile.trim()));
    }
    
    
}
