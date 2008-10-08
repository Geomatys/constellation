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
package org.constellation.provider.postgis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;
import org.constellation.provider.LayerDataProvider;
import org.constellation.provider.LayerDetails;

import org.constellation.provider.configuration.ProviderConfig;
import org.constellation.provider.configuration.ProviderSource;
import org.constellation.resources.ArraySet;
import org.constellation.ws.rs.WebService;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.postgis.PostgisDataStoreFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.xml.sax.SAXException;


/**
 * Shapefile Data provider. index and cache Datastores for the shapefiles
 * whithin the given folder.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class PostGisNamedLayerDP implements LayerDataProvider{
    private static final Logger LOGGER = Logger.getLogger("org.constellation.provider.postgis");
    private static final String KEY_POSTGIS_CONFIG  = "postgis_config";
    private static final String KEY_HOST            = PostgisDataStoreFactory.HOST.key;
    private static final String KEY_PORT            = PostgisDataStoreFactory.PORT.key;
    private static final String KEY_SCHEMA          = PostgisDataStoreFactory.SCHEMA.key;
    private static final String KEY_DATABASE        = PostgisDataStoreFactory.DATABASE.key;
    private static final String KEY_USER            = PostgisDataStoreFactory.USER.key;
    private static final String KEY_PASSWD          = PostgisDataStoreFactory.PASSWD.key;
    private static final String KEY_MAXCONN         = PostgisDataStoreFactory.MAXCONN.key;
    private static final String KEY_MINCONN         = PostgisDataStoreFactory.MINCONN.key;
    private static final String KEY_NAMESPACE       = PostgisDataStoreFactory.NAMESPACE.key;
    private static final String KEY_VALIDATECONN    = PostgisDataStoreFactory.VALIDATECONN.key;
    private static final String KEY_ESTIMATEDEXTENT = PostgisDataStoreFactory.ESTIMATEDEXTENT.key;
    private static final String KEY_LOOSEBBOX       = PostgisDataStoreFactory.LOOSEBBOX.key;
    private static final String KEY_WKBENABLED      = PostgisDataStoreFactory.WKBENABLED.key;
            
    private final Map<String,Object> params = new HashMap<String,Object>();
    private final DataStore store;
    private final ProviderSource source;
    private final List<String> index = new ArrayList<String>();
    
    
    private PostGisNamedLayerDP(ProviderSource source) throws IllegalArgumentException, IOException{
        this.source = source;
        
        params.put("dbtype", "postgis");
        
        // HOST ----------------------------------------------------------------
        final String host = source.parameters.get(KEY_HOST);
        if(host != null){
            params.put(KEY_HOST, host);
        }else{
            params.put(KEY_HOST, "localhost");
        }
        
        // PORT ----------------------------------------------------------------
        final String port = source.parameters.get(KEY_PORT);
        if(port != null){
            try{
                final Integer iport = Integer.valueOf(port);
                params.put(KEY_PORT, iport);
            }catch(NumberFormatException ex){
                //just log it, use the default port
                params.put(KEY_PORT, 5432);
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }else{
            //this parameter is needed
            params.put(KEY_PORT, 5432);
        }
        
        // OTHERS --------------------------------------------------------------
        final String schema = source.parameters.get(KEY_SCHEMA);
        final String database = source.parameters.get(KEY_DATABASE);
        final String user       = source.parameters.get(KEY_USER);
        final String passwd     = source.parameters.get(KEY_PASSWD);
        params.put(KEY_SCHEMA, schema);
        params.put(KEY_DATABASE, database);
        params.put(KEY_USER, user);
        params.put(KEY_PASSWD, passwd);
        
        //TODO Handle thoses when we think it is necessary
//            final Integer maxconn    = source.parameters.get(KEY_MAXCONN);
//            final Integer minconn    = source.parameters.get(KEY_MINCONN);
//            final String namespace  = source.parameters.get(KEY_NAMESPACE);
//            final Boolean validate   = source.parameters.get(KEY_VALIDATECONN);
//            final Boolean estimated  = source.parameters.get(KEY_ESTIMATEDEXTENT);
//            final Boolean bbox       = source.parameters.get(KEY_LOOSEBBOX);
//            final Boolean wkb        = source.parameters.get(KEY_WKBENABLED);
        
        store = DataStoreFinder.getDataStore(params);
        visit();
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
        return new ArraySet<String>(index);
    }

    /**
     * {@inheritDoc }
     */
    public boolean contains(String key) {
        return index.contains(key);
    }

    /**
     * {@inheritDoc }
     */
    public LayerDetails get(String key) {
        if(index.contains(key)){
            FeatureSource<SimpleFeatureType,SimpleFeature> fs = null;

            try {
                fs = store.getFeatureSource(key);
            } catch (IOException ex) {
                //could not create the requested featuresource
                LOGGER.log(Level.SEVERE, null, ex);
            }

            if(fs != null){
                final List<String> styles = source.layers.get(key);
                return new PostGisLayerDetails(key, fs, styles);
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
            visit();
        }
    }

    /**
     * {@inheritDoc }
     */
    public void dispose() {
        synchronized(this){
            index.clear();
        }
    }
    
    private void visit() {
        try {
            for (final String name : store.getTypeNames()) {
                if (source.layers.containsKey(name)) {
                    index.add(name);
                }
            }
        } catch (IOException ex) {
            //Looks like we could not connect to the postgis database, the layers won't be indexed and the getCapability
            //won't be able to find thoses layers.
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    public static final Collection<PostGisNamedLayerDP> loadProviders(){
        final Collection<PostGisNamedLayerDP> dps = new ArrayList<PostGisNamedLayerDP>();
        final ProviderConfig config;
        try {
            config = getConfig();
        } catch (ParserConfigurationException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return Collections.emptyList();
        } catch (SAXException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return Collections.emptyList();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return Collections.emptyList();
        }

        if (config == null) {
            return dps;
        }
        for(final ProviderSource ps : config.sources){
            try{
                dps.add(new PostGisNamedLayerDP(ps));
            }catch(IllegalArgumentException ex){
                LOGGER.log(Level.WARNING, "Invalide postgis provider config", ex);
            }catch(IOException ex){
                LOGGER.log(Level.WARNING, "Invalide postgis provider config", ex);
            }
        }
        
        final StringBuilder builder = new StringBuilder("DATA PROVIDER : PostGIS ");
        for(final PostGisNamedLayerDP dp : dps){
            builder.append("\n["+ dp.source.parameters.get(KEY_DATABASE)+"=");
            for(final String layer : dp.getKeys()){
                builder.append(layer + ",");
            }
            builder.append("]");
        }
        LOGGER.log(Level.INFO, builder.toString());
        
        return dps;
    }
    
    /**
     * 
     * @return List of folders holding shapefiles
     */
    private static final ProviderConfig getConfig() throws ParserConfigurationException, SAXException, IOException{
        
        String configFile = "";
        try{
            configFile = WebService.getPropertyValue(JNDI_GROUP,KEY_POSTGIS_CONFIG);
        }catch(NamingException ex){
            LOGGER.log(Level.WARNING, "Serveur property has not be set : "+JNDI_GROUP +" - "+ KEY_POSTGIS_CONFIG, ex);
        }

        if (configFile == null || configFile.trim().isEmpty()) {
            return null;
        }
        
        return ProviderConfig.read(new File(configFile.trim()));
    }
    
}
