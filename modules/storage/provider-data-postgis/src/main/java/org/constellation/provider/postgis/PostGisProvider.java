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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;

import org.constellation.provider.AbstractLayerProvider;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.configuration.ConfigDirectory;
import org.constellation.provider.configuration.ProviderConfig;
import org.constellation.provider.configuration.ProviderLayer;
import org.constellation.provider.configuration.ProviderSource;

import org.geotoolkit.data.DataStore;
import org.geotoolkit.data.DataStoreFinder;
import org.geotoolkit.data.postgis.PostgisNGDataStoreFactory;
import org.geotoolkit.map.ElevationModel;
import org.geotoolkit.storage.DataStoreException;

import org.opengis.feature.type.Name;

import org.xml.sax.SAXException;


/**
 * Shapefile Data provider. index and cache Datastores for the shapefiles
 * whithin the given folder.
 *
 * @version $Id$
 *
 * @author Johann Sorel (Geomatys)
 */
public class PostGisProvider extends AbstractLayerProvider{

    private static final Logger LOGGER = Logger.getLogger("org.constellation.provider.postgis");
    private static final String KEY_POSTGIS_CONFIG  = "postgis_config";
    public static final String KEY_DBTYPE          = PostgisNGDataStoreFactory.DBTYPE.getName().toString();
    public static final String KEY_HOST            = PostgisNGDataStoreFactory.HOST.getName().toString();
    public static final String KEY_PORT            = PostgisNGDataStoreFactory.PORT.getName().toString();
    public static final String KEY_SCHEMA          = PostgisNGDataStoreFactory.SCHEMA.getName().toString();
    public static final String KEY_DATABASE        = PostgisNGDataStoreFactory.DATABASE.getName().toString();
    public static final String KEY_USER            = PostgisNGDataStoreFactory.USER.getName().toString();
    public static final String KEY_PASSWD          = PostgisNGDataStoreFactory.PASSWD.getName().toString();
    public static final String KEY_MAXCONN         = PostgisNGDataStoreFactory.MAXCONN.getName().toString();
    public static final String KEY_MINCONN         = PostgisNGDataStoreFactory.MINCONN.getName().toString();
    public static final String KEY_NAMESPACE       = PostgisNGDataStoreFactory.NAMESPACE.getName().toString();
    public static final String KEY_VALIDATECONN    = PostgisNGDataStoreFactory.VALIDATECONN.getName().toString();
//    public static final String KEY_ESTIMATEDEXTENT = PostgisNGDataStoreFactory.ESTIMATEDEXTENT.key;
    public static final String KEY_LOOSEBBOX       = PostgisNGDataStoreFactory.LOOSEBBOX.getName().toString();
//    public static final String KEY_WKBENABLED      = PostgisNGDataStoreFactory.WKBENABLED.key;

    private final Map<String,Serializable> params = new HashMap<String,Serializable>();
    private final Set<Name> index = new LinkedHashSet<Name>();
    private final DataStore store;
    private final ProviderSource source;


    protected PostGisProvider(ProviderSource source) throws DataStoreException {
        this.source = source;
        params.put(KEY_DBTYPE, "postgisng");

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
        final String namespace     = source.parameters.get(KEY_NAMESPACE);
        final String validateConnec = source.parameters.get(KEY_VALIDATECONN);
        params.put(KEY_SCHEMA, schema);
        params.put(KEY_DATABASE, database);
        params.put(KEY_USER, user);
        params.put(KEY_PASSWD, passwd);
        params.put(KEY_NAMESPACE, namespace);
        final Boolean validate = Boolean.valueOf(validateConnec);
        params.put(KEY_VALIDATECONN, validate);

        store = (DataStore) DataStoreFinder.getDataStore(params);

        if (store == null) {
            final StringBuilder sb = new StringBuilder("Could not connect to PostGIS : \n");
            for (final Map.Entry<String,Serializable> entry : params.entrySet()){
                if (entry.getKey().equals(KEY_PASSWD)){
                    sb.append(entry.getKey()).append(" : *******").append('\n');
                } else {
                    sb.append(entry.getKey()).append(" : ").append(entry.getValue()).append('\n');
                }
            }
            throw new DataStoreException(sb.toString());
        } else {
            visit();
        }
        
    }

    protected ProviderSource getSource(){
        return source;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Set<Name> getKeys() {
        return Collections.unmodifiableSet(index);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Set<Name> getKeys(String service) {
        if (source.services.contains(service) || source.services.isEmpty()) {
            return Collections.unmodifiableSet(index);
        } else {
            return Collections.emptySet();
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean contains(Name key) {
        return index.contains(key);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public LayerDetails get(Name key) {
        if (!index.contains(key)) {
            key = containsOnlyLocalPart(index, key);
            if (key == null) {
                return null;
            }
        }
        final ProviderLayer layer = source.getLayer(key.getLocalPart());
        if (layer == null) {
            return new PostGisLayerDetails(key.getLocalPart(), store, key, null, null, null, null, null);

        } else {
            final List<String> styles = layer.styles;
            return new PostGisLayerDetails(key.getLocalPart(), store, key, styles,
                    layer.dateStartField, layer.dateEndField,
                    layer.elevationStartField, layer.elevationEndField);
        }
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
            for (final Name name : store.getNames()) {
                if (source.loadAll || source.containsLayer(name.getLocalPart())) {
                    index.add(name);
                }
            }
        } catch (DataStoreException ex) {
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
            } catch(DataStoreException ex){
                LOGGER.log(Level.WARNING, "Invalide postgis provider config", ex);
            }
        }

        final StringBuilder builder = new StringBuilder("DATA PROVIDER : PostGIS ");
        for(final PostGisProvider dp : dps){
            builder.append("\n["+ dp.source.parameters.get(KEY_DATABASE)+"=");
            for(final Name layer : dp.getKeys()){
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

        final String configFile = ConfigDirectory.getPropertyValue(JNDI_GROUP,KEY_POSTGIS_CONFIG);

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
