/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2009, Geomatys
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

import org.constellation.provider.LayerProvider;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.configuration.ConfigDirectory;
import org.constellation.provider.configuration.ProviderConfig;
import org.constellation.provider.configuration.ProviderLayer;
import org.constellation.provider.configuration.ProviderSource;
import org.constellation.resources.ArraySet;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
//import org.geotools.data.postgis.PostgisDataStoreFactory;

import org.geotoolkit.map.ElevationModel;
import org.geotools.data.postgis.PostgisNGDataStoreFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import org.xml.sax.SAXException;


/**
 * Shapefile Data provider. index and cache Datastores for the shapefiles
 * whithin the given folder.
 *
 * @version $Id$
 *
 * @author Johann Sorel (Geomatys)
 */
public class PostGisProvider implements LayerProvider{
    private static final Logger LOGGER = Logger.getLogger("org.constellation.provider.postgis");
    private static final String KEY_POSTGIS_CONFIG  = "postgis_config";
    public static final String KEY_DBTYPE          = PostgisNGDataStoreFactory.DBTYPE.key;
    public static final String KEY_HOST            = PostgisNGDataStoreFactory.HOST.key;
    public static final String KEY_PORT            = PostgisNGDataStoreFactory.PORT.key;
    public static final String KEY_SCHEMA          = PostgisNGDataStoreFactory.SCHEMA.key;
    public static final String KEY_DATABASE        = PostgisNGDataStoreFactory.DATABASE.key;
    public static final String KEY_USER            = PostgisNGDataStoreFactory.USER.key;
    public static final String KEY_PASSWD          = PostgisNGDataStoreFactory.PASSWD.key;
    public static final String KEY_MAXCONN         = PostgisNGDataStoreFactory.MAXCONN.key;
    public static final String KEY_MINCONN         = PostgisNGDataStoreFactory.MINCONN.key;
    public static final String KEY_NAMESPACE       = PostgisNGDataStoreFactory.NAMESPACE.key;
    public static final String KEY_VALIDATECONN    = PostgisNGDataStoreFactory.VALIDATECONN.key;
//    public static final String KEY_ESTIMATEDEXTENT = PostgisNGDataStoreFactory.ESTIMATEDEXTENT.key;
    public static final String KEY_LOOSEBBOX       = PostgisNGDataStoreFactory.LOOSEBBOX.key;
//    public static final String KEY_WKBENABLED      = PostgisNGDataStoreFactory.WKBENABLED.key;

    private final Map<String,Object> params = new HashMap<String,Object>();
    private final List<String> index = new ArrayList<String>();
    private final DataStore store;
    private final ProviderSource source;


    protected PostGisProvider(ProviderSource source) throws IOException {
        this.source = source;
        params.put(KEY_DBTYPE, "postgis");

        // HOST ----------------------------------------------------------------
        final String host = source.parameters.get(KEY_HOST);        
        params.put(KEY_HOST, (host != null) ? host : "localhost");

        // PORT ----------------------------------------------------------------
        final String port = source.parameters.get(KEY_PORT);
        if (port != null) {
            try{
                final Integer iport = Integer.valueOf(port);
                params.put(KEY_PORT, iport);
            } catch (NumberFormatException ex) {
                //just log it, use the default port
                params.put(KEY_PORT, 5432);
                LOGGER.log(Level.SEVERE, null, ex);
            }
        } else {
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

    protected ProviderSource getSource(){
        return source;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class<String> getKeyClass() {
        return String.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class<LayerDetails> getValueClass() {
        return LayerDetails.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Set<String> getKeys() {
        return new ArraySet<String>(index);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean contains(String key) {
        return index.contains(key);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public LayerDetails get(final String key) {
        if (!index.contains(key)) {
            return null;
        }
        final ProviderLayer layer = source.getLayer(key);
        final FeatureSource<SimpleFeatureType, SimpleFeature> fs;
        try {
            fs = store.getFeatureSource(key);
        } catch (IOException ex) {
            //could not create the requested featuresource
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
        if (fs == null) {
            return null;
        }
        final List<String> styles = layer.styles;
        return new PostGisLayerDetails(key, fs, styles,
                layer.dateStartField, layer.dateEndField,
                layer.elevationStartField, layer.elevationEndField);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void reload() {
        synchronized(this){
            index.clear();
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
            params.clear();
            source.layers.clear();
            source.parameters.clear();
        }
    }

    private void visit() {
        try {
            for (final String name : store.getTypeNames()) {
                if (source.containsLayer(name)) {
                    index.add(name);
                }
            }
        } catch (IOException ex) {
            //Looks like we could not connect to the postgis database, the layers won't be indexed and the getCapability
            //won't be able to find thoses layers.
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public static final Collection<PostGisProvider> loadProviders(){
        final Collection<PostGisProvider> dps = new ArrayList<PostGisProvider>();
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
        } catch (NamingException ex) {
            return Collections.emptyList();
        }

        if (config == null) {
            return dps;
        }
        for(final ProviderSource ps : config.sources) {
            try {
                dps.add(new PostGisProvider(ps));
            } catch(IOException ex){
                LOGGER.log(Level.WARNING, "Invalide postgis provider config", ex);
            }
        }

        final StringBuilder builder = new StringBuilder("DATA PROVIDER : PostGIS ");
        for(final PostGisProvider dp : dps){
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
    private static final ProviderConfig getConfig() throws ParserConfigurationException,
            SAXException, IOException, NamingException
    {

        String configFile = ConfigDirectory.getPropertyValue(JNDI_GROUP,KEY_POSTGIS_CONFIG);

        if (configFile == null || configFile.trim().isEmpty()) {
            return null;
        }

        return ProviderConfig.read(new File(configFile.trim()));
    }

    @Override
    public ElevationModel getElevationModel(String name) {
        return null;
    }

}
